/*
 * Created 11.08.2005
 * $LogicalOperator.java$
 * @author Philipp Walther philATredmoonDOTch
 *
 */
package net.sourceforge.pbeans;

/**
 * @author Philipp Walther philATredmoonDOTch
 * 11.08.2005
 */
public interface LogicalOperator {
    /** 
     * Logical Operator AND
     * (as in select * from table where id = 1 AND fk = 2)
     */
	public static final LogicalOperator LOGICAL_AND = new SimpleLogicalOperator("and");
	
	/** 
     * Logical Operator OR
     * (as in select * from table where id = 2 OR fk = 3)
     */
	public static final LogicalOperator LOGICAL_OR = new SimpleLogicalOperator("or");
	
	/**
	 * like toString(), prints out the operator string (AND or OR ...)
	 */
	String operatorString();
}
