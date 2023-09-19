import net.sourceforge.pbeans.annotations.*;;

@PersistentClass(deleteFields=true)
public class RegressionBean {
	private MyEnum myEnum;
	private float f;
	private short s;
	private byte[] ba;
	private int[] intArray;
	private long[] longArray;
	private RegressionBean[] refArray;
	private char c;
	private boolean booleanValue;
	private java.sql.Timestamp timestampValue;
	private double doubleValue;
	private long longValue;
	private byte vyteValue;
	private java.util.Date date;
	private Object myGlobalReference;
	
	public MyEnum getMyEnum() {
		return myEnum;
	}

	public void setMyEnum(MyEnum myEnum) {
		this.myEnum = myEnum;
	}

	public byte[] getBa() {
		return ba;
	}

	public void setBa(byte[] ba) {
		this.ba = ba;
	}

	@PersistentProperty(field="myboolvalue",nullable=false,renamedFrom="isBooleanValue")
	public boolean isBooleanValue() {
		return booleanValue;
	}

	public void setBooleanValue(boolean booleanValue) {
		this.booleanValue = booleanValue;
	}

	@PersistentProperty(nullable=false)
	public char getC() {
		return c;
	}

	public void setC(char c) {
		this.c = c;
	}

	public java.util.Date getDate() {
		return date;
	}

	public void setDate(java.util.Date date) {
		this.date = date;
	}

	@PersistentProperty(nullable=false)
	public double getDoubleValue() {
		return doubleValue;
	}

	public void setDoubleValue(double doubleValue) {
		this.doubleValue = doubleValue;
	}

	@PersistentProperty(nullable=false)
	public float getF() {
		return f;
	}

	public void setF(float f) {
		this.f = f;
	}

	@PersistentProperty(nullable=false)
	public long getLongValue() {
		return longValue;
	}

	public void setLongValue(long longValue) {
		this.longValue = longValue;
	}

	@PersistentProperty(nullable=false)
	public short getS() {
		return s;
	}

	public void setS(short s) {
		this.s = s;
	}

	public java.sql.Timestamp getTimestampValue() {
		return timestampValue;
	}

	public void setTimestampValue(java.sql.Timestamp timestampValue) {
		this.timestampValue = timestampValue;
	}

	@PersistentProperty(nullable=false)
	public byte getVyteValue() {
		return vyteValue;
	}

	public void setVyteValue(byte vyteValue) {
		this.vyteValue = vyteValue;
	}

	@PersistentProperty(nullable=false)
	public float getFloatValue() {
		return this.f;
	}

	@PersistentProperty(nullable=false)
	public short getShortValue() {
		return this.s;
	}

	public byte[] getArrayValue() {
		return this.ba;
	}

	@PersistentProperty(nullable=false)
	public char getCharValue() {
		return this.c;
	}

	public java.util.Date getDateValue() {
		return this.date;
	}

	public void setCharValue (char c) {
		this.c = c;
	}

	public void setFloatValue (float f) {
		this.f = f;
	}

	public void setShortValue (short s) {
		this.s = s;
	}

	public void setArrayValue (byte[] ba) {
		this.ba = ba;
	}

	public void setDateValue (java.util.Date d) {
		this.date = d;
	}

	@PersistentProperty(globalReference=true)
	public Object getMyGlobalReference() {
		// Can refer to any persistent object.
		return myGlobalReference;
	}

	public void setMyGlobalReference(Object myGlobalReference) {
		this.myGlobalReference = myGlobalReference;
	}

	public int[] getIntArray() {
		return intArray;
	}

	public void setIntArray(int[] intArray) {
		this.intArray = intArray;
	}

	public RegressionBean[] getRefArray() {
		return refArray;
	}

	public void setRefArray(RegressionBean[] refArray) {
		this.refArray = refArray;
	}

	public long[] getLongArray() {
		return longArray;
	}

	public void setLongArray(long[] longArray) {
		this.longArray = longArray;
	}
	
}
