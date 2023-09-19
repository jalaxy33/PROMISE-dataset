/*
 * This file is subject to the license found in LICENCE.TXT in the root directory of the project.
 * 
 * version 1.1
 */
package fr.jayasoft.ivy.ant;

import java.io.File;

import junit.framework.TestCase;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Delete;

import fr.jayasoft.ivy.Ivy;
import fr.jayasoft.ivy.ModuleRevisionId;

public class IvyResolveTest extends TestCase {
    private File _cache;
    private IvyResolve _resolve;
    
    protected void setUp() throws Exception {
        createCache();
        Project project = new Project();
        project.setProperty("ivy.conf.file", "test/repositories/ivyconf.xml");

        _resolve = new IvyResolve();
        _resolve.setProject(project);
        _resolve.setCache(_cache);
    }

    private void createCache() {
        _cache = new File("build/cache");
        _cache.mkdirs();
    }
    
    protected void tearDown() throws Exception {
        cleanCache();
    }

    private void cleanCache() {
        Delete del = new Delete();
        del.setProject(new Project());
        del.setDir(_cache);
        del.execute();
    }

    public void testSimple() throws Exception {
        _resolve.setFile(new File("test/java/fr/jayasoft/ivy/ant/ivy-simple.xml"));
        _resolve.execute();
        
        assertTrue(getIvy().getIvyFileInCache(_cache, ModuleRevisionId.newInstance("jayasoft", "resolve-simple", "1.0")).exists());
        
        // dependencies
        assertTrue(getIvy().getIvyFileInCache(_cache, ModuleRevisionId.newInstance("org1", "mod1.2", "2.0")).exists());
        assertTrue(getIvy().getArchiveFileInCache(_cache, "org1", "mod1.2", "2.0", "mod1.2", "jar", "jar").exists());
    }

    public void testFailure() throws Exception {
        try {
            _resolve.setFile(new File("test/java/fr/jayasoft/ivy/ant/ivy-failure.xml"));
            _resolve.execute();
            fail("failure didn't raised an exception with default haltonfailure setting");
        } catch (BuildException ex) {
            // ok => should raised an exception
        }
    }

    public void testHaltOnFailure() throws Exception {
        try {
            _resolve.setFile(new File("test/java/fr/jayasoft/ivy/ant/ivy-failure.xml"));
            _resolve.setHaltonfailure(false);
            _resolve.execute();
        } catch (BuildException ex) {
            ex.printStackTrace();
            fail("failure raised an exception with haltonfailure set to false");
        }
    }
    
    private Ivy getIvy() {
        return _resolve.getIvyInstance();
    }

    
}
