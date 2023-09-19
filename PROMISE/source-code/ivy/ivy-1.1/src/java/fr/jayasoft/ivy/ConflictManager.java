/*
 * This file is subject to the license found in LICENCE.TXT in the root directory of the project.
 * 
 * version 1.1
 */
package fr.jayasoft.ivy;

import java.util.Collection;

public interface ConflictManager {
    /**
     * Resolves the eventual conflicts found in the given collection of IvyNode.
     * This method return a Collection of IvyNode which have not been evicted.
     * The returned collection must contain at least one IvyNode.
     * The given conflicts Collection contains at least two IvyNode.
     * @param parent the ivy node parent for which the conflict is to be resolved
     * @param conflicts the collection of IvyNode to check for conflicts
     * @return a Collection of IvyNode which have not been evicted
     */
    Collection resolveConflicts(IvyNode parent, Collection conflicts);
    String getName();
}
