package net.sourceforge.pbeans;

import java.sql.*;
import java.util.*;
import java.lang.ref.*;
import java.lang.reflect.*;
import java.beans.*;

import javax.sql.*;


import net.sourceforge.pbeans.data.*;
import net.sourceforge.pbeans.util.*;
import net.sourceforge.pbeans.annotations.*;
import java.util.logging.*;

/**
 * A <code><b>Store</b></code> instance provides 
 * mapping of Java objects (beans) to relational
 * database tables and vice-versa. It attempts to
 * do this in a manner that is comparable to
 * an orthogonal and transitive persistence layer.
 * <p>
 * <code>Store</code> should be instantiated only once
 * per database by a Java application.
 * <p>
 * <code>Store</code> deals with JavaBeans that are 
 * tagged with {@link net.sourceforge.pbeans.annotations.PersistentClass}. A store will
 * first know about a persistent object if the object
 * is <code>register</code>ed for the first time, <code>insert</code>ed,
 * or obtained from Store via one of its
 * <code>select</code> methods. 
 *
 * <h3>Insertion</h3>
 * A new object, previously unkown by Store, may be
 * inserted by invoking the <code>insert</code> method.
 * If the same object or another
 * object with a matching unique index was already
 * inserted, the method will throw <code>DuplicateEntryException</code>.
 * This is an example of insertion:
 * <code>
 * <pre>
 *
 *      User user = new User();
 *      user.setUserName ("joe");
 *      try {
 *          store.insert (user);
 *      } catch (DuplicateEntryException dee) {
 *          // Error: User already exists.
 *      }
 *
 * </pre>
 * </code>
 * Unique indexes may be defined by specifying the
 * <code>indexes</code> value of the {@link net.sourceforge.pbeans.annotations.PersistentClass} annotation.
 *
 * <h3>Updates</h3>
 * Changes to a persistent object already inserted should
 * be done by calling the <code>save</code> method,
 * for example, as follows:
 * <code>
 * <pre>
 *
 *      try {
 *         store.save (user);
 *      } catch (DuplicateEntryException dee) {
 *         // Error: Can't change because of unique-index conflict.
 *      }
 * 
 * </pre>
 * </code>
 * The <code>save</code> method may throw <code>DuplicateEntryException</code>
 * if the change conflicts with another persistent object's unique index.
 * <h3>Queries</h3>
 * There are several <code>select</code> and <code>selectSingle</code>
 * methods which may be used to query previously inserted persistent objects
 * by class. For example, to obtain an object by providing a property
 * value, use code similar to the following:
 * <code>
 * <pre>
 * 
 *      User user = (User) store.selectSingle(User.class, "userName", "joe");
 *    
 * </pre>
 * </code>
 * 
 * Other <code>select</code> methods return a {@link net.sourceforge.pbeans.data.ResultsIterator}  
 * instance, which works similar to a regular <code>Iterator</code>. A
 * <code>ResultsIterator<code> instance should be closed, however, when the
 * caller is done with it.
 * 
 * <code>
 * <pre> 
 *      ResultsIterator ri = store.select(User.class, "SELECT * FROM User", new Object[0]);
 *      try {
 *          while(ri.hasNext()) {
 *              User user = (User) ri.next();
 *              System.out.println("User: " + user.getUserName());
 *          }
 *      } finally {
 *          ri.close();
 *      }
 * </pre>
 * </code>
 */
public class Store {
	private static final Logger logger = Logger.getLogger(Store.class.getName());
	//private static final PersistentID ROOT_ID = new PersistentID(0);
	//private static final String objectIdFieldName = "JP__OBJECTID";
	//private static final String INDEX_OBJECT_ID = "JP__IDXOBJID";
	private static final Object[] EMPTY_PARAMETERS = new Object[0];

	private final Object objectsMonitor = new Object();
	private final Map<GlobalPersistentID,ObjectInfo> objectInfosByID = new HashMap<GlobalPersistentID,ObjectInfo>();
	private final Map<Object,ObjectInfo> objectInfosByObject = new java.util.WeakHashMap<Object,ObjectInfo>();
	private final Map<Class,StoreInfo> storeInfoByClass = new HashMap<Class,StoreInfo>();
	//private final Map tableNames = new HashMap();
	//private final Map fieldNames = new HashMap();
	private final ReferenceQueue REF_QUEUE = new ReferenceQueue();
	private final Database database;
	private final int maxTableNameLength;
	private final int maxColumnNameLength;
	private final DatabaseFactory databaseFactory;
	private final ClassLoader defaultClassLoader;

	/**
	 * Constructs a Store instance.
	 * @param dataSource A DataSource instance, which may be obtained from
	 *                   a JDBC 2.0 provider, or by instantiating 
	 *                   net.sourceforge.pbeans.data.GenericDataSource.
	 * @param dbf An implementation of DatabaseFactory. This may be an instance
	 *            of DefaultDatabaseFactory.
	 * @param defaultClassLoader The class loader the Store instance will use
	 *                           to load classes by default.
	 * @see net.sourceforge.pbeans.data.GenericDataSource
	 * @throws StoreException
	 */
	public Store (DataSource dataSource, DatabaseFactory dbf, ClassLoader defaultClassLoader) throws StoreException {
		try {
			this.databaseFactory = dbf;
			this.database = dbf.getDatabase (dataSource);
			this.maxTableNameLength = this.database.getMaxTableNameLength();
			this.maxColumnNameLength = this.database.getMaxColumnNameLength();
			this.defaultClassLoader = defaultClassLoader;
		} catch (SQLException se) {
			throw new StoreException (se);
		}
	}

	/**
	 * Constructs a Store instance. Internally it creates an instance of
	 * <code>DefaultDatabaseFactory</code>.
	 * @param dataSource A DataSource instance, which may be obtained from
	 *                   a JDBC 2.0 provider, or by instantiating 
	 *                   net.sourceforge.pbeans.data.GenericDataSource.
	 * @see net.sourceforge.pbeans.data.GenericDataSource
	 * @throws StoreException
	 */
	public Store (DataSource dataSource) throws StoreException {
		this (dataSource, new DefaultDatabaseFactory(null), null);
	}

	/**
	 * Constructs a Store instance. Internally it creates an instance of
	 * <code>DefaultDatabaseFactory</code>.
	 * @param dataSource A DataSource instance, which may be obtained from
	 *                   a JDBC 2.0 provider, or by instantiating 
	 *                   net.sourceforge.pbeans.data.GenericDataSource.
	 * @param maxConnections The maximum number of connections to be kept
	 *                       in the pBeans connection pool.
	 * @param connectionTimeout The maximum length of time (in milliseconds)
	 *                          an inactive JDBC connection is kept in the
	 *                          pBeans connection pool. The default is 
	 *                          about one hour.
	 * @see net.sourceforge.pbeans.data.GenericDataSource
	 * @throws StoreException
	 */
	public Store (DataSource dataSource, int maxConnections, int connectionTimeout) throws StoreException {
		this (dataSource, new DefaultDatabaseFactory(null), null);
		DefaultDatabaseFactory ddb = (DefaultDatabaseFactory) this.databaseFactory;
		ddb.setMaxConnections(maxConnections);
		ddb.setConnectionTimeout(connectionTimeout);
	}

	public Store (DataSource dataSource, int maxConnections, int connectionTimeout, ClassLoader classLoader) throws StoreException {
		this (dataSource, new DefaultDatabaseFactory(classLoader), classLoader);
		DefaultDatabaseFactory ddb = (DefaultDatabaseFactory) this.databaseFactory;
		ddb.setMaxConnections(maxConnections);
		ddb.setConnectionTimeout(connectionTimeout);
	}

	/**
	 * Gets the maximum column name length supported by the
	 * underlying database.
	 */
	public int getMaxColumnNameLength() {
		return this.maxColumnNameLength;
	}
	
	public int getMaxTableNameLength() {
		return this.maxTableNameLength;
	}

//	/** 
//	 * Gets the preferred database field name for a requested field name.
//	 * If the requested field name's length is less than or equal to the maximum
//	 * field name length supported by the underlying store, no changes
//	 * are made to the requested field name.
//	 */
//	protected String getPreferredFieldName(String requestedFieldName) {
//		synchronized (this.fieldNames) {
//			String fieldName = (String) this.fieldNames.get(requestedFieldName);
//			if (fieldName != null) {
//				return fieldName;
//			}
//			fieldName = getShortColumnName (requestedFieldName, "F");
//			this.fieldNames.put (requestedFieldName, fieldName);
//			return fieldName;
//		}
//	}
	
	/**
	 * Gets the field name into which a JavaBean property name is mapped.
	 */
	protected String getFieldName (StoreInfo sinfo, String propertyName) throws StoreException {
		PropertyDescriptor pd = sinfo.getPropertyDescriptor(propertyName);
		if (pd == null) {
			throw new StoreException ("No property named " + propertyName);
		}
		FieldDescriptor fd = sinfo.getFieldDescriptor (this, pd);
		if (fd == null) {
			throw new StoreException ("Property named " + propertyName + " is not persistent.");
		}
		return fd.getName();
	}

	/**
	 * Gets a shorter column name if necessary.
	 * @param name The desired column name.
	 * @param prefix The column name prefix used when the field name is shortened.
	 */
    String getShortColumnName (String name, String prefix) {
		int maxColumnNameLength = this.getMaxColumnNameLength();
		String tName = name;
		int totalLength = tName.length();
		if (totalLength <= maxColumnNameLength) {
			return tName;
		}
		// Note: Don't change what the fixedHash is obtained from.
		int hash = Math.abs (Hash.fixedHash (name));
		String hashStr = String.valueOf(hash);
		int canUseLength = maxColumnNameLength - prefix.length() - hashStr.length();
		String fieldName =  
			prefix + 
			hashStr + 
			tName.substring(
					tName.length() - canUseLength, 
					tName.length());
		return fieldName;
	}

	/**
	 * Gets a StoreInfo instance corresponding to a persistent class. Normally,
	 * an instance of net.sourceforge.pbeans.DefaultStoreInfo is used, unless
	 * another class is provided with the same name as that of <code>beanClass</code> but
	 * suffixed with "_StoreInfo" (similar to
	 * BeanInfo for JavaBeans.) Implementing StoreInfo gives more
	 * control over which properties are persistent, indexes, etc.
	 * @throws StoreException
	 */
	public StoreInfo getStoreInfo (Class beanClass) throws StoreException {
		synchronized (storeInfoByClass) {
			StoreInfo sinfo = storeInfoByClass.get (beanClass);
			if (sinfo != null) {
				return sinfo;
			}
			sinfo = createStoreInfo (beanClass);
			storeInfoByClass.put (beanClass, sinfo);
			String objectIdFieldName = sinfo.getIdField();
			
			// Now make sure underlying table exists

			String tableName = getTableName (beanClass);
			try {
				BeanInfo binfo = Introspector.getBeanInfo(beanClass);
				PropertyDescriptor[] ppDescriptors = binfo.getPropertyDescriptors();
				Collection<FieldDescriptor> fieldDescriptors = new LinkedList<FieldDescriptor>();
				for (int i = 0; i < ppDescriptors.length; i++) {
					FieldDescriptor fd = sinfo.getFieldDescriptor(this,ppDescriptors[i]);
					if (fd != null) {
						fieldDescriptors.add (fd);
					}
				}
				Collection indexDescriptors = new LinkedList();
				Index[] indexes = sinfo.getIndexes (this);
				for (int i = 0; i < indexes.length; i++) {
					Index index = indexes[i];
					String indexName = getIndexName(index);
					try {
						indexDescriptors.add (new IndexDescriptor (indexName, index.isUnique(), getFieldNames (sinfo, index.getPropertyNames()), index.getKeyLength()));
					}
					catch (StoreException se) {
						throw new StoreException ("Unable to add index named " + indexName + " to table " + tableName + " because: " + se.getMessage(), se);
					}
				}
				FieldDescriptor oidDescriptor = new FieldDescriptor (objectIdFieldName, Types.BIGINT, false, null, true, false);
				try {
					database.ensureTableExists (tableName, oidDescriptor, fieldDescriptors, indexDescriptors, sinfo.isUserManaged(), sinfo.isAutoIncrementRequested(), sinfo.isDeleteFields());
				} catch (SQLException se) {
					if (database.isDuplicateEntryError (se)) {
						throw new DuplicateEntryException ("Unable to modify table " + tableName + " perhaps because the requested changes apparently generate a duplicate entry error (e.g adding an index on a column that has existing duplicate values.) You might want to change your StoreInfo settings or modify the database manually.");
					}
					else {
						throw new StoreException ("Unable to modify table " + tableName + " because: " + se.getMessage() + ". You might want to change your StoreInfo settings or modify the database manually.", se);
					}
				}
			} catch (IntrospectionException ie) {
				throw new StoreException (ie);
			}
			return sinfo;
		}
	}

	private String getIndexName (Index index) {
		StringBuffer buf = new StringBuffer();
		buf.append ("I");
		Collection propNames = index.getPropertyNames();
		Iterator i = propNames.iterator();
		while (i.hasNext()) {
			buf.append ('_');
			buf.append (i.next());
		}
		return getShortColumnName(buf.toString(), "I");
	}

	private String[] getFieldNames (StoreInfo sinfo, Collection propertyNames) throws StoreException {
		int size = propertyNames.size();
		String[] fieldNames = new String[size];
		Iterator i = propertyNames.iterator();
		int idx = 0;
		while (i.hasNext()) {
			fieldNames[idx++] = getFieldName(sinfo, (String) i.next());
		}
		return fieldNames;
	}
	
	private Class lookupClass(String name) throws ClassNotFoundException {
		ClassLoader cl = this.defaultClassLoader;
		return cl == null ? Class.forName(name) : cl.loadClass(name);
	}

	private StoreInfo createStoreInfo (Class beanClass) throws StoreException {
		if(!Persistent.class.isAssignableFrom(beanClass)) {
			return new DefaultStoreInfo(beanClass, this);
		}
		String checkName = beanClass.getName() + "_StoreInfo";
		try {
			ClassLoader cl = beanClass.getClassLoader();
			Class sinfobeanClass = cl == null ? this.lookupClass(checkName) : cl.loadClass (checkName);
			return (StoreInfo) sinfobeanClass.newInstance();
		} catch (ClassNotFoundException cnfe) {
			return new DefaultStoreInfo(beanClass, this);
		} catch (InstantiationException ie) {
			throw new StoreException ("Unable to create instance of " + checkName + ". It should have a public default constructor.");
		} catch (IllegalAccessException iae) {
			throw new StoreException (iae);
		}
	}

	/** 
	 * Associates a persistent object with a name.
	 * @see net.sourceforge.pbeans.Store#get
	 */
	public void put (String name, Object obj) throws StoreException {
		PersistentMap<String,Object> map = getRootMap();
		map.putObject(name, obj);
	}

	/** 
	 * Gets a persistent object previously associated with a name
	 * using the put() method.
	 * @see net.sourceforge.pbeans.Store#put
	 */
	public Object get (String name) throws StoreException {
		checkWeakReferences();
		PersistentMap<String,Object> map = getRootMap();
		return map.getObject (name);	
	}

	/**
	 * Removes an association of a name with a persistent object.
	 * @throws StoreException
	 * @see net.sourceforge.pbeans.Store#put
	 * @see net.sourceforge.pbeans.Store#get
	 */
	public void remove (String name) throws StoreException {
		PersistentMap<String,Object> map = getRootMap();
		map.removeObject (name);
	}
	
	private static final String ROOT_MAP_NAME = "pbeans.root.map";

	private Object getRootMapMonitor() {
		return ("Monitor:" + ROOT_MAP_NAME).intern();
	}
	
	/**
	 * Gets the PersistentMap instance used by the get() and put() methods
	 * in this class.
	 * @throws StoreException
	 */
	protected PersistentMap<String,Object> getRootMap() throws StoreException {
		PersistentMap<String,Object> rootMap = (PersistentMap<String,Object>) this.selectSingle(PersistentMap.class, "name", ROOT_MAP_NAME);
		if (rootMap == null) {
			synchronized(this.getRootMapMonitor()) {
				rootMap = (PersistentMap<String,Object>) this.selectSingle(PersistentMap.class, "name", ROOT_MAP_NAME);
				if (rootMap == null) {
					rootMap = new PersistentMap<String,Object>();
					rootMap.setName(ROOT_MAP_NAME);
					// Ensure rootMap is a known object.
					this.register(rootMap);
				}
				rootMap.setStore(this);
			}
		}
		rootMap.setStore(this);
		return rootMap;
	}

	private void checkWeakReferences() {
		for (;;) {
			ObjectInfo wr = (ObjectInfo) this.REF_QUEUE.poll();
			if (wr == null) {
				break;
			}
			GlobalPersistentID objectID = wr.getGlobalPersistentID();
			//Object persistentObject = wr.getObject();
			synchronized (objectsMonitor) {
				this.objectInfosByID.remove (objectID);
				// No need to remove from objectInfosByObject
			}
		}
	}


	/**
	 * Gets a persistent object given its unique ID.
	 * The object may already be available in memory or
	 * it may be retrieved from secondary storage.
	 * @see Store#getObject(GlobalPersistentID, ClassLoader)
	 * @see Store#getObject(PersistentID, Class)
	 * @throws StoreException
	 */
	public Object getObject(GlobalPersistentID globalID) throws StoreException {
		Object obj = this.getCachedObject (globalID);
		if (obj == null) {
			// This does not require synchronization and performs I/O.
			obj = this.lookupStoredObject(globalID, null);
		}
		return obj;
	}

	/**
	 * Gets a persistent object given its unique ID.
	 * The object may already be available in memory or
	 * it may be retrieved from secondary storage.
	 * If the object is retrieved from secondary storage, 
	 * the class loader provided is used to find its class.
	 * @see Store#getObject(PersistentID,java.lang.Class)
	 * @throws StoreException
	 */
	public Object getObject (GlobalPersistentID globalID, ClassLoader classLoader) throws StoreException {
		Object obj = this.getCachedObject (globalID);
		if (obj == null) {
			// This does not require synchronization and performs I/O.
			obj = this.lookupStoredObject (globalID, classLoader);
		}
		return obj;
	}

	/**
	 * Obtains an object given a {@link PersistentID} and a class reference.
	 * @param objectID The ID of the desired object.
	 * @param beanClass The beanClass of the desired object.
	 * @return A Persistent object or <code>null</code> if an object of the given 
	 *         class and PersistentID does not exist.
	 */
	public Object getObject(PersistentID objectID, Class beanClass) throws StoreException {
		GlobalPersistentID globalID = new GlobalPersistentID(objectID, beanClass);
		Object obj = this.getCachedObject (globalID);
		if (obj == null) {
			// This does not require synchronization and performs I/O.
			obj = this.lookupStoredObject(objectID, beanClass);
		}
		return obj;
	}

//	private Object getObjectImpl(PersistentID objectID, Class beanClass) throws StoreException {
//		GlobalPersistentID gid = new GlobalPersistentID(objectID, beanClass);
//		Object obj = this.getCachedObjectImpl(gid);
//		if (obj == null) {
//			obj = this.lookupStoredObject(objectID, beanClass);
//		}
//		return obj;
//	}	

	/**
	 * Gets a persistent object from main memory, if available.
	 * This method will not load the object from secondary storage.
	 */
	public Object getCachedObject(GlobalPersistentID globalID) {
		synchronized (objectsMonitor) {
			return this.getCachedObjectImpl(globalID);
		}
	}

	private Object getCachedObjectImpl(GlobalPersistentID globalID) {
		ObjectInfo oi = (ObjectInfo) this.objectInfosByID.get(globalID);
		if (oi == null) {
			return null;
		}
		return oi.getObject();
	}

//	private ObjectInfo getCachedObjectInfoImpl(GlobalPersistentID gid) {
//		return (ObjectInfo) this.objectInfosByID.get (gid);
//	}

	/**
	 * Loads a persistent object from storage.
	 */
	private Object lookupStoredObject(GlobalPersistentID globalID, ClassLoader classLoader) throws StoreException {
		PersistentID objectID = globalID.getObjectID();
		String className = globalID.getClassName();
		try {
			Class beanClass = classLoader == null ? this.lookupClass(className) : classLoader.loadClass(className);
			return lookupStoredObject(objectID, beanClass);
		} catch (ClassNotFoundException cnf) {
			throw new StoreException (cnf);
		}
	}

	/**
	 * Gets the table name used for a given class.
	 * @throws StoreException
	 */
	protected String getTableName(Class beanClass) throws StoreException {
		StoreInfo sinfo = this.getStoreInfo(beanClass);
		return sinfo.getTableName(this.maxTableNameLength);
	}

	/**
	 * Retrieves an object from the database. If the object happens to
	 * be registered with the store before it's retrieved, the instance
	 * previously in memory is populated. This method does not require
	 * synchronization.
	 * @param objectID
	 * @param beanClass
	 * @throws StoreException
	 */
	private Object lookupStoredObject(PersistentID objectID, Class beanClass) throws StoreException {
		StoreInfo sinfo = getStoreInfo(beanClass);
		BeanFactory factory = new SimpleBeanFactory(beanClass);
		String tableName = sinfo.getTableName(this.maxTableNameLength);
		String sqlString = "SELECT * FROM " + tableName + " WHERE " + sinfo.getIdField() + "=" + objectID;
		ResultsIterator results = this.select(sinfo.getBeanClass(), sqlString, new Object[0], factory);
		try {
			if(results.hasNext()) {
				return results.next();
			}				
			else {
				return null;
			}
		} finally {
			results.close();
		}
	}

	/**
	 * Repopulates Persistent object properties with stored data.
	 * It should only be used if changes have been made to the
	 * database outside of this API.
	 * @throws StoreException
	 */
	public void reload(Object persObject) throws StoreException {
		PersistentID objectID = this.getPersistentID(persObject);
		Class beanClass = persObject.getClass();
		this.lookupStoredObject(objectID, beanClass);
	}

//	private Object buildOrPopulateBean (PersistentID objectID, Class persClass, Map record) throws StoreException {
//		Object persObject;
//		synchronized (objectsMonitor) {
//			ObjectInfo oi = getCachedObjectInfoImpl(objectID);
//			persObject = oi == null ? null : oi.getObject();
//			if (persObject == null) {
//				if(oi != null) {
//					this.objectInfosByObject.remove(persObject);
//					this.objectInfosByID.remove(objectID);
//				}
//				persObject = buildBean (persClass, record);
//				ensureKnownImpl (persObject, objectID, false);
//			}
//			else {
//				// TBD: Is this necessary?
//						// TBD: Does not reload external changes.
//				// populateBean (persClass, record, persObject, oi.getUnchangedProperties());
//			}
//		}
//		return persObject;
//	}

//	private Object buildBean (Class c, Map dbRecord) throws StoreException {
//		StoreInfo sinfo = getStoreInfo (c);
//		Object persObject = sinfo.create (this);
//		populateBean (c, dbRecord, persObject, null);
//		return persObject;
//	}

//	/**
//	 * Populates a Persistent object with
//	 * data obtained from a database query.
//	 * @param persClass A class assignable to Persistent.
//	 * @param dbRecord A map of values obtained from JDBC.
//	 * @param bean An instance of <code>persClass</code>
//	 * @throws StoreException
//	 */
//	protected void populateBean(Class persClass, Map dbRecord, Object bean, Set dbFields) throws StoreException {
//		try {
//			StoreInfo sinfo = getStoreInfo (persClass);
//			BeanInfo binfo = Introspector.getBeanInfo (persClass);
//			PropertyDescriptor[] pds = binfo.getPropertyDescriptors();
//			for (int i = 0; i < pds.length; i++) {
//				PropertyDescriptor pd = pds[i];	
//				String propName = pd.getName();
//				FieldDescriptor fd = sinfo.getFieldDescriptor (this, pd);
//				if (fd != null) {
//					String fieldName = fd.getName();
//					if (dbFields == null || dbFields.contains (fieldName)) {
//						if (!dbRecord.containsKey (fieldName)) {
//							throw new StoreException ("Underlying database record does not contain field " + fieldName);
//						}
//						Object dbValue = dbRecord.get (fieldName);
//						Object propValue = unmarshallValue (sinfo, pd, dbValue);
//						Method setter = pd.getWriteMethod();
//						try {
//							setter.invoke (bean, new Object[] { propValue });
//						} catch (RuntimeException iae) {
//							if (propValue == null && pd.getPropertyType().isPrimitive()) {
//								warn ("populateBean(): Not setting property " + propName + " with null value because its type is primitive.");
//							}
//							else {
//								throw new IllegalArgumentException ("Unable to set property " + propName + " with object of type " + (propValue == null ? "<null>" : propValue.getClass().getName()) + " obtained from StoreInfo.unmarshallValue(). Reason: " + iae.getMessage());
//							}
//						}
//					}
//				}
//			}
//		} catch (IntrospectionException ie) {
//			throw new StoreException (ie);
//		} catch (IllegalAccessException iae) {
//			throw new StoreException (iae);
//		} catch (InvocationTargetException ite) {
//			throw new StoreException (ite);
//		}
//	}

//	private boolean isPersistent (StoreInfo sinfo, PropertyDescriptor pd) throws StoreException {
//		return sinfo.getFieldDescriptor(this,pd) != null;
//	}

	/**
	 * Updates the database record previously created
	 * for a Persistent object. The record is actually updated 
	 * only if property changes have occurred
	 * since the last save or insertion.
	 * This method will not work on Persistent objects not
	 * previously inserted, registered or obtained from this
	 * Store, or if the record had previously been deleted.
	 * @throws StoreException
	 */
	public void save(Object persObject) throws StoreException {
		this.save(persObject, false, false);
	}

	/**
	 * Saves an object in the database. 
	 * @param persObject A bean.
	 * @param force Whether saving should be forced even if the object has not
	 *              changed since the last time it was saved.
	 * @param reload If true, the record is reloaded from secondary storage
	 *               if the save operation fails for any reason.
	 * @throws StoreException
	 */
	public void save(Object persObject, boolean force, boolean reload) throws StoreException {
		checkWeakReferences();
		ObjectInfo oi = getObjectInfo(persObject);
		try {
			oi.save(force);
		} catch(StoreException err) {
			if(reload) {
				if(logger.isLoggable(Level.INFO)) {
					logger.info("save(): Operation failed; reloading record...");
				}
				this.reload(persObject);
			}
			throw err;
		}
	}

	/**
	 * Attempts to insert a new persistent object in its underlying database table.
	 * This method should only be invoked on new objects unknown
	 * to this Store or objects that have been previously deleted.
	 * Persistent objects referred to by <code>persObject</code> 
	 * should normally be obtained from Store prior to insertion, 
	 * unless they are objects
	 * also known to be insertable.
	 * The object is also cached in main memory (using a weak reference
	 * cache) until it is garbage collected, so subsequent invocations
	 * to <code>register</code> passing <code>persObject</code> will have
	 * no effect.
	 * @see net.sourceforge.pbeans.StoreInfo
	 * @see net.sourceforge.pbeans.Index
	 * @throws DuplicateEntryException Thrown if the object was already inserted or
	 *                                 another object with
	 *                                 equal unique-index values already exists
	 *                                 in persistent storage. This applies to
	 *                                 the object being inserted and to any other objects
	 *                                 referred to by <code>persObject</code>, directly
	 *                                 and indirectly, which are not already known
	 *                                 to Store.
	 * @throws StoreException
	 */
	public void insert(Object persObject) throws StoreException {
		checkWeakReferences();
		ObjectInfo oi;
		synchronized (this.objectsMonitor) {
			oi = this.objectInfosByObject.get(persObject);
			if (oi != null) {
				throw new StoreException("Object already known by Store. A new bean must be created before invoking insert.");
			}
		}
		this.insertBeanTransac(persObject);
	}

	/** 
	 * Informs this Store about a new persistent object.
	 * If the object is already known by this Store, nothing is done.
	 * If the object is determined to be new, it is cached in main
	 * memory (using a weak reference cache) and inserted in
	 * data storage.
	 * @throws DuplicateEntryException Thrown if the object was not previously
	 *                                 known by this Store instance and a record 
	 *                                 already exists with
	 *                                 equal unique-index values.
	 * @throws StoreException
	 */
	public void register(Object persObject) throws StoreException {
		checkWeakReferences();
		getObjectInfo(persObject);
	}

	/**
	 * Removes a persistent object from storage.
	 * Any subsequent attempts to save the object will fail,
	 * unless <code>insert</code> is invoked first.
	 * @throws StoreException
	 */
	public void delete(Object persObject) throws StoreException {
		this.delete(persObject, false);
	}

	/**
	 * Deletes an object's database representation.
	 * @param persObject
	 * @param forget If this parameter is true, the object will also be removed
	 *               for Store's memory data structures. This means
	 *               that the object can be saved again. If <code>forget</code>
	 *               is false, <code>save</code> 
	 *               operations will have no effect on the deleted object.
	 * @throws StoreException
	 */
	public void delete(Object persObject, boolean forget) throws StoreException {
		ObjectInfo oi = getObjectInfo (persObject);
		oi.delete(forget);	
	}

	/**
	 * Gets the PersistentID associated with a Persistent object.
	 * If the object given was not previously obtained from Store
	 * or registered with Store, a new persistent record is created.
	 */
	public PersistentID getPersistentID (Object persObject) throws StoreException {
		ObjectInfo oi = getObjectInfo (persObject);
		return oi.getObjectID();
	}
	
	public GlobalPersistentID getGlobalPersistentID(Object persObject) throws StoreException {
		ObjectInfo oi = getObjectInfo (persObject);
		return new GlobalPersistentID(oi.getObjectID(), oi.getClassName());		
	}

	private ObjectInfo getObjectInfo (Object persObject) throws StoreException {
		return getObjectInfo (persObject, true);
	}

	/**
	 * Begins a database transaction with isolation level
	 * <code>TRANSACTION_READ_COMMITTED</code>.
	 * @throws StoreException
	 * @see #beginTransaction(int)
	 * @see #requestLock(Class, String, int)
	 */
	public void beginTransaction() throws StoreException {
		this.beginTransaction(Connection.TRANSACTION_READ_COMMITTED);
	}

	/**
	 * Begins a database transaction with a given transaction isolation
	 * level. A transaction <i>must<i> be ended in the same thread
	 * with a call to {@link #endTransaction}. A try-finally block
	 * is recommended to achieve this, as follows
	 * <p>
	 * <code><pre>
	 * store.beginTransaction(Connection.TRANSACTION_READ_COMMITTED);
	 * try {
	 *     // store operations here
	 * } finally {
	 *     store.endTransaction();
	 * }
	 * </pre></code>
	 * <p>
	 * Transactions only apply
	 * to the database. They do not prevent concurrent modification
	 * of Java objects, which can be shared by different threads 
	 * when this <code>Store</code> class is used.
	 * <p>
	 * Nested transactions are disallowed.
	 * @param transactionIsolationLevel A transaction isolation level.
	 *        See constants in {@link java.sql.Connection}.
	 * @throws StoreException
	 * @see #requestLock
	 */
	public void beginTransaction(int transactionIsolationLevel) throws StoreException {
		try {
			this.database.beginTransaction(transactionIsolationLevel);
		}
		catch(SQLException err) {
			throw new StoreException(err);
		}
	}

	/**
	 * Ends a transaction previously begun by a call to {@link #beginTransaction}.
	 */
	public void endTransaction() throws StoreException {
		try {
			this.database.endTransaction();
		}
		catch(SQLException err) {
			throw new StoreException(err);
		}		
	}

	/**
	 * Determines whether the current thread execution point
	 * is inside a transaction started by {@link #beginTransaction}.
	 */
	public boolean inTransaction() {
		return this.database.inTransaction();
	}

	/**
	 * Requests a lock with a transaction isolation level
	 * of <code>TRANSACTION_READ_COMMITTED</code>.
	 * @see #requestLock(Class, String, int)
	 */
	public void requestLock(Class beanClass, String dataName) throws StoreException, InterruptedException {
		this.requestLock(beanClass, dataName, Connection.TRANSACTION_READ_COMMITTED);
	}
	
	/**
	 * Requests entrance to a critical section of code for both
	 * Java threads and database alterations. In other words, it
	 * obtains a thread lock and begins a database transaction.
	 * This is useful in pBeans given that object instances looked up from
	 * <code>Store</code> are often shared between threads. Database
	 * transactions ensure isolation at the database level, but
	 * they do not prevent concurrent modifications of beans
	 * in memory.
	 * <p>
	 * If another thread owns the lock identified by the given
	 * class and data name, this call will block until the other
	 * thread relinquishes its lock.
	 * <p>
	 * A call to <code>requestLock</code> <i>must</i> be matched
	 * by a call to {@link #relinquishLock}. A try-finally block
	 * is recommended, as follows.
	 * <p>
	 * <code><pre>
	 * store.requestLock(User.class, "johnsmith");
	 * try {
	 *     User user = store.selectSingle(User.class, "username", "johnsmith");
	 *     user.setBalance(user.getBalance() + 100.0);
	 *     store.save(user);
	 * } finally {
	 *     store.relinquishLock();
	 * }
	 * </pre></code>
	 * <p>
	 * Lock requests may be nested.
	 * @see #relinquishLock
	 * @see #beginTransaction
	 * @param beanClass A class.
	 * @param dataName A string that identifies the data to be accessed
	 *                 in the critical section. This can be any string
	 *                 but preferably it should be something that sufficiently identifies
	 *                 the row to be modified to provide proper lock isolation. For example, it
	 *                 could be a string representation or a hash of a 
	 *                 unique index.
	 * @param transactionIsolationLevel See {@link java.sql.Connection} constants.
	 */
	public void requestLock(Class beanClass, String dataName, int transactionIsolationLevel) throws StoreException, InterruptedException {
		String lockName = beanClass.getName() + ":" + dataName;
		try {
			this.database.requestLock(lockName, transactionIsolationLevel);
		} catch(SQLException se) {
			throw new StoreException(se);
		}
	} 
	
	public void relinquishLock() throws StoreException {
		try {
			this.database.relinquishLock();
		} catch(SQLException se) {
			throw new StoreException(se);
		}				
	}
	
	private ObjectInfo insertBeanTransac(Object persObject) throws StoreException {
		boolean inTransaction = this.inTransaction();
		if(!inTransaction) {
			this.beginTransaction();
		}
		try {
			Long id = this.insertBeanImpl(persObject);
			PersistentID pid = new PersistentID(id);
			Class clazz = persObject.getClass();
			GlobalPersistentID gid = new GlobalPersistentID(pid, clazz);
			synchronized (this.objectsMonitor) {	
				ObjectInfo oi = this.objectInfosByID.get(gid);
				if (oi != null) {
					throw new IllegalStateException("Object with same ID as that just inserted already registered. There must be a problem with JDBC transaction support, class-table mapping consistency, or bean identity (i.e. the equals method does not implement identity properly).");
				}
				oi = new ObjectInfo(pid, persObject, clazz);			
				this.objectInfosByID.put(gid, oi);
				this.objectInfosByObject.put(persObject, oi);
				return oi;
			}			
			// There may be an issue right here still.
			// At this point the object is in the map, but
			// it cannot be saved, for example.
		} finally {
			if(!inTransaction) {
				this.endTransaction();
			}
		}
	}
	
	private ObjectInfo getObjectInfo(Object persObject, boolean doInsert) throws StoreException {
		// Should not be invoked if persObject is not user-provided but
		// built from a database record.
		if(persObject == null) {
			throw new java.lang.IllegalArgumentException("Persistent object cannot be null.");
		}
		this.checkWeakReferences();
		ObjectInfo oi;
		synchronized (this.objectsMonitor) {
			oi = this.objectInfosByObject.get(persObject);
			if (oi != null) {
				return oi;
			}
		}
		// Not found.
		// Need to insert, but doing that in the synchronized block
		// could create a blottleneck. Using transaction instead.
		// Theoretically another thread could try to read the record
		// right after it's inserted and register it with the objectInfo's map.
		return this.insertBeanTransac(persObject);
	}

//	private ObjectInfo getExistingObjectInfo (Persistent persObject) {
//		if(persObject == null) {
//			throw new java.lang.IllegalArgumentException("Persistent object cannot be null.");
//		}
//		synchronized (objectsMonitor) {
//			return (ObjectInfo) this.objectInfosByObject.get(persObject);
//		}
//	}

//	/**
//	 * This method ensures that a persistent object
//	 * is known by the Store class. If the persistent
//	 * object was previously seen by the store, then
//	 * no operation is performed. Otherwise, the object
//	 * is registered in store maps. 
//	 * <p>
//	 * This method is meant for beans retrieved from
//	 * the database.
//	 */
//	private void ensureKnownImpl(Object persObject, PersistentID objectID) throws StoreException {
//		ObjectInfo oi;
//   	    oi = (ObjectInfo) this.objectInfosByObject.get(persObject);
//		if (oi == null) {
//			oi = new ObjectInfo (objectID, persObject);
//			this.objectInfosByObject.put(persObject, oi);
//			this.objectInfosByID.put(new GlobalPersistentID(objectID, persObject.getClass()), oi);
//		}
//		else if (!objectID.equals (oi.getObjectID())) {
//			throw new StoreException("Failed to ensure object " + persObject + " has ID=" + objectID + ". Expecing ID=" + oi.getObjectID() + ".");
//		}
//	}
	
	/**
	 * Determines if a class is understood by <code>Store</code> as
	 * one whose instances can be persisted in a database. 
	 * @param beanClass A bean's class.
	 */
	public static boolean isPersistable(Class beanClass) {
		return beanClass.isAnnotationPresent(PersistentClass.class) || Persistent.class.isAssignableFrom(beanClass);
	}
 
//	private Map getMarshalledValues (Object obj, PersistentID objectID) throws StoreException {    
//		Map map = getMarshalledBeanValues (obj);
//		map.put (objectIdFieldName, objectID.longValue());
//		return map;
//	}    

	private NameValueArray marshallBean(Object bean, StoreInfo sinfo, boolean normalizeFieldNames, InsertionEntry currentInsertionEntry) throws StoreException {
		PropertyDescriptor[] pds = sinfo.getPropertyDescriptors();
		ArrayList pairList = new ArrayList(pds.length);
		Database database = this.database;
		for (int i = 0; i < pds.length; i++) {
			PropertyDescriptor pd = pds[i];
			FieldDescriptor fd = sinfo.getFieldDescriptor(this, pd);
			if (fd != null) {
				Method readMethod = pd.getReadMethod();
				Object value;
				try {
					value = readMethod.invoke (bean, new Object[0]);
				}
				catch (IllegalAccessException iae) {
					String propName = pd.getName();
					throw new StoreException("Store has no access to property " + propName + " in class " + sinfo.getBeanClass().getName() + ".", iae);
				} catch (InvocationTargetException ite) {
					String propName = pd.getName();
					throw new StoreException("Unable to invoke getter of property " + propName + " in class " + sinfo.getBeanClass().getName() + ".", ite);
				}
				String fieldName = normalizeFieldNames ? database.normalizeName(fd.getName()) : fd.getName();
				pairList.add(new Object[] {fieldName, marshallValue (sinfo, pd, value, currentInsertionEntry) });
			}
		}
		int length = pairList.size();
		NameValueArray nva = new NameValueArray(length);
		for(int i = 0; i < length; i++) {
			Object[] pair = (Object[]) pairList.get(i);
			nva.setPair(i, (String) pair[0], pair[1]);
		}
		return nva;
	}

	/**
	 * Marshalls a map of property-based (Java) criteria into a map of
	 * field-based (database) criteria.
	 */
	private Map marshallCriteriaValues(Class beanClass, StoreInfo sinfo, Map values) throws StoreException {
		Map mValues;
		if (values instanceof Criteria)
		{
			Criteria tmpCrit = (Criteria) values;
			mValues = new Criteria(tmpCrit.getOperatorMap(), tmpCrit.getDefaultComparisonOperator(), tmpCrit.getLogicalOperatorMap(), tmpCrit.getDefaultLogicalOperator());
		}
		else
		{
			mValues = new HashMap();
		}
		try {
			BeanInfo beanInfo = Introspector.getBeanInfo (beanClass);
			PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
			for (int i = 0; i < pds.length; i++) {
				PropertyDescriptor pd = pds[i];
				FieldDescriptor fd = sinfo.getFieldDescriptor(this,pd);
				if (fd != null) {
					String propName = pd.getName();
					if (values.containsKey(propName)) {
						Object value = values.get(propName);
						String fieldName = fd.getName();
						if (value != null) {
							mValues.put (fieldName, marshallValue(sinfo, pd, value, null));
						}
						else {
							mValues.put (fieldName, null);
						}
					}
				}
			}
		} catch (IntrospectionException ie) {
			throw new StoreException (ie);
		}
		return mValues;
	}

	private Object marshallValue(StoreInfo sinfo, String propertyName, Object value, InsertionEntry currentInsertionEntry) throws StoreException {
		PropertyDescriptor pd = sinfo.getPropertyDescriptor(propertyName);
		if(pd == null) {
			throw new StoreException("Property " + pd.getName() + " does not exist.");
		}
		return this.marshallValue(sinfo, pd, value, currentInsertionEntry);
	}
	
	private long[] fetchRawIDDArrayDuringMarshall(StoreInfo sinfo, PropertyDescriptor pd, FieldDescriptor fd, Object[] objects, InsertionEntry currentInsertionEntry) throws StoreException {
		if(fd == null) {
			throw new IllegalStateException("Property " + pd.getName() + " is not persistent.");
		}
		LinkedList<InsertionEntry> insertionStack = this.insertionStackTL.get();
		if(insertionStack != null) {
			if(currentInsertionEntry == null && !insertionStack.isEmpty()) {
				throw new IllegalStateException("Insertion stack is active but not currently in an insertion; scenario not supported (UpdateEntry would be necessary).");
			}
		}
		Map<InsertionEntry,Collection<Integer>> indexesByObject = new IdentityHashMap<InsertionEntry,Collection<Integer>>();
		long[] rawObjectIDs = new long[objects.length];
		for(int objidx = 0; objidx < objects.length; objidx++) {
			Object persObject = objects[objidx];
			if(persObject == null) {
				rawObjectIDs[objidx] = 0;
			}
			else {
				boolean matchesStack = false;
				if(insertionStack != null) {
					for(int i = insertionStack.size(); --i >= 0;) {
						InsertionEntry entry = insertionStack.get(i);
						if(entry.contains(persObject)) {
							matchesStack = true;
							Collection<Integer> indexes = indexesByObject.get(entry);
							if(indexes == null) {
								indexes = new LinkedList<Integer>();
								indexesByObject.put(entry, indexes);
							}
							indexes.add(objidx);
							// It's not possible that this will happen
							// more than once in this loop. Break.
							break;
						}
					}
				}
				if(matchesStack) {
					// Temporary ID until late update.
					rawObjectIDs[objidx] = 0;
				}
				else {
					PersistentID pid = this.getPersistentID(persObject);
					rawObjectIDs[objidx] = pid.longValue();
				}
			}
		}
		if(indexesByObject != null) {
			Iterator<Map.Entry<InsertionEntry,Collection<Integer>>> i = indexesByObject.entrySet().iterator();
			while(i.hasNext()) {
				Map.Entry<InsertionEntry,Collection<Integer>> entry = i.next();
				InsertionEntry insentry = entry.getKey();
				Collection<Integer> indexes = entry.getValue();
				LateUpdate update = new LateUpdate(sinfo, pd, fd.getName(), false, rawObjectIDs, indexes);
				insentry.addLateUpdateToExecute(update);
				currentInsertionEntry.addLateUpdateToModify(update);
			}
		}
		return rawObjectIDs;
	}

	private PersistentID fetchPersistentIDDuringMarshall(StoreInfo sinfo, PropertyDescriptor pd, FieldDescriptor fd, Object propertyValueObject, boolean forGlobalReference, InsertionEntry currentInsertionEntry) throws StoreException {
		if(fd == null) {
			throw new IllegalStateException("Property " + pd.getName() + " is not persistent.");
		}
		LinkedList<InsertionEntry> insertionStack = this.insertionStackTL.get();
		if(insertionStack != null) {
			if(currentInsertionEntry == null && !insertionStack.isEmpty()) {
				throw new IllegalStateException("Insertion stack is active but not currently in an insertion; scenario not supported (UpdateEntry would be necessary).");
			}
			LateUpdate update = null;
			for(int i = insertionStack.size(); --i >= 0;) {
				InsertionEntry entry = insertionStack.get(i);
				if(entry.contains(propertyValueObject)) {
					if(update == null) {
						update = new LateUpdate(sinfo, pd, fd.getName(), forGlobalReference);
					}
					entry.addLateUpdateToExecute(update);
					// It's not possible that this will happen
					// more than once in this loop. Break.
					break;
				}
			}
			if(update != null) {
				currentInsertionEntry.addLateUpdateToModify(update);
				// Return temporary PersistentID (non-null just in case there are constraints).
				return new PersistentID(0L);
			}
		}
		return this.getPersistentID(propertyValueObject);
	}

	private Object marshallValue(StoreInfo sinfo, PropertyDescriptor pd, Object value, InsertionEntry currentInsertionEntry) throws StoreException {
		if(value instanceof String || value instanceof Integer || value instanceof Long || value == null) {
			return value;
		}
		else { 
			Class propertyType = pd.getPropertyType();
			FieldDescriptor fd = sinfo.getFieldDescriptor(this, pd);
			boolean globalRef = fd == null ? false : fd.isGlobalReference();
			if(globalRef) {
				Class valueClass = value.getClass();
				if(!Store.isPersistable(valueClass)) {
					throw new StoreException("Unable to marshall value of type " + value.getClass().getName() + " for global reference property in class " + sinfo.getBeanClass().getName() + ". The value type must either be null, a class annotated with @PersistentClass or one that implements Persistent.");
				}
				PersistentID pid = this.fetchPersistentIDDuringMarshall(sinfo, pd, fd, value, true, currentInsertionEntry);
				GlobalPersistentID gpid = new GlobalPersistentID(pid, valueClass);
				return gpid.toString();
			}
			else if(Store.isPersistable(propertyType)) {
				Class valueClass = value.getClass();
				if(valueClass != pd.getPropertyType()) {
					throw new StoreException("Unable to marshall value of type " + valueClass.getName() + " for property of type " + pd.getPropertyType() + " in class " + sinfo.getBeanClass().getName() + ". Values not matching the property type exactly are not allowed unless the property is annotated using the globalReference element of the @PersistentProperty annotation.");
				}
				return this.fetchPersistentIDDuringMarshall(sinfo, pd, fd, value, false, currentInsertionEntry).longValue();
			}
			else if(propertyType.isArray() && Store.isPersistable(propertyType.getComponentType())) {
				// Convert to array of longs
				Class componentType = propertyType.getComponentType();
				Object[] objects = (Object[]) value;
				// Do some checks...
				for(int i = 0; i < objects.length; i++) {
					Object object = objects[i];
					if(object != null) {
						if(object.getClass() != componentType) {
							throw new StoreException("Unable to marshall value of type " + object.getClass().getName() + " for array property with component type " + componentType.getClass().getName() + " in class " + sinfo.getBeanClass().getName() + ".");							
						}
					}
				}
				// Note: fetch method does not support global references.
				long[] rawObjectIDs = this.fetchRawIDDArrayDuringMarshall(sinfo, pd, fd, objects, currentInsertionEntry);
				return sinfo.marshallValue(pd, rawObjectIDs);
			}
			else {
				return sinfo.marshallValue (pd, value);
			}
		}
	}

	private Object unmarshallValue(StoreInfo sinfo, PropertyDescriptor pd, Object value) throws StoreException {
		if (value == null) {
			return null;
		}
		else {
			FieldDescriptor fd = sinfo.getFieldDescriptor(this, pd);
			boolean globalRef = fd == null ? false : fd.isGlobalReference();
			if(globalRef) {
				String rawGid = value instanceof String ? (String) value : String.valueOf(value);
				GlobalPersistentID gid;
				try {
					gid = GlobalPersistentID.valueOf(rawGid);
				} catch(Exception err) {
					logger.warning("unmarshallValue(): Unable to parse globalPersistentID=" + rawGid);
					return null;
				}
				return this.getObject(gid);
			}
			else {
				Class propType = pd.getPropertyType();
				if(Store.isPersistable(propType)) { 
					if(!(value instanceof Long)) {
						logger.warning("Expected value for property " + pd.getName() + " to be of type Long, not " + value.getClass().getName() + ".");
						return null;
					}
					PersistentID objectID = new PersistentID(((Long) value).longValue());
					return getObject(objectID, propType);		
				}
				else if(propType.isArray()) {
					Class componentType = propType.getComponentType();
					if(Store.isPersistable(componentType)) {
						long[] rawObjectIDs = (long[]) sinfo.unmarshallValue(pd, value);
						if(rawObjectIDs == null) {
							return null;
						}
						Object[] objects = (Object[]) java.lang.reflect.Array.newInstance(componentType, rawObjectIDs.length);
						for(int i = 0; i < objects.length; i++) {
							long rawID = rawObjectIDs[i];
							Object object = rawID == 0 ? null : this.getObject(new PersistentID(rawID), componentType);
							objects[i] = object;
						}
						return objects;
					}
					else {
						return sinfo.unmarshallValue (pd, value);						
					}
				}
				else {
					return sinfo.unmarshallValue (pd, value);
				}
			}
		}
	}

//	private Map<String,Object> getMarshalledBeanValues (Object bean) throws StoreException {
//		try {
//			Class beanClass = bean.getClass();
//			StoreInfo sinfo = getStoreInfo(beanClass);
//			BeanInfo beanInfo = Introspector.getBeanInfo (beanClass);
//			PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
//			Map<String,Object> values = new HashMap<String,Object>(pds.length * 2);
//			for (int i = 0; i < pds.length; i++) {
//				PropertyDescriptor pd = pds[i];
//				FieldDescriptor fd = sinfo.getFieldDescriptor(this,pd);
//				if (fd != null) {
//					String propName = pd.getName();
//					Method readMethod = pd.getReadMethod();
//					Object value;
//					try {
//						value = readMethod.invoke (bean, new Object[0]);
//					}
//					catch (IllegalAccessException iae) {
//						throw new StoreException ("Store has no access to property " + propName + " in class " + beanClass.getName(), iae);
//					}
//					values.put (fd.getName(), marshallValue (sinfo, pd, value));
//				}
//			}
//			return values;
//		} catch (IntrospectionException ie) {
//			throw new StoreException (ie);
//		} catch (InvocationTargetException ite) {
//			throw new StoreException (ite);
//		}
//	}

	private boolean equalValues(NameValueArray array1, NameValueArray array2) {
		// Assumes that names are in the same order in both arrays.
		if(array1 == null) {
			return array2 == null;
		}
		else if(array2 == null) {
			return false;
		}
		int length = array1.length;
		Object[] values1 = array1.valueArray;
		Object[] values2 = array2.valueArray;
		for(int i = 0; i < length; i++) {
			Object val1 = values1[i];
			Object val2 = values2[i];
			if (val1 == null) {
				if (val2 != null) {
					return false;
				}
			}
			else if (!val1.equals (val2)) {
				return false;
			}			
		}
		return true;
	}
	
	private boolean equalValues (Map values1, Map values2) {
		if (values2 == null) {
			return values1 == null;
		}
		Iterator i1 = values1.entrySet().iterator();
		while (i1.hasNext()) {
			Map.Entry entry1 = (Map.Entry) i1.next();
			String key1 = (String) entry1.getKey();
			Object val1 = entry1.getValue();
			Object val2 = values2.get (key1);
			if (val1 == null) {
				if (val2 != null) {
					return false;
				}
			}
			else if (!val1.equals (val2)) {
				return false;
			}
		}
		return true;
	}

	private void warn (String text) {
		if(logger.isLoggable(Level.WARNING)) {
			logger.warning(text);
		}
	}
	
	//------------------------ Finders & Updaters -------------------

	/**
	 * Gets an iterator of objects of the given class which have a "foreign key"
	 * property value equal to the <code>containerBean</code> given. 
	 * The foreign key property <code>linkPropertyInPart</code> type must be assignable to Persistent
	 * (or PersistentID).
	 * <p>
	 * This is useful
	 * in queryting one-to-many relationships, and getting parts of a whole.
	 * @throws StoreException
	 */
	public ResultsIterator selectParts (Object containerBean, final Class partbeanClass, String linkPropertyInPart) throws StoreException {
		Map props = new HashMap(2);
		props.put(linkPropertyInPart, containerBean);
		return select (partbeanClass, props);
	}

	/**
	 * Gets an iterator of objects of the given class which have a "foreign key"
	 * property value equal to the <code>containerBeanID</code> given. 
	 * The foreign key property <code>linkPropertyInPart</code> type must be assignable to Persistent
	 * (or PersistentID).
	 * <p>
	 * This is useful
	 * in queryting one-to-many relationships, and getting parts of a whole.
	 * @throws StoreException
	 */
	public ResultsIterator selectParts (PersistentID containerBeanID, final Class partbeanClass, String linkPropertyInPart) throws StoreException {
		Map props = new HashMap(2);
		props.put (linkPropertyInPart, containerBeanID);
		return select (partbeanClass, props);
	}

	/**
	 * Queries beans linked to a given bean by means of an intermediate
	 * class. In other words, this query is designed for many-to-many
	 * relationships involving three classes, e.g. Instructor, Department
	 * and InstructorDepartment. A sample invocation follows.
	 * <p>
	 * <code><pre>
	 * // Find departments an instructor works for
	 * store.selectParts(instructor, InstructorDepartment.class, Department.class, "instructorID", "departmentID");
	 * </pre></code>
	 * <p>
	 * Notes:
	 * <ul>
	 * <li>This method requires INNER JOIN support.</li>
	 * <li>At the very least <code>propertyLinkingContainer</code> should be indexed.</li>
	 * </ul>
	 * @param containerBeanID ID of the bean we're trying find related beans for.
	 * @param linkClass The intermediate class used to implement the many-to-many relationship.
	 * @param partClass The class we are querying.
	 * @param propertyLinkingContainer Property in <code>linkClass</code> which
	 *        refers to the <code>containerBean</code>instance.
	 * @param propertyLinkingPart Property in <code>linkClass</code> which
	 *        refers to a <code>partClass</code> instance.
	 * @throws StoreException
	 */
	public ResultsIterator selectParts(final PersistentID containerBeanID, final Class linkClass, final Class partClass, String propertyLinkingContainer, String propertyLinkingPart) throws StoreException {
		Database database = this.database;
		StoreInfo partSinfo = this.getStoreInfo(partClass);
		String partNormalTableName = database.normalizeName(partSinfo.getTableName(this.maxTableNameLength));
		String partNormalFieldID = database.normalizeName(partSinfo.getIdField());
		//StoreInfo containerSinfo = this.getStoreInfo(containerBeanClass);
		//String containerNormalTableName = database.normalizeName(containerSinfo.getTableName(this.maxTableNameLength));
		//String containerNormalFieldID = database.normalizeName(containerSinfo.getIdField());
		StoreInfo linkSinfo = this.getStoreInfo(linkClass);
		String linkNormalTableName = database.normalizeName(linkSinfo.getTableName(this.maxTableNameLength));
		StringBuffer sqlBuffer = new StringBuffer();
		sqlBuffer.append("SELECT ");
		sqlBuffer.append(partNormalTableName);
		sqlBuffer.append(".* FROM ");
		sqlBuffer.append(linkNormalTableName);
		sqlBuffer.append(" INNER JOIN ");
		sqlBuffer.append(partNormalTableName);
		sqlBuffer.append(" ON (");
		sqlBuffer.append(linkNormalTableName);
		sqlBuffer.append('.');
		sqlBuffer.append(propertyLinkingPart);
		sqlBuffer.append('=');
		sqlBuffer.append(partNormalTableName);
		sqlBuffer.append('.');
		sqlBuffer.append(partNormalFieldID);		
		sqlBuffer.append(") WHERE ");
		sqlBuffer.append(linkNormalTableName);
		sqlBuffer.append('.');
		sqlBuffer.append(propertyLinkingContainer);
		sqlBuffer.append('=');
		sqlBuffer.append(containerBeanID);
		String sqlQuery = sqlBuffer.toString();
		if(logger.isLoggable(Level.INFO)) {
			logger.info("selectParts(): sql=[" + sqlQuery + "]");
		}
		return this.select(partClass, sqlQuery, new Object[0]);
	}

	/**
	 * Gets an iterator of all stored instances of the given class.
	 */
	public ResultsIterator select (final Class inbeanClass) throws StoreException {
		return select (inbeanClass, null);
	}

	/**
	 * 
	 * Finds a stored instance with the property value given.
	 * @return An instance of <code>beanClass</code> or <code>null</code> 
	 *         if an object is not found.
	 * @throws StoreException
	 */
	public Object selectSingle (final Class beanClass, String propertyName, Object propertyValue) throws StoreException {
		Map values = new HashMap(2);
		values.put (propertyName, propertyValue);
		return selectSingle (beanClass, values);				      
	}

	/**
	 * Finds a stored instance matching the property values given.
	 * @param beanClass Only instances of this class are searched.
	 * @param values A map of property names to values.
	 * @return An instance of <code>beanClass</code> or <code>null</code> 
	 *         if an object is not found.
	 * @throws StoreException
	 */
	public Object selectSingle (final Class beanClass, Map values) throws StoreException {
		ResultsIterator i = select (beanClass, values);
		try {
			if (i.hasNext()) {
				return i.next();
			}
		} finally {
			i.close();
		}
		return null;
	}

	/**
	 * Finds a stored instance matching the property values given.
	 * @param beanClass Only instances of this class are searched.
	 * @param values A map of property names to values.
	 * @return An instance of <code>beanClass</code> or <code>null</code> 
	 *         if an object is not found.
	 * @throws StoreException
	 */
	public Object selectSingle (final Class beanClass, Map values, String orderBy, boolean descending) throws StoreException {
		ResultsIterator i = select (beanClass, values, orderBy, descending, new Integer(1));
		try {
			if (i.hasNext()) {
				return i.next();
			}
		} finally {
			i.close();
		}
		return null;
	}    


	/**
	 * Finds all stored instances matching the property values given.
	 * @param beanClass Only instances of this class are searched.
	 * @param values A map of property names to values.
	 * @return An iterator of Persistent objects.
	 * @throws StoreException
	 */
	public ResultsIterator select (final Class beanClass, Map values) throws StoreException {
		return select (beanClass, values, null, false, null);
	}

	/**
	 * Finds stored instances matching the property values given.
	 * @param beanClass Only instances of this class are searched.
	 * @param values A map of property names to values. A value of <code>null</code> indicates
	 *               all stored instances should 
	 * @param orderByProperty Property name to order by. A value of <code>null</code> indicates
	 *                        there's no ordering.
	 * @param descending In case orderByProperty is not null, whether ordering should descend.
	 * @param limit The maximum number of results to return. A value of <code>null</code>
	 *              indicates there's no limit.
	 * @return An iterator of Persistent objects.
	 * @throws StoreException
	 */
	public ResultsIterator select (final Class beanClass, Map values, String orderByProperty, boolean descending, Integer limit) throws StoreException {
		BeanFactory beanFactory = new SimpleBeanFactory(beanClass);
		return this.select(beanClass, values, orderByProperty, descending, limit, beanFactory);
	}

	/**
	 * Finds stored instances matching the property values given.
	 * @param beanClass Only instances of this class are searched.
	 * @param propertyValues A map of property names to values. The map can be of
	 *               type <code>Criteria</code> which allows operators other
	 *               than equality.
	 * @param orderByProperty Property name to order by. A value of <code>null</code> indicates
	 *                        there's no ordering.
	 * @param descending In case orderByProperty is not null, whether ordering should descend.
	 * @param limit The maximum number of results to return. A value of <code>null</code>
	 *              indicates there's no limit.
	 * @param beanFactory A factory that instantiates <code>beanClass</code>.
	 * @return An iterator of Persistent objects.
	 * @throws StoreException
	 */
	public ResultsIterator select(final Class beanClass, Map propertyValues, String orderByProperty, boolean descending, Integer limit, BeanFactory beanFactory) throws StoreException {
		StoreInfo sinfo = this.getStoreInfo(beanClass);
		String tableName = sinfo.getTableName(this.maxTableNameLength);
		String cond = propertyValues == null ? null : getNameValueSequence(sinfo, propertyValues, "AND", true, true);
		Object[] params = propertyValues == null ? EMPTY_PARAMETERS : getParameters(sinfo, propertyValues, true, null);
		String orderBy = null;
		if(orderByProperty != null) {
			PropertyDescriptor orderByPd = sinfo.getPropertyDescriptor(orderByProperty);
			if(orderByPd == null) {
				throw new StoreException("No such property: " + orderByProperty);
			}
			FieldDescriptor orderByFd = sinfo.getFieldDescriptor(this, orderByPd);
			if(orderByFd == null) {
				throw new StoreException("Property " + orderByProperty + " not persistent.");
			}
			orderBy = orderByFd.getName();
		}
		Database database = this.database;
		String normalTableName = database.normalizeName(tableName);
		String normalOrderBy = database.normalizeName(orderBy);
		String queryString = "SELECT * FROM " + normalTableName + (cond == null ? "" : " WHERE " + cond) + (normalOrderBy == null ? "" : " ORDER BY " + normalOrderBy) + (descending ? " DESC " : "") + (limit == null ? "" : " LIMIT " + limit);
		return this.select(beanClass, queryString, params, beanFactory);
	}

	/**
	 * General-purpose method for arbitrary SQL
	 * queries that return an iterator of 
	 * beans. Note that this is a potentially
	 * unsafe method. The caller is expected to
	 * query the right table, with the right field
	 * list, and matching bean class information.
	 * <p>
	 * The <code>select</code> overload that takes
	 * a <code>BeanFactory</code> argument is potentially faster.
	 * @param sqlQuery An arbitrary SQL query, optionally with JDBC-style placeholders (i.e. question marks). 
	 * @param parameters An array of parameters corresponding to placeholders in the query.
	 * @param beanClass The bean class that will be instantiated for each row.
	 * @return An iterator of beans. Note that the iterator should be closed when the caller is done with it.
	 */
	public ResultsIterator select(Class beanClass, String sqlQuery, Object[] parameters) throws StoreException {
		BeanFactory factory = new SimpleBeanFactory(beanClass);
		return this.select(beanClass, sqlQuery, parameters, factory);
	}
	
	/**
	 * General-purpose method for arbitrary SQL
	 * queries that return an iterator of 
	 * beans. Note that this is a potentially
	 * unsafe method. The caller is expected to
	 * query the right table, with the right field
	 * list, and matching bean class information.
	 * @param sqlQuery An arbitrary SQL query, optionally with JDBC-style placeholders (i.e. question marks). 
	 * @param parameters An array of parameters corresponding to placeholders in the query.
	 * @param beanClass The bean class that will be instantiated for each row.
	 * @param beanFactory A bean class instantiator factory.
	 * @return An iterator of beans. Note that the iterator should be closed when the caller is done with it.
	 */
	public ResultsIterator select(Class beanClass, String sqlQuery, Object[] parameters, BeanFactory beanFactory) throws StoreException {
		StoreInfo sinfo = this.getStoreInfo(beanClass);
		BeanUnmarshaller bu = new LocalBeanUnmarshaller(sinfo, beanFactory);
		try {
			final ResultsIterator<BeanWrapper> llbi = this.database.query(sqlQuery, parameters, bu);
			return new ResultsIterator() {
				@Override
				public void finalize() {
					llbi.close();
				}

				public boolean hasNext() {
					return llbi.hasNext();
				}

				public Object next() {
					BeanWrapper bw = llbi.next();
					// Note that the unmarshaller 
					// has already registered the
					// object with the store.
					return bw.bean;
				}

				public void remove() {
					throw new UnsupportedOperationException();
				}

				public void close() {
					llbi.close();
				}			
			};
		} catch(SQLException se) {
			throw new StoreException(se);
		}
	}

	
	/**
	 * Deletes all database records that match the given criteria.
	 * Once the record corresponding to a Persistent object is deleted,
	 * subsequent invocations to <code>save</code> will fail.
	 * @throws StoreException
	 */
	public boolean delete (Class beanClass, Map values) throws StoreException {
		StoreInfo sinfo = getStoreInfo(beanClass);
		String tableName = getTableName (beanClass);
		Map mValues = marshallCriteriaValues(beanClass, sinfo, values);
		if (mValues.size() == 0) {
			throw new StoreException ("No marshalled condition values found from map provided. Make sure property names used in query exist, are not capitalized, and are persistent.");
		}
		try {
			return deleteRecords (tableName, mValues);
		} catch (SQLException se) {
			throw new StoreException (se);
		}
	}

	/**
	 * Executes an arbitrary database update. The bean class provided
	 * along with the array of property names are used to convert
	 * parameters to types that can be saved in the database.
	 * @param beanClass A bean class.
	 * @param sqlStatement An arbitrary SQL update statement.
	 * @param parameters An array of Java objects. These can be bean instances,
	 *                   which should be converted to reference IDs properly.
	 * @param propertyNames An array of property names corresponding to each
	 *                      parameter. Property names are only used for 
	 *                      conversion purposes. This array can be shorter than the 
	 *                      parameters array. Remaining parameters won't
	 *                      be converted.
	 * @return True if updates were actually made.
	 */
	public boolean update(Class beanClass, String sqlStatement, String[] propertyNames, Object[] parameters) throws StoreException {
		StoreInfo sinfo = this.getStoreInfo(beanClass);
		Object[] newParameters = new Object[parameters.length];
		for(int i = 0; i < parameters.length; i++) {
			if(i < propertyNames.length) {
				PropertyDescriptor pd = sinfo.getPropertyDescriptor(propertyNames[i]);
				if(pd == null) {
					throw new StoreException("No property named " + propertyNames[i] + " found in class " + beanClass.getName() + ".");
				}
				newParameters[i] = this.marshallValue(sinfo, pd, parameters[i], null);
			}
			else {
				newParameters[i] = parameters[i];
			}
		}
		try {
			return this.database.update(sqlStatement, newParameters) > 0;
		} catch(SQLException se) {
			throw new StoreException(se);
		}
	}
	
//	/** 
//	 * Requests a lock for a record with a given column value.
//	 * This method may block if another thread is holding a lock
//	 * with the same parameters. Call <code>relinquishLock</code> at the end
//	 * of the critical section, preferably in a finally block.
//	 * @throws StoreException
//	 */
//	public Object requestLock (Class beanClass, String propertyName, Object value) throws StoreException, InterruptedException {
//		StoreInfo sinfo = getStoreInfo (beanClass);
//		PropertyDescriptor pd = sinfo.getPropertyDescriptor(propertyName);
//		if (pd == null) {
//			throw new IllegalArgumentException ("Property not found " + propertyName);
//		}
//		FieldDescriptor fd = sinfo.getFieldDescriptor(this,pd);
//		if (fd == null) {
//			throw new IllegalArgumentException ("Property " + propertyName + " is transient.");
//		}
//		Object mValue = marshallValue (sinfo, pd, value);
//		try {
//			return database.requestLock (getTableName (beanClass), fd.getName(), mValue);
//		} catch (SQLException se) {
//			throw new StoreException (se);
//		}
//	}
//
//	/**
//	 * Must be invoked to give up a lock previously obtained
//	 * by calling <code>requestLock</code>.
//	 * @throws StoreException
//	 */
//	public void relinquishLock (Object lock) throws StoreException {
//		try {
//			database.relinquishLock (lock);
//		} catch (SQLException se) {
//			throw new StoreException (se);
//		}
//	}

	//---------------- Database Methods ----------------------------

	private boolean deleteRecords (String tableName, Map fieldValues)  throws SQLException, StoreException {
		String cond = getNameValueSequence (null, fieldValues, " AND ", true, false);
		Object[] params = getParameters(null, fieldValues, false, null);
		return this.database.update ("DELETE FROM " + tableName + " WHERE " + cond, params) > 0;
	}
//
//	private boolean deleteRecords (String tableName, String cond)  throws SQLException {
//		return this.database.update ("delete FROM " + tableName + " WHERE " + cond, new Object[0]) > 0;
//	}
//
//	private boolean insertRecord (String tableName, Map fieldValues) throws SQLException, StoreException  {
//		String names = getNameSequence (fieldValues, ",");
//		String values = getPlaceholderSequence (fieldValues.size(), ",");
//		Object[] params = getParameters(null, fieldValues, false);
//		return this.database.update ("insert into " + tableName + " (" + names + ") values (" + values + ")", params) > 0;
//	}

//	private boolean updateRecords(String tableName, Map fieldValues, String condition) throws SQLException, StoreException {
//		String setters = getNameValueSequence(null, fieldValues, ",", false, false);
//		Object[] params = getParameters(null, fieldValues, false);
//		return this.database.update ("UPDATE " + tableName + " SET " + setters + " WHERE " + condition, params) > 0;
//	}

	/**
	 * The insertion stack is used to determined whether an object
	 * being marshalled is one that is currently undergoing insertion
	 * (i.e. before its ID has been determined). In other words,
	 * this thread local helps avoid infinite recursion due to 
	 * self-loops, direct or indirect.
	 */
	private final ThreadLocal<LinkedList<InsertionEntry>> insertionStackTL = new ThreadLocal<LinkedList<InsertionEntry>>();
	
	private Long insertBeanImpl(Object persObject) throws StoreException {
		// All bean insertions must go through this method.
		Class beanClass = persObject.getClass();
		StoreInfo sinfo = this.getStoreInfo(beanClass);
		Long resultingID = null;
		LinkedList<InsertionEntry> insertionStack = this.insertionStackTL.get();
		if(insertionStack == null) {
			insertionStack = new LinkedList<InsertionEntry>();
			this.insertionStackTL.set(insertionStack);
		}
		InsertionEntry entry = new InsertionEntry(persObject);
		insertionStack.add(entry);
		try {
			String tableName = sinfo.getTableName(this.maxTableNameLength);
			String idField = sinfo.getIdField();
			boolean autoIncrementRequested = sinfo.isAutoIncrementRequested();
			NameValueArray pairs = this.marshallBean(persObject, sinfo, true, entry);
			try {
				resultingID = this.database.insert(tableName, idField, pairs.nameArray, pairs.valueArray, autoIncrementRequested);		
				// resultingID is needed by finally block.
				return resultingID;
			} catch(SQLException se) {
				if (database.isDuplicateEntryError(se)) {
					throw new DuplicateEntryException ("Duplicate entry on " + beanClass.getName(), se);
				}
				else {
					throw new StoreException (se);
				}
			}
		} finally {
			insertionStack.removeLast();
			LinkedList lateUpdatesToModify = entry.getLateUpdatesToModify();
			if(lateUpdatesToModify != null) {
				PersistentID pid = new PersistentID(resultingID);
				Iterator i = lateUpdatesToModify.iterator();
				while(i.hasNext()) {
					LateUpdate update = (LateUpdate) i.next();
					update.setIdToUpdate(pid);
				}				
			}
			LinkedList lateUpdatesToExecute = entry.getLateUpdatesToExecute();
			if(lateUpdatesToExecute != null) {
				Iterator i = lateUpdatesToExecute.iterator();
				while(i.hasNext()) {
					LateUpdate update = (LateUpdate) i.next();
					try {
						if(logger.isLoggable(Level.INFO)) {
							logger.info("insertBeanImpl(): Will execute late update due to self-loop: " + update + ".");
						}
						update.execute(beanClass, resultingID);
					} catch(Exception err) {
						logger.log(Level.WARNING, "insertBeanImpl(): Unable to execute late update: " + update + ".", err);
					}
				}
			}
		}
	}
	
	private boolean updateBeanImpl(Object persObject, PersistentID objectID) throws StoreException {
		Database database = this.database;
		Class beanClass = persObject.getClass();
		StoreInfo sinfo = this.getStoreInfo(beanClass);
		String tableName = database.normalizeName(sinfo.getTableName(this.maxTableNameLength));
		String idField = database.normalizeName(sinfo.getIdField());
		NameValueArray pairs = this.marshallBean(persObject, sinfo, false, null);
		String setters = this.getUpdateNameValueSequence(sinfo, pairs);
		Object[] parameters =  pairs.valueArray;
		String sql = "UPDATE " + tableName + " SET " + setters + " WHERE " + idField + "=" + objectID;
		try {
			return this.database.update(sql, parameters) > 0;		
		} catch(SQLException se) {
			if (database.isDuplicateEntryError(se)) {
				throw new DuplicateEntryException ("Duplicate entry on " + beanClass.getName(), se);
			}
			else {
				throw new StoreException (se);
			}
		}		
	}
	
	private boolean updatePairsImpl(StoreInfo sinfo, NameValueArray fieldValuePairs, PersistentID objectID) throws StoreException {
		String tableName = sinfo.getTableName(this.maxTableNameLength);
		Database database = this.database;
		String idField = database.normalizeName(sinfo.getIdField());
		String setters = this.getUpdateNameValueSequence(sinfo, fieldValuePairs);
		Object[] parameters =  fieldValuePairs.valueArray;
		String sql = "UPDATE " + database.normalizeName(tableName) + " SET " + setters + " WHERE " + idField + "=" + objectID;
		try {
			return this.database.update(sql, parameters) > 0;		
		} catch(SQLException se) {
			if (database.isDuplicateEntryError(se)) {
				throw new DuplicateEntryException("Duplicate entry on " + sinfo.getClass().getName(), se);
			}
			else {
				throw new StoreException (se);
			}
		}		
	}

	private boolean deleteBeanImpl(StoreInfo sinfo, PersistentID objectID) throws StoreException {
		String tableName = sinfo.getTableName(this.maxTableNameLength);
		String sql = "DELETE FROM " + tableName + " WHERE " + sinfo.getIdField() + "=" + objectID;
		try {
			return this.database.update(sql, new Object[0]) > 0;		
		} catch(SQLException se) {
			throw new StoreException(se);
		}		
	}
	
	private String getUpdateNameValueSequence(StoreInfo storeInfo, NameValueArray fieldValuePairs) {
		int length = fieldValuePairs.length;
		String[] nameArray = fieldValuePairs.nameArray;
		StringBuffer condBuf = new StringBuffer();
		Database database = this.database;
		for(int i = 0; i < length; i++) {
			if(i > 0) {
				condBuf.append(",");
			}
			String fieldName = nameArray[i];
			condBuf.append (database.normalizeName(fieldName));
			condBuf.append ("=?");
		}
		return condBuf.toString();
	}

	/**
	 * 
	 * @param storeInfo A StoreInfo instance. It may be null if isPropertyValues==false
	 * @param values
	 * @param defaultLogicalOperator
	 * @param isQuery
	 * @param isPropertyValues
	 * @throws StoreException
	 */
	private String getNameValueSequence(StoreInfo storeInfo, Map values, String defaultLogicalOperator, boolean isQuery, boolean isPropertyValues) throws StoreException {

		// TODO: Implement MultiValueContainer handling

		// some changes to implement logical operators and more by Philipp Walther @ REDMOON.ch
		StringBuffer condBuf = new StringBuffer();
		Criteria criteria = null;
		LogicalOperator logicalOperator = new SimpleLogicalOperator(defaultLogicalOperator);
		if (values instanceof Criteria)
		{
			criteria = (Criteria) values;
		}
		Database database = this.database;
		Iterator i = values.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry entry = (Map.Entry) i.next();
			String fieldName;
			String itemName = (String) entry.getKey();
			if(isPropertyValues) {
				String propertyName = itemName;
				PropertyDescriptor pd = storeInfo.getPropertyDescriptor(propertyName);
				if(pd == null) {
					throw new StoreException("Property " + propertyName + " not found.");
				}
				FieldDescriptor fd = storeInfo.getFieldDescriptor(this, pd);
				if(fd == null) {
					throw new StoreException("Property " + propertyName + " is not persistent.");
				}
				fieldName = database.normalizeName(fd.getName());
			} 
			else {
				fieldName = database.normalizeName(itemName);
			}
			Object rawValue = entry.getValue();
			if (criteria != null)
			{
				Operator operator = criteria.getOperator(fieldName);
				logicalOperator = criteria.getLogicalOperator(fieldName);
				if(rawValue instanceof MultiValueContainer) {
					/* is multivaluecontainer */
					MultiValueContainer container = (MultiValueContainer) rawValue;
					Iterator itContainer = container.iterator();
					while(itContainer.hasNext()) {
						Object entryObject = itContainer.next(); // they are all Map.Entry since we use add(Map.Entry value) in MultiValueContainer
						if ((operator == Operator.EQUALS || operator == Operator.LIKE) && isQuery && entryObject == null) {
							operator = Operator.IS; // when comparing to null, use 'is'
						} 
						if(entryObject != null && operator == Operator.LIKE && (entryObject.getClass()== Integer.class || entryObject.getClass() == Long.class)) {
							operator = Operator.EQUALS; // 'LIKE' on NUMERIC fields is nonsense, so we autom. switch to '='
						}
						condBuf.append(operator.operation(fieldName, "?")); // ? will be replaced when preparing statement
						// if there are more condition fields add a coma
						if (itContainer.hasNext()) {
							condBuf.append(" " + logicalOperator.operatorString() + " "); // picks the logical operator (AND/OR/..)
							// logicalOperator is either a SimpleLogicalOperator("and") or set by criteria fieldValues
						}
					}

				} else {
					if ((operator == Operator.EQUALS || operator == Operator.LIKE) && isQuery && entry.getValue() == null) {
						operator = Operator.IS; // when comparing to null, use 'is'
					} 
					if(entry.getValue() != null && operator == Operator.LIKE && (entry.getValue().getClass()== Integer.class || entry.getValue().getClass() == Long.class)) {
						operator = Operator.EQUALS; // 'LIKE' on NUMERIC fields is nonsense, so we autom. switch to '='
					}
					condBuf.append(operator.operation(fieldName, "?")); // ? will be replaced when preparing statement
				}
			} else {
				/* is not a Criteria, so we use '=' (EQUAL) as default */
				condBuf.append (fieldName);
				/* comparisons to null done using 'is' */
				if (isQuery && entry.getValue() == null) {
					condBuf.append (" IS ");
				} else {
					condBuf.append ('=');
				}
				condBuf.append ('?'); // ? will be replaced when preparing statement
			}
			// if there are more condition fields add a coma
			if (i.hasNext()) {
				condBuf.append(" " + logicalOperator.operatorString() + " "); // picks the logical operator (AND/OR/..)
				// logicalOperator is either a SimpleLogicalOperator("and") or set by criteria fieldValues
			}
		}
		return condBuf.toString();
	}

//	private String getNameSequence (Map fieldValues, String separator) {
//		StringBuffer condBuf = new StringBuffer();
//		Iterator i = fieldValues.entrySet().iterator();
//		while (i.hasNext()) {
//			Map.Entry entry = (Map.Entry) i.next();
//			String fieldName = (String) entry.getKey();
//			condBuf.append (fieldName);
//			if (i.hasNext()) {
//				condBuf.append(separator);
//			}
//		}
//		return condBuf.toString();
//	}
//
//	private String getPlaceholderSequence (int count, String separator) {
//		StringBuffer condBuf = new StringBuffer();
//		for (int i = 0; i < count; i++) {
//			condBuf.append ('?');
//			if (i + 1 < count) {
//				condBuf.append(separator);
//			}
//		}
//		return condBuf.toString();
//	}

	/**
	 * All this method does is get the values from a Map fieldValues
	 * and returning it as Object[].
	 * @param fieldValues
	 * @return Object[] of parameter values
	 * @throws SQLException
	 */
	private Object[] getParameters(StoreInfo storeInfo, Map values, boolean isPropertyValues, InsertionEntry currentInsertionEntry) throws StoreException {
		Iterator i = values.entrySet().iterator();
		Collection params = new LinkedList();
		// TODO: if entry is instance of MultiValueContainer, add all it's entries the same way as vanilla entries 
		while (i.hasNext()) {
			Map.Entry entry = (Map.Entry) i.next();
			Object rawValue = entry.getValue();
			if(rawValue instanceof IMultiValueContainer) {
				Iterator MultiValueIt = ((IMultiValueContainer) rawValue).iterator();
				while(MultiValueIt.hasNext()) {
					Object nestedRawValue = MultiValueIt.next();
					Object nestedValue;
					if(isPropertyValues) {
						String propertyName = (String) entry.getKey();
						nestedValue = this.marshallValue(storeInfo, propertyName, nestedRawValue, null);						
					}
					else {
						nestedValue = rawValue;
					}				
					params.add(nestedValue);
				}
			} else {
				Object value;
				if(isPropertyValues) {
					String propertyName = (String) entry.getKey();
					value = this.marshallValue(storeInfo, propertyName, rawValue, currentInsertionEntry);
				}
				else {
					value = rawValue;
				}				
				params.add(value);
			}
		}
		return params.toArray();
	}
	
	String normalizeName(String name) {
		return this.database.normalizeName(name);
	}

	//------------------------ ObjectInfo ---------------------------

	class ObjectInfo extends WeakReference<Object> {
		private final PersistentID objectID;
		//private final String primaryTableName;
		private final Class beanClass;
		//private final Object deleteMonitor;
		private final Object saveMonitor;
		private final StoreInfo storeInfo;
		private volatile NameValueArray lastValues = null;

		ObjectInfo (PersistentID objectID, Object persObject) throws StoreException {
			super (persObject, REF_QUEUE);
			if (persObject == null) {
				throw new IllegalArgumentException ("persObject is null");
			}
			this.objectID = objectID;
			this.beanClass = persObject.getClass();
			this.storeInfo = Store.this.getStoreInfo(this.beanClass);
			//this.primaryTableName = Store.this.getTableName(this.beanClass);
			//this.deleteMonitor = this;
			this.saveMonitor = this;
			//Note: This class must not have a
			//live reference to persObject.
		}

		ObjectInfo (PersistentID objectID, Object persObject, Class beanClass) throws StoreException {
			super (persObject, REF_QUEUE);
			if (persObject == null) {
				throw new IllegalArgumentException ("persObject is null");
			}
			this.objectID = objectID;
			this.beanClass = beanClass;
			//this.primaryTableName = Store.this.getTableName(beanClass);
			this.saveMonitor = this;
			this.storeInfo = Store.this.getStoreInfo(beanClass);
			//Note: This class must not have a
			//live reference to persObject.
		}
		
		String getClassName() {
			return this.beanClass.getName();
		}

		Class getPersistentClass() {
			return this.beanClass;
		}

		PersistentID getObjectID() {
			return this.objectID;
		}
		
		GlobalPersistentID getGlobalPersistentID() {
			return new GlobalPersistentID(this.objectID, this.beanClass);
		}

		Object getObject() {
			return this.get();
		}

		// Insertion would need to happen before ObjectInfo is created.
		
//		void insert() throws StoreException {
//			Object bean = getObject();
//			if (bean == null) {
//				throw new StoreException ("Unexpected: Object already garbage collected on insert().");
//			}
//			// Table will be created when getStoreInfo is called.
//			getStoreInfo(bean.getClass());
//			// Insert record in corresponding class table.
//			Map newValues = getMarshalledValues(bean, this.objectID);
//			try {
//				synchronized (saveMonitor) {
//					insertRecord (primaryTableName, newValues);
//					this.lastValues = newValues;
//				}
//			} catch (SQLException se) {
//				if (database.isDuplicateEntryError(se)) {
//					throw new DuplicateEntryException ("Record on " + beanClass.getName() + " already exists.", se);
//				}
//				else {
//					throw new StoreException (se);
//				}
//			}
//		}

		void save(boolean force) throws StoreException {
			Object bean = this.getObject();
			StoreInfo sinfo = this.storeInfo;
			NameValueArray newValues = Store.this.marshallBean(bean, sinfo, false, null);
			synchronized(this.saveMonitor) {
				NameValueArray oldValues = this.lastValues;
				if (force || !equalValues(newValues, oldValues)) {
					//TODO: Update only changed values.
					Store.this.updatePairsImpl(sinfo, newValues, objectID);
					this.lastValues = newValues;
				}
			}
		}

		void delete(boolean forget) throws StoreException {
			//Persistent bean = this.getObject();
			synchronized(this.saveMonitor) {
				Store.this.deleteBeanImpl(this.storeInfo, this.objectID);
				this.lastValues = null;
			}
			if(forget) {
				Object bean = this.getObject();
				GlobalPersistentID gid = new GlobalPersistentID(this.objectID, this.beanClass);
				synchronized(Store.this.objectsMonitor) {
					Store.this.objectInfosByID.remove(gid);
					if(bean != null) {
						Store.this.objectInfosByObject.remove(bean);
					}
				}
			}
		}
	}

    //------------- LocalBeanUnmarshaller ------------
	
	private class LocalBeanUnmarshaller implements BeanUnmarshaller {
		private final StoreInfo storeInfo;
		private final BeanFactory beanFactory;
		
		public LocalBeanUnmarshaller(StoreInfo storeInfo, BeanFactory beanFactory) {
			this.storeInfo = storeInfo;
			this.beanFactory = beanFactory;
		}
		
		public Object registerBean(Long id) {
			Class beanClass = this.storeInfo.getBeanClass();
			PersistentID objectID = new PersistentID(id);
			GlobalPersistentID gid = new GlobalPersistentID(objectID, beanClass);
			synchronized(Store.this.objectsMonitor) {
				Object existingBean = Store.this.getCachedObjectImpl(gid);
				if(existingBean != null) {
					return existingBean;
				}
				Object persObject = this.beanFactory.create();
				// Must register right after creation
				// to prevent infinite loops due to
				// mutual object references.
				try {
					ObjectInfo oi = new ObjectInfo (objectID, persObject);
					Store.this.objectInfosByObject.put(persObject, oi);
					Store.this.objectInfosByID.put(new GlobalPersistentID(objectID, persObject.getClass()), oi);
				} catch(StoreException err) {
					throw new IllegalStateException(err);
				}
				return persObject;
			}
		}

		public String getIDField() {
			return this.storeInfo.getIdField();
		}

		public void setProperty(Object bean, String normalFieldName, Object fieldValue) {
			StoreInfo sinfo = this.storeInfo;
			PropertyDescriptor pd = sinfo.getPropertyDescriptorByNormalFieldName(Store.this, normalFieldName);
			if(pd != null) {
				Method writeMethod = pd.getWriteMethod();
				Object umvalue = null;
				try {
					umvalue = Store.this.unmarshallValue(sinfo, pd, fieldValue);
					writeMethod.invoke(bean, umvalue);
				} catch(Exception err) {
					throw new IllegalStateException("Unable to set property " + pd.getName() + " in class " + sinfo.getBeanClass().getName() + " with value " + umvalue + ", unmarshalled from " + fieldValue, err);
					
				}
			}
		}		
	}

	//----------- SimpleBeanFactory -----------
	
	private static class SimpleBeanFactory implements BeanFactory {
		private final Class beanClass;
		
		public SimpleBeanFactory(Class beanClass) {
			this.beanClass = beanClass;
		}
		
		public Object create() {
			try {
				return this.beanClass.newInstance();
			} catch(Exception err) {
				throw new IllegalStateException("Unable to instantiate class " + this.beanClass.getName() + ".", err);
			}
		}		
	}

	//------------- InsertionEntry 
	
	private static class InsertionEntry {
		private final Object insertedObject;
		private LinkedList<LateUpdate> updatesToExecute;
		private LinkedList<LateUpdate> updatesToModify;
		
		public InsertionEntry(Object insertedObject) {
			this.insertedObject = insertedObject;
		}
		
		public final void addLateUpdateToExecute(LateUpdate update) {
			LinkedList<LateUpdate> updates = this.updatesToExecute;
			if(updates == null) {
				updates = new LinkedList<LateUpdate>();
				this.updatesToExecute = updates;
			}
			updates.add(update);
		}
		
		public final void addLateUpdateToModify(LateUpdate update) {
			LinkedList<LateUpdate> updates = this.updatesToModify;
			if(updates == null) {
				updates = new LinkedList<LateUpdate>();
				this.updatesToModify = updates;
			}
			updates.add(update);
		}

		public final LinkedList<LateUpdate> getLateUpdatesToExecute() {
			return this.updatesToExecute;
		}
		
		public final LinkedList<LateUpdate> getLateUpdatesToModify() {
			return this.updatesToModify;
		}

		public final boolean contains(Object bean) {
			return bean == this.insertedObject;
		}
	}
	
	//------------------- LateUpdate
	
	private class LateUpdate {
		private final StoreInfo storeInfoToUpdate;
		private final PropertyDescriptor pd;
		private final String fieldToUpdate;
		private final boolean globalReference;
		private final long[] rawObjectIDs;
		private final Collection indexesToChange;
		private PersistentID idToUpdate;
		
		public LateUpdate(StoreInfo storeInfoToUpdate, PropertyDescriptor pd, String fieldToUpdate, boolean globalReference) {
			this.storeInfoToUpdate = storeInfoToUpdate;
			this.fieldToUpdate = fieldToUpdate;
			this.globalReference = globalReference;
			this.pd = pd;
			this.rawObjectIDs = null;
			this.indexesToChange = null;
		}
		
		public LateUpdate(final StoreInfo storeInfoToUpdate, PropertyDescriptor pd, final String fieldToUpdate, final boolean globalReference, final long[] rawObjectIDs, final Collection indexesToChange) {
			super();
			this.storeInfoToUpdate = storeInfoToUpdate;
			this.fieldToUpdate = fieldToUpdate;
			this.globalReference = globalReference;
			this.pd = pd;
			this.rawObjectIDs = rawObjectIDs;
			this.indexesToChange = indexesToChange;
		}

		public void setIdToUpdate(PersistentID id) {
			this.idToUpdate = id;
		}
		
		public void execute(Class insertedBeanClass, Long insertionID) throws StoreException {
			PersistentID idToUpdate = this.idToUpdate;
			if(idToUpdate == null) {
				throw new IllegalStateException("No ID to update in " + this +  ".");
			}
			PersistentID pid = new PersistentID(insertionID);
			Object fieldValue;
			long[] rawObjectIDs = this.rawObjectIDs;
			if(rawObjectIDs != null) {
				long[] newRawObjectIDs = new long[rawObjectIDs.length];
				System.arraycopy(rawObjectIDs, 0, newRawObjectIDs, 0, rawObjectIDs.length);
				Iterator i = this.indexesToChange.iterator();
				while(i.hasNext()) {
					int index = (Integer) i.next();
					newRawObjectIDs[index] = insertionID;
				}
				fieldValue = this.storeInfoToUpdate.marshallValue(this.pd, newRawObjectIDs);
			}
			else {
				fieldValue = this.globalReference ? new GlobalPersistentID(pid, insertedBeanClass).toString() : pid.longValue();
			}
			NameValueArray pairs = new NameValueArray(1);
			pairs.setPair(0, this.fieldToUpdate, fieldValue);
			Store.this.updatePairsImpl(this.storeInfoToUpdate, pairs, this.idToUpdate);
		}
		
		public String toString() {
			return "LateUpdate[class=" + this.storeInfoToUpdate.getBeanClass().getName() + ",fieldToUpdate=" + this.fieldToUpdate + ",globalReference=" + globalReference + ",idToUpdate=" + this.idToUpdate + ",rawObjectIDs=" + this.rawObjectIDs + ",indexesToChange=" + this.indexesToChange + "]";			
		}
	}
}




