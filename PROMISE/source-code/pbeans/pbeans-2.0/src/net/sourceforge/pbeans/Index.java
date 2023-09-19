package net.sourceforge.pbeans;

import java.util.*;

/**
 * Represents a database index. An array of instances of Index may be
 * used in a StoreInfo implementation to
 * indicate which table indexes should be created. 
 * @see net.sourceforge.pbeans.StoreInfo#getIndexes
 */
public class Index {
	private final boolean unique;
	private final int keyLength;
	private final Collection propertyNames = new HashSet();

	/**
	 * Construcst an instance of Index.
	 * @param propertyNames The names of JavaBean properties belonging to this index.
	 */
	public Index (String[] propertyNames) {
		this (false, propertyNames, 0);
	}

	/**
	 * Construcst an instance of Index.
	 * @param unique If true, the underlying table may not have two rows with the same property value for the property name given.
	 * @param propertyName The name of a JavaBean property belonging to this index.
	 */
	public Index (boolean unique, String propertyName) {
		this (unique, new String[] { propertyName }, 0);
	}

	/**
	 * Construcst an instance of Index.
	 * @param unique If true, the underlying table may not have two rows with the same set of property values for the property names given.
	 * @param propertyNames The names of JavaBean properties belonging to this index.
	 * @param keyLength Index length for string fields. If it's zero, it isn't used.
	 */
	public Index (boolean unique, String[] propertyNames, int keyLength) {
		this.unique = unique;
		this.keyLength = keyLength;
		for (int i = 0; i < propertyNames.length; i++) {
			this.propertyNames.add (propertyNames[i]);
		}
	}
	
	public int getKeyLength() {
		return keyLength;
	}

	public Collection getPropertyNames() {
		return this.propertyNames;
	}

	public boolean isUnique() {
		return this.unique;
	}

	public int hashCode() {
		int hc = 0;
		Iterator i = this.propertyNames.iterator();
		while (i.hasNext()) {
			hc ^= i.next().hashCode();
		}
		return hc;
	}

	/**
	 * Compares this Index with another. 
	 * @return True if their uniqueness is the same and the sets of property names are equivalent.
	 */
	public boolean equals (Object other) {
		if (!(other instanceof Index)) {
			return false;
		}
		Index oid = (Index) other;
		return 
		this.unique == oid.isUnique() &&
		this.propertyNames.equals (oid.getPropertyNames());
	}

	/**
	 * Adds a property name to the index. Note that this will change
	 * the identity of the index, and will thus affect the <code>equals</code> method.
	 * @param name
	 */
	public void addPropertyName(String name) {
		this.propertyNames.add(name);
	}
}
