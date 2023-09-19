/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
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

import java.io.*;

import junit.framework.TestCase;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.condition.Os;

/**
 * Tests for org.apache.tools.ant.util.FileUtils.
 *
 * @author Stefan Bodewig
 * @author <a href="mailto:jtulley@novell.com">Jeff Tulley</a>
 */
public class FileUtilsTest extends TestCase {

    private FileUtils fu;
    private File removeThis;
    private String root;

    public FileUtilsTest(String name) {
        super(name);
    }

    public void setUp() {
        fu = FileUtils.newFileUtils();
        // Windows adds the drive letter in uppercase, unless you run Cygwin
        root = new File(File.separator).getAbsolutePath().toUpperCase();
    }

    public void tearDown() {
        if (removeThis != null && removeThis.exists()) {
            removeThis.delete();
        }
    }

    public void testSetLastModified() throws IOException {
        removeThis = new File("dummy");
        FileOutputStream fos = new FileOutputStream(removeThis);
        fos.write(new byte[0]);
        fos.close();
        long modTime = removeThis.lastModified();
        assertTrue(modTime != 0);

        /*
         * Sleep for some time to make sure a touched file would get a
         * more recent timestamp according to the file system's
         * granularity (should be > 2s to account for Windows FAT).
         */
        try {
            Thread.currentThread().sleep(5000);
        } catch (InterruptedException ie) {
            fail(ie.getMessage());
        }

        fu.setFileLastModified(removeThis, -1);
        long secondModTime = removeThis.lastModified();
        try {
            Class.forName("java.lang.ThreadLocal");
            assertTrue(secondModTime > modTime);
        } catch (ClassNotFoundException e) {
            // JDK 1.1
            assertEquals(modTime, secondModTime);
        }


        // number of milliseconds in a day
        final int millisperday=24 * 3600 * 1000;
        // in a previous version, the date of the file was set to 123456
        // milliseconds since 01.01.1970
        // it did not work on a computer running JDK 1.4.1_02 + Windows 2000
        fu.setFileLastModified(removeThis, secondModTime + millisperday);
        long thirdModTime = removeThis.lastModified();
        try {
            Class.forName("java.lang.ThreadLocal");
            /*
             * I would love to compare this with 123456, but depending on
             * the filesystems granularity it can take an arbitrary value.
             *
             * Just assert the time has changed.
             */
            assertTrue(thirdModTime != secondModTime);
        } catch (ClassNotFoundException e) {
            // JDK 1.1
            assertEquals(modTime, thirdModTime);
        }
    }

    public void testResolveFile() {
        /*
         * Start with simple absolute file names.
         */
        assertEquals(File.separator,
                     fu.resolveFile(null, "/").getPath());
        assertEquals(File.separator,
                     fu.resolveFile(null, "\\").getPath());

        /*
         * throw in drive letters
         */
        String driveSpec = "C:";
        assertEquals(driveSpec + "\\",
                     fu.resolveFile(null, driveSpec + "/").getPath());
        assertEquals(driveSpec + "\\",
                     fu.resolveFile(null, driveSpec + "\\").getPath());
        String driveSpecLower = "c:";
        assertEquals(driveSpec + "\\",
                     fu.resolveFile(null, driveSpecLower + "/").getPath());
        assertEquals(driveSpec + "\\",
                     fu.resolveFile(null, driveSpecLower + "\\").getPath());
        /*
         * promised to eliminate consecutive slashes after drive letter.
         */
        assertEquals(driveSpec + "\\",
                     fu.resolveFile(null, driveSpec + "/////").getPath());
        assertEquals(driveSpec + "\\",
                     fu.resolveFile(null, driveSpec + "\\\\\\\\\\\\").getPath());

        if (Os.isFamily("netware")) {
            /*
             * throw in NetWare volume names
             */
            driveSpec = "SYS:";
            assertEquals(driveSpec,
                         fu.resolveFile(null, driveSpec + "/").getPath());
            assertEquals(driveSpec,
                         fu.resolveFile(null, driveSpec + "\\").getPath());
            driveSpecLower = "sys:";
            assertEquals(driveSpec,
                         fu.resolveFile(null, driveSpecLower + "/").getPath());
            assertEquals(driveSpec,
                         fu.resolveFile(null, driveSpecLower + "\\").getPath());
            /*
             * promised to eliminate consecutive slashes after drive letter.
             */
            assertEquals(driveSpec,
                         fu.resolveFile(null, driveSpec + "/////").getPath());
            assertEquals(driveSpec,
                         fu.resolveFile(null, driveSpec + "\\\\\\\\\\\\").getPath());
        }

        /*
         * Now test some relative file name magic.
         */
        assertEquals(localize("/1/2/3/4"),
                     fu.resolveFile(new File(localize("/1/2/3")), "4").getPath());
        assertEquals(localize("/1/2/3/4"),
                     fu.resolveFile(new File(localize("/1/2/3")), "./4").getPath());
        assertEquals(localize("/1/2/3/4"),
                     fu.resolveFile(new File(localize("/1/2/3")), ".\\4").getPath());
        assertEquals(localize("/1/2/3/4"),
                     fu.resolveFile(new File(localize("/1/2/3")), "./.\\4").getPath());
        assertEquals(localize("/1/2/3/4"),
                     fu.resolveFile(new File(localize("/1/2/3")), "../3/4").getPath());
        assertEquals(localize("/1/2/3/4"),
                     fu.resolveFile(new File(localize("/1/2/3")), "..\\3\\4").getPath());
        assertEquals(localize("/1/2/3/4"),
                     fu.resolveFile(new File(localize("/1/2/3")), "../../5/.././2/./3/6/../4").getPath());
        assertEquals(localize("/1/2/3/4"),
                     fu.resolveFile(new File(localize("/1/2/3")), "..\\../5/..\\./2/./3/6\\../4").getPath());

        try {
            fu.resolveFile(new File(localize("/1")), "../../b");
            fail("successfully crawled beyond the filesystem root");
        } catch (BuildException e) {
            // Expected Exception caught
        }

    }

    public void testNormalize() {
        /*
         * Start with simple absolute file names.
         */
        assertEquals(File.separator,
                     fu.normalize("/").getPath());
        assertEquals(File.separator,
                     fu.normalize("\\").getPath());

        /*
         * throw in drive letters
         */
        String driveSpec = "C:";
        assertEquals(driveSpec,
                     fu.normalize(driveSpec).getPath());
        assertEquals(driveSpec + "\\",
                     fu.normalize(driveSpec + "/").getPath());
        assertEquals(driveSpec + "\\",
                     fu.normalize(driveSpec + "\\").getPath());
        String driveSpecLower = "c:";
        assertEquals(driveSpec + "\\",
                     fu.normalize(driveSpecLower + "/").getPath());
        assertEquals(driveSpec + "\\",
                     fu.normalize(driveSpecLower + "\\").getPath());
        /*
         * promised to eliminate consecutive slashes after drive letter.
         */
        assertEquals(driveSpec + "\\",
                     fu.normalize(driveSpec + "/////").getPath());
        assertEquals(driveSpec + "\\",
                     fu.normalize(driveSpec + "\\\\\\\\\\\\").getPath());

        if (Os.isFamily("netware")) {
            /*
             * throw in NetWare volume names
             */
            driveSpec = "SYS:";
            assertEquals(driveSpec,
                         fu.normalize(driveSpec).getPath());
            assertEquals(driveSpec,
                         fu.normalize(driveSpec + "/").getPath());
            assertEquals(driveSpec,
                         fu.normalize(driveSpec + "\\").getPath());
            driveSpecLower = "sys:";
            assertEquals(driveSpec,
                         fu.normalize(driveSpecLower).getPath());
            assertEquals(driveSpec,
                         fu.normalize(driveSpecLower + "/").getPath());
            assertEquals(driveSpec,
                         fu.normalize(driveSpecLower + "\\").getPath());
            assertEquals(driveSpec + "\\junk",
                         fu.normalize(driveSpecLower + "\\junk").getPath());
            /*
             * promised to eliminate consecutive slashes after drive letter.
             */
            assertEquals(driveSpec,
                         fu.normalize(driveSpec + "/////").getPath());
            assertEquals(driveSpec,
                         fu.normalize(driveSpec + "\\\\\\\\\\\\").getPath());
        }

        /*
         * Now test some relative file name magic.
         */
        assertEquals(localize("/1/2/3/4"),
                     fu.normalize(localize("/1/2/3/4")).getPath());
        assertEquals(localize("/1/2/3/4"),
                     fu.normalize(localize("/1/2/3/./4")).getPath());
        assertEquals(localize("/1/2/3/4"),
                     fu.normalize(localize("/1/2/3/.\\4")).getPath());
        assertEquals(localize("/1/2/3/4"),
                     fu.normalize(localize("/1/2/3/./.\\4")).getPath());
        assertEquals(localize("/1/2/3/4"),
                     fu.normalize(localize("/1/2/3/../3/4")).getPath());
        assertEquals(localize("/1/2/3/4"),
                     fu.normalize(localize("/1/2/3/..\\3\\4")).getPath());
        assertEquals(localize("/1/2/3/4"),
                     fu.normalize(localize("/1/2/3/../../5/.././2/./3/6/../4")).getPath());
        assertEquals(localize("/1/2/3/4"),
                     fu.normalize(localize("/1/2/3/..\\../5/..\\./2/./3/6\\../4")).getPath());

        try {
            fu.normalize("foo");
            fail("foo is not an absolute path");
        } catch (BuildException e) {
            // Expected exception caught
        }

        try {
            fu.normalize(localize("/1/../../b"));
            fail("successfully crawled beyond the filesystem root");
        } catch (BuildException e) {
            // Expected exception caught
        }
    }

    /**
     * Test handling of null arguments.
     */
    public void testNullArgs() {
        try {
            fu.normalize(null);
            fail("successfully normalized a null-file");
        } catch (NullPointerException npe) {
            // Expected exception caught
        }

        File f = fu.resolveFile(null, "a");
        assertEquals(f, new File("a"));
    }

    /**
     * Test createTempFile
     */
    public void testCreateTempFile() {
        File parent = new File((new File("/tmp")).getAbsolutePath());
        File tmp1 = fu.createTempFile("pre", ".suf", parent);
        assertTrue("new file", !tmp1.exists());

        String name = tmp1.getName();
        assertTrue("starts with pre", name.startsWith("pre"));
        assertTrue("ends with .suf", name.endsWith(".suf"));
        assertEquals("is inside parent dir",
                     parent.getAbsolutePath(),
                     tmp1.getParent());

        File tmp2 = fu.createTempFile("pre", ".suf", parent);
        assertTrue("files are different",
                   !tmp1.getAbsolutePath().equals(tmp2.getAbsolutePath()));

        // null parent dir
        File tmp3 = fu.createTempFile("pre", ".suf", null);
        String  tmploc = System.getProperty("java.io.tmpdir");
        assertEquals((new File(tmploc, tmp3.getName())).getAbsolutePath(),
                     tmp3.getAbsolutePath());
    }

    /**
     * Test contentEquals
     */
    public void testContentEquals() throws IOException {
        assertTrue("Non existing files", fu.contentEquals(new File("foo"),
                                                          new File("bar")));
        assertTrue("One exists, the other one doesn\'t",
                   !fu.contentEquals(new File("foo"), new File("build.xml")));
        assertTrue("Don\'t compare directories",
                   !fu.contentEquals(new File("src"), new File("src")));
        assertTrue("File equals itself",
                   fu.contentEquals(new File("build.xml"),
                                    new File("build.xml")));
        assertTrue("Files are different",
                   !fu.contentEquals(new File("build.xml"),
                                     new File("docs.xml")));
    }

    /**
     * Test createNewFile
     */
    public void testCreateNewFile() throws IOException {
        removeThis = new File("dummy");
        assertTrue(!removeThis.exists());
        fu.createNewFile(removeThis);
        assertTrue(removeThis.exists());
    }

    /**
     * Test removeLeadingPath.
     */
    public void testRemoveLeadingPath() {
        assertEquals("bar", fu.removeLeadingPath(new File("/foo"),
                                                 new File("/foo/bar")));
        assertEquals("bar", fu.removeLeadingPath(new File("/foo/"),
                                                 new File("/foo/bar")));
        assertEquals("bar", fu.removeLeadingPath(new File("\\foo"),
                                                 new File("\\foo\\bar")));
        assertEquals("bar", fu.removeLeadingPath(new File("\\foo\\"),
                                                 new File("\\foo\\bar")));
        assertEquals("bar", fu.removeLeadingPath(new File("c:/foo"),
                                                 new File("c:/foo/bar")));
        assertEquals("bar", fu.removeLeadingPath(new File("c:/foo/"),
                                                 new File("c:/foo/bar")));
        assertEquals("bar", fu.removeLeadingPath(new File("c:\\foo"),
                                                 new File("c:\\foo\\bar")));
        assertEquals("bar", fu.removeLeadingPath(new File("c:\\foo\\"),
                                                 new File("c:\\foo\\bar")));
        assertEqualsIgnoreDriveCase(fu.normalize("/bar").getAbsolutePath(),
                     fu.removeLeadingPath(new File("/foo"), new File("/bar")));
        assertEqualsIgnoreDriveCase(fu.normalize("/foobar").getAbsolutePath(),
                     fu.removeLeadingPath(new File("/foo"), new File("/foobar")));
        // bugzilla report 19979
        assertEquals("", fu.removeLeadingPath(new File("/foo/bar"),
                                              new File("/foo/bar")));
        assertEquals("", fu.removeLeadingPath(new File("/foo/bar"),
                                              new File("/foo/bar/")));
        assertEquals("", fu.removeLeadingPath(new File("/foo/bar/"),
                                              new File("/foo/bar/")));
        assertEquals("", fu.removeLeadingPath(new File("/foo/bar/"),
                                              new File("/foo/bar")));

        String expected = "foo/bar".replace('\\', File.separatorChar)
            .replace('/', File.separatorChar);
        assertEquals(expected, fu.removeLeadingPath(new File("/"),
                                                    new File("/foo/bar")));
        assertEquals(expected, fu.removeLeadingPath(new File("c:/"),
                                                    new File("c:/foo/bar")));
        assertEquals(expected, fu.removeLeadingPath(new File("c:\\"),
                                                    new File("c:\\foo\\bar")));
    }

    /**
     * test toUri
     */
    public void testToURI() {
        String dosRoot = null;
        if (Os.isFamily("dos")) {
            dosRoot = System.getProperty("user.dir").charAt(0) + ":/";
        }
        else
        {
            dosRoot = "";
        }
        if (Os.isFamily("dos")) {
            assertEquals("file:///C:/foo", fu.toURI("c:\\foo"));
        }
        if (Os.isFamily("netware")) {
            assertEquals("file:///SYS:/foo", fu.toURI("sys:\\foo"));
        }
        assertEquals("file:///" + dosRoot + "foo", fu.toURI("/foo"));
        assertEquals("file:./foo",  fu.toURI("./foo"));
        assertEquals("file:///" + dosRoot + "foo", fu.toURI("\\foo"));
        assertEquals("file:./foo",  fu.toURI(".\\foo"));
        assertEquals("file:///" + dosRoot + "foo%20bar", fu.toURI("/foo bar"));
        assertEquals("file:///" + dosRoot + "foo%20bar", fu.toURI("\\foo bar"));
        assertEquals("file:///" + dosRoot + "foo%23bar", fu.toURI("/foo#bar"));
        assertEquals("file:///" + dosRoot + "foo%23bar", fu.toURI("\\foo#bar"));
    }

    /**
     * test fromUri
     */
    public void testFromURI() {
        if (Os.isFamily("netware")) {
            assertEqualsIgnoreDriveCase("SYS:\\foo", fu.fromURI("file:///sys:/foo"));
        }
        if (Os.isFamily("dos")) {
            assertEqualsIgnoreDriveCase("C:\\foo", fu.fromURI("file:///c:/foo"));
        }
        assertEqualsIgnoreDriveCase(localize("/foo"), fu.fromURI("file:///foo"));
        assertEquals("." + File.separator + "foo",
                     fu.fromURI("file:./foo"));
        assertEqualsIgnoreDriveCase(localize("/foo bar"), fu.fromURI("file:///foo%20bar"));
        assertEqualsIgnoreDriveCase(localize("/foo#bar"), fu.fromURI("file:///foo%23bar"));
    }

    /**
     * adapt file separators to local conventions
     */
    private String localize(String path) {
        path = root + path.substring(1);
        return path.replace('\\', File.separatorChar).replace('/', File.separatorChar);
    }
    /**
     * convenience method
     * normalize brings the drive in uppercase
     * the drive letter is in lower case under cygwin
     * calling this method allows tests where normalize is called to pass under cygwin
     */
    private void assertEqualsIgnoreDriveCase(String s1, String s2) {
        if (Os.isFamily("dos") && s1.length()>=1 && s2.length()>=1) {
            StringBuffer sb1= new StringBuffer(s1);
            StringBuffer sb2= new StringBuffer(s2);
            sb1.setCharAt(0,Character.toUpperCase(s1.charAt(0)));
            sb2.setCharAt(0,Character.toUpperCase(s2.charAt(0)));
            assertEquals(sb1.toString(),sb2.toString());
        }   else {
            assertEquals(s1,s2);
        }
    }
}
