package net.sourceforge.pbeans.util;

import java.beans.*;
import java.util.*;
import java.lang.reflect.*;

import java.util.logging.*;
/**
 * Utilities to populate JavaBeans.
 */
public class BeanMapper {
	private static final Logger logger = Logger.getLogger(BeanMapper.class.getName());
	
	/**
	 * Populates a JavaBean with values from a map, excluding <code>null</code> values.
	 */
	public static void populateBean (Object bean, Map propertyValues) throws IntrospectionException, IllegalAccessException, InvocationTargetException {
		populateBean (bean, propertyValues, false);
	}

	/**
	 * Populates a JavaBean with values from a map.
	 */
	public static void populateBean (Object bean, Map propertyValues, boolean setNullValues) throws IntrospectionException, IllegalAccessException, InvocationTargetException {
		BeanInfo binfo = Introspector.getBeanInfo (bean.getClass());
		PropertyDescriptor[] pds = binfo.getPropertyDescriptors();
		for (int i = 0; i < pds.length; i++) {
			PropertyDescriptor pd = pds[i];	
			String propName = pd.getName();
			Object dbValue = propertyValues.get (propName);
			Method setter = pd.getWriteMethod();
			if (setter != null) {
				if (dbValue != null || setNullValues) {
					if (dbValue == null && pd.getPropertyType().isPrimitive()) {
						throw new IllegalArgumentException ("Null property value for primitive property " + propName);
					}
					setter.invoke (bean, new Object[] { dbValue });
				}
			}
		}
	}

	public static void populateBean(Object bean, Object sourceBean) throws IntrospectionException, IllegalAccessException, InvocationTargetException {
		// Map source property descriptor
		Map sourcepdbyname = new HashMap();
		BeanInfo sourcebinfo = Introspector.getBeanInfo(sourceBean.getClass());
		PropertyDescriptor[] spds = sourcebinfo.getPropertyDescriptors();
		for (int i = 0; i < spds.length; i++) {
			PropertyDescriptor pd = spds[i];	
			String propName = pd.getName();
			sourcepdbyname.put(propName, pd);
		}
		BeanInfo binfo = Introspector.getBeanInfo (bean.getClass());
		PropertyDescriptor[] pds = binfo.getPropertyDescriptors();
		for (int i = 0; i < pds.length; i++) {
			PropertyDescriptor pd = pds[i];	
			String propName = pd.getName();
			Method setter = pd.getWriteMethod();
			if (setter != null) {
				PropertyDescriptor spd = (PropertyDescriptor) sourcepdbyname.get(propName);
				if(spd != null) {
					Method sourceGetter = spd.getReadMethod();
					if(sourceGetter != null) {
						Object value = sourceGetter.invoke(sourceBean, (Object[]) null);
						setter.invoke(bean, value);
					}
					else {
						logger.warning("Property " + propName + " in " + sourceBean.getClass().getName() + " is not readable.");
					}
				}
			}
		}
	}

}
