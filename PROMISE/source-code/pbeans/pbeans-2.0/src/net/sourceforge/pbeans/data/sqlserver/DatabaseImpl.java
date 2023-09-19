package net.sourceforge.pbeans.data.sqlserver;

import java.sql.*;

import javax.sql.*;

import net.sourceforge.pbeans.data.AbstractDatabase;
import net.sourceforge.pbeans.data.FieldDescriptor;

import java.io.*;

/**
 * An abstraction for the SQL Server database.
 */
public class DatabaseImpl extends AbstractDatabase {
    public DatabaseImpl () throws SQLException {
    	super();
    }

    @Override
	public void init(DataSource dataSource) throws SQLException {
    	super.init(dataSource);
	}
    
    @Override
	protected PreparedStatement getAutoIncrementRetrievalStatement(Connection c) throws SQLException {
    	throw new UnsupportedOperationException();
	}

	protected PreparedStatement[] getChangeFieldStatements (Connection c, String tableName, FieldDescriptor fd, boolean autoIncrement) throws SQLException {
        String cfs = "ALTER TABLE " + tableName + " ALTER COLUMN " + fd.getName() + " " + getTypeDeclaration(fd.getSqlType()) + (fd.isNullable() ? " NULL" : "NOT NULL");
        PreparedStatement ps1 = c.prepareStatement (cfs);
        return new PreparedStatement[] { ps1 };
    }

    /**
     * Attempts to rename field and set other info.
     */
    protected PreparedStatement[] getRenameFieldStatements (Connection c, String tableName, String oldName, FieldDescriptor fd, boolean autoIncrement) throws SQLException {
        String cfs3 = "EXEC sp_rename '" + tableName + ".[" + oldName + "]', ?, ?";
        PreparedStatement ps3 = c.prepareStatement (cfs3);
        ps3.setString (1, fd.getName());
        ps3.setString (2, "COLUMN");
        PreparedStatement[] cfs = getChangeFieldStatements (c, tableName, fd, autoIncrement);
        return new PreparedStatement[] { ps3, cfs[0] };
    }

    /**
     * Checks if error code is 2601.
     */
    public boolean isDuplicateEntryError (SQLException se) {
	return se.getErrorCode() == 2601;
    }

    protected String getTypeDeclaration (int dataType) throws SQLException {
	if (dataType == Types.BLOB || dataType == Types.VARBINARY) {
            // There's an issue with byte[] and JTDS.
	    return getTypeDeclaration (Types.VARCHAR);
	}
        if (dataType == Types.DOUBLE) {
            return "DECIMAL";
        }
        else if (dataType == Types.FLOAT) {
            return "DECIMAL";
        }
        else {
            return super.getTypeDeclaration (dataType);
        }
    }

    protected boolean isLong (int type) {
        return type == Types.NUMERIC || super.isLong(type);
    }

    protected Object getResultSetValue (int i, String name, int type, ResultSet resultSet) throws SQLException {
        if (type == Types.NUMERIC) {
            long value = resultSet.getLong(i);
            return (resultSet.wasNull() ? null : new Long(value));
        }
        else {
            return super.getResultSetValue (i, name, type, resultSet);
        }
    }

    protected void setParameter (PreparedStatement ps, int index, Object objVal) throws SQLException {
        if (objVal instanceof String) {
            ps.setString (index, (String) objVal);
        }
        else if (objVal instanceof Character) {
            ps.setString (index, String.valueOf(objVal));
        }
        else if (objVal instanceof byte[]) {
            // JTDS doesn't like setting byte arrays.
            String s;
            try {
                s = new String((byte[]) objVal, "8859_1");
            } catch (UnsupportedEncodingException ue) {
                throw new SQLException ("Unsupported encoding: " + ue.getMessage());
            }
            ps.setString (index, s);
        }
        else {
            super.setParameter (ps, index, objVal);
        }
    }

    

}
