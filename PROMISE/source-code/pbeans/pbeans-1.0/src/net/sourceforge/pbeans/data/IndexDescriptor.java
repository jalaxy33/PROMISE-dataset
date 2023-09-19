package net.sourceforge.pbeans.data;

import java.util.*;

/**
 * Represents a unique or non-unique table index.
 */
public class IndexDescriptor {
    private final String name;
    private final boolean unique;
    private final Collection fieldNames = new HashSet();

    public IndexDescriptor (String name, String[] fieldNames) {
	this (name, false, fieldNames);
    }

    public IndexDescriptor (String name, boolean unique, String fieldName) {
	this (name, unique, new String[] { fieldName });
    }

    public IndexDescriptor (String name, boolean unique, String[] fieldNames) {
	this.name = name;
	this.unique = unique;
	for (int i = 0; i < fieldNames.length; i++) {
	    this.fieldNames.add (fieldNames[i]);
	}
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
	    this.fieldNames.equals (oid.getFieldNames());
    }

    public int hashCode() {
        return this.name.hashCode();
    }
}
