import net.sourceforge.pbeans.*;

/**
 * An account. A User has zero or more accounts.
 */
public class Account1 implements Persistent {
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
     * It is more efficient to define a property of
     * type PersistentID than one of type Persistent.
     */
    public PersistentID getOwnerID() {
        return this.ownerID;
    }

    public void setOwnerID (PersistentID value) {
	this.ownerID = value;
    }

    public double getAcctBalance() {
	return this.balance;
    }

    public void setAcctBalance (double value) {
	this.balance = value;
    }
}
