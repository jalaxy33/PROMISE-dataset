/*
 * This file is subject to the license found in LICENCE.TXT in the root directory of the project.
 * 
 * version 1.1
 */
package fr.jayasoft.ivy;

import java.util.Date;

/**
 * @author x.hanin
 *
 */
public interface ResolvedModuleRevision {
    DependencyResolver getResolver();
    ModuleRevisionId getId();
    Date getPublicationDate();
    ModuleDescriptor getDescriptor();
    boolean isDownloaded();
    boolean isSearched();
}
