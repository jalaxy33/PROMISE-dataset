/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
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

package org.apache.tools.ant.taskdefs;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildFileTest;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.Project;

/**
 * @author Conor MacNeill
 */
public class ImportTest extends BuildFileTest {

    public ImportTest(String name) {
        super(name);
    }

    public void setUp() {
    }

    public void tearDown() {
    }

    public void testSimpleImport() {
        configureProject("src/etc/testcases/taskdefs/import/import.xml");
        assertLogContaining("Before importIn imported topAfter import");
    }

    public void testUnnamedNesting() {
        configureProject("src/etc/testcases/taskdefs/import/unnamedImport.xml",
                         Project.MSG_WARN);
        String log = getLog();
        assertTrue("Warnings logged when not expected: " + log,
                    log.length() == 0);
    }

    public void testSerial() {
        configureProject("src/etc/testcases/taskdefs/import/subdir/serial.xml");
        assertLogContaining(
            "Unnamed2.xmlUnnamed1.xmlSkipped already imported file");
    }

    // allow this as imported in targets are only tested when a target is run
    public void testImportInTargetNoEffect() {
        configureProject("src/etc/testcases/taskdefs/import/subdir/importintarget.xml");
        expectPropertyUnset("no-import", "foo");
        assertTrue(null == getProject().getReference("baz"));
    }

    // deactivate this test as imports within targets are not allowed
    public void notTestImportInTargetWithEffect() {
        configureProject("src/etc/testcases/taskdefs/import/subdir/importintarget.xml");
        expectPropertySet("do-import", "foo", "bar");
        assertNotNull(getProject().getReference("baz"));
    }
    
    public void testImportInTargetNotAllowed() {
        configureProject(
            "src/etc/testcases/taskdefs/import/subdir/importintarget.xml");
        expectBuildExceptionContaining(
            "do-import", "not a top level task",
            "import only allowed as a top-level task");
    }

    public void testImportInSequential() {
        configureProject(
            "src/etc/testcases/taskdefs/import/subdir/importinsequential.xml");
        expectPropertySet("within-imported", "foo", "bar");
        assertNotNull(getProject().getReference("baz"));
    }

    public void testImportError() {
        try {
            configureProject(
                "src/etc/testcases/taskdefs/import/import_bad_import.xml");
        } catch (BuildException ex) {
            Location lo = ex.getLocation();
            assertTrue(
                "expected location of build exception to be set",
                (lo != null));
            assertTrue(
                "expected location to contain calling file",
                lo.getFileName().indexOf("import_bad_import.xml") != -1);
            assertTrue(
                "expected message of ex to contain called file",
                ex.getMessage().indexOf("bad.xml") != -1);
            return;
        }
        assertTrue(
            "Did not see build exception",
            false);
    }
}

