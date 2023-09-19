/*
 * This file is subject to the license found in LICENCE.TXT in the root directory of the project.
 * 
 * version 1.1
 */
package fr.jayasoft.ivy.util;

/**
 * Listen to copy progression
 */
public interface CopyProgressListener {
    void start(CopyProgressEvent evt);
    void progress(CopyProgressEvent evt);
    void end(CopyProgressEvent evt);
}
