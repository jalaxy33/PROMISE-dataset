/*
 * This file is subject to the license found in LICENCE.TXT in the root directory of the project.
 * 
 * version 1.1
 */
package fr.jayasoft.ivy.resolver;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import fr.jayasoft.ivy.Artifact;
import fr.jayasoft.ivy.DefaultArtifact;
import fr.jayasoft.ivy.DefaultModuleDescriptor;
import fr.jayasoft.ivy.DefaultModuleRevision;
import fr.jayasoft.ivy.DependencyDescriptor;
import fr.jayasoft.ivy.DependencyResolver;
import fr.jayasoft.ivy.Ivy;
import fr.jayasoft.ivy.IvyNode;
import fr.jayasoft.ivy.LatestStrategy;
import fr.jayasoft.ivy.ModuleDescriptor;
import fr.jayasoft.ivy.ModuleRevisionId;
import fr.jayasoft.ivy.ResolveData;
import fr.jayasoft.ivy.ResolvedModuleRevision;
import fr.jayasoft.ivy.Status;
import fr.jayasoft.ivy.report.ArtifactDownloadReport;
import fr.jayasoft.ivy.report.DownloadReport;
import fr.jayasoft.ivy.report.DownloadStatus;
import fr.jayasoft.ivy.repository.Resource;
import fr.jayasoft.ivy.util.IvyPatternHelper;
import fr.jayasoft.ivy.util.Message;
import fr.jayasoft.ivy.xml.XmlModuleDescriptorParser;
import fr.jayasoft.ivy.xml.XmlModuleDescriptorUpdater;
import fr.jayasoft.ivy.xml.XmlModuleDescriptorWriter;

/**
 * @author Xavier Hanin
 *
 */
public abstract class BasicResolver extends AbstractResolver {
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");

    protected String _workspaceName;
    /**
     * True if the files resolved are dependent of the environment from which they have been resolved, false otherwise. In general, relative paths are dependent of the environment, and absolute paths including machine reference are not. 
     */
    private boolean _envDependent = true;

    /**
     * The latest strategy to use to find latest among several artifacts
     */
    private LatestStrategy _latestStrategy;

    private String _latestStrategyName;

    private List _ivyattempts = new ArrayList();
    private Map _artattempts = new HashMap();

    private Boolean _checkmodified = null;
    
    
    public BasicResolver() {
        _workspaceName = Ivy.getLocalHostName();
    }

    public String getWorkspaceName() {
        return _workspaceName;
    }

    public void setWorkspaceName(String workspaceName) {
        _workspaceName = workspaceName;
    }

    public boolean isEnvDependent() {
        return _envDependent;
    }

    public void setEnvDependent(boolean envDependent) {
        _envDependent = envDependent;
    }

    /**
     * True if this resolver should check lastmodified date to know if ivy files are up to date.
     * @return
     */
    public boolean isCheckmodified() {
        if (_checkmodified == null) {
            if (getIvy() != null) {
                String check = getIvy().getVariable("ivy.resolver.default.check.modified");
                return check != null ? Boolean.valueOf(check).booleanValue() : false;
            } else {
                return false;
            }
        } else {
            return _checkmodified.booleanValue();
        }
    }
    

    public void setCheckmodified(boolean check) {
        _checkmodified = Boolean.valueOf(check);
    }
    
    public LatestStrategy getLatestStrategy() {        
        if (_latestStrategy == null) {
            if (getIvy() != null) {
                if (_latestStrategyName != null) {
                    _latestStrategy = getIvy().getLatestStrategy(_latestStrategyName);
                    if (_latestStrategy == null) {
                        Message.error("unknown latest strategy: "+_latestStrategyName);
                        _latestStrategy = getIvy().getDefaultLatestStrategy();
                    }
                } else {
                    _latestStrategy = getIvy().getDefaultLatestStrategy();
                    Message.debug(getName()+": no latest strategy defined: using default");
                }
            } else {
                throw new IllegalStateException("no ivy instance found: impossible to get a latest strategy without ivy instance");
            }
        }
        return _latestStrategy;
    }
    

    public void setLatestStrategy(LatestStrategy latestStrategy) {
        _latestStrategy = latestStrategy;
    }    

    public void setLatest(String strategyName) {
        _latestStrategyName = strategyName;
    }    

    public ResolvedModuleRevision getDependency(DependencyDescriptor dd, ResolveData data) throws ParseException {
        _ivyattempts.clear();
        boolean downloaded = false;
        boolean searched = false;
        ModuleRevisionId mrid = dd.getDependencyRevisionId();
    	// check revision
		int index = mrid.getRevision().indexOf("@");
		if (index != -1 && !mrid.getRevision().substring(index+1).equals(_workspaceName)) {
            Message.verbose("\t"+getName()+": unhandled revision => "+mrid.getRevision());
            return null;
		}
        
        if (!mrid.isExactRevision() && !acceptLatest()) {
            Message.error("latest revisions not handled by "+getClass().getName()+". impossible to resolve "+mrid);
            return null;
        }
    	
        // if we do not have to check modified and if the revision is exact, we first 
        // search for it in cache
        if (mrid.isExactRevision() && !isCheckmodified()) {
            ResolvedModuleRevision rmr = data.getIvy().findModuleInCache(mrid, data.getCache(), doValidate(data));
            if (rmr != null) {
                Message.verbose("\t"+getName()+": revision in cache: "+mrid);
                return rmr;
            }
        }
        URL cachedIvyURL = null;
        ResolvedResource ivyRef = findIvyFileRef(dd, data);
        searched = true;
        
        // get module descriptor
        ModuleDescriptor md;
        if (ivyRef == null) {
            md = DefaultModuleDescriptor.newDefaultInstance(mrid, dd.getAllDependencyArtifactsIncludes());
            ResolvedResource artifactRef = findFirstArtifactRef(md, dd, data);
            if (artifactRef == null) {
                Message.verbose("\t"+getName()+": no ivy file nor artifact found for "+mrid);
                logIvyNotFound(mrid);
                String[] conf = md.getConfigurationsNames();
                for (int i = 0; i < conf.length; i++) {
                    Artifact[] artifacts = md.getArtifacts(conf[i]);
                    for (int j = 0; j < artifacts.length; j++) {
                        logArtifactNotFound(artifacts[j]);
                    }
                }
                return null;
            } else {
                Message.verbose("\t"+getName()+": no ivy file found for "+mrid+": using default data");            
                logIvyNotFound(mrid);
    	        if (!mrid.isExactRevision()) {
    	            md.setResolvedModuleRevisionId(new ModuleRevisionId(mrid.getModuleId(), artifactRef.getRevision()));
    	        }
            }
        } else {
            Message.verbose("\t"+getName()+": found ivy file for "+mrid);
            Message.verbose("\t\t=> "+ivyRef);

            ModuleRevisionId resolvedMrid = mrid;
            // first check if this dependency has not yet been resolved
            if (!mrid.isExactRevision()) {
                resolvedMrid = new ModuleRevisionId(mrid.getModuleId(), ivyRef.getRevision());
                IvyNode node = data.getNode(resolvedMrid);
                if (node != null && node.getModuleRevision() != null) {
                    // this revision has already be resolved : return it
                    Message.verbose("\t"+getName()+": revision already resolved: "+resolvedMrid);
                    return searchedRmr(node.getModuleRevision());
                }
            }
            
            // now let's see if we can find it in cache and if it is up to date
            ResolvedModuleRevision rmr = data.getIvy().findModuleInCache(resolvedMrid, data.getCache(), doValidate(data));
            if (rmr != null) {
                if (!isCheckmodified()) {
                    Message.verbose("\t"+getName()+": revision in cache: "+resolvedMrid);
                    return searchedRmr(rmr);
                }
                long repLastModified = ivyRef.getLastModified();
                long cacheLastModified = rmr.getDescriptor().getLastModified(); 
                if (rmr.getDescriptor().isDefault() || repLastModified <= cacheLastModified) {
                    Message.verbose("\t"+getName()+": revision in cache (not updated): "+resolvedMrid);
                    return searchedRmr(rmr);
                } else {
                    Message.verbose("\t"+getName()+": revision in cache is not up to date: "+resolvedMrid);
                }
            }
            
            // now download ivy file and parse it
            try {
                // temp file is used to prevent downloading twice
                File ivyTempFile = File.createTempFile("ivy", "xml"); 
                ivyTempFile.deleteOnExit();
                Message.debug("\t"+getName()+": downloading "+ivyRef.getResource());
                get(ivyRef.getResource(), ivyTempFile);
                downloaded = true;
                try {
                    cachedIvyURL = ivyTempFile.toURL();
                } catch (MalformedURLException ex) {
                    Message.warn("malformed url exception for temp file: "+ivyTempFile+": "+ex.getMessage());
                    return null;
                }
            } catch (IOException ex) {
                Message.warn("problem while downloading ivy file: "+ivyRef.getResource()+": "+ex.getMessage());
                return null;
            }
            try {
                md = XmlModuleDescriptorParser.parseDescriptor(data.getIvy(), cachedIvyURL, ivyRef.getResource(), doValidate(data));
                Message.debug("\t"+getName()+": parsed downloaded ivy file for "+mrid+" parsed="+md.getModuleRevisionId());
            } catch (IOException ex) {
                Message.warn("io problem while parsing ivy file: "+ivyRef.getResource()+": "+ex.getMessage());
                return null;
            }
        }
        md.setResolverName(getName());
        
        // check module descriptor revision
        if (mrid.getRevision().startsWith("latest.")) {
            String askedStatus = mrid.getRevision().substring("latest.".length());
            if (Status.getPriority(askedStatus) < Status.getPriority(md.getStatus())) {
                Message.info("\t"+getName()+": unacceptable status => was="+md.getStatus()+" required="+askedStatus);
                return null;
            }
        } else if (!mrid.acceptRevision(md.getModuleRevisionId().getRevision())) {
            Message.info("\t"+getName()+": unacceptable revision => was="+md.getModuleRevisionId().getRevision()+" required="+mrid.getRevision());
            return null;
        }
        
        // resolve revision
        ModuleRevisionId resolvedMrid = mrid;
        if (!resolvedMrid.isExactRevision()) {
            resolvedMrid = md.getResolvedModuleRevisionId();
            if (resolvedMrid.getRevision() == null || resolvedMrid.getRevision().length() == 0) {
                if (ivyRef.getRevision() == null || ivyRef.getRevision().length() == 0) {
                    resolvedMrid = new ModuleRevisionId(resolvedMrid.getModuleId(), (_envDependent?"##":"")+DATE_FORMAT.format(data.getDate())+"@"+_workspaceName);
                } else {
                    resolvedMrid = new ModuleRevisionId(resolvedMrid.getModuleId(), ivyRef.getRevision());
                }
            }
            Message.verbose("\t\t["+resolvedMrid.getRevision()+"] "+mrid.getModuleId());
        }
        md.setResolvedModuleRevisionId(resolvedMrid);
        
        // resolve and check publication date
        if (data.getDate() != null) {
            long pubDate = getPublicationDate(md, dd, data);
            if (pubDate > data.getDate().getTime()) {
                Message.info("\t"+getName()+": unacceptable publication date => was="+new Date(pubDate)+" required="+data.getDate());
                return null;
            } else if (pubDate == -1) {
                Message.info("\t"+getName()+": impossible to guess publication date: artifact missing for "+mrid);
                return null;
            }
            md.setResolvedPublicationDate(new Date(pubDate));
        }
    
        try {
            File ivyFile = data.getIvy().getIvyFileInCache(data.getCache(), md.getResolvedModuleRevisionId());
	        if (ivyRef == null) {
                // a basic ivy file is written containing default data
	            XmlModuleDescriptorWriter.write(md, ivyFile);
	        } else {
	            // copy and update ivy file from source to cache
                XmlModuleDescriptorUpdater.update(
                        cachedIvyURL, 
                        ivyFile, 
                        Collections.EMPTY_MAP, 
                        md.getStatus(), 
                        md.getResolvedModuleRevisionId().getRevision(), 
                        md.getResolvedPublicationDate(), getName());
                long repLastModified = ivyRef.getLastModified();
                if (repLastModified > 0) {
                    ivyFile.setLastModified(repLastModified);
                }
	        }
        } catch (Exception e) {
            Message.warn("impossible to copy ivy file to cache : "+ivyRef.getResource());
        }
        
        return new DefaultModuleRevision(this, md, searched, downloaded);
    }

    private ResolvedModuleRevision searchedRmr(final ResolvedModuleRevision rmr) {
        // delegate all to previously found except isSearched
        return new ResolvedModuleRevision() {                    
            public boolean isSearched() {
                return true;
            }
        
            public boolean isDownloaded() {
                return rmr.isDownloaded();
            }
        
            public ModuleDescriptor getDescriptor() {
                return rmr.getDescriptor();
            }
        
            public Date getPublicationDate() {
                return rmr.getPublicationDate();
            }
        
            public ModuleRevisionId getId() {
                return rmr.getId();
            }
        
            public DependencyResolver getResolver() {
                return rmr.getResolver();
            }                    
        };
    }
    
    protected void logIvyAttempt(String attempt) {
        _ivyattempts.add(attempt);
        Message.verbose("\t\ttried "+attempt);
    }
    
    protected void logArtifactAttempt(Artifact art, String attempt) {
        List attempts = (List)_artattempts.get(art);
        if (attempts == null) {
            attempts = new ArrayList();
            _artattempts.put(art, attempts);
        }
        attempts.add(attempt);
        Message.verbose("\t\ttried "+attempt);
    }
    
    public void reportFailure() {
        for (ListIterator iter = _ivyattempts.listIterator(); iter.hasNext();) {
            String m = (String)iter.next();
            Message.warn("\t\t"+getName()+": tried "+m);
        }
        for (Iterator iter = _artattempts.keySet().iterator(); iter.hasNext();) {
            Artifact art = (Artifact)iter.next();
            List attempts = (List)_artattempts.get(art);
            if (attempts != null) {
                Message.warn("\t\t"+getName()+": tried artifact "+art+":");
                for (ListIterator iterator = attempts.listIterator(); iterator.hasNext();) {
                    String m = (String)iterator.next();
                    Message.warn("\t\t\t"+m);
                }
            }
        }
    }

    public void reportFailure(Artifact art) {
        List attempts = (List)_artattempts.get(art);
        if (attempts != null) {
            for (ListIterator iter = attempts.listIterator(); iter.hasNext();) {
                String m = (String)iter.next();
                Message.warn("\t\t"+getName()+": tried "+m);
            }
        }
    }

    protected boolean acceptLatest() {
        return true;
    }

    public DownloadReport download(Artifact[] artifacts, Ivy ivy, File cache) {
        _artattempts.clear();
        DownloadReport dr = new DownloadReport();
        for (int i = 0; i < artifacts.length; i++) {
        	final ArtifactDownloadReport adr = new ArtifactDownloadReport(artifacts[i]);
        	dr.addArtifactReport(adr);
        	File archiveFile = ivy.getArchiveFileInCache(cache, artifacts[i]);
        	if (archiveFile.exists()) {
        		Message.verbose("\t[NOT REQUIRED] "+artifacts[i]);
        		adr.setDownloadStatus(DownloadStatus.NO);  
                adr.setSize(archiveFile.length());
        	} else {
                ResolvedResource artifactRef = findArtifactRef(artifacts[i]);
        		if (artifactRef != null) {
    			    long start = System.currentTimeMillis();
        			try {
        			    Message.info("downloading "+artifactRef.getResource()+" ...");
                        File tmp = ivy.getArchiveFileInCache(cache, 
                                new DefaultArtifact(
                                        artifacts[i].getModuleRevisionId(), 
                                        artifacts[i].getPublicationDate(), 
                                        artifacts[i].getName(), 
                                        artifacts[i].getType(), 
                                        artifacts[i].getExt()+".part"));
                        adr.setSize(get(artifactRef.getResource(), tmp));
                        tmp.renameTo(archiveFile);
                		Message.info("\t[SUCCESSFUL ] "+artifacts[i]+" ("+(System.currentTimeMillis()-start)+"ms)");
        				adr.setDownloadStatus(DownloadStatus.SUCCESSFUL);
        			} catch (Exception ex) {
                		Message.warn("\t[FAILED     ] "+artifacts[i]+" ("+(System.currentTimeMillis()-start)+"ms)");
        				adr.setDownloadStatus(DownloadStatus.FAILED);
        			}
        		} else {
        		    logArtifactNotFound(artifacts[i]);
        			adr.setDownloadStatus(DownloadStatus.FAILED);                
        		}
        	}
        }
    	return dr;
    }
    
    public boolean exists(Artifact artifact) {
        ResolvedResource artifactRef = findArtifactRef(artifact);
        if (artifactRef != null) {
            return artifactRef.getResource().exists();
        }
        return false;
    }

    protected long getPublicationDate(ModuleDescriptor md, DependencyDescriptor dd, ResolveData data) {
        if (md.getPublicationDate() != null) {
            return md.getPublicationDate().getTime();
        }
        ResolvedResource artifactRef = findFirstArtifactRef(md, dd, data);
        if (artifactRef != null) {
            return artifactRef.getLastModified();
        }
        return -1;
    }

    public String toString() {
        return getName();
    }

    public OrganisationEntry[] listOrganisations() {
        Collection names = findNames(Collections.EMPTY_MAP, IvyPatternHelper.ORGANISATION_KEY);
        OrganisationEntry[] ret = new OrganisationEntry[names.size()];
        int i =0;
        for (Iterator iter = names.iterator(); iter.hasNext(); i++) {
            String org = (String)iter.next();
            ret[i] = new OrganisationEntry(this, org);
        }
        return ret;
    }

    public ModuleEntry[] listModules(OrganisationEntry org) {
        Map tokenValues = new HashMap();
        tokenValues.put(IvyPatternHelper.ORGANISATION_KEY, org.getOrganisation());
        Collection names = findNames(tokenValues, IvyPatternHelper.MODULE_KEY);
        ModuleEntry[] ret = new ModuleEntry[names.size()];
        int i =0;
        for (Iterator iter = names.iterator(); iter.hasNext(); i++) {
            String name = (String)iter.next();
            ret[i] = new ModuleEntry(org, name);
        }
        return ret;
    }

    public RevisionEntry[] listRevisions(ModuleEntry mod) {
        Map tokenValues = new HashMap();
        tokenValues.put(IvyPatternHelper.ORGANISATION_KEY, mod.getOrganisation());
        tokenValues.put(IvyPatternHelper.MODULE_KEY, mod.getModule());
        Collection names = findNames(tokenValues, IvyPatternHelper.REVISION_KEY);
        RevisionEntry[] ret = new RevisionEntry[names.size()];
        int i =0;
        for (Iterator iter = names.iterator(); iter.hasNext(); i++) {
            String name = (String)iter.next();
            ret[i] = new RevisionEntry(mod, name);
        }
        return ret;
    }

    protected abstract Collection findNames(Map tokenValues, String token);

    protected abstract ResolvedResource findIvyFileRef(DependencyDescriptor dd, ResolveData data);

    protected abstract ResolvedResource findFirstArtifactRef(ModuleDescriptor md, DependencyDescriptor dd, ResolveData data);

    protected abstract ResolvedResource findArtifactRef(Artifact artifact);

    protected abstract long get(Resource resource, File ivyTempFile) throws IOException;

    protected abstract void logIvyNotFound(ModuleRevisionId mrid);    

    protected abstract void logArtifactNotFound(Artifact artifact);


}
