package net.sourceforge.pbeans.data;

import java.sql.*;
import javax.sql.*;

/**
 * Base implementation of the DatabaseFactory interface.
 */
public class DefaultDatabaseFactory implements DatabaseFactory {
    private int maxConnections = 100;
    private int timeout = 10 * 60 * 1000;
    private boolean deleteFields;

    /**
     * Sets the maximum number of simultaneously open connections 
     * that a Database is expected to hold. Default is 100.
     * This setting only applies to subsequent Database creations.
     */
    public void setMaxConnections (int maxConnections) {
	this.maxConnections = maxConnections;
    }

    /**
     * Sets the amount of time, in milliseconds, that a
     * connection may be idle before it is discarded. Default is 600000 (10 minutes.)
     * This setting only applies to subsequent Database creations.
     */
    public void setConnectionTimeout (int timeout) {
	this.timeout = timeout;
    }

    /**
     * Sets whether table fields no longer in use should be deleted.
     * This setting only applies to subsequent Database creations.
     */
    public void setDeleteFields (boolean value) {
	this.deleteFields = value;
    }

    /**
     * Implements the DatabaseFactory interface. It looks at
     * <code>DatabaseMetaData.getDatabaseProductName</code> to determine
     * which class to instantiate. This method may create
     * instances of MySQLDatabase, SQLServerDatabase, and
     * GenericDatabase.
     */
    public Database getDatabase (DataSource store) throws SQLException {
	Connection c = store.getConnection();
	try {
	    DatabaseMetaData dmd = c.getMetaData();
	    String name = dmd.getDatabaseProductName();
	    Database db;
	    if (name.equalsIgnoreCase ("mysql")) {
		db = new MySQLDatabase (store);
	    }
	    else if (name.toLowerCase().indexOf("sql server") != -1) {
		db = new SQLServerDatabase (store);
	    }
	    else {
		System.out.println ("WARN: DatabaseFactory: Unknown database name: " + name + ". Will use GenericDatabase, but it might not work properly.");
		db = new GenericDatabase (store);
	    }
	    if (db instanceof AbstractDatabase) {
		AbstractDatabase bdb = (AbstractDatabase) db;
		bdb.setMaxConnections (this.maxConnections);
		bdb.setConnectionTimeout (this.timeout);
	    }
	    return db;
	} finally {
	    c.close();
	}
    }
}
