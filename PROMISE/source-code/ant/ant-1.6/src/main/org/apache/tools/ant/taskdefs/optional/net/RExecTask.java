/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
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

package org.apache.tools.ant.taskdefs.optional.net;

import org.apache.commons.net.bsd.RExecClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Vector;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * Automates the rexec protocol.
 *
 * @since Ant 1.6
 */

public class RExecTask extends Task {
    /**
     *  The userid to login with, if automated login is used
     */
    private String userid  = null;

    /**
     *  The password to login with, if automated login is used
     */
    private String password = null;

    /**
     *  The command to execute
     */
    private String command = null;

    /**
     *  The server to connect to.
     */
    private String server  = null;

    /**
     *  The tcp port to connect to.
     */
    private int port = RExecClient.DEFAULT_PORT;

    /**
     *  The Object which handles the rexec session.
     */
    private AntRExecClient rexec = null;

    /**
     *  The list of read/write commands for this session
     */
    private Vector rexecTasks = new Vector();

    /**
     *  If true, adds a CR to beginning of login script
     */
    private boolean addCarriageReturn = false;

    /**
     *  Default time allowed for waiting for a valid response
     *  for all child reads.  A value of 0 means no limit.
     */
    private Integer defaultTimeout = null;

    /**
     *  This class is the parent of the Read and Write tasks.
     *  It handles the common attributes for both.
     */
    public class RExecSubTask {
        protected String taskString = "";
        public void execute(AntRExecClient rexec)
                throws BuildException {
            throw new BuildException("Shouldn't be able instantiate a SubTask directly");
        }

        /**
         *  the message as nested text
         */
        public void addText(String s) {
            setString(getProject().replaceProperties(s));
        }

        /**
         * the message as an attribute
         */
        public void setString(String s) {
           taskString += s;
        }
    }

    /**
     *  Sends text to the connected server
     */
    public class RExecWrite extends RExecSubTask {
        private boolean echoString = true;
        public void execute(AntRExecClient rexec)
               throws BuildException {
           rexec.sendString(taskString, echoString);
        }

        /**
         * Whether or not the message should be echoed to the log.
         * Defaults to <code>true</code>.
         */
        public void setEcho(boolean b) {
           echoString = b;
        }
    }

    /**
     *  Reads the output from the connected server
     *  until the required string is found or we time out.
     */
    public class RExecRead extends RExecSubTask {
        private Integer timeout = null;
        public void execute(AntRExecClient rexec)
               throws BuildException {
            rexec.waitForString(taskString, timeout);
        }
        /**
         *  a timeout value that overrides any task wide timeout.
         */
        public void setTimeout(Integer i) {
           this.timeout = i;
        }

        /**
         * Sets the default timeout if none has been set already
         * @ant.attribute ignore="true"
         */
        public void setDefaultTimeout(Integer defaultTimeout) {
           if (timeout == null) {
              timeout = defaultTimeout;
           }
        }
    }

    /**
     *  This class handles the abstraction of the rexec protocol.
     *  Currently it is a wrapper around <a
     *  href="http://jakarta.apache.org/commons/net/index.html">Jakarta
     *  Commons Net</a>.
     */
    public class AntRExecClient extends RExecClient {
        /**
         * Read from the rexec session until the string we are
         * waiting for is found
         * @param s The string to wait on
         */
        public void waitForString(String s) {
            waitForString(s, null);
        }

        /**
         * Read from the rexec session until the string we are
         * waiting for is found or the timeout has been reached
         * @param s The string to wait on
         * @param timeout The maximum number of seconds to wait
         */
        public void waitForString(String s, Integer timeout) {
            InputStream is = this.getInputStream();
            try {
                StringBuffer sb = new StringBuffer();
                if (timeout == null || timeout.intValue() == 0) {
                    while (sb.toString().indexOf(s) == -1) {
                        sb.append((char) is.read());
                    }
                } else {
                    Calendar endTime = Calendar.getInstance();
                    endTime.add(Calendar.SECOND, timeout.intValue());
                    while (sb.toString().indexOf(s) == -1) {
                        while (Calendar.getInstance().before(endTime)
                            && is.available() == 0) {
                            Thread.sleep(250);
                        }
                        if (is.available() == 0) {
                            throw new BuildException(
                                "Response timed-out waiting for \"" + s + '\"',
                                getLocation());
                        }
                        sb.append((char) is.read());
                    }
                }
                log(sb.toString(), Project.MSG_INFO);
            } catch (BuildException be) {
                throw be;
            } catch (Exception e) {
                throw new BuildException(e, getLocation());
            }
        }

        /**
         * Write this string to the rexec session.
         * @param echoString  Logs string sent
         */
        public void sendString(String s, boolean echoString) {
            OutputStream os = this.getOutputStream();
            try {
                os.write((s + "\n").getBytes());
                if (echoString) {
                    log(s, Project.MSG_INFO);
                }
                os.flush();
            } catch (Exception e) {
                throw new BuildException(e, getLocation());
            }
        }
        /**
         * Read from the rexec session until the EOF is found or
         * the timeout has been reached
         * @param timeout The maximum number of seconds to wait
         */
        public void waitForEOF(Integer timeout) {
            InputStream is = this.getInputStream();
            try {
                StringBuffer sb = new StringBuffer();
                if (timeout == null || timeout.intValue() == 0) {
                int read;
                    while ((read = is.read()) != -1) {
                        char c = (char) read;
                        sb.append(c);
                        if (c == '\n') {
                        log(sb.toString(), Project.MSG_INFO);
                        sb.delete(0, sb.length());
                        }
                    }
                } else {
                    Calendar endTime = Calendar.getInstance();
                    endTime.add(Calendar.SECOND, timeout.intValue());
                int read = 0;
                    while (read != -1) {
                        while (Calendar.getInstance().before(endTime) && is.available() == 0) {
                            Thread.sleep(250);
                        }
                        if (is.available() == 0) {
                        log(sb.toString(), Project.MSG_INFO);
                            throw new BuildException(
                                                     "Response timed-out waiting for EOF",
                                                     getLocation());
                        }
                        read =  is.read();
                        if (read != -1) {
                        char c = (char) read;
                        sb.append(c);
                        if (c == '\n') {
                                log(sb.toString(), Project.MSG_INFO);
                                sb.delete(0, sb.length());
                        }
                        }
                    }
                }
                if (sb.length() > 0) {
                log(sb.toString(), Project.MSG_INFO);
                }
            } catch (BuildException be) {
                throw be;
            } catch (Exception e) {
                throw new BuildException(e, getLocation());
            }
        }

    }
    /**
     *  A string to wait for from the server.
     *  A subTask &lt;read&gt; tag was found.  Create the object,
     *  Save it in our list, and return it.
     */

    public RExecSubTask createRead() {
        RExecSubTask task = (RExecSubTask) new RExecRead();
        rexecTasks.addElement(task);
        return task;
    }
    /**
     *  Add text to send to the server
     *  A subTask &lt;write&gt; tag was found.  Create the object,
     *  Save it in our list, and return it.
     */
    public RExecSubTask createWrite() {
        RExecSubTask task = (RExecSubTask) new RExecWrite();
        rexecTasks.addElement(task);
        return task;
    }
    /**
     *  Verify that all parameters are included.
     *  Connect and possibly login
     *  Iterate through the list of Reads and writes
     */
    public void execute() throws BuildException {
        /**  A server name is required to continue */
        if (server == null) {
            throw new BuildException("No Server Specified");
        }
        /**  A userid and password must appear together
         *   if they appear.  They are not required.
         */
        if (userid == null && password != null) {
            throw new BuildException("No Userid Specified");
        }
        if (password == null && userid != null) {
            throw new BuildException("No Password Specified");
        }

        /**  Create the telnet client object */
        rexec = new AntRExecClient();
        try {
            rexec.connect(server, port);
        } catch (IOException e) {
            throw new BuildException("Can't connect to " + server);
        }
        /**  Login if userid and password were specified */
        if (userid != null && password != null) {
            login();
        }
        /**  Process each sub command */
        Enumeration tasksToRun = rexecTasks.elements();
        while (tasksToRun != null && tasksToRun.hasMoreElements()) {
            RExecSubTask task = (RExecSubTask) tasksToRun.nextElement();
            if (task instanceof RExecRead && defaultTimeout != null) {
                ((RExecRead) task).setDefaultTimeout(defaultTimeout);
            }
            task.execute(rexec);
        }

        /** Keep reading input stream until end of it or time-out */
        rexec.waitForEOF(defaultTimeout);
    }
    /**
     *  Process a 'typical' login.  If it differs, use the read
     *  and write tasks explicitely
     */
    private void login() {
        if (addCarriageReturn) {
            rexec.sendString("\n", true);
        }
        rexec.waitForString("ogin:");
        rexec.sendString(userid, true);
        rexec.waitForString("assword:");
        rexec.sendString(password, false);
    }
    /**
     * Set the the comand to execute on the server;
     */
    public void setCommand(String c) { this.command = c; }
    /**
     *  send a carriage return after connecting; optional, defaults to false.
     */
    public void setInitialCR(boolean b) {
        this.addCarriageReturn = b;
    }
    /**
     *  Set the the login password to use
     * required if <tt>userid</tt> is set.
     */
    public void setPassword(String p) { this.password = p; }
    /**
     *  Set the tcp port to connect to; default is 23.
     */
    public void setPort(int p) { this.port = p; }
    /**
     *  Set the hostname or address of the remote server.
     */
    public void setServer(String m) { this.server = m; }
    /**
     * set a default timeout in seconds to wait for a response,
     * zero means forever (the default)
     */
    public void setTimeout(Integer i) {
        this.defaultTimeout = i;
    }
    /**
     * Set the the login id to use on the server;
     * required if <tt>password</tt> is set.
     */
    public void setUserid(String u) { this.userid = u; }
}
