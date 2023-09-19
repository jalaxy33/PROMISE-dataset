package net.sourceforge.pbeans;

/**
 * @deprecated Unused.
 * @author user
 * @hide
 */
class MissingEntryException {
    private final String primaryKeyProperty;
    private final String value;

    public MissingEntryException(String primaryKeyProperty, String value) {
        this.primaryKeyProperty = primaryKeyProperty;
        this.value = value;
    }

    public String toString() {
        return "Missing entry with " + this.primaryKeyProperty + "=" + this.value;
    }
}
