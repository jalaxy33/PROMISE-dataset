package net.sourceforge.pbeans.servlet;

import java.beans.*;
import java.lang.reflect.*;

import javax.servlet.*;

/**
 * Utilities to populate JavaBeans.
 * @deprecated not used
 * @hidden
 */
class ServletMapper {
	public static Object createBean(ClassLoader classLoader, String className, ServletRequest request) throws IntrospectionException, IllegalAccessException, InvocationTargetException, ConversionException, ClassNotFoundException, InstantiationException {
		Class clazz = classLoader == null ? Class.forName(className) : classLoader.loadClass(className);
		Object bean = clazz.newInstance();
		populateBean(bean, request); 
		return bean;
	}

	public static Object createBean(Class clazz, ServletRequest request) throws IntrospectionException, IllegalAccessException, InvocationTargetException, ConversionException, InstantiationException {
		Object bean = clazz.newInstance();
		populateBean(bean, request); 
		return bean;
	}

	/**
	 * Populates a JavaBean with values from a serlvet request, excluding <code>null</code> values.
	 */
	public static void populateBean (Object bean, ServletRequest request) throws IntrospectionException, IllegalAccessException, InvocationTargetException, ConversionException {
		populateBean (bean, request, false);
	}

	/**
	 * Populates a JavaBean with values from a servlet request.
	 */
	public static void populateBean (Object bean, ServletRequest request, boolean setNullValues) throws IntrospectionException, IllegalAccessException, InvocationTargetException, ConversionException {
		BeanInfo binfo = Introspector.getBeanInfo (bean.getClass());
		PropertyDescriptor[] pds = binfo.getPropertyDescriptors();
		for (int i = 0; i < pds.length; i++) {
			PropertyDescriptor pd = pds[i];	
			String propName = pd.getName();
			String dbValueText = request.getParameter(propName);
			Method setter = pd.getWriteMethod();
			if (setter != null) {
				if (dbValueText != null || setNullValues) {
					if (dbValueText == null && pd.getPropertyType().isPrimitive()) {
						throw new IllegalArgumentException ("Null property value for primitive property " + propName);
					}
					else {
						Object dbValue = convertText(propName, dbValueText, pd.getPropertyType());
						setter.invoke (bean, new Object[] { dbValue });
					}
				}
			}
		}
	}

	private static Object convertText(String propName, String textValue, Class propertyType) throws ConversionException {
		try {
			if (propertyType == String.class) {
				return textValue;
			}
			else if (propertyType == Integer.class || propertyType == int.class) {
				return Integer.valueOf(textValue);
			}
			else if (propertyType == Long.class || propertyType == long.class) {
				return Long.valueOf(textValue);
			}
			else if (propertyType == Short.class || propertyType == short.class) {
				return Short.valueOf(textValue);
			}
			else if (propertyType == Double.class || propertyType == double.class) {
				return Double.valueOf(textValue);
			}
			else if (propertyType == Float.class || propertyType == float.class) {
				return Float.valueOf(textValue);
			}
			else if (propertyType == Byte.class || propertyType == byte.class) {
				return Byte.valueOf(textValue);
			}
			else if (propertyType == Boolean.class || propertyType == boolean.class) {
				return Boolean.valueOf(textValue);
			}
			else if (propertyType == byte[].class) {
				return textValue.getBytes("UTF-8");
			}
			else {
				throw new IllegalArgumentException("Cannot convert text value for property " + propName + " of type " + propertyType.getClass().getName());
			}
		} catch (NumberFormatException nfe) {
			throw new ConversionException(propertyType, propName, "Bad number format for text value '" + textValue + "'");
		} catch (java.io.UnsupportedEncodingException uee) {
			throw new ConversionException(propertyType, propName, "Unable to encode text value '" + textValue + "'");	    
		}
	}
}
