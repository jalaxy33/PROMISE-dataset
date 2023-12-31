
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

package org.apache.poi.poifs.filesystem;

import java.io.*;

import java.util.*;

import org.apache.poi.poifs.property.Property;

/**
 * Abstract implementation of Entry
 *
 * Extending classes should override isDocument() or isDirectory(), as
 * appropriate
 *
 * Extending classes must override isDeleteOK()
 *
 * @author Marc Johnson (mjohnson at apache dot org)
 */

public abstract class EntryNode
    implements Entry
{

    // the DocumentProperty backing this object
    private Property      _property;

    // this object's parent Entry
    private DirectoryNode _parent;

    /**
     * create a DocumentNode. This method is not public by design; it
     * is intended strictly for the internal use of extending classes
     *
     * @param property the Property for this Entry
     * @param parent the parent of this entry
     */

    protected EntryNode(final Property property, final DirectoryNode parent)
    {
        _property = property;
        _parent   = parent;
    }

    /**
     * grant access to the property
     *
     * @return the property backing this entry
     */

    protected Property getProperty()
    {
        return _property;
    }

    /**
     * is this the root of the tree?
     *
     * @return true if so, else false
     */

    protected boolean isRoot()
    {

        // only the root Entry has no parent ...
        return (_parent == null);
    }

    /**
     * extensions use this method to verify internal rules regarding
     * deletion of the underlying store.
     *
     * @return true if it's ok to delete the underlying store, else
     *         false
     */

    protected abstract boolean isDeleteOK();

    /* ********** START implementation of Entry ********** */

    /**
     * get the name of the Entry
     *
     * @return name
     */

    public String getName()
    {
        return _property.getName();
    }

    /**
     * is this a DirectoryEntry?
     *
     * @return true if the Entry is a DirectoryEntry, else false
     */

    public boolean isDirectoryEntry()
    {
        return false;
    }

    /**
     * is this a DocumentEntry?
     *
     * @return true if the Entry is a DocumentEntry, else false
     */

    public boolean isDocumentEntry()
    {
        return false;
    }

    /**
     * get this Entry's parent (the DocumentEntry that owns this
     * Entry). All Entry objects, except the root Entry, has a parent.
     *
     * @return this Entry's parent; null iff this is the root Entry
     */

    public DirectoryEntry getParent()
    {
        return _parent;
    }

    /**
     * Delete this Entry. This operation should succeed, but there are
     * special circumstances when it will not:
     *
     * If this Entry is the root of the Entry tree, it cannot be
     * deleted, as there is no way to create another one.
     *
     * If this Entry is a directory, it cannot be deleted unless it is
     * empty.
     *
     * @return true if the Entry was successfully deleted, else false
     */

    public boolean delete()
    {
        boolean rval = false;

        if ((!isRoot()) && isDeleteOK())
        {
            rval = _parent.deleteEntry(this);
        }
        return rval;
    }

    /**
     * Rename this Entry. This operation will fail if:
     *
     * There is a sibling Entry (i.e., an Entry whose parent is the
     * same as this Entry's parent) with the same name.
     *
     * This Entry is the root of the Entry tree. Its name is dictated
     * by the Filesystem and many not be changed.
     *
     * @param newName the new name for this Entry
     *
     * @return true if the operation succeeded, else false
     */

    public boolean renameTo(final String newName)
    {
        boolean rval = false;

        if (!isRoot())
        {
            rval = _parent.changeName(getName(), newName);
        }
        return rval;
    }

    /* **********  END  implementation of Entry ********** */
}   // end public class EntryNode

