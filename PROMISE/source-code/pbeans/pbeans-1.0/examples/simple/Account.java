package net.sourceforge.pbeans.examples;

import net.sourceforge.pbeans.*;

/**
 * An account. A User has zero or more accounts.
 */
public class Account implements Persistent {
    private String accountID;
    private double balance;
    private User owner;

    public String getAccountID() {
	return this.accountID;
    }

    public void setAccountID (String value) {
	this.accountID = value;
    }

    public User getOwner() {
	return this.owner;
    }

    public void setOwner (User value) {
	this.owner = value;
    }

    public double getAcctBalance() {
	return this.balance;
    }

    public void setAcctBalance (double value) {
	this.balance = value;
    }
} 
