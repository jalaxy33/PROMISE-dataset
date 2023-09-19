package net.sourceforge.pbeans.examples;

import net.sourceforge.pbeans.*;
import net.sourceforge.pbeans.data.*;
import net.sourceforge.pbeans.util.*;
import java.util.*;
import java.io.*;
import javax.sql.*;

public class RegressionTest {
    private Store store;

    public static void main (String[] args) throws Exception {
	if (args.length == 1 && "reset".equals(args[0])) {
	    new RegressionTest().reset();
	}
	else {
	    new RegressionTest().run();
	}
    }

    public RegressionTest() throws Exception {
	// Load properties from db.properties file in current directory.
	Properties props = new Properties();
	props.load (new FileInputStream ("db.properties"));

	// Get DataSource class and instance.
	Class dataSourceClass = Class.forName(props.getProperty("dataSourceClass"));
	DataSource source = (DataSource) dataSourceClass.newInstance();

	// Allow logging to file simple.out
        try {
            source.setLogWriter (new PrintWriter (new FileWriter ("regression.log")));
        } catch (UnsupportedOperationException uoe) {
            System.out.println ("setLogWriter unsupported by DataSource.");
        }

	// Set DataSource properties from values in db.properties.
	BeanMapper.populateBean (source, props);

	// Create persistent store
	this.store = new Store (source);
    }

    public void run() throws Exception {
	testMap();
	testDuplicate();
    }

    public void reset() throws Exception {
	store.remove ("flip");
	store.remove ("flop");
    }

    public void println (String s) throws Exception {
	System.out.println (s);
    }

    public void testMap() throws Exception {
	RegressionBean flip = (RegressionBean) store.get ("flip");
	RegressionBean flop = (RegressionBean) store.get ("flop");
	if (flip == null && flop == null) {
	    println ("Warn: This must be first run with this database?");
	    RegressionBean rb = new RegressionBean();
	    rb.setDateValue (null);
	    byte[] ba = new byte[10];
	    rb.setArrayValue (ba);
	    rb.setFloatValue (1.0f);
	    store.put ("flip", rb);
	}
	else if (flip == null && flop != null) {	
	    println ("Info: Flip");
	    if (flop.getArrayValue().length != 10) {
		println ("Error: Bad byte array");
	    }
	    println ("Info: last date: " + flop.getDateValue());
	    if (flop.getFloatValue() != 1.0f) {
		println ("Error: Bad float value: " + flop.getFloatValue());
	    }
	    flop.setDateValue (new java.util.Date());
	    store.save(flop);
	    store.put ("flop", null);
	    store.put ("flip", flop);
	}
	else if (flop == null && flip != null) {
	    println ("Info: Flop");
	    if (flip.getArrayValue().length != 10) {
		println ("Error: Bad byte array");
	    }
	    println ("Info: last date: " + flip.getDateValue());
	    flip.setDateValue (new java.util.Date());
	    store.save(flip);
	    store.put ("flip", null);
	    store.put ("flop", flip);
	}	    
	else {
	    println ("Error: Both flip and flop are non-null");
	}
    }

    public void testDuplicate() throws Exception {
	RegressionBean rb = new RegressionBean();
	store.insert (rb);
	try {
	    store.insert (rb);
	    println ("Error: Second insertion of same object should´ve given DuplicateEntryException");
	} catch (DuplicateEntryException de) {
	    // passed
	}
    }
}

