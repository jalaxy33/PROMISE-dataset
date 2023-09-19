package net.sourceforge.pbeans;

import java.sql.*;
import java.util.*;
import java.lang.ref.*;
import java.lang.reflect.*;
import java.beans.*;
import java.io.*;

import javax.sql.*;

import net.sourceforge.pbeans.data.*;

/**
 * <b>This is the main point of interaction with
 * persistent storage</b>.
 * <p>
 * Store should be instantiated only once
 * by a Java application.
 * <p>
 * Store deals with JavaBeans that implement
 * the Persistent tag interface. A Store will
 * first know about a Persistent object if the object
 * is registered for the first time, inserted,
 * or obtained from Store via one of its
 * <code>select</code> methods. If an object is
 * inserted, the same object reference may be later obtained
 * from Store by calling a <code>select</code> method.
 * <p>
 * In order to update changes made to a Persistent
 * object previously registered or inserted, the
 * <code>save</code> method should be invoked.
 */
public class Store {
    private static final Long ROOT_ID = new Long(0);
    private static final String FIELD_OBJECT_ID = "JP__OBJECTID";
    private static final String INDEX_OBJECT_ID = "JP__IDXOBJID";
    private static final Object[] EMPTY_PARAMETERS = new Object[0];

    private final Object objectsMonitor = new Object();
    private final Map objectInfosByID = new HashMap();
    private final Map objectInfosByIHC = new HashMap();
    private final Map storeInfoByClass = new HashMap();
    private final Map tableNames = new HashMap();
    private final Map fieldNames = new HashMap();
    private final Random RANDOM1 = new Random (System.currentTimeMillis());
    private final Random RANDOM2 = new Random (System.identityHashCode(this));
    private final ReferenceQueue REF_QUEUE = new ReferenceQueue();
    private final Database database;
    private final int maxTableNameLength;
    private final int maxColumnNameLength;
    private final PrintWriter logWriter;

    /**
     * Constructs a Store instance.
     * @param dataSource A DataSource instance, which may be obtained from
     *                   a JDBC 2.0 provider, or by instantiating 
     *                   net.sourceforge.pbeans.data.GenericDataSource.
     * @param dbf An implementation of DatabaseFactory. This may be an instance
     *            of DefaultDatabaseFactory.
     * @see net.sourceforge.pbeans.data.GenericDataSource
     * @throws StoreException
     */
    public Store (DataSource dataSource, DatabaseFactory dbf) throws StoreException {
	try {
            PrintWriter lw;
            try {
                lw = dataSource.getLogWriter();
            } catch (UnsupportedOperationException uo) {
                lw = null;
            }
            this.logWriter = lw;
	    this.database = dbf.getDatabase (dataSource);
	    this.maxTableNameLength = this.database.getMaxTableNameLength();
	    this.maxColumnNameLength = this.database.getMaxColumnNameLength();
	} catch (SQLException se) {
	    throw new StoreException (se);
	}
    }

    /**
     * Constructs a Store instance. Internally it creates an instance of
     * <code>DefaultDatabaseFactory</code> and invokes the constructor
     * that takes a <code>DatabaseFactory</code>.
     * @param dataSource A DataSource instance, which may be obtained from
     *                   a JDBC 2.0 provider, or by instantiating 
     *                   net.sourceforge.pbeans.data.GenericDataSource.
     * @see net.sourceforge.pbeans.data.GenericDataSource
     * @throws StoreException
     */
    public Store (DataSource dataSource) throws StoreException {
	this (dataSource, new DefaultDatabaseFactory());
    }

    /**
     * Gets the maximum column name length supported by the
     * underlying database.
     */
    public int getMaxColumnNameLength() {
	return this.maxColumnNameLength;
    }

    /** 
     * Gets the preferred database field name for a requested field name.
     * If the requested field name's length is less than or equal to the maximum
     * field name length supported by the underlying store, no changes
     * are made to the requested field name.
     */
    protected String getPreferredFieldName (String requestedFieldName) {
        synchronized (this.fieldNames) {
            String fieldName = (String) this.fieldNames.get(requestedFieldName);
            if (fieldName != null) {
                return fieldName;
            }
            fieldName = getShortColumnName (requestedFieldName, "F");
            this.fieldNames.put (requestedFieldName, fieldName);
            return fieldName;
        }
    }

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
	return getPreferredFieldName (fd.getName());
    }

    private String getShortColumnName (String name, String prefix) {
        int maxColumnNameLength = this.getMaxColumnNameLength();
        String tName = name;
        int totalLength = tName.length();
        if (totalLength <= maxColumnNameLength) {
            return tName;
        }
        // Note: Don´t change what the fixedHash is obtained from.
        int hash = Math.abs (this.fixedHash (name));
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
     * another class is provided with the same name as that of <code>clazz</code> but
     * suffixed with "_StoreInfo" (similar to
     * BeanInfo for JavaBeans.) Implementing StoreInfo gives more
     * control over which properties are persistent, indexes, etc.
     * @throws StoreException
     */
    public StoreInfo getStoreInfo (Class clazz) throws StoreException {
	synchronized (storeInfoByClass) {
	    StoreInfo sinfo = (StoreInfo) storeInfoByClass.get (clazz);
	    if (sinfo != null) {
		return sinfo;
	    }
   	    sinfo = createStoreInfo (clazz);
	    storeInfoByClass.put (clazz, sinfo);

            // Now make sure underlying table exists

	    String tableName = getTableName (clazz);
	    try {
		BeanInfo binfo = Introspector.getBeanInfo(clazz);
		PropertyDescriptor[] ppDescriptors = binfo.getPropertyDescriptors();
		Collection fieldDescriptors = new LinkedList();
		for (int i = 0; i < ppDescriptors.length; i++) {
		    FieldDescriptor fd = sinfo.getFieldDescriptor(this,ppDescriptors[i]);
		    if (fd != null) {
                        String name = fd.getName();
                        String actualName = getPreferredFieldName (name);
                        if (!name.equals (actualName)) {
                            fd = new FieldDescriptor (actualName, fd);
                        }
			fieldDescriptors.add (fd);
		    }
		    else {
			String pname = ppDescriptors[i].getName();
			if (!"class".equals (pname)) {
			    warn ("getStoreInfo(): Won't persist " + clazz.getName() + "." + pname + ".");
			}
		    }
		}
		Collection indexDescriptors = new LinkedList();
                Index[] indexes = sinfo.getIndexes (this);
                for (int i = 0; i < indexes.length; i++) {
                    Index index = indexes[i];
		    String indexName = getIndexName(index);
		    try {
			indexDescriptors.add (new IndexDescriptor (indexName, index.isUnique(), getFieldNames (sinfo, index.getPropertyNames())));
		    }
		    catch (StoreException se) {
			throw new StoreException ("Unable to add index named " + indexName + " to table " + tableName + " because: " + se.getMessage(), se);
		    }
                }
		FieldDescriptor oidDescriptor = new FieldDescriptor (FIELD_OBJECT_ID, Types.BIGINT);
		IndexDescriptor pkDescriptor = new IndexDescriptor (INDEX_OBJECT_ID, true, FIELD_OBJECT_ID);
		indexDescriptors.add (pkDescriptor);
                try {
		    
                    database.ensureTableExists (tableName, oidDescriptor, fieldDescriptors, indexDescriptors);
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
        return getShortColumnName (buf.toString(), "I");
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

    private StoreInfo createStoreInfo (Class clazz) throws StoreException {
	String checkName = clazz.getName() + "_StoreInfo";
	try {
	    Class sinfoClazz = Class.forName (checkName);
	    return (StoreInfo) sinfoClazz.newInstance();
	} catch (ClassNotFoundException cnfe) {
	    return new DefaultStoreInfo (clazz);
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
    public void put (String name, Persistent obj) throws StoreException {
	PersistentMap map = getRootMap();
	map.putObject (name, obj);
    }

    /** 
     * Gets a persistent object previously associated with a name
     * using the put() method.
     * @see net.sourceforge.pbeans.Store#put
     */
    public Persistent get (String name) throws StoreException {
	checkWeakReferences();
	PersistentMap map = getRootMap();
	return map.getObject (name);	
    }

    /**
     * Removes an association of a name with a persistent object.
     * @throws StoreException
     * @see net.sourceforge.pbeans.Store#put
     * @see net.sourceforge.pbeans.Store#get
     */
    public void remove (String name) throws StoreException {
	PersistentMap map = getRootMap();
	map.removeObject (name);
    }
	
    private void checkWeakReferences() {
	for (;;) {
	    ObjectInfo wr = (ObjectInfo) this.REF_QUEUE.poll();
	    if (wr == null) {
		break;
	    }
	    Long objectID = wr.getObjectID();
	    Integer identityHashCode = wr.getIdentityHashCode();
	    synchronized (objectsMonitor) {
		this.objectInfosByID.remove (objectID);
		this.objectInfosByIHC.remove (identityHashCode);
	    }
	}
    }

    /**
     * Gets the PersistentMap instance used by the get() and put() methods
     * in this class.
     * @throws StoreException
     */
    protected PersistentMap getRootMap() throws StoreException {
	PersistentMap rootMap = (PersistentMap) getObject (ROOT_ID);
	if (rootMap == null) {
	    synchronized (objectsMonitor) {
		rootMap = (PersistentMap) getObjectImpl (ROOT_ID);
		if (rootMap == null) {
		    rootMap = new PersistentMap(this);
		    // Ensure rootMap is a known object with ROOT_ID.
		    ensureKnownImpl (rootMap, ROOT_ID, true);
		}
	    }
	}
	return rootMap;
    }

    /**
     * Gets a persistent object given its unique ID.
     * The object may already be available in memory or
     * it may be retrieved from storage.
     * @throws StoreException
     */
    private Persistent getObject (Long objectID) throws StoreException {
	Persistent obj = (Persistent) getCachedObject (objectID);
	if (obj == null) {
	    obj = (Persistent) getStoredObject (objectID);
	}
	return obj;
    }

    private Persistent getObjectImpl (Long objectID) throws StoreException {
	Persistent obj = (Persistent) getCachedObjectImpl (objectID);
	if (obj == null) {
	    obj = (Persistent) getStoredObject (objectID);
	}
	return obj;
    }	

    /**
     * Gets a persistent object from main memory, if available.
     */
    private Persistent getCachedObject (Long objectID) {
	synchronized (objectsMonitor) {
	    ObjectInfo oi = (ObjectInfo) this.objectInfosByID.get (objectID);
	    if (oi == null) {
		return null;
	    }
	    return oi.getObject();
	}
    }

    private Persistent getCachedObjectImpl (Long objectID) {
	ObjectInfo oi = (ObjectInfo) this.objectInfosByID.get (objectID);
	if (oi == null) {
	    return null;
	}
	return oi.getObject();
    }

    /**
     * Loads a persistent object from storage.
     */
    private Persistent getStoredObject (Long objectID) throws StoreException {
	Map fields = new HashMap(2);
	// Note: This is "objectID" and not FIELD_OBJECT_ID because
	// we are looking on a property of ObjectClass named "objectID"
	fields.put ("objectID", objectID);
	ObjectClass oc = (ObjectClass) this.selectSingle (ObjectClass.class, fields);
	if (oc == null) {
	    return null;
	}
	String className = oc.getClassName();
	if (className == null) {
	    throw new StoreException ("Class was null for objectID=" + objectID);
	}
	return buildOrPopulateStoredObject (objectID, className);
    }

    /**
     * Gets the table name used to persist instances of a given persistent class name.
     * @throws StoreException
     */
    protected String getTableName (String className) {
        synchronized (this.tableNames) {
            String tableName = (String) this.tableNames.get(className);
            if (tableName != null) {
                return tableName;
            }
            String prefix = "T";
            String tName = className.replace('.','_').replace('$','_');
            int totalLength = prefix.length() + tName.length();
            if (totalLength <= this.maxTableNameLength) {
                return prefix + tName;
            }
            // Note: Don´t change what the fixedHash is obtained from.
            int hash = Math.abs (fixedHash (className));
            String hashStr = String.valueOf(hash);
            int canUseLength = this.maxTableNameLength - prefix.length() - hashStr.length();
            tableName =
                prefix + 
                hashStr + 
                tName.substring(
                                tName.length() - canUseLength, 
                                tName.length());
            this.tableNames.put (className, tableName);
            return tableName;
        }
    }

    static int fixedHash (String text) {
	// Note: Never change the implementation of this method.
	// It should remain unchanged across JVM versions, etc.
	byte[] hashBytes = new byte[4];
	int len = text.length();
	for (int i = 0; i < len; i++) {
	    int hi = i % 4;
	    hashBytes[hi] ^= (byte) text.charAt(i);
	}
	return hashBytes[0] + hashBytes[1] << 8 + hashBytes[2] << 16 + hashBytes[3] << 24;
    }
    
    /**
     * Gets the table name used for a given class.
     * @throws StoreException
     */
    protected String getTableName (Class clazz) {
	return getTableName (clazz.getName());
    }

    private Persistent buildOrPopulateStoredObject (Long objectID, String className) throws StoreException {
	try {
	    Class c = Class.forName (className);
	    String table = getTableName (className);
	    Map record = getRecord (table, FIELD_OBJECT_ID + "=" + objectID);
	    if (record == null) {
		return null;
	    }
	    return buildOrPopulateBean (objectID, c, record);
	} catch (ClassNotFoundException cnfe) {
	    throw new StoreException (cnfe);
	} catch (SQLException se) {
	    throw new StoreException (se);
	}
    }

    /**
     * Repopulates Persistent object properties with stored data.
     * It should only be used if changes have been made to the
     * database outside of this API.
     * @throws StoreException
     */
    public void reload (Persistent persObject) throws StoreException {
	ObjectInfo oi = getObjectInfo (persObject, false);
	this.buildOrPopulateStoredObject (oi.getObjectID(), oi.getClassName());
    }

    private Persistent buildOrPopulateBean (Long objectID, Class persClass, Map record) throws StoreException {
	Persistent persObject;
	boolean populate = false;
	synchronized (objectsMonitor) {
	    persObject = (Persistent) getCachedObjectImpl (objectID);
	    if (persObject == null) {
		persObject = (Persistent) buildBean (persClass, record);
		ensureKnownImpl (persObject, objectID, false);
	    }
	    else {
		populate = true;
	    }
	}
	if (populate) {
	    populateBean (persClass, record, persObject);
	}
	return persObject;
    }

    private Persistent buildBean (Class c, Map dbRecord) throws StoreException {
	StoreInfo sinfo = getStoreInfo (c);
	Persistent persObject = sinfo.create (this);
	populateBean (c, dbRecord, persObject);
	return persObject;
    }

    /**
     * Populates a Persistent object with
     * data obtained from a database query.
     * @param persClass A class assignable to Persistent.
     * @param dbRecord A map of values obtained from JDBC.
     * @param bean An instance of <code>persClass</code>
     * @throws StoreException
     */
    protected void populateBean (Class persClass, Map dbRecord, Persistent bean) throws StoreException {
	try {
	    StoreInfo sinfo = getStoreInfo (persClass);
	    BeanInfo binfo = Introspector.getBeanInfo (persClass);
	    PropertyDescriptor[] pds = binfo.getPropertyDescriptors();
	    for (int i = 0; i < pds.length; i++) {
		PropertyDescriptor pd = pds[i];	
		String propName = pd.getName();
		Object dbValue = dbRecord.get (propName);
		if (isPersistent (sinfo, pd)) {
		    Object propValue = unmarshallValue (sinfo, pd, dbValue);
		    Method setter = pd.getWriteMethod();
		    try {
			setter.invoke (bean, new Object[] { propValue });
		    } catch (IllegalArgumentException iae) {
			if (propValue == null && pd.getPropertyType().isPrimitive()) {
			    warn ("populateBean(): Not setting property " + propName + " with null value because its type is primitive.");
			}
			else {
			    throw new IllegalArgumentException ("Unable to set property " + propName + " with object of type " + (propValue == null ? "<null>" : propValue.getClass().getName()) + " obtained from StoreInfo.unmarshallValue(). " + iae.getMessage());
			}
		    }
		}
	    }
	} catch (IntrospectionException ie) {
	    throw new StoreException (ie);
	} catch (IllegalAccessException iae) {
	    throw new StoreException (iae);
	} catch (InvocationTargetException ite) {
	    throw new StoreException (ite);
	}
    }

    private boolean isPersistent (StoreInfo sinfo, PropertyDescriptor pd) throws StoreException {
	return sinfo.getFieldDescriptor(this,pd) != null;
    }

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
    public void save (Persistent persObject) throws StoreException {
	checkWeakReferences();
	ObjectInfo oi = getObjectInfo (persObject);
	oi.save();
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
    public void insert (Persistent persObject) throws StoreException {
	checkWeakReferences();
	ObjectInfo oi = getObjectInfo (persObject, false);
	oi.insert();
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
    public void register (Persistent persObject) throws StoreException {
	checkWeakReferences();
	getObjectInfo (persObject);
    }

    /**
     * Removes a persistent object from storage.
     * Any subsequent attempts to save the object will fail,
     * unless <code>insert</code> is invoked first.
     * @throws StoreException
     */
    public void delete (Persistent persObject) throws StoreException {
	ObjectInfo oi = getObjectInfo (persObject);
	oi.delete();	
    }

    private Long createObjectID() {
	return new Long(Math.abs(RANDOM1.nextLong() ^ RANDOM2.nextLong()));
    }

    private Long getObjectID (Persistent persObject) throws StoreException {
	ObjectInfo oi = getObjectInfo (persObject);
	return oi.getObjectID();
    }

    private ObjectInfo getObjectInfo (Persistent persObject) throws StoreException {
	return getObjectInfo (persObject, true);
    }

    private ObjectInfo getObjectInfo (Persistent persObject, boolean doInsert) throws StoreException {
	// Should not be invoked if persObject is not user-provided but
        // built from a database record.

	Integer ihc = new Integer (System.identityHashCode (persObject));
	ObjectInfo oi;
	boolean isNew = false;
	synchronized (objectsMonitor) {
	    oi = (ObjectInfo) this.objectInfosByIHC.get (ihc);
	    if (oi == null) {
		// Any instance of Persistent that is not new
		// and not read from the database
		// would already be known by Store.
		Long objectID = createObjectID();
		oi = new ObjectInfo (objectID, persObject);
		isNew = true;
		this.objectInfosByIHC.put (ihc, oi);
		this.objectInfosByID.put (objectID, oi);
	    }
	}
	if (isNew && doInsert) {
	    oi.insert();
	}
	return oi;
    }

    private ObjectInfo getExistingObjectInfo (Persistent persObject) {
	Integer ihc = new Integer (System.identityHashCode (persObject));
	synchronized (objectsMonitor) {
	    return (ObjectInfo) this.objectInfosByIHC.get (ihc);
	}
    }

    private void ensureKnownImpl (Persistent persObject, Long objectID, boolean doInsert) throws StoreException {
	Integer ihc = new Integer(System.identityHashCode (persObject));
	ObjectInfo oi;
	oi = (ObjectInfo) this.objectInfosByIHC.get (ihc);
	if (oi == null) {
	    oi = new ObjectInfo (objectID, persObject);
	    if (doInsert) {
		// Don't insert when read from database, for example.
		oi.insert();
	    }
	    this.objectInfosByIHC.put (ihc, oi);
	    this.objectInfosByID.put (objectID, oi);
	}
	else if (!objectID.equals (oi.getObjectID())) {
	    throw new StoreException ("ensureKnown() failed to ensure object " + persObject + " has ID " + objectID);
	}
    }

    private Map getMarshalledValues (Object obj, Long objectID) throws StoreException {    
	Map map = getBeanValues (obj);
        map.put (FIELD_OBJECT_ID, objectID);
	return map;
    }    

    private Map marshallValues (Class clazz, StoreInfo sinfo, Map values) throws StoreException {
	Map mValues = new HashMap();
	try {
	    BeanInfo beanInfo = Introspector.getBeanInfo (clazz);
	    PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
	    for (int i = 0; i < pds.length; i++) {
		PropertyDescriptor pd = pds[i];
		FieldDescriptor fd = sinfo.getFieldDescriptor(this,pd);
		if (fd != null) {
		    String propName = pd.getName();
		    if (values.containsKey(propName)) {
			Object value = values.get(propName);
			String fieldName = getPreferredFieldName (fd.getName());
			if (value != null) {
			    mValues.put (fieldName, marshallValue (sinfo, pd, value));
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

    private Object marshallValue (StoreInfo sinfo, PropertyDescriptor pd, Object value) throws StoreException {
	if (value == null) {
	    return null;
	}
	else {
	    Class propType = pd.getPropertyType();
	    if (Persistent.class.isAssignableFrom (propType)) {
		Long objectID = getObjectID ((Persistent) value);
		return objectID;
	    }
	    else {
		return sinfo.marshallValue (pd, value);
	    }	
	}    
    }

    private Object unmarshallValue (StoreInfo sinfo, PropertyDescriptor pd, Object value) throws StoreException {
	if (value == null) {
	    return null;
	}
	else {
	    Class propType = pd.getPropertyType();
	    if (Persistent.class.isAssignableFrom (propType)) {
		Long objectID = (Long) value;
		return getObject (objectID);		
	    }
	    else {
		return sinfo.unmarshallValue (pd, value);
	    }
	}
    }

    private Map getBeanValues (Object bean) throws StoreException {
	try {
	    StoreInfo sinfo = getStoreInfo(bean.getClass());
	    BeanInfo beanInfo = Introspector.getBeanInfo (bean.getClass());
	    PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
	    Map values = new HashMap(pds.length * 2);
	    for (int i = 0; i < pds.length; i++) {
		PropertyDescriptor pd = pds[i];
		FieldDescriptor fd = sinfo.getFieldDescriptor(this,pd);
		if (fd != null) {
		    String propName = pd.getName();
		    Method readMethod = pd.getReadMethod();
		    Object value;
		    try {
			value = readMethod.invoke (bean, new Object[0]);
		    }
		    catch (IllegalAccessException iae) {
			throw new StoreException ("Store has no access to property " + propName + " in class " + bean.getClass().getName(), iae);
		    }
		    values.put (getPreferredFieldName(fd.getName()), marshallValue (sinfo, pd, value));
		}
	    }
	    return values;
	} catch (IntrospectionException ie) {
	    throw new StoreException (ie);
	} catch (InvocationTargetException ite) {
	    throw new StoreException (ite);
	}
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
	PrintWriter writer = this.logWriter;
	if (writer != null) {
	    writer.println (new java.util.Date() + ": WARN: " + text);
	    writer.flush();
	}
    }

    //------------------------ Finders & Updaters -------------------
    
    /**
     * Gets an iterator of objects of the given class which have a "foreign key"
     * property value equal to the <code>container</code> given. 
     * The foreign key property <code>fkProperty</code> type must be assignable to Persistent.
     * This is useful
     * in defining one-to-many relationships, and getting all children of a
     * container.
     * @throws StoreException
     */
    public ResultsIterator selectAggregation (final Class inClazz, String fkProperty, Persistent container) throws StoreException {
	Map props = new HashMap(2);
	props.put (fkProperty, container);
	return select (inClazz, props);
    }

    /**
     * Gets an iterator of all stored instances of the given class.
     */
    public ResultsIterator select (final Class inClazz) throws StoreException {
	return select (inClazz, null);
    }

    /**
     * Finds a stored instance with the property value given.
     * @return An instance of <code>clazz</code> or <code>null</code> 
     *         if an object is not found.
     * @throws StoreException
     */
    public Persistent selectSingle (final Class clazz, String propertyName, Object propertyValue) throws StoreException {
	Map values = new HashMap(2);
	values.put (propertyName, propertyValue);
	return selectSingle (clazz, values);				      
    }

    /**
     * Finds a stored instance matching the property values given.
     * @param clazz Only instances of this class are searched.
     * @param values A map of property names to values.
     * @return An instance of <code>clazz</code> or <code>null</code> 
     *         if an object is not found.
     * @throws StoreException
     */
    public Persistent selectSingle (final Class clazz, Map values) throws StoreException {
	ResultsIterator i = select (clazz, values);
	try {
	    if (i.hasNext()) {
		return (Persistent) i.next();
	    }
	} finally {
	    try {
		i.close();
	    }
	    catch (SQLException se) {
		throw new StoreException (se);
	    }
	}
	return null;
    }

    /**
     * Finds all stored instances matching the property values given.
     * @param clazz Only instances of this class are searched.
     * @param values A map of property names to values.
     * @return An iterator of Persistent objects.
     * @throws StoreException
     */
    public ResultsIterator select (final Class clazz, Map values) throws StoreException {
	return select (clazz, values, null, false, null);
    }

    /**
     * Finds stored instances matching the property values given.
     * @param clazz Only instances of this class are searched.
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
    public ResultsIterator select (final Class clazz, Map values, String orderByProperty, boolean descending, Integer limit) throws StoreException {
	StoreInfo sinfo = getStoreInfo (clazz);
	Map mValues = values == null ? null : marshallValues (clazz, sinfo, values);
        if (mValues != null && mValues.size() == 0) {
            throw new StoreException ("No marshalled condition values found from map provided. Make sure property names used in query exist, are not capitalized, and persistent."); 
        }
	String orderByField = orderByProperty == null ? null : getFieldName (sinfo, orderByProperty);
	String tableName = getTableName (clazz);
	try {
	    final ResultsIterator dbIterator = getRecords (tableName, mValues, orderByField, descending, limit);
	    return new ResultsIterator() {
		    public boolean hasNext() {
			return dbIterator.hasNext();
		    }
		
		    public Object next() {
			Map record = (Map) dbIterator.next();
			Long objectID = (Long) record.get(FIELD_OBJECT_ID);
			try {
			    return buildOrPopulateBean (objectID, clazz, record);
			} catch (StoreException se) {
			    throw new NoSuchElementException ("Element unavailable due to StoreException=" + se);
			}
		    }

		    public void remove() {
			throw new UnsupportedOperationException();
		    }

		    public void close() throws SQLException {
			dbIterator.close();
		    }
		};
	} catch (SQLException se) {
	    throw new StoreException (se);
	}
    }

    /**
     * Deletes all database records that match the given criteria.
     * Once the record corresponding to a Persistent object is deleted,
     * subsequent invocations to <code>save</code> will fail.
     * @throws StoreException
     */
    public boolean delete (Class clazz, Map values) throws StoreException {
	StoreInfo sinfo = getStoreInfo(clazz);
	String tableName = getTableName (clazz);
	Map mValues = marshallValues (clazz, sinfo, values);
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
     * Requests a lock for a record with a given column value.
     * This method may block if another thread is holding a lock
     * with the same parameters. Call <code>relinquishLock</code> at the end
     * of the critical section, preferably in a finally block.
     * @throws StoreException
     */
    public Object requestLock (Class clazz, String propertyName, Object value) throws StoreException, InterruptedException {
	StoreInfo sinfo = getStoreInfo (clazz);
	PropertyDescriptor pd = sinfo.getPropertyDescriptor(propertyName);
	if (pd == null) {
	    throw new IllegalArgumentException ("Property not found " + propertyName);
	}
	FieldDescriptor fd = sinfo.getFieldDescriptor(this,pd);
	if (fd == null) {
	    throw new IllegalArgumentException ("Property " + propertyName + " is transient.");
	}
	Object mValue = marshallValue (sinfo, pd, value);
	try {
	    return database.requestLock (getTableName (clazz), getPreferredFieldName(fd.getName()), mValue);
	} catch (SQLException se) {
	    throw new StoreException (se);
	}
    }

    /**
     * Must be invoked to give up a lock previously obtained
     * by calling <code>requestLock</code>.
     * @throws StoreException
     */
    public void relinquishLock (Object lock) throws StoreException {
	try {
	    database.relinquishLock (lock);
	} catch (SQLException se) {
	    throw new StoreException (se);
	}
    }

    //---------------- Database Methods ----------------------------

    private Map getRecord (String tableName, String condition) throws SQLException {
	ResultsIterator i = getRecords (tableName, condition);
	try {
	    if (i.hasNext()) {
		return (Map) i.next();
	    }
	    else {
		return null;
	    }
	}
	finally {
	    i.close();
	}
    }

    private ResultsIterator getRecords (String tableName, String condition) throws SQLException {
	return this.database.query ("SELECT * FROM " + tableName + " WHERE " + condition, EMPTY_PARAMETERS);
    }

    private ResultsIterator getRecords (String tableName, Map whereFieldValues, String orderBy, boolean descending, Integer limit) throws SQLException {
	String cond = whereFieldValues == null ? null : getNameValueSequence (whereFieldValues, " and ", true);
	Object[] params = whereFieldValues == null ? EMPTY_PARAMETERS : getParameters(whereFieldValues);
        String queryString = "SELECT * FROM " + tableName + (cond == null ? "" : " WHERE " + cond) + (orderBy == null ? "" : " ORDER BY " + orderBy) + (descending ? " DESC " : "") + (limit == null ? "" : " LIMIT " + limit);
        try {
            return this.database.query (queryString, params);
        } catch (SQLException se) {
            SQLException newSe = new SQLException ("Unable to execute query: " + queryString + ". Parameters: " + params.length + ". Reason: " + se.getMessage(), se.getSQLState(), se.getErrorCode());
            newSe.setNextException (se);
            throw newSe;
        }
    }

    private boolean deleteRecords (String tableName, Map whereFieldValues)  throws SQLException {
	String cond = getNameValueSequence (whereFieldValues, " and ", true);
	Object[] params = getParameters (whereFieldValues);
	return this.database.update ("delete FROM " + tableName + " WHERE " + cond, params) > 0;
    }

    private boolean insertRecord (String tableName, Map fieldValues) throws SQLException  {
	String names = getNameSequence (fieldValues, ",");
	String values = getPlaceholderSequence (fieldValues.size(), ",");
	Object[] params = getParameters(fieldValues);
	return this.database.update ("insert into " + tableName + " (" + names + ") values (" + values + ")", params) > 0;
    }

    private boolean updateRecords (String tableName, Map fieldValues, String condition) throws SQLException {
	String setters = getNameValueSequence (fieldValues, ",", false);
	Object[] params = getParameters(fieldValues);
	return this.database.update ("UPDATE " + tableName + " set " + setters + " WHERE " + condition, params) > 0;
    }

    private String getNameValueSequence (Map fieldValues, String separator, boolean isQuery) {
	StringBuffer condBuf = new StringBuffer();
	Iterator i = fieldValues.entrySet().iterator();
	while (i.hasNext()) {
	    Map.Entry entry = (Map.Entry) i.next();
	    String fieldName = (String) entry.getKey();
	    condBuf.append (fieldName);
	    if (isQuery && entry.getValue() == null) {
		condBuf.append (" IS ");
	    }
	    else {
		condBuf.append ('=');
	    }
	    condBuf.append ('?');
	    if (i.hasNext()) {
		condBuf.append(separator);
	    }
	}
	return condBuf.toString();
    }

    private String getNameSequence (Map fieldValues, String separator) {
	StringBuffer condBuf = new StringBuffer();
	Iterator i = fieldValues.entrySet().iterator();
	while (i.hasNext()) {
	    Map.Entry entry = (Map.Entry) i.next();
	    String fieldName = (String) entry.getKey();
	    condBuf.append (fieldName);
	    if (i.hasNext()) {
		condBuf.append(separator);
	    }
	}
	return condBuf.toString();
    }

    private String getPlaceholderSequence (int count, String separator) {
	StringBuffer condBuf = new StringBuffer();
	for (int i = 0; i < count; i++) {
	    condBuf.append ('?');
	    if (i + 1 < count) {
		condBuf.append(separator);
	    }
	}
	return condBuf.toString();
    }

    private Object[] getParameters (Map fieldValues) throws SQLException {
	Iterator i = fieldValues.entrySet().iterator();
	Collection params = new LinkedList();
	while (i.hasNext()) {
	    Map.Entry entry = (Map.Entry) i.next();
	    Object value = entry.getValue();
	    params.add (value);
	}
	return params.toArray();
    }

    //------------------------ ObjectInfo ---------------------------

    class ObjectInfo extends WeakReference {
	private final Long objectID;
	private final String primaryTableName;
	private final String className;
	private final Integer identityHashCode;
	private final Object deleteMonitor;
	private final Object saveMonitor;
	private volatile Map lastValues = null;
	// objectClass will be null when the persistent
	// object is of type ObjectClass
	private volatile ObjectClass objectClass = null;
	
	ObjectInfo (Long objectID, Persistent persObject) {
	    super (persObject, REF_QUEUE);
	    if (persObject == null) {
		throw new IllegalArgumentException ("persObject is null");
	    }
	    this.objectID = objectID;
	    this.className = persObject.getClass().getName();
	    this.primaryTableName = getTableName (persObject.getClass().getName());
	    this.identityHashCode = new Integer (System.identityHashCode (persObject));
	    this.deleteMonitor = this;
	    this.saveMonitor = this.objectID;
	}

	String getClassName() {
	    return this.className;
	}

	Long getObjectID() {
	    return this.objectID;
	}

	Persistent getObject() {
	    return (Persistent) this.get();
	}

	Integer getIdentityHashCode() {
	    return this.identityHashCode;
	}

	void insert() throws StoreException {
	    Persistent bean = getObject();
	    if (bean == null) {
		throw new StoreException ("Unexpected: Object already garbage collected on insert().");
	    }

	    // Insert record in ObjectClass table.

	    if (!(bean instanceof ObjectClass)) {
		// This condition prevents an infinite recursion.
		// It works because lookups of ObjectClass instances
		// are never done by ObjectClass instance ID.
		// (Given an ObjectClass instance ID,
		// Store is unable determine its class, but that's ok.)
		ObjectClass oc = new ObjectClass (this.objectID, this.className);
		synchronized (deleteMonitor) {
		    this.objectClass = oc;
		}
		register (oc);
	    }
	    // Table will be created when getStoreInfo is called
	    // inevitably.
	    // Insert record in corresponding class table.
	    Map newValues = getMarshalledValues (bean, this.objectID);
	    try {
		synchronized (saveMonitor) {
		    insertRecord (primaryTableName, newValues);
		    this.lastValues = newValues;
		}
	    } catch (SQLException se) {
		if (database.isDuplicateEntryError(se)) {
		    throw new DuplicateEntryException ("Record on " + className + " already exists", se);
		}
		else {
		    throw new StoreException (se);
		}
	    }
	}

	void save() throws StoreException {
	    Persistent bean = this.getObject();
	    // Note: getMarshalledValues() converts Persistent objects
	    // to objectIDs, which in turn ensures referred objects
	    // are known.
	    Map newValues = getMarshalledValues (bean, this.objectID);
	    try {
		synchronized (saveMonitor) {
		    Map oldValues = this.lastValues;
		    if (!equalValues (newValues, oldValues)) {
			updateRecords (primaryTableName, newValues, FIELD_OBJECT_ID + "=" + objectID);
			this.lastValues = newValues;
		    }
		}
	    } catch (SQLException se) {
		throw new StoreException (se);
	    }
	}

	void delete() throws StoreException {
	    Persistent bean = this.getObject();
	    Map values = new HashMap(2);
	    values.put (FIELD_OBJECT_ID, this.objectID);
	    try {
		deleteRecords (primaryTableName, values);
		ObjectClass oc;
		synchronized (deleteMonitor) {
		    oc = this.objectClass;
		    this.objectClass = null;
		}
		if (oc != null) {
		   Store.this.delete (oc);
		}
	    } catch (SQLException se) {
		throw new StoreException (se);
	    }
	}
    }


}




