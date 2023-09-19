package net.sourceforge.pbeans;

/**
 * <a href="http://java.sun.com/products/javabeans/docs/spec.html" target="_blank">JavaBeans</a> 
 * tagged with this interface can be used
 * with {@link Store Store}.
 * <p>
 * <h4>Getters and Setters</h4>
 * Classes that implement this interface define properties
 * with getters and setters. For example, class <code>User</code>
 * may define a property named <code>userName</code>
 * as follows:
 * <code>
 * <pre>
 *    public class User implements Persistent {
 *        private String uname;
 *
 *        public String getUserName() {
 *            return this.uname;
 *        }
 *
 *        public void setUserName(String u) {
 *            this.uname = u;
 *        }
 *    }
 * </pre>
 * </code>
 * <h4>Persistent Properties</h4>
 * By default, properties of certain <i>compile-time</i> types are considered
 * persistent. These types are PersistentID,
 * String, java.util.Date, java.sql.Date, java.sql.Timestamp,
 * primitive types, and boxed primitive types (such as Integer,
 * Long, etc.) Additionally, properties with a compile-time type
 * assignable to <code>Persistent</code> also persist by default.
 * <h4>Customization</h4>
 * You may define a class that accompanies the Persistent
 * JavaBean in order to define which properties persist,
 * how they are mapped into database fields, how those fields
 * are indexed, etc. The accompanying class must implement
 * {@link StoreInfo StoreInfo} or preferably extend
 * {@link AbstractStoreInfo AbstractStoreInfo}, and
 * have the same name as the JavaBean but suffixed with
 * <code>_StoreInfo</code>.
 * <h4>Collections</h4>
 * The recommended technique for defining whole/part
 * relationships is to define a "foreign key" property
 * in the part. For example, suppose <code>User</code>
 * can have zero or more instances of <code>Account</code>.
 * We could make <code>Account</code> have a property
 * named <code>owner</code> as follows:
 * <code>
 * <pre>
 *    public class Account implements Persistent {
 *        private User owner;
 *
 *        public User getOwner() {
 *            return this.owner;
 *        }
 *
 *        public void setOwner(User u) {
 *            this.owner = u;
 *        }
 *    }
 * </pre>
 * </code>
 *
 * A more efficient alternative is to use a 
 * property of type {@link PersistentID PersistentID}
 * so the <code>User</code> instance is not forced to 
 * be loaded from secondary storage when we obtain
 * an instance of <code>Account</code>.
 * <code>
 * <pre>
 *    public class Account implements Persistent {
 *        private PersistentID ownerID;
 *
 *        public PersistentID getOwnerID() {
 *            return this.ownerID;
 *        }
 *
 *        public void setOwnerID(PersistentID pid) {
 *            this.ownerID = pid;
 *        }
 *
 *        public User fetchOwner(Store store) {
 *            return (User) store.getObject(this.ownerID, User.class);
 *        }
 *    }
 * </pre>
 * </code>
 *
 * In order to add an account to a
 * user, we simply set the <code>ownerID</code> property.
 * And to obtain all accounts that belong to a user
 * we call <code>Store.selectParts</code>.
 * <code>
 * <pre>
 *    public class User implements Persistent {
 *        public void addAccount(Store store, Account account) {
 *            account.setOwnerID(store.getPersistentID(this));
 *        }
 *
 *        public Iterator fetchAccounts(Store store) {
 *            return store.selectParts(store.getPersistentID(this), Account.class, "ownerID");
 *        }
 *    }
 * </pre>
 * </code>
 * <h4>Note on Synchronization</h4>
 * If you need to synchronize getters and setters,
 * do not invoke <code>Store</code> from synchronized
 * sections of those methods because that could result 
 * in a deadlock.
 * 
 * @deprecated Use a {@link net.sourceforge.pbeans.annotations.PersistentClass} annotation instead.
 */
public interface Persistent {    
}
