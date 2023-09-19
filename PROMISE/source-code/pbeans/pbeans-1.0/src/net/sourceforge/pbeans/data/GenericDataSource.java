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
    public void setDriverClassName (String dcn) throws IllegalAccessException {
	try {
	    Class c = Class.forName (dcn);
	    this.driver = (Driver) c.newInstance();
	} catch (ClassNotFoundException cnf) {
	    throw new IllegalArgumentException ("Class not found: " + dcn);
	} catch (ClassCastException cc) {
	    throw new IllegalArgumentException ("Class " + dcn + " is not a java.sql.Driver.");
	} catch (InstantiationException ie) {
	    throw new IllegalArgumentException ("Class " + dcn + " does not have a default public constructor.");
	}
    }

    /**
     * Sets mandatory property url, the JDBC connection
     * URL.
     */
    public void setUrl (String url) {
	this.url = url;
    }

    public Connection getConnection() throws SQLException {
	if (this.driver == null || this.url == null) {
	    throw new IllegalStateException ("You must set properties driverClassName and url.");
	}
	return driver.connect (this.url, new Properties());
    }

    public Connection getConnection(String username, String password) throws SQLException {
	if (this.driver == null || this.url == null) {
	    throw new IllegalStateException ("You must set properties driverClassName and url.");
	}
	Properties properties = new Properties();
	properties.put ("user", username);
	properties.put ("password", password);
	return driver.connect (this.url, properties);
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
