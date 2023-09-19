
import net.sourceforge.pbeans.*;
import net.sourceforge.pbeans.annotations.*;

/**
 * An account. A User has zero or more accounts.
 * An account is owned by exactly one user.
 */
@PersistentClass(
		table="Account",
		idField="InternalAID",
		deleteFields=true,
		indexes={
				@PropertyIndex(unique=true,propertyNames="accountID")	
		}
)
public class Account {
	private String accountID;
	private double balance;
	private PersistentID ownerID;

	public String getAccountID() {
		return this.accountID;
	}

	public void setAccountID (String value) {
		this.accountID = value;
	}

	/**
	 * Note that we could define a property of type
	 * <code>User</code>, and that works. But it is
	 * more efficient to define a property of type
	 * PersistentID and load User objects on demand.
	 */
	public PersistentID getOwnerID() {
		return this.ownerID;
	}

	public void setOwnerID (PersistentID value) {
		this.ownerID = value;
	}
	
	/**
	 * This is the method that loads the User object
	 * on demand, which is effectively very similar
	 * to having a property of type User.
	 */
	@TransientProperty
	public User getOwner(Store store) throws StoreException {
		return (User) store.getObject(this.getOwnerID(), User.class);
	}

	public double getAcctBalance() {
		return this.balance;
	}

	public void setAcctBalance (double value) {
		this.balance = value;
	}
}
