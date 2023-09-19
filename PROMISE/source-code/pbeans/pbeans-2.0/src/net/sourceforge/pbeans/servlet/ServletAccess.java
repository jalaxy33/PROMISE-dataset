package net.sourceforge.pbeans.servlet;

import javax.servlet.*;
import javax.sql.*;
import javax.naming.*;
import java.util.*;
import net.sourceforge.pbeans.*;
import net.sourceforge.pbeans.data.*;

/**
 * Utilities that facilitate usage of pBeans in servlet engines.
 */
public class ServletAccess {
	private static final String PARAM_STORES = "pbeans.stores";
	private static final String SUFFIX_JDBC_URL = "jdbc.url";
	private static final String SUFFIX_JDBC_DRIVER = "jdbc.driver";
	private static final String SUFFIX_JNDI_PATH = "datasource.def.name";
	private static final String SUFFIX_MAX_CONNECTIONS = "max.connections";
	private static final String SUFFIX_CONNECTION_TIMEOUT = "connection.timeout";

	/**
	 * Calls {@link #getStore(ServletConfig, String, ClassLoader)} by passing
	 * the servlet configuration of the servlet. The store will use the
	 * class loader that loaded the servlet class by default.
	 * @param servlet A servlet.
	 * @param storeName The name of the store sought.
	 * @return A <code>Store</code> instance.
	 * @throws StoreException
	 */
	public static Store getStore(Servlet servlet, String storeName) throws StoreException {
		return getStore(servlet.getServletConfig(), storeName, servlet.getClass().getClassLoader());
	}
	
	/**
	 * Calls {@link #getStore(ServletContext, String, ClassLoader)} by passing
	 * a servlet context.
	 * @param servletConfig A servlet configuration instance.
	 * @param storeName The name of the store sought.
	 * @param classLoader The class loader Store will use by default to load classes.
	 *                    This may be <code>null</code>.
	 * @return A <code>Store</code> instance.
	 * @throws ServletException
	 */
	public static Store getStore(ServletConfig servletConfig, String storeName, ClassLoader classLoader) throws StoreException {
		if(servletConfig == null) {
			throw new StoreException("ServletConfig parameter is null. This sometimes happens when the init() method of a servlet is overridden without calling its super implementation.");
		}
		return getStore(servletConfig.getServletContext(), storeName, classLoader);
	}

	private static final Map storesByName = new HashMap(1);

	/**
	 * Gets a store based on servlet context parameters. 
	 * First, a parameter named <code>pbeans.stores</code> will
	 * be looked up. It should contain a comma-separated list
	 * of store names. For each store name <i>mystorename</i>,
	 * there should be either a context parameter named 
	 * <code><i>mystorename</i>.jdbc.url</code> (defining the
	 * JDBC URL for {@link net.sourceforge.pbeans.data.GenericDataSource})
	 * or one named <code><i>mystorename</i>.datasource.def.name</code>
	 * (definining a JNDI name identifying a <code>DataSource</code>
	 * instance). If <code><i>mystorename</i>.jdbc.url</code> is defined,
	 * <code><i>mystorename</i>.jdbc.driver</code> should also 
	 * be defined. Optionally, context parameters <code><i>mystorename</i>.max.connections</code>
	 * and <code><i>mystorename</i>.connection.timeout</code> may also
	 * be provided.
	 * @param servletContext
	 * @param storeName
	 * @param classLoader
	 * @throws StoreException
	 */
	public static Store getStore(ServletContext servletContext, String storeName, ClassLoader classLoader) throws StoreException {
		synchronized(storesByName) {
			Store store = (Store) storesByName.get(storeName);
			if(store != null) {
				return store;
			}
		}
		String stores = servletContext.getInitParameter(PARAM_STORES);
		if(stores == null) {
			throw new StoreException("Context parameter " + PARAM_STORES + " not found in servlet context (web.xml).");
		}
		StringTokenizer tok = new StringTokenizer(stores, ",");
		while(tok.hasMoreTokens()) {
			String declaredName = tok.nextToken().trim();
			if(declaredName.equals(storeName)) {
				Store store = getStoreImpl(servletContext, storeName, classLoader);
				synchronized(storesByName) {
					storesByName.put(storeName, store);
				}
				return store;
			}
		}
		return null;
	}
	
	private static Store getStoreImpl(ServletContext servletContext, String storeName, ClassLoader classLoader) throws StoreException {
		String jdbcUrlParam = storeName + "." + SUFFIX_JDBC_URL;
		String jdbcUrl = servletContext.getInitParameter(jdbcUrlParam);
		DataSource dataSource;
		if(jdbcUrl == null) {
			String jndiNameParam = storeName + "." + SUFFIX_JNDI_PATH;
			String jndiName = servletContext.getInitParameter(jndiNameParam);
			if(jndiName == null) {
				throw new StoreException("Neither servlet context parameter " + jdbcUrlParam + " nor " + jndiNameParam + " are defined (in web.xml).");
			}
			try {
				Context initialContext = new InitialContext();
				dataSource = (DataSource) initialContext.lookup("java:comp/env/" + jndiName);
			} catch(NamingException ne) {
				throw new StoreException(ne);
			}
		}
		else {
			String dcnParam = storeName + "." + SUFFIX_JDBC_DRIVER;
			String dcn = servletContext.getInitParameter(dcnParam);
			if(dcn == null) {
				throw new StoreException("Context parameter " + dcnParam + " not provided.");
			}
			GenericDataSource gds = new net.sourceforge.pbeans.data.GenericDataSource();
			gds.setUrl(jdbcUrl);
			gds.setDriverClassName(dcn);
			dataSource = gds;			
		}
		int maxConnections;
		int connectionTimeout;
		String maxConnectionsParam = storeName + "." + SUFFIX_MAX_CONNECTIONS;
		String maxConnectionsText = servletContext.getInitParameter(maxConnectionsParam);
		try {
			maxConnections = maxConnectionsText == null ? 99 : Integer.parseInt(maxConnectionsText);
		} catch(NumberFormatException nfe) {
			throw new StoreException("Value of " + maxConnectionsParam + " is not a number.");
		}			
		String connectionTimeoutParam = storeName + "." + SUFFIX_CONNECTION_TIMEOUT;
		String connectionTimeoutText = servletContext.getInitParameter(connectionTimeoutParam);
		try {
			connectionTimeout = connectionTimeoutText == null ? 3600000 : Integer.parseInt(connectionTimeoutText);
		} catch(NumberFormatException nfe) {
			throw new StoreException("Value of " + connectionTimeoutParam + " is not a number.");			
		}
		return new Store(dataSource, maxConnections, connectionTimeout, classLoader);
	}
}
