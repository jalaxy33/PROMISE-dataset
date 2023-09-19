package net.sourceforge.pbeans;

import java.beans.*;
import java.sql.*;
import java.util.*;
import net.sourceforge.pbeans.data.*;

/**
 * Abstract implementation of StoreInfo. In order to easily
 * implement StoreInfo, extend this class.
 */
public abstract class AbstractStoreInfo implements StoreInfo {
    private final Class clazz;
    private final Map propertyDescriptors = new HashMap();

    /**
     * Constucts an instance of AbstractStoreInfo.
     * @param clazz A class assignable to Persistent.
     */
    protected AbstractStoreInfo (Class clazz) {
	if (!Persistent.class.isAssignableFrom (clazz)) {
	    throw new IllegalArgumentException ("Class " + clazz.getName() + " does not implement " + Persistent.class.getName());
	}
	this.clazz = clazz;
	try {
	    BeanInfo binfo = Introspector.getBeanInfo (clazz);
	    PropertyDescriptor[] pds = binfo.getPropertyDescriptors();
	    for (int i = 0; i < pds.length; i++) {
		this.propertyDescriptors.put (pds[i].getName(), pds[i]);
	    }
	} catch (IntrospectionException ie) {
	    throw new IllegalArgumentException ("Unable to instrospect " + clazz);
	}
    }

    public PropertyDescriptor getPropertyDescriptor(String propertyName) {
	return (PropertyDescriptor) this.propertyDescriptors.get(propertyName);
    }

    /**
     * Creates an instance of the class passed to the constructor
     * of this AbstractStoreInfo instance. The class is expected
     * to have a default public constructor.
     */
    public Persistent create (Store store) throws StoreException {
	try {
	    return (Persistent) this.clazz.newInstance();
	} catch (InstantiationException ie) {
	    throw new StoreException ("Unable to instantiate class " + this.clazz.getName() + ". It should have a default public constructor, or it should have a _StoreInfo suffixed class which implements StoreInfo.");
	} catch (IllegalAccessException iae) {
	    throw new StoreException (iae);
	}
    }

    /**
     * Gets a field descriptor for a property if its <i>compile-time</i> type
     * is a primitive type, a boxed primitive type, String, java.util.Date, java.sql.Date,
     * java.sql.Timestamp, java.sql.Time, java.sql.Blob, byte[], or <code>Persistent</code>.
     * For any other types, this method returns <code>null</null>.
     */
    public FieldDescriptor getFieldDescriptor (Store store, PropertyDescriptor pd) {
	if (pd.getReadMethod() == null || pd.getWriteMethod() == null) {
	    return null;
	}
	Class propType = pd.getPropertyType();
	String fieldName = pd.getName();
	if (String.class == propType) {
	    return new FieldDescriptor (fieldName, Types.VARCHAR);
	}
	else if (Persistent.class.isAssignableFrom (propType)) {
	    return new FieldDescriptor (fieldName, Types.BIGINT);
	}
	else if (int.class == (propType)) {
	    return new FieldDescriptor (fieldName, Types.INTEGER);
	}
	else if (Integer.class == (propType)) {
	    return new FieldDescriptor (fieldName, Types.INTEGER);
	}
	else if (boolean.class == (propType)) {
	    return new FieldDescriptor (fieldName, Types.BOOLEAN);
	}
	else if (Boolean.class == (propType)) {
	    return new FieldDescriptor (fieldName, Types.BOOLEAN);
	}
	else if (long.class == (propType)) {
	    return new FieldDescriptor (fieldName, Types.BIGINT);
	}
	else if (Long.class == (propType)) {
	    return new FieldDescriptor (fieldName, Types.BIGINT);
	}
	else if (double.class == (propType)) {
	    return new FieldDescriptor (fieldName, Types.DOUBLE);
	}
	else if (Double.class == (propType)) {
	    return new FieldDescriptor (fieldName, Types.DOUBLE);
	}
	else if (float.class == (propType)) {
	    return new FieldDescriptor (fieldName, Types.FLOAT);
	}
	else if (Float.class == (propType)) {
	    return new FieldDescriptor (fieldName, Types.FLOAT);
	}
	else if (short.class == (propType)) {
	    return new FieldDescriptor (fieldName, Types.SMALLINT);
	}
	else if (Short.class == (propType)) {
	    return new FieldDescriptor (fieldName, Types.SMALLINT);
	}
	else if (char.class == (propType)) {
	    return new FieldDescriptor (fieldName, Types.CHAR);
	}
	else if (Character.class == (propType)) {
	    return new FieldDescriptor (fieldName, Types.CHAR);
	}
	else if (byte.class == (propType)) {
	    return new FieldDescriptor (fieldName, Types.SMALLINT);
	}
	else if (Byte.class == (propType)) {
	    return new FieldDescriptor (fieldName, Types.SMALLINT);
	}
	else if (java.sql.Timestamp.class.isAssignableFrom (propType)) {
	    return new FieldDescriptor (fieldName, Types.TIMESTAMP);
	}
	else if (java.sql.Date.class.isAssignableFrom (propType)) {
	    return new FieldDescriptor (fieldName, Types.DATE);
	}
	else if (java.sql.Time.class.isAssignableFrom (propType)) {
	    return new FieldDescriptor (fieldName, Types.TIME);
	}
	else if (java.util.Date.class == propType) {
	    return new FieldDescriptor (fieldName, Types.BIGINT);
	}
	else if (java.sql.Blob.class.isAssignableFrom (propType)) {
	    return new FieldDescriptor (fieldName, Types.BLOB);
	}
	else if (byte[].class == (propType)) {
	    return new FieldDescriptor (fieldName, Types.BLOB);
	}
	else {
	    // Ignoring... don´t know how to persist this
	    return null;
	}
    }

    public Object marshallValue (PropertyDescriptor pd, Object value) {
	Class propertyType = pd.getPropertyType();
	if (value == null) {
	    return null;
	}
	else if (java.util.Date.class == propertyType) {
	    return new Long(((java.util.Date) value).getTime());
	}
	else {
	    return value;
	}
    }

    public Object unmarshallValue (PropertyDescriptor pd, Object fieldValue) {
	Class propertyType = pd.getPropertyType();
	if (fieldValue == null) {
	    return null;
	}
	else if ((char.class == propertyType || java.lang.Character.class == propertyType) && fieldValue instanceof String) {
	    return new Character(((String) fieldValue).charAt(0));
	}
	else if (fieldValue instanceof String) {
	    return fieldValue;
	}
	else if (java.util.Date.class == propertyType) {
	    return new java.util.Date(((Long) fieldValue).longValue());
	}
	else if ((float.class == propertyType || java.lang.Float.class == propertyType) && fieldValue instanceof Double) {
	    return new Float((float) ((Double) fieldValue).doubleValue());
	}
	else if ((short.class == propertyType || java.lang.Short.class == propertyType) && fieldValue instanceof Integer) {
	    return new Short((short) ((Integer) fieldValue).intValue());
	}
	else if ((byte.class == propertyType || java.lang.Byte.class == propertyType) && fieldValue instanceof Integer) {
	    return new Byte((byte) ((Integer) fieldValue).intValue());
	}
	else {
	    return fieldValue;
	}
    }

    /**
     * Returns non-unique Index instances for all JavaBean properties
     * that are persisted
     * except those of type BLOB.
     */
    public Index[] getIndexes (Store store) throws StoreException {
        try {
            BeanInfo binfo = Introspector.getBeanInfo(this.clazz);
            PropertyDescriptor[] ppDescriptors = binfo.getPropertyDescriptors();
            Collection indexes = new LinkedList();
            for (int i = 0; i < ppDescriptors.length; i++) {
                FieldDescriptor fd = this.getFieldDescriptor(store, ppDescriptors[i]);
                if (fd != null && fd.getSqlType() != Types.BLOB) {
                    indexes.add (new Index (false, store.getPreferredFieldName(fd.getName())));
                }
            }
            return (Index[]) indexes.toArray (new Index[0]);        
        } catch (IntrospectionException ie) {
            throw new StoreException (ie);
        }
    }
}
