package net.sourceforge.pbeans.data;

import java.util.*;
import java.sql.*;
import javax.sql.*;

/**
 * An abstraction for the SQL Server database.
 */
public class SQLServerDatabase extends AbstractDatabase {
    public SQLServerDatabase (DataSource source) throws SQLException {
	super (source);
    }

    protected PreparedStatement[] getChangeFieldStatements (Connection c, String tableName, FieldDescriptor fd) throws SQLException {
        String cfs = "ALTER TABLE " + tableName + " ALTER COLUMN " + fd.getName() + " " + getTypeDeclaration(fd.getSqlType()) + (fd.isNullable() ? " NULL" : "NOT NULL");
	PreparedStatement ps1 = c.prepareStatement (cfs);
	return new PreparedStatement[] { ps1 };
    }

    /**
     * Attempts to rename field by modifying catalog tables.
     */
    protected PreparedStatement[] getRenameFieldStatements (Connection c, String tableName, String oldName, FieldDescriptor fd) throws SQLException {
        String cfs3 = "EXEC sp_rename '" + tableName + ".[" + oldName + "]', ?, ?";
	PreparedStatement ps3 = c.prepareStatement (cfs3);
        ps3.setString (1, fd.getName());
        ps3.setString (2, "COLUMN");
        PreparedStatement[] cfs = getChangeFieldStatements (c, tableName, fd);
	return new PreparedStatement[] { ps3, cfs[0] };
    }

    /**
     * Checks if error code is 2601.
     */
    public boolean isDuplicateEntryError (SQLException se) {
	return se.getErrorCode() == 2601;
    }

    protected String getTypeDeclaration (int dataType) {
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
}
