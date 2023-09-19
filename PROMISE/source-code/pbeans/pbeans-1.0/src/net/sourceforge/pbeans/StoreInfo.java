package net.sourceforge.pbeans;

import net.sourceforge.pbeans.data.*;
import java.beans.*;

/**
 * For any class <code><i>Clazz</i></code> that implements Persistent,
 * you may define <code><i>Clazz</i>_StoreInfo</code> which implements
 * this interface. StoreInfo is used to customize
 * how the object is persisted, whether it has unique or non-unique
 * indexes, how values are marshalled and unmarshalled, etc.
 */
public interface StoreInfo {
    /**
     * Creates a new instance of the class described by this
     * StoreInfo instance.
     */
    public Persistent create (Store store) throws StoreException;
    /**
     * Gets a field descriptor for the given PropertyDescriptor.
     * May return null to indicate the property should not persist.
     */
    public FieldDescriptor getFieldDescriptor (Store store, PropertyDescriptor pd) throws StoreException;
    /**
     * Obtains the PropertyDescriptor for a property name.
     * Results of this method must be consistent with BeanInfo.
     */
    public PropertyDescriptor getPropertyDescriptor(String propertyName);
    /** 
     * Gets indexes that should be used in the underlying database table.
     */
    public Index[] getIndexes (Store store) throws StoreException;
    /**
     * Marshalls a property value except for any of type Persistent.
     * The resulting object is passed to JDBC.
     */
    public Object marshallValue (PropertyDescriptor pd, Object propertyValue) throws StoreException ;
    /**
     * Unarshalls a field value except for any of type Persistent.
     * A Persistent property is set with the resulting object value.
     */
    public Object unmarshallValue (PropertyDescriptor pd, Object fieldValue) throws StoreException;
}
