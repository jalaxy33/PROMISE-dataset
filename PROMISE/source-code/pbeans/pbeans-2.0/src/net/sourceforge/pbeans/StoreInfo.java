package net.sourceforge.pbeans;

import net.sourceforge.pbeans.data.*;
import java.beans.*;

/**
 * For any class <code><i>Clazz</i></code> that implements Persistent,
 * you may define <code><i>Clazz</i>_StoreInfo</code> which implements
 * this interface (<b>but note that this technique is deprecated in 
 * favor of {@link net.sourceforge.pbeans.annotations.PersistentClass} annotations</b>).
 * <p>
 * StoreInfo is used to customize
 * how the object is persisted, whether it has unique or non-unique
 * indexes, how values are marshalled and unmarshalled, etc.
 * <p>
 * Instead of directly implementing this interface, 
 * extending {@link AbstractStoreInfo AbstractStoreInfo} is recommended.
 * <h3>Unique Indexes</h3>
 * Unique indexes may be defined by implementing the <code>getIndexes</code>
 * method, for example, as follows:
 * <code>
 * <pre>
 *
 *     public class User_StoreInfo extends AbstractStoreInfo {
 *         public User_StoreInfo() {
 *             super (User.class);
 *         }
 *
 *         public Index[] getIndexes (Store store) {
 *             return new Index[] {
 *                 new Index (true, "userName")
 *             };
 *         }
 *     }
 *
 * </pre>
 * </code>
 */
public interface StoreInfo {
	public String getTableName(int maxLength);
	public Class getBeanClass();
	public String getIdField();
    public boolean isDeleteFields();
	public boolean isAutoIncrementRequested();
	public PropertyDescriptor[] getPropertyDescriptors();

	
    /**
     * Creates a new instance of the class described by this
     * StoreInfo instance.
     */
    public Object create (Store store) throws StoreException;
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
    
    public PropertyDescriptor getPropertyDescriptorByNormalFieldName(Store store, String fieldName);
    
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

    /**
     * Gets a class loader which may be used to find the underlying
     * StoreInfo class.
     */
    public ClassLoader getClassLoader();

    /**
     * Gets a boolean value indicating whether
     * table creation and alteration is user-managed
     * as opposed to automatic.
     */
    public boolean isUserManaged();    
}
