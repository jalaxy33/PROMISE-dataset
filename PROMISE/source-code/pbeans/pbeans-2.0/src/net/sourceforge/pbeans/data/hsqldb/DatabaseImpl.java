package net.sourceforge.pbeans.data.hsqldb;

import java.sql.*;
import java.util.*;
import javax.sql.*;

import net.sourceforge.pbeans.data.AbstractDatabase;
import net.sourceforge.pbeans.data.FieldDescriptor;
import net.sourceforge.pbeans.data.IndexDescriptor;
import net.sourceforge.pbeans.data.ResultsIterator;

/**
 * A database abstraction for HSQLDB since 1.7.2.
 * Contributed by 
 */
public class DatabaseImpl extends AbstractDatabase {
    private final Random RAND = new Random(System.currentTimeMillis());
    
    public DatabaseImpl() throws SQLException {
    }
    
    @Override
	public void init(DataSource dataSource) throws SQLException {
    	this.init(dataSource);
	}
    
    
	/**
     * Changes field type and other settings by copying data to a temporary field.
     */
    protected PreparedStatement[] getChangeFieldStatements(Connection c, String tableName, FieldDescriptor fd, boolean autoIncrement) throws SQLException {
        String randomName = "tmp_" + Math.abs(RAND.nextInt());
        PreparedStatement[] ps1 = getRenameFieldStatements(c, tableName, fd.getName(), new FieldDescriptor(randomName, fd), false);
        PreparedStatement[] ps2 = getRenameFieldStatements(c, tableName, randomName, fd, autoIncrement);
        PreparedStatement[] result = new PreparedStatement[ps1.length + ps2.length];
        for (int i = 0; i < ps1.length; i++) {
            result[i] = ps1[i];
        }
        for (int i = 0; i < ps2.length; i++) {
            result[i + ps1.length] = ps2[i];
        }
        return result;
    }
    
    /**
     * Renames field and changes other settings by copying data to a temporary field.
     */
    protected PreparedStatement[] getRenameFieldStatements(Connection c, String tableName, String oldName, FieldDescriptor fd, boolean autoIncrement) throws SQLException {
        PreparedStatement[] pss1 = getCreateFieldStatements(c, tableName, fd, autoIncrement);
        PreparedStatement ps2 = c.prepareStatement("UPDATE " + tableName + " SET " + fd.getName() + "=" + oldName);
        PreparedStatement ps3 = getRemoveFieldStatement(c, tableName, oldName);
		int length = pss1.length + 2;
		PreparedStatement[] result = new PreparedStatement[length];
		int index;
		for(index = 0; index < pss1.length; index++) {
			result[index] = pss1[index];
		}
		result[index++] = ps2;
		result[index++] = ps3;
		return result;
    }
    
    /**
     * Checks for SQL state 23505 in case SQL99 codes are supported,
     * otherwise makes assumptions based on exception message.
     */
    public boolean isDuplicateEntryError(SQLException se) {        
        if (this.sqlStateType == DatabaseMetaData.sqlStateSQL99) {
        	return "23505".equals(se.getSQLState());
        }
        String message = se.getMessage();
        boolean isDuplicate = false;
        isDuplicate |= message != null && message.toLowerCase().indexOf("duplicate") != -1;
        isDuplicate |= (se.getErrorCode() == -23);
        isDuplicate |= (se.getErrorCode() == -21);
        return isDuplicate;
    }
    
    protected String getTypeDeclaration(int dataType) throws SQLException {
        if (dataType == Types.VARBINARY || dataType == Types.BINARY || dataType == Types.BLOB) {
            return "binary";
        }
        else if (dataType == Types.FLOAT || dataType == Types.DOUBLE) {
            return "double";
        }
        else if (dataType == Types.VARCHAR) {
            return "varchar";
        }
        else if (dataType == Types.CHAR) {
            return "char";
        }
        else {
            return super.getTypeDeclaration(dataType);
        }
    }
    
    /**
     * Gets a Statement that removes a field.
     */
    protected PreparedStatement getRemoveFieldStatement(Connection c, String tableName, String columnName) throws SQLException {
        final String sqlCmd = "ALTER TABLE " + tableName + " DROP COLUMN " + columnName;
        PreparedStatement ps = c.prepareStatement(sqlCmd);
        return ps;
    }
    
    /**
     * Gets a Statement that adds a field to a table.
     */
    protected PreparedStatement getCreateFieldStatement(Connection c, String tableName, FieldDescriptor fd) throws SQLException {
        final String sqlCmd = "ALTER TABLE " + tableName + " ADD COLUMN " + fd.getName() + " " + getTypeDeclaration(fd.getSqlType());
        PreparedStatement ps = c.prepareStatement(sqlCmd);
        return ps;
    }
    
    public String normalizeName(String s) {
    	if(s == null) {
    		return null;
    	}
        return s.toUpperCase();
    }
    
    protected IndexDescriptor normalizeIndex(String normalTableName, IndexDescriptor id) {
        IndexDescriptor nid = new IndexDescriptor(normalTableName + "_" + id.getName().toUpperCase(), id.isUnique(), new String[0], id.getKeyLength());
        Collection rawFieldNames = id.getFieldNames();
        for (Iterator i = rawFieldNames.iterator(); i.hasNext();) {
            String rawFieldName = (String) i.next();
            nid.addFieldName(rawFieldName.toUpperCase());
        }
        return nid;
    }
    
    protected Map createEmptyRecordMap() {
    	return new UpperCaseMap();
    }
    
    @Override
	protected PreparedStatement getAutoIncrementRetrievalStatement(Connection c) throws SQLException {
    	throw new java.lang.UnsupportedOperationException();
	}

	/**
     * A simple helper class for hsqldb field names.
     * Note: hsqldb works internally with upper case names
     */
    static class UpperCaseMap extends HashMap {
    	public boolean containsKey (Object key) {
    		if (key instanceof String) {
    			return super.containsKey (((String) key).toUpperCase());
    		}
    		return super.containsKey (key);
    	}

    	public Object get (Object key) {
    		if (key instanceof String) {
    			return super.get (((String) key).toUpperCase());
    		}
    		return super.get (key);
    	}
    }
}
