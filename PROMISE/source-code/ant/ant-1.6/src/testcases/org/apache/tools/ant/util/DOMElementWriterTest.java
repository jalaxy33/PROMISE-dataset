/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000-2003 The Apache Software Foundation.  All rights
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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for org.apache.tools.ant.util.DOMElementWriter.
 *
 * @author Stefan Bodewig 
 */
public class DOMElementWriterTest extends TestCase {

    private DOMElementWriter w = new DOMElementWriter();

    public DOMElementWriterTest(String name) {
        super(name);
    }

    public void testIsReference() {
        assertTrue("&#20;", w.isReference("&#20;"));
        assertTrue("&#x20;", w.isReference("&#x20;"));
        assertTrue("&#xA0;", w.isReference("&#xA0;"));
        assertTrue("&#A0;", !w.isReference("&#A0;"));
        assertTrue("20;", !w.isReference("20;"));
        assertTrue("&#20", !w.isReference("&#20"));
        assertTrue("&quot;", w.isReference("&quot;"));
        assertTrue("&apos;", w.isReference("&apos;"));
        assertTrue("&gt;", w.isReference("&gt;"));
        assertTrue("&lt;", w.isReference("&lt;"));
        assertTrue("&amp;", w.isReference("&amp;"));
    }

    public void testEncode() {
        assertEquals("&#20;", w.encode("&#20;"));
        assertEquals("&#x20;", w.encode("&#x20;"));
        assertEquals("&#xA0;", w.encode("&#xA0;"));
        assertEquals("&amp;#A0;", w.encode("&#A0;"));
        assertEquals("20;", w.encode("20;"));
        assertEquals("&amp;#20", w.encode("&#20"));
        assertEquals("&quot;", w.encode("&quot;"));
        assertEquals("&apos;", w.encode("&apos;"));
        assertEquals("&gt;", w.encode("&gt;"));
        assertEquals("&lt;", w.encode("&lt;"));
        assertEquals("&amp;", w.encode("&amp;"));
        assertEquals("&quot;", w.encode("\""));
        assertEquals("&lt;", w.encode("<"));
        assertEquals("&amp;", w.encode("&"));
        assertEquals("", w.encode("\u0017"));
        assertEquals("&#20;\"20;&", w.encodedata("&#20;\"20;&"));
        assertEquals("", w.encodedata("\u0017"));
    }

    public void testIsLegalCharacter() {
        assertTrue("0x00", !w.isLegalCharacter('\u0000'));
        assertTrue("0x09", w.isLegalCharacter('\t'));
        assertTrue("0x0A", w.isLegalCharacter('\n'));
        assertTrue("0x0C", w.isLegalCharacter('\r'));
        assertTrue("0x1F", !w.isLegalCharacter('\u001F'));
        assertTrue("0x20", w.isLegalCharacter('\u0020'));
        assertTrue("0xD7FF", w.isLegalCharacter('\uD7FF'));
        assertTrue("0xD800", !w.isLegalCharacter('\uD800'));
        assertTrue("0xDFFF", !w.isLegalCharacter('\uDFFF'));
        assertTrue("0xE000", w.isLegalCharacter('\uE000'));
        assertTrue("0xFFFD", w.isLegalCharacter('\uFFFD'));
        assertTrue("0xFFFE", !w.isLegalCharacter('\uFFFE'));
    }

    public void testCDATAEndEncoding() {
        assertEquals("]>", w.encodedata("]>"));
        assertEquals("]]", w.encodedata("]]"));
        assertEquals("&#x5d;&#x5d;&gt;", w.encodedata("]]>"));
        assertEquals("&#x5d;&#x5d;&gt;A", w.encodedata("]]>A"));
        assertEquals("A&#x5d;&#x5d;&gt;", w.encodedata("A]]>"));
        assertEquals("A&#x5d;&#x5d;&gt;A", w.encodedata("A]]>A"));
        assertEquals("A&#x5d;&#x5d;&gt;B&#x5d;&#x5d;&gt;C",
                     w.encodedata("A]]>B]]>C"));
    }
}
