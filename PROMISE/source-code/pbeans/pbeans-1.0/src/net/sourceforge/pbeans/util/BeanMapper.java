package net.sourceforge.pbeans.util;

import java.beans.*;
import java.util.*;
import java.lang.reflect.*;
import net.sourceforge.pbeans.*;

/**
 * Utilities to populate JavaBeans.
 */
public class BeanMapper {
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
}
