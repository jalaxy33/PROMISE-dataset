package net.sourceforge.pbeans.data.mysql;

import java.sql.*;

import javax.sql.*;

import net.sourceforge.pbeans.data.AbstractDatabase;
import net.sourceforge.pbeans.data.FieldDescriptor;

/**
 * An abstraction for the MySQL database.
 */
public class DatabaseImpl extends AbstractDatabase {
	public DatabaseImpl() throws SQLException {
	}

    @Override
	public void init(DataSource dataSource) throws SQLException {
    	super.init(dataSource);
	}
    
	@Override
	protected boolean supportsAutoIncrement() {
		return true;
	}
	
	@Override 
    public String getAutomaticPrimaryKeyIndexName() {
    	return "PRIMARY";
    }

	@Override
    protected PreparedStatement getCreateTableStatement (Connection c, String tableName, FieldDescriptor primaryFd, boolean autoIncrementRequested) throws SQLException {
    	return c.prepareStatement ("CREATE TABLE " + tableName + " (" + primaryFd.getName() + " " + getTypeDeclaration(primaryFd.getSqlType()) + (primaryFd.isNullable() ? " NULL" : " NOT NULL") + (autoIncrementRequested ? " AUTO_INCREMENT" : "") + (primaryFd.isPrimaryKey() ? " PRIMARY KEY" : "") + ")");
    }

	/**
	 * Calls <code>getRenameFieldStatements</code>.
	 */
    @Override
	protected PreparedStatement[] getChangeFieldStatements (Connection c, String tableName, FieldDescriptor fd, boolean autoIncrement) throws SQLException {
		return getRenameFieldStatements (c, tableName, fd.getName(), fd, autoIncrement);
	}

	@Override
    protected PreparedStatement[] getCreateFieldStatements (Connection c, String tableName, FieldDescriptor fd, boolean autoIncrement) throws SQLException {
    	return new PreparedStatement[] { c.prepareStatement ("ALTER TABLE " + tableName + " ADD " + fd.getName() + " " + getTypeDeclaration(fd.getSqlType()) + (fd.isNullable() ? " NULL" : " NOT NULL") + (autoIncrement ? " AUTO_INCREMENT" : "")) };
    }

    /**
	 * Uses ALTER TABLE ... CHANGE ... syntax.
	 */
	@Override
	protected PreparedStatement[] getRenameFieldStatements (Connection c, String tableName, String oldName, FieldDescriptor fd, boolean autoIncrement) throws SQLException {
		PreparedStatement ps1 = c.prepareStatement ("ALTER TABLE " + tableName + " CHANGE " + oldName + " " + fd.getName() + " " + getTypeDeclaration(fd.getSqlType()) + (fd.isNullable() ? " NULL" : " NOT NULL") + (autoIncrement ? " AUTO_INCREMENT" : "") + (fd.isPrimaryKey() ? " PRIMARY KEY" : ""));
		return new PreparedStatement[] { ps1 };
	}

	
	/**
	 * Uses DROP INDEX ... ON ... syntax.
	 */
	@Override
	protected PreparedStatement[] getDropIndexStatements (Connection c, String tableName, String name) throws SQLException  {
		PreparedStatement ps1 = c.prepareStatement ("DROP INDEX " + name + " ON " + tableName);
		return new PreparedStatement[] { ps1 };
	}

	/**
	 * Checks if error code is 1062.
	 */
	@Override
	public boolean isDuplicateEntryError (SQLException se) {
		return se.getErrorCode() == 1062;
	}

	/** 
	 * Deals with BLOB in a special way. For everything else,
	 * it just invokes super.getTypeDeclaration.
	 */
	@Override
	protected String getTypeDeclaration (int dataType) throws SQLException {
		//TODO: If TEXT is used, indexes have to specify a key
		//length. Existing indexes may not.
		
		//if(dataType == Types.VARCHAR) {
		//	return "TEXT";
		//}
		//else 
		if (dataType == Types.BLOB) {
			return "BLOB";
		}
		if (dataType == Types.VARBINARY) {
			return "BLOB";
		}
		else if (dataType == Types.BOOLEAN) {
			return "TINYINT(1)";
		}
		else if (dataType == Types.FLOAT) {
			return "DECIMAL";
		}
		else {
			return super.getTypeDeclaration (dataType);
		}
	}

	@Override
    protected PreparedStatement getAutoIncrementRetrievalStatement(Connection c) throws SQLException {
    	return c.prepareStatement("SELECT LAST_INSERT_ID()");
    }

	@Override
    protected boolean requiresKeyLength() {
    	return true;
    }

	@Override
    public boolean shouldCloseConnectionAfterTransaction() {
		return false;
    }

	@Override
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
    	else if (objVal instanceof Boolean) {
    		ps.setBoolean(index, (Boolean) objVal);
    	}
    	else if (objVal instanceof byte[]) {
    		ps.setBytes (index, (byte[]) objVal);
    	}
    	else if (objVal instanceof Character) {
    		ps.setString(index, String.valueOf(objVal));
    	}
    	else {
    		super.setParameter(ps, index, objVal);
    	}
    }

}
