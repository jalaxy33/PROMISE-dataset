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

import org.w3c.dom.*;
import org.apache.xerces.utils.StringPool;

/**
 * This class represents a Document Type <em>declaraction</em> in
 * the document itself, <em>not</em> a Document Type Definition (DTD).
 * An XML document may (or may not) have such a reference.
 * <P>
 * DocumentType is an Extended DOM feature, used in XML documents but
 * not in HTML.
 * <P>
 * Note that Entities and Notations are no longer children of the
 * DocumentType, but are parentless nodes hung only in their
 * appropriate NamedNodeMaps.
 * <P>
 * This area is UNDERSPECIFIED IN REC-DOM-Level-1-19981001
 * Most notably, absolutely no provision was made for storing
 * and using Element and Attribute information. Nor was the linkage
 * between Entities and Entity References nailed down solidly.
 *
 * @version
 * @since  PR-DOM-Level-1-19980818.
 */
public class DeferredDocumentTypeImpl
    extends DocumentTypeImpl
    implements DeferredNode {

    //
    // Constants
    //

    /** Serialization version. */
    static final long serialVersionUID = -2172579663227313509L;

    //
    // Data
    //

    /** Node index. */
    protected transient int fNodeIndex;

    //
    // Constructors
    //

    /**
     * This is the deferred constructor. Only the fNodeIndex is given here.
     * All other data, can be requested from the ownerDocument via the index.
     */
    DeferredDocumentTypeImpl(DeferredDocumentImpl ownerDocument, int nodeIndex) {
        super(ownerDocument, null);

        fNodeIndex = nodeIndex;
        syncData = true;
        syncChildren = true;

    } // <init>(DeferredDocumentImpl,int)

    //
    // DeferredNode methods
    //

    /** Returns the node index. */
    public int getNodeIndex() {
        return fNodeIndex;
    }

    //
    // Protected methods
    //

    /** Synchronizes the data (name and value) for fast nodes. */
    protected void synchronizeData() {

        // no need to sync in the future
        syncData = false;

        // fluff data
        DeferredDocumentImpl ownerDocument = (DeferredDocumentImpl)this.ownerDocument;
        name = ownerDocument.getNodeNameString(fNodeIndex);

        // public and system ids
        StringPool pool = ownerDocument.getStringPool();
        int extraDataIndex = ownerDocument.getNodeValue(fNodeIndex);
        publicID = pool.toString(ownerDocument.getFirstChild(extraDataIndex));
        systemID = pool.toString(ownerDocument.getLastChild(extraDataIndex));
    } // synchronizeData()

    /** Synchronizes the entities, notations, and elements. */
    protected void synchronizeChildren() {

        // no need to synchronize again
        syncChildren = false;

        // create new node maps
        DeferredDocumentImpl ownerDocument = (DeferredDocumentImpl)this.ownerDocument;

        entities  = new NamedNodeMapImpl(ownerDocument, null);
        notations = new NamedNodeMapImpl(ownerDocument, null);
        elements  = new NamedNodeMapImpl(ownerDocument, null);

        // fill node maps
        for (int index = ownerDocument.getFirstChild(fNodeIndex);
            index != -1;
            index = ownerDocument.getNextSibling(index)) {

            DeferredNode node = ownerDocument.getNodeObject(index);
            int type = node.getNodeType();
            switch (type) {

                // internal, external, and unparsed entities
                case Node.ENTITY_NODE: {
                    entities.setNamedItem(node);
                    break;
                }

                // notations
                case Node.NOTATION_NODE: {
                    notations.setNamedItem(node);
                    break;
                }

                // element definitions
                case NodeImpl.ELEMENT_DEFINITION_NODE: {

                    // add element definition
                    elements.setNamedItem(node);

                    // add attributes to element definition
                    NamedNodeMap attrs = node.getAttributes();
                    for (int attrIndex = ownerDocument.getFirstChild(node.getNodeIndex());
                         attrIndex != -1;
                         attrIndex = ownerDocument.getNextSibling(attrIndex)) {
                        DeferredNode attr = ownerDocument.getNodeObject(attrIndex);
                        attrs.setNamedItem(attr);
                    }
                    break;
                }

                // elements
                case Node.ELEMENT_NODE: {
                    if (((DocumentImpl)getOwnerDocument()).allowGrammarAccess) {
                        appendChild(node);
                        break;
                    }
                }

                // NOTE: Should never get here! -Ac
                default: {
                    System.out.println("DeferredDocumentTypeImpl#synchronizeInfo: node.getNodeType() = "+node.getNodeType()+", class = "+node.getClass().getName());
                }
             }
        }

    } // synchronizeChildren()

} // class DeferredDocumentTypeImpl
