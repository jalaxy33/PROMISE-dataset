/*
 * This file is subject to the license found in LICENCE.TXT in the root directory of the project.
 * 
 * version 1.1
 */
package fr.jayasoft.ivy.xml;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;

import junit.framework.TestCase;
import fr.jayasoft.ivy.Artifact;
import fr.jayasoft.ivy.Configuration;
import fr.jayasoft.ivy.ConflictManager;
import fr.jayasoft.ivy.DependencyArtifactDescriptor;
import fr.jayasoft.ivy.DependencyDescriptor;
import fr.jayasoft.ivy.Ivy;
import fr.jayasoft.ivy.License;
import fr.jayasoft.ivy.ModuleDescriptor;
import fr.jayasoft.ivy.ModuleId;
import fr.jayasoft.ivy.Configuration.Visibility;
import fr.jayasoft.ivy.conflict.FixedConflictManager;
import fr.jayasoft.ivy.conflict.NoConflictManager;

/**
 * 
 */
public class XmlModuleDescriptorParserTest extends TestCase {
    private Ivy _ivy = new Ivy();
    public void testSimple() throws Exception {
        ModuleDescriptor md = XmlModuleDescriptorParser.parseDescriptor(_ivy, getClass().getResource("test-simple.xml"), true);
        assertNotNull(md);
        assertEquals("myorg", md.getModuleRevisionId().getOrganisation());
        assertEquals("mymodule", md.getModuleRevisionId().getName());
        assertEquals(null, md.getModuleRevisionId().getRevision());
        assertEquals("integration", md.getStatus());
        
        assertNotNull(md.getConfigurations());
        assertEquals(Arrays.asList(new Configuration[] {new Configuration("default")}), Arrays.asList(md.getConfigurations()));
        
        assertNotNull(md.getArtifacts("default"));
        assertEquals(1, md.getArtifacts("default").length);
        assertEquals("mymodule", md.getArtifacts("default")[0].getName());
        assertEquals("jar", md.getArtifacts("default")[0].getType());
        
        assertNotNull(md.getDependencies());
        assertEquals(0, md.getDependencies().length);
    }
    
    public void testBad() throws IOException {
        try {
            XmlModuleDescriptorParser.parseDescriptor(_ivy, getClass().getResource("test-bad.xml"), true);
            fail("bad ivy file raised no error");
        } catch (ParseException ex) {
            // ok
        }
    }

    public void testNoValidate() throws IOException, ParseException {
        XmlModuleDescriptorParser.parseDescriptor(_ivy, getClass().getResource("test-novalidate.xml"), false);
    }

    public void testBadVersion() throws IOException {
        try {
            XmlModuleDescriptorParser.parseDescriptor(_ivy, getClass().getResource("test-bad-version.xml"), true);
            fail("bad version ivy file raised no error");
        } catch (ParseException ex) {
            // ok
        }
    }
    
    public void testFull() throws Exception {
        ModuleDescriptor md = XmlModuleDescriptorParser.parseDescriptor(_ivy, getClass().getResource("test.xml"), true);
        assertNotNull(md);
        assertEquals("myorg", md.getModuleRevisionId().getOrganisation());
        assertEquals("mymodule", md.getModuleRevisionId().getName());
        assertEquals("myrev", md.getModuleRevisionId().getRevision());
        assertEquals("integration", md.getStatus()); 
        Date pubdate = new GregorianCalendar(2004, 10, 1, 11, 0, 0).getTime();
        assertEquals(pubdate, md.getPublicationDate());
        
        License[] licenses = md.getLicenses();
        assertEquals(1, licenses.length);
        assertEquals("MyLicense", licenses[0].getName());
        assertEquals("http://www.my.org/mymodule/mylicense.html", licenses[0].getUrl());
        
        assertEquals("http://www.my.org/mymodule/", md.getHomePage());
        
        Configuration[] confs = md.getConfigurations();
        assertNotNull(confs);
        assertEquals(5, confs.length);
        
        assertConf(md, "myconf1", "desc 1", Configuration.Visibility.PUBLIC, new String[0]);
        assertConf(md, "myconf2", "desc 2", Configuration.Visibility.PUBLIC, new String[0]);
        assertConf(md, "myconf3", "desc 3", Configuration.Visibility.PRIVATE, new String[0]);
        assertConf(md, "myconf4", "desc 4", Configuration.Visibility.PUBLIC, new String[] {"myconf1", "myconf2"});
        assertConf(md, "myoldconf", "my old desc", Configuration.Visibility.PUBLIC, new String[0]);
        
        assertArtifacts(md.getArtifacts("myconf1"), new String[] {"myartifact1", "myartifact2", "myartifact3", "myartifact4"});
        assertArtifacts(md.getArtifacts("myconf2"), new String[] {"myartifact1", "myartifact3"});
        assertArtifacts(md.getArtifacts("myconf3"), new String[] {"myartifact1", "myartifact3", "myartifact4"});
        assertArtifacts(md.getArtifacts("myconf4"), new String[] {"myartifact1"});
        
        DependencyDescriptor[] dependencies = md.getDependencies();
        assertNotNull(dependencies);
        assertEquals(11, dependencies.length);
        
        // no conf def => equivalent to *->*
        DependencyDescriptor dd = getDependency(dependencies, "mymodule2");
        assertNotNull(dd);
        assertEquals("myorg", dd.getDependencyId().getOrganisation());
        assertEquals("2.0", dd.getDependencyRevisionId().getRevision());
        assertEquals(Arrays.asList(new String[] {"*"}), Arrays.asList(dd.getModuleConfigurations()));
        assertEquals(Arrays.asList(new String[] {"*"}), Arrays.asList(dd.getDependencyConfigurations("myconf1")));        
        assertEquals(Arrays.asList(new String[] {"*"}), Arrays.asList(dd.getDependencyConfigurations(new String[] {"myconf2", "myconf3", "myconf4"})));
        assertDependencyArtifactsIncludes(dd, new String[] {"myconf1", "myconf2", "myconf3", "myconf4"}, new String[0]);
        
        // conf="myconf1" => equivalent to myconf1->myconf1
        dd = getDependency(dependencies, "yourmodule1");
        assertNotNull(dd);
        assertEquals("yourorg", dd.getDependencyId().getOrganisation());
        assertEquals("1.1", dd.getDependencyRevisionId().getRevision());
        assertEquals(Arrays.asList(new String[] {"myconf1"}), Arrays.asList(dd.getModuleConfigurations()));
        assertEquals(Arrays.asList(new String[] {"myconf1"}), Arrays.asList(dd.getDependencyConfigurations("myconf1")));        
        assertEquals(Arrays.asList(new String[] {}), Arrays.asList(dd.getDependencyConfigurations(new String[] {"myconf2", "myconf3", "myconf4"})));
        assertDependencyArtifactsIncludes(dd, new String[] {"myconf1", "myconf2", "myconf3", "myconf4"}, new String[0]);
        
        // conf="myconf1->yourconf1"
        dd = getDependency(dependencies, "yourmodule2");
        assertNotNull(dd);
        assertEquals("yourorg", dd.getDependencyId().getOrganisation());
        assertEquals("2+", dd.getDependencyRevisionId().getRevision());
        assertEquals(Arrays.asList(new String[] {"myconf1"}), Arrays.asList(dd.getModuleConfigurations()));
        assertEquals(Arrays.asList(new String[] {"yourconf1"}), Arrays.asList(dd.getDependencyConfigurations("myconf1")));        
        assertEquals(Arrays.asList(new String[] {}), Arrays.asList(dd.getDependencyConfigurations(new String[] {"myconf2", "myconf3", "myconf4"})));
        assertDependencyArtifactsIncludes(dd, new String[] {"myconf1", "myconf2", "myconf3", "myconf4"}, new String[0]);
        
        // conf="myconf1->yourconf1, yourconf2"
        dd = getDependency(dependencies, "yourmodule3");
        assertNotNull(dd);
        assertEquals("yourorg", dd.getDependencyId().getOrganisation());
        assertEquals("3.1", dd.getDependencyRevisionId().getRevision());
        assertEquals(Arrays.asList(new String[] {"myconf1"}), Arrays.asList(dd.getModuleConfigurations()));
        assertEquals(Arrays.asList(new String[] {"yourconf1", "yourconf2"}), Arrays.asList(dd.getDependencyConfigurations("myconf1")));        
        assertEquals(Arrays.asList(new String[] {}), Arrays.asList(dd.getDependencyConfigurations(new String[] {"myconf2", "myconf3", "myconf4"})));
        assertDependencyArtifactsIncludes(dd, new String[] {"myconf1", "myconf2", "myconf3", "myconf4"}, new String[0]);
        
        // conf="myconf1, myconf2->yourconf1, yourconf2"
        dd = getDependency(dependencies, "yourmodule4");
        assertNotNull(dd);
        assertEquals("yourorg", dd.getDependencyId().getOrganisation());
        assertEquals("4.1", dd.getDependencyRevisionId().getRevision());
        assertEquals(new HashSet(Arrays.asList(new String[] {"myconf1", "myconf2"})), new HashSet(Arrays.asList(dd.getModuleConfigurations())));
        assertEquals(Arrays.asList(new String[] {"yourconf1", "yourconf2"}), Arrays.asList(dd.getDependencyConfigurations("myconf1")));        
        assertEquals(Arrays.asList(new String[] {"yourconf1", "yourconf2"}), Arrays.asList(dd.getDependencyConfigurations("myconf2")));        
        assertEquals(Arrays.asList(new String[] {}), Arrays.asList(dd.getDependencyConfigurations(new String[] {"myconf3", "myconf4"})));
        assertDependencyArtifactsIncludes(dd, new String[] {"myconf1", "myconf2", "myconf3", "myconf4"}, new String[0]);
        
        // conf="myconf1->yourconf1;myconf2->yourconf1, yourconf2"
        dd = getDependency(dependencies, "yourmodule5");
        assertNotNull(dd);
        assertEquals("yourorg", dd.getDependencyId().getOrganisation());
        assertEquals("5.1", dd.getDependencyRevisionId().getRevision());
        assertEquals(new HashSet(Arrays.asList(new String[] {"myconf1", "myconf2"})), new HashSet(Arrays.asList(dd.getModuleConfigurations())));
        assertEquals(Arrays.asList(new String[] {"yourconf1"}), Arrays.asList(dd.getDependencyConfigurations("myconf1")));        
        assertEquals(Arrays.asList(new String[] {"yourconf1", "yourconf2"}), Arrays.asList(dd.getDependencyConfigurations("myconf2")));        
        assertEquals(Arrays.asList(new String[] {}), Arrays.asList(dd.getDependencyConfigurations(new String[] {"myconf3", "myconf4"})));
        assertDependencyArtifactsIncludes(dd, new String[] {"myconf1", "myconf2", "myconf3", "myconf4"}, new String[0]);
        
        dd = getDependency(dependencies, "yourmodule6");
        assertNotNull(dd);
        assertEquals("yourorg", dd.getDependencyId().getOrganisation());
        assertEquals("latest.integration", dd.getDependencyRevisionId().getRevision());
        assertEquals(new HashSet(Arrays.asList(new String[] {"myconf1", "myconf2"})), new HashSet(Arrays.asList(dd.getModuleConfigurations())));
        assertEquals(Arrays.asList(new String[] {"yourconf1"}), Arrays.asList(dd.getDependencyConfigurations("myconf1")));        
        assertEquals(Arrays.asList(new String[] {"yourconf1", "yourconf2"}), Arrays.asList(dd.getDependencyConfigurations("myconf2")));        
        assertEquals(Arrays.asList(new String[] {}), Arrays.asList(dd.getDependencyConfigurations(new String[] {"myconf3", "myconf4"})));
        assertDependencyArtifactsIncludes(dd, new String[] {"myconf1", "myconf2", "myconf3", "myconf4"}, new String[0]);
        
        dd = getDependency(dependencies, "yourmodule7");
        assertNotNull(dd);
        assertEquals("yourorg", dd.getDependencyId().getOrganisation());
        assertEquals("7.1", dd.getDependencyRevisionId().getRevision());
        assertEquals(new HashSet(Arrays.asList(new String[] {"myconf1", "myconf2"})), new HashSet(Arrays.asList(dd.getModuleConfigurations())));
        assertEquals(Arrays.asList(new String[] {"yourconf1"}), Arrays.asList(dd.getDependencyConfigurations("myconf1")));        
        assertEquals(Arrays.asList(new String[] {"yourconf1", "yourconf2"}), Arrays.asList(dd.getDependencyConfigurations("myconf2")));        
        assertEquals(Arrays.asList(new String[] {}), Arrays.asList(dd.getDependencyConfigurations(new String[] {"myconf3", "myconf4"})));
        assertDependencyArtifactsIncludes(dd, new String[] {"myconf1", "myconf2", "myconf3", "myconf4"}, new String[0]);
        
        dd = getDependency(dependencies, "yourmodule8");
        assertNotNull(dd);
        assertEquals("yourorg", dd.getDependencyId().getOrganisation());
        assertEquals("8.1", dd.getDependencyRevisionId().getRevision());
        assertEquals(new HashSet(Arrays.asList(new String[] {"*"})), new HashSet(Arrays.asList(dd.getModuleConfigurations())));
        assertDependencyArtifactsIncludes(dd, new String[] {"myconf1"}, new String[] {"yourartifact8-1", "yourartifact8-2"});
        assertDependencyArtifactsIncludes(dd, new String[] {"myconf2"}, new String[] {"yourartifact8-1", "yourartifact8-2"});
        assertDependencyArtifactsIncludes(dd, new String[] {"myconf3"}, new String[] {"yourartifact8-1", "yourartifact8-2"});
        assertDependencyArtifactsIncludes(dd, new String[] {"myconf4"}, new String[] {"yourartifact8-1", "yourartifact8-2"});
        
        dd = getDependency(dependencies, "yourmodule9");
        assertNotNull(dd);
        assertEquals("yourorg", dd.getDependencyId().getOrganisation());
        assertEquals("9.1", dd.getDependencyRevisionId().getRevision());
        assertEquals(new HashSet(Arrays.asList(new String[] {"myconf1", "myconf2", "myconf3"})), new HashSet(Arrays.asList(dd.getModuleConfigurations())));
        assertDependencyArtifactsIncludes(dd, new String[] {"myconf1"}, new String[] {"yourartifact9-1"});
        assertDependencyArtifactsIncludes(dd, new String[] {"myconf2"}, new String[] {"yourartifact9-1", "yourartifact9-2"});
        assertDependencyArtifactsIncludes(dd, new String[] {"myconf3"}, new String[] {"yourartifact9-2"});
        assertDependencyArtifactsIncludes(dd, new String[] {"myconf4"}, new String[] {});
        assertDependencyArtifactsExcludes(dd, new String[] {"myconf1"}, new String[] {});
        assertDependencyArtifactsExcludes(dd, new String[] {"myconf2"}, new String[] {});
        assertDependencyArtifactsExcludes(dd, new String[] {"myconf3"}, new String[] {});
        assertDependencyArtifactsExcludes(dd, new String[] {"myconf4"}, new String[] {});
        
        dd = getDependency(dependencies, "yourmodule10");
        assertNotNull(dd);
        assertEquals("yourorg", dd.getDependencyId().getOrganisation());
        assertEquals("10.1", dd.getDependencyRevisionId().getRevision());
        assertEquals(new HashSet(Arrays.asList(new String[] {"*"})), new HashSet(Arrays.asList(dd.getModuleConfigurations())));
        assertDependencyArtifactsIncludes(dd, new String[] {"myconf1"}, new String[] {"your.*", ".*"});
        assertDependencyArtifactsIncludes(dd, new String[] {"myconf2"}, new String[] {"your.*", ".*"});
        assertDependencyArtifactsIncludes(dd, new String[] {"myconf3"}, new String[] {"your.*", ".*"});
        assertDependencyArtifactsIncludes(dd, new String[] {"myconf4"}, new String[] {"your.*", ".*"});
        assertDependencyArtifactsExcludes(dd, new String[] {"myconf1"}, new String[] {"toexclude"});
        assertDependencyArtifactsExcludes(dd, new String[] {"myconf2"}, new String[] {"toexclude"});
        assertDependencyArtifactsExcludes(dd, new String[] {"myconf3"}, new String[] {"toexclude"});
        assertDependencyArtifactsExcludes(dd, new String[] {"myconf4"}, new String[] {"toexclude"});
        
        ConflictManager cm = md.getConflictManager(new ModuleId("yourorg", "yourmodule1"));
        assertNotNull(cm);
        assertTrue(cm instanceof NoConflictManager);
        
        cm = md.getConflictManager(new ModuleId("yourorg", "yourmodule2"));
        assertNotNull(cm);
        assertTrue(cm instanceof NoConflictManager);

        cm = md.getConflictManager(new ModuleId("theirorg", "theirmodule1"));
        assertNotNull(cm);
        assertTrue(cm instanceof FixedConflictManager);
        FixedConflictManager fcm = (FixedConflictManager)cm;
        assertEquals(2, fcm.getRevs().size());
        assertTrue(fcm.getRevs().contains("1.0"));
        assertTrue(fcm.getRevs().contains("1.1"));

        cm = md.getConflictManager(new ModuleId("theirorg", "theirmodule2"));
        assertNull(cm);
    }

    public void testBug60() throws Exception {
        ModuleDescriptor md = XmlModuleDescriptorParser.parseDescriptor(_ivy, getClass().getResource("test-bug60.xml"), true);
        assertNotNull(md);
        assertEquals("myorg", md.getModuleRevisionId().getOrganisation());
        assertEquals("mymodule", md.getModuleRevisionId().getName());
        assertEquals("myrev", md.getModuleRevisionId().getRevision());
        assertEquals("integration", md.getStatus()); 
        Date pubdate = new GregorianCalendar(2004, 10, 1, 11, 0, 0).getTime();
        assertEquals(pubdate, md.getPublicationDate());
        
        assertEquals(Arrays.asList(new Configuration[] {new Configuration("default")}), Arrays.asList(md.getConfigurations()));
        
        assertArtifacts(md.getArtifacts("default"), new String[] {"myartifact1", "myartifact2"});
    }

    public void testNoArtifact() throws Exception {
        ModuleDescriptor md = XmlModuleDescriptorParser.parseDescriptor(_ivy, getClass().getResource("test-noartifact.xml"), true);
        assertNotNull(md);
        assertEquals("myorg", md.getModuleRevisionId().getOrganisation());
        assertEquals("mymodule", md.getModuleRevisionId().getName());
        assertEquals(null, md.getModuleRevisionId().getRevision());
        assertEquals("integration", md.getStatus());
        
        assertNotNull(md.getConfigurations());
        assertEquals(Arrays.asList(new Configuration[] {new Configuration("default")}), Arrays.asList(md.getConfigurations()));
        
        assertNotNull(md.getArtifacts("default"));
        assertEquals(0, md.getArtifacts("default").length);
        
        assertNotNull(md.getDependencies());
        assertEquals(0, md.getDependencies().length);
    }
    
    public void testNoPublication() throws Exception {
        ModuleDescriptor md = XmlModuleDescriptorParser.parseDescriptor(_ivy, getClass().getResource("test-nopublication.xml"), true);
        assertNotNull(md);
        assertEquals("myorg", md.getModuleRevisionId().getOrganisation());
        assertEquals("mymodule", md.getModuleRevisionId().getName());
        assertEquals("myrev", md.getModuleRevisionId().getRevision());
        assertEquals("integration", md.getStatus()); 
        Date pubdate = new GregorianCalendar(2004, 10, 1, 11, 0, 0).getTime();
        assertEquals(pubdate, md.getPublicationDate());
        
        assertNotNull(md.getConfigurations());
        assertEquals(Arrays.asList(new Configuration[] {new Configuration("default")}), Arrays.asList(md.getConfigurations()));
        
        assertNotNull(md.getArtifacts("default"));
        assertEquals(1, md.getArtifacts("default").length);
        
        assertNotNull(md.getDependencies());
        assertEquals(1, md.getDependencies().length);
    }
    
    public void testDefaultConf() throws Exception {
        ModuleDescriptor md = XmlModuleDescriptorParser.parseDescriptor(_ivy, getClass().getResource("test-defaultconf.xml"), true);
        assertNotNull(md);
        
        DependencyDescriptor[] dependencies = md.getDependencies();
        assertNotNull(dependencies);
        assertEquals(2, dependencies.length);
        
        // no conf def => defaults to defaultConf: default
        DependencyDescriptor dd = getDependency(dependencies, "mymodule1");
        assertNotNull(dd);
        assertEquals("myorg", dd.getDependencyId().getOrganisation());
        assertEquals("1.0", dd.getDependencyRevisionId().getRevision());
        assertEquals(Arrays.asList(new String[] {"default"}), Arrays.asList(dd.getModuleConfigurations()));
        assertEquals(Arrays.asList(new String[] {"default"}), Arrays.asList(dd.getDependencyConfigurations("default")));        

        // confs def: *->*
        dd = getDependency(dependencies, "mymodule2");
        assertNotNull(dd);
        assertEquals("myorg", dd.getDependencyId().getOrganisation());
        assertEquals("2.0", dd.getDependencyRevisionId().getRevision());
        assertEquals(Arrays.asList(new String[] {"*"}), Arrays.asList(dd.getModuleConfigurations()));
        assertEquals(Arrays.asList(new String[] {"*"}), Arrays.asList(dd.getDependencyConfigurations("default")));        
    }
    
    private DependencyDescriptor getDependency(DependencyDescriptor[] dependencies, String name) {
        for (int i = 0; i < dependencies.length; i++) {
            assertNotNull(dependencies[i]);
            assertNotNull(dependencies[i].getDependencyId());
            if (name.equals(dependencies[i].getDependencyId().getName())) {
                return dependencies[i];
            }
        }
        return null;
    }

    private void assertArtifacts(Artifact[] artifacts, String[] artifactsNames) {
        assertNotNull(artifacts);
        assertEquals(artifactsNames.length, artifacts.length);
        for (int i = 0; i < artifactsNames.length; i++) {
            boolean found = false;
            for (int j = 0; j < artifacts.length; j++) {
                assertNotNull(artifacts[j]);
                if (artifacts[j].getName().equals(artifactsNames[i])) {
                    found = true;
                    break;
                }
            }
            assertTrue("artifact not found: "+artifactsNames[i], found);
        }
    }

    private void assertDependencyArtifactsIncludes(DependencyDescriptor dd, String[] confs, String[] artifactsNames) {
        DependencyArtifactDescriptor[] dads = dd.getDependencyArtifactsIncludes(confs);
        assertNotNull(dads);
        assertEquals(artifactsNames.length, dads.length);
        for (int i = 0; i < artifactsNames.length; i++) {
            boolean found = false;
            for (int j = 0; j < dads.length; j++) {
                assertNotNull(dads[j]);
                if (dads[j].getName().equals(artifactsNames[i])) {
                    found = true;
                    break;
                }
            }
            assertTrue("dependency artifact include not found: "+artifactsNames[i], found);
        }
    }

    private void assertDependencyArtifactsExcludes(DependencyDescriptor dd, String[] confs, String[] artifactsNames) {
        DependencyArtifactDescriptor[] dads = dd.getDependencyArtifactsExcludes(confs);
        assertNotNull(dads);
        assertEquals(artifactsNames.length, dads.length);
        for (int i = 0; i < artifactsNames.length; i++) {
            boolean found = false;
            for (int j = 0; j < dads.length; j++) {
                assertNotNull(dads[j]);
                if (dads[j].getName().equals(artifactsNames[i])) {
                    found = true;
                    break;
                }
            }
            assertTrue("dependency artifact exclude not found: "+artifactsNames[i], found);
        }
    }

    private void assertConf(ModuleDescriptor md, String name, String desc, Visibility visibility, String[] exts) {
        Configuration conf = md.getConfiguration(name);
        assertNotNull("configuration not found: "+name, conf);
        assertEquals(name, conf.getName());
        assertEquals(desc, conf.getDescription());
        assertEquals(visibility, conf.getVisibility());
        assertEquals(Arrays.asList(exts), Arrays.asList(conf.getExtends()));
    }
}
