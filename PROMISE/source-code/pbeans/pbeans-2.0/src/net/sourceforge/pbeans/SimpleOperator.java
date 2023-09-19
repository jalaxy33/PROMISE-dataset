/*
 * SimpleOperator.java created on 2004.12.09 for pBeans.
 * $Id: SimpleOperator.java,v 1.2 2005/10/20 20:23:10 philz Exp $
 */
package net.sourceforge.pbeans;


/**
 * SimpleOperator generates a simple operation on two expressions.
 * The two given expressions are simply concatenated with operation
 * (specified in constructor) between them.
 * 
 * Note: objects of this class are immutable (i.e. once created they cannot be changed). 
 * 
 * @author Marius Siegas
 */
public final class SimpleOperator
	implements Operator
{
	private String mOperation;

	public SimpleOperator(String operation)
	{
		mOperation = operation;
	}
	
	public String operation(String variable, String value)
	{
		return variable + mOperation + value;
	}

	/**
	 * Compares this simple operation to other object. The result is true
	 * if and only if the given object is <tt>SimpleOperator</tt> and its
	 * operation string equals to the operation string of this object.  
	 */
	public boolean equals(Object obj)
	{
		if (obj == null)
		{
			return false;
		}
		if (obj instanceof SimpleOperator)
		{
			return mOperation.equals(((SimpleOperator) obj).mOperation);
		}
		else
		{
			return false;
		}
	}
	
    public String toString() {
        return mOperation;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.pbeans.Operator#operatorString()
     */
    public String operatorString() {
        return mOperation;
    }
}
