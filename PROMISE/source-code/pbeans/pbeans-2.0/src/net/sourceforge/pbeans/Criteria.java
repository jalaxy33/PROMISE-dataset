/*
 * Criteria.java created on 2004.12.08 for pBeans.
 * $Id: Criteria.java,v 1.4 2007/03/25 21:18:16 jhsolorz Exp $
 */
package net.sourceforge.pbeans;

import java.util.HashMap;
import java.util.Map;

/**
 * Criteria is a simple {@link java.util.HashMap} with additional bells and whistles.
 * When using criteria instead of simple map, you can use any operator for identifying
 * needed objects in the database.
 * 
 * Example of using <code>Criteria</code>.
 * <code>
 * <pre>
 *	Criteria criteria = new Criteria();
 *	criteria.put("userName", "joe");
 *	criteria.put("age", "30", Operator.LESS_THAN);
 *	criteria.put("description", "%book%", new SimpleOperator(" LIKE "));
 *	
 *	ResultsIterator iterator = store.select(User.class, criteria);
 * </pre>
 * </code>
 * @author Marius Siegas
 */
public class Criteria
	extends HashMap
{
	private Map mComparisonOperatorMap;
	private Operator mDefaultComparisonOperator;
	
	private Map mLogicalOperatorMap;
	private LogicalOperator mDefaultLogicalOperator;
	
	/**
	 * Creates new criteria using specified operator mapping and default operator.
	 * 
	 * @param comparisonoperatorMap
	 * 	operator mapping for this criteria.
	 * @param defaultComparisonOperator
	 * 	default operator to use when no operator mapping exists for a key.
	 */
	public Criteria(Map comparisonoperatorMap, Operator defaultComparisonOperator)
	{
		if (comparisonoperatorMap == null || defaultComparisonOperator == null)
		{
			throw new NullPointerException("Criteria creation failed because operator map or default comparison operator was null.");
		}
		mComparisonOperatorMap = comparisonoperatorMap;
		mDefaultComparisonOperator = defaultComparisonOperator;
		mLogicalOperatorMap = new HashMap();
		mDefaultLogicalOperator = new SimpleLogicalOperator("AND NOT");
	}
	
	/**
	 * Creates new criteria using specified operator mapping and default logical operator.
	 * 
	 * @param logicaloperatorMap
	 * 	operator mapping for this criteria.
	 * @param defaultLogicalOperator
	 * 	default operator to use when no operator mapping exists for a key.
	 */
	public Criteria(Map logicaloperatorMap, LogicalOperator defaultLogicalOperator)
	{
		if (logicaloperatorMap == null || defaultLogicalOperator == null)
		{
			throw new NullPointerException("Criteria creation failed because operator map or default logical operator was null.");
		}
		mComparisonOperatorMap = new HashMap();
		mDefaultComparisonOperator = Operator.EQUALS;
		mLogicalOperatorMap = logicaloperatorMap;
		
		mDefaultLogicalOperator = defaultLogicalOperator;
	}
	
	
	/**
	 * Creates new criteria using specified operator mapping and default logical operator.
	 * 
	 * @param comparisonoperatorMap
	 * 	operator mapping for this criteria.
	 * @param defaultcomparisonOperator
	 * 	default operator to use when no operator mapping exists for a key.
	 */
	public Criteria(Map comparisonoperatorMap, Operator defaultcomparisonOperator, LogicalOperator defaultLogicalOperator)
	{
		if (comparisonoperatorMap == null || defaultLogicalOperator == null || defaultcomparisonOperator == null)
		{
			throw new NullPointerException("Criteria creation failed because operator map or default comparison/logical operator was null.");
		}
		mComparisonOperatorMap = comparisonoperatorMap;
		mDefaultComparisonOperator = defaultcomparisonOperator;
		mLogicalOperatorMap = new HashMap();
		mDefaultLogicalOperator = defaultLogicalOperator;
	
	}
	
	/**
	 * Creates new criteria using specified operator mapping and default comparison operator and default logical operator.
	 * @param comparisonoperatorMap Comparison operator mapping for this criteria.
	 * @param defaultComparisonOperator Comparison operator to use when no comparison operator mapping exists for a key.
	 * @param logicalOperatorMap
	 * @param defaultLogicalOperator
	 */
	public Criteria(Map comparisonoperatorMap, Operator defaultComparisonOperator, Map logicalOperatorMap, LogicalOperator defaultLogicalOperator)
	{
		if (comparisonoperatorMap == null || defaultComparisonOperator == null || defaultLogicalOperator == null || logicalOperatorMap == null)
		{
			throw new NullPointerException("Criteria creation failed because operator map or default operator was null.");
		}
		mComparisonOperatorMap = comparisonoperatorMap;
		mDefaultComparisonOperator = defaultComparisonOperator;
		mLogicalOperatorMap = logicalOperatorMap;
		mDefaultLogicalOperator = defaultLogicalOperator;
	}
	
	
	/**
	 * Creates new criteria using specified operator mapping.
	 * Uses {@link Operator#EQUALS} for default comparison operator.
	 * 
	 * @param comparisonoperatorMap
	 * 	operator mapping for this criteria.
	 */
	public Criteria(Map comparisonoperatorMap)
	{
		this(comparisonoperatorMap, Operator.EQUALS);
	}
	
	/**
	 * Creates new criteria.
	 * Uses new {@link HashMap} for operators and equality operator for default operator.
	 */
	public Criteria()
	{
		this(new HashMap());
	}
	
	/**
	 * Returns internal operator map.
	 * This is used internally when recreating Criteria with modified values.
	 * @return	key-operator mapping.
	 */
	public Map getOperatorMap()// TODO tighten protection?
	{
		return mComparisonOperatorMap; // TODO make unmodifiable?
	}
	
	public Map getLogicalOperatorMap() {
	    return mLogicalOperatorMap;
	}
	
	/**
	 * Returns operator associated with specified key.
	 * 
	 * @param key
	 * 	key whose operator is to be returned.
	 * @return Operator for the key. Returns default operator if the key has none
	 * 	or <code>null</code> operator associated with it. Should never return <code>null</code>.
	 */
	public Operator getOperator(Object key)
	{
		Operator operator = (Operator) mComparisonOperatorMap.get(key);
		if (operator == null)
		{
			return mDefaultComparisonOperator;
		}
		return operator;
	}
	
	/**
	 * Specifies operator to use on the key.
	 * 
	 * @param key
	 * 	the key.
	 * @param operator
	 * 	the new operator.
	 * @return
	 * 	previous operator used with the key.
	 */
	public Operator putOperator(Object key, Operator operator)
	{
		return (Operator) mComparisonOperatorMap.put(key, operator);
	}
	
	/**
	 * Removes operator used for the specified key.
	 * 
	 * @param key
	 * 	key whose operator is to be removed.
	 * @return
	 * 	the removed operator.
	 */
	public Operator removeOperator(Object key)
	{
		return (Operator) mComparisonOperatorMap.remove(key);
	}
	

	
	/**
	 * Returns logical operator associated with specified key.
	 * 
	 * @param key
	 * 	key whose logical operator is to be returned.
	 * @return
	 * 	logical operator for the key. Returns default logical operator if the key has none
	 * 	or <code>null</code> logical operator associated with it. Should never return <code>null</code>.
	 */
	public LogicalOperator getLogicalOperator(Object key)
	{
		LogicalOperator operator = (LogicalOperator) mLogicalOperatorMap.get(key);
		if (operator == null)
		{
			return mDefaultLogicalOperator;
		}
		return operator;
	}
	
	/**
	 * Specifies logical operator to use on the key.
	 * 
	 * @param key
	 * 	the key.
	 * @param operator
	 * 	the new operator.
	 * @return
	 * 	previous operator used with the key.
	 */
	public LogicalOperator putLogicalOperator(Object key, LogicalOperator operator)
	{
		return (LogicalOperator) mLogicalOperatorMap.put(key, operator);
	}
	
	/**
	 * Removes logical operator used for the specified key.
	 * 
	 * @param key
	 * 	key whose operator is to be removed.
	 * @return
	 * 	the removed operator.
	 */
	public LogicalOperator removeLogicalOperator(Object key)
	{
		return (LogicalOperator) mLogicalOperatorMap.remove(key);
	}
	
	/**
	 * Same as {@link HashMap#put(java.lang.Object, java.lang.Object)}, but also
	 * associates specified comparison operator with the key.
	 */
	public Object put(Object key, Object value, Operator operator)
	{
		putOperator(key, operator);
		return put(key, value);
	}

	/**
	 * Same as {@link HashMap#put(java.lang.Object, java.lang.Object)}, but also
	 * associates specified logical operator with the key.
	 */
	public Object put(Object key, Object value, LogicalOperator logicalOperator)
	{
		putLogicalOperator(key, logicalOperator);
		return put(key, value);
	}
	
	/**
	 * Same as {@link HashMap#put(java.lang.Object, java.lang.Object)}, but also
	 * associates specified logical and comparison operator with the key.
	 */
	public Object put(Object key, Object value, Operator operator, LogicalOperator logicalOperator)
	{
		putLogicalOperator(key, logicalOperator);
		putOperator(key, operator);
		return put(key, value);
	}
	
	/**
	 * Removes the mapping for the specified key from this criteria	 if present. Also
	 * removes operator associated with the key.
	 *
	 * @param key
	 * 	key whose mapping is to be removed from the map.
	 * @return
	 * 	previous value associated with the key.
	 * @see HashMap#remove(java.lang.Object)
	 */
	public Object remove(Object key)
	{
		removeOperator(key);
		removeLogicalOperator(key);
		return super.remove(key);
	}
	
	public LogicalOperator getDefaultLogicalOperator() {
	    return mDefaultLogicalOperator;
	}
	public Operator getDefaultComparisonOperator() {
	    return mDefaultComparisonOperator;
	}
	
}
