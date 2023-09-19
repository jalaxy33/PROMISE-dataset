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
public interface Artifact {
    /**
     * Returns the resolved module revision id for this artifact
     * @return
     */
    ModuleRevisionId getModuleRevisionId();
    /**
     * Returns the resolved publication date for this artifact
     * @return the resolved publication date
     */
    Date getPublicationDate();
    String getName();
    String getType();
    String getExt();
    String[] getConfigurations();

    /**
     * @return the id of the artifact
     */
    ArtifactRevisionId getId();
}
