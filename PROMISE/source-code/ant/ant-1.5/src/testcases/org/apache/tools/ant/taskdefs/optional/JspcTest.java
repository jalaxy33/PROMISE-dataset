/*
 *  The Apache Software License, Version 1.1
 *
 *  Copyright (c) 2002 The Apache Software Foundation.  All rights
 *  reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in
 *  the documentation and/or other materials provided with the
 *  distribution.
 *
 *  3. The end-user documentation included with the redistribution, if
 *  any, must include the following acknowlegement:
 *  "This product includes software developed by the
 *  Apache Software Foundation (http://www.apache.org/)."
 *  Alternately, this acknowlegement may appear in the software itself,
 *  if and wherever such third-party acknowlegements normally appear.
 *
 *  4. The names "The Jakarta Project", "Ant", and "Apache Software
 *  Foundation" must not be used to endorse or promote products derived
 *  from this software without prior written permission. For written
 *  permission, please contact apache@apache.org.
 *
 *  5. Products derived from this software may not be called "Apache"
 *  nor may "Apache" appear in their names without prior written
 *  permission of the Apache Group.
 *
 *  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 *  ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *  USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 *  OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 *  SUCH DAMAGE.
 *  ====================================================================
 *
 *  This software consists of voluntary contributions made by many
 *  individuals on behalf of the Apache Software Foundation.  For more
 *  information on the Apache Software Foundation, please see
 *  <http://www.apache.org/>.
 */
package org.apache.tools.ant.taskdefs.optional;

import java.io.*;
import java.util.Properties;

import org.apache.tools.ant.BuildFileTest;

/**
 * Tests the Jspc task.
 *
 * @author slo
 * @created 07 March 2002
 * @since Ant 1.5
 */
public class JspcTest extends BuildFileTest {
    /**
     * Description of the Field
     */
    private File baseDir;
    /**
     * Description of the Field
     */
    private File outDir;

    /**
     * Description of the Field
     */
    private final static String TASKDEFS_DIR = "src/etc/testcases/taskdefs/optional/";


    /**
     * Constructor for the JspcTest object
     *
     * @param name Description of Parameter
     */
    public JspcTest(String name) {
        super(name);
    }


    /**
     * The JUnit setup method
     */
    public void setUp() {
        configureProject(TASKDEFS_DIR + "jspc.xml");
        baseDir = new File(TASKDEFS_DIR);
        outDir = new File(baseDir, "jsp/java");
    }


    /**
     * The teardown method for JUnit
     */
    public void tearDown() {
        executeTarget("cleanup");
    }


    /**
     * A unit test for JUnit
     */
    public void testSimple() throws Exception {
        executeJspCompile("testSimple", "simple.java");
    }


    /**
     * A unit test for JUnit
     */
    public void testUriroot() throws Exception {
        executeJspCompile("testUriroot", "uriroot.java");
    }


    /**
     * A unit test for JUnit
     */
    public void testXml() throws Exception {
        executeJspCompile("testXml", "xml.java");
    }


    /**
     * try a keyword in a file
     */
    public void testKeyword() throws Exception {
        executeJspCompile("testKeyword", "default_00025.java");
    }


    /**
     * what happens to 1nvalid-classname
     */
    public void testInvalidClassname() throws Exception {
        executeJspCompile("testInvalidClassname", 
                "_00031nvalid_0002dclassname.java");
    }

    
    /**
     * A unit test for JUnit
     */
    public void testNoTld() throws Exception {
        expectBuildExceptionContaining("testNoTld",
                "Jasper found an error in a file",
                "Java returned: 9");
    }



    /**
     * run a target then verify the named file gets created
     *
     * @param target Description of Parameter
     * @param javafile Description of Parameter
     * @exception Exception trouble
     */
    protected void executeJspCompile(String target, String javafile)
        throws Exception {
        executeTarget(target);
        assertJavaFileCreated(javafile);
    }


    /**
     * verify that a named file was created
     *
     * @param filename Description of Parameter
     * @exception Exception trouble
     */
    protected void assertJavaFileCreated(String filename)
        throws Exception {
        File file = getOutputFile(filename);
        assertTrue("file " + filename + " not found", file.exists());
        assertTrue("file " + filename + " is empty", file.length() > 0);
    }


    /**
     * Gets the OutputFile attribute of the JspcTest object
     *
     * @param subpath Description of Parameter
     * @return The OutputFile value
     */
    protected File getOutputFile(String subpath) {
        return new File(outDir, subpath);
    }
}

