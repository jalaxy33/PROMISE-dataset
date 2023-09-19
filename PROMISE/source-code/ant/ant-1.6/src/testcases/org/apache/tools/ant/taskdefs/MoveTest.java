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

package org.apache.tools.ant.taskdefs;

import org.apache.tools.ant.BuildFileTest;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.util.FileUtils;
import java.io.File;
import java.io.IOException;

/**
 * Tests the Move task.
 *
 * @author Magesh Umasankar
 */
public class MoveTest extends BuildFileTest {

    public MoveTest(String name) {
        super(name);
    }

    public void setUp() {
        configureProject("src/etc/testcases/taskdefs/move.xml");
    }

    public void tearDown() {
        executeTarget("cleanup");
    }

    public void testFilterSet() throws IOException {
        executeTarget("testFilterSet");
        FileUtils fileUtils = FileUtils.newFileUtils();
        File tmp  = new File(getProjectDir(), "move.filterset.tmp");
        File check  = new File(getProjectDir(), "expected/copy.filterset.filtered");
        assertTrue(tmp.exists());
        assertTrue(fileUtils.contentEquals(tmp, check));
    }

    public void testFilterChain() throws IOException {
        executeTarget("testFilterChain");
        FileUtils fileUtils = FileUtils.newFileUtils();
        File tmp  = new File(getProjectDir(), "move.filterchain.tmp");
        File check  = new File(getProjectDir(), "expected/copy.filterset.filtered");
        assertTrue(tmp.exists());
        assertTrue(fileUtils.contentEquals(tmp, check));
    }

    /** Bugzilla Report 11732 */
    public void testDirectoryRemoval() throws IOException {
        executeTarget("testDirectoryRemoval");
        assertTrue(!getProject().resolveFile("E/B/1").exists());
        assertTrue(getProject().resolveFile("E/C/2").exists());
        assertTrue(getProject().resolveFile("E/D/3").exists());
        assertTrue(getProject().resolveFile("A/B/1").exists());
        assertTrue(!getProject().resolveFile("A/C/2").exists());
        assertTrue(!getProject().resolveFile("A/D/3").exists());
        assertTrue(!getProject().resolveFile("A/C").exists());
        assertTrue(!getProject().resolveFile("A/D").exists());
    }

    /** Bugzilla Report 18886 */
    public void testDirectoryRetaining() throws IOException {
        executeTarget("testDirectoryRetaining");
        assertTrue(getProject().resolveFile("E").exists());
        assertTrue(getProject().resolveFile("E/1").exists());
        assertTrue(!getProject().resolveFile("A/1").exists());
        assertTrue(getProject().resolveFile("A").exists());
    }

    public void testCompleteDirectoryMove() throws IOException {
        executeTarget("testCompleteDirectoryMove");
        assertTrue(getProject().resolveFile("E").exists());
        assertTrue(getProject().resolveFile("E/1").exists());
        assertTrue(!getProject().resolveFile("A/1").exists());
        assertTrue(!getProject().resolveFile("A").exists());
    }
}
