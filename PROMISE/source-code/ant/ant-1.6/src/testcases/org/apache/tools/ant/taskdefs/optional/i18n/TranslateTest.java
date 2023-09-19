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

package org.apache.tools.ant.taskdefs.optional.i18n;

import org.apache.tools.ant.BuildFileTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Tests the Translate task.
 *
 * @author    <a href="mailto:antoine@antbuild.com">Antoine Levy-Lambert</a>
 * @since     Ant 1.6
 */
public class TranslateTest extends BuildFileTest {
    static private final int BUF_SIZE = 32768;

    private final static String TASKDEFS_DIR = "src/etc/testcases/taskdefs/optional/i18n/translate";

    public TranslateTest(String name) {
        super(name);
    }


    public void setUp() {
        configureProject(TASKDEFS_DIR + "/translate.xml");
    }

    public void tearDown() {
        executeTarget("cleanup");
    }

    public void test1() {
        executeTarget("test1");
        assertTrue("translation of "+ TASKDEFS_DIR + "/input/template.txt",compareFiles(TASKDEFS_DIR+"/expected/de/template.txt",TASKDEFS_DIR+"/output/de/template.txt"));
    }
    private boolean compareFiles(String name1, String name2) {
        File file1 = new File(name1);
        File file2 = new File(name2);

        try {
            if (!file1.exists() || !file2.exists()) {
                System.out.println("One or both files do not exist:" + name1 + ", " + name2);
                return false;
            }

            if (file1.length() != file2.length()) {
                System.out.println("File size mismatch:" + name1 + "(" + file1.length() + "), " +
                                   name2  + "(" + file2.length() + ")");
                return false;
            }

            // byte - byte compare
            byte[] buffer1 = new byte[BUF_SIZE];
            byte[] buffer2 = new byte[BUF_SIZE];

            FileInputStream fis1 = new FileInputStream(file1);
            FileInputStream fis2 = new FileInputStream(file2);
            int index = 0;
            int read = 0;
            while ((read = fis1.read(buffer1)) != -1) {
                fis2.read(buffer2);
                for (int i = 0; i < read; ++i, ++index) {
                    if (buffer1[i] != buffer2[i]) {
                        System.out.println("Bytes mismatch:" + name1 + ", " + name2 +
                                           " at byte " + index);
                        return false;
                    }
                }
            }
            return true;
        }
        catch (IOException e) {
            System.out.println("IOException comparing files: " + name1 + ", " + name2);
            return false;
        }
    }
}

