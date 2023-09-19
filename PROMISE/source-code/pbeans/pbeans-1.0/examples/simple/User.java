package net.sourceforge.pbeans.examples;

import net.sourceforge.pbeans.*;
import java.util.*;

/**
 * A user.
 */
public class User implements net.sourceforge.pbeans.Persistent {
    private String userName;
    private String passwordHash;

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

    private String tGetPasswordHash (String password) {
	return String.valueOf(password.hashCode());
    }

    /**
     * Password is only persisted as a hash,
     * so there's no Password property.
     */
    public void tSetPassword (String password) {
	setPasswordHash (tGetPasswordHash (password));
    }

    public boolean MatchPassword (String password) {
	return tGetPasswordHash(password).equals(getPasswordHash());
    }	

    /**
     * This is a good way to get a collection of Accounts
     * belonging to this User. We are asking for all instances
     * of Account whose "owner" property value is equal to this User instance.
     */
    public Iterator tGetAccounts (Store store) throws Exception {
	return store.selectAggregation (Account.class, "owner", this);
    }

    /**
     * To add an Account to this User, we simply set
     * the "owner" property of the Account instance.
     */
    public void AddAccount (Account account) {
	account.setOwner (this);
    }

} 
