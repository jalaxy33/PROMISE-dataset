/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.apache.ivy.ant;

import java.io.File;

import junit.framework.TestCase;

import org.apache.ivy.TestHelper;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.types.Path;

public class IvyCachePathTest extends TestCase {
    private File cache;

    private IvyCachePath path;

    private Project project;

    protected void setUp() throws Exception {
        createCache();
        project = new Project();
        project.setProperty("ivy.settings.file", "test/repositories/ivysettings.xml");

        path = new IvyCachePath();
        path.setProject(project);
        System.setProperty("ivy.cache.dir", cache.getAbsolutePath());
    }

    private void createCache() {
        cache = new File("build/cache");
        cache.mkdirs();
    }

    protected void tearDown() throws Exception {
        cleanCache();
    }

    private void cleanCache() {
        Delete del = new Delete();
        del.setProject(new Project());
        del.setDir(cache);
        del.execute();
    }

    public void testSimple() throws Exception {
        project.setProperty("ivy.dep.file", "test/java/org/apache/ivy/ant/ivy-simple.xml");
        path.setPathid("simple-pathid");
        path.execute();
        Object ref = project.getReference("simple-pathid");
        assertNotNull(ref);
        assertTrue(ref instanceof Path);
        Path p = (Path) ref;
        assertEquals(1, p.size());
        assertEquals(getArchiveFileInCache("org1", "mod1.2", "2.0", "mod1.2", "jar", "jar")
                .getAbsolutePath(), new File(p.list()[0]).getAbsolutePath());
    }

    public void testInline1() throws Exception {
        // we first resolve another ivy file
        IvyResolve resolve = new IvyResolve();
        resolve.setProject(project);
        resolve.setFile(new File("test/java/org/apache/ivy/ant/ivy-latest.xml"));
        resolve.execute();

        assertTrue(getArchiveFileInCache("org1", "mod1.2", "2.2", "mod1.2", "jar", "jar").exists());

        // then we resolve a dependency directly
        path.setOrganisation("org1");
        path.setModule("mod1.2");
        path.setRevision("2.0");
        path.setInline(true);
        path.setPathid("simple-pathid");
        path.execute();
        Object ref = project.getReference("simple-pathid");
        assertNotNull(ref);
        assertTrue(ref instanceof Path);
        Path p = (Path) ref;
        assertEquals(1, p.size());
        assertEquals(getArchiveFileInCache("org1", "mod1.2", "2.0", "mod1.2", "jar", "jar")
                .getAbsolutePath(), new File(p.list()[0]).getAbsolutePath());
    }

    public void testInline2() throws Exception {
        // we first resolve a dependency directly
        path.setOrganisation("org1");
        path.setModule("mod1.2");
        path.setRevision("2.0");
        path.setInline(true);
        path.setPathid("simple-pathid");
        path.execute();
        Object ref = project.getReference("simple-pathid");
        assertNotNull(ref);
        assertTrue(ref instanceof Path);
        Path p = (Path) ref;
        assertEquals(1, p.size());
        assertEquals(getArchiveFileInCache("org1", "mod1.2", "2.0", "mod1.2", "jar", "jar")
                .getAbsolutePath(), new File(p.list()[0]).getAbsolutePath());

        // we then resolve another ivy file
        IvyResolve resolve = new IvyResolve();
        resolve.setProject(project);
        resolve.setFile(new File("test/java/org/apache/ivy/ant/ivy-latest.xml"));
        resolve.execute();

        assertTrue(getArchiveFileInCache("org1", "mod1.2", "2.2", "mod1.2", "jar", "jar").exists());
    }

    public void testEmptyConf() throws Exception {
        project.setProperty("ivy.dep.file", "test/java/org/apache/ivy/ant/ivy-108.xml");
        path.setPathid("emptyconf-pathid");
        path.setConf("empty");
        path.execute();
        Object ref = project.getReference("emptyconf-pathid");
        assertNotNull(ref);
        assertTrue(ref instanceof Path);
        Path p = (Path) ref;
        assertEquals(0, p.size());
    }

    public void testFailure() throws Exception {
        try {
            project.setProperty("ivy.dep.file", "test/java/org/apache/ivy/ant/ivy-failure.xml");
            path.setPathid("failure-pathid");
            path.execute();
            fail("failure didn't raised an exception with default haltonfailure setting");
        } catch (BuildException ex) {
            // ok => should raised an exception
        }
    }

    public void testHaltOnFailure() throws Exception {
        try {
            project.setProperty("ivy.dep.file", "test/java/org/apache/ivy/ant/ivy-failure.xml");
            path.setPathid("haltfailure-pathid");
            path.setHaltonfailure(false);
            path.execute();
        } catch (BuildException ex) {
            fail("failure raised an exception with haltonfailure set to false");
        }
    }

    public void testWithResolveId() throws Exception {
        IvyResolve resolve = new IvyResolve();
        resolve.setProject(project);
        resolve.setFile(new File("test/java/org/apache/ivy/ant/ivy-simple.xml"));
        resolve.setResolveId("withResolveId");
        resolve.execute();

        // resolve another ivy file
        resolve = new IvyResolve();
        resolve.setProject(project);
        resolve.setFile(new File("test/java/org/apache/ivy/ant/ivy-latest.xml"));
        resolve.execute();

        path.setResolveId("withResolveId");
        path.setPathid("withresolveid-pathid");
        path.execute();

        Object ref = project.getReference("withresolveid-pathid");
        assertNotNull(ref);
        assertTrue(ref instanceof Path);
        Path p = (Path) ref;
        assertEquals(1, p.size());
        assertEquals(getArchiveFileInCache("org1", "mod1.2", "2.0", "mod1.2", "jar", "jar")
                .getAbsolutePath(), new File(p.list()[0]).getAbsolutePath());
    }

    public void testWithResolveIdWithoutResolve() throws Exception {
        Project otherProject = new Project();
        otherProject.setProperty("ivy.settings.file", "test/repositories/ivysettings.xml");

        IvyResolve resolve = new IvyResolve();
        resolve.setProject(otherProject);
        resolve.setFile(new File("test/java/org/apache/ivy/ant/ivy-simple.xml"));
        resolve.setResolveId("withResolveId");
        resolve.execute();

        // resolve another ivy file
        resolve = new IvyResolve();
        resolve.setProject(project);
        resolve.setFile(new File("test/java/org/apache/ivy/ant/ivy-latest.xml"));
        resolve.execute();

        path.setResolveId("withResolveId");
        path.setPathid("withresolveid-pathid");
        path.setFile(new File("test/java/org/apache/ivy/ant/ivy-simple.xml"));
        path.execute();

        Object ref = project.getReference("withresolveid-pathid");
        assertNotNull(ref);
        assertTrue(ref instanceof Path);
        Path p = (Path) ref;
        assertEquals(1, p.size());
        assertEquals(getArchiveFileInCache("org1", "mod1.2", "2.0", "mod1.2", "jar", "jar")
                .getAbsolutePath(), new File(p.list()[0]).getAbsolutePath());
    }

    public void testWithResolveIdAndMissingConfs() throws Exception {
        Project otherProject = new Project();
        otherProject.setProperty("ivy.settings.file", "test/repositories/ivysettings.xml");

        IvyResolve resolve = new IvyResolve();
        resolve.setProject(otherProject);
        resolve.setFile(new File("test/java/org/apache/ivy/ant/ivy-multiconf.xml"));
        resolve.setResolveId("testWithResolveIdAndMissingConfs");
        resolve.setConf("default");
        resolve.execute();

        // resolve another ivy file
        resolve = new IvyResolve();
        resolve.setProject(project);
        resolve.setFile(new File("test/java/org/apache/ivy/ant/ivy-latest.xml"));
        resolve.execute();

        project.setProperty("ivy.dep.file", "test/java/org/apache/ivy/ant/ivy-multiconf.xml");

        path.setResolveId("testWithResolveIdAndMissingConfs");
        path.setPathid("withresolveid-pathid");
        path.setConf("default,compile");
        path.setFile(new File("test/java/org/apache/ivy/ant/ivy-multiconf.xml"));
        path.execute();
    }

    private File getArchiveFileInCache(String organisation, String module, String revision,
            String artifact, String type, String ext) {
        return TestHelper.getArchiveFileInCache(path.getIvyInstance(), organisation,
            module, revision, artifact, type, ext);
    }
}
