/*
 *  The Apache Software License, Version 1.1
 *
 *  Copyright (c) 2002-2003 The Apache Software Foundation.  All rights
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
 *  4. The names "Ant" and "Apache Software
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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildFileTest;

/**
 * Tests the XMLValidate optional task, by running targets in the test script
 * <code>src/etc/testcases/taskdefs/optional/xmlvalidate.xml</code>
 * <p>
 *
 * @see XmlValidateCatalogTest
 * @author steve loughran
 * @author Jeff Turner
 * @since Ant 1.5
 */
public class XmlValidateTest extends BuildFileTest {

    /**
     * where tasks run
     */
    private final static String TASKDEFS_DIR = "src/etc/testcases/taskdefs/optional/";


    /**
     * Constructor 
     *
     * @param name testname
     */
    public XmlValidateTest(String name) {
        super(name);
    }


    /**
     * The JUnit setup method
     */
    public void setUp() {
        configureProject(TASKDEFS_DIR + "xmlvalidate.xml");
    }


    /**
     * The teardown method for JUnit
     */
    public void tearDown() {

    }


    /**
     * Basic inline 'dtd' element test.
     */
    public void testValidate() throws Exception {
         executeTarget("testValidate");
    }
    

    /**
     * Test indirect validation.
     */
    public void testDeepValidate() throws Exception {
         executeTarget("testDeepValidate");
    }

    /**
     *
     */
    public void testXmlCatalog() {
        executeTarget("xmlcatalog");
    }

    /**
     *
     */
    public void testXmlCatalogViaRefid() {
        executeTarget("xmlcatalogViaRefid");
    }

    /**
     * Test that the nested dtd element is used when resolver.jar is not
     * present.  This test should pass either way.
     */
    public void testXmlCatalogFiles() {
        executeTarget("xmlcatalogfiles-override");
    }

    /**
     * Test nested catalogpath.
     * Test that the nested dtd element is used when resolver.jar is not
     * present.  This test should pass either way.
     */
    public void testXmlCatalogPath() {
        executeTarget("xmlcatalogpath-override");
    }

    /**
     * Test nested xmlcatalog definitions
     */
    public void testXmlCatalogNested() {
        executeTarget("xmlcatalognested");
    }

    /**
     * Test xml schema validation
     */
    public void testXmlSchemaGood() throws BuildException {
        try {
            executeTarget("testSchemaGood");
        } catch (BuildException e) {
            if (e.getMessage()
                .endsWith(" doesn't recognize feature http://apache.org/xml/features/validation/schema") ||
                e.getMessage()
                .endsWith(" doesn't support feature http://apache.org/xml/features/validation/schema")) {
                System.err.println(" skipped, parser doesn't support schema");
            } else {
                throw e;
            }
        }
    }
    /**
     * Test xml schema validation
     */
    public void testXmlSchemaBad() {
        try {
            executeTarget("testSchemaBad");
            fail("Should throw BuildException because 'Bad Schema Validation'");

            expectBuildExceptionContaining("testSchemaBad",
                                           "Bad Schema Validation", 
                                           "not a valid XML document");
        } catch (BuildException e) {
            if (e.getMessage()
                .endsWith(" doesn't recognize feature http://apache.org/xml/features/validation/schema") ||
                e.getMessage()
                .endsWith(" doesn't support feature http://apache.org/xml/features/validation/schema")) {
                System.err.println(" skipped, parser doesn't support schema");
            } else {
                assertTrue(e.getMessage()
                           .indexOf("not a valid XML document") > -1);
            }
        }
    }

    /**
     * iso-2022-jp.xml is valid but wouldn't get recognized on systems
     * with a different native encoding.
     *
     * Bug 11279
     */
    public void testIso2022Jp() {
        executeTarget("testIso2022Jp");
    }

    /**
     * utf-8.xml is invalid as it contains non-UTF-8 characters, but
     * would pass on systems with a native iso-8859-1 (or similar)
     * encoding.
     *
     * Bug 11279
     */
    public void testUtf8() {
        expectBuildException("testUtf8", "invalid characters in file");
    }

}
