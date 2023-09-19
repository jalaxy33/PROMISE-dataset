package net.sourceforge.pbeans.util;

public class NameValueArray {
	public final String[] nameArray;
	public final Object[] valueArray;
	public final int length;
	
	public NameValueArray(int length) {
		this.length = length;
		this.nameArray = new String[length];
		this.valueArray = new Object[length];
	}
 
	public final void setPair(int index, String name, Object value) {
		this.nameArray[index] = name;
		this.valueArray[index] = value;
	}
}
