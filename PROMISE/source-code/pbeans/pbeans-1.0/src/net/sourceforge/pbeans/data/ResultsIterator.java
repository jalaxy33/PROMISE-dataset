package net.sourceforge.pbeans.data;

/**
 * An extension of <code>java.util.Iterator</code> 
 * for objects obtained from a database connection.
 * If the iterator is enumerated until hasNext() returns
 * false, there is no need to close it. If not, we
 * recommend that the <code>close</code> method be invoked
 * to avoid holding resources longer than necessary.
 * The <code>close</code> method should be invoked implicitly if
 * the ResultsIterator is garbage collected.
 */
public interface ResultsIterator extends java.util.Iterator {
    public void close() throws java.sql.SQLException;
}
