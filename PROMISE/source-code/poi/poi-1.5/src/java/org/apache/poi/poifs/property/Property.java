
/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
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
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
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

package org.apache.poi.poifs.property;

import java.io.*;

import java.util.*;

import org.apache.poi.poifs.common.POIFSConstants;
import org.apache.poi.poifs.dev.POIFSViewable;
import org.apache.poi.util.ByteField;
import org.apache.poi.util.IntegerField;
import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.ShortField;

/**
 * This abstract base class is the ancestor of all classes
 * implementing POIFS Property behavior.
 *
 * @author Marc Johnson (mjohnson at apache dot org)
 */

public abstract class Property
    implements Child, POIFSViewable
{
    static final private byte   _default_fill             = ( byte ) 0x00;
    static final private int    _name_size_offset         = 0x40;
    static final private int    _max_name_length          =
        (_name_size_offset / LittleEndianConsts.SHORT_SIZE) - 1;
    static final protected int  _NO_INDEX                 = -1;

    // useful offsets
    static final private int    _node_color_offset        = 0x43;
    static final private int    _previous_property_offset = 0x44;
    static final private int    _next_property_offset     = 0x48;
    static final private int    _child_property_offset    = 0x4C;
    static final private int    _seconds_1_offset         = 0x64;
    static final private int    _days_1_offset            = 0x68;
    static final private int    _seconds_2_offset         = 0x6C;
    static final private int    _days_2_offset            = 0x70;
    static final private int    _start_block_offset       = 0x74;
    static final private int    _size_offset              = 0x78;

    // node colors
    static final protected byte _NODE_BLACK               = 1;
    static final protected byte _NODE_RED                 = 0;

    // documents must be at least this size to be stored in big blocks
    static final private int    _big_block_minimum_bytes  = 4096;
    private String              _name;
    private ShortField          _name_size;
    private ByteField           _property_type;
    private ByteField           _node_color;
    private IntegerField        _previous_property;
    private IntegerField        _next_property;
    private IntegerField        _child_property;
    private IntegerField        _seconds_1;
    private IntegerField        _days_1;
    private IntegerField        _seconds_2;
    private IntegerField        _days_2;
    private IntegerField        _start_block;
    private IntegerField        _size;
    private byte[]              _raw_data;
    private int                 _index;
    private Child               _next_child;
    private Child               _previous_child;

    /**
     * Default constructor
     */

    protected Property()
    {
        _raw_data = new byte[ POIFSConstants.PROPERTY_SIZE ];
        Arrays.fill(_raw_data, _default_fill);
        _name_size         = new ShortField(_name_size_offset);
        _property_type     =
            new ByteField(PropertyConstants.PROPERTY_TYPE_OFFSET);
        _node_color        = new ByteField(_node_color_offset);
        _previous_property = new IntegerField(_previous_property_offset,
                                              _NO_INDEX, _raw_data);
        _next_property     = new IntegerField(_next_property_offset,
                                              _NO_INDEX, _raw_data);
        _child_property    = new IntegerField(_child_property_offset,
                                              _NO_INDEX, _raw_data);
        _seconds_1         = new IntegerField(_seconds_1_offset, 0,
                                              _raw_data);
        _days_1            = new IntegerField(_days_1_offset, 0, _raw_data);
        _seconds_2         = new IntegerField(_seconds_2_offset, 0,
                                              _raw_data);
        _days_2            = new IntegerField(_days_2_offset, 0, _raw_data);
        _start_block       = new IntegerField(_start_block_offset);
        _size              = new IntegerField(_size_offset, 0, _raw_data);
        _index             = _NO_INDEX;
        setName("");
        setNextChild(null);
        setPreviousChild(null);
    }

    /**
     * Constructor from byte data
     *
     * @param index index number
     * @param array byte data
     * @param offset offset into byte data
     */

    protected Property(final int index, final byte [] array, final int offset)
    {
        _raw_data = new byte[ POIFSConstants.PROPERTY_SIZE ];
        System.arraycopy(array, offset, _raw_data, 0,
                         POIFSConstants.PROPERTY_SIZE);
        _name_size         = new ShortField(_name_size_offset, _raw_data);
        _property_type     =
            new ByteField(PropertyConstants.PROPERTY_TYPE_OFFSET, _raw_data);
        _node_color        = new ByteField(_node_color_offset, _raw_data);
        _previous_property = new IntegerField(_previous_property_offset,
                                              _raw_data);
        _next_property     = new IntegerField(_next_property_offset,
                                              _raw_data);
        _child_property    = new IntegerField(_child_property_offset,
                                              _raw_data);
        _seconds_1         = new IntegerField(_seconds_1_offset, _raw_data);
        _days_1            = new IntegerField(_days_1_offset, _raw_data);
        _seconds_2         = new IntegerField(_seconds_2_offset, _raw_data);
        _days_2            = new IntegerField(_days_2_offset, _raw_data);
        _start_block       = new IntegerField(_start_block_offset, _raw_data);
        _size              = new IntegerField(_size_offset, _raw_data);
        _index             = index;
        int name_length = (_name_size.get() / LittleEndianConsts.SHORT_SIZE)
                          - 1;

        if (name_length < 1)
        {
            _name = "";
        }
        else
        {
            char[] char_array  = new char[ name_length ];
            int    name_offset = 0;

            for (int j = 0; j < name_length; j++)
            {
                char_array[ j ] = ( char ) new ShortField(name_offset,
                                                          _raw_data).get();
                name_offset     += LittleEndianConsts.SHORT_SIZE;
            }
            _name = new String(char_array, 0, name_length);
        }
        _next_child     = null;
        _previous_child = null;
    }

    /**
     * Write the raw data to an OutputStream.
     *
     * @param stream the OutputStream to which the data should be
     *               written.
     *
     * @exception IOException on problems writing to the specified
     *            stream.
     */

    public void writeData(final OutputStream stream)
        throws IOException
    {
        stream.write(_raw_data);
    }

    /**
     * Set the start block for the document referred to by this
     * Property.
     *
     * @param startBlock the start block index
     */

    public void setStartBlock(final int startBlock)
    {
        _start_block.set(startBlock, _raw_data);
    }

    /**
     * @return the start block
     */

    public int getStartBlock()
    {
        return _start_block.get();
    }

    /**
     * find out the document size
     *
     * @return size in bytes
     */

    public int getSize()
    {
        return _size.get();
    }

    /**
     * Based on the currently defined size, should this property use
     * small blocks?
     *
     * @return true if the size is less than _big_block_minimum_bytes
     */

    public boolean shouldUseSmallBlocks()
    {
        return Property.isSmall(_size.get());
    }

    /**
     * does the length indicate a small document?
     *
     * @param length length in bytes
     *
     * @return true if the length is less than
     *         _big_block_minimum_bytes
     */

    public static boolean isSmall(final int length)
    {
        return length < _big_block_minimum_bytes;
    }

    /**
     * Get the name of this property
     *
     * @return property name as String
     */

    public String getName()
    {
        return _name;
    }

    /**
     * @return true if a directory type Property
     */

    abstract public boolean isDirectory();

    /**
     * Set the name; silently truncates the name if it's too long.
     *
     * @param name the new name
     */

    protected final void setName(final String name)
    {
        char[] char_array = name.toCharArray();
        int    limit      = Math.min(char_array.length, _max_name_length);

        _name = new String(char_array, 0, limit);
        short offset = 0;
        int   j      = 0;

        for (; j < limit; j++)
        {
            new ShortField(offset, ( short ) char_array[ j ], _raw_data);
            offset += LittleEndianConsts.SHORT_SIZE;
        }
        for (; j < _max_name_length + 1; j++)
        {
            new ShortField(offset, ( short ) 0, _raw_data);
            offset += LittleEndianConsts.SHORT_SIZE;
        }

        // double the count, and include the null at the end
        _name_size
            .set(( short ) ((limit + 1)
                            * LittleEndianConsts.SHORT_SIZE), _raw_data);
    }

    /**
     * Set the property type. Makes no attempt to validate the value.
     *
     * @param propertyType the property type (root, file, directory)
     */

    protected void setPropertyType(final byte propertyType)
    {
        _property_type.set(propertyType, _raw_data);
    }

    /**
     * Set the node color.
     *
     * @param nodeColor the node color (red or black)
     */

    protected void setNodeColor(final byte nodeColor)
    {
        _node_color.set(nodeColor, _raw_data);
    }

    /**
     * Set the child property.
     *
     * @param child the child property's index in the Property Table
     */

    protected void setChildProperty(final int child)
    {
        _child_property.set(child, _raw_data);
    }

    /**
     * Get the child property (its index in the Property Table)
     *
     * @return child property index
     */

    protected int getChildIndex()
    {
        return _child_property.get();
    }

    /**
     * Set the size of the document associated with this Property
     *
     * @param size the size of the document, in bytes
     */

    protected void setSize(final int size)
    {
        _size.set(size, _raw_data);
    }

    /**
     * Set the index for this Property
     *
     * @param index this Property's index within its containing
     *              Property Table
     */

    protected void setIndex(final int index)
    {
        _index = index;
    }

    /**
     * get the index for this Property
     *
     * @return the index of this Property within its Property Table
     */

    protected int getIndex()
    {
        return _index;
    }

    /**
     * Perform whatever activities need to be performed prior to
     * writing
     */

    abstract protected void preWrite();

    /**
     * get the next sibling
     *
     * @return index of next sibling
     */

    int getNextChildIndex()
    {
        return _next_property.get();
    }

    /**
     * get the previous sibling
     *
     * @return index of previous sibling
     */

    int getPreviousChildIndex()
    {
        return _previous_property.get();
    }

    /**
     * determine whether the specified index is valid
     *
     * @param index value to be checked
     *
     * @return true if the index is valid
     */

    static boolean isValidIndex(int index)
    {
        return index != _NO_INDEX;
    }

    /* ********** START implementation of Child ********** */

    /**
     * Get the next Child, if any
     *
     * @return the next Child; may return null
     */

    public Child getNextChild()
    {
        return _next_child;
    }

    /**
     * Get the previous Child, if any
     *
     * @return the previous Child; may return null
     */

    public Child getPreviousChild()
    {
        return _previous_child;
    }

    /**
     * Set the next Child
     *
     * @param child the new 'next' child; may be null, which has the
     *              effect of saying there is no 'next' child
     */

    public void setNextChild(final Child child)
    {
        _next_child = child;
        _next_property.set((child == null) ? _NO_INDEX
                                           : (( Property ) child)
                                               .getIndex(), _raw_data);
    }

    /**
     * Set the previous Child
     *
     * @param child the new 'previous' child; may be null, which has
     *              the effect of saying there is no 'previous' child
     */

    public void setPreviousChild(final Child child)
    {
        _previous_child = child;
        _previous_property.set((child == null) ? _NO_INDEX
                                               : (( Property ) child)
                                                   .getIndex(), _raw_data);
    }

    /* **********  END  implementation of Child ********** */
    /* ********** START begin implementation of POIFSViewable ********** */

    /**
     * Get an array of objects, some of which may implement
     * POIFSViewable
     *
     * @return an array of Object; may not be null, but may be empty
     */

    public Object [] getViewableArray()
    {
        Object[] results = new Object[ 5 ];

        results[ 0 ] = "Name          = \"" + getName() + "\"";
        results[ 1 ] = "Property Type = " + _property_type.get();
        results[ 2 ] = "Node Color    = " + _node_color.get();
        long time = _days_1.get();

        time         <<= 32;
        time         += (( long ) _seconds_1.get()) & 0x0000FFFFL;
        results[ 3 ] = "Time 1        = " + time;
        time         = _days_2.get();
        time         <<= 32;
        time         += (( long ) _seconds_2.get()) & 0x0000FFFFL;
        results[ 4 ] = "Time 2        = " + time;
        return results;
    }

    /**
     * Get an Iterator of objects, some of which may implement
     * POIFSViewable
     *
     * @return an Iterator; may not be null, but may have an empty
     * back end store
     */

    public Iterator getViewableIterator()
    {
        return Collections.EMPTY_LIST.iterator();
    }

    /**
     * Give viewers a hint as to whether to call getViewableArray or
     * getViewableIterator
     *
     * @return true if a viewer should call getViewableArray, false if
     *         a viewer should call getViewableIterator
     */

    public boolean preferArray()
    {
        return true;
    }

    /**
     * Provides a short description of the object, to be used when a
     * POIFSViewable object has not provided its contents.
     *
     * @return short description
     */

    public String getShortDescription()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("Property: \"").append(getName()).append("\"");
        return buffer.toString();
    }

    /* **********  END  begin implementation of POIFSViewable ********** */
}   // end public abstract class Property

