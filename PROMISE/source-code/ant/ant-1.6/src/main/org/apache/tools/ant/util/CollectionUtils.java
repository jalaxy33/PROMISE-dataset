/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002-2003 The Apache Software Foundation.  All rights
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
 * 4. The names "Ant" and "Apache Software
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
package org.apache.tools.ant.util;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Vector;

/**
 * A set of helper methods related to collection manipulation.
 *
 * @author Stefan Bodewig
 * @author <a href="mailto:stein@xtramind.com">Ingmar Stein</a>
 * @author  <a href="mailto:martijn@kruithof.xs4all.nl">Martijn Kruithof</a>
 *
 * @since Ant 1.5
 */
public class CollectionUtils {

    /**
     * Please use Vector.equals() or List.equals() 
     *
     * @since Ant 1.5
     * @deprecated
     */
    public static boolean equals(Vector v1, Vector v2) {
        if (v1 == v2) {
            return true;
        }

        if (v1 == null || v2 == null) {
            return false;
        }

        return v1.equals(v2);
    }

    /**
     * Dictionary does not have an equals.
     * Please use  Map.equals()
     *
     * <p>Follows the equals contract of Java 2's Map.</p>
     *
     * @since Ant 1.5
     * @deprecated
     */
    public static boolean equals(Dictionary d1, Dictionary d2) {
        if (d1 == d2) {
            return true;
        }

        if (d1 == null || d2 == null) {
            return false;
        }

        if (d1.size() != d2.size()) {
            return false;
        }

        Enumeration e1 = d1.keys();
        while (e1.hasMoreElements()) {
            Object key = e1.nextElement();
            Object value1 = d1.get(key);
            Object value2 = d2.get(key);
            if (value2 == null || !value1.equals(value2)) {
                return false;
            }
        }

        // don't need the opposite check as the Dictionaries have the
        // same size, so we've also covered all keys of d2 already.

        return true;
    }

    /**
     * Dictionary does not know the putAll method. Please use Map.putAll().
     *
     * @since Ant 1.6
     * @deprecated
     */
    public static void putAll(Dictionary m1, Dictionary m2) {
        for (Enumeration it = m2.keys(); it.hasMoreElements();) {
            Object key = it.nextElement();
            m1.put(key, m2.get(key));
        }
    }

    /**
     * @since Ant 1.6
     */
    public static final class EmptyEnumeration implements Enumeration {
        public EmptyEnumeration() {
        }

        public boolean hasMoreElements() {
            return false;
        }

        public Object nextElement() throws NoSuchElementException {
            throw new NoSuchElementException();
        }
    }

}
