/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "Ant" and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

/*
 * Since the initial version of this file was deveolped on the clock on
 * an NSF grant I should say the following boilerplate:
 *
 * This material is based upon work supported by the National Science
 * Foundaton under Grant No. EIA-0196404. Any opinions, findings, and
 * conclusions or recommendations expressed in this material are those
 * of the author and do not necessarily reflect the views of the
 * National Science Foundation.
 */

package org.apache.tools.ant.taskdefs.optional.unix;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.condition.Os;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.FileSet;

/**
 * @author Patrick G. Heck
 *         <a href="mailto:gus.heck@olin.edu">gus.heck@olin.edu</a>
 * @since Ant 1.6
 *
 * @ant.task category="filesystem"
 */

public abstract class AbstractAccessTask
    extends org.apache.tools.ant.taskdefs.ExecuteOn {

    /**
     * Chmod task for setting file and directory permissions.
     */
    public AbstractAccessTask() {
        super.setParallel(true);
        super.setSkipEmptyFilesets(true);
    }

    /**
     * Set the file which should have its access attributes modified.
     */
    public void setFile(File src) {
        FileSet fs = new FileSet();
        fs.setFile(src);
        addFileset(fs);
    }

    /**
     * Prevent the user from specifying a different command.
     *
     * @ant.attribute ignore="true"
     * @param cmdl A user supplied command line that we won't accept.
     */
    public void setCommand(Commandline cmdl) {
        throw new BuildException(getTaskType()
                                 + " doesn\'t support the command attribute",
                                 getLocation());
    }

    /**
     * Prevent the skipping of empty filesets
     *
     * @ant.attribute ignore="true"
     * @param skip A user supplied boolean we won't accept.
     */
    public void setSkipEmptyFilesets(boolean skip) {
        throw new BuildException(getTaskType() + " doesn\'t support the "
                                 + "skipemptyfileset attribute",
                                 getLocation());
    }

    /**
     * Prevent the use of the addsourcefile atribute.
     *
     * @ant.attribute ignore="true"
     * @param b A user supplied boolean we won't accept.
     */
    public void setAddsourcefile(boolean b) {
        throw new BuildException(getTaskType()
            + " doesn\'t support the addsourcefile attribute", getLocation());
    }

    /**
     * Automatically approve Unix OS's.
     */
    protected boolean isValidOs() {
        return Os.isFamily("unix") && super.isValidOs();
    }
}
