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
package org.apache.tools.ant.filters;

import java.io.IOException;
import java.io.Reader;
import org.apache.tools.ant.util.LineTokenizer;
import org.apache.tools.ant.types.Parameter;

/**
 * Reads the first <code>n</code> lines of a stream.
 * (Default is first 10 lines.)
 * <p>
 * Example:
 * <pre>&lt;headfilter lines=&quot;3&quot;/&gt;</pre>
 * Or:
 * <pre>&lt;filterreader classname=&quot;org.apache.tools.ant.filters.HeadFilter&quot;&gt;
 *    &lt;param name=&quot;lines&quot; value=&quot;3&quot;/&gt;
 * &lt;/filterreader&gt;</pre>
 *
 * @author Magesh Umasankar
 */
public final class HeadFilter extends BaseParamFilterReader
    implements ChainableReader {
    /** Parameter name for the number of lines to be returned. */
    private static final String LINES_KEY = "lines";

    /** Parameter name for the number of lines to be skipped. */
    private static final String SKIP_KEY = "skip";

    /** Number of lines currently read in. */
    private long linesRead = 0;

    /** Default number of lines to show */
    private static final int DEFAULT_NUM_LINES = 10;

    /** Number of lines to be returned in the filtered stream. */
    private long lines = DEFAULT_NUM_LINES;

    /** Number of lines to be skipped. */
    private long skip = 0;

    /** A line tokenizer */
    private LineTokenizer lineTokenizer = null;

    /** the current line from the input stream */
    private String    line      = null;
    /** the position in the current line */
    private int       linePos   = 0;

    /**
     * Constructor for "dummy" instances.
     *
     * @see BaseFilterReader#BaseFilterReader()
     */
    public HeadFilter() {
        super();
    }

    /**
     * Creates a new filtered reader.
     *
     * @param in A Reader object providing the underlying stream.
     *           Must not be <code>null</code>.
     */
    public HeadFilter(final Reader in) {
        super(in);
        lineTokenizer = new LineTokenizer();
        lineTokenizer.setIncludeDelims(true);
    }

    /**
     * Returns the next character in the filtered stream. If the desired
     * number of lines have already been read, the resulting stream is
     * effectively at an end. Otherwise, the next character from the
     * underlying stream is read and returned.
     *
     * @return the next character in the resulting stream, or -1
     * if the end of the resulting stream has been reached
     *
     * @exception IOException if the underlying stream throws an IOException
     * during reading
     */
    public final int read() throws IOException {
        if (!getInitialized()) {
            initialize();
            setInitialized(true);
        }

        while (line == null || line.length() == 0) {
            line = lineTokenizer.getToken(in);
            if (line == null) {
                return -1;
            }
            line = headFilter(line);
            linePos = 0;
        }

        int ch = line.charAt(linePos);
        linePos++;
        if (linePos == line.length()) {
            line = null;
        }
        return ch;
    }

    /**
     * Sets the number of lines to be returned in the filtered stream.
     *
     * @param lines the number of lines to be returned in the filtered stream
     */
    public final void setLines(final long lines) {
        this.lines = lines;
    }

    /**
     * Returns the number of lines to be returned in the filtered stream.
     *
     * @return the number of lines to be returned in the filtered stream
     */
    private final long getLines() {
        return lines;
    }

    /**
     * Sets the number of lines to be skipped in the filtered stream.
     *
     * @param skip the number of lines to be skipped in the filtered stream
     */
    public final void setSkip(final long skip) {
        this.skip = skip;
    }

    /**
     * Returns the number of lines to be skipped in the filtered stream.
     *
     * @return the number of lines to be skipped in the filtered stream
     */
    private final long getSkip() {
        return skip;
    }

    /**
     * Creates a new HeadFilter using the passed in
     * Reader for instantiation.
     *
     * @param rdr A Reader object providing the underlying stream.
     *            Must not be <code>null</code>.
     *
     * @return a new filter based on this configuration, but filtering
     *         the specified reader
     */
    public final Reader chain(final Reader rdr) {
        HeadFilter newFilter = new HeadFilter(rdr);
        newFilter.setLines(getLines());
        newFilter.setSkip(getSkip());
        newFilter.setInitialized(true);
        return newFilter;
    }

    /**
     * Scans the parameters list for the "lines" parameter and uses
     * it to set the number of lines to be returned in the filtered stream.
     * also scan for skip parameter.
     */
    private final void initialize() {
        Parameter[] params = getParameters();
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                if (LINES_KEY.equals(params[i].getName())) {
                    lines = new Long(params[i].getValue()).longValue();
                    continue;
                }
                if (SKIP_KEY.equals(params[i].getName())) {
                    skip = new Long(params[i].getValue()).longValue();
                    continue;
                }
            }
        }
    }

    /**
     * implements a head filter on the input stream
     */
    private String headFilter(String line) {
        linesRead++;
        if (skip > 0) {
            if ((linesRead - 1) < skip) {
                return null;
            }
        }

        if (lines > 0) {
            if (linesRead > (lines + skip)) {
                return null;
            }
        }
        return line;
    }
}
