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

package org.apache.poi.hpsf;

import java.io.*;
import java.util.*;
import org.apache.poi.hpsf.littleendian.*;
import org.apache.poi.hpsf.wellknown.*;
import org.apache.poi.poifs.filesystem.*;

/**
 * <p>Represents a property set in the Horrible Property Set Format
 * (HPSF). These are usually metadata of a Microsoft Office
 * document.</p>
 *
 * <p>An application that wants to access these metadata should create
 * an instance of this class or one of its subclasses by calling the
 * factory method {@link PropertySetFactory#create} and then retrieve
 * the information its needs by calling appropriate methods.</p>
 *
 * <p>{@link PropertySetFactory#create} does its work by calling one
 * of the constructors {@link PropertySet#PropertySet(InputStream)} or
 * {@link PropertySet#PropertySet(byte[])}. If the constructor's
 * argument is not in the Horrible Property Set Format, i.e. not a
 * property set stream, or if any other error occurs, an appropriate
 * exception is thrown.</p>
 *
 * <p>A {@link PropertySet} has a list of {@link Section}s, and each
 * {@link Section} has a {@link Property} array. Use {@link
 * #getSections} to retrieve the {@link Section}s, then call {@link
 * Section#getProperties} for each {@link Section} to get hold of the
 * {@link Property} arrays.</p>
 *
 * Since the vast majority of {@link PropertySet}s contains only a
 * single {@link Section}, the convenience method {@link
 * #getProperties} returns the properties of a {@link PropertySet}'s
 * {@link Section} (throwing a {@link NoSingleSectionException} if the
 * {@link PropertySet} contains more (or less) than exactly one {@link
 * Section}).
 *
 * @author Rainer Klute (klute@rainer-klute.de)
 * @version $Id: PropertySet.java,v 1.1 2002/02/14 04:00:59 mjohnson Exp $
 * @since 2002-02-09
 */
public class PropertySet
{
    static final byte[] BYTE_ORDER_ASSERTION =
        new byte[] {(byte) 0xFF, (byte) 0xFE};
    static final byte[] FORMAT_ASSERTION =
        new byte[] {(byte) 0x00, (byte) 0x00};



    private Word byteOrder; // Must equal BYTE_ORDER_ASSERTION

    /**
     * <p>Returns the property set stream's low-level "byte order"
     * field. It is always <tt>0xFFFE</tt>.</p>
     */
    public Word getByteOrder()
    {
        return byteOrder;
    }



    private Word format;    // Must equal FORMAT_ASSERTION

    /**
     * <p>Returns the property set stream's low-level "format"
     * field. It is always <tt>0x0000</tt>.</p>
     */
    public Word getFormat()
    {
        return format;
    }



    private DWord osVersion;

    /**
     * <p>Returns the property set stream's low-level "OS version"
     * field.</p>
     */
    public DWord getOSVersion()
    {
        return osVersion;
    }



    private ClassID classID;

    /**
     * <p>Returns the property set stream's low-level "class ID"
     * field.</p>
     */
    public ClassID getClassID()
    {
        return classID;
    }



    private int sectionCount;

    /**
     * <p>Returns the number of {@link Section}s in the property
     * set.</p>
     */
    public int getSectionCount()
    {
        return sectionCount;
    }



    private List sections;

    /**
     * <p>Returns the {@link Section}s in the property set.</p>
     */
    public List getSections()
    {
        return sections;
    }



    /**
     * <p>Creates an empty (uninitialized) {@link PropertySet}.</p>
     *
     * <p><strong>Please note:</strong> For the time being this
     * constructor is protected since it is used for internal purposes
     * only, but expect it to become public once the property set
     * writing functionality is implemented.</p>
     */
    protected PropertySet()
    {}



    /**
     * <p>Creates a {@link PropertySet} instance from an {@link
     *  InputStream} in the Horrible Property Set Format.</p>
     *
     * <p>The constructor reads the first few bytes from the stream
     * and determines whether it is really a property set stream. If
     * it is, it parses the rest of the stream. If it is not, it
     * resets the stream to its beginning in order to let other
     * components mess around with the data and throws an
     * exception.</p>
     *
     * @throws NoPropertySetStreamException if the stream is not a
     * property set stream.
     *
     * @throws MarkUnsupportedException if the stream does not support
     * the {@link InputStream#markSupported} method.
     *
     * @throws IOException if the {@link InputStream} cannot not be
     * accessed as needed.
     */
    public PropertySet(final InputStream stream)
        throws NoPropertySetStreamException, MarkUnsupportedException,
               IOException
    {
        if (isPropertySetStream(stream))
        {
            final int avail = stream.available();
            final byte[] buffer = new byte[avail];
            stream.read(buffer, 0, buffer.length);
            init(buffer, 0, buffer.length);
        }
        else
            throw new NoPropertySetStreamException();
    }



    /**
     * <p>Creates a {@link PropertySet} instance from a byte array
     * that represents a stream in the Horrible Property Set
     * Format.</p>
     *
     * @param stream The byte array holding the stream data.
     *
     * @param offset The offset in <var>stream</var> where the stream
     * data begin. If the stream data begin with the first byte in the
     * array, the <var>offset</var> is 0.
     *
     * @param length The length of the stream data.
     *
     * @throws NoPropertySetStreamException if the byte array is not a
     * property set stream.
     */
    public PropertySet(final byte[] stream, final int offset, final int length)
        throws NoPropertySetStreamException
    {
        if (isPropertySetStream(stream, offset, length))
            init(stream, offset, length);
        else
            throw new NoPropertySetStreamException();
    }



    /**
     * <p>Creates a {@link PropertySet} instance from a byte array
     * that represents a stream in the Horrible Property Set
     * Format.</p>
     *
     * @param stream The byte array holding the stream data. The
     * complete byte array contents is the stream data.
     *
     * @throws NoPropertySetStreamException if the byte array is not a
     * property set stream.
     */
    public PropertySet(final byte[] stream)
        throws NoPropertySetStreamException
    {
        this(stream, 0, stream.length);
    }



    /**
     * <p>Checks whether an {@link InputStream} is in the Horrible
     * Property Set Format.</p>
     *
     * @param stream The {@link InputStream} to check. In order to
     * perform the check, the method reads the first bytes from the
     * stream. After reading, the stream is reset to the position it
     * had before reading. The {@link InputStream} must support the
     * {@link InputStream#mark} method.
     *
     * @return <code>true</code> if the stream is a property set
     * stream, else <code>false</code>.
     *
     * @throws MarkUnsupportedException if the {@link InputStream}
     * does not support the {@link InputStream#mark} method.
     */
    public static boolean isPropertySetStream(final InputStream stream)
        throws MarkUnsupportedException, IOException
    {
        /* Read at most this many bytes. */
        final int BUFFER_SIZE = 50;

        /* Mark the current position in the stream so that we can
         * reset to this position if the stream does not contain a
         * property set. */
        if (!stream.markSupported())
            throw new MarkUnsupportedException(stream.getClass().getName());
        stream.mark(BUFFER_SIZE);

        /* Read a couple of bytes from the stream. */
        final byte[] buffer = new byte[BUFFER_SIZE];
        final int bytes =
            stream.read(buffer, 0,
                        Math.min(buffer.length, stream.available()));
        final boolean isPropertySetStream =
            isPropertySetStream(buffer, 0, bytes);
        stream.reset();
        return isPropertySetStream;
    }



    /**
     * <p>Checks whether a byte array is in the Horrible Property Set
     * Format.</p>
     *
     * @param src The byte array to check.
     *
     * @param offset The offset in the byte array.
     *
     * @param length The significant number of bytes in the byte
     * array. Only this number of bytes will be checked.
     *
     * @return <code>true</code> if the byte array is a property set
     * stream, <code>false</code> if not.
     */
    public static boolean isPropertySetStream(final byte[] src, int offset,
                                              final int length)
    {
        /* Read the header fields of the stream. They must always be
         * there. */
        final Word byteOrder = new Word(src, offset);
        offset += Word.LENGTH;
        if (!Util.equal(byteOrder.getBytes(), BYTE_ORDER_ASSERTION))
            return false;
        final Word format = new Word(src, offset);
        offset += Word.LENGTH;
        if (!Util.equal(format.getBytes(), FORMAT_ASSERTION))
            return false;
        final DWord osVersion = new DWord(src, offset);
        offset += DWord.LENGTH;
        final ClassID classID = new ClassID(src, offset);
        offset += ClassID.LENGTH;
        final DWord sectionCount = new DWord(src, offset);
        offset += DWord.LENGTH;
        if (sectionCount.intValue() < 1)
            return false;
        return true;
    }



    /**
     * <p>Initializes this {@link PropertySet} instance from a byte
     * array. The method assumes that it has been checked already that
     * the byte array indeed represents a property set stream. It does
     * no more checks on its own.</p>
     */
    private void init(final byte[] src, int offset, final int length)
    {
        /* Read the stream's header fields. */
        byteOrder = new Word(src, offset);
        offset += Word.LENGTH;
        format = new Word(src, offset);
        offset += Word.LENGTH;
        osVersion = new DWord(src, offset);
        offset += DWord.LENGTH;
        classID = new ClassID(src, offset);
        offset += ClassID.LENGTH;
        sectionCount = new DWord(src, offset).intValue();
        offset += DWord.LENGTH;
        
        /* Read the sections, which are following the header. They
         * start with an array of section descriptions. Each one
         * consists of a format ID telling what the section contains
         * and an offset telling how many bytes from the start of the
         * stream the section begins. */
        /* Most property sets have only one section. The Document
         * Summary Information stream has 2. Everything else is a rare
         * exception and is no longer fostered by Microsoft. */
        sections = new ArrayList(2);

        /* Loop over the section descriptor array. Each descriptor
         * consists of a ClassID and a DWord, and we have to increment
         * "offset" accordingly. */
        for (int i = 0; i < sectionCount; i++)
        {
            final Section s = new Section(src, offset);
            offset += ClassID.LENGTH + DWord.LENGTH;
            sections.add(s);
        }
    }



    /**
     * <p>Checks whether this {@link PropertySet} represents a Summary
     * Information.</p>
     */
    public boolean isSummaryInformation()
    {
        return Util.equal(((Section) sections.get(0)).getFormatID().getBytes(),
                          SectionIDMap.SUMMARY_INFORMATION_ID);
    }



    /**
     * <p>Checks whether this {@link PropertySet} is a Document
     * Summary Information.</p>
     */
    public boolean isDocumentSummaryInformation()
    {
        return Util.equal(((Section) sections.get(0)).getFormatID().getBytes(),
                          SectionIDMap.DOCUMENT_SUMMARY_INFORMATION_ID);
    }



    /**
     * <p>Convenience method returning the {@link Property} array
     * contained in this property set. It is a shortcut for getting
     * the {@link PropertySet}'s {@link Section}s list and then
     * getting the {@link Property} array from the first {@link
     * Section}. However, it can only be used if the {@link
     * PropertySet} contains exactly one {@link Section}, so check
     * {@link #getSectionCount} first!</p>
     *
     * @return The properties of the only {@link Section} of this
     * {@link PropertySet}.
     *
     * @throws NoSingleSectionException if the {@link PropertySet} has
     * more or less than one {@link Section}.
     */
    public Property[] getProperties()
        throws NoSingleSectionException
    {
        return getSingleSection().getProperties();
    }



    /**
     * <p>Convenience method returning the value of the property with
     * the specified ID. If the property is not available,
     * <code>null</code> is returned and a subsequent call to {@link
     * #wasNull} will return <code>true</code>.</p>
     *
     * @throws NoSingleSectionException if the {@link PropertySet} has
     * more or less than one {@link Section}.
     */
    protected Object getProperty(final int id)
        throws NoSingleSectionException
    {
        return getSingleSection().getProperty(id);
    }



    /**
     * <p>Convenience method returning the value of the numeric
     * property with the specified ID. If the property is not
     * available, 0 is returned. A subsequent call to {@link #wasNull}
     * will return <code>true</code> to let the caller distinguish
     * that case from a real property value of 0.</p>
     *
     * @throws NoSingleSectionException if the {@link PropertySet} has
     * more or less than one {@link Section}.
     */
    protected int getPropertyIntValue(final int id)
        throws NoSingleSectionException
    {
        return getSingleSection().getPropertyIntValue(id);
    }



    /**
     * <p>Checks whether the property which the last call to {@link
     * #getPropertyIntValue} or {@link #getProperty} tried to access
     * was available or not. This information might be important for
     * callers of {@link #getPropertyIntValue} since the latter
     * returns 0 if the property does not exist. Using {@link
     * #wasNull}, the caller can distiguish this case from a
     * property's real value of 0.</p>
     *
     * @return <code>true</code> if the last call to {@link
     * #getPropertyIntValue} or {@link #getProperty} tried to access a
     * property that was not available, else <code>false</code>.
     *
     * @throws NoSingleSectionException if the {@link PropertySet} has
     * more than one {@link Section}.
     */
    public boolean wasNull() throws NoSingleSectionException
    {
        return getSingleSection().wasNull();
    }



    /**
     * <p>If the {@link PropertySet} has only a single section this
     * method returns it.</p>
     *
     * @throws NoSingleSectionException if the {@link PropertySet} has
     * more or less than exactly one {@link Section}.
     */
    public Section getSingleSection()
    {
        if (sectionCount != 1)
            throw new NoSingleSectionException
                ("Property set contains " + sectionCount + " sections.");
        return ((Section) sections.get(0));
    }

}
