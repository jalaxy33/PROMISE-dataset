package net.sourceforge.pbeans.examples;

import java.sql.*;
import javax.sql.*;
import java.util.*;
import java.io.*;

import net.sourceforge.pbeans.util.*;
import net.sourceforge.pbeans.*;

public class Main {
    private final Store store;

    public static void main (String[] arg) throws Exception {
	new Main().run(arg);
    }

    public Main() throws Exception {
	// Load properties from db.properties file in current directory.
	Properties props = new Properties();
	props.load (new FileInputStream ("db.properties"));

	// Get DataSource class and instance.
	Class dataSourceClass = Class.forName(props.getProperty("dataSourceClass"));
	DataSource source = (DataSource) dataSourceClass.newInstance();

	// Allow logging to file simple.out
        try {
            source.setLogWriter (new PrintWriter (new FileWriter ("simple.out")));
        } catch (UnsupportedOperationException uoe) {
            System.out.println ("setLogWriter unsupported by DataSource.");
        }

	// Set DataSource properties from values in db.properties.
	BeanMapper.populateBean (source, props);

	// Create persistent store
	this.store = new Store (source);
    }

    private void run(String[] arg) throws Exception {
	String cmd = arg.length > 0 ? arg[0] : null;
	if ("createuser".equals (cmd)) {
	    createUser (arg);
	}
	else if ("createaccount".equals (cmd)) {
	    createAccount (arg);
	}
	else if ("deposit".equals (cmd)) {
	    deposit (arg);
	}
	else if ("showinfo".equals (cmd)) {
	    showInfo (arg);
	}
	else {
	    System.out.println ("Use: ./run.sh <command> arguments");
	    System.out.println ("Commands:");
	    System.out.println (" createuser <username> <password>");
	    System.out.println (" createaccount <username> <accountid>");
	    System.out.println (" deposit <accountid> <amount>");
	    System.out.println (" showinfo");
	}
    }

    private void createUser (String[] arg) throws Exception {
	User user = new User();
	user.setUserID (arg[1]);
	user.tSetPassword (arg[2]);
	try {
	    store.insert (user);
	} catch (DuplicateEntryException dee) {
	    System.out.println ("User " + arg[1] + " already exists.");
	}
    }

    private void createAccount (String[] arg) throws Exception {
	String userName = arg[1];
	User user = (User) store.selectSingle (User.class, "userID", userName);
	if (user == null) {
	    System.out.println ("User " + arg[1] + " does not exist.");
	}
	else {
	    Account account = new Account();
	    account.setOwner (user);
	    account.setAccountID (arg[2]);
	    account.setAcctBalance (0);
	    store.insert (account);
	}
    }

    private void deposit (String[] arg) throws Exception {
	String accountID = arg[1];
	double amount = Double.parseDouble(arg[2]);
	Object lock = store.requestLock (Account.class, "accountID", accountID);
	try {
	    Account account = (Account) store.selectSingle (Account.class, "accountID", accountID);
	    if (account == null) {
		System.out.println ("Account " + accountID + " not found.");
		return;
	    }
	    account.setAcctBalance (account.getAcctBalance() + amount);
	    // Important to save after a modification
	    store.save (account);
	}
	finally {
	    store.relinquishLock (lock);
	}
    }

    private void showInfo (String[] arg) throws Exception {
	Iterator i = store.select (User.class);
	while (i.hasNext()) {
	    User user = (User) i.next();
	    System.out.println ("User " + user.getUserID());
	    Iterator i2 = user.tGetAccounts(store);
	    while (i2.hasNext()) {
		Account account = (Account) i2.next();
		System.out.println (" Account " + account.getAccountID() + " balance=" + account.getAcctBalance());
	    }
	}
    }
}
