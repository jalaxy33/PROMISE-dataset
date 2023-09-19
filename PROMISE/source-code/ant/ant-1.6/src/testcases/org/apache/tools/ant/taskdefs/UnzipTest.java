/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000-2001,2003 The Apache Software Foundation.  All rights
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
import org.apache.tools.ant.BuildFileTest;
import org.apache.tools.ant.util.FileUtils;

/**
 * @author Nico Seessle <nico@seessle.de> 
 * @author Stefan Bodewig
 */
public class UnzipTest extends BuildFileTest { 
    
    public UnzipTest(String name) { 
        super(name);
    }    
    
    public void setUp() { 
        configureProject("src/etc/testcases/taskdefs/unzip.xml");
    }
    
    public void tearDown() {
        executeTarget("cleanup");
    }

    public void test1() { 
        expectBuildException("test1", "required argument not specified");
    }

    public void test2() { 
        expectBuildException("test2", "required argument not specified");
    }

    public void test3() { 
        expectBuildException("test3", "required argument not specified");
    }


    public void testRealTest() throws java.io.IOException {
        FileUtils fileUtils = FileUtils.newFileUtils();
        executeTarget("realTest");
        assertTrue(fileUtils.contentEquals(project.resolveFile("../asf-logo.gif"),
                                           project.resolveFile("asf-logo.gif")));
    }
    
    public void testTestZipTask() throws java.io.IOException {
        FileUtils fileUtils = FileUtils.newFileUtils();
        executeTarget("testZipTask");
        assertTrue(fileUtils.contentEquals(project.resolveFile("../asf-logo.gif"),
                                           project.resolveFile("asf-logo.gif")));
    }
    
    public void testTestUncompressedZipTask() throws java.io.IOException {
        FileUtils fileUtils = FileUtils.newFileUtils();
        executeTarget("testUncompressedZipTask");
        assertTrue(fileUtils.contentEquals(project.resolveFile("../asf-logo.gif"),
                                           project.resolveFile("asf-logo.gif")));
    }
    
    /*
     * PR 11100
     */
    public void testPatternSetExcludeOnly() {
        executeTarget("testPatternSetExcludeOnly");
        assertTrue("1/foo is excluded",
                   !getProject().resolveFile("unziptestout/1/foo").exists());
        assertTrue("2/bar is not excluded",
                   getProject().resolveFile("unziptestout/2/bar").exists());
    }

    /*
     * PR 11100
     */
    public void testPatternSetIncludeOnly() {
        executeTarget("testPatternSetIncludeOnly");
        assertTrue("1/foo is not included",
                   !getProject().resolveFile("unziptestout/1/foo").exists());
        assertTrue("2/bar is included",
                   getProject().resolveFile("unziptestout/2/bar").exists());
    }

    /*
     * PR 11100
     */
    public void testPatternSetIncludeAndExclude() {
        executeTarget("testPatternSetIncludeAndExclude");
        assertTrue("1/foo is not included",
                   !getProject().resolveFile("unziptestout/1/foo").exists());
        assertTrue("2/bar is excluded",
                   !getProject().resolveFile("unziptestout/2/bar").exists());
    }

    /*
     * PR 16213
     */
    public void testSelfExtractingArchive() {
        executeTarget("selfExtractingArchive");
    }


    /*
     * PR 20969
     */
    public void testPatternSetSlashOnly() {
        executeTarget("testPatternSetSlashOnly");
        assertTrue("1/foo is not included",
                   !getProject().resolveFile("unziptestout/1/foo").exists());
        assertTrue("2/bar is included",
                   getProject().resolveFile("unziptestout/2/bar").exists());
    }

    /*
     * PR 10504
     */
    public void testEncoding() {
        executeTarget("encodingTest");
        assertTrue("foo has been properly named",
                   getProject().resolveFile("unziptestout/foo").exists());
    }

}
