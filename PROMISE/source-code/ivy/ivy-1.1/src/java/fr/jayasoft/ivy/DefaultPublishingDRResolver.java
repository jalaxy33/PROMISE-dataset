/*
 * This file is subject to the license found in LICENCE.TXT in the root directory of the project.
 * 
 * version 1.1
 */
package fr.jayasoft.ivy;


public final class DefaultPublishingDRResolver implements PublishingDependencyRevisionResolver {
    public String resolve(ModuleDescriptor published, String publishedStatus, ModuleDescriptor dependency) {
        return dependency.getResolvedModuleRevisionId().getRevision();
    }
}
