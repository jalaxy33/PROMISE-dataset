package net.sourceforge.pbeans.data.postgresql;

import java.sql.*;
import java.util.*;
import java.io.*;
import javax.sql.*;

import net.sourceforge.pbeans.data.AbstractDatabase;
import net.sourceforge.pbeans.data.FieldDescriptor;
import net.sourceforge.pbeans.data.IndexDescriptor;

import java.util.logging.*;

/**
 * A Database implementation for PostgreSQL.
 */
public class DatabaseImpl extends AbstractDatabase {
	private static final Logger logger = Logger.getLogger(DatabaseImpl.class.getName());
	private static final Random RAND = new Random(System.currentTimeMillis());

	public DatabaseImpl() throws SQLException {
	}

    @Override
	public void init(DataSource dataSource) throws SQLException {
    	super.init(dataSource);
	}
    
	@Override
	protected PreparedStatement getAutoIncrementRetrievalStatement(Connection c) throws SQLException {
		throw new UnsupportedOperationException();
	}

	/**
	 * Changes field type and other settings by copying data to a temporary field.
	 */
	@Override
	protected PreparedStatement[] getChangeFieldStatements (Connection c, String tableName, FieldDescriptor fd, boolean autoIncrement) throws SQLException {
		String randomName = "tmp_" + Math.abs(RAND.nextInt());
		String normalFieldName = this.normalizeName(fd.getName());
		PreparedStatement ps1 = c.prepareStatement ("ALTER TABLE " + tableName + " RENAME " + normalFieldName + " TO " + randomName);
		PreparedStatement[] ps2 = getRenameFieldStatements (c, tableName, randomName, fd, autoIncrement);
		PreparedStatement[] result = new PreparedStatement[1 + ps2.length];
		result[0] = ps1;
		for (int i = 0; i < ps2.length; i++) {
			result[1 + i] = ps2[i];
		}
		return result;
	}

	@Override
	protected PreparedStatement[] getCreateFieldStatements (Connection c, String normalTableName, FieldDescriptor fd, boolean autoIncrement) throws SQLException {
		return this.getCreateFieldStatements(c, normalTableName, fd, autoIncrement, true);
	}
	
	private PreparedStatement[] getCreateFieldStatements (Connection c, String normalTableName, FieldDescriptor fd, boolean autoIncrement, boolean nonNullConstraint) throws SQLException {
		// PostgreSQL does not support NOT NULL when adding fields. 
		// Creating field and adding constraint if necessary.		
    	String normalFdName = this.normalizeName(fd.getName());
    	PreparedStatement ps1 = c.prepareStatement ("ALTER TABLE " + normalTableName + " ADD " + normalFdName + " " + getTypeDeclaration(fd.getSqlType()));
    	// NOTE: Does not support nullable alteration.
   		return new PreparedStatement[] { ps1 };
    }
	
    @Override
	protected boolean canSetNullableOnTableAlter() {
    	// Cannot alter field to be nullable in a way
    	// that is reflected in database metadata.
    	// Further, does not deal very well with scenario
    	// where values violate constraint.
    	return false;
	}

	/**
	 * Renames field and changes other settings by copying data to a temporary field.
	 */
	@Override
	protected PreparedStatement[] getRenameFieldStatements (Connection c, String normalTableName, String oldName, FieldDescriptor fd, boolean autoIncrement) throws SQLException {
		String normalFieldName = this.normalizeName(fd.getName());
		PreparedStatement[] pss1 = getCreateFieldStatements (c, normalTableName, fd, autoIncrement, false);
		PreparedStatement ps2 = c.prepareStatement ("UPDATE " + normalTableName + " SET " + normalFieldName + "=" + oldName);
		PreparedStatement ps3 = getRemoveFieldStatement (c, normalTableName, oldName);
		// Add non-null constraint now
		PreparedStatement ps4 = null;
//    	if(!fd.isNullable()) {
//    		//(doesn't work well - not supporting nullable alters)
//    		//ps4 = c.prepareStatement ("ALTER TABLE " + normalTableName + " ADD CHECK (" + normalFieldName + " IS NOT NULL)");		
//    	}		
		int length = pss1.length + 2 + (ps4 == null ? 0 : 1);
		PreparedStatement[] result = new PreparedStatement[length];
		int index;
		for(index = 0; index < pss1.length; index++) {
			result[index] = pss1[index];
		}
		result[index++] = ps2;
		result[index++] = ps3;
		if(ps4 != null) {
			result[index++] = ps4;
		}
		return result;
	}

	@Override
	protected PreparedStatement[] getDropIndexStatements (Connection c, String tableName, String name) throws SQLException  {
		PreparedStatement ps1 = c.prepareStatement ("DROP INDEX " + name);
		return new PreparedStatement[] { ps1 };	
	}

	//private static final String NORM_PREFIX = "f$";
	
	@Override
	public String normalizeName (String s) {
		if(s == null) {
			return null;
		}
		String newName = s.toLowerCase();
//		// Check for likely keywords
//		if("name".equals(newName)) {
//			newName = NORM_PREFIX + newName;
//		}
		return newName;
	}	

	@Override
	protected IndexDescriptor normalizeIndex (String normalTableName, IndexDescriptor id) {
		IndexDescriptor nid = new IndexDescriptor (normalTableName + "_" + id.getName().toLowerCase(), id.isUnique(), new String[0], id.getKeyLength());
		Collection rawFieldNames = id.getFieldNames();
		for (Iterator i = rawFieldNames.iterator(); i.hasNext();) {
			String rawFieldName = (String) i.next();
			nid.addFieldName (rawFieldName.toLowerCase());
		}
		return nid;
	}

	/**
	 * Checks if SQLState is 23505.
	 */
	@Override
	public boolean isDuplicateEntryError (SQLException se) {
		try {
			return "23505".equals(se.getSQLState());
		} catch(Exception err) {
			logger.log(Level.WARNING, "Got exception trying to get SQLState from SQLException", err);
			return false;
		}
	}

	@Override
	protected String getTypeDeclaration (int dataType) throws SQLException {
		if (dataType == Types.VARBINARY || dataType == Types.BINARY || dataType == Types.BLOB) {
			return "bytea";
		}
		else if (dataType == Types.FLOAT || dataType == Types.DOUBLE) {
			return "decimal";
		}
		else if (dataType == Types.VARCHAR) {
			return "text";
		}
		else if (dataType == Types.CHAR) {
			return "bytea";
		}
		else if (dataType == Types.BOOLEAN) {
			return "bool";
		}
		else {
			return super.getTypeDeclaration (dataType);
		}
	}

	@Override
	protected void setParameter (PreparedStatement ps, int index, Object objVal) throws SQLException {
		if (objVal instanceof String) {
			ps.setString (index, (String) objVal);
		}
		else if (objVal instanceof Character) {
			try {
				byte[] bytes = String.valueOf(objVal).getBytes("UTF-8");
				if(logger.isLoggable(Level.INFO)) {
				    logger.info("setParameter(): Character bytes.length=" + bytes.length);
				}
				ps.setBytes (index, bytes);
			}
			catch (UnsupportedEncodingException ue) {
				throw new IllegalArgumentException (ue.getMessage());
			}
		}
		else {
			super.setParameter (ps, index, objVal);
		}
	}

	@Override
	protected boolean typesMatch (int type1, int type2) {
		if (isCharacter(type1) && isCharacter(type2)) {
			return true;
		}
		else {
			return super.typesMatch (type1, type2);
		}
	}

	private boolean isCharacter (int sqlType) {
		return sqlType == Types.CHAR || sqlType == Types.BINARY;
	}

	@Override
	protected boolean isDecimal (int type) {
		return type == Types.NUMERIC || super.isDecimal(type);
	}

	@Override
	protected Object getResultSetValue (int i, String name, int type, ResultSet resultSet) throws SQLException {
		if (type == Types.NUMERIC) {
			double value = resultSet.getDouble(i);
			return (resultSet.wasNull() ? null : new Double(value));
		}
		else {
			return super.getResultSetValue (i, name, type, resultSet);
		}
	}

	protected Map createEmptyRecordMap() {
		return new LowerCaseMap();
	}

	static class LowerCaseMap extends HashMap {
		public boolean containsKey (Object key) {
			if (key instanceof String) {
				return super.containsKey (((String) key).toLowerCase());
			}
			else {
				return super.containsKey (key);
			}
		}

		public Object get (Object key) {
			if (key instanceof String) {
				return super.get (((String) key).toLowerCase());
			}
			else {
				return super.get (key);
			}
		}
	}
}
