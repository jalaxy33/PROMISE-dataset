/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
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
 * 4. The names "Xerces" and "Apache Software Foundation" must
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
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 1999, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.xerces.dom;

import java.util.Enumeration;
import java.util.Vector;

import org.apache.xerces.utils.StringPool;

import org.w3c.dom.*;

/**
 * Elements represent most of the "markup" and structure of the
 * document.  They contain both the data for the element itself
 * (element name and attributes), and any contained nodes, including
 * document text (as children).
 * <P>
 * Elements may have Attributes associated with them; the API for this is
 * defined in Node, but the function is implemented here. In general, XML
 * applications should retrive Attributes as Nodes, since they may contain
 * entity references and hence be a fairly complex sub-tree. HTML users will
 * be dealing with simple string values, and convenience methods are provided
 * to work in terms of Strings.
 * <P>
 * @version
 * @since  PR-DOM-Level-1-19980818.
 */
public class DeferredElementImpl
    extends ElementImpl
    implements DeferredNode {

    //
    // Constants
    //

    /** Serialization version. */
    static final long serialVersionUID = 1698024469924430384L;

    //
    // Data
    //

    /** Node index. */
    protected transient int fNodeIndex;

    //
    // Constructors
    //

    /**
     * This is the deferred constructor. Only the fNodeIndex is given here. All
     * other data, can be requested from the ownerDocument via the index.
     */
    DeferredElementImpl(DeferredDocumentImpl ownerDoc, int nodeIndex) {
        super(ownerDoc, null);

        fNodeIndex = nodeIndex;
        syncData = true;
        syncChildren = true;

    } // <init>(DocumentImpl,int)

    //
    // DeferredNode methods
    //

    /** Returns the node index. */
    public final int getNodeIndex() {
        return fNodeIndex;
    }

    //
    // Protected methods
    //

    /** Synchronizes the data (name and value) for fast nodes. */
    protected final void synchronizeData() {

        // no need to sync in the future
        syncData = false;

        // fluff data
        DeferredDocumentImpl ownerDocument = (DeferredDocumentImpl)this.ownerDocument;
        name  = ownerDocument.getNodeNameString(fNodeIndex);
        
        if (ownerDocument.fNamespacesEnabled) {
            int elementTypeName = ownerDocument.getNodeName(fNodeIndex);
            StringPool fStringPool = ownerDocument.fStringPool;
            prefix = 
                fStringPool.toString(
                    fStringPool.getPrefixForQName(elementTypeName));
            namespaceURI = 
                fStringPool.toString(
                    fStringPool.getURIForQName(elementTypeName));
            localName = 
                fStringPool.toString(
                    fStringPool.getLocalPartForQName(elementTypeName));
        } else {
            localName = name;
        }
                
        // attributes
        setupDefaultAttributes();
        int index = ownerDocument.getNodeValue(fNodeIndex);
        if (index != -1) {
            NamedNodeMap attrs = getAttributes();
            do {
                NodeImpl attr = (NodeImpl)ownerDocument.getNodeObject(index);
                attrs.setNamedItem(attr);
                attr.parentNode = this;
                index = ownerDocument.getNextSibling(index);
            } while (index != -1);
        }

    } // synchronizeData()

    /**
     * Synchronizes the node's children with the internal structure.
     * Fluffing the children at once solves a lot of work to keep
     * the two structures in sync. The problem gets worse when
     * editing the tree -- this makes it a lot easier.
     */
    protected final void synchronizeChildren() {

        // no need to sync in the future
        syncChildren = false;

        // create children and link them as siblings
        DeferredDocumentImpl ownerDocument = (DeferredDocumentImpl)this.ownerDocument;
        NodeImpl last = null;
        for (int index = ownerDocument.getFirstChild(fNodeIndex);
             index != -1;
             index = ownerDocument.getNextSibling(index)) {

            NodeImpl node = (NodeImpl)ownerDocument.getNodeObject(index);
            if (last == null) {
                firstChild = node;
            }
            else {
                last.nextSibling = node;
            }
            node.parentNode = this;
            node.previousSibling = last;
            last = node;
        }
        if (last != null) {
            lastChild = last;
        }

    } // synchronizeChildren()

} // class DeferredElementImpl
