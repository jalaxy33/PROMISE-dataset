/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.util;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class IntrospectionSupport {
        
	
    static public boolean getProperties(Object target, Map props, String optionPrefix) {
    	
        boolean rc = false;
        if( target == null )
            throw new IllegalArgumentException("target was null.");
        if( props == null )
            throw new IllegalArgumentException("props was null.");
        
        if( optionPrefix == null )
        	optionPrefix="";
        
        Class clazz = target.getClass();
        Method[] methods = clazz.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            String name = method.getName();
            Class type = method.getReturnType();
            Class params[] = method.getParameterTypes();
            if( name.startsWith("get") && params.length==0 && 
            		type!=null && isSettableType(type)) {

            	try {
            		
            		Object value = method.invoke(target, new Object[]{});
            		if( value == null )
            			continue;
            		
            		String strValue = convertToString(value, type);
            		if( strValue ==null )
            			continue;
            		
                    name = name.substring(3,4).toLowerCase()+name.substring(4);
                    props.put(optionPrefix+name, strValue);
                    rc = true;
                    
            	} catch ( Throwable ignore) {
            	}
            	
            }
        }
        
        return rc;
    }
	
	
	
    static public boolean setProperties(Object target, Map props, String optionPrefix) {
        boolean rc = false;
        if( target == null )
            throw new IllegalArgumentException("target was null.");
        if( props == null )
            throw new IllegalArgumentException("props was null.");
        
        for (Iterator iter = props.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            if( name.startsWith(optionPrefix) ) {
                Object value = props.get(name);
                name = name.substring(optionPrefix.length());
                if( setProperty(target, name, value) ) {
                    iter.remove();
                    rc = true;
                }
            }
        }
        return rc;
    }
    
    public static Map extractProperties(Map props, String optionPrefix) {
        if( props == null )
            throw new IllegalArgumentException("props was null.");

        HashMap rc = new HashMap(props.size());
        
        for (Iterator iter = props.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            if( name.startsWith(optionPrefix) ) {
                Object value = props.get(name);
                name = name.substring(optionPrefix.length());
                rc.put(name, value);
                iter.remove();
            }
        }
        
        return rc;
    }
          
    public static boolean setProperties(Object target, Map props) {
    	boolean rc = false;
    	
        if( target == null )
            throw new IllegalArgumentException("target was null.");
        if( props == null )
            throw new IllegalArgumentException("props was null.");
        
        for (Iterator iter = props.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Entry) iter.next();
            if( setProperty(target, (String) entry.getKey(), entry.getValue()) ) {
                iter.remove();
                rc=true;
            }
        }
        
        return rc;
    }

    public static boolean setProperty(Object target, String name, Object value) {
        try {
            Class clazz = target.getClass();
            Method setter = findSetterMethod(clazz, name);
            if( setter == null )
                return false;
            
            // If the type is null or it matches the needed type, just use the value directly
            if( value == null || value.getClass()==setter.getParameterTypes()[0] ) {
                setter.invoke(target, new Object[]{value});
            } else {
                // We need to convert it
                setter.invoke(target, new Object[]{ convert(value, setter.getParameterTypes()[0]) });
            }
            return true;
        } catch (Throwable ignore) {
            return false;
        }
    }

    private static Object convert(Object value, Class type) throws URISyntaxException {
        PropertyEditor editor = PropertyEditorManager.findEditor(type);
        if( editor != null ) { 
            editor.setAsText(value.toString());
            return editor.getValue();
        }
        if( type == URI.class ) {
            return new URI(value.toString());
        }
        return null;
    }

    private static String convertToString(Object value, Class type) throws URISyntaxException {
        PropertyEditor editor = PropertyEditorManager.findEditor(type);
        if( editor != null ) { 
            editor.setValue(value);
            return editor.getAsText();
        }
        if( type == URI.class ) {
            return ((URI)value).toString();
        }
        return null;
    }

    private static Method findSetterMethod(Class clazz, String name) {
        // Build the method name.
        name = "set"+name.substring(0,1).toUpperCase()+name.substring(1);
        Method[] methods = clazz.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            Class params[] = method.getParameterTypes();
            if( method.getName().equals(name) 
                    && params.length==1
                    && isSettableType(params[0])) {
                return method;
            }
        }
        return null;
    }

    private static boolean isSettableType(Class clazz) {
        if( PropertyEditorManager.findEditor(clazz)!=null )
            return true;
        if( clazz == URI.class )
            return true;
        if( clazz == Boolean.class )
            return true;
        return false;
    }

    static public String toString(Object target) {
        return toString(target, Object.class);
    }

    static public String toString(Object target, Class stopClass) {
        LinkedHashMap map = new LinkedHashMap();
        addFields(target, target.getClass(), stopClass, map);
        StringBuffer buffer = new StringBuffer(simpleName(target.getClass()));
        buffer.append(" {");
        Set entrySet = map.entrySet();
        boolean first = true;
        for (Iterator iter = entrySet.iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            if (first) {
                first = false;
            }
            else {
                buffer.append(", ");
            }
            buffer.append(entry.getKey());
            buffer.append(" = ");
            appendToString(buffer, entry.getValue());
        }
        buffer.append("}");
        return buffer.toString();
    }

    protected static void appendToString(StringBuffer buffer, Object value) {
//        if (value instanceof ActiveMQDestination) {
//            ActiveMQDestination destination = (ActiveMQDestination) value;
//            buffer.append(destination.getQualifiedName());
//        }
//        else {
            buffer.append(value);
//        }
    }

    static public String simpleName(Class clazz) {
        String name = clazz.getName();
        int p = name.lastIndexOf(".");
        if( p >= 0 ) {
            name = name.substring(p+1);
        }
        return name;
    }
    

    static private void addFields(Object target, Class startClass, Class stopClass, LinkedHashMap map) {
        
        if( startClass!=stopClass ) 
            addFields( target, startClass.getSuperclass(), stopClass, map );
        
        Field[] fields = startClass.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            if( Modifier.isStatic(field.getModifiers()) || 
                Modifier.isTransient(field.getModifiers()) ||
                Modifier.isPrivate(field.getModifiers())  ) {
                continue;
            }
            
            try {
                field.setAccessible(true);
                Object o = field.get(target);
                if( o!=null && o.getClass().isArray() ) {
                    try {
                        o = Arrays.asList((Object[]) o);
                    } catch (Throwable e) {
                    }
                }
                map.put(field.getName(), o);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        
    }

    
}
