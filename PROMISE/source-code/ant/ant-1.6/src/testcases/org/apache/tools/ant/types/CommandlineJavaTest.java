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

package org.apache.tools.ant.types;

import org.apache.tools.ant.Project;

import junit.framework.TestCase;
import junit.framework.AssertionFailedError;

import java.io.File;

/**
 * JUnit 3 testcases for org.apache.tools.ant.CommandlineJava
 *
 * @author Stefan Bodewig
 */
public class CommandlineJavaTest extends TestCase {

    public CommandlineJavaTest(String name) {
        super(name);
    }

    private Project project;

    public void setUp() {
        project = new Project();
        project.setBasedir(".");
        project.setProperty("build.sysclasspath", "ignore");
    }

    public void testGetCommandline() {
        CommandlineJava c = new CommandlineJava();
        c.createArgument().setValue("org.apache.tools.ant.CommandlineJavaTest");
        c.setClassname("junit.textui.TestRunner");
        c.createVmArgument().setValue("-Djava.compiler=NONE");
        String[] s = c.getCommandline();
        assertEquals("no classpath", 4, s.length);
        /*
         * After changing CommandlineJava to search for the java
         * executable, I don't know, how to tests the value returned
         * here without using the same logic as applied in the class
         * itself.
         *
         * assertTrue("no classpath", "java", s[0]);
         */
        assertEquals("no classpath", "-Djava.compiler=NONE", s[1]);
        assertEquals("no classpath", "junit.textui.TestRunner", s[2]);
        assertEquals("no classpath",
                     "org.apache.tools.ant.CommandlineJavaTest", s[3]);
        try {
            CommandlineJava c2 = (CommandlineJava) c.clone();
        } catch (NullPointerException ex) {
            fail("cloning should work without classpath specified");
        }

        c.createClasspath(project).setLocation(project.resolveFile("build.xml"));
        c.createClasspath(project).setLocation(project.resolveFile(
            System.getProperty("ant.home")+"/lib/ant.jar"));
        s = c.getCommandline();
        assertEquals("with classpath", 6, s.length);
        //        assertEquals("with classpath", "java", s[0]);
        assertEquals("with classpath", "-Djava.compiler=NONE", s[1]);
        assertEquals("with classpath", "-classpath", s[2]);
        assertTrue("build.xml contained",
               s[3].indexOf("build.xml"+java.io.File.pathSeparator) >= 0);
        assertTrue("ant.jar contained", s[3].endsWith("ant.jar"));
        assertEquals("with classpath", "junit.textui.TestRunner", s[4]);
        assertEquals("with classpath",
                     "org.apache.tools.ant.CommandlineJavaTest", s[5]);
    }

    public void testJarOption() throws Exception {
        CommandlineJava c = new CommandlineJava();
        c.createArgument().setValue("arg1");
        c.setJar("myfile.jar");
        c.createVmArgument().setValue("-classic");
        c.createVmArgument().setValue("-Dx=y");
        String[] s = c.getCommandline();
        assertEquals("-classic", s[1]);
        assertEquals("-Dx=y", s[2]);
        assertEquals("-jar", s[3]);
        assertEquals("myfile.jar", s[4]);
        assertEquals("arg1", s[5]);
    }

    public void testSysproperties() {
        String currentClasspath = System.getProperty("java.class.path");
        assertNotNull(currentClasspath);
        assertNull(System.getProperty("key"));
        CommandlineJava c = new CommandlineJava();
        Environment.Variable v = new Environment.Variable();
        v.setKey("key");
        v.setValue("value");
        c.addSysproperty(v);

        project.setProperty("key2", "value2");
        PropertySet ps = new PropertySet();
        ps.setProject(project);
        ps.appendName("key2");
        c.addSyspropertyset(ps);

        try {
            c.setSystemProperties();
            String newClasspath = System.getProperty("java.class.path");
            assertNotNull(newClasspath);
            assertEquals(currentClasspath, newClasspath);
            assertNotNull(System.getProperty("key"));
            assertEquals("value", System.getProperty("key"));
            assertTrue(System.getProperties().containsKey("java.class.path"));
            assertNotNull(System.getProperty("key2"));
            assertEquals("value2", System.getProperty("key2"));
        } finally {
            c.restoreSystemProperties();
        }
        assertNull(System.getProperty("key"));
        assertNull(System.getProperty("key2"));
    }

}
