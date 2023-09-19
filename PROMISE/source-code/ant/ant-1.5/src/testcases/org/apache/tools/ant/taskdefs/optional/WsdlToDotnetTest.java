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
 * Tests the WsdlToDotnet task.
 *
 * @author steve loughran
 * @since Ant 1.5
 */
public class WsdlToDotnetTest extends BuildFileTest {

    /**
     * Description of the Field
     */
    private final static String TASKDEFS_DIR = "src/etc/testcases/taskdefs/optional/";


    /**
     * Constructor 
     *
     * @param name testname
     */
    public WsdlToDotnetTest(String name) {
        super(name);
    }


    /**
     * The JUnit setup method
     */
    public void setUp() {
        configureProject(TASKDEFS_DIR + "WsdlToDotnet.xml");
    }


    /**
     * The teardown method for JUnit
     */
    public void tearDown() {
        executeTarget("teardown");
    }


    
    /**
     * A unit test for JUnit
     */
    public void testNoParams() throws Exception {
        expectBuildExceptionContaining("testNoParams",
                "expected failure",
                "destination file must be specified");
    }

    /**
     * A unit test for JUnit
     */
    public void testNoSrc() throws Exception {
        expectBuildExceptionContaining("testNoSrc",
                "expected failure",
                "you must specify either a source file or a URL");
    }

    /**
     * A unit test for JUnit
     */
    public void testDestIsDir() throws Exception {
        expectBuildExceptionContaining("testDestIsDir",
                "expected failure",
                "is a directory");
    }
    
    /**
     * A unit test for JUnit
     */
    public void testBothSrc() throws Exception {
        expectBuildExceptionContaining("testBothSrc",
                "expected failure",
                "both a source file and a URL");
    } 
     /**
     * A unit test for JUnit
     */
    public void testSrcIsDir() throws Exception {
        expectBuildExceptionContaining("testSrcIsDir",
                "expected failure",
                "is a directory");
    } 
    
    /**
     * A unit test for JUnit
     */
    public void testSrcIsMissing() throws Exception {
        expectBuildExceptionContaining("testSrcIsMissing",
                "expected failure",
                "does not exist");
    }
     
    /**
     * A unit test for JUnit
     */
    public void testLocalWsdl() throws Exception {
        executeTarget("testLocalWsdl");
    } 
    /**
     * A unit test for JUnit
     */
    public void testLocalWsdlServer() throws Exception {
        executeTarget("testLocalWsdlServer");
    } 
     /**
     * A unit test for JUnit
     */
    public void testInvalidExtraOps() throws Exception {
        expectBuildExceptionContaining("testInvalidExtraOps",
                "expected failure",
                "WSDL returned: 1");
    }   
}

