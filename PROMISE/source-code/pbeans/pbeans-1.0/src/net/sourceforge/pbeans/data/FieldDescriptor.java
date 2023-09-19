package net.sourceforge.pbeans.data;

import java.util.*;
import java.sql.*;
import javax.sql.*;

/**
 * Describes a table column.
 */
public class FieldDescriptor {
    private final String name;
    private final int sqlType;
    private final boolean nullable;
    private final String[] renamedFrom;

    /**
     * Constructs a FieldDescriptor.
     * @param name The requested field name.
     * @param sqlType An integer constant from java.sql.Types.
     * @param nullable Whether the field should accept NULL database values.
     * @param renamedFrom A list of old field names that the infrastructure
     *                    should look at in the order given to see if they
     *                    can be renamed to create this field when necessary.
     */
    public FieldDescriptor (String name, int sqlType, boolean nullable, String[] renamedFrom) {
	this.name = name;
	this.sqlType = sqlType;
        this.nullable = nullable;
        this.renamedFrom = renamedFrom;
    }

    public FieldDescriptor (String name, int sqlType, boolean nullable, String renamedFrom) {
        this (name, sqlType, nullable, new String[] { renamedFrom });
    }

    public FieldDescriptor (String name, int sqlType, boolean nullable) {
        this (name, sqlType, nullable, (String[]) null);
    }

    public FieldDescriptor (String name, int sqlType, String[] renamedFrom) {
        this (name, sqlType, true, renamedFrom);
    }

    public FieldDescriptor (String name, FieldDescriptor other) {
        this (name, other.getSqlType(), other.isNullable(), other.getRenamedFrom());
    }

    public FieldDescriptor (FieldDescriptor other, String[] renamedFrom) {
        this (other.getName(), other.getSqlType(), other.isNullable(), renamedFrom);
    }

    public FieldDescriptor (String name, int sqlType) {
        this (name, sqlType, true);
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
}
