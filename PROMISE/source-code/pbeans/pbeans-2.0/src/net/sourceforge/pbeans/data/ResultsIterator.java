package net.sourceforge.pbeans.data;

/**
 * An extension of <code>java.util.Iterator</code> 
 * for objects obtained from a database connection.
 * A <code>ResultsIterator</code> instance should always be closed
 * in order to release underlying resources.
 * <p>
 * A <code>ResultsIterator</code> is closed implicitly
 * if the number of elements run out, or if it is
 * garbage collected.
 */
public interface ResultsIterator<T> extends java.util.Iterator<T> {
	// Should not throw SQLException as callers don't expect to catch that.
    public void close();
}
