import net.sourceforge.pbeans.*;
import net.sourceforge.pbeans.annotations.*;
import java.util.*;

/**
 * A user.
 */
@PersistentClass(
		table="User", // Use this one instead of an automatically generated name
		idField="InternalUID", // Use this field name for implicit primary key
		userManaged=false, // Let pBeans manage schema evolution
		deleteFields=true, // If a field becomes unused, remove it.
		indexes={
				//Unique index for field userID
				@PropertyIndex(unique=true,propertyNames={"userID"})
		}
)
public class User {
	private String userName;
	private String passwordHash;

	//------------- PERSISTENT PROPERTIES ---------------

	public String getUserID() {
		return this.userName;
	}

	public void setUserID (String value) {
		this.userName = value;
	}

	public String getPasswordHash() {
		return this.passwordHash;
	}

	public void setPasswordHash (String value) {
		this.passwordHash = value;
	}

	//------------- TRANSIENT PROPERTIES----------
	
	@TransientProperty
	public void setPassword(String password) {
		setPasswordHash (createPasswordHash (password));
	}
	
	//------------- HELPER METHODS ---------------

	private String createPasswordHash (String password) {
		return String.valueOf(password.hashCode());
	}

	public boolean matchPassword (String password) {
		return createPasswordHash(password).equals(getPasswordHash());
	}	

	/**
	 * This is a good way to get a collection of Accounts
	 * belonging to this User. We are asking for all instances
	 * of Account whose "ownerID" property value is equal to 
	 * the ID of this User instance.
	 */
	public Iterator fetchAccounts (Store store) throws Exception {
		return store.selectParts (store.getPersistentID(this), Account.class, "ownerID");
	}

	/**
	 * To add an Account to this User, we simply set
	 * the "ownerID" property of the Account instance.
	 */
	public void addAccount (Account account, Store store) throws Exception {
		account.setOwnerID(store.getPersistentID(this));
		store.save(account);
	}
} 
