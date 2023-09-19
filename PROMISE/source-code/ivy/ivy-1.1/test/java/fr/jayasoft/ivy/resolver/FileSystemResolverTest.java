/*
 * This file is subject to the license found in LICENCE.TXT in the root directory of the project.
 * 
 * version 1.1
 */
package fr.jayasoft.ivy.resolver;

import java.io.File;
import java.util.Date;
import java.util.GregorianCalendar;

import junit.framework.TestCase;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Delete;

import fr.jayasoft.ivy.Artifact;
import fr.jayasoft.ivy.DefaultArtifact;
import fr.jayasoft.ivy.DefaultDependencyDescriptor;
import fr.jayasoft.ivy.Ivy;
import fr.jayasoft.ivy.ModuleRevisionId;
import fr.jayasoft.ivy.ResolveData;
import fr.jayasoft.ivy.ResolvedModuleRevision;
import fr.jayasoft.ivy.latest.LatestRevisionStrategy;
import fr.jayasoft.ivy.latest.LatestTimeStrategy;
import fr.jayasoft.ivy.report.ArtifactDownloadReport;
import fr.jayasoft.ivy.report.DownloadReport;
import fr.jayasoft.ivy.report.DownloadStatus;
import fr.jayasoft.ivy.util.FileUtil;

/**
 * 
 */
public class FileSystemResolverTest extends TestCase {

    private static final String FS = System.getProperty("file.separator");
    private static final String IVY_PATTERN = "test"+FS+"repositories"+FS+"1"+FS+"[organisation]"+FS+"[module]"+FS+"ivys"+FS+"ivy-[revision].xml";
    private File _cache;
    private ResolveData _data;
    private Ivy _ivy = new Ivy();
    
    protected void setUp() throws Exception {
        _cache = new File("build/cache");
        _data = new ResolveData(_ivy, _cache, null, null, true);
        _cache.mkdirs();
    }
    
    protected void tearDown() throws Exception {
        Delete del = new Delete();
        del.setProject(new Project());
        del.setDir(_cache);
        del.execute();
    }

    public void testFixedRevision() throws Exception {
        FileSystemResolver resolver = new FileSystemResolver();
        resolver.setName("test");
        resolver.setIvy(_ivy);
        assertEquals("test", resolver.getName());
        
        resolver.addIvyPattern(IVY_PATTERN);
        resolver.addArtifactPattern("test/repositories/1/[organisation]/[module]/[type]s/[artifact]-[revision].[type]");
        
        ModuleRevisionId mrid = ModuleRevisionId.newInstance("org1", "mod1.1", "1.0");
        ResolvedModuleRevision rmr = resolver.getDependency(new DefaultDependencyDescriptor(mrid, false), _data);
        assertNotNull(rmr);
        
        assertEquals(mrid, rmr.getId());
        Date pubdate = new GregorianCalendar(2004, 10, 1, 11, 0, 0).getTime();
        assertEquals(pubdate, rmr.getPublicationDate());
        
        
        // test to ask to download
        DefaultArtifact artifact = new DefaultArtifact(mrid, pubdate, "mod1.1", "jar", "jar");
        DownloadReport report = resolver.download(new Artifact[] {artifact}, _data.getIvy(), _cache);
        assertNotNull(report);
        
        assertEquals(1, report.getArtifactsReports().length);
        
        ArtifactDownloadReport ar = report.getArtifactReport(artifact);
        assertNotNull(ar);
        
        assertEquals(artifact, ar.getArtifact());
        assertEquals(DownloadStatus.SUCCESSFUL, ar.getDownloadStatus());

        // test to ask to download again, should use cache
        report = resolver.download(new Artifact[] {artifact}, _data.getIvy(), _cache);
        assertNotNull(report);
        
        assertEquals(1, report.getArtifactsReports().length);
        
        ar = report.getArtifactReport(artifact);
        assertNotNull(ar);
        
        assertEquals(artifact, ar.getArtifact());
        assertEquals(DownloadStatus.NO, ar.getDownloadStatus());
    }

    public void testCheckModified() throws Exception {
        FileSystemResolver resolver = new FileSystemResolver();
        resolver.setName("test");
        resolver.setIvy(_ivy);
        _ivy.addResolver(resolver);
        assertEquals("test", resolver.getName());
        
        resolver.addIvyPattern("test"+FS+"repositories"+FS+"checkmodified"+FS+"ivy-[revision].xml");
        FileUtil.copy(new File("test/repositories/checkmodified/ivy-1.0-before.xml"), new File("test/repositories/checkmodified/ivy-1.0.xml"), null);
        
        ModuleRevisionId mrid = ModuleRevisionId.newInstance("org1", "mod1.1", "1.0");
        ResolvedModuleRevision rmr = resolver.getDependency(new DefaultDependencyDescriptor(mrid, false), _data);
        assertNotNull(rmr);
        
        assertEquals(mrid, rmr.getId());
        Date pubdate = new GregorianCalendar(2004, 10, 1, 11, 0, 0).getTime();
        assertEquals(pubdate, rmr.getPublicationDate());
                
        // updates ivy file in repository
        FileUtil.copy(new File("test/repositories/checkmodified/ivy-1.0-after.xml"), new File("test/repositories/checkmodified/ivy-1.0.xml"), null);
        
        // should not get the new version
        resolver.setCheckmodified(false);
        rmr = resolver.getDependency(new DefaultDependencyDescriptor(mrid, false), _data);
        assertNotNull(rmr);
        
        assertEquals(mrid, rmr.getId());
        pubdate = new GregorianCalendar(2004, 10, 1, 11, 0, 0).getTime();
        assertEquals(pubdate, rmr.getPublicationDate());

        // should now get the new version
        resolver.setCheckmodified(true);
        rmr = resolver.getDependency(new DefaultDependencyDescriptor(mrid, false), _data);
        assertNotNull(rmr);
        
        assertEquals(mrid, rmr.getId());
        pubdate = new GregorianCalendar(2005, 4, 1, 11, 0, 0).getTime();
        assertEquals(pubdate, rmr.getPublicationDate());
    }

    public void testLatestTime() throws Exception {
        FileSystemResolver resolver = new FileSystemResolver();
        resolver.setName("test");
        resolver.setIvy(_ivy);
        assertEquals("test", resolver.getName());
        
        resolver.addIvyPattern(IVY_PATTERN);
        resolver.addArtifactPattern("test/repositories/1/[organisation]/[module]/[type]s/[artifact]-[revision].[type]");
        
        resolver.setLatestStrategy(new LatestTimeStrategy());
        
        ModuleRevisionId mrid = ModuleRevisionId.newInstance("org1", "mod1.1", "2.0");
        ResolvedModuleRevision rmr = resolver.getDependency(new DefaultDependencyDescriptor(ModuleRevisionId.newInstance("org1", "mod1.1", "latest.integration"), false), _data);
        assertNotNull(rmr);
        
        assertEquals(mrid, rmr.getId());
        Date pubdate = new GregorianCalendar(2004, 10, 15, 11, 0, 0).getTime();
        assertEquals(pubdate, rmr.getPublicationDate());
    }

    public void testLatestRevision() throws Exception {
        FileSystemResolver resolver = new FileSystemResolver();
        resolver.setName("test");
        resolver.setIvy(_ivy);
        assertEquals("test", resolver.getName());
        
        resolver.addIvyPattern(IVY_PATTERN);
        resolver.addArtifactPattern("test/repositories/1/[organisation]/[module]/[type]s/[artifact]-[revision].[type]");
        
        resolver.setLatestStrategy(new LatestRevisionStrategy());
        
        ModuleRevisionId mrid = ModuleRevisionId.newInstance("org1", "mod1.1", "2.0");
        ResolvedModuleRevision rmr = resolver.getDependency(new DefaultDependencyDescriptor(ModuleRevisionId.newInstance("org1", "mod1.1", "latest.integration"), false), _data);
        assertNotNull(rmr);
        
        assertEquals(mrid, rmr.getId());
        Date pubdate = new GregorianCalendar(2004, 10, 15, 11, 0, 0).getTime();
        assertEquals(pubdate, rmr.getPublicationDate());
    }

    public void testRelativePath() throws Exception {
        FileSystemResolver resolver = new FileSystemResolver();
        resolver.setName("test");
        resolver.setIvy(_ivy);
        assertEquals("test", resolver.getName());
        
        resolver.addIvyPattern(new File("src/java").getAbsolutePath()+"/../../"+IVY_PATTERN);
        resolver.addArtifactPattern("src/../test/repositories/1/[organisation]/[module]/[type]s/[artifact]-[revision].[type]");
        
        resolver.setLatestStrategy(new LatestRevisionStrategy());
        
        ModuleRevisionId mrid = ModuleRevisionId.newInstance("org1", "mod1.1", "2.0");
        ResolvedModuleRevision rmr = resolver.getDependency(new DefaultDependencyDescriptor(ModuleRevisionId.newInstance("org1", "mod1.1", "latest.integration"), false), _data);
        assertNotNull(rmr);
        
        assertEquals(mrid, rmr.getId());
        Date pubdate = new GregorianCalendar(2004, 10, 15, 11, 0, 0).getTime();
        assertEquals(pubdate, rmr.getPublicationDate());
    }

    public void testFormattedLatestTime() throws Exception {
        FileSystemResolver resolver = new FileSystemResolver();
        resolver.setName("test");
        resolver.setIvy(_ivy);
        assertEquals("test", resolver.getName());
        
        resolver.addIvyPattern(IVY_PATTERN);
        resolver.addArtifactPattern("test/repositories/1/[organisation]/[module]/[type]s/[artifact]-[revision].[type]");
        
        resolver.setLatestStrategy(new LatestTimeStrategy());
        
        ModuleRevisionId mrid = ModuleRevisionId.newInstance("org1", "mod1.1", "1.1");
        ResolvedModuleRevision rmr = resolver.getDependency(new DefaultDependencyDescriptor(ModuleRevisionId.newInstance("org1", "mod1.1", "1+"), false), _data);
        assertNotNull(rmr);
        
        assertEquals(mrid, rmr.getId());
        Date pubdate = new GregorianCalendar(2004, 10, 2, 11, 0, 0).getTime();
        assertEquals(pubdate, rmr.getPublicationDate());
    }

    public void testFormattedLatestRevision() throws Exception {
        FileSystemResolver resolver = new FileSystemResolver();
        resolver.setName("test");
        resolver.setIvy(_ivy);
        assertEquals("test", resolver.getName());
        
        resolver.addIvyPattern(IVY_PATTERN);
        resolver.addArtifactPattern("test/repositories/1/[organisation]/[module]/[type]s/[artifact]-[revision].[type]");
        
        resolver.setLatestStrategy(new LatestRevisionStrategy());
        
        ModuleRevisionId mrid = ModuleRevisionId.newInstance("org1", "mod1.1", "1.1");
        ResolvedModuleRevision rmr = resolver.getDependency(new DefaultDependencyDescriptor(ModuleRevisionId.newInstance("org1", "mod1.1", "1+"), false), _data);
        assertNotNull(rmr);
        
        assertEquals(mrid, rmr.getId());
        Date pubdate = new GregorianCalendar(2004, 10, 2, 11, 0, 0).getTime();
        assertEquals(pubdate, rmr.getPublicationDate());
    }

    public void testPublish() throws Exception {
        try {
            FileSystemResolver resolver = new FileSystemResolver();
            resolver.setName("test");
            resolver.setIvy(_ivy);
            assertEquals("test", resolver.getName());
            
            resolver.addIvyPattern("test"+FS+"repositories"+FS+"1"+FS+"[organisation]"+FS+"[module]"+FS+"[revision]"+FS+"[artifact].[ext]");
            resolver.addArtifactPattern("test/repositories/1/[organisation]/[module]/[type]s/[artifact]-[revision].[ext]");
            
            ModuleRevisionId mrid = ModuleRevisionId.newInstance("myorg", "mymodule", "myrevision");
            Artifact ivyArtifact = new DefaultArtifact(mrid, new Date(), "ivy", "ivy", "xml");
            Artifact artifact = new DefaultArtifact(mrid, new Date(), "myartifact", "mytype", "myext");
            File src = new File("test/repositories/ivyconf.xml");
            resolver.publish(ivyArtifact, src);
            resolver.publish(artifact, src);
            
            assertTrue(new File("test/repositories/1/myorg/mymodule/myrevision/ivy.xml").exists());
            assertTrue(new File("test/repositories/1/myorg/mymodule/mytypes/myartifact-myrevision.myext").exists());
        } finally {
            Delete del = new Delete();
            del.setProject(new Project());
            del.setDir(new File("test/repositories/1/myorg"));
            del.execute();
        }
    }
    
    public void testListing() throws Exception {
        FileSystemResolver resolver = new FileSystemResolver();
        resolver.setName("test");
        resolver.setIvy(_ivy);
        assertEquals("test", resolver.getName());
        
        resolver.addIvyPattern(IVY_PATTERN);
        resolver.addArtifactPattern("test/repositories/1/[organisation]/[module]/[type]s/[artifact]-[revision].[ext]");

        OrganisationEntry[] orgs = resolver.listOrganisations();
        ResolverTestHelper.assertOrganisationEntries(resolver, new String[] {"org1", "org2", "org6"}, orgs);
        
        OrganisationEntry org = ResolverTestHelper.getEntry(orgs, "org1");
        ModuleEntry[] mods = resolver.listModules(org);
        ResolverTestHelper.assertModuleEntries(resolver, org, new String[] {"mod1.1", "mod1.2", "mod1.3", "mod1.4"}, mods);

        ModuleEntry mod = ResolverTestHelper.getEntry(mods, "mod1.1");
        RevisionEntry[] revs = resolver.listRevisions(mod);
        ResolverTestHelper.assertRevisionEntries(resolver, mod, new String[] {"1.0", "1.0.1", "1.1", "2.0"}, revs);

        mod = ResolverTestHelper.getEntry(mods, "mod1.2");
        revs = resolver.listRevisions(mod);
        ResolverTestHelper.assertRevisionEntries(resolver, mod, new String[] {"2.0", "2.1", "2.2"}, revs);
    }

}
