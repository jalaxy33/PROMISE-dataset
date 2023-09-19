package net.sourceforge.pbeans.data;

import java.sql.*;
import javax.sql.*;

/**
 * Base implementation of the DatabaseFactory interface.
 * 
 */
public class DefaultDatabaseFactory implements DatabaseFactory {
	private int maxConnections = 99;
	private int timeout = 59 * 60 * 1000;
	private final ClassLoader classLoader;

	public DefaultDatabaseFactory(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

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
	 * Implements the DatabaseFactory interface. It looks at
	 * the JDBC URL to determine
	 * which class to instantiate.
	 */
	public Database getDatabase (DataSource dataSource) throws SQLException {
		Connection c = dataSource.getConnection();
		try {			
			DatabaseMetaData dmd = c.getMetaData();
			String url = dmd.getURL().toLowerCase();
			String prefix = "jdbc:";
			if(!url.startsWith(prefix)) {
				throw new java.lang.IllegalArgumentException("Expected URL to start with jdbc: " + url);
			}
			int colonIdx = url.indexOf(':', prefix.length());
			if(colonIdx == -1) {
				throw new java.lang.IllegalArgumentException("Expected second colon in URL: " + url);
			}
			String dbName = url.substring(prefix.length(), colonIdx);
			String expectedClassName = "net.sourceforge.pbeans.data." + dbName + ".DatabaseImpl";
			Database db;
			try {
				Class clazz = this.classLoader == null ? Class.forName(expectedClassName) : this.classLoader.loadClass(expectedClassName);
				if(!Database.class.isAssignableFrom(clazz)) {
					throw new java.lang.IllegalArgumentException("Class " + clazz.getName() + " does not implement Database.");
				}
				db = (Database) clazz.newInstance();
			} catch(java.lang.ClassNotFoundException cnf) {
				throw new java.sql.SQLException("Could not find class " + expectedClassName + ". Database \"" + dbName + "\" not supported by default factory. Either a different factory needs to be used or the missing class implemented, preferably by extending AbstractDatabase.");
			} catch(java.lang.Exception err) {
				throw new java.sql.SQLException("Error instantiating " + expectedClassName + ": " + err.getMessage());
			}
			db.init(dataSource);
			if(db instanceof AbstractDatabase) {
				AbstractDatabase adb = (AbstractDatabase) db;
				adb.setMaxConnections (this.maxConnections);
				adb.setConnectionTimeout (this.timeout);
			}
			return db;
		} finally {
			c.close();
		}
	}
}
