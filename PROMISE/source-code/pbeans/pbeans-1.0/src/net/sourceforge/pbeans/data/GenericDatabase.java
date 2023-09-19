package net.sourceforge.pbeans.data;

import java.sql.*;
import javax.sql.*;

/**
 * A database abstraction for an RDBMS unknown to DefaultDatabaseFactory.
 */
public class GenericDatabase extends AbstractDatabase {
    public GenericDatabase (DataSource source) throws SQLException {
	super (source);
    }

    /**
     * Returns an ALTER TABLE REMOVE statement followed by an ALTER TABLE ADD statement.
     * This method will not rename or change field types in a way that preserves
     * column data. It is preferable to extend AbstractDatabase and provide
     * proper change and rename methods.
     */
    protected PreparedStatement[] getChangeFieldStatements (Connection c, String tableName, FieldDescriptor fd) throws SQLException {
	this.warn ("getChangeFieldStatements(): GenericDatabase removes fields in order to change names or types.");
	PreparedStatement ps1 = getRemoveFieldStatement (c, tableName, fd.getName());
	PreparedStatement ps2 = getCreateFieldStatement (c, tableName, fd);
	return new PreparedStatement[] { ps1, ps2 };
    }

    public boolean isDuplicateEntryError (SQLException se) {
        String message = se.getMessage();
        boolean isDuplicate = message != null && message.toLowerCase().indexOf("duplicate") != -1;
        try {
            this.warn ("isDuplicateEntryError(): GenericDatabase assuming isDuplicate=" + isDuplicate + " for SQLException: " + message);
        } catch (SQLException se2) {
            // ignore
        }
        return isDuplicate;        
    }

    protected PreparedStatement[] getRenameFieldStatements (Connection c, String tableName, String oldName, FieldDescriptor fd) throws SQLException {
        throw new UnsupportedOperationException ("getRenameFieldStatements is not supported by GenericDatabase. You need to override AbstractDatabase and DatabaseFactory if you must request field renaming.");
    }

}
