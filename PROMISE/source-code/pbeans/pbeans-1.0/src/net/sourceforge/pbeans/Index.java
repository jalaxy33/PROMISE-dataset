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
    private final Collection propertyNames = new HashSet();

    /**
     * Construcst an instance of Index.
     * @param propertyNames The names of JavaBean properties belonging to this index.
     */
    public Index (String[] propertyNames) {
	this (false, propertyNames);
    }

    /**
     * Construcst an instance of Index.
     * @param unique If true, the underlying table may not have two rows with the same property value for the property name given.
     * @param propertyName The name of a JavaBean property belonging to this index.
     */
    public Index (boolean unique, String propertyName) {
	this (unique, new String[] { propertyName });
    }

    /**
     * Construcst an instance of Index.
     * @param unique If true, the underlying table may not have two rows with the same set of property values for the property names given.
     * @param propertyNames The names of JavaBean properties belonging to this index.
     */
    public Index (boolean unique, String[] propertyNames) {
	this.unique = unique;
        for (int i = 0; i < propertyNames.length; i++) {
            this.propertyNames.add (propertyNames[i]);
        }
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

}
