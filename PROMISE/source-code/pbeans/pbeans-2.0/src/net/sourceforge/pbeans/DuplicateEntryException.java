package net.sourceforge.pbeans;

/**
 * Thrown when a database record cannot be inserted
 * because there's already an entry with equal unique-index values
 * or primary keys.
 * @see net.sourceforge.pbeans.Store#insert
 * @see net.sourceforge.pbeans.Store#register
 * @see net.sourceforge.pbeans.Index
 */
public class DuplicateEntryException extends StoreException {
    public DuplicateEntryException (String message) {
	super (message);
    }

    public DuplicateEntryException (Exception innerException) {
	super (innerException.getMessage(), innerException);
    }

    public DuplicateEntryException (String message, Exception innerException) {
	super (message, innerException);
    }
}
