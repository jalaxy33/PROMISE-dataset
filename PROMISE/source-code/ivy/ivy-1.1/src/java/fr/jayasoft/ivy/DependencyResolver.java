/*
 * This file is subject to the license found in LICENCE.TXT in the root directory of the project.
 * 
 * version 1.1
 */
package fr.jayasoft.ivy;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import fr.jayasoft.ivy.report.DownloadReport;
import fr.jayasoft.ivy.resolver.ModuleEntry;
import fr.jayasoft.ivy.resolver.OrganisationEntry;
import fr.jayasoft.ivy.resolver.RevisionEntry;

/**
 * @author x.hanin
 *
 */
public interface DependencyResolver {
    String getName();
    /**
     * Should only be used by configurator
     * @param name the new name of the resolver
     */
    void setName(String name);
    /**
     * Resolve a module by id, getting its module descriptor and
     * resolving the revision if it's a latest one (i.e. a revision
     * uniquely identifying the revision of a module in the current environment -
     * If this revision is not able to identify uniquelely the revision of the module
     * outside of the current environment, then the resolved revision must begin by ##)
     * @throws ParseException
     */
    ResolvedModuleRevision getDependency(DependencyDescriptor dd, ResolveData data) throws ParseException;
    DownloadReport download(Artifact[] artifacts, Ivy ivy, File cache);
    boolean exists(Artifact artifact);
    void publish(Artifact artifact, File src) throws IOException;
    
    /**
     * Reports last resolve failure as Messages
     */
    void reportFailure();
    /**
     * Reports last artifact download failure as Messages
     * @param art
     */
    void reportFailure(Artifact art);
    
    // listing methods, enable to know what is available from this resolver
    // the listing methods must only list entries directly
    // available from them, no recursion is needed as long as sub resolvers
    // are registered in ivy too.
    
    OrganisationEntry[] listOrganisations();
    ModuleEntry[] listModules(OrganisationEntry org);
    RevisionEntry[] listRevisions(ModuleEntry module);
    void dumpConfig();
}
