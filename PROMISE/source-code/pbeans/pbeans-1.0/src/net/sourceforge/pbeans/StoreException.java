package net.sourceforge.pbeans;

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
