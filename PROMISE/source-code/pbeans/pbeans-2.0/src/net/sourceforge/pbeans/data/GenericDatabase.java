package net.sourceforge.pbeans.data;

import java.sql.*;
import java.util.*;
import javax.sql.*;

/**
 * A database abstraction for an RDBMS unknown to DefaultDatabaseFactory.
 * @deprecated No longer used.
 */
public class GenericDatabase extends AbstractDatabase {
    private final Random RAND = new Random (System.currentTimeMillis());

    public GenericDatabase() throws SQLException {
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
    protected PreparedStatement[] getChangeFieldStatements (Connection c, String tableName, FieldDescriptor fd, boolean autoIncrement) throws SQLException {
    	String randomName = "tmp_" + Math.abs(RAND.nextInt());
    	PreparedStatement[] ps1 = getRenameFieldStatements (c, tableName, fd.getName(), new FieldDescriptor (randomName, fd), autoIncrement);
    	PreparedStatement[] ps2 = getRenameFieldStatements (c, tableName, randomName, fd, autoIncrement);
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
    protected PreparedStatement[] getRenameFieldStatements (Connection c, String tableName, String oldName, FieldDescriptor fd, boolean autoIncrement) throws SQLException {
    	PreparedStatement[] pss1 = getCreateFieldStatements (c, tableName, fd, autoIncrement);
    	PreparedStatement ps2 = c.prepareStatement ("UPDATE " + tableName + " SET " + fd.getName() + "=" + oldName);
    	PreparedStatement ps3 = getRemoveFieldStatement (c, tableName, oldName);
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
    public boolean isDuplicateEntryError (SQLException se) {
    	if (this.sqlStateType == DatabaseMetaData.sqlStateSQL99) {
    		return "23505".equals (se.getSQLState());
    	}
    	else {
    		String message = se.getMessage();
    		boolean isDuplicate = message != null && message.toLowerCase().indexOf("duplicate") != -1;
    		return isDuplicate;        
    	}
    }

}
