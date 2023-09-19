package net.sourceforge.pbeans.data;

import java.util.*;
import java.sql.*;
import javax.sql.*;

/**
 * An abstraction for the MySQL database.
 */
public class MySQLDatabase extends AbstractDatabase {
    public MySQLDatabase (DataSource source) throws SQLException {
	super (source);
    }

    /**
     * Calls <code>getRenameFieldStatements</code>.
     */
    protected PreparedStatement[] getChangeFieldStatements (Connection c, String tableName, FieldDescriptor fd) throws SQLException {
        return getRenameFieldStatements (c, tableName, fd.getName(), fd);
    }
 
    /**
     * Uses ALTER TABLE ... CHANGE ... syntax.
     */
    protected PreparedStatement[] getRenameFieldStatements (Connection c, String tableName, String oldName, FieldDescriptor fd) throws SQLException {
	PreparedStatement ps1 = c.prepareStatement ("ALTER TABLE " + tableName + " CHANGE " + oldName + " " + fd.getName() + " " + getTypeDeclaration(fd.getSqlType()) + (fd.isNullable() ? " NULL" : " NOT NULL"));
	return new PreparedStatement[] { ps1 };
    }

    /**
     * Uses DROP INDEX ... ON ... syntax.
     */
    protected PreparedStatement[] getDropIndexStatements (Connection c, String tableName, String name) throws SQLException  {
	PreparedStatement ps1 = c.prepareStatement ("DROP INDEX " + name + " ON " + tableName);
	return new PreparedStatement[] { ps1 };
    }

    /**
     * Checks if error code is 1062.
     */
    public boolean isDuplicateEntryError (SQLException se) {
	return se.getErrorCode() == 1062;
    }
}
