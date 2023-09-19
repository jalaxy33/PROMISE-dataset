package net.sourceforge.pbeans.data;

import java.sql.*;
import java.util.*;
import java.io.*;
import javax.sql.*;

/**
 * An implementation of DataSource that can be
 * used with any JDBC driver. The caller is required
 * to set properties driverClassName and url.
 */
public class GenericDataSource implements DataSource {
    private String url;
    private Driver driver;
    private PrintWriter logWriter;
    private Properties properties;

    public GenericDataSource() {
    }

    public GenericDataSource (String driverClassName, String url) throws IllegalAccessException {
    	this.url = url;
    	setDriverClassName (driverClassName);
    }

    /**
     * Sets mandatory property driverClassName, the
     * name of the JDBC Driver class (e.g.
     */
    public void setDriverClassName (String dcn) {
    	try {
    		Class c = Class.forName (dcn);
    		this.driver = (Driver) c.newInstance();
    	} catch (ClassNotFoundException cnf) {
    		throw new IllegalArgumentException ("Class not found: " + dcn);
    	} catch (ClassCastException cc) {
    		throw new IllegalArgumentException ("Class " + dcn + " is not a java.sql.Driver.");
    	} catch (InstantiationException ie) {
    		throw new IllegalArgumentException ("Class " + dcn + " does not have a default public constructor.");
    	} catch (IllegalAccessException iae) {
    		throw new IllegalStateException(iae);
    	}
    }

    /**
     * Sets mandatory property url, the JDBC connection
     * URL.
     */
    public void setUrl (String url) {
	this.url = url;
    }

    /**
     * Sets properties passed to <code>Driver.connect</code> method.
     * Properties typically needed are <code>user</code> and 
     * <code>password</code>,
     * if they are not passed as part of the URL.
     */
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    protected Connection getConnection(Properties properties) throws SQLException
    {
        if (this.driver == null || this.url == null) {
            throw new IllegalStateException ("You must set properties driverClassName and url.");
        }
        
        Connection connection = driver.connect(this.url, properties);
        
        if (connection == null)
        {
            throw new IllegalStateException("Incompatible driver used (or incorrect database URL).");
        }
        return connection;
    }
    

    public Connection getConnection() throws SQLException {
        return getConnection(this.properties);
    }

    public Connection getConnection(String username, String password) throws SQLException {
	Properties properties = new Properties();
	properties.put ("user", username);
	properties.put ("password", password);
	return getConnection(properties);
    }

    public int getLoginTimeout() {
	return 0;
    }

    public PrintWriter getLogWriter() {
	return this.logWriter;
    }

    public void setLoginTimeout(int seconds) {
	throw new UnsupportedOperationException();
    }

    public void setLogWriter(PrintWriter out){
	this.logWriter = out;
    }
}
