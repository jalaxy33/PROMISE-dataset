/*
 * This file is subject to the license found in LICENCE.TXT in the root directory of the project.
 * 
 * version 1.1
 */
package fr.jayasoft.ivy.resolver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import fr.jayasoft.ivy.Artifact;
import fr.jayasoft.ivy.DefaultArtifact;
import fr.jayasoft.ivy.DependencyDescriptor;
import fr.jayasoft.ivy.ModuleDescriptor;
import fr.jayasoft.ivy.ModuleRevisionId;
import fr.jayasoft.ivy.ResolveData;
import fr.jayasoft.ivy.repository.Resource;
import fr.jayasoft.ivy.util.IvyPattern;
import fr.jayasoft.ivy.util.IvyPatternHelper;
import fr.jayasoft.ivy.util.Message;

/**
 * @author Xavier Hanin
 *
 */
public abstract class AbstractResourceResolver extends BasicResolver {
    
    private List _ivyPatterns = new ArrayList(); // List (String pattern)
    private List _artifactPatterns = new ArrayList();  // List (String pattern)

    
    public AbstractResourceResolver() {
    }

    protected ResolvedResource findIvyFileRef(DependencyDescriptor dd, ResolveData data) {
        return findResourceUsingPatterns(dd.getDependencyRevisionId(), _ivyPatterns, "ivy", "ivy", "xml", data.getDate());
    }

    protected ResolvedResource findFirstArtifactRef(ModuleDescriptor md, DependencyDescriptor dd, ResolveData data) {
        ResolvedResource ret = null;
        String[] conf = md.getConfigurationsNames();
        for (int i = 0; i < conf.length; i++) {
            Artifact[] artifacts = md.getArtifacts(conf[i]);
            for (int j = 0; j < artifacts.length; j++) {
                ret = findResourceUsingPatterns(dd.getDependencyRevisionId(), _artifactPatterns, artifacts[j].getName(), artifacts[j].getType(), artifacts[j].getExt(), data.getDate());
                if (ret != null) {
                    return ret;
                }
            }
        }
        return null;
    }

    protected ResolvedResource findArtifactRef(Artifact artifact) {
        return findResourceUsingPatterns(artifact.getModuleRevisionId(), _artifactPatterns, artifact.getName(), artifact.getType(), artifact.getExt(), null);
    }

    protected ResolvedResource findResourceUsingPatterns(ModuleRevisionId moduleRevision, List patternList, String artifact, String type, String ext, Date date) {
        ResolvedResource rres = null;
        for (Iterator iter = patternList.iterator(); iter.hasNext() && rres == null;) {
            String pattern = (String)iter.next();
            rres = findResourceUsingPattern(moduleRevision, pattern, artifact, type, ext, date);
        }
        return rres;
    }
    
    protected abstract ResolvedResource findResourceUsingPattern(ModuleRevisionId mrid, String pattern, String artifact, String type, String ext, Date date);
    protected abstract ResolvedResource[] findAll(ModuleRevisionId mrid, String pattern, String artifact, String type, String ext);
    protected abstract long get(Resource resource, File ivyTempFile) throws IOException;    

    /**
     * Output message to log indicating what have been done to look for an artifact which
     * has finally not been found
     * 
     * @param artifact the artifact which has not been found
     */
    protected void logIvyNotFound(ModuleRevisionId mrid) {
        Artifact artifact = new DefaultArtifact(mrid, new Date(), "ivy", "ivy", "xml");
        String revisionToken = mrid.getRevision().startsWith("latest.")?"[any "+mrid.getRevision().substring("latest.".length())+"]":"["+mrid.getRevision()+"]";
        Artifact latestArtifact = new DefaultArtifact(new ModuleRevisionId(mrid.getModuleId(), revisionToken), new Date(), "ivy", "ivy", "xml");
        if (_ivyPatterns.isEmpty()) {
            logIvyAttempt("no ivy pattern => no attempt to find ivy file for "+mrid);
        } else {
            for (Iterator iter = _ivyPatterns.iterator(); iter.hasNext();) {
                String pattern = (String)iter.next();
                String resolvedFileName = IvyPatternHelper.substitute(pattern, artifact);
                logIvyAttempt(resolvedFileName);
                if (!mrid.isExactRevision()) {
                    resolvedFileName = IvyPatternHelper.substitute(pattern, latestArtifact);
                    logIvyAttempt(resolvedFileName);
                }
            }
        }
    }

    /**
     * Output message to log indicating what have been done to look for an artifact which
     * has finally not been found
     * 
     * @param artifact the artifact which has not been found
     */
    protected void logArtifactNotFound(Artifact artifact) {
        if (_artifactPatterns.isEmpty()) {
            logArtifactAttempt(artifact, "no artifact pattern => no attempt to find artifact "+artifact);
        }
        for (Iterator iter = _artifactPatterns.iterator(); iter.hasNext();) {
            String pattern = (String)iter.next();
            String resolvedFileName = IvyPatternHelper.substitute(pattern, artifact);
            logArtifactAttempt(artifact, resolvedFileName);
        }
    }

    protected Collection findNames(Map tokenValues, String token) {
        Collection names = new HashSet();
        names.addAll(findIvyNames(tokenValues, token));
        names.addAll(findArtifactNames(tokenValues, token));
        return names;
    }

    protected Collection findIvyNames(Map tokenValues, String token) {
        Collection names = new HashSet();
        tokenValues = new HashMap(tokenValues);
        tokenValues.put(IvyPatternHelper.ARTIFACT_KEY, "ivy");
        tokenValues.put(IvyPatternHelper.TYPE_KEY, "ivy");
        tokenValues.put(IvyPatternHelper.EXT_KEY, "xml");
        findTokenValues(names, getIvyPatterns(), tokenValues, token);
        getIvy().filterIgnore(names);
        return names;
    }
    
    protected Collection findArtifactNames(Map tokenValues, String token) {
        Collection names = new HashSet();
        tokenValues = new HashMap(tokenValues);
        tokenValues.put(IvyPatternHelper.ARTIFACT_KEY, tokenValues.get(IvyPatternHelper.MODULE_KEY));
        tokenValues.put(IvyPatternHelper.TYPE_KEY, "jar");
        tokenValues.put(IvyPatternHelper.EXT_KEY, "jar");
        findTokenValues(names, getArtifactPatterns(), tokenValues, token);
        getIvy().filterIgnore(names);
        return names;
    }

    // should be overridden by subclasses wanting to have listing features
    protected void findTokenValues(Collection names, List patterns, Map tokenValues, String token) {
    }
    /**
     * example of pattern : ~/Workspace/[module]/[module].ivy.xml
     * @param pattern
     */
    public void addIvyPattern(String pattern) {
        _ivyPatterns.add(pattern);
    }

    public void addArtifactPattern(String pattern) {
        _artifactPatterns.add(pattern);
    }
    
    public List getIvyPatterns() {
        return Collections.unmodifiableList(_ivyPatterns);
    }

    public List getArtifactPatterns() {
        return Collections.unmodifiableList(_artifactPatterns);
    }
    protected void setIvyPatterns(List ivyPatterns) {
        _ivyPatterns = ivyPatterns;
    }
    protected void setArtifactPatterns(List artifactPatterns) {
        _artifactPatterns = artifactPatterns;
    }

    /*
     * Methods respecting ivy conf method specifications
     */
    public void addConfiguredIvy(IvyPattern p) {
        _ivyPatterns.add(p.getPattern());
    }

    public void addConfiguredArtifact(IvyPattern p) {
        _artifactPatterns.add(p.getPattern());
    }
    
    public void dumpConfig() {
        super.dumpConfig();
        Message.debug("\t\tivy patterns:");
        for (ListIterator iter = _ivyPatterns.listIterator(); iter.hasNext();) {
            String p = (String)iter.next();
            Message.debug("\t\t\t"+p);
        }
        Message.debug("\t\tartifact patterns:");
        for (ListIterator iter = _artifactPatterns.listIterator(); iter.hasNext();) {
            String p = (String)iter.next();
            Message.debug("\t\t\t"+p);
        }
    }
}
