package net.sourceforge.pbeans.data;

import java.sql.*;
import java.util.*;
import javax.sql.*;

/**
 * Interface intended to be an abstraction for a any database.
 */
public interface Database {
    int getMaxTableNameLength() throws SQLException;
    int getMaxColumnNameLength() throws SQLException;

    /**
     * Ensures that a table exists and modifies fields and indexes as
     * needed.
     * @param tableName Name of the table.
     * @param fixedDescriptor A FieldDescriptor which cannot be removed later (typically the table's primary key.)
     * @param fieldDescriptors A collection of FieldDescriptor instances.
     * @param indexDescriptors A collection of IndexDescriptor instances.
     */
    void ensureTableExists (String tableName, FieldDescriptor fixedDescriptor, Collection fieldDescriptors, Collection indexDescriptors) throws SQLException;

    /**
     * Performs a database query.
     * @param sql A SQL SELECT statement with question marks (?) as placeholders.
     * @param parameters An array of parameters whose length is equal to the number of placeholders in the SQL statement.
     * @see net.sourceforge.pbeans.data.Database#update
     */
    public ResultsIterator query (String sql, Object[] parameters) throws SQLException;

    /**
     * Executes a database statement.
     * @param sql A SQL modification statement with question marks (?) as placeholders.
     * @param parameters An array of parameters whose length is equal to the number of placeholders in the SQL statement.
     * @see net.sourceforge.pbeans.data.Database#query
     */
    public int update (String sql, Object[] parameters) throws SQLException;

    /**
     * Requests a record lock. The lock must be given up at some point 
     * by invoking <code>relinquishLock</code>.
     */
    Object requestLock (String tableName, String fieldName, Object value) throws InterruptedException, SQLException;

    /**
     * Gives up a lock previously obtained by invoking <code>requestLock</code>.
     */
    void relinquishLock (Object lock) throws SQLException;

    /**
     * Determines whether a SQLException corresponds to a duplicate record error.
     */
    boolean isDuplicateEntryError (SQLException se);
}
