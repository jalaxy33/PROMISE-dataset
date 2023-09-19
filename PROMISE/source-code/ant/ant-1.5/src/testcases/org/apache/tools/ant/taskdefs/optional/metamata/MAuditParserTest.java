/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
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
package org.apache.tools.ant.taskdefs.optional.metamata;

import java.io.File;

import junit.framework.TestCase;

import org.apache.tools.ant.util.StringUtils;

/**
 * Test for the Audit parser.
 *
 * @author <a href="mailto:sbailliez@apache.org">Stephane Bailliez</a>
 */
public class MAuditParserTest extends TestCase {

    private MAuditParser parser;

    public MAuditParserTest(String s) {
        super(s);
    }

    protected void setUp() {
        parser = new MAuditParser();
    }

    public void testViolation() {
        String line = "file:\\WebGain\\QA\\examples\\auditexamples\\Vector.java:55: Array declarators (\"[]\") should be placed with their component types and not after field/method declarations (5.27).";
        // the replace is done to simulate a platform dependant separator since
        // the parser may do some magic with the file separator
        line = StringUtils.replace(line, "\\", File.separator);
        MAuditParser.Violation violation = parser.parseLine(line);
        assertEquals("\\WebGain\\QA\\examples\\auditexamples\\Vector.java",
                StringUtils.replace(violation.file, File.separator, "\\"));
        assertEquals("55", violation.line);
        assertEquals("Array declarators (\"[]\") should be placed with their component types and not after field/method declarations (5.27).", violation.error);
    }

    public void testNonViolation(){
        String line = "Audit completed with 36 violations.";
        Object violation = parser.parseLine(line);
        assertNull(violation);
    }

    public void testFilePathInViolation(){
        String line = "file:\\WebGain\\QA\\examples\\auditexamples\\Hashtable.java:302: Loop variable defined at file:\\WebGain\\QA\\examples\\auditexamples\\Hashtable.java:300 is being modified (5.16).";
        line = StringUtils.replace(line, "\\", File.separator);
        MAuditParser.Violation violation = parser.parseLine(line);
        assertEquals("\\WebGain\\QA\\examples\\auditexamples\\Hashtable.java",
                StringUtils.replace(violation.file, File.separator, "\\"));
        assertEquals("302", violation.line);
        assertEquals("Loop variable defined at Hashtable.java:300 is being modified (5.16).", violation.error);
    }

}
