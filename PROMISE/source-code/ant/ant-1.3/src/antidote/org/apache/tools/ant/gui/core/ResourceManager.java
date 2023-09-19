/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999, 2000 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Ant", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.tools.ant.gui.core;

import java.util.*;
import java.text.MessageFormat;
import javax.swing.ImageIcon;
import java.net.URL;
import java.io.File;

/**
 * Singleton class for accessing various resources by the application.
 * Relies on the resource bundles for resource values.
 *
 * @version $Revision: 1.6 $ 
 * @author Simeon H.K. Fitch 
 */
public class ResourceManager {

    /** Path to default resources. */
    private static final String RESOURCE_PKG = 
        "org.apache.tools.ant.gui.resources";
    /** Path to default resources. */
    private static final String DEF_PROPERTIES = 
        RESOURCE_PKG + ".antidote";

    /** Image path. */
    private static final String IMG_PATH = 
        '/' + RESOURCE_PKG.replace('.', '/');

    /** Resources to reference. */
    private ResourceBundle _resources = null;

    /** 
     * Default ctor. Uses the default properties file for antidote.
     * 
     */
    public ResourceManager() {
        this(DEF_PROPERTIES);
    }

    /** 
     * Standard ctor.
     * 
     * @param propName Fully qualified name of the resources to use.
     */
    public ResourceManager(String propName) {
        _resources = ResourceBundle.getBundle(propName);
    }

    /** 
     * Generate a composit key from the given class and key name.
     * 
     * @param clazz Class to find resource for.
     * @param name Name of the resource.
     * @return Composite key.
     */
    private String getKey(Class clazz, String name) {
        name = name == null ? "" : name;

        return clazz == null ? name : clazz.getName() + "." + name;
    }

    /** 
     * Get non-qualified String resource.
     * 
     * @param name Name of the resource.
     * @return Value of the resource.
     */
    public String getString(String name) {
        return getString(null, name);
    }

    /** 
     * Get a string resource for the given class.
     * 
     * @param clazz Class to get resource for.
     * @param name Name of the string resource.
     * @return String resource for the given class.
     */
    public String getString(Class clazz, String name) {
        if(name == null) {
            return null;
        }

        try {
            return _resources.getString(getKey(clazz, name));
        }
        catch(MissingResourceException ex) {
            return null;
        }
    }

    /** 
     * Get a non-qualified array of string resources for the given class.
     * 
     * @param name Name of the string resource.
     * @return Array of string resources for the given class.
     */
    public String[] getStringArray(String name) {
        return getStringArray(null, name);
    }
    /** 
     * Get an array of string resources for the given class.
     * 
     * @param clazz Class to get resource for.
     * @param name Name of the string resource.
     * @return Array of string resources for the given class.
     */
    public String[] getStringArray(Class clazz, String name) {
        if(name == null) {
            return null;
        }

        String key = getKey(clazz, name);

        String toTok = null;
        try {
            toTok = _resources.getString(key);
        }
        catch(MissingResourceException ex) {
            // Ignore as we are doing a cascading lookup.
        }

        if(toTok == null) {
            try {
                return _resources.getStringArray(key);
            }
            catch(MissingResourceException ex) {
                return null;
            }
        }
        else {
            StringTokenizer tok = new StringTokenizer(toTok, ", ");
            String[] retval = new String[tok.countTokens()];
            for(int i = 0; i < retval.length; i++) {
                retval[i] = tok.nextToken();
            }
            return retval;
        }
    }

    /** 
     * Get the boolean resource for the given name. Case
     * insensitive values of "yes" or "true" evaluate to TRUE.
     * All others, including undefined resources evaluate to FALSE.
     * 
     * @param name Name of the boolean resource.
     * @return True if defined as true, false otherwise.
     */
    public boolean getBoolean(String name) {
        return getBoolean(null, name);
    }

    /** 
     * Get the boolean resource for the given class. Case
     * insensitive values of "yes" or "true" evaluate to TRUE.
     * All others, including undefined resources evaluate to FALSE.
     * 
     * @param clazz Class to get resource for.
     * @param name Name of the boolean resource.
     * @return True if defined as true, false otherwise.
     */
    public boolean getBoolean(Class clazz, String name) {
        if(name == null) {
            return false;
        }

        String key = getKey(clazz, name);

        String value = "";
        try {
            value = _resources.getString(key);
        }
        catch(MissingResourceException ex) {
            return false;
        }

        return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes");
    }

    /** 
     * Get the non-qualified Class type resource for the given key.
     * 
     * @param name Name of the resourdce.
     * @return Class associated with the key name.
     */
    public Class getClass(String name) 
        throws ClassNotFoundException {
        return getClass(null, name);
    }

    /** 
     * Get the Class type resource for the given class and key name.
     * 
     * @param clazz Class requesting the resource.
     * @param name Name of the resource.
     * @return Class associated with the key name.
     */
    public Class getClass(Class clazz, String name) 
        throws ClassNotFoundException {

        String key = getKey(clazz, name);
        try {
            String value = _resources.getString(key);
            return Class.forName(value);
        }
        catch(MissingResourceException ex) {
            return null;
        }

    }


    /** 
     * Generate a localized message using the given set of arguments to 
     * format the message with.
     * 
     * @param name Name of the message.
     * @param arguments Arguments to the message.
     * @return The formatted message.
     */
    public String getMessage(String name, Object[] arguments) {
        return getMessage(null, name, arguments);
    }

    /** 
     * Generate a localized message using the given set of arguments to 
     * format the message with.
     * 
     * @param clazz Class to get message resource for.
     * @param name Name of the message.
     * @param arguments Arguments to the message.
     * @return The formatted message.
     */
    public String getMessage(Class clazz, String name, Object[] arguments) {
        String format = getString(clazz, name);
        return MessageFormat.format(format, arguments);
    }

    /** 
     * Get the image as an ImageIcon assigned to the given class with the
     * given key.
     * 
     * @param clazz The class to load icon for.
     * @param key The key for looking up the icon.
     * @return Image as an ImageIcon, or null if not found.
     */
    public ImageIcon getImageIcon(Class clazz, String key) {
        return loadImageIcon(getString(clazz, key));
    }

    /** 
     * Get the image as an ImageIcon with the given file name. 
     * For example "open.gif". The image is loaded from the resources package.
     * 
     * @param fileName Image file to load.
     * @return Image as an ImageIcon, or null if not found.
     */
    public ImageIcon loadImageIcon(String fileName) {
        if(fileName == null) return null;

        ImageIcon icon = null;
        URL location = getClass().getResource(IMG_PATH + "/" + fileName);
        if(location != null) {
            icon = new ImageIcon(location);
        }
        return icon;
    }

}
