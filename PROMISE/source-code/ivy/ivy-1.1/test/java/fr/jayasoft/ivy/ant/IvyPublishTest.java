/*
 * This file is subject to the license found in LICENCE.TXT in the root directory of the project.
 * 
 * version 1.1
 */
package fr.jayasoft.ivy.ant;

import java.io.File;

import junit.framework.TestCase;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Delete;

import fr.jayasoft.ivy.Ivy;
import fr.jayasoft.ivy.ModuleDescriptor;
import fr.jayasoft.ivy.util.FileUtil;
import fr.jayasoft.ivy.xml.XmlModuleDescriptorParser;

public class IvyPublishTest extends TestCase {
    private File _cache;
    private IvyPublish _publish;
    private Project _project;
    
    protected void setUp() throws Exception {
        cleanTestDir();
        cleanRep();
        createCache();
        _project = new Project();
        _project.setProperty("ivy.conf.file", "test/repositories/ivyconf.xml");
        _project.setProperty("build", "build/test/publish");

        _publish = new IvyPublish();
        _publish.setProject(_project);
        _publish.setCache(_cache);
    }

    private void createCache() {
        _cache = new File("build/cache");
        _cache.mkdirs();
    }
    
    protected void tearDown() throws Exception {
        cleanCache();
        cleanTestDir();
        cleanRep();
    }

    private void cleanCache() {
        Delete del = new Delete();
        del.setProject(new Project());
        del.setDir(_cache);
        del.execute();
    }

    private void cleanTestDir() {
        Delete del = new Delete();
        del.setProject(new Project());
        del.setDir(new File("build/test/publish"));
        del.execute();
    }

    private void cleanRep() {
        Delete del = new Delete();
        del.setProject(new Project());
        del.setDir(new File("test/repositories/1/jayasoft"));
        del.execute();
    }

    public void testSimple() throws Exception {
        _project.setProperty("ivy.dep.file", "test/java/fr/jayasoft/ivy/ant/ivy-simple.xml");
        IvyResolve res = new IvyResolve();
        res.setProject(_project);
        res.execute();
        
        _publish.setPubrevision("1.2");
        _publish.setResolver("1");
        File art = new File("build/test/publish/resolve-simple-1.2.jar");
        FileUtil.copy(new File("test/repositories/1/org1/mod1.1/jars/mod1.1-1.0.jar"), art, null);
        _publish.execute();
        
        // should have do the ivy delivering
        assertTrue(new File("build/test/publish/ivy-1.2.xml").exists()); 
        
        // should have published the files with "1" resolver
        assertTrue(new File("test/repositories/1/jayasoft/resolve-simple/ivys/ivy-1.2.xml").exists()); 
        assertTrue(new File("test/repositories/1/jayasoft/resolve-simple/jars/resolve-simple-1.2.jar").exists());
        
        // should have updated published ivy version
        ModuleDescriptor md = XmlModuleDescriptorParser.parseDescriptor(new Ivy(), new File("test/repositories/1/jayasoft/resolve-simple/ivys/ivy-1.2.xml").toURL(), false);
        assertEquals("1.2", md.getModuleRevisionId().getRevision());
    }

}
