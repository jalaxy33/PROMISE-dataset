/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
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

package org.apache.tools.ant.taskdefs.optional.vss;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.Path;

/**
 * Performs CheckOut commands to Microsoft Visual SourceSafe.
 *
 * @author Martin Poeschl
 * @author Jesse Stockall
 *
 * @ant.task name="vsscheckout" category="scm"
 * @ant.attribute.group name="vdl" description="Only one of version, date or label"
 */
public class MSVSSCHECKOUT extends MSVSS {

    /**
     * Builds a command line to execute ss.
     * @return     The constructed commandline.
     */
    protected Commandline buildCmdLine() {
        Commandline commandLine = new Commandline();

        // first off, make sure that we've got a command and a vssdir ...
        if (getVsspath() == null) {
            String msg = "vsspath attribute must be set!";
            throw new BuildException(msg, getLocation());
        }

        // build the command line from what we got the format is
        // ss Checkout VSS items [-G] [-C] [-H] [-I-] [-N] [-O] [-R] [-V] [-Y] [-?]
        // as specified in the SS.EXE help
        commandLine.setExecutable(getSSCommand());
        commandLine.createArgument().setValue(COMMAND_CHECKOUT);

        // VSS items
        commandLine.createArgument().setValue(getVsspath());
        // -GL
        commandLine.createArgument().setValue(getLocalpath());
        // -I- or -I-Y or -I-N
        commandLine.createArgument().setValue(getAutoresponse());
        // -R
        commandLine.createArgument().setValue(getRecursive());
        // -V
        commandLine.createArgument().setValue(getVersionDateLabel());
        // -Y
        commandLine.createArgument().setValue(getLogin());
        // -G
        commandLine.createArgument().setValue(getFileTimeStamp());
        // -GWS or -GWR
        commandLine.createArgument().setValue(getWritableFiles());
        // -G-
        commandLine.createArgument().setValue(getGetLocalCopy());

        return commandLine;
    }

    /**
     * Override the project working directory.
     *
     * @param   localPath   The path on disk.
     */
    public void setLocalpath(Path localPath) {
        super.setInternalLocalPath(localPath.toString());
    }

    /**
     * Check-out files recursively. Defaults to false.
     *
     * @param recursive  The boolean value for recursive.
     */
    public void setRecursive(boolean recursive) {
        super.setInternalRecursive(recursive);
    }

    /**
     * Version to check-out.
     *
     * @param  version The version to check-out.
     *
     * @ant.attribute group="vdl"
     */
    public void setVersion(String version) {
        super.setInternalVersion(version);
    }

    /**
     * Date to check-out.
     *
     * @param  date The date to check-out.
     *
     * @ant.attribute group="vdl"
     */
    public void setDate(String date) {
        super.setInternalDate(date);
    }

    /**
     * Label to check-out.
     *
     * @param  label The label to check-out.
     *
     * @ant.attribute group="vdl"
     */
    public void setLabel(String label) {
        super.setInternalLabel(label);
    }

    /**
     * Autoresponce behaviour. Valid options are Y and N.
     *
     * @param response The auto response value.
     */
    public void setAutoresponse(String response) {
        super.setInternalAutoResponse(response);
    }

    /**
     * Date and time stamp given to the local copy. Defaults to <code>current</code>.
     *
     * @param timestamp     The file time stamping behaviour.
     */
    public void setFileTimeStamp(CurrentModUpdated timestamp) {
        super.setInternalFileTimeStamp(timestamp);
    }

    /**
     * Action taken when local files are writable. Defaults to <code>fail</code>.
     * <p>
     * Due to ss.exe returning with an exit code of '100' for both errors and when
     * a file has been skipped, <code>failonerror</code> is set to false when using
     * the <code>skip</code> option.
     * </p>
     *
     * @param files     The writable files behaviour
     */
    public void setWritableFiles(WritableFiles files) {
        super.setInternalWritableFiles(files);
    }

    /**
     * Retrieve a local copy during a checkout. Defaults to true.
     *
     * @param get   The get local copy behaviour
     */
    public void setGetLocalCopy(boolean get) {
        super.setInternalGetLocalCopy(get);
    }
}
