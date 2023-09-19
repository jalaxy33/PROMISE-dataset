package net.sourceforge.pbeans.data;

import java.util.*;
import java.sql.*;
import javax.sql.*;
import java.io.*;

/**
 * Base implementation of Database.
 * This class implements a considerable amount of common functionalilty
 * in order to allow straight-forward Database implementations
 * by simply extending this class and overriding a few methods.
 */
public abstract class AbstractDatabase implements Database {
    private int maxTableNameLength;
    private int maxColumnNameLength;
    private int sqlStateType;

    private final DataSource source;
    private final boolean logEnabled;
    private final Map typesByTypeID = new HashMap();
    private final Map locks = new HashMap();
    private final Set keywords = new HashSet();
    private final ConnectionPool pool;
    
    private boolean deleteFields = false;

    private static final String SQL92_KEYWORDS = 

	"ABSOLUTE ACTION ADD ADMIN AFTER ALIAS ALL ALLOCATE ALTER AND ANY ARE AS ASC ASSERTION AT " +
	"AUTHORIZATION AVG BEFORE BEGIN BETWEEN BIT BIT_LENGTH BOOLEAN BOTH BREADTH BY CASCADE CASCADED " + 
	"CASE CAST CATALOG CHAR_LENGTH CHAR CHARACTER_LENGTH CHARACTER CHECK CLASS CLOSE COALESCE  " +
	"COBOL COLLATE COLLATION COLUMN COMMIT COMPLETION CONNECT CONNECTION CONSTRAINT CONSTRAINTS  " +
	"CONSTRUCTOR CONTINUE CONVERT CORRESPONDING COUNT CREATE CROSS CURRENT_DATE CURRENT_TIME  " +
	"CURRENT_TIMESTAMP CURRENT_USER CURRENT CURSOR CYCLE DATA DATE DAY DEALLOCATE DEC DECIMAL  " +
	"DECLARE DEFAULT DEFERRABLE DEFERRED DELETE DEPTH DEREF DESC DESCRIBE DESCRIPTOR DESTROY  " +
	"DESTRUCTOR DIAGNOSTICS DICTIONARY DISCONNECT DISTINCT DO DOMAIN DOUBLE DROP EACH ELEMENT  " +
	"ELSE ELSEIF END-EXEC END EQUALS ESCAPE EXCEPT EXCEPTION EXEC EXECUTE EXISTS EXTERNAL  " +
	"EXTRACT FALSE FETCH FIRST FLOAT FOR FOREIGN FORTRAN FOUND FROM FULL FUNCTION GENERAL  " +
	"GET GLOBAL GO GOTO GRANT GROUP HAVING HOUR IDENTITY IF IGNORE IMMEDIATE IN INDICATOR  " +
	"INITIALLY INNER INOUT INPUT INSENSITIVE INSERT INSTEAD INT INTEGER INTERSECT INTERVAL  " +
	"INTO IS ISOLATION JOIN KEY LANGUAGE LAST LEADING LEAVE LEFT LESS LEVEL LIKE LIMIT " + 
	"LOCAL LOOP LOWER MATCH MAX MIN MINUTE MODIFY MODULE MONTH MOVE MULTISET NAMES NATIONAL " + 
	"NATURAL NCHAR NEW_TABLE NEXT NO NONE NOT NULL NULLIF NUMERIC OCTET_LENGTH OF OFF OID  " +
	"OLD OLD_TABLE ON ONLY OPEN OPERATION OPERATORS OPTION OR ORDER OTHERS OUT OUTER OUTPUT " + 
	"OVERLAPS PAD PARAMETER PARTIAL PASCAL PENDANT PLI POSITION PRECISION PREORDER PREPARE  " +
	"PRESERVE PRIMARY PRIOR PRIVATE PRIVILEGES PROCEDURE PROTECTED PUBLIC READ REAL RECURSIVE  " +
	"REF REFERENCES REFERENCING RELATIVE REPRESENTATION RESIGNAL RESTRICT RETURN RETURNS REVOKE  " +
	"RIGHT ROLE ROLLBACK ROUTINE ROW SAVEPOINT SCHEMA SCROLL SEARCH SECOND SECTION SELECT  " +
	"SENSITIVE SEQUENCE SESSION SESSION_USER SET SIGNAL SIMILAR SIZE SMALLINT SOME SPACE  " +
	"SPECIFIC SQL SQLCODE SQLERROR SQLEXCEPTION SQLSTATE SQLWARNING STRUCTURE SUBSTRING SUM  " +
	"SYSTEM_USER TABLE TEMPLATE TEMPORARY TEST THAN THEN THERE TIME TIMESTAMP TIMEZONE_HOUR  " +
	"TIMEZONE_MINUTE TO TRAILING TRANSACTION TRANSLATE TRANSLATION TRIGGER TRIM TRUE TUPLE  " +
	"TYPE UNDER UNION UNIQUE UNKNOWN UPDATE UPPER USAGE USING VALUE VALUES VARCHAR VARIABLE  " +
	"VARIANT VIRTUAL VARYING VIEW VISIBLE WAIT WHEN WHENEVER WHERE WHILE WITH WITHOUT WORK  " +
	"WRITE YEAR ZONE";

    /**
     * Constructs an AbstractDatabase. 
     * @param source A DataSource which may be an instance of GenericDataSource.
     *               There is no need to pass a pooled DataSource, since AbstractDatabase
     *               implements its own connection pool.
     * @see GenericDataSource
     */
    public AbstractDatabase (DataSource source) throws SQLException {
        PrintWriter lw = null;
        try {
            lw = source.getLogWriter();
        } catch (UnsupportedOperationException uo) {
            lw = null;
        }
        this.logEnabled = lw != null;
	this.source = source;
	this.pool = new ConnectionPool (source, 100, 10 * 60 * 1000);
	this.initTypeInfo();
    }

    /**
     * Sets the maximum number of simultaneous open connections.
     */
    public void setMaxConnections (int maxConnections) {
	this.pool.setMaxConnections (maxConnections);
    }

    /**
     * Sets the amount of time, in milliseconds, that a
     * pooled connection may be idle before it is discarded.
     */
    public void setConnectionTimeout (int timeout) {
	this.pool.setConnectionTimeout (timeout);
    }

    /**
     * Tells this AbstractDatabase whether it should delete
     * fields no longer requested in a call to <code>ensureTableExists</code>.
     */
    public void setDeleteFields (boolean value) {
	this.deleteFields = value;
    }

    public final int getMaxTableNameLength() throws SQLException {
	return this.maxTableNameLength;
    }

    public final int getMaxColumnNameLength() throws SQLException {
	return this.maxColumnNameLength;
    }

    private void initTypeInfo() throws SQLException {
	ConnectionPool.ConnectionWrapper cw = this.pool.getConnectionWrapper();
	try {
	    Connection c = cw.getConnection();
	    DatabaseMetaData dmd = c.getMetaData();
	    this.maxTableNameLength = dmd.getMaxTableNameLength();
	    this.maxColumnNameLength = dmd.getMaxColumnNameLength();
            try {
                this.sqlStateType = dmd.getSQLStateType();
            } catch (UnsupportedOperationException uoe) {
                this.sqlStateType = 0;
            }
	    ResultSet rs;
	    
	    // Get type information
	    rs = dmd.getTypeInfo();
	    try {
		while (rs.next()) {
		    String typeName = rs.getString("TYPE_NAME");
		    int dataType = rs.getInt("DATA_TYPE");
                    int precision = rs.getInt("PRECISION");
		    Integer dtInt = new Integer(dataType);
		    if (this.typesByTypeID.get (dtInt) == null) {
			this.typesByTypeID.put (dtInt, new TypeInfo(dataType,typeName,precision));
		    }
		    if (isLogEnabled()) {
			info ("initTypeInfo(): for type=" + dataType + " typeName=" + typeName + " precision=" + precision);
		    }
		}
	    } finally {
		rs.close();
	    }

	    // Get keyword information
	    String keywords = dmd.getSQLKeywords() + " " + SQL92_KEYWORDS;
	    StringTokenizer tok = new StringTokenizer(keywords, ", ");
	    while (tok.hasMoreTokens()) {
		String token = tok.nextToken();
		this.keywords.add (token.toLowerCase());
	    }
	} finally {
	    cw.release();
	    info ("initTypeInfo(): DONE");
	}
    }

    /**
     * Gets the type declaration for a type constant as
     * defined in java.sql.Types.
     */
    protected String getTypeDeclaration (int dataType) {
	if (dataType == Types.BLOB) {
	    return "BLOB";
	}
	TypeInfo ti = (TypeInfo) this.typesByTypeID.get (new Integer(dataType));
	if (ti == null) {
	    throw new IllegalArgumentException ("Database is not aware of Java java.sql.Types constant " + dataType);
	}
	return ti.getDeclaration();
    }

    /**
     * Determines if the SQLException given corresponds
     * to a duplicate record error (such as one given when
     * you try to insert a record with a unique index that
     * already exists.)
     */
    public abstract boolean isDuplicateEntryError (SQLException se);

    /**
     * Creates a table if necessary. First, it makes sure field names don't match
     * any reserved database keywords. Then, it removes table fields not found in
     * <code>fieldDescriptors</code>. It adds any new fields, and changes types
     * of existing fields if needed. Finally, it modifies and creates requested indexes.
     */
    public void ensureTableExists (String tableName, FieldDescriptor mandatoryField, Collection fieldDescriptors, Collection indexDescriptors) throws SQLException {
	Collection fdToChange = new LinkedList();
	Collection fdChecked = new HashSet();
	Collection fieldsToRemove = new LinkedList();
	Map fdMap = new HashMap();
	fdMap.put (mandatoryField.getName(), mandatoryField);
	Iterator fdI = fieldDescriptors.iterator();
	while (fdI.hasNext()) {
	    FieldDescriptor fd = (FieldDescriptor) fdI.next();
	    String name = fd.getName();
	    if (this.keywords.contains (name.toLowerCase())) {
		throw new IllegalStateException ("Name of field '" + name + "' requested for table " + tableName + " matches database keyword.");
	    }
	    fdMap.put (name, fd);
	}
	info ("ensureTableExists(): Getting connection...");
	ConnectionPool.ConnectionWrapper cw = this.pool.getConnectionWrapper();
	try {
	    Connection c = cw.getConnection();
	    DatabaseMetaData dmd = c.getMetaData();
	    info ("ensureTableExists(): Querying columns...");
	    ResultSet rs = dmd.getColumns (c.getCatalog(), "%", tableName, "%");
	    int numColumns = 0;
	    try {
		while (rs.next()) {
		    numColumns++;
		    String fieldName = rs.getString("COLUMN_NAME");
		    int fieldType = rs.getInt("DATA_TYPE");
		    FieldDescriptor fd = (FieldDescriptor) fdMap.get(fieldName);
		    if (fd != null) {
			fdChecked.add (fd);
			if (!typesMatch (fd.getSqlType(), fieldType)) {
                            info ("ensureTableExists(): Will change field " + fd.getName() + " because types didn't match: newType=" + fd.getSqlType() + " oldType=" + fieldType);
			    fdToChange.add (fd);
			}
		    }
		    else {
			fieldsToRemove.add (fieldName);
		    }
		}
	    } finally {
		rs.close();
	    }
	    if (numColumns == 0) {
		
		PreparedStatement createPs = getCreateTableStatement (c, tableName, mandatoryField);
		if (this.logEnabled) {
		    info ("ensureTableExists(): Because metadata gave no columns for table, will create table " + tableName + " by executing: " + createPs);
		}
		createPs.execute();
	    }
	    // Check which fields need to be added
	    Iterator checkI = fieldDescriptors.iterator();
	    while (checkI.hasNext()) {
		FieldDescriptor fd = (FieldDescriptor) checkI.next();
		if (!fdChecked.contains (fd)) {
                    // Add field, but first check renames
                    String renamedFieldName = null;
                    String[] renamedFrom = fd.getRenamedFrom();
                    if (renamedFrom != null) {
                        for (int i = 0; i < renamedFrom.length; i++) {
                            if (fieldsToRemove.contains (renamedFrom[i])) {
                                renamedFieldName = renamedFrom[i];
                                fieldsToRemove.remove (renamedFieldName);
                                break;
                            }
                        }
                    }
                    if (renamedFieldName != null) {
                        PreparedStatement[] ps = getRenameFieldStatements (c, tableName, renamedFieldName, fd);
                        if (this.logEnabled) {
                            info ("ensureTableExists(): Renaming field from " + renamedFieldName + " to " + fd.getName() + " by executing: " + ps);
                        }
                        for (int i = 0; i < ps.length; i++) {
                            try {
                                ps[i].execute();
                            } catch (SQLException se) {
                                SQLException nse = new SQLException ("Unable to rename field from " + renamedFieldName + " to " + fd.getName() + ". Statement: " + ps + ". Message: " + se.getMessage(), se.getSQLState(), se.getErrorCode());
                                nse.setNextException (se);
                                throw nse;
                            }                        
                        }
                    }
                    else {
                        // Actually add
                        PreparedStatement ps = getCreateFieldStatement (c, tableName, fd);
                        if (this.logEnabled) {
                            info ("ensureTableExists(): Adding field by executing: " + ps);
                        }
                        try {
                            ps.execute();
                        } catch (SQLException se) {
                            SQLException nse = new SQLException ("Unable to addfield " + fd.getName() + ". Statement: " + ps + ". Reason: " + se.getMessage(), se.getSQLState(), se.getErrorCode());
                            nse.setNextException (se);
                            throw nse;
                        }
                    }
                }
	    }

	    // Remove old fields (make sure to do it after adding
            // and renaming.)
	    if (this.deleteFields) {
		// TBD: If not deleting, perhaps we should ensure they are nullable.
		Iterator removeI = fieldsToRemove.iterator();
		while (removeI.hasNext()) {
		    String name = (String) removeI.next();
		    PreparedStatement ps = getRemoveFieldStatement (c, tableName, name);
		    if (this.logEnabled) {
			info ("ensureTableExists(): Dropping field by executing: " + ps);
		    }
		    try {
			ps.execute();
		    } catch (SQLException se) {
			warn ("ensureTableExists(): Unable to drop field " + name + ": " + se.getMessage());
		    }
		}
	    }

	    // Change types of existing fields
	    Iterator changeI = fdToChange.iterator();
	    while (changeI.hasNext()) {
		FieldDescriptor fd = (FieldDescriptor) changeI.next();
		PreparedStatement[] ps = getChangeFieldStatements (c, tableName, fd);
		for (int i = 0; i < ps.length; i++) {
		    if (this.logEnabled) {
			info ("ensureTableExists(): Changing field " + fd.getName() + " by executing: " + ps[i]);
		    }
		    ps[i].execute();
		}
	    }		
	    // Get exising index information
	    Map oldIndexes = new HashMap();
	    ResultSet irs = dmd.getIndexInfo (c.getCatalog(), "%", tableName, false, false);
            boolean foundIndexes = false;
	    try {
		while (irs.next()) {
                    foundIndexes = true;
		    String name = irs.getString("INDEX_NAME");
		    String column = irs.getString("COLUMN_NAME");
		    boolean unique = !irs.getBoolean("NON_UNIQUE");
		    IndexDescriptor id = (IndexDescriptor) oldIndexes.get(name);
		    if (id == null) {
			id = new IndexDescriptor (name, unique, column);
			oldIndexes.put (name, id);
		    }
		    else {
			id.addFieldName (column);
		    }			
		}
	    } finally {
		irs.close();
	    }
            boolean noGetIndexInfo = !foundIndexes && numColumns > 0;
            if (noGetIndexInfo) {
                warn ("ensureTableExists(): Did not find any indexes, which indicates the JDBC driver does not properly support getIndexInfo(). Will have to drop indexes before adding them.");
            }
	    // TBD: Remove indexes no longer available
	    // Create indexes
	    Iterator indexI = indexDescriptors.iterator();
	    while (indexI.hasNext()) {
		IndexDescriptor id = (IndexDescriptor) indexI.next();
		IndexDescriptor oldId = noGetIndexInfo ? id : (IndexDescriptor) oldIndexes.get(id.getName());
		if (oldId != null) {
		    if (oldId.equals (id) && !noGetIndexInfo) {
                        // Leave index as is and check next.
			continue;
		    }
		    PreparedStatement[] dps = getDropIndexStatements (c, tableName, oldId.getName());
		    for (int i = 0; i < dps.length; i++) {
			if (this.logEnabled) {
			    info ("ensureTableExists(): In order to drop index named " + oldId.getName() + " executing: " + dps[i]);
			}
                        try {
                            dps[i].execute();
                        } catch (SQLException se) {
                            if (noGetIndexInfo) {
                                warn ("Assuming index " + oldId.getName() + " does not exist because we're unable to drop it: " + se.getMessage());
                            }
                            else {
                                throw se;
                            }
                        }
		    }
		}
		PreparedStatement[] ps = getCreateIndexStatements (c, tableName, id);
		for (int i = 0; i < ps.length; i++) {
		    if (this.logEnabled) {
			info ("ensureTableExists(): Executing: " + ps[i]);
		    }
		    ps[i].execute();
		}
	    }
	} finally {
	    cw.release();
	}
    }

    protected boolean typesMatch (int type1, int type2) {
        if (type1 == type2) {
            return true;
        }
        if (isNumeric(type1) && isNumeric(type2)) {
            return true;
        }
        if (isDecimal(type1) && isDecimal(type2)) {
            return true;
        }
	if (isBlob(type1) && isBlob(type2)) {
	    return true;
	}
        return false;
    }

    private boolean isNumeric (int type) {
        return type == Types.INTEGER || type == Types.NUMERIC;
    }

    private boolean isDecimal (int type) {
        return type == Types.DECIMAL || type == Types.DOUBLE || type == Types.FLOAT || type == Types.REAL;
    }

    private boolean isBlob (int type) {
	return type == Types.BLOB || type == Types.VARBINARY || type == Types.BINARY;
    }

    /**
     * Gets a Statement that creates a table.
     */
    protected PreparedStatement getCreateTableStatement (Connection c, String tableName, FieldDescriptor fd) throws SQLException {
	return c.prepareStatement ("CREATE TABLE " + tableName + " (" + fd.getName() + " " + getTypeDeclaration(fd.getSqlType()) + (fd.isNullable() ? " NULL" : " NOT NULL") + ")");
    }

    /**
     * Gets a Statement that removes a field.
     */
    protected PreparedStatement getRemoveFieldStatement (Connection c, String tableName, String columnName) throws SQLException {
	PreparedStatement ps = c.prepareStatement ("ALTER TABLE " + tableName + " DROP " + columnName);
	return ps;
    }

    /**
     * Gets a Statement that adds a field to a table.
     */
    protected PreparedStatement getCreateFieldStatement (Connection c, String tableName, FieldDescriptor fd) throws SQLException {
	PreparedStatement ps = c.prepareStatement ("ALTER TABLE " + tableName + " ADD " + fd.getName() + " " + getTypeDeclaration(fd.getSqlType()));
	return ps;
    }

    /**
     * Gets a sequence of Statements that can modify a field's type
     * and whether it's nullable. 
     */
    protected abstract PreparedStatement[] getChangeFieldStatements (Connection c, String tableName, FieldDescriptor fd) throws SQLException;

    /**
     * Gets a sequence of Statements that can rename a field. The statements should change the field's type and whether the field is nullable.
     */
    protected abstract PreparedStatement[] getRenameFieldStatements (Connection c, String tableName, String oldName, FieldDescriptor fd) throws SQLException;

    /**
     * Gets a sequence of Statements that create indexes.
     */
    protected PreparedStatement[] getCreateIndexStatements (Connection c, String tableName, IndexDescriptor id) throws SQLException  {
	Collection fieldNames = id.getFieldNames();
	StringBuffer sb = new StringBuffer();
	sb.append ("CREATE ");
	sb.append (id.isUnique() ? "UNIQUE " : "");
	sb.append (" INDEX ");
	sb.append (id.getName());
	sb.append (" ON ");
	sb.append (tableName);
	sb.append (" (");
	Iterator i = fieldNames.iterator();
	while (i.hasNext()) {
	    String fieldName = (String) i.next();
	    sb.append (fieldName);
	    if (i.hasNext()) {
		sb.append (",");
	    }
	}
	sb.append (")");
	PreparedStatement ps1 = c.prepareStatement (sb.toString());
	return new PreparedStatement[] { ps1 };
    }

    /**
     * Gets a sequence of Statements that remove indexes.
     */
    protected PreparedStatement[] getDropIndexStatements (Connection c, String tableName, String name) throws SQLException  {
	PreparedStatement ps1 = c.prepareStatement ("DROP INDEX " + tableName + "." + name);
	return new PreparedStatement[] { ps1 };
    }

 
    private String requestLockString (String tableName, String fieldName, Object value) {
	if (value == null) {
	    return tableName + ":" + fieldName;
	}
	else {
	    return tableName + ":" + fieldName + ":" + String.valueOf(value);
	}
    }

    public Object requestLock (String tableName, String fieldName, Object value) throws InterruptedException, SQLException {
	String lockStr = requestLockString (tableName, fieldName, value);
	Thread ct = Thread.currentThread();
	for (;;) {
	    synchronized (locks) {
		LockInfo li = (LockInfo) locks.get(lockStr);
		if (li == null) {
		    locks.put (lockStr, new LockInfo (ct));
		    return lockStr;		
		}
		Thread t = li.getThread();
		if (t == ct) {
		    li.incrementLevel();
		    return lockStr;
		}
		locks.wait();
	    }
	}
    }

    public void relinquishLock (Object lock) throws SQLException {
	synchronized (locks) {
	    LockInfo li = (LockInfo) locks.get(lock);
	    if (li == null) {
		warn ("relinquishLock(): No such lock");
		return;
	    }
	    if (li.getLevel() == 0) {
		locks.remove (lock);
		locks.notifyAll();
	    }
	    else if (Thread.currentThread() == li.getThread()) {
		li.decrementLevel();
	    }
	    else {
		warn ("relinquishLock(): Attempt to relinquish lock obtained by different thread.");
	    }
	}
    }    

    public ResultsIterator query (String sql, Object[] parameters) throws SQLException {
	ConnectionPool.ConnectionWrapper c = this.pool.getConnectionWrapper();
	try {
	    PreparedStatement ps = c.prepareStatement (sql);
	    for (int i = 0; i < parameters.length; i++) {
		ps.setObject(i + 1, parameters[i]);
	    }
	    return getMapIterator (c, ps.executeQuery());
	} finally {
	    c.release();
	}
    }

    public int update (String sql, Object[] parameters) throws SQLException {
	ConnectionPool.ConnectionWrapper c = this.pool.getConnectionWrapper();
	try {
	    PreparedStatement ps = c.prepareStatement (sql);
	    for (int i = 0; i < parameters.length; i++) {
		ps.setObject(i + 1, parameters[i]);
	    }
	    return ps.executeUpdate();
	} finally {
	    c.release();
	}
    }

    /**
     * Given a ResultSet, this method returns an iterator of Maps containing
     * ResultSet data.
     */
    protected ResultsIterator getMapIterator (final ConnectionPool.ConnectionWrapper conn, final ResultSet resultSet) {
	return new ResultsIterator() {
		private Boolean hasNext;

		public void finalize() throws Throwable {
		    super.finalize();
		    if (this.hasNext()) {
			this.close();
		    }
		}

		public void close() throws SQLException {
		    resultSet.close();
		    conn.release();
		}

		public boolean hasNext() {
		    boolean hn;
		    if (hasNext == null) {
			try {
			    hn = resultSet.next();
			} catch (SQLException se) {
			    throw new IllegalStateException ("Unable to check next due to " + se);
			}
			hasNext = new Boolean (hn);
		    }
		    else {
			hn = hasNext.booleanValue();
		    }
		    if (!hn) {
			try {
			    resultSet.close();
			    conn.release();
			} catch (SQLException se) {
			    // ignore
			}
		    }
		    return hn;
		}

		public Object next() {
		    if (!hasNext()) {
			throw new NoSuchElementException();
		    }
		    try {
			return getMap (resultSet);
		    } catch (SQLException se) {
			throw new NoSuchElementException ("Unable to retrieve due to " + se);
		    } finally {
			hasNext = null;
		    }
		}

		public void remove() {
		    throw new UnsupportedOperationException();
		}
	    };
    }

    /**
     * Converts current ResultSet data into a Map.
     */
    protected Map getMap (ResultSet resultSet) throws SQLException {
	Map map = new HashMap();
	ResultSetMetaData md = resultSet.getMetaData();
	int cc = md.getColumnCount();
	// Column numbers start at 1.
	for (int i = 1; i <= cc; i++) {
	    String name = md.getColumnName (i);
            int type = md.getColumnType(i);
            //System.out.println (" For name=" + name + " type=" + type);
            if (type == Types.NUMERIC) {
                long value = resultSet.getLong(i);
                map.put (name, resultSet.wasNull() ? null : new Long(value));
            }
            else if (type == Types.DECIMAL) {
                double value = resultSet.getLong(i);
                map.put (name, resultSet.wasNull() ? null : new Double(value));
            }
            else {
                map.put (name, resultSet.getObject(i));
            }
	}
	return map;
    }

    protected boolean isLogEnabled() {
	return this.logEnabled;
    }

    protected void info (String text)  throws SQLException {
	println ("INFO", text);
    }

    protected void warn (String text)  throws SQLException {
	println ("WARN", text);
    }

    protected void error (String text)  throws SQLException {
	println ("ERROR", text);
    }

    protected void println (String prefix, String text) throws SQLException {
	if (this.logEnabled) {
	    PrintWriter writer = this.source.getLogWriter();
	    writer.println (new java.util.Date() + ": " + prefix + ": " + text);
	    writer.flush();
	}
    }

    class LockInfo {
	private final Thread thread;
	private int level = 0;

	public LockInfo (Thread t) {
	    this.thread = t;
	}

	public void incrementLevel() {
	    this.level++;
	}

	public void decrementLevel() {
	    this.level--;
	}

	public int getLevel() {
	    return this.level;
	}

	public Thread getThread() {
	    return this.thread;
	}
    }

    class TypeInfo {
	private final String typeName;
	private final int precision;
	private final int sqlType;

	public TypeInfo (int sqlType, String typeName, int precision) {
	    this.sqlType = sqlType;
	    this.typeName = typeName;
	    this.precision = precision;
	}

	public final String getDeclaration() {
	    if (this.sqlType == Types.VARCHAR) {
		return this.typeName + "(" + this.precision + ")";
	    }
	    else {
		return this.typeName;
	    }
	}	    
    }
}

