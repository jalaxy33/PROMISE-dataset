package net.sourceforge.pbeans.data;

import java.sql.*;
import java.util.*;

/**
 * Interface intended to be an abstraction for a any database.
 */
public interface Database {
	/**
	 * This method should be invoked as soon as the Database implementation
	 * is instantiated by a <code>DatabaseFactory</code>.
	 * @param dataSource A DataSource instance.
	 * @throws SQLException
	 */
	public void init(javax.sql.DataSource dataSource) throws SQLException;
	
    int getMaxTableNameLength() throws SQLException;
    int getMaxColumnNameLength() throws SQLException;

    /**
     * Ensures that a table exists and modifies fields and indexes as
     * needed.
     * @param tableName Name of the table.
     * @param fixedDescriptor A FieldDescriptor which cannot be removed later (typically the table's primary key.)
     * @param fieldDescriptors A collection of FieldDescriptor instances.
     * @param indexDescriptors A collection of IndexDescriptor instances.
     * @param autoIncrementRequested Whether auto-increment is requested for mandatory field.
     * @param deleteFields Whether fields not requested that were found in the table should be deleted.
     */
    void ensureTableExists (String tableName, FieldDescriptor fixedDescriptor, Collection fieldDescriptors, Collection indexDescriptors, boolean userManaged, boolean autoIncrementRequested, boolean deleteFields) throws SQLException;

    /**
     * Performs a database query.
     * @param sql A SQL SELECT statement with question marks (?) as placeholders.
     * @param parameters An array of parameters whose length is equal to the number of placeholders in the SQL statement.
     * @param unmarshaller A BeanUnmarshaller implementation instance.
     * @see net.sourceforge.pbeans.data.Database#update
     */
    public ResultsIterator<BeanWrapper> query (String sql, Object[] parameters, BeanUnmarshaller unmarshaller) throws SQLException;

    /**
     * Executes a database statement.
     * @param sql A SQL modification statement with question marks (?) as placeholders.
     * @param parameters An array of parameters whose length is equal to the number of placeholders in the SQL statement.
     * @see net.sourceforge.pbeans.data.Database#query
     */
    public int update (String sql, Object[] parameters) throws SQLException;
    
    /**
     * Inserts a record in the database.
     * @param tableName
     * @param idField Name of ID field.
     * @param fieldNames
     * @param values
     * @param autoIncrementRequested Whether auto-increment is requested for the ID field.
     * @return The record ID.
     * @throws SQLException
     */
    public Long insert(String tableName, String idField, String[] fieldNames, Object[] values, boolean autoIncrementRequested) throws SQLException;


    /**
     * Requests a record lock. The lock must be given up at some point 
     * by invoking <code>relinquishLock</code>. Nested locks are allowed.
     */
    void requestLock(String lockName, int transactionIsolationLevel) throws InterruptedException, SQLException;

    /**
     * Gives up a lock previously obtained by invoking <code>requestLock</code>
     * in the current thread.
     */
    void relinquishLock() throws SQLException;

    /**
     * Starts a transaction. Note that nested transactions are not allowed.
     * @param transactionIsolationLevel
     * @throws SQLException
     */
    void beginTransaction(int transactionIsolationLevel) throws SQLException;
    
    /**
     * Ends a transaction.
     */
    void endTransaction() throws SQLException;
    
    public Savepoint setSavepoint(String savepointName) throws SQLException;

    public void rollback(Savepoint savepoint) throws SQLException;

    public boolean inTransaction();

    /**
     * Determines whether a SQLException corresponds to a duplicate record error.
     */
    boolean isDuplicateEntryError (SQLException se);
    
    /**
     * Coverts a field or table name to the name that will actually
     * be used in the database. Usually this will return the <code>itemName</code>
     * parameter unchanged.
     */
    public String normalizeName(String itemName);
}
