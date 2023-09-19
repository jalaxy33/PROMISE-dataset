package net.sourceforge.pbeans.data;

import java.util.*;
import java.sql.*;

import javax.sql.*;
import java.util.logging.*;

import net.sourceforge.pbeans.util.*;

/**
 * Base implementation of Database.
 * This class implements common functionalilty
 * in order to allow fairly straight-forward Database implementations
 * by simply extending this class and overriding some methods.
 */
public abstract class AbstractDatabase implements Database {
	private static final Logger logger = Logger.getLogger(AbstractDatabase.class.getName());
    private int maxTableNameLength;
    private int maxColumnNameLength;
    protected int sqlStateType;

    //private final DataSource source;
    private final Map typesByTypeID = new HashMap();
    private final Map locks = new HashMap();
    private final ThreadLocal transactionedConnection = new ThreadLocal();
    private volatile ConnectionPool pool;

    /**
     * Constructs an AbstractDatabase. 
     * @see GenericDataSource
     */
    public AbstractDatabase() throws SQLException {
    }

    /**
     * Override this method to obtain metadata information, such
     * as the JDBC URL. Implementations for "odbc" and "datadirect"
     * should override this. Note that the override must invoke
     * the <code>super</code> implementation.
     */
    public void init(DataSource dataSource) throws SQLException {
    	this.pool = new ConnectionPool(dataSource, 99, 59 * 60 * 1000);
    	this.initDatabaseInfo();
    }

    /**
     * Sets the maximum number of simultaneous open connections.
     */
    public final void setMaxConnections (int maxConnections) {
    	this.pool.setMaxConnections (maxConnections);
    }

    /**
     * Sets the amount of time, in milliseconds, that a
     * pooled connection may be idle before it is discarded.
     */
    public final void setConnectionTimeout (int timeout) {
    	this.pool.setConnectionTimeout (timeout);
    }

    /**
     * Gets the maximum length of a table name. This information
     * is obtained from database metadata. There should be no need
     * to override this method.
     */
    public int getMaxTableNameLength() throws SQLException {
    	return this.maxTableNameLength;
    }

    /**
     * Gets the maximum length of a column name in this database.
     * This information is obtained from database metadata. There
     * should be no need to override this method.
     */
    public final int getMaxColumnNameLength() throws SQLException {
    	return this.maxColumnNameLength;
    }

    /**
     * Initializes some meta-information about the database,
     * particularly about supported types. There should be
     * no need to override this method.
     */
    protected void initDatabaseInfo() throws SQLException {
    	ConnectionPool.ConnectionWrapper cw = this.getConnectionWrapper();
    	try {
    		Connection c = cw.getConnection();
    		DatabaseMetaData dmd = c.getMetaData();
    		int mtnl = dmd.getMaxTableNameLength();
    		int mcnl = dmd.getMaxColumnNameLength();
    		this.maxTableNameLength = mtnl == 0 ? Integer.MAX_VALUE : mtnl;
    		this.maxColumnNameLength = mcnl == 0 ? Integer.MAX_VALUE : mcnl;
    		try {
    			this.sqlStateType = dmd.getSQLStateType();
    		} catch (UnsupportedOperationException uoe) {
    			this.sqlStateType = 0;
    		}
    		catch (SQLException sqle)
    		{
    			this.sqlStateType = 0;
    		}
    		if(logger.isLoggable(Level.INFO)) {
    			logger.info("initDatabaseInfo(): sqlStateType=" + this.sqlStateType);
    		}
    		ResultSet rs;
    		// Get type information
    		rs = dmd.getTypeInfo();
    		try {
    			while (rs.next()) {
    				String typeName = rs.getString("TYPE_NAME");
    				int dataType = rs.getInt("DATA_TYPE");
    				int precision = rs.getInt("PRECISION");
    				int maximumScale = rs.getInt("MAXIMUM_SCALE");
    				Integer dtInt = new Integer(dataType);

    				if (this.typesByTypeID.get (dtInt) == null) {
    					this.typesByTypeID.put (dtInt, new TypeInfo(dataType,typeName,precision));
    				}
    				if(logger.isLoggable(Level.INFO)) {
    					logger.info("initDatabaseInfo(): for type=" + dataType + " typeName=" + typeName + " precision=" + precision + " maximumScale=" + maximumScale);
    				}
    			}
    		} finally {
    			rs.close();
    		}

    	} finally {
    		cw.release();
    	}
    }

    /**
     * Gets the type declaration for a type constant as
     * defined in java.sql.Types. By default, type declarations
     * are populated from metadata. If this information is
     * not accurate, override this method.
     */
    protected String getTypeDeclaration(int dataType) throws SQLException {
    	TypeInfo ti = (TypeInfo) this.typesByTypeID.get (new Integer(dataType));
    	if (ti == null) {
    		throw new SQLException ("Database is not aware of Java java.sql.Types constant " + dataType);
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
     * Makes sure that a table exists. There should be no
     * need to override this method.
     * @param tableName The name of the table.
     * @param mandatoryField Information about the mandatory (primary key) field.
     * @param fieldDescriptors Information about other fields.
     * @param indexDescriptors Information about indexes.
     */
    public void ensureTableExists (String tableName, FieldDescriptor mandatoryField, Collection fieldDescriptors, Collection indexDescriptors) throws SQLException {
    	this.ensureTableExists(tableName, mandatoryField, fieldDescriptors, indexDescriptors, false, false, false);
    }
    
    /**
     * Gets the name of an implicit index created when there's 
     * a primary, as reported in database metadata. If no such
     * index is reported, this method returns <code>null</code>.
     * Override if the create table statement is sufficient to create
     * a primary key which need not be created explicitly.
     */
    public String getAutomaticPrimaryKeyIndexName() {
    	return null;
    }
    
    /**
     * Creates a table if necessary. First, it makes sure field names don't match
     * any reserved database keywords. Then, it removes table fields not found in
     * <code>fieldDescriptors</code>. It adds any new fields, and changes types
     * of existing fields if needed. Finally, it modifies and creates requested indexes.
     * There should be no need to override this method.
     */
    public void ensureTableExists (String tableName, FieldDescriptor mandatoryField, Collection fieldDescriptors, Collection indexDescriptors, boolean userManaged, boolean autoIncrementRequested, boolean deleteFields) throws SQLException {
    	if(!userManaged) {
    		if(logger.isLoggable(Level.INFO)) {
    			logger.info("ensureTableExists(): Table " + tableName + " will be altered/created automatically if necessary. Note that user-managed mode is now provided as an alternative.");
    		}
    	}
    	else {
    		if(logger.isLoggable(Level.INFO)) {
    			logger.info("ensureTableExists(): Table " + tableName + " will NOT be altered automatically since user-managed mode has been requested.");
    		}
    	}
    	boolean autoIncrement = autoIncrementRequested && this.supportsAutoIncrement();
    	if(autoIncrementRequested && !this.supportsAutoIncrement()) {
    		logger.warning("ensureTableExists(): Auto-increment was requested but the Database interface implementation reports that it does not support it. Random IDs will be used.");
    	}
    	Collection<FieldDescriptor> fdToChange = new LinkedList<FieldDescriptor>();
    	Collection<FieldDescriptor> fdFoundToAlreadyExist = new HashSet<FieldDescriptor>();
    	Collection<FieldDescriptor> fieldsToRemove = new LinkedList<FieldDescriptor>();
    	Collection<String> normalFieldNamesToRemove = new HashSet<String>();
    	Map<String,FieldDescriptor> fdMap = new HashMap<String,FieldDescriptor>();
    	String normalMandatoryFieldName = normalizeName(mandatoryField.getName());
    	fdMap.put (normalMandatoryFieldName, mandatoryField);    	
    	Iterator fdI = fieldDescriptors.iterator();
    	while (fdI.hasNext()) {
    		FieldDescriptor fd = (FieldDescriptor) fdI.next();
    		String name = normalizeName (fd.getName());
    		fdMap.put (name, fd);
    	}
    	boolean changingMandatoryField = false;
    	ConnectionPool.ConnectionWrapper cw = this.getConnectionWrapper();
    	try {
    		Connection c = cw.getConnection();
    		DatabaseMetaData dmd = c.getMetaData();
    		String normalTableName = normalizeName (tableName);
    		    		
    		//----- Get existing column information
    		ResultSet rs = dmd.getColumns (c.getCatalog(), null, normalTableName, "%");
    		int numColumns = 0;
    		try {
    			while (rs.next()) {
    				numColumns++;
    				String normalFieldName = null, typeName = null, remarks = null, isNullable = null;
    				int fieldType = 0, nullableInt = 0; // sourceDataType = 0;
    				try {
    					normalFieldName = rs.getString("COLUMN_NAME");
    				} catch(Exception err) {
    					logger.warning("In table " + normalTableName + ": " + err.getMessage());
    				}
    				try {
    					fieldType = rs.getInt("DATA_TYPE");
    				} catch(Exception err) {
    					logger.warning("In table " + normalTableName + ": " + err.getMessage());
    				}
    				try {
    					nullableInt = rs.getInt("NULLABLE");
    				} catch(Exception err) {
    					logger.warning("In table " + normalTableName + ": " + err.getMessage());
    				}
    				try {
    					typeName = rs.getString("TYPE_NAME");
    				} catch(Exception err) {
    					logger.warning("In table " + normalTableName + ": " + err.getMessage());
    				}
    				try {
    					remarks = rs.getString("REMARKS");
    				} catch(Exception err) {
    					logger.warning("In table " + normalTableName + ": " + err.getMessage());
    				}
    				try {
    					isNullable = rs.getString("IS_NULLABLE");
    				} catch(Exception err) {
    					logger.warning("In table " + normalTableName + ": " + err.getMessage());
    				}
//    				try {
//    					sourceDataType = rs.getInt("SOURCE_DATA_TYPE"); 
//    				} catch(Exception err) {
//    					logger.warning("In table " + normalTableName + ": " + err.getMessage());
//    				}
    				if(logger.isLoggable(Level.INFO)) {
    					logger.info("ensureTableExists(): Field from table " + normalTableName + ": COLUMN_NAME=" + normalFieldName + ",DATA_TYPE=" + fieldType + ",NULLABLE=" + nullableInt + ",TYPE_NAME=" + typeName + ",REMARKS=" + remarks + ",IS_NULLABLE=" + isNullable);
    				}
    				FieldDescriptor fd = fdMap.get(normalFieldName);
    				//TODO: nullable change
    				if (fd != null) {
    					fdFoundToAlreadyExist.add (fd);
    					boolean nullableToMatch = nullableInt != 0;
    					if(!this.canSetNullableOnTableAlter()) {
    						logger.warning("ensureTableExists(): Because of database limitations, skipping nullable status check for field " + fd.getName() + ".");
    						nullableToMatch = fd.isNullable();
    					}
    					if (!fd.matches(this, fieldType, nullableToMatch)) {
    						if(logger.isLoggable(Level.INFO)) {
    							logger.info("ensureTableExists(): Will change field " + fd.getName() + "; newType=" + fd.getSqlType() + ", oldType=" + fieldType + ", oldNullable=" + nullableInt + ",newNullable=" + fd.isNullable());
    						}
    						if(fd == mandatoryField) {
    							changingMandatoryField = true;
    						}
    						fdToChange.add (fd);
    					}
    				}
    				else {
    					normalFieldNamesToRemove.add (normalFieldName);
    					fieldsToRemove.add (new FieldDescriptor (normalFieldName, fieldType, true));
    				}
    			}
    		} finally {
    			rs.close();
    		}

    		//---- Get exising index information (only if table exists!)
			boolean foundIndexes = false;
			boolean foundPrimaryKey = false;
			Map<String,IndexDescriptor> oldIndexes = new HashMap<String,IndexDescriptor>();
			Collection<IndexDescriptor> indexesToRemove = new LinkedList<IndexDescriptor>();
    		if(numColumns > 0) {
    			ResultSet irs = dmd.getIndexInfo (c.getCatalog(), null, normalTableName, false, false);
    			try {
    				while (irs.next()) {
    					foundIndexes = true;
    					String name = irs.getString("INDEX_NAME");
    					String column = irs.getString("COLUMN_NAME");
    					boolean unique = !irs.getBoolean("NON_UNIQUE");
    					if (logger.isLoggable(Level.INFO)) {
    						logger.info("ensureTableExists(): Found existing index name=" + name + " column=" + column);
    					}
    					if(name.equals(this.getAutomaticPrimaryKeyIndexName())) {
    						foundPrimaryKey = true;
    					}
    					IndexDescriptor id = oldIndexes.get(name);
    					if (id == null) {
    						id = new IndexDescriptor (name, unique, column);
    						oldIndexes.put (name, id);
    					}
    					else {
    						id.addFieldName (column);
    					}			
    					if (getFieldDescriptor(fieldsToRemove, column) != null) {
    						if (logger.isLoggable(Level.INFO)) {
    							logger.info("ensureTableExists(): Scheduling index " + name + " for removal since column " + column + " is no longer there.");
    						}    					
    						indexesToRemove.add(id);
    					}
    				}
    			} finally {
    				irs.close();
    			}
    		}

    		//----- Determine if mandatory field needs to be changed because it's not a primary ket
    		boolean mustRecreatePk = this.getAutomaticPrimaryKeyIndexName() != null && !foundPrimaryKey && numColumns > 0;
    		if(mustRecreatePk) {
    			if(logger.isLoggable(Level.WARNING)) {
    				logger.warning("ensureTableExists(): Database uses implicit primary key indexes, but it wasn't found in table " + normalTableName + ".");
    			}
    		}
    		
    		//----- Add mandatory field to list of fields to change if PK must be recreated
    		if(mustRecreatePk && fdFoundToAlreadyExist.contains(mandatoryField) && !fdToChange.contains(mandatoryField)) {
    			fdToChange.add(mandatoryField);
    			changingMandatoryField = true;
    		}
    		
    		//----- Create table if it does not exist
    		if (numColumns == 0) {
    			if(userManaged) {
    				if(logger.isLoggable(Level.WARNING)) {
    					logger.severe("ensureTableExists(): In user-managed mode: Table " + normalTableName + " has zero columns.");
    				}
    			}    			
    			else {
    				PreparedStatement createPs = getCreateTableStatement (c, normalTableName, mandatoryField, autoIncrement);
    				if(logger.isLoggable(Level.INFO)) {
    					logger.info("ensureTableExists(): Metadata gave no columns for table " + tableName + ". Creating table by executing: " + createPs);
    				}
    				long time1 = System.currentTimeMillis();
    				createPs.execute();
    				long time2 = System.currentTimeMillis();
    				if(logger.isLoggable(Level.INFO)) {
    					logger.info("ensureTableExists(): Table " + tableName + " created in " + (time2 - time1) + " ms.");
    				}    				
    			}
    		}
    		else {
    			//----- Check prior auto-increment status of mandatory field
    			//----- to determine if mandatory field needs to change.
    			
    			if (logger.isLoggable(Level.INFO)) {
    				logger.info("ensureTableExists(): Found " + numColumns + " columns in table " + tableName + ".");
    			}
    			if(!changingMandatoryField && this.supportsAutoIncrement()) {
    				// Do a query with no rows to find out auto-increment status
    				boolean wasAutoIncrement = false;
    				PreparedStatement ps = c.prepareStatement("SELECT " + normalMandatoryFieldName + " FROM " + normalTableName + " WHERE 1=0;");
    				ResultSet airs = ps.executeQuery();
    				try {
    					ResultSetMetaData rsmd = airs.getMetaData();
    					wasAutoIncrement = rsmd.isAutoIncrement(1);
    					if(logger.isLoggable(Level.INFO)) {
    						logger.info("ensureTableExists(): In table " + tableName + ", ID field prior auto-increment status: " + wasAutoIncrement + ".");
    					}
    				} finally {
    					airs.close();
    				}
    				if(wasAutoIncrement != autoIncrement) {
    					fdToChange.add(mandatoryField);
    					changingMandatoryField = true;
    				}
    			}
    		}
    		    		
    		//----- Add requested fields not found in metadata.
    		Iterator checkI = fieldDescriptors.iterator();
    		while (checkI.hasNext()) {
    			FieldDescriptor fd = (FieldDescriptor) checkI.next();
    			if (!fdFoundToAlreadyExist.contains (fd)) {
    				// Add field, but first check renames
    				String renamedFieldName = null;
    				String[] renamedFrom = fd.getRenamedFrom();
    				if (renamedFrom != null) {
    					for (int i = 0; i < renamedFrom.length; i++) {
    						String normalRenamedFrom = normalizeName (renamedFrom[i]);
    						FieldDescriptor rfd = getFieldDescriptor (fieldsToRemove, normalRenamedFrom);
    						if (rfd != null) {
    							fieldsToRemove.remove (rfd);
    							normalFieldNamesToRemove.remove (normalRenamedFrom);
    							renamedFieldName = normalRenamedFrom;
    							break;
    						}
    					}
    				}
    				if (renamedFieldName != null) {
    					if(userManaged) {
    						logger.severe("ensureTableExists(): User-managed mode: Field " + renamedFieldName + " should have been renamed to " + fd.getName() + "."); 
    					}
    					else {
    						boolean doai = fd == mandatoryField ? autoIncrement : false;
    						PreparedStatement[] pss = getRenameFieldStatements (c, normalTableName, renamedFieldName, fd, doai);
    						for (int i = 0; i < pss.length; i++) {
    							try {
    								PreparedStatement ps = pss[i];
    								if(ps != null) {
    									if (logger.isLoggable(Level.INFO)) {
    										logger.info("ensureTableExists(): Renaming field from " + renamedFieldName + " to " + fd.getName() + " by executing: " + pss[i]);
    									}
    									long time1 = System.currentTimeMillis();
    									pss[i].execute();
    									long time2 = System.currentTimeMillis();
    									if (logger.isLoggable(Level.INFO)) {
    										logger.info("ensureTableExists(): Renamed field from " + renamedFieldName + " to " + fd.getName() + " in " + (time2 - time1) + " ms.");
    									}
    								}
    							} catch (SQLException se) {
    								SQLException nse = new SQLException ("Unable to rename field from " + renamedFieldName + " to " + fd.getName() + ". Statement: " + pss[i] + ". Message: " + se.getMessage(), se.getSQLState(), se.getErrorCode());
    								nse.setNextException (se);
    								throw nse;
    							}                        
    						}
    					}
    				}
    				else {
    					if(userManaged) {
    						logger.severe("ensureTableExists(): User-managed mode: Expected field " + fd.getName() + " to exist in table " + normalTableName + ".");
    					}
    					else {
    						// Actually add
    						boolean doai = fd == mandatoryField ? autoIncrement : false;
    						PreparedStatement[] pss = getCreateFieldStatements(c, normalTableName, fd, doai);
    						for(int i = 0; i < pss.length; i++) {
    							PreparedStatement ps = pss[i];
    							if (logger.isLoggable(Level.INFO)) {
    								logger.info ("ensureTableExists(): Adding field " + fd.getName() + ". Executing: " + ps);
    							}
    							try {
    								long time1 = System.currentTimeMillis();
    								ps.execute();
    								long time2 = System.currentTimeMillis();
    								if(logger.isLoggable(Level.INFO)) {
    									logger.info ("ensureTableExists(): Added field " + fd.getName() + " in " + (time2 - time1) + " ms.");
    								}
    							} catch (SQLException se) {
    								SQLException nse = new SQLException ("Unable to add field " + fd.getName() + ". Make sure field name does not conflict with database keyword. Statement: " + ps + ". Exception Reason: " + se.getMessage(), se.getSQLState(), se.getErrorCode());
    								nse.setNextException (se);
    								throw nse;
    							}
    						}
    					}
    				}
    			}
    		}

    		//----- Remove fields found in metadata but not requested.
    		if (deleteFields) {
    			if(userManaged) {
    				logger.severe("ensureTableExists(): User-managed mode: There are fields in " + normalTableName + " that should have been deleted.");
    			}
    			else {
    				Iterator removeI = fieldsToRemove.iterator();
    				while (removeI.hasNext()) {
    					FieldDescriptor rfd = (FieldDescriptor) removeI.next();
    					String name = rfd.getName();
    					PreparedStatement ps = getRemoveFieldStatement (c, normalTableName, name);
    					if (logger.isLoggable(Level.INFO)) {
    						logger.info("ensureTableExists(): Dropping field " + name + " by executing: " + ps);
    					}
    					try {
    						long time1 = System.currentTimeMillis();
    						ps.execute();
    						long time2 = System.currentTimeMillis();
    						if(logger.isLoggable(Level.INFO)) {
    							logger.info("ensureTableExists(): Dropped field " + name + " in " + (time2 - time1) + " ms.");
    						}
    					} catch (SQLException se) {
    						logger.warning("ensureTableExists(): Unable to drop field " + name + ": " + se.getMessage());
    					}
    				}
    			}
    		}
    		else {
    			// Not removing, but ensuring they are nullable now.
    			if(!userManaged) {
    				Iterator removeI = fieldsToRemove.iterator();
    				while (removeI.hasNext()) {
    					FieldDescriptor rfd = (FieldDescriptor) removeI.next();
    					boolean doai = rfd == mandatoryField ? autoIncrement : false;
    					PreparedStatement[] ps = getChangeFieldStatements (c, normalTableName, rfd, doai);
    					try {
    						for (int i = 0; i < ps.length; i++) {
    							if (logger.isLoggable(Level.INFO)) {
    								logger.info("ensureTableExists(): Ensuring unused field nullable by executing: " + ps[i]);
    							}
    							ps[i].execute();
    						}
    					} catch (SQLException se) {
    						logger.warning("ensureTableExists(): Unable to change field " + rfd.getName() + ": " + se.getMessage());
    					}
    				}
    			}
    		}

    		//----- Change fields whose declaration is no longer the same.
    		Iterator changeI = fdToChange.iterator();
    		while (changeI.hasNext()) {
    			FieldDescriptor fd = (FieldDescriptor) changeI.next();
    			if(userManaged) {
    				logger.warning("ensureTableExists(): User-managed mode: Expected declaration of field " + fd.getName() + " to change.");
    			}
    			else {
    				boolean doai = fd == mandatoryField ? autoIncrement : false;
    				PreparedStatement[] ps = getChangeFieldStatements (c, normalTableName, fd, doai);
    				for (int i = 0; i < ps.length; i++) {
    					if (logger.isLoggable(Level.INFO)) {
    						logger.info("ensureTableExists(): Changing declaration of field " + fd.getName() + " by executing: " + ps[i]);
    					}
    					long time1 = System.currentTimeMillis();
    					try {
    						ps[i].execute();
    					} catch (SQLException se) {
    						SQLException nse = new SQLException ("Unable to change field " + fd.getName() + ". Statment: " + ps[i] + ". Exception Reason: " + se.getMessage(), se.getSQLState(), se.getErrorCode());
    						nse.setNextException (se);
    						throw nse;
    					}
    					long time2 = System.currentTimeMillis();
    					if (logger.isLoggable(Level.INFO)) {
    						logger.info("ensureTableExists(): Changed declaration of field " + fd.getName() + " in " + (time2 - time1) + " ms.");
    					}
    				}
    			}
    		}	

    		Collection normalizedIndexDescriptors = new HashSet();
    		Collection normalIndexNames = new HashSet();
    		Collection actualIndexDescriptors = new LinkedList();
    		actualIndexDescriptors.addAll(indexDescriptors);
    		if(this.getAutomaticPrimaryKeyIndexName() == null) {
    			String indexName = "JP__IDXOBJID";
    			IndexDescriptor pkDescriptor = new IndexDescriptor(indexName, true, normalMandatoryFieldName);
    			actualIndexDescriptors.add (pkDescriptor);
    		}
    		Iterator indexesI = actualIndexDescriptors.iterator();
    		while(indexesI.hasNext()) {
    			IndexDescriptor id = (IndexDescriptor) indexesI.next();
    			IndexDescriptor nid = this.normalizeIndex(normalTableName, id);
    			normalizedIndexDescriptors.add(nid);
    			normalIndexNames.add(nid.getName());
    		}
    		Iterator oldIndexesI = oldIndexes.values().iterator();
    		while(oldIndexesI.hasNext()) {
    			IndexDescriptor oldId = (IndexDescriptor) oldIndexesI.next();
    			String oldIdName = oldId.getName();
    			if(!oldIdName.equals(this.normalizeName(this.getAutomaticPrimaryKeyIndexName())) && !normalIndexNames.contains(oldId.getName())) {
    				if (logger.isLoggable(Level.INFO)) {
    					logger.info("ensureTableExists(): Scheduling index " + oldId.getName() + " for removal since it is no longer declared.");
    				}    					    				
    				indexesToRemove.add(oldId);
    			}
    		}
    		boolean noGetIndexInfo = !foundIndexes && numColumns > 0 && this.getAutomaticPrimaryKeyIndexName() == null;
    		if (noGetIndexInfo) {
    			logger.warning("ensureTableExists(): Did not find any indexes, which indicates the JDBC driver does not properly support getIndexInfo().");
    			if(!userManaged) {
    				logger.warning("ensureTableExists(): Will have to remove each index before adding it.");
    			}
    		}
    		Iterator indexToRemoveI = indexesToRemove.iterator();
    		while (indexToRemoveI.hasNext()) {
    			IndexDescriptor rid = (IndexDescriptor) indexToRemoveI.next();
    			if(userManaged) {
    				logger.warning("ensureTableExists(): User-managed mode: Index " + rid.getName() + " should have been dropped.");
    			}
    			else {
    				PreparedStatement[] dps = getDropIndexStatements (c, normalTableName, rid.getName());
    				for (int i = 0; i < dps.length; i++) {
    					if(logger.isLoggable(Level.INFO)) {
    						logger.info("ensureTableExists(): In order to drop index named " + rid.getName() + " executing: " + dps[i]);
    					}
    					long time1 = System.currentTimeMillis();
    					try {
    						dps[i].execute();
    					} catch(SQLException sqe) {
    						logger.warning("ensureTableExists(): Was unable to remove index named " + rid.getName() + ": " + sqe);
    					}
    					long time2 = System.currentTimeMillis();
    					if(logger.isLoggable(Level.INFO)) {
    						logger.info("ensureTableExists(): Dropped index " + rid.getName() + " in " + (time2 - time1) + " ms.");
    					}
    				}
    			}
    		}
    		
    		//----- Created indexes that were not found in metadata
    		Iterator indexI = normalizedIndexDescriptors.iterator();
    		while (indexI.hasNext()) {
    			IndexDescriptor id = (IndexDescriptor) indexI.next();
    			Collection fieldNames = id.getFieldNames();
    			for (Iterator fnI = fieldNames.iterator(); fnI.hasNext();) {
    				String fn = (String) fnI.next();
    				if (normalFieldNamesToRemove.contains (fn)) {
    					throw new SQLException ("Index named " + id.getName() + " contains field " + fn + " which is either removed or no longer requested as part of table " + tableName + ".");
    				}
    			}
    			String indexName = id.getName();
    			IndexDescriptor oldId = noGetIndexInfo ? id : (IndexDescriptor) oldIndexes.get(indexName);
    			if (oldId != null) {
    				if (oldId.equals(id) && !noGetIndexInfo) {
    					// Leave index as is and check next.
    					continue;
    				}
    				if(userManaged) {
    					if(!noGetIndexInfo) {
    						logger.warning("ensureTableExists(): User-managed mode: Expected index " + oldId.getName() + " to be dropped.");
    					}
    				}
    				else {
    					PreparedStatement[] dps = getDropIndexStatements(c, normalTableName, oldId.getName());
    					for (int i = 0; i < dps.length; i++) {
    						if (logger.isLoggable(Level.INFO)) {
    							logger.info("ensureTableExists(): In order to drop index named " + oldId.getName() + " executing: " + dps[i]);
    						}
    						try {
    							long time1 = System.currentTimeMillis();
    							dps[i].execute();
    							long time2 = System.currentTimeMillis();
    							if(logger.isLoggable(Level.INFO)) {
    								logger.info("ensureTableExists(): Dropped index named " + oldId.getName() + " in " + (time2 - time1) + " ms.");
    							}
    						} catch (SQLException se) {
								logger.warning("ensureTableExists(): Assuming index " + oldId.getName() + " does not exist because we're unable to drop it: " + se.getMessage());
    						}
    					}
    				}
    			}
    			//----- Create index that either was not in metadata or changed
    			if(userManaged) {
    				logger.warning("ensureTableExists(): Expected index " + id.getName() + " to exist.");
    			}
    			else {
    				PreparedStatement[] ps = getCreateIndexStatements (c, normalTableName, id, fdMap);
    				for (int i = 0; i < ps.length; i++) {
    					if (logger.isLoggable(Level.INFO)) {
    						logger.info("ensureTableExists(): In order to create index, executing: " + ps[i]);
    					}
    					long time1 = System.currentTimeMillis();
    					ps[i].execute();
    					long time2 = System.currentTimeMillis();
    					if (logger.isLoggable(Level.INFO)) {
    						logger.info("ensureTableExists(): Created index " + id.getName() + " in " + (time2 - time1) + " ms.");
    					}
    				}
    			}
    		}
    	} finally {
    		cw.release();
    	}
    }

    private FieldDescriptor getFieldDescriptor (Collection fds, String fieldName) {
    	for (Iterator fdsI = fds.iterator(); fdsI.hasNext();) {
    		FieldDescriptor fd = (FieldDescriptor) fdsI.next();
    		if (fieldName.equals (fd.getName())) {
    			return fd;
    		}
    	}
    	return null;
    }

    /**
     * Takes a field name or table name as it would be requested
     * by a caller and coverts it to a string as it would be reported
     * by database metadata (for example, as lowercase strings, in some databases.)
     * Override this method if the database has special naming requirements for
     * field or table names.
     */
    public String normalizeName(String s) {
    	return s;
    }
    
    /** 
     * Determines if the database is able to alter 
     * existing fields such that their nullable status
     * changes (and the change is reflected in database
     * metadata.) This implementation returns <code>true</code>
     * by default. Override if the database is unable to
     * alter fields in this manner.
     */
    protected boolean canSetNullableOnTableAlter() {
    	return true;
    }

//    /**
//     * Takes a normalized table or field name and unnormalizes it. 
//     * The unnormalized name returned by this method 
//     * may differ from the original unnormalized name only in whether it's upper-
//     * or lower-case.
//     */
//    public String unnormalizeName(String s) {
//    	return s;
//    }
    
    /**
     * Override this method if the database has special requirements
     * on index names, such as global uniqueness or lowercase strings.
     * @param normalTableName A table name, already normalized.
     * @param id An IndexDescriptor instance.
     */
    protected IndexDescriptor normalizeIndex(String normalTableName, IndexDescriptor id) {
    	return id;
    }

    /**
     * Checks whether two SQL types match when trying to determine
     * if a field declaration should be changed. Override this method if pBeans
     * is changing fields to equivalent types unnecessarily.
     */
    protected boolean typesMatch (int type1, int type2) {
    	if (type1 == type2) {
    		return true;
    	}
    	if (isLong(type1) && isLong(type2)) {
    		return true;
    	}
    	if (isDecimal(type1) && isDecimal(type2)) {
    		return true;
    	}
    	if (isBlob(type1) && isBlob(type2)) {
    		return true;
    	}
        if(isTiny(type1) && isTiny(type2)) {
        	return true;
        }
    	return false;
    }

    protected boolean isLong (int type) {
    	return type == Types.BIGINT;
    }

    protected boolean isDecimal (int type) {
    	return type == Types.DECIMAL || type == Types.DOUBLE || type == Types.FLOAT || type == Types.REAL;
    }

    protected boolean isBlob (int type) {
    	return type == Types.BLOB || type == Types.VARBINARY || type == Types.BINARY;
    }
    
    protected boolean isText(int type) {
    	return type == Types.VARCHAR;
    }
    
    protected boolean isTiny(int type) {
    	return type == Types.BOOLEAN || type == Types.TINYINT || type == Types.BIT;
    }

    /**
     * Gets a Statement that creates a table with a single field.
     * Override this method if the default CREATE syntax does not
     * work.
     */
    protected PreparedStatement getCreateTableStatement (Connection c, String tableName, FieldDescriptor fd, boolean autoIncrementRequested) throws SQLException {
    	return c.prepareStatement ("CREATE TABLE " + tableName + " (" + fd.getName() + " " + getTypeDeclaration(fd.getSqlType()) + (fd.isNullable() ? " NULL" : " NOT NULL") + ")");
    }

    /**
     * Gets a Statement that removes a field.
     * Override this method if the default ALTER syntax does not work.
     */
    protected PreparedStatement getRemoveFieldStatement (Connection c, String tableName, String columnName) throws SQLException {
    	PreparedStatement ps = c.prepareStatement ("ALTER TABLE " + tableName + " DROP " + columnName);
    	return ps;
    }

    /**
     * Gets a Statement that adds a field to a table.
     * Override this method if the default ALTER syntax does not work.
     */
    protected PreparedStatement[] getCreateFieldStatements (Connection c, String normalTableName, FieldDescriptor fd, boolean autoIncrement) throws SQLException {
    	String normalFdName = this.normalizeName(fd.getName());
    	PreparedStatement ps1 = c.prepareStatement ("ALTER TABLE " + normalTableName + " ADD " + normalFdName + " " + getTypeDeclaration(fd.getSqlType()) + (fd.isNullable() ? "" : " NOT NULL"));
    	if(!fd.isNullable()) {
    		PreparedStatement ps2 = c.prepareStatement ("ALTER TABLE " + normalTableName + " ADD CHECK (" + normalFdName + " IS NOT NULL)");
    		return new PreparedStatement[] { ps1, ps2 };
    	}
    	else {
    		return new PreparedStatement[] { ps1 };
    	}    	
    }

    /**
     * Gets a sequence of Statements that can modify a field's type
     * and whether it's nullable. 
     */
    protected abstract PreparedStatement[] getChangeFieldStatements (Connection c, String tableName, FieldDescriptor fd, boolean autoIncrement) throws SQLException;

    /**
     * Gets a sequence of Statements that can rename a field. The statements should change the field's type and whether the field is nullable.
     */
    protected abstract PreparedStatement[] getRenameFieldStatements (Connection c, String tableName, String oldName, FieldDescriptor fd, boolean autoIncrement) throws SQLException;

    /**
     * Determines whether the database requires a key length
     * qualifier for indexes on text fields. The default is <code>false</code>.
     * Override if database requires key lenghts in the text fields
     * of an index.
     */
    protected boolean requiresKeyLength() {
    	return false;
    }
    
    /**
     * Gets a sequence of Statements that create one index.
     * Override if the default CREATE INDEX syntax does not work.
     * @param normalFieldMap A map from <i>normalized</i> field names to FieldDescriptors.
     */
    protected PreparedStatement[] getCreateIndexStatements (Connection c, String tableName, IndexDescriptor id, Map<String,FieldDescriptor> normalFieldMap) throws SQLException  {
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
    		String normalFieldName = this.normalizeName(fieldName);
    		sb.append (normalFieldName);
    		FieldDescriptor fd = normalFieldMap.get(normalFieldName);
    		if(fd == null) {
    			throw new SQLException("No field named " + normalFieldName + " in table " + tableName + ". Fields found are: " + normalFieldMap.keySet());
    		}
    		if(this.isText(fd.getSqlType()) && this.requiresKeyLength()) {
    			int keyLength = id.getKeyLength();
    			if(keyLength > 0) {
    				sb.append("(" + keyLength  + ")");
    			}
    		}
    		if (i.hasNext()) {
    			sb.append (",");
    		}
    	}
    	sb.append (")");
    	PreparedStatement ps1 = c.prepareStatement (sb.toString());
    	return new PreparedStatement[] { ps1 };
    }

    /**
     * Gets a sequence of Statements that remove one index.
     * Override if the default DROP INDEX syntax does not work.
     * @param name The name of the index.
     */
    protected PreparedStatement[] getDropIndexStatements (Connection c, String tableName, String name) throws SQLException  {
    	PreparedStatement ps1 = c.prepareStatement ("DROP INDEX " + tableName + "." + name);
    	return new PreparedStatement[] { ps1 };
    }

    private final ThreadLocal lockStackHolder = new ThreadLocal();

    /**
     * Requests a Java thread lock, in addition to starting a transaction,
     * if not already in one. 
     * @throws InterruptedException
     * @throws SQLException
     */
    public final void requestLock(String lockStr, int transactionIsolationLevel) throws InterruptedException, SQLException {
    	Thread ct = Thread.currentThread();
    	Object lock = lockStr;
    	boolean firstLevel;
    	for (;;) {
    		synchronized (locks) {
    			LockInfo li = (LockInfo) locks.get(lock);
    			if (li == null) {
    				firstLevel = true;
    				locks.put(lock, new LockInfo(ct));
    				break;
    			}
    			else {
    				Thread t = li.getThread();
    				if (t == ct) {
    					firstLevel = false;
    					li.incrementLevel();
    					break;
    				}
    				else {
    					locks.wait();
    				}
    			}
    		}
    	}
    	boolean inTransaction = this.inTransaction();
		if(!inTransaction) {	
			try {
				this.beginTransaction(transactionIsolationLevel);
			} catch(Throwable err) {
				// Roll back lock
				synchronized(locks) {
					if(firstLevel) {
						locks.remove(lock);
						locks.notifyAll();
					}
					else {
						((LockInfo) locks.get(lock)).decrementLevel();
					}
				}
				if(err instanceof SQLException) {
					throw (SQLException) err;
				}
				else {
					throw new SQLException(err.getMessage());
				}
			}
 		}
    	LinkedList lockStack = (LinkedList) lockStackHolder.get();
    	if(lockStack == null) {
    		lockStack = new LinkedList();
    		lockStackHolder.set(lockStack);
    	}
		lockStack.add(new LockEntry(lock, !inTransaction));
    }
    
    /**
     * If the matching call to requestLock requested a transaction, 
     * this method ends it, and also relinquishes the Java lock.
     * @throws SQLException
     */
    public final void relinquishLock() throws SQLException {
    	LinkedList lockStack = (LinkedList) this.lockStackHolder.get();
    	if(lockStack == null && lockStack.isEmpty()) {
    		throw new SQLException("There is no lock to relinquish in this thread.");
    	}
    	LockEntry entry = (LockEntry) lockStack.removeLast();
    	if(entry.startedTransaction) {
    		try {
    			this.endTransaction();
    		} catch(Exception err) {
    			// Throwing the exception would leave us in an inconsistent state.
    			logger.log(Level.SEVERE, "relinquishLock(): Unable to end transaction.", err);
    		}
    	}
    	Object lock = entry.lock;
    	synchronized (locks) {
    		LockInfo li = (LockInfo) locks.get(lock);
    		if (li == null) {
    			throw new IllegalStateException("No such lock: " + lock);
    		}
    		if (Thread.currentThread() != li.getThread()) {
    			throw new IllegalStateException("Thread does not own lock: " + lock);
    		}
    		if (li.getLevel() == 0) {
    			locks.remove (lock);
    			locks.notifyAll();
    		}
    		else {
    			li.decrementLevel();
    		}
    	}
    }    

    /**
     * Queries the database. There should be no need
     * to override this method.
     */
    public ResultsIterator<BeanWrapper> query (String sql, Object[] parameters, BeanUnmarshaller unmarshaller) throws SQLException {
    	ConnectionPool.ConnectionWrapper c = this.getConnectionWrapper();
    	try {
    		PreparedStatement ps = c.prepareStatement (sql);
    		for (int i = 0; i < parameters.length; i++) {
    			ps.setObject(i + 1, parameters[i]);
    		}
    		return getBeanIterator (c, ps.executeQuery(), unmarshaller);
    	} finally {
    		c.release();
    	}
    }

    /**
     * Executes an insert or update operation in the database.
     * There should be no need to override this method.
     */
    public int update (String sql, Object[] parameters) throws SQLException {
    	if(logger.isLoggable(Level.INFO)) {
    		logger.info("update(): sql=[" + sql + "], parameter=" + Arrays.asList(parameters));
    	}
    	ConnectionPool.ConnectionWrapper c = this.getConnectionWrapper();
    	try {
    		PreparedStatement ps = c.prepareStatement (sql);
    		for (int i = 0; i < parameters.length; i++) {
    			Object objVal = parameters[i];
    			try {
    				setParameter (ps, i + 1, objVal);
    			} catch (SQLException se) {
    				SQLException se2 = new SQLException ("Unable to set parameter # " + (i + 1) + " of type " + (objVal == null ? "<null>" : objVal.getClass().getName()) + ". SQL: " + sql + ". Reason: " + se.getMessage(), se.getSQLState(), se.getErrorCode());
    				se.setNextException (se);
    				throw se2;
    			}
    		}
    		return ps.executeUpdate();
    	} finally {
    		c.release();
    	}
    }

    /**
     * Determines if the database supports auto-increment primary keys. 
     * This default implementation returns false. Override if the database
     * supports auto-increment fields.
     */
    protected boolean supportsAutoIncrement() {
    	return false;
    }

    /**
     * Override to provide field declaration qualifier 
     * for auto-increment fields. By default this method
     * returns AUTO_INCREMENT.
     */
    protected String autoIncrementDeclaration() {
    	return "AUTO_INCREMENT";
    }
    
    /**
     * Gets a <code>PreparedStatement</code> instance that can be
     * used to retrieve the auto-increment value of a record just
     * inserted.
     * @param c A Connection instance.
     */
    protected abstract PreparedStatement getAutoIncrementRetrievalStatement(Connection c) throws SQLException;
    
    /**
     * Returns the column number of the auto-increment value 
     * found in the result set of the query provided by
     * <code>getAutoIncrementRetrievalStatement</code>. Override
     * if the column number is anything other than 1.
     */
    protected int autoIncrementColumnNumber() {
    	return 1;
    }

    /**
     * Inserts a record in a table. There should be no need
     * to override this method.
     */
    public Long insert(String tableName, String idField, String[] normalFieldNames, Object[] values, boolean autoIncrementRequested) throws SQLException {
    	if(normalFieldNames.length != values.length) {
    		throw new IllegalArgumentException("Length of fieldNames and values differ.");
    	}
    	if(logger.isLoggable(Level.INFO)) {
    		logger.info("insert(): tableName=" + tableName + ",idField=" + idField + ",normalFieldNames=" + Arrays.asList(normalFieldNames) + ",values=" + Arrays.asList(values) + ",autoIncrementRequested=" + autoIncrementRequested);
    	}
    	boolean autoIncrement = autoIncrementRequested && this.supportsAutoIncrement();
    	ConnectionPool.ConnectionWrapper c = this.getConnectionWrapper();
    	try {
    		StringBuffer sqlBuffer = new StringBuffer();
    		sqlBuffer.append("INSERT INTO ");
    		sqlBuffer.append(this.normalizeName(tableName));
    		sqlBuffer.append(" (");
    		if(!autoIncrement) {
    			sqlBuffer.append(this.normalizeName(idField));
    		}
    		for(int i = 0; i < normalFieldNames.length; i++) {
    			if(!autoIncrement || i > 0) {
    				sqlBuffer.append(",");
    			}
    			sqlBuffer.append(normalFieldNames[i]);
    		}
    		sqlBuffer.append(") VALUES (");    		
    		if(!autoIncrement) {
    			sqlBuffer.append("?");
    		}
    		for(int i = 0; i < values.length; i++) {
    			if(!autoIncrement || i > 0) {
    				sqlBuffer.append(",");
    			}
    			sqlBuffer.append("?");
    		}
    		sqlBuffer.append(")");    		
    		PreparedStatement ps = c.prepareStatement(sqlBuffer.toString(), java.sql.ResultSet.TYPE_FORWARD_ONLY,
                    java.sql.ResultSet.CONCUR_UPDATABLE);
    		int extra = autoIncrement ? 1 : 2;
    		Long recordID = null;
    		if(!autoIncrement) {
    			recordID = Hash.generateLong();
    			setParameter(ps, 1, recordID);
    		}
    		for (int i = 0; i < values.length; i++) {
    			Object objVal = values[i];
    			try {
    				setParameter (ps, i + extra, objVal);
    			} catch (SQLException se) {
    				SQLException se2 = new SQLException ("Unable to set parameter # " + (i + extra) + " of type " + (objVal == null ? "<null>" : objVal.getClass().getName()) + ". SQL: " + sqlBuffer.toString() + ". Auto-increment: " + autoIncrement + ". Extra=" + extra + ". Reason: " + se.getMessage() + ".", se.getSQLState(), se.getErrorCode());
    				se.setNextException (se);
    				throw se2;
    			}
    		}
    		ps.executeUpdate();
    		if(autoIncrement) {
    			PreparedStatement aips = this.getAutoIncrementRetrievalStatement(c.getConnection());
    			ResultSet airs = aips.executeQuery();
    			try {
    				while(airs.next()) {
    					recordID = airs.getLong(this.autoIncrementColumnNumber());
    					break;
    				}
    			} finally {
    				airs.close();
    			}    			
    			if(recordID == null) {
    				throw new SQLException("Auto-increment failed to produce a resulting ID.");
    			}
    		}
   			return recordID;
    	} finally {
    		c.release();
    	}
    }

    /**
     * Sets a <code>PrepartedStatement</code> parameter. Override if this method
     * or the <code>setObject</code> method of <code>PrepartedStatement<code>
     * are not setting parameter values as expected.
     */
    protected void setParameter (PreparedStatement ps, int index, Object objVal) throws SQLException {
    	if (objVal instanceof String) {
    		ps.setString (index, (String) objVal);
    	}
    	else if(objVal instanceof Integer) {
    		ps.setInt(index, (Integer) objVal);
    	}
    	else if(objVal instanceof Long) {
    		ps.setLong(index, (Long) objVal);
    	}
    	else if(objVal instanceof Boolean) {
    		ps.setBoolean(index, (Boolean) objVal);
    	}
    	else if (objVal instanceof byte[]) {
    		ps.setBytes (index, (byte[]) objVal);
    	}
    	else {
    		ps.setObject (index, objVal);
    	}
    }

    /**
     * Given a ResultSet, this method returns an iterator of BeanWrapper's.
     * There should be no need to override this method.
     */
    protected ResultsIterator<BeanWrapper> getBeanIterator(final ConnectionPool.ConnectionWrapper conn, final ResultSet resultSet, final BeanUnmarshaller unmarshaller) {
    	final String idField = this.normalizeName(unmarshaller.getIDField());
    	return new ResultsIterator<BeanWrapper>() {
    		private Boolean hasNext;

    		public void finalize() throws Throwable {
    			super.finalize();
    			if (this.hasNext()) {
    				this.close();
    			}
    		}

    		public void close() {
    			try {
    				resultSet.close();
    			} catch(Exception err) {
    				// Shouldn't throw SQLException
    				logger.log(Level.WARNING, "close(): Unable to close ResultSet", err);
    			}
    			conn.release();
    		}

    		public boolean hasNext() {
    			boolean hn;
    			if (hasNext == null) {
    				try {
    					hn = resultSet.next();
    				} catch (SQLException se) {
    					throw new RuntimeException ("Unable to check next due to ", se);
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
    				} catch (SQLException err) {
        				logger.log(Level.WARNING, "hasNext(): Unable to close ResultSet", err);
    				}
    			}
    			return hn;
    		}

    		public BeanWrapper next() {
    			if (!hasNext()) {
    				throw new NoSuchElementException();
    			}
    			try {
    				return getBeanWrapper(resultSet, unmarshaller, idField);
    			} catch (SQLException se) {
    				throw new RuntimeException ("Unable to retrieve data", se);
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
     * Converts current ResultSet data into a BeanWrapper.
     * There should be no need to override this method.
     */
    protected BeanWrapper getBeanWrapper(ResultSet resultSet, BeanUnmarshaller unmarshaller, String idField) throws SQLException {
    	//Map map = createEmptyRecordMap();
    	Long id = resultSet.getLong(idField);
    	if(id == null) {
    		throw new SQLException("Value of field " + idField + " is null.");
    	}
    	Object bean = unmarshaller.registerBean(id);
    	ResultSetMetaData md = resultSet.getMetaData();
    	int cc = md.getColumnCount();
    	// Column numbers start at 1.
    	for (int i = 1; i <= cc; i++) {
    		String name = md.getColumnName (i);
    		int type = md.getColumnType(i);
    		Object fieldValue = getResultSetValue (i, name, type, resultSet); 
    		if(!name.equals(idField)) {
    			unmarshaller.setProperty(bean, name, fieldValue);
    		}
    	}
    	return new BeanWrapper(id, bean);
    }

//    protected Map createEmptyRecordMap() {
//    	return new HashMap(20);
//    }

    /**
     * Gets an object from the ResultSet. Override if the ResultSet is
     * not returing an object of the desired type.
     */
    protected Object getResultSetValue (int i, String name, int type, ResultSet resultSet) throws SQLException {
    	if (isDecimal(type)) {
    		double value = resultSet.getDouble(i);
    		return (resultSet.wasNull() ? null : new Double(value));
    	}
    	else if (isLong(type)) {
    		long value = resultSet.getLong(i);
    		return (resultSet.wasNull() ? null : new Long(value));
    	}
    	else {
    		return resultSet.getObject(i);
    	}
    }

    /**
     * Gets a connection wrapper from the internal
     * connection pool. For transactions to work properly,
     * JDBC connections must be obtained by invoking this method.
     * There should be no need to
     * override this method.
     */
    protected ConnectionPool.ConnectionWrapper getConnectionWrapper() throws SQLException {
    	ConnectionPool.ConnectionWrapper tcwrapper = (ConnectionPool.ConnectionWrapper) this.transactionedConnection.get();
    	if(tcwrapper != null) {
    		return tcwrapper;
    	}
    	return this.pool.getConnectionWrapper(); 
    }

    /**
     * Begins a transaction.
     */
    public final void beginTransaction(int transactionIsolationLevel) throws SQLException {
    	ConnectionPool.ConnectionWrapper tcwrapper = (ConnectionPool.ConnectionWrapper) this.transactionedConnection.get();
    	if(tcwrapper != null) {
    		throw new SQLException("Already in transaction block. Nested transaction blocks not supported.");
    	}
    	tcwrapper = this.pool.getConnectionWrapper();
    	Connection c = tcwrapper.getConnection();
    	c.setTransactionIsolation(transactionIsolationLevel);
    	c.setAutoCommit(false);
    	this.transactionedConnection.set(tcwrapper);
    }

    /**
     * Ends a transaction.
     */
    public final void endTransaction() throws SQLException {
    	ConnectionPool.ConnectionWrapper tcwrapper = (ConnectionPool.ConnectionWrapper) this.transactionedConnection.get();
    	if(tcwrapper == null) {
    		throw new SQLException("Not in a transaction.");
    	}    	
   		Connection c = tcwrapper.getConnection();
   		c.commit();
   		this.transactionedConnection.set(null);
   		c.setAutoCommit(true);
   		if(this.shouldCloseConnectionAfterTransaction()) {
   			tcwrapper.destroy();
   		}
   		else {
   			tcwrapper.release();
   		}
    }
    
    /**
     * Determines if the current thread is executing
     * within a transaction.
     */
    public final boolean inTransaction() {
    	return this.transactionedConnection.get() != null;
    }
    
    /**
     * Determines if the database connection should be closed
     * after a commit. This default implemetation returns
     * <code>true</code>.
     * Override if a connection where a transaction just occurred
     * can still be used.
     */
    public boolean shouldCloseConnectionAfterTransaction() {
    	return true;
    }
    
    public final Savepoint setSavepoint(String savepointName) throws SQLException {
    	ConnectionPool.ConnectionWrapper tcwrapper = (ConnectionPool.ConnectionWrapper) this.transactionedConnection.get();
    	if(tcwrapper == null) {
    		throw new SQLException("Not in a transaction.");
    	}    	
    	return tcwrapper.getConnection().setSavepoint(savepointName);
    }
    
    public final void rollback(Savepoint savepoint) throws SQLException {
    	ConnectionPool.ConnectionWrapper tcwrapper = (ConnectionPool.ConnectionWrapper) this.transactionedConnection.get();
    	if(tcwrapper == null) {
    		throw new SQLException("Not in a transaction.");
    	}    	
    	tcwrapper.getConnection().rollback(savepoint);
    }
    
    private class LockInfo {
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

    private static class LockEntry {
    	public Object lock;
    	public boolean startedTransaction;
    	
		public LockEntry(Object lock, boolean startedTransaction) {
			super();
			this.lock = lock;
			this.startedTransaction = startedTransaction;
		}    	
    }

    protected class TypeInfo {
    	private final String typeName;
    	private final int precision;
    	private final int sqlType;

    	public TypeInfo (int sqlType, String typeName, int precision) {
    		this.sqlType = sqlType;
    		this.typeName = typeName;
    		this.precision = precision;
    	}

    	public final String getDeclaration() {
    		if (this.sqlType == Types.VARCHAR || this.sqlType == Types.VARBINARY) {
    			return this.typeName + "(" + this.precision + ")";
    		}
    		return this.typeName;
    	}	    
    }
}

