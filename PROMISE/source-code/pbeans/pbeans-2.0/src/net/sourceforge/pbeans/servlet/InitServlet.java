package net.sourceforge.pbeans.servlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import net.sourceforge.pbeans.*;
import net.sourceforge.pbeans.data.*;

/**
 * Servlet used to initialize a global pBeans store.
 * It should be configured with load-on-startup,
 * and init parameters jdbc-url, jdbc-driver-class and store-attribute
 * should be provided. The value of the store-attribute
 * parameter is a servlet context attribute that maps to
 * the Store instance. 
 * @hidden 
 * @deprecated Unused (delete later)
 */
class InitServlet extends HttpServlet {
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		String jdbcUrl = config.getInitParameter("jdbc-url");
        if(jdbcUrl == null) {		
        	throw new ServletException("Init parameter jdbc-url missing.");
        }
        String jdbcDriverClass = config.getInitParameter("jdbc-driver-class");
        if(jdbcDriverClass == null) {
        	throw new ServletException("Init parameter jdbc-driver-class missing.");
        }
        String storeAttribute = config.getInitParameter("store-attribute");
        if(storeAttribute == null) {
        	throw new ServletException("Init parameter store-attribute missing.");
        }
        try {
        	javax.sql.DataSource dataSource = new GenericDataSource(jdbcDriverClass, jdbcUrl);
        	Store store = new Store(dataSource);
        	config.getServletContext().setAttribute(storeAttribute, store);
        } catch(Exception err) {
        	throw new ServletException(err);
        }
	}
	
	/**
	 * Gets a Store from the servlet context.
	 * @param config A servlet configuration instance.
	 * @param storeAttribute The store attribute name, which should have been previously used to configure an InitServlet.
	 * @return A Store instance or <code>null</code> if not found.
	 * @throws ServletException
	 */
	public static Store getStore(ServletConfig config, String storeAttribute) throws ServletException {
		if(config == null) {
			throw new ServletException("ServletConfig parameter is null. This sometimes happens when the init() method of a servlet is overridden without calling its super implementation.");
		}
		return (Store) config.getServletContext().getAttribute(storeAttribute);
	}
}

	