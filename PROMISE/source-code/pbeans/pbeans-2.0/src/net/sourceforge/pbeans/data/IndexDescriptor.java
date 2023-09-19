package net.sourceforge.pbeans.data;

import java.util.*;

/**
 * Represents a unique or non-unique table index.
 */
public class IndexDescriptor {
	private final String name;
	private final boolean unique;
	private final int keyLength;
	private final Collection<String> fieldNames = new HashSet<String>();

	public IndexDescriptor (String name, String[] fieldNames) {
		this (name, false, fieldNames, 0);
	}

	public IndexDescriptor (String name, boolean unique, String fieldName) {
		this (name, unique, new String[] { fieldName }, 0);
	}

	public IndexDescriptor (String name, boolean unique, String[] fieldNames, int keyLength) {
		this.name = name;
		this.unique = unique;
		this.keyLength = keyLength;
		for (int i = 0; i < fieldNames.length; i++) {
			this.fieldNames.add (fieldNames[i]);
		}
	}

	public int getKeyLength() {
		return keyLength;
	}

	public void addFieldName (String fieldName) {
		this.fieldNames.add (fieldName);
	}

	public String getName() {
		return this.name;
	}

	public Collection getFieldNames() {
		return this.fieldNames;
	}

	public boolean isUnique() {
		return this.unique;
	}

	/**
	 * Compares this IndexDescriptor to another.
	 * @return True if the name, its uniqueness and the set of field names match the other
	 *         IndexDescriptor.
	 */
	public boolean equals (Object other) {
		if (!(other instanceof IndexDescriptor)) {
			return false;
		}
		IndexDescriptor oid = (IndexDescriptor) other;
		return 
		this.name.equals (oid.getName()) &&
		this.unique == oid.isUnique() &&
		this.fieldNames.equals (oid.getFieldNames()) 
		//(TODO: keyLength cannot be compared because index from metadata doesn't have this)
		//&& this.keyLength == oid.keyLength
		;
	}

	public int hashCode() {
		return this.name.hashCode();
	}
}
