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
package org.apache.tools.ant.taskdefs.optional;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.ExecuteStreamHandler;
import org.apache.tools.ant.taskdefs.LogOutputStream;
import org.apache.tools.ant.taskdefs.LogStreamHandler;
import org.apache.tools.ant.taskdefs.PumpStreamHandler;
import org.apache.tools.ant.taskdefs.condition.Os;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.Path;

/**
 * Invokes the rpm tool to build a Linux installation file.
 *
 * @author lucas@collab.net
 */
public class Rpm extends Task {

    /**
     * the spec file
     */
    private String specFile;

    /**
     * the rpm top dir
     */
    private File topDir;

    /**
     * the rpm command to use
     */
    private String command = "-bb";

    /**
     * The executable to use for building the packages.
     * @since Ant 1.6
     */
    private String rpmBuildCommand = null;

    /**
     * clean BUILD directory
     */
    private boolean cleanBuildDir = false;

    /**
     * remove spec file
     */
    private boolean removeSpec = false;

    /**
     * remove sources
     */
    private boolean removeSource = false;

    /**
     * the file to direct standard output from the command
     */
    private File output;

    /**
     * the file to direct standard error from the command
     */
    private File error;

    /**
     * Execute the task
     *
     * @throws BuildException is there is a problem in the task execution.
     */
    public void execute() throws BuildException {

        Commandline toExecute = new Commandline();

        toExecute.setExecutable(rpmBuildCommand == null
                                ? guessRpmBuildCommand()
                                : rpmBuildCommand);
        if (topDir != null) {
            toExecute.createArgument().setValue("--define");
            toExecute.createArgument().setValue("_topdir" + topDir);
        }

        toExecute.createArgument().setLine(command);

        if (cleanBuildDir) {
            toExecute.createArgument().setValue("--clean");
        }
        if (removeSpec) {
            toExecute.createArgument().setValue("--rmspec");
        }
        if (removeSource) {
            toExecute.createArgument().setValue("--rmsource");
        }

        toExecute.createArgument().setValue("SPECS/" + specFile);

        ExecuteStreamHandler streamhandler = null;
        OutputStream outputstream = null;
        OutputStream errorstream = null;
        if (error == null && output == null) {
            streamhandler = new LogStreamHandler(this, Project.MSG_INFO,
                                                 Project.MSG_WARN);
        } else {
            if (output != null) {
                try {
                    BufferedOutputStream bos
                        = new BufferedOutputStream(new FileOutputStream(output));
                    outputstream = new PrintStream(bos);
                } catch (IOException e) {
                    throw new BuildException(e, getLocation());
                }
            } else {
                outputstream = new LogOutputStream(this, Project.MSG_INFO);
            }
            if (error != null) {
                try {
                    BufferedOutputStream bos
                        = new BufferedOutputStream(new FileOutputStream(error));
                    errorstream = new PrintStream(bos);
                }  catch (IOException e) {
                    throw new BuildException(e, getLocation());
                }
            } else {
                errorstream = new LogOutputStream(this, Project.MSG_WARN);
            }
            streamhandler = new PumpStreamHandler(outputstream, errorstream);
        }

        Execute exe = new Execute(streamhandler, null);

        exe.setAntRun(getProject());
        if (topDir == null) {
            topDir = getProject().getBaseDir();
        }
        exe.setWorkingDirectory(topDir);

        exe.setCommandline(toExecute.getCommandline());
        try {
            exe.execute();
            log("Building the RPM based on the " + specFile + " file");
        } catch (IOException e) {
            throw new BuildException(e, getLocation());
        } finally {
            if (output != null) {
                try {
                    outputstream.close();
                } catch (IOException e) {
                    // ignore any secondary error
                }
            }
            if (error != null) {
                try {
                    errorstream.close();
                } catch (IOException e) {
                    // ignore any secondary error
                }
            }
        }
    }

    /**
     * The directory which will have the expected
     * subdirectories, SPECS, SOURCES, BUILD, SRPMS ; optional.
     * If this isn't specified,
     * the <tt>baseDir</tt> value is used
     *
     * @param td the directory containing the normal RPM directories.
     */
    public void setTopDir(File td) {
        this.topDir = td;
    }

    /**
     * What command to issue to the rpm build tool; optional.
     * The default is "-bb"
     */
    public void setCommand(String c) {
        this.command = c;
    }

    /**
     * The name of the spec File to use; required.
     */
    public void setSpecFile(String sf) {
        if ((sf == null) || (sf.trim().equals(""))) {
            throw new BuildException("You must specify a spec file", getLocation());
        }
        this.specFile = sf;
    }

    /**
     * Flag (optional, default=false) to remove
     * the generated files in the BUILD directory
     */
    public void setCleanBuildDir(boolean cbd) {
        cleanBuildDir = cbd;
    }

    /**
     * Flag (optional, default=false) to remove the spec file from SPECS
     */
    public void setRemoveSpec(boolean rs) {
        removeSpec = rs;
    }

    /**
     * Flag (optional, default=false)
     * to remove the sources after the build.
     * See the <tt>--rmsource</tt>  option of rpmbuild.
     */
    public void setRemoveSource(boolean rs) {
        removeSource = rs;
    }

    /**
     * Optional file to save stdout to.
     */
    public void setOutput(File output) {
        this.output = output;
    }

    /**
     * Optional file to save stderr to
     */
    public void setError(File error) {
        this.error = error;
    }

    /**
     * The executable to run when building; optional.
     * The default is <code>rpmbuild</code>.
     *
     * @since Ant 1.6
     * @param c the rpm build executable
     */
    public void setRpmBuildCommand(String c) {
        this.rpmBuildCommand = c;
    }

    /**
     * Checks whether <code>rpmbuild</code> is on the PATH and returns
     * the absolute path to it - falls back to <code>rpm</code>
     * otherwise.
     *
     * @since 1.6
     */
    protected String guessRpmBuildCommand() {
        Vector env = Execute.getProcEnvironment();
        String path = null;
        for (Enumeration e = env.elements(); e.hasMoreElements();) {
            String var = (String) e.nextElement();
            if (var.startsWith("PATH=") || var.startsWith("Path=")) {
                path = var.substring(6 /* "PATH=".length() + 1 */);
                break;
            }
        }

        if (path != null) {
            Path p = new Path(getProject(), path);
            String[] pElements = p.list();
            for (int i = 0; i < pElements.length; i++) {
                File f = new File(pElements[i],
                                  "rpmbuild"
                                  + (Os.isFamily("dos") ? ".exe" : ""));
                if (f.canRead()) {
                    return f.getAbsolutePath();
                }
            }
        }

        return "rpm";
    }
}
