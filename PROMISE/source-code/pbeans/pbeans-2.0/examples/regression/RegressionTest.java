import net.sourceforge.pbeans.*;
import net.sourceforge.pbeans.annotations.*;
import net.sourceforge.pbeans.util.*;
import java.util.*;
import java.io.*;
import javax.sql.*;
import java.sql.*;

/**
 * This is used to test pBeans features, e.g.
 * whether database "plug ins" work.
 */
public class RegressionTest {
	private Store store;

	private byte[] bytes = new byte[10];

	public void run() throws Exception {
		testMap();
		testAutoIncrement();
		testDuplicate();
		testLock();
		testSelfLoop();
		System.out.println("--- TESTS DONE ---");
	}

	public static void main(String[] args) throws Exception {
		RegressionTest rt = new RegressionTest();
		if (args.length == 1 && "reset".equals(args[0])) {
			rt.reset();
		} else {
			rt.run();
		}
	}

	public RegressionTest() throws Exception {
		this.bytes[0] = 50;
		this.bytes[1] = -50;

		// Load properties from db.properties file in current directory.
		System.out.println("Current directory: " + System.getProperty("user.dir"));
		Properties props = new Properties();
		try {
			props.load(new FileInputStream("db.properties"));
		} catch(FileNotFoundException fnf) {
			props.load(new FileInputStream("examples/regression/db.properties"));			
		}

		// Get DataSource class and instance.
		Class dataSourceClass = Class.forName(props
				.getProperty("dataSourceClass"));
		DataSource source = (DataSource) dataSourceClass.newInstance();

		// Set DataSource properties from values in db.properties.
		BeanMapper.populateBean(source, props);

		// Create persistent store
		this.store = new Store(source);
	}

	public void reset() throws Exception {
		store.remove("flip");
		store.remove("flop");
	}

	public void println(String s) throws Exception {
		System.out.println(s);
	}

	public void testMap() throws Exception {
		RegressionBean flip = (RegressionBean) store.get("flip");
		RegressionBean flop = (RegressionBean) store.get("flop");
		int[] initialIntArray = new int[] { 56000, Integer.MAX_VALUE, -90500, Integer.MIN_VALUE };
		long[] initialLongArray = new long[] { Long.MAX_VALUE, Long.MIN_VALUE, 0L, -1L, 543210L };
		if (flip == null && flop == null) {
			println("Warn: This must be first run with this database?");
			RegressionBean rb = new RegressionBean();
			rb.setDateValue(new java.util.Date());
			rb.setArrayValue(this.bytes);
			rb.setFloatValue(1.0f);
			rb.setCharValue('*');
			// Reference to itself
			rb.setMyGlobalReference(rb);
			rb.setIntArray(initialIntArray);
			rb.setLongArray(initialLongArray);
			RegressionBean[] initialRefArray = new RegressionBean[] { rb, rb };
			rb.setRefArray(initialRefArray);
			rb.setMyEnum(MyEnum.RED);
			store.put("flip", rb);
		} else {
			RegressionBean nonNullBean = flop == null ? flip : flop;
			if(nonNullBean != null && nonNullBean.getMyEnum() != MyEnum.RED) {
				System.out.println("ERROR: enum value wrong: " + nonNullBean.getMyEnum());
				nonNullBean.setMyEnum(MyEnum.RED);
			}
			Object myRef = nonNullBean.getMyGlobalReference();
			if(myRef != nonNullBean) {
				System.out.println("ERROR: myGlobalReference does not self-loop.");
				nonNullBean.setMyGlobalReference(nonNullBean);
			}
			int[] storedInt = nonNullBean.getIntArray();
			if(storedInt == null || !Arrays.equals(initialIntArray, storedInt)) {
				System.out.println("ERROR: stored integer array differs from that initially set: " + (storedInt == null ? null : Arrays.asList(storedInt)));	
				nonNullBean.setIntArray(initialIntArray);
			}
			long[] storedLong = nonNullBean.getLongArray();
			if(storedLong == null || !Arrays.equals(initialLongArray, storedLong)) {
				System.out.println("ERROR: stored long array differs from that initially set: " + (storedLong == null ? null : Arrays.asList(storedLong)));	
				nonNullBean.setLongArray(initialLongArray);				
			}
			RegressionBean[] storeRefs = nonNullBean.getRefArray();
			if(storeRefs == null || storeRefs[0] != nonNullBean | storeRefs[1] != nonNullBean) {
				System.out.println("ERROR: stored refs differ from those initially set: " + (storeRefs == null ? null : Arrays.asList(storeRefs)));	
				nonNullBean.setRefArray(new RegressionBean[] { nonNullBean, nonNullBean });				
			}
			java.util.Date lastDate = nonNullBean.getDateValue();
			if(lastDate == null) {
				System.out.println("ERROR: dateValue is NULL.");
			}
			else {
				println("Info: last date: " + lastDate);
			}
			char ch = nonNullBean.getCharValue();
			if(ch != '*') {
				println("ERROR: Bad char value: [" + ch + "]");
				nonNullBean.setCharValue('*');
			}
			byte[] ba = nonNullBean.getArrayValue();
			if (ba == null || ba.length != 10) {
				println("ERROR: Bad byte array");
			}
			if (ba != null && ba[0] != this.bytes[0]) {
				println("ERROR: Bad bytes[0] value=" + ba[0]);
			}
			if (ba != null && ba[1] != this.bytes[1]) {
				println("ERROR: Bad bytes[1] value=" + ba[1]);
			}
			if (nonNullBean.getFloatValue() != 1.0f) {
				println("ERROR: Bad float value: " + nonNullBean.getFloatValue());
			}
			nonNullBean.setDateValue(new java.util.Date());
			store.save(nonNullBean);
			if (flip == null && flop != null) {
				println("Info: Flip");
				store.put("flop", null);
				store.put("flip", flop);
			} else if (flop == null && flip != null) {
				println("Info: Flop");
				store.put("flip", null);
				store.put("flop", flip);
			} else {
				println("ERROR: Both flip and flop are non-null");
			}
		}
	}

	public void testDuplicate() throws Exception {
		RegressionBean rb = new RegressionBean();
		store.insert(rb);
		try {
			store.insert(rb);
			println("Double-insert test: FAILED: Second insertion of same instance should've given StoreException.");
		} catch (StoreException de) {
			println("Double-insert test: PASSED");
		}
		DummyClass account = this.getAccount();
		if(account == null) {
			account = new DummyClass();
			account.setAccountID(accountID);
			store.insert(account);
		}
		try {
			account = new DummyClass();
			account.setAccountID(accountID);
			store.insert(account);
			println("Insert same-key test: FAILED: Second insertion with same key should've given DuplicateEntryException.");
		} catch (DuplicateEntryException de) {
			println("Insert same-key test: PASSED");
		} catch (StoreException se) {
			String sqlState = null;
			int errorCode = 0;
			if(se.getCause() instanceof SQLException) {
				SQLException sqle = (SQLException) se.getCause();
				sqlState = sqle.getSQLState();
				errorCode = sqle.getErrorCode();
			}
			println("Insert same-key test: FAILED: Got non-DuplicateEntryException with cause: " + se.getCause() + ",sqlState=" + sqlState + ",errorCode=" + errorCode);			
		}
	}
	
	private static final String accountID = "12345";
	
	private DummyClass getAccount() throws StoreException {
		return (DummyClass) store.selectSingle(DummyClass.class, "accountID", accountID);
	}
	
	public void testLock() throws Exception {	
		DummyClass account = this.getAccount();
		if(account == null) {
			account = new DummyClass();
			account.setAccountID(accountID);
			store.insert(account);
		}
		final double initialBalance = account.getBalance();
	    final double stepIncrement = 10.0;
		final int numSteps = 10;
		final int numThreads = 3;
		Thread[] threads = new Thread[numThreads];
		for(int i = 0; i < numThreads; i++) {
			final int tn = i;
			Thread t = new Thread() {
				public void run() {
					try {
						for(int count = 0; count < numSteps; count++) {
							System.out.println("- Thread " + tn + " step " + count);
							Thread.yield();
							store.requestLock(DummyClass.class, accountID);
							try {
								DummyClass account = getAccount();
								double oldBalance = account.getBalance();
								Thread.sleep(50);
								account.setBalance(oldBalance + stepIncrement);
								store.save(account);
							} finally {
								store.relinquishLock();
							}
						}
					} catch(Exception err) {
						err.printStackTrace();
					}
				}
			};
			t.start();
			threads[i] = t;
		}
		// Wait for threads to finish
		for(int i = 0; i < numThreads; i++) {
			threads[i].join();
		}
		boolean passed = account.getBalance() == initialBalance + (numSteps * numThreads * stepIncrement);
		System.out.println("Lock Test: " + (passed ? "PASSED" : "FAILED - " + account.getBalance()));
	}
	
	public void testAutoIncrement() throws StoreException {
		for(int i = 0; i < 3; i++) {
			String aid = String.valueOf(net.sourceforge.pbeans.util.Hash.generateLong());
			DummyClass account = new DummyClass();
			account.setAccountID(aid);
			store.insert(account);
			System.out.println("Info: ID of new inserted account: " + store.getPersistentID(account));
		}
	}

    public void testSelfLoop() throws Exception {
    	RegressionBean bean = new RegressionBean();
    	RegressionBean bean2 = new RegressionBean();
    	bean.setMyGlobalReference(bean);
    	bean.setRefArray(new RegressionBean[] { bean, bean2 });
    	bean2.setMyGlobalReference(bean);
    	bean2.setRefArray(new RegressionBean[] { bean2, bean });
    	this.store.insert(bean);
    	System.out.println("Info: Insertion with self-loop passed.");
    	this.store.reload(bean);
    	this.store.register(bean2);
    	if(bean.getMyGlobalReference() != bean) {
    		System.out.println("ERROR: Global reference not right on reload.");
    	}
    	if(bean2.getMyGlobalReference() != bean) {
    		System.out.println("ERROR: Global reference not right on reload.");
    	}
    	if(bean.getRefArray()[0] != bean || bean2 != bean.getRefArray()[1]) {
    		System.out.println("ERROR: References not right on reload (1).");
    	}
    	if(bean2.getRefArray()[0] != bean2 || bean != bean2.getRefArray()[1]) {
    		System.out.println("ERROR: References not right on reload (2).");
    	}
    }
    
	@PersistentClass(
		table="DummyTable",
		autoIncrement=true,
		deleteFields=true,
		idField="DummyID",
		indexes=@PropertyIndex(unique=true,propertyNames="accountID")
	)
	public static class DummyClass {
		private String accountID;
		private double balance = 0.0;

		@PersistentProperty(nullable=false)
		public String getAccountID() {
			return accountID;
		}

		public void setAccountID(String accountID) {
			this.accountID = accountID;
		}

		public double getBalance() {
			return balance;
		}
		
		public void setBalance(double balance) {
			this.balance = balance;
		}
		
		@TransientProperty
		public Object getObjectProperty() {
			return null;
		}
		
		public void setObjectProperty(Object val) {			
		}
	}
}
