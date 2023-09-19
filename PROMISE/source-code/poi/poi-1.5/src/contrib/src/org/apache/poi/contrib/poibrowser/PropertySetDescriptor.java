/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights
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
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
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
 *
 * Portions of this software are based upon public domain software
 * originally written at the National Center for Supercomputing Applications,
 * University of Illinois, Urbana-Champaign.
 */

package org.apache.poi.contrib.poibrowser;

import java.io.*;
import org.apache.poi.hpsf.*;
import org.apache.poi.poifs.filesystem.*;

/**
 * <p>Describes the most important (whatever that is) features of a
 * stream containing a {@link PropertySet}.</p>
 *
 * @author Rainer Klute (klute@rainer-klute.de)
 * @version $Id: PropertySetDescriptor.java,v 1.1 2002/02/14 04:00:58 mjohnson Exp $
 * @since 2002-02-05
 */
public class PropertySetDescriptor extends DocumentDescriptor
{

    protected PropertySet propertySet;

    /**
     * <p>Returns this {@link PropertySetDescriptor}'s {@link
     * PropertySet}.</p>
     */
    public PropertySet getPropertySet()
    {
        return propertySet;
    }



    /**
     * <p>Creates a {@link PropertySetDescriptor} by reading a {@link
     * PropertySet} from a {@link DocumentInputStream}.</p>
     *
     * @param name The stream's name.
     *
     * @param path The stream's path in the POI filesystem hierarchy.
     *
     * @param stream The stream.
     *
     * @param nrOfBytes The maximum number of bytes to display in a
     * dump starting at the beginning of the stream.
     */
    public PropertySetDescriptor(final String name,
                                 final POIFSDocumentPath path,
                                 final DocumentInputStream stream,
                                 final int nrOfBytesToDump)
        throws NoPropertySetStreamException, MarkUnsupportedException,
        UnexpectedPropertySetTypeException, IOException
    {
        super(name, path, stream, nrOfBytesToDump);
        propertySet = PropertySetFactory.create(stream);
    }

}
