package net.sourceforge.pbeans.data;

import java.sql.*;
import javax.sql.*;

/**
 * Factory of Database instances.
 */
public interface DatabaseFactory {
    /**
     * Should build a proper Database implementation
     * depending on the underlying RDBMS given by <code>store</code>.
     */
    public Database getDatabase (DataSource store) throws SQLException;
}
