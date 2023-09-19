/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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
package org.apache.tools.ant.gui.customizer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.lang.reflect.Method;
import javax.swing.border.BevelBorder;

/**
 * Custom property editor for generic Object types. Useful for 
 * complex objects where using the DynamicCustomizer may be useful.
 * 
 * @version $Revision: 1.1 $ 
 * @author Simeon Fitch 
 */
public class ObjectPropertyEditor extends AbstractPropertyEditor {
    /** Area for typing in the file name. */
    private JTextField _widget = null;
    /** Container for the editor. */
    private JPanel _container = null;
    /** The current object value. */
    private Object _value = null;
    /** The editing button. */
    private JButton _button = null;
    /** Flag to indicate that cancellation of editing is supported. */
    private boolean _supportCancel = true;
    /** Original value. Only used if _supportCancel is true. */
    private Object _original = null;

    /** 
     * Default ctor.
     * 
     */
    public ObjectPropertyEditor() {
        _container = new JPanel(new BorderLayout());
        
        _widget = new JTextField(25);
        _widget.setEditable(false);
        _widget.addFocusListener(new FocusHandler(this));
        _widget.setBorder(
            BorderFactory.createBevelBorder(BevelBorder.LOWERED));

        _container.add(_widget, BorderLayout.CENTER);

        _button = new JButton("Edit...");
        _button.addActionListener(new ActionHandler());
        _container.add(_button, BorderLayout.EAST);
    }

    /** 
     * Get the child editing component. Uses JComponent so we can have tool
     * tips, etc.
     * 
     * @return Child editing component.
     */
    protected Component getChild() {
        return _container;
    }


    /**
     * This method is intended for use when generating Java code to set
     * the value of the property.  It should return a fragment of Java code
     * that can be used to initialize a variable with the current property
     * value.
     * <p>
     * Example results are "2", "new Color(127,127,34)", "Color.orange", etc.
     *
     * @return A fragment of Java code representing an initializer for the
     *      current value.
     */
    public String getJavaInitializationString() {
        return null;
    }

    /**
     * Set (or change) the object that is to be edited.  Builtin types such
     * as "int" must be wrapped as the corresponding object type such as
     * "java.lang.Integer".
     *
     * @param value The new target object to be edited.  Note that this
     *     object should not be modified by the PropertyEditor, rather 
     *     the PropertyEditor should create a new object to hold any
     *     modified value.
     * @exception IllegalArgumentException thrown if value can't be cloned.
     */
    public void setValue(Object value) {

        if(_supportCancel && value != _value) {
            try {
                _value = makeClone(value);
            }
            catch(CloneNotSupportedException ex){
                // If cloning doesn't work then we can't support a "cancel"
                // option on the editing dialog.
                _supportCancel = false;
            }
            _original = value;
        }
        
        _value = value;

        _button.setEnabled(_value != null);

        _widget.setText(getAsString(_value));
    }

    /** 
     * Convert the given value into some appropriate string. NB: This method
     * can be continually improved to be made more and more smart over time.
     * 
     * @param value Value to convert.
     * @return String value to display.
     */
    private String getAsString(Object value) {
        String retval = null;
        if(value == null) {
            retval = "<null>";
        }

        // We try to be smart by querying for various, logical string 
        // representation of the value.
        if(retval == null) {
            try {
                Method m = value.getClass().getMethod("getName", null);
                retval = (String) m.invoke(value, null);
            }
            catch(Exception ex) {
            }
        }
        if(retval == null) {
            try {
                Method m = value.getClass().getMethod("getLabel", null);
                retval = (String) m.invoke(value, null);
            }
            catch(Exception ex) {
            }
            
        }
        if(retval == null) {
            try {
                Method m = value.getClass().getMethod("getText", null);
                retval = (String) m.invoke(value, null);
            }
            catch(Exception ex) {
            }
        }

        if(retval == null) {
            retval = value.toString();
        }

        if(retval.length() > 256) {
            retval = retval.substring(0, 253) + "...";
        }

        return retval;
    }

    /** 
     * Attampt to make a clone of the given value.
     * 
     * @param value Value to clone.
     * @return Cloned value, or null if value given was null.
     * @exception IllegalArgumentException thrown if value can't be cloned.
     */
    private Object makeClone(Object value) throws CloneNotSupportedException {
        Object retval = null;
        if(value != null) {
            try {
                Method m = value.getClass().getMethod("clone", null);
                retval = m.invoke(value, null);
            }
            catch(Throwable ex) {
                throw new CloneNotSupportedException(
                    "This editor only supports types that have publically " +
                    "accessible clone() methods.\n" + 
                    value.getClass().getName() + 
                    " does not have such a method.");
            }
        }
        return retval;
    }

    /**
     * @return The value of the property.  Builtin types
     * such as "int" will be wrapped as the corresponding
     * object type such as "java.lang.Integer".  */
    public Object getValue() {
        return _value;
    }

    /**
     * Set the property value by parsing a given String.  May raise
     * java.lang.IllegalArgumentException if either the String is
     * badly formatted or if this kind of property can't be expressed
     * as text.
     * @param text  The string to be parsed.
     */
    public void setAsText(String text) throws IllegalArgumentException {
        throw new IllegalArgumentException("String conversion not supported.");
    }

    /**
     * @return The property value as a human editable string.
     * <p>   Returns null if the value can't be expressed 
     *       as an editable string.
     * <p>   If a non-null value is returned, then the PropertyEditor should
     *       be prepared to parse that string back in setAsText().
     */
    public String getAsText() {
        return null;
    } 

    /** Handler for presses of the edit button. */
    private class ActionHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if(_value == null) return;
            Class type = _value.getClass();
            DynamicCustomizer c = new DynamicCustomizer(type);
            c.setObject(_value);

            int returnVal = JOptionPane.OK_OPTION;
            if(_supportCancel) {
                returnVal = JOptionPane.showConfirmDialog(
                    getChild(), new JScrollPane(c), "Editing...", 
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            }
            else {
                JOptionPane.showMessageDialog(
                    getChild(), new JScrollPane(c), "Editing...",
                    JOptionPane.PLAIN_MESSAGE);
                returnVal = JOptionPane.OK_OPTION;
            }

            if(returnVal == JOptionPane.OK_OPTION) {
                Object oldValue = _original;
                Object newValue = _value;

                setValue(newValue);
                firePropertyChange(oldValue, newValue);
            } 
            else {
                try {
                    _value = makeClone(_original);
                }
                catch(CloneNotSupportedException ex) {
                    // XXX log me. Shouldn't have gotten here as
                    // the test for cloneability should have already been done.
                    ex.printStackTrace();
                    _supportCancel = false;
                }
            }
        }
    }

}


