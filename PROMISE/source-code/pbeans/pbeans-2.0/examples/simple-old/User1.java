import net.sourceforge.pbeans.*;
import java.util.*;

/**
 * A user.
 */
public class User1 implements net.sourceforge.pbeans.Persistent {
    private String userName;
    private String passwordHash;

    //------------- PROPERTIES ---------------

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

    //------------- HELPER METHODS ---------------

    private String createPasswordHash (String password) {
	return String.valueOf(password.hashCode());
    }

    /**
     * Password is only persisted as a hash,
     * so there's no Password property.
     */
    public void assignPassword (String password) {
	setPasswordHash (createPasswordHash (password));
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
	return store.selectParts (store.getPersistentID(this), Account1.class, "ownerID");
    }

    /**
     * To add an Account to this User, we simply set
     * the "ownerID" property of the Account instance.
     */
    public void addAccount (Account1 account, Store store) throws Exception {
	account.setOwnerID (store.getPersistentID(this));
    }

} 
