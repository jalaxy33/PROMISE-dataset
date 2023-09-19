package net.sourceforge.pbeans;

/**
 * Exception thrown by {@link Store} methods, generally
 * when there's a database error.
 */
public class StoreException extends Exception {
    public StoreException (String message) {
	super (message);
    }

    public StoreException (Exception innerException) {
	super (innerException.getMessage(), innerException);
    }

    public StoreException (String message, Exception innerException) {
	super (message, innerException);
    }
}
