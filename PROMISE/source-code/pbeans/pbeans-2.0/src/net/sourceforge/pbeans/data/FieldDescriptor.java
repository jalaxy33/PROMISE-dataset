package net.sourceforge.pbeans.data;

/**
 * Describes a table column.
 */
public class FieldDescriptor {
    private final String name;
    private final int sqlType;
    private final boolean nullable;
    private final boolean primaryKey;
    private final String[] renamedFrom;
    private final boolean globalReference;
    
    /**
     * Constructs a FieldDescriptor.
     * @param name A field name (unnormalized) as requested by the user.
     *             This may differ slightly from the name actually used
     *             in the database, called a <i>normalized</i> name.
     * @param sqlType An integer constant from java.sql.Types.
     * @param nullable Whether the field should accept NULL database values.
     * @param renamedFrom A list of old field names that the infrastructure
     *                    should look at in the order given to see if they
     *                    can be renamed to create this field when necessary.
     */
    public FieldDescriptor (String name, int sqlType, boolean nullable, String[] renamedFrom, boolean primaryKey, boolean globalReference) {
    	this.name = name;
    	this.sqlType = sqlType;
        this.nullable = nullable;
        this.renamedFrom = renamedFrom;
        this.primaryKey = primaryKey;
        this.globalReference = globalReference;
    }

    public FieldDescriptor (String name, int sqlType, boolean nullable, String renamedFrom, boolean globalReference) {
        this (name, sqlType, nullable, renamedFrom == null ? null : new String[] { renamedFrom }, false, globalReference);
    }

    public FieldDescriptor (String name, int sqlType, boolean nullable) {
        this (name, sqlType, nullable, (String[]) null, false, false);
    }

    public FieldDescriptor (String name, int sqlType, String[] renamedFrom) {
        this (name, sqlType, true, renamedFrom, false, false);
    }

    public FieldDescriptor (String name, FieldDescriptor other) {
        this (name, other.getSqlType(), other.isNullable(), other.getRenamedFrom(), other.isPrimaryKey(), other.globalReference);
    }

    public FieldDescriptor (FieldDescriptor other, String[] renamedFrom) {
        this (other.getName(), other.getSqlType(), other.isNullable(), renamedFrom, other.isPrimaryKey(), other.globalReference);
    }

    public FieldDescriptor (String name, int sqlType) {
        this (name, sqlType, true);
    }

    public boolean isGlobalReference() {
    	return this.globalReference;
    }
    
    public boolean isPrimaryKey() {
    	return this.primaryKey;
    }

    public String getName() {
    	return this.name;
    }

    public int getSqlType() {
	return this.sqlType;
    }

    public boolean isNullable() {
        return this.nullable;
    }

    
	public String[] getRenamedFrom() {
        return this.renamedFrom;
    }
	
	boolean matches(AbstractDatabase ad, int sqlType, boolean nullable) {
		return ad.typesMatch(this.sqlType, sqlType) &&
		       this.nullable == nullable;
	}
}
