/*
 * Operator.java created on 2004.12.08 for pBeans.
 * $Id: Operator.java,v 1.3 2007/03/14 01:03:12 jhsolorz Exp $
 */
package net.sourceforge.pbeans;


/**
 * Operator generates an SQL operation on two expressions.
 * 
 * @author Marius Siegas
 */
public interface Operator
{
	/**
	 * Common operator equals ("="). Automaticaly replaced to {@link Operator#IS} when comparing to <tt>null</tt> value. 
	 */
	public static final Operator EQUALS = new SimpleOperator("=");
	
	/**
	 * Operator is (" IS ") for comparing to <tt>null</tt>.
	 * @see Operator#EQUALS
	 */
	public static final Operator IS = new SimpleOperator(" IS ");
	
	/**
	 * Operator less than ("<").
	 */
	public static final Operator LESS_THAN = new SimpleOperator("<");
	
	/**
	 * Operator greater than (">").
	 */
	public static final Operator GREATER_THAN = new SimpleOperator(">");
	
	
	public static final Operator LIKE = new SimpleOperator(" LIKE ");
	
	public static final Operator IN = new SimpleOperator(" IN ");
	
	/**
	 * Generates SQL operation using given expressions as arguments. The resulting
	 * string should containt the two specified strings in it. Also there must be enough
	 * spacing around operation (if needed) for it not to collapse with expressions.
	 * 
	 * @param variable
	 * 	variable expression (i.e. left-hand expression).
	 * @param value
	 * 	value expression (i.e. right-hand expression).
	 * @return
	 * 	generated string containing <tt>variable</tt>, <tt>value</tt> and the
	 * 	operation(-s) performed on them.
	 */
	String operation(String variable, String value);
	
	String operatorString();
}
