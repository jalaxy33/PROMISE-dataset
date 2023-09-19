package net.sourceforge.pbeans.servlet;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import net.sourceforge.pbeans.*;
import net.sourceforge.pbeans.annotations.*;

/**
 * @deprecated not used
 * @hidden
 */
class RequestHelper {
    private static final String UPDATE_RESULT_ATTRIBUTE = "pbeans.UPDATE_RESULT";
    private final ClassLoader classLoader;
    private final String storeAttribute;
    
    /**
     * @param storeName A store-name defined as an init parameter
     *                  in InitServlet's configuration for the webapp
     *                  where the request occurs.
     */
    public RequestHelper(String storeAttribute) throws ServletException {
        this(storeAttribute, null);
    }

    public RequestHelper(String storeAttribute, ClassLoader classLoader) throws ServletException {
        this.classLoader = classLoader;
        this.storeAttribute = storeAttribute;
    }

    public UpdateResult insert(ServletConfig config, HttpServletRequest request, String className) throws ServletException {
        try {
            ClassLoader classLoader = this.classLoader;
            if(classLoader == null) {
                classLoader = this.getClass().getClassLoader();
            }
            Class clazz = classLoader == null ? Class.forName(className) : classLoader.loadClass(className);
            return this.insert(config, request, clazz);
        } catch(ClassNotFoundException cnf) {
            throw new ServletException("Did not find " + className, cnf);
        }
    }

    public UpdateResult insert(ServletConfig config, HttpServletRequest request, Class clazz) throws ServletException {
        Store store = InitServlet.getStore(config, this.storeAttribute);
        Collection exceptions = new LinkedList();
        try {
            try {
                Object bean = ServletMapper.createBean(clazz,
                                                       request);
                if (!(bean instanceof Persistent) && !clazz.isAnnotationPresent(PersistentClass.class)) {
                    throw new ServletException("Class " + clazz.getName() + " not assignable to Persistent interface.");
                }
                store.insert(bean);
            } catch (DuplicateEntryException dee) {
                exceptions.add(dee);
            }           
            return new UpdateResult(exceptions);
        } catch(Exception err) {
            throw new ServletException(err);
        }
    }

    public UpdateResult update(ServletConfig config, HttpServletRequest request, String className, String primaryKeyProperty) throws ServletException {
        try {
            ClassLoader classLoader = this.classLoader;
            if(classLoader == null) {
                classLoader = this.getClass().getClassLoader();
            }
            Class clazz = classLoader == null ? Class.forName(className) : classLoader.loadClass(className);
            return this.update(config, request, clazz, primaryKeyProperty);
        } catch(ClassNotFoundException cnf) {
            throw new ServletException("Did not find " + className, cnf);
        }
    }

    public UpdateResult update(ServletConfig config, HttpServletRequest request, Class clazz, String primaryKeyProperty) throws ServletException {
        Store store = InitServlet.getStore(config, this.storeAttribute);
        Collection exceptions = new LinkedList();
        if (primaryKeyProperty == null) {
            throw new ServletException("primaryKeyProperty was null");
        }
        String value = request.getParameter(primaryKeyProperty);
        if(value == null) {
            throw new ServletException("Expected form parameter named " + primaryKeyProperty);
        }
        try {
            Object pObject = store.selectSingle(clazz, primaryKeyProperty, value);
            if (pObject == null) {
                exceptions.add(new MissingEntryException(primaryKeyProperty, value));
            }
            else {
                try {
                    ServletMapper.populateBean(pObject, request);
                    store.save(pObject);
                } catch (DuplicateEntryException dee) {
                    exceptions.add(dee);
                }
            }
            return new UpdateResult(exceptions);
        } catch(Exception err) {
            throw new ServletException(err);
        }
    }

    public static UpdateResult getLastUpdateRequest(ServletRequest request) throws ServletException {
        UpdateResult updateResult = (UpdateResult) request.getAttribute(UPDATE_RESULT_ATTRIBUTE);
        if (updateResult == null) {
            throw new ServletException("Unable to find error codes. Apparently UpdateFilter has not been run in the current request context.");
        }
        return updateResult;
    }

    //  public static Iterator getExceptions(ServletRequest request) throws ServletException {
//          Collection exceptions = (Collection) request.getAttribute(UPDATE_RESULT_ATTRIBUTE);
//          if (exceptions == null) {
//              throw new ServletException("Unable to find error codes. Apparently UpdateFilter has not been run in the current request context.");
//          }
//          return exceptions.iterator();
//      }

//      public static boolean hasExceptions(ServletRequest request) throws ServletException {
//          Collection exceptions = (Collection) request.getAttribute(UPDATE_RESULT_ATTRIBUTE);
//          if (exceptions == null) {
//              throw new ServletException("Unable to find error codes. Apparently UpdateFilter has not been run in the current request context.");
//          }
//          return exceptions.size() > 0;        
//      }


}
