/*
 * Created 11.08.2005
 * $SimpleLogicalOperator.java$
 * @author Philipp Walther philATredmoonDOTch
 *
 */
package net.sourceforge.pbeans;

/**
 * @author Philipp Walther philATredmoonDOTch
 * 11.08.2005
 */
public class SimpleLogicalOperator implements LogicalOperator {


	private String mOperation;

	public SimpleLogicalOperator(String operation)
	{
		mOperation = operation;
	}

    /* (non-Javadoc)
     * @see net.sourceforge.pbeans.LogicalOperator#operatorString()
     */
    public String operatorString() {
        return mOperation;
    }

    public String toString() {
        return mOperation;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.pbeans.Operator#operation(java.lang.String, java.lang.String)
     */
    public String operation(String aVariable, String aValue) {
        // TODO Auto-generated method stub
        return mOperation;
    }
}
