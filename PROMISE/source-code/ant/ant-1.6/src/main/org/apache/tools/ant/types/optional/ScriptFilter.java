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
package org.apache.tools.ant.types.optional;

import org.apache.tools.ant.filters.TokenFilter;
import java.io.File;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.util.ScriptRunner;


/**
 * Most of this is CAP (Cut And Paste) from the Script task
 * ScriptFilter class, implements TokenFilter.Filter
 * for scripts to use.
 * This provides the same beans as the Script Task
 * to a script.
 * The script is meant to use get self.token and
 * set self.token in the reply.
 *
 * @author Not Specified.
 *
 * @since Ant 1.6
 */
public class ScriptFilter extends TokenFilter.ChainableReaderFilter {
    /** Has this object been initialized ? */
    private boolean initialized = false;
    /** the token used by the script */
    private String token;

    private ScriptRunner runner = new ScriptRunner();

    /**
     * Defines the language (required).
     *
     * @param language the scripting language name for the script.
     */
    public void setLanguage(String language) {
        runner.setLanguage(language);
    }

    /**
     * Initialize.
     *
     * @exception BuildException if someting goes wrong
     */
    private void init() throws BuildException {
        if (initialized) {
            return;
        }
        initialized = true;

        runner.addBeans(getProject().getProperties());
        runner.addBeans(getProject().getUserProperties());
        runner.addBeans(getProject().getTargets());
        runner.addBeans(getProject().getReferences());

        runner.addBean("project", getProject());
        runner.addBean("self", this);
    }

    /**
     * The current token
     *
     * @param token the string filtered by the script
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * The current token
     *
     * @return the string filtered by the script
     */
    public String getToken() {
        return token;
    }

    /**
     * Called filter the token.
     * This sets the token in this object, calls
     * the script and returns the token.
     *
     * @param token the token to be filtered
     * @return the filtered token
     */
    public String filter(String token) {
        init();
        setToken(token);
        runner.executeScript("<ANT-Filter>");
        return getToken();
    }

    /**
     * Load the script from an external file ; optional.
     *
     * @param file the file containing the script source.
     */
    public void setSrc(File file) {
        runner.setSrc(file);
    }

    /**
     * The script text.
     *
     * @param text a component of the script text to be added.
     */
    public void addText(String text) {
        runner.addText(text);
    }
}
