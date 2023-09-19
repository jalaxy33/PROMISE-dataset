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

import java.io.*;
import java.util.Enumeration;
import java.util.Vector;

import org.w3c.dom.*;

import org.apache.xerces.dom.traversal.NodeIteratorImpl;

import org.apache.xerces.domx.events.*;
import org.apache.xerces.dom.events.*;
//import org.w3c.dom.events.*;

/**
 * Node provides the basic structure of a DOM tree. It is never used
 * directly, but instead is subclassed to add type and data
 * information, and additional methods, appropriate to each node of
 * the tree. Only its subclasses should be instantiated -- and those,
 * with the exception of Document itself, only through a specific
 * Document's factory methods.
 * <P>
 * Node itself implements shared behaviors such as siblings and
 * children, both for consistancy and so that the most common tree
 * operations may be performed without constantly having to downcast
 * to specific node types. When there is no obvious mapping for one of
 * these queries, it will respond with null.
 * <P>
 * NodeImpl also implements NodeList, so it can return itself in
 * response to the getChildNodes() query. This eliminiates the need
 * for a separate ChildNodeList object. Note that this is an
 * IMPLEMENTATION DETAIL; applications should _never_ assume that
 * this identity exists.
 * <P>
 * Note that the default behavior is that children are forbidden. To
 * permit them, the subclass's constructor sets isLeafNode false.
 * <P>
 * All nodes in a single document must originate
 * in that document. (Note that this is much tighter than "must be
 * same implementation") Nodes are all aware of their ownerDocument,
 * and attempts to mismatch will throw WRONG_DOCUMENT_ERR.
 *
 * @version
 * @since  PR-DOM-Level-1-19980818.
 */
public abstract class NodeImpl
    implements Node, NodeList, EventTarget, Cloneable, Serializable {

    //
    // Constants
    //

    /** Serialization version. */
    static final long serialVersionUID = 2815829867052120872L;

    // public

    /** Element definition node type. */
    public static final short ELEMENT_DEFINITION_NODE = -1;

    //
    // Data
    //

    // links

    /** Owner document. */
	protected DocumentImpl ownerDocument;

    /** Parent node. */
    protected NodeImpl parentNode;

    /** Previous sibling. */
	protected NodeImpl previousSibling;

    /** Next sibling. */
    protected NodeImpl nextSibling;

    // data

    /** Node name. */
	protected String name;

    /** Node value. */
	protected String value;

    /** Read-only property. */
	protected boolean readOnly;

	/** NON-DOM FEATURE; see setUserData/getUserData. **/
	protected Object userData;
	
    // children

    /** First child. */
	protected NodeImpl firstChild;

    /** Last child. */
    protected NodeImpl lastChild;

    // lazy-evaluation nifo

    /** Synchronization of child nodes needed. */
    protected transient boolean syncChildren;

    /** Synchronization of data needed. */
    protected transient boolean syncData;

    // internal data

	/**
     * Number of alterations made to this subtree since its creation.
	 * Serves as a "dirty bit" so NodeList can recognize when an
	 * alteration has been made and discard its cached state information.
	 * <p>
	 * Any method that alters the tree structure MUST cause or be
	 * accompanied by a call to changed(), to inform it and its
	 * parents that any outstanding NodeLists may have to be updated.
     * <p>
	 * (Required because NodeList is simultaneously "live" and integer-
	 * indexed -- a bad decision in the DOM's design.)
	 * <p>
	 * Note that changes which do not affect the tree's structure -- changing
	 * the node's name, for example -- do _not_ have to call changed().
	 * <p>
	 * Alternative implementation would be to use a cryptographic
	 * Digest value rather than a count. This would have the advantage that
	 * "harmless" changes (those producing equal() trees) would not force
	 * NodeList to resynchronize. Disadvantage is that it's slightly more prone
	 * to "false negatives", though that's the difference between "wildly
	 * unlikely" and "absurdly unlikely". IF we start maintaining digests,
	 * we should consider taking advantage of them.
     */
	int changes = 0;

    /** Table for quick check of child insertion. */
	protected static int[] kidOK;

    //
    // Static initialization
    //

	static {

		kidOK = new int[13];
		
		kidOK[DOCUMENT_NODE] =
			1 << ELEMENT_NODE | 1 << PROCESSING_INSTRUCTION_NODE |
			1 << COMMENT_NODE | 1 << DOCUMENT_TYPE_NODE;
			
		kidOK[DOCUMENT_FRAGMENT_NODE] =
		kidOK[ENTITY_NODE] =
		kidOK[ENTITY_REFERENCE_NODE] =
		kidOK[ELEMENT_NODE] =
			1 << ELEMENT_NODE | 1 << PROCESSING_INSTRUCTION_NODE |
			1 << COMMENT_NODE | 1 << TEXT_NODE |
			1 << CDATA_SECTION_NODE | 1 << ENTITY_REFERENCE_NODE |
			1 << ATTRIBUTE_NODE;
			
		kidOK[ATTRIBUTE_NODE] =
			1 << TEXT_NODE | 1 << ENTITY_REFERENCE_NODE;
			
        kidOK[DOCUMENT_TYPE_NODE] =
		kidOK[PROCESSING_INSTRUCTION_NODE] =
		kidOK[COMMENT_NODE] =
		kidOK[TEXT_NODE] =
		kidOK[CDATA_SECTION_NODE] =
		kidOK[NOTATION_NODE] =
			0;

    } // static

    //
    // Constructors
    //

    /**
     * No public constructor; only subclasses of Node should be
     * instantiated, and those normally via a Document's factory methods
     * <p>
     * Every Node knows what Document it belongs to.
     */
    protected NodeImpl(DocumentImpl ownerDocument, String name, String value) {

        // set information
        // REVISITNS: This may have to be modifoed for DOM2:
        this.ownerDocument = ownerDocument;

        this.name  = name;
        this.value = value;

    } // <init>(DocumentImpl,String,short,boolean,String)

    /** Constructor for serialization. */
    public NodeImpl() {}

    //
    // Node methods
    //

    /**
     * A short integer indicating what type of node this is. The named
     * constants for this value are defined in the org.w3c.dom.Node interface.
     */
    public abstract short getNodeType();

    /**
     * For many nodes, nodeName is derived from their nodeType mnemonic
     * (lowercasified and preceeded by a '#' character). Others return the
     * "name of the data" -- tag name, attribute name, and so on.
     */
    public String getNodeName() {

        if (syncData) {
            synchronizeData();
        }
        return name;

    } // getNodeName():String

    /**
     * Any node which can have a nodeValue (@see getNodeValue) will
     * also accept requests to set it to a string.  The exact response to
     * this varies from node to node -- Attribute, for example, stores
     * its values in its children and has to replace them with a new Text
     * holding the replacement value.
     * <p>
     * For most types of Node, value is null and attempting to set it
     * will throw DOMException(NO_MODIFICATION_ALLOWED_ERR). This will
     * also be thrown if the node is read-only.
     */
    public void setNodeValue(String value) {

    	if (readOnly)
    		throw new DOMExceptionImpl(
    			DOMException.NO_MODIFICATION_ALLOWED_ERR, 
    			"NO_MODIFICATION_ALLOWED_ERR");
        // revisit: may want to set the value in ownerDocument.
    	// Default behavior, overridden in some subclasses
        if (syncData) {
            synchronizeData();
            }
            
        // Cache old value for DOMCharacterDataModified.
        String oldvalue=this.value;
        EnclosingAttr enclosingAttr=null;
        if(MUTATIONEVENTS)
        {
            // MUTATION PREPROCESSING AND PRE-EVENTS:
            // If we're within the scope of an Attr and DOMAttrModified 
            // was requested, we need to preserve its previous value for
            // that event.
            LCount lc=LCount.lookup(MutationEventImpl.DOM_ATTR_MODIFIED);
            if(lc.captures+lc.bubbles+lc.defaults>0)
            {
                enclosingAttr=getEnclosingAttr();
            }
        } // End mutation preprocessing
            
    	this.value = value;
    	
        if(MUTATIONEVENTS)
        {
            // MUTATION POST-EVENTS:
            LCount lc=LCount.lookup(MutationEventImpl.DOM_CHARACTER_DATA_MODIFIED);
            if(lc.captures+lc.bubbles+lc.defaults>0)
            {
                MutationEvent me=
                    new MutationEventImpl();
                    //?????ownerDocument.createEvent("MutationEvents");
                me.initMutationEvent(MutationEventImpl.DOM_CHARACTER_DATA_MODIFIED,true,false,
                    null,oldvalue,value,null);
                dispatchEvent(me);
            }
            
            // Subroutine: Transmit DOMAttrModified and DOMSubtreeModified,
            // if required. (Common to most kinds of mutation)
            dispatchAggregateEvents(enclosingAttr);
        } // End mutation postprocessing

    } // setNodeValue(String)

    /**
     * nodeValue is a string representation of the data contained in
     * this node. Each subclass of Node defines what information is
     * considered its value, or returns null if the query is not
     * appropriate. Entity references should be expanded before being
     * returned.
     * <p>
     * For most types of Node, value is null and may not be changed.
     * <p>
     * For Text, CDATASection, ProcessingInstruction, and Comment value
     * is a string representing the contents of the node.
     * <p>
     * For Attribute, value is the string returned by the attribute's
     * getValue method. This will be gathered from the Attribute's
     * children.
     *
     * @throws DOMException(DOMSTRING_SIZE_ERR) when it would return more
     * characters than fit in a DOMString variable on the implementation
     * platform. (Will never arise in this implementation)
     */
    public String getNodeValue() {

        if (syncData) {
            synchronizeData();
            }
    	return value;

    } // getNodeValue():String

    /**
     * Adds a child node to the end of the list of children for this node.
     * Convenience shorthand for insertBefore(newChild,null).
     * @see #insertBefore(Node, Node)
     *
     * @returns newChild, in its new state (relocated, or emptied in the
     * case of DocumentNode.)
     *
     * @throws DOMException(HIERARCHY_REQUEST_ERR) if newChild is of a
     * type that shouldn't be a child of this node.
     *
     * @throws DOMException(WRONG_DOCUMENT_ERR) if newChild has a
     * different owner document than we do.
     *
     * @throws DOMException(NO_MODIFICATION_ALLOWED_ERR) if this node is
     * read-only.
     */
    public Node appendChild(Node newChild) throws DOMException {
    	return insertBefore(newChild, null);
    }

    /**
     * Returns a duplicate of a given node. You can consider this a
     * generic "copy constructor" for nodes. The newly returned object should
     * be completely independent of the source object's subtree, so changes
     * in one after the clone has been made will not affect the other.
     * <p>
     * Example: Cloning a Text node will copy both the node and the text it
     * contains.
     * <p>
     * Example: Cloning something that has children -- Element or Attr, for
     * example -- will _not_ clone those children unless a "deep clone"
     * has been requested. A shallow clone of an Attr node will yield an
     * empty Attr of the same name.
     * <p>
     * NOTE: Clones will always be read/write, even if the node being cloned
     * is read-only, to permit applications using only the DOM API to obtain
     * editable copies of locked portions of the tree.
     */
    public Node cloneNode(boolean deep) {

    	
        if (syncData) {
            synchronizeData();
            }
        if (syncChildren) {
            synchronizeChildren();
        }
    	
    	NodeImpl newnode;
    	try {
    		newnode = (NodeImpl)clone();
    	}
    	catch (CloneNotSupportedException e) {
//	        Revisit : don't fail silently - but don't want to tie to parser guts
//    		System.out.println("UNEXPECTED "+e);
    		return null;
    	}
    	
    	newnode.readOnly = false;

    	// Need to break the association w/ original kids
    	newnode.parentNode      = null;
    	newnode.previousSibling = null;
        newnode.nextSibling     = null;
    	newnode.firstChild      = null;
        newnode.lastChild       = null;

    	// Then, if deep, clone the kids too.
    	if (deep) {
    		for (NodeImpl child = (NodeImpl)getFirstChild();
    			child != null;
    			child = (NodeImpl)child.getNextSibling()) {
    			newnode.appendChild(child.cloneNode(true));
            }
        }

        // REVISIT: Should the user data be cloned?

    	return newnode;

    } // cloneNode(boolean):Node

    /**
     * Find the Document that this Node belongs to (the document in
     * whose context the Node was created). The Node may or may not
     * currently be part of that Document's actual contents.
     */
    public Document getOwnerDocument() {
        return ownerDocument;
    }

    /**
     * Obtain the DOM-tree parent of this node, or null if it is not
     * currently active in the DOM tree (perhaps because it has just been
     * created or removed). Note that Document, DocumentFragment, and
     * Attribute will never have parents.
     */
    public Node getParentNode() {
        return parentNode;
    }

    /** The next child of this node's parent, or null if none */
    public Node getNextSibling() {
        return nextSibling;
    }

    /** The previous child of this node's parent, or null if none */
    public Node getPreviousSibling() {
        return previousSibling;
    }

    /**
     * Return the collection of attributes associated with this node,
     * or null if none. At this writing, Element is the only type of node
     * which will ever have attributes.
     *
     * @see ElementImpl
     */
    public NamedNodeMap getAttributes() {
    	return null; // overridden in ElementImpl
    }

    /**
     * Test whether this node has any children. Convenience shorthand
     * for (Node.getFirstChild()!=null)
     */
    public boolean hasChildNodes() {
        if (syncChildren) {
            synchronizeChildren();
        }
        return firstChild != null;
    }

    /**
     * Obtain a NodeList enumerating all children of this node. If there
     * are none, an (initially) empty NodeList is returned.
     * <p>
     * NodeLists are "live"; as children are added/removed the NodeList
     * will immediately reflect those changes. Also, the NodeList refers
     * to the actual nodes, so changes to those nodes made via the DOM tree
     * will be reflected in the NodeList and vice versa.
     * <p>
     * In this implementation, Nodes implement the NodeList interface and
     * provide their own getChildNodes() support. Other DOMs may solve this
     * differently.
     */
    public NodeList getChildNodes() {
        // JKESS: KNOWN ISSUE HERE 

        if (syncChildren) {
            synchronizeChildren();
        }
        return this;

    } // getChildNodes():NodeList

    /** The first child of this Node, or null if none. */
    public Node getFirstChild() {

        if (syncChildren) {
            synchronizeChildren();
        }
    	return firstChild;

    }   // getFirstChild():Node

    /** The first child of this Node, or null if none. */
    public Node getLastChild() {

        if (syncChildren) {
            synchronizeChildren();
        }
   	    return lastChild;

    } // getLastChild():Node

    /**
     * Move one or more node(s) to our list of children. Note that this
     * implicitly removes them from their previous parent.
     *
     * @param newChild The Node to be moved to our subtree. As a
     * convenience feature, inserting a DocumentNode will instead insert
     * all its children.
     *
     * @param refChild Current child which newChild should be placed
     * immediately before. If refChild is null, the insertion occurs
     * after all existing Nodes, like appendChild().
     *
     * @returns newChild, in its new state (relocated, or emptied in the
     * case of DocumentNode.)
     *
     * @throws DOMException(HIERARCHY_REQUEST_ERR) if newChild is of a
     * type that shouldn't be a child of this node, or if newChild is an
     * ancestor of this node.
     *
     * @throws DOMException(WRONG_DOCUMENT_ERR) if newChild has a
     * different owner document than we do.
     *
     * @throws DOMException(NOT_FOUND_ERR) if refChild is not a child of
     * this node.
     *
     * @throws DOMException(NO_MODIFICATION_ALLOWED_ERR) if this node is
     * read-only.
     */
	public Node insertBefore(Node newChild, Node refChild) 
		throws DOMException {
		  // Tail-call; optimizer should be able to do good things with.
		  return internalInsertBefore(newChild,refChild,MUTATION_ALL);
    } // insertBefore(Node,Node):Node
     
	/** NON-DOM INTERNAL: Within DOM actions,we sometimes need to be able
	 * to control which mutation events are spawned. This version of the
	 * insertBefore operation allows us to do so. It is not intended
	 * for use by application programs.
	 */
    Node internalInsertBefore(Node newChild, Node refChild,int mutationMask) 
        throws DOMException {

    	if (readOnly)
    		throw new DOMExceptionImpl(
    			DOMException.NO_MODIFICATION_ALLOWED_ERR, 
    			"NO_MODIFICATION_ALLOWED_ERR");
     	
    	if( !(newChild instanceof NodeImpl)
    		||
    		!(
    		  newChild.getOwnerDocument() == ownerDocument
    		  ||
    	 	 // SPECIAL-CASE: Document has no owner, but may be the owner.
    		  ( getNodeType() == Node.DOCUMENT_NODE &&
    		    newChild.getOwnerDocument() == (Document)this )
    		) ) {
    		throw new DOMExceptionImpl(DOMException.WRONG_DOCUMENT_ERR, 
    		                           "WRONG_DOCUMENT_ERR");
        }

        if (syncChildren) {
            synchronizeChildren();
        }

    	// Convert to internal type, to avoid repeated casting
    	NodeImpl newInternal = (NodeImpl)newChild;

    	// Prevent cycles in the tree
    	boolean treeSafe = true;
    	for (NodeImpl a = parentNode; treeSafe && a != null; a = a.parentNode) {
    		treeSafe = newInternal != a;
        }
    	if(!treeSafe) {
    	  	throw new DOMExceptionImpl(DOMException.HIERARCHY_REQUEST_ERR, 
    	  	                           "HIERARCHY_REQUEST_ERR");
        }

    	// refChild must in fact be a child of this node (or null)
    	if(refChild != null && refChild.getParentNode() != this) {
    		throw new DOMExceptionImpl(DOMException.NOT_FOUND_ERR,
    		                           "NOT_FOUND_ERR");
        }
    	
    	if (newInternal.getNodeType() == Node.DOCUMENT_FRAGMENT_NODE) {
    		// SLOW BUT SAFE: We could insert the whole subtree without
    		// juggling so many next/previous pointers. (Wipe out the
    		// parent's child-list, patch the parent pointers, set the
    		// ends of the list.) But we know some subclasses have special-
    		// case behavior they add to insertBefore(), so we don't risk it.
    		// This approch also takes fewer bytecodes.

    		// NOTE: If one of the children is not a legal child of this
    		// node, throw HIERARCHY_REQUEST_ERR before _any_ of the children
    		// have been transferred. (Alternative behaviors would be to
    		// reparent up to the first failure point or reparent all those
    		// which are acceptable to the target node, neither of which is
    		// as robust. PR-DOM-0818 isn't entirely clear on which it
    		// recommends?????

    		// No need to check kids for right-document; if they weren't,
    		// they wouldn't be kids of that DocFrag.
    		for (Node kid = newInternal.getFirstChild(); // Prescan
    			kid != null;
    			kid = kid.getNextSibling()) {

    		    if (!ownerDocument.isKidOK(this, kid)) {
        		  	throw new DOMExceptionImpl(DOMException.HIERARCHY_REQUEST_ERR, 
        		  	                           "HIERARCHY_REQUEST_ERR");
                }
    		}		  	

    		while (newInternal.hasChildNodes())	{
    		    insertBefore(newInternal.getFirstChild(), refChild);
            }
    	}
    	
    	else if (!ownerDocument.isKidOK(this, newInternal)) {
    		throw new DOMExceptionImpl(DOMException.HIERARCHY_REQUEST_ERR, 
    		                           "HIERARCHY_REQUEST_ERR");
        }
    	else {
		    EnclosingAttr enclosingAttr=null;
		    if(MUTATIONEVENTS && (mutationMask&MUTATION_AGGREGATE)!=0)
			{
			    // MUTATION PREPROCESSING
			    // No direct pre-events, but if we're within the scope 
			    // of an Attr and DOMAttrModified was requested,
        	    // we need to preserve its previous value.
		        LCount lc=LCount.lookup(MutationEventImpl.DOM_ATTR_MODIFIED);
                if(lc.captures+lc.bubbles+lc.defaults>0)
                {
                    enclosingAttr=getEnclosingAttr();
                }
			}
    	    
    		Node oldparent = newInternal.getParentNode();
    		if (oldparent != null) {
    			oldparent.removeChild(newInternal);
            }

    		NodeImpl prev;
    		// Find the node we're inserting after, if any (null if
    		// inserting to head of list)
    		prev = (refChild == null)
                   ? lastChild : ((NodeImpl)refChild).previousSibling;

    		// Attach up
    		newInternal.parentNode = this;

    		// Attach after
    		newInternal.previousSibling = prev;
    		if (prev == null) {
    		    firstChild = newInternal;
            }
            else {
    		    prev.nextSibling = newInternal;
            }

    		// Attach before
    		newInternal.nextSibling = (NodeImpl)refChild;
    		if (refChild == null) {
    		    lastChild=newInternal;
            }
            else {
    		    ((NodeImpl)refChild).previousSibling = newInternal;
            }

    	    changed();
    	
			if(MUTATIONEVENTS)
			{
			    // MUTATION POST-EVENTS:
			    // "Local" events (non-aggregated)
			    if( (mutationMask&MUTATION_LOCAL) != 0)
			    {
    			    // New child is told it was inserted, and where
                    LCount lc=LCount.lookup(MutationEventImpl.DOM_NODE_INSERTED);
                    if(lc.captures+lc.bubbles+lc.defaults>0)
                    {
                        MutationEvent me=
                            new MutationEventImpl();
                            //?????ownerDocument.createEvent("MutationEvents");
                        me.initMutationEvent(MutationEventImpl.DOM_NODE_INSERTED,true,false,
                            this,null,null,null);
                        newInternal.dispatchEvent(me);
                    }
		    	    
			        // If within the Document, tell the subtree it's been added
			        // to the Doc.
                    lc=LCount.lookup(MutationEventImpl.DOM_NODE_INSERTED_INTO_DOCUMENT);
                    if(lc.captures+lc.bubbles+lc.defaults>0)
                    {
                        NodeImpl eventAncestor=this;
                        if(enclosingAttr!=null) 
                            eventAncestor=(NodeImpl)(enclosingAttr.node.getOwnerElement());
                        if(eventAncestor!=null) // Might have been orphan Attr
                        {
                            NodeImpl p=eventAncestor;
                            while(p!=null)
                            {
                                eventAncestor=p; // Last non-null ancestor
                                // In this context, ancestry includes
                                // walking back from Attr to Element
                                if(p.getNodeType()==ATTRIBUTE_NODE)
                                    p=(ElementImpl)( ((AttrImpl)p).getOwnerElement() );
                                else
                                    p=p.parentNode;
                            }
                            if(eventAncestor.getNodeType()==Node.DOCUMENT_NODE)
                            {
                                MutationEvent me=
                                    new MutationEventImpl();
                                    //?????ownerDocument.createEvent("MutationEvents");
                                me.initMutationEvent(MutationEventImpl.DOM_NODE_INSERTED_INTO_DOCUMENT,
                                    false,false,
                                    null,null,null,null);
                                dispatchEventToSubtree(newInternal,me);
                            }
                        }
			        }
			    }  

                // Subroutine: Transmit DOMAttrModified and DOMSubtreeModified
                // (Common to most kinds of mutation)
                if( (mutationMask&MUTATION_AGGREGATE) != 0)
                    dispatchAggregateEvents(enclosingAttr);
			}
            
    	}
    	
    	return newInternal;

    } // internalInsertBefore(Node,Node,int):Node

    /**
     * Remove a child from this Node. The removed child's subtree
     * remains intact so it may be re-inserted elsewhere.
     *
     * @return oldChild, in its new state (removed).
     *
     * @throws DOMException(NOT_FOUND_ERR) if oldChild is not a child of
     * this node.
     *
     * @throws DOMException(NO_MODIFICATION_ALLOWED_ERR) if this node is
     * read-only.
     */
	public Node removeChild(Node oldChild) 
		throws DOMException {
		    // Tail-call, should be optimizable
		    return internalRemoveChild(oldChild,MUTATION_ALL);
	} // removeChild(Node) :Node
     
	/** NON-DOM INTERNAL: Within DOM actions,we sometimes need to be able
	 * to control which mutation events are spawned. This version of the
	 * removeChild operation allows us to do so. It is not intended
	 * for use by application programs.
	 */
    Node internalRemoveChild(Node oldChild,int mutationMask)
        throws DOMException {

    	if (readOnly) {
    		throw new DOMExceptionImpl(
    			DOMException.NO_MODIFICATION_ALLOWED_ERR, 
    			"NO_MODIFICATION_ALLOWED_ERR");
        }
     	
    	if (oldChild != null && oldChild.getParentNode() != this) {
    		throw new DOMExceptionImpl(DOMException.NOT_FOUND_ERR, 
    		                           "NOT_FOUND_ERR");
        }

        // call out to any NodeIterators to remove the Node and fix-up the iterator.
        Enumeration iterators = ownerDocument.getNodeIterators();
        if (iterators != null) {
            while ( iterators.hasMoreElements()) {
                ((NodeIteratorImpl)iterators.nextElement()).removeNode(oldChild);
            }
        }

        NodeImpl oldInternal = (NodeImpl) oldChild;

	    EnclosingAttr enclosingAttr=null;
	    if(MUTATIONEVENTS)
	    {
    		// MUTATION PREPROCESSING AND PRE-EVENTS:
	    	// If we're within the scope of an Attr and DOMAttrModified 
		    // was requested, we need to preserve its previous value for
		    // that event.
		    LCount lc=LCount.lookup(MutationEventImpl.DOM_ATTR_MODIFIED);
            if(lc.captures+lc.bubbles+lc.defaults>0)
            {
                enclosingAttr=getEnclosingAttr();
            }
            
            if( (mutationMask&MUTATION_LOCAL) != 0)
            {
                // Child is told that it is about to be removed
                lc=LCount.lookup(MutationEventImpl.DOM_NODE_REMOVED);
                if(lc.captures+lc.bubbles+lc.defaults>0)
                {
                    MutationEvent me=
                        new MutationEventImpl();
                        //?????ownerDocument.createEvent("MutationEvents");
                    me.initMutationEvent(MutationEventImpl.DOM_NODE_REMOVED,true,false,
                        this,null,null,null);
                    oldInternal.dispatchEvent(me);
                }
            
                // If within Document, child's subtree is informed that it's
                // losing that status
                lc=LCount.lookup(MutationEventImpl.DOM_NODE_REMOVED_FROM_DOCUMENT);
                if(lc.captures+lc.bubbles+lc.defaults>0)
                {
                    NodeImpl eventAncestor=this;
                    if(enclosingAttr!=null) 
                        eventAncestor=(NodeImpl)(enclosingAttr.node.getOwnerElement());
                    if(eventAncestor!=null) // Might have been orphan Attr
                    {
                        for(NodeImpl p=eventAncestor.parentNode;
                            p!=null;
                            p=p.parentNode)
                        {
                            eventAncestor=p; // Last non-null ancestor
                        }
                        if(eventAncestor.getNodeType()==Node.DOCUMENT_NODE)
                        {
                            MutationEvent me=
                                new MutationEventImpl();
                                //?????ownerDocument.createEvent("MutationEvents");
                            me.initMutationEvent(MutationEventImpl.DOM_NODE_REMOVED_FROM_DOCUMENT,
                                    false,false,
                                    null,null,null,null);
                            dispatchEventToSubtree(oldInternal,me);
                        }
                    }
                }
            }
		} // End mutation preprocessing

    	// Patch tree past oldChild
    	NodeImpl prev = oldInternal.previousSibling;
    	NodeImpl next = oldInternal.nextSibling;

    	if (prev != null) {
    		prev.nextSibling = next;
        }
        else {
    		firstChild = next;
        }

    	if (next != null) {
    		next.previousSibling = prev;
        }
        else {
    		lastChild = prev;
        }

    	// Remove oldInternal's references to tree
    	oldInternal.parentNode      = null;
        oldInternal.nextSibling     = null;
        oldInternal.previousSibling = null;

    	changed();

	    if(MUTATIONEVENTS)
	    {
    		// MUTATION POST-EVENTS:
            // Subroutine: Transmit DOMAttrModified and DOMSubtreeModified,
		    // if required. (Common to most kinds of mutation)
		    if( (mutationMask&MUTATION_AGGREGATE) != 0)
                dispatchAggregateEvents(enclosingAttr);
	    } // End mutation postprocessing

    	return oldInternal;

    } // internalRemoveChild(Node,int):Node

    /**
     * Make newChild occupy the location that oldChild used to
     * have. Note that newChild will first be removed from its previous
     * parent, if any. Equivalent to inserting newChild before oldChild,
     * then removing oldChild.
     *
     * @returns oldChild, in its new state (removed).
     *
     * @throws DOMException(HIERARCHY_REQUEST_ERR) if newChild is of a
     * type that shouldn't be a child of this node, or if newChild is
     * one of our ancestors.
     *
     * @throws DOMException(WRONG_DOCUMENT_ERR) if newChild has a
     * different owner document than we do.
     *
     * @throws DOMException(NOT_FOUND_ERR) if oldChild is not a child of
     * this node.
     *
     * @throws DOMException(NO_MODIFICATION_ALLOWED_ERR) if this node is
     * read-only.
     */
    public Node replaceChild(Node newChild, Node oldChild)
        throws DOMException {
		// If Mutation Events are being generated, this operation might
		// throw aggregate events twice when modifying an Attr -- once 
		// on insertion and once on removal. DOM Level 2 does not specify 
		// this as either desirable or undesirable, but hints that
		// aggregations should be issued only once per user request.
		
        EnclosingAttr enclosingAttr=null;
        if(MUTATIONEVENTS)
        {
            // MUTATION PREPROCESSING AND PRE-EVENTS:
            // If we're within the scope of an Attr and DOMAttrModified 
            // was requested, we need to preserve its previous value for
            // that event.
            LCount lc=LCount.lookup(MutationEventImpl.DOM_ATTR_MODIFIED);
            if(lc.captures+lc.bubbles+lc.defaults>0)
            {
                enclosingAttr=getEnclosingAttr();
            }
        } // End mutation preprocessing

		internalInsertBefore(newChild, oldChild,MUTATION_LOCAL);
		internalRemoveChild(oldChild,MUTATION_LOCAL);
		
		if(MUTATIONEVENTS)
		{
		    dispatchAggregateEvents(enclosingAttr);
		}
		
		return oldChild;
    }

    //
    // NodeList methods
    //

    /**
     * NodeList method: Count the immediate children of this node
     * @return int
     */
    public int getLength() {

        // It is assumed that the getChildNodes call synchronized
        // the children. Therefore, we can access the first child
        // reference directly.
    	int count = 0;
    	for (NodeImpl node = firstChild; node != null; node = node.nextSibling) {
    		count++;
    	}
    	return count;

    } // getLength():int

    /**
     * NodeList method: Return the Nth immediate child of this node, or
     * null if the index is out of bounds.
     * @return org.w3c.dom.Node
     * @param Index int
     */
    public Node item(int index) {

        // It is assumed that the getChildNodes call synchronized
        // the children. Therefore, we can access the first child
        // reference directly.
        NodeImpl node = firstChild;
        for (int i = 0; i < index && node != null; i++) {
            node = node.nextSibling;
        }
        return node;

    } // item(int):Node

    //
    // DOM2: methods, getters, setters
    //

    /**
     * Introduced in DOM Level 2. <p>
     * Tests whether the DOM implementation implements a specific feature and that
     * feature is supported by this node.
     * @param feature       The package name of the feature to test. This is the
     *                      same name as what can be passed to the method
     *                      hasFeature on DOMImplementation.
     * @param version       This is the version number of the package name to
     *                      test. In Level 2, version 1, this is the string "2.0". If
     *                      the version is not specified, supporting any version of
     *                      the feature will cause the method to return true.
     * @return boolean      Returns true if this node defines a subtree within which the
     *                      specified feature is supported, false otherwise.
     * @since WD-DOM-Level-2-19990923
     */
    public boolean supports(String feature, String version)
    {
        return ownerDocument.getImplementation().hasFeature(feature, version);
    }

    /**
     * Introduced in DOM Level 2. <p>
     *
     * The namespace URI of this node, or null if it is unspecified. When this node
     * is of any type other than ELEMENT_NODE and ATTRIBUTE_NODE, this is always
     * null and setting it has no effect. <p>
     *
     * This is not a computed value that is the result of a namespace lookup based on
     * an examination of the namespace declarations in scope. It is merely the
     * namespace URI given at creation time.<p>
     *
     * For nodes created with a DOM Level 1 method, such as createElement
     * from the Document interface, this is null.
     * @since WD-DOM-Level-2-19990923
     * @see AttrImpl
     * @see ElementImpl
     */
    public String getNamespaceURI()
    {
        return null;
    }

    /**
     * Introduced in DOM Level 2. <p>
     *
     * The namespace prefix of this node, or null if it is unspecified. When this
     * node is of any type other than ELEMENT_NODE and ATTRIBUTE_NODE this is
     * always null and setting it has no effect.<p>
     *
     * For nodes created with a DOM Level 1 method, such as createElement
     * from the Document interface, this is null. <p>
     *
     * @since WD-DOM-Level-2-19990923
     * @see AttrImpl
     * @see ElementImpl
     */
    public String getPrefix()
    {
        return null;
    }

    /**
     *  Introduced in DOM Level 2. <p>
     *
     *  The namespace prefix of this node, or null if it is unspecified. When this
     *  node is of any type other than ELEMENT_NODE and ATTRIBUTE_NODE this is
     *  always null and setting it has no effect.<p>
     *
     *  For nodes created with a DOM Level 1 method, such as createElement
     *  from the Document interface, this is null.<p>
     *
     *  Note that setting this attribute changes the nodeName attribute, which holds the
     *  qualified name, as well as the tagName and name attributes of the Element
     *  and Attr interfaces, when applicable.<p>
     *
     * @throws INVALID_CHARACTER_ERR Raised if the specified
     *  prefix contains an invalid character.
     *
     * @since WD-DOM-Level-2-19990923
     * @see AttrImpl
     * @see ElementImpl
     */
    public void setPrefix(String prefix)
        throws DOMException
    {
        //this space intentionally left blank.
    }

    /**
     * Introduced in DOM Level 2. <p>
     *
     * Returns the local part of the qualified name of this node.
     * For nodes created with a DOM Level 1 method, such as createElement
     * from the Document interface, and for nodes of any type other than
     * ELEMENT_NODE and ATTRIBUTE_NODE this is the same as the nodeName
     * attribute.
     * @since WD-DOM-Level-2-19990923
     * @see AttrImpl
     * @see ElementImpl
     */
    public String             getLocalName()
    {
        if (syncData) {
            synchronizeData();
        }
        return name;
    }
    
    
    //
    // EventTarget support (public and internal)
    //
	// Constants
	//
	/** Compile-time flag. If false, disables our code for
	    the DOM Level 2 Events module, perhaps allowing it
	    to be optimized out to save bytecodes.
	*/
	protected final static boolean MUTATIONEVENTS=true;
	
	/** The MUTATION_ values are parameters to the NON-DOM 
	    internalInsertBefore() and internalRemoveChild() operations,
	    allowing us to control which MutationEvents are generated.
	 */
	protected final static int MUTATION_NONE=0x00;
	protected final static int MUTATION_LOCAL=0x01;
	protected final static int MUTATION_AGGREGATE=0x02;
	protected final static int MUTATION_ALL=0xffff;
	/** NON-DOM INTERNAL: EventListeners currently registered at
	 * THIS NODE; preferably null if none.
	 */
    Vector nodeListeners=null;
	
	/* NON-DOM INTERNAL: Class LEntry is just a struct used to represent
	 * event listeners registered with this node. Copies of this object
	 * are hung from the nodeListeners Vector.
	 * <p>
	 * I considered using two vectors -- one for capture,
	 * one for bubble -- but decided that since the list of listeners 
	 * is probably short in most cases, it might not be worth spending
	 * the space. ***** REVISIT WHEN WE HAVE MORE EXPERIENCE.
	 */
	class LEntry
	{
	    String type;
	    EventListener listener;
	    boolean useCapture;
	    
	    /** NON-DOM INTERNAL: Constructor for Listener list Entry 
	     * @param type Event name (NOT event group!) to listen for.
	     * @param listener Who gets called when event is dispatched
	     * @param useCaptue True iff listener is registered on
	     *  capturing phase rather than at-target or bubbling
	     */
	    LEntry(String type,EventListener listener,boolean useCapture)
	    {
	        this.type=type;this.listener=listener;this.useCapture=useCapture;
	    }
	}; // LEntry
	
	/** Introduced in DOM Level 2. <p>
     * Register an event listener with this Node. A listener may be independently
     * registered as both Capturing and Bubbling, but may only be
     * registered once per role; redundant registrations are ignored.
     * @param type Event name (NOT event group!) to listen for.
	 * @param listener Who gets called when event is dispatched
	 * @param useCapture True iff listener is registered on
	 *  capturing phase rather than at-target or bubbling
	 */
	public void addEventListener(String type,EventListener listener,boolean useCapture)
	{
        // We can't dispatch to blank type-name, and of course we need
        // a listener to dispatch to
	    if(type==null || type.equals("") || listener==null)
	        return;

	    // Each listener may be registered only once per type per phase.
	    // Simplest way to code that is to zap the previous entry, if any.
	    removeEventListener(type,listener,useCapture);
	    
	    if(nodeListeners==null) nodeListeners=new Vector();
	    nodeListeners.addElement(new LEntry(type,listener,useCapture));
	    
	    // Record active listener
	    LCount lc=LCount.lookup(type);
	    if(useCapture)
	        ++lc.captures;
	    else
	        ++lc.bubbles;
	} // addEventListener(String,EventListener,boolean) :void
	
	/** Introduced in DOM Level 2. <p>
     * Deregister an event listener previously registered with this Node. 
     * A listener must be independently removed from the 
     * Capturing and Bubbling roles. Redundant removals (of
     * listeners not currently registered for this role) are ignored.
     * @param type Event name (NOT event group!) to listen for.
	 * @param listener Who gets called when event is dispatched
	 * @param useCapture True iff listener is registered on
	 *  capturing phase rather than at-target or bubbling
	 */
	public void removeEventListener(String type,EventListener listener,boolean useCapture)
	{
	    // If this couldn't be a valid listener registration, ignore request
  	    if(nodeListeners==null || type==null || type.equals("") || listener==null)
	        return;

        // Note that addListener has previously ensured that 
	    // each listener may be registered only once per type per phase.
        for(int i=nodeListeners.size()-1;i>=0;--i) // count-down is OK for deletions!
        {
            LEntry le=(LEntry)(nodeListeners.elementAt(i));
            if(le.useCapture==useCapture && le.listener==listener && 
                le.type.equals(type))
            {
                nodeListeners.removeElementAt(i);
                // Storage management: Discard empty listener lists
                if(nodeListeners.size()==0) nodeListeners=null;

	            // Remove active listener
	            LCount lc=LCount.lookup(type);
        	    if(useCapture)
	                --lc.captures;
        	    else
	                --lc.bubbles;
	                
                break;  // Found it; no need to loop farther.
            }
        }
	} // removeEventListener(String,EventListener,boolean) :void
	
	/** NON-DOM INTERNAL:
	    A finalizer has added to NodeImpl in order to correct the event-usage
	    counts of any remaining listeners before discarding the Node.
	    This isn't absolutely required, and finalizers are of dubious
	    reliability and have odd effects on some implementations of GC.
	    But given the expense of event generation and distribution it 
	    seems a worthwhile safety net.
	    ***** RECONSIDER at some future point.
	   */
	protected void finalize() throws Throwable
	{
	    super.finalize();
	    if(nodeListeners!=null)
            for(int i=nodeListeners.size()-1;i>=0;--i) // count-down is OK for deletions!
            {
                LEntry le=(LEntry)(nodeListeners.elementAt(i));
                LCount lc=LCount.lookup(le.type);
           	    if(le.useCapture)
	                --lc.captures;
                else
	                --lc.bubbles;
	        }
	}	

    /**
     * Introduced in DOM Level 2. <p>
     * Distribution engine for DOM Level 2 Events. 
     * <p>
     * Event propagation runs as follows:
     * <ol>
     * <li>Event is dispatched to a particular target node, which invokes
     *   this code. Note that the event's stopPropagation flag is
     *   cleared when dispatch begins; thereafter, if it has 
     *   been set before processing of a node commences, we instead
     *   immediately advance to the DEFAULT phase.
     * <li>The node's ancestors are established as destinations for events.
     *   For capture and bubble purposes, node ancestry is determined at 
     *   the time dispatch starts. If an event handler alters the document 
     *   tree, that does not change which nodes will be informed of the event. 
     * <li>CAPTURING_PHASE: Ancestors are scanned, root to target, for 
     *   Capturing listeners. If found, they are invoked (see below). 
     * <li>AT_TARGET: 
     *   Event is dispatched to NON-CAPTURING listeners on the
     *   target node. Note that capturing listeners on this node are _not_
     *   invoked.
     * <li>BUBBLING_PHASE: Ancestors are scanned, target to root, for
     *   non-capturing listeners. 
     * <li>Default processing: Some DOMs have default behaviors bound to specific
     *   nodes. If this DOM does, and if the event's preventDefault flag has
     *   not been set, we now return to the target node and process its
     *   default handler for this event, if any.
     * </ol>
     * <p>
     * Note that (de)registration of handlers during
     * processing of an event does not take effect during
     * this phase of this event; they will not be called until
     * the next time this node is visited by dispatchEvent.
     * <p>
     * If an event handler itself causes events to be dispatched, they are
     * processed synchronously, before processing resumes
     * on the event which triggered them. Please be aware that this may 
     * result in events arriving at listeners "out of order" relative
     * to the actual sequence of requests.
     * <p>
     * Note that our implementation resets the event's stop/prevent flags
     * when dispatch begins.
     * I believe the DOM's intent is that event objects be redispatchable,
     * though it isn't stated in those terms.
     * @param event the event object to be dispatched to 
     * registered EventListeners
     * @return true if the event's <code>preventDefault()</code>
     * method was invoked by an EventListener; otherwise false.
    */
	public boolean dispatchEvent(Event event)
    {
        if(event==null) return false;
        
        // Can't use anyone else's implementation, since there's no public
        // API for setting the event's processing-state fields.
        EventImpl evt=(EventImpl)event;

        // VALIDATE -- must have been initialized at least once, must have
        // a non-null non-blank name.
        if(!evt.initialized || evt.type==null || evt.type.equals(""))
            throw new DOMExceptionImpl(DOMExceptionImpl.UNSPECIFIED_EVENT_TYPE,"");
        
        // If nobody is listening for this event, discard immediately
        LCount lc=LCount.lookup(evt.getType());
        if(lc.captures+lc.bubbles+lc.defaults==0)
            return evt.preventDefault;

        // INITIALIZE THE EVENT'S DISPATCH STATUS
        // (Note that Event objects are reusable in our implementation;
        // that doesn't seem to be explicitly guaranteed in the DOM, but
        // I believe it is the intent.)
        evt.target=this;
        evt.stopPropagation=false;
        evt.preventDefault=false;
        
        // Capture pre-event parentage chain, not including target;
        // use pre-event-dispatch ancestors even if event handlers mutate
        // document and change the target's context.
        // Note that this is parents ONLY; events do not
        // cross the Attr/Element "blood/brain barrier". 
        // DOMAttrModified. which looks like an exception,
        // is issued to the Element rather than the Attr
        // and causes a _second_ DOMSubtreeModified in the Element's
        // tree.
        Vector pv=new Vector(10,10);
        Node p=this,n=p.getParentNode();
        while(n!=null)
        {
            pv.addElement(n);
            p=n;
            n=n.getParentNode();
        }
        
        //CAPTURING_PHASE:
        if(lc.captures>0)
        {
            evt.eventPhase=Event.CAPTURING_PHASE;
            //Ancestors are scanned, root to target, for 
            //Capturing listeners.
            for(int j=pv.size()-1;j>=0;--j)
            {
                if(evt.stopPropagation)
                    break;  // Someone set the flag. Phase ends.
                    
                // Handle all capturing listeners on this node
                NodeImpl nn=(NodeImpl)pv.elementAt(j);
                evt.currentNode=nn;
                if(nn.nodeListeners!=null)
                {
                    Vector nl=(Vector)(nn.nodeListeners.clone());
                    for(int i=nl.size()-1;i>=0;--i) // count-down more efficient
                    {
	                    LEntry le=(LEntry)(nl.elementAt(i));
                        if(le.useCapture && le.type.equals(evt.type))
                            try
                            {
    	                        le.listener.handleEvent(evt);
	                        }
	                        catch(Exception e)
	                        {
	                            // All exceptions are ignored.
	                        }
	                }
	            }
            }
        }
        
        //Both AT_TARGET and BUBBLE use non-capturing listeners.
        if(lc.bubbles>0)
        {
            //AT_TARGET PHASE: Event is dispatched to NON-CAPTURING listeners
            //on the target node. Note that capturing listeners on the target node 
            //are _not_ invoked, even during the capture phase.
            evt.eventPhase=Event.AT_TARGET;
            evt.currentNode=this;
            if(!evt.stopPropagation && nodeListeners!=null)
            {
                Vector nl=(Vector)nodeListeners.clone();
                for(int i=nl.size()-1;i>=0;--i) // count-down is more efficient
                {
                    LEntry le=(LEntry)nl.elementAt(i);
       	            if(le!=null && !le.useCapture && le.type.equals(evt.type))
   	                    try
   	                    {
                            le.listener.handleEvent(evt);
                        }
                        catch(Exception e)
                        {
                            // All exceptions are ignored.
                        }
	            }
            }
            //BUBBLING_PHASE: Ancestors are scanned, target to root, for
            //non-capturing listeners. If the event's preventBubbling flag has
            //been set before processing of a node commences, we instead
            //immediately advance to the default phase.
            //Note that not all events bubble.
            if(evt.bubbles) 
            {
                evt.eventPhase=Event.BUBBLING_PHASE;
                for(int j=0;j<pv.size();++j)
                {
                    if(evt.stopPropagation)
                        break;  // Someone set the flag. Phase ends.
                    
                    // Handle all bubbling listeners on this node
                    NodeImpl nn=(NodeImpl)pv.elementAt(j);
                    evt.currentNode=nn;
                    if(nn.nodeListeners!=null)
                    {
                        Vector nl=(Vector)(nn.nodeListeners.clone());
                        for(int i=nl.size()-1;i>=0;--i) // count-down more efficient
    	                {
	                        LEntry le=(LEntry)(nl.elementAt(i));
    	                    if(!le.useCapture && le.type.equals(evt.type))
            	                try
            	                {
	                                le.listener.handleEvent(evt);
	                            }
	                            catch(Exception e)
	                            {
	                                // All exceptions are ignored.
	                            }
	                    }
	                }
                }
            }
        }
        
        //DEFAULT PHASE: Some DOMs have default behaviors bound to specific
        //nodes. If this DOM does, and if the event's preventDefault flag has
        //not been set, we now return to the target node and process its
        //default handler for this event, if any.
        // No specific phase value defined, since this is DOM-internal
        if(lc.defaults>0 && (!evt.cancelable || !evt.preventDefault))
        {
            // evt.eventPhase=Event.DEFAULT_PHASE;
            // evt.currentNode=this;
            // DO_DEFAULT_OPERATION
        }

        return evt.preventDefault;        
    } // dispatchEvent(Event) :boolean


    /** NON-DOM INTERNAL: DOMNodeInsertedIntoDocument and ...RemovedFrom...
     * are dispatched to an entire subtree. This is the distribution code
     * therefor. They DO NOT bubble, thanks be, but may be captured.
     * <p>
     * ***** At the moment I'm being sloppy and using the normal
     * capture dispatcher on every node. This could be optimized hugely
     * by writing a capture engine that tracks our position in the tree to
     * update the capture chain without repeated chases up to root.
     * @param n node which was directly inserted or removed
     * @param e event to be sent to that node and its subtree
     */
	void dispatchEventToSubtree(NodeImpl n,Event e)
	{
      if(MUTATIONEVENTS)
      {
	    if(nodeListeners==null || n==null)
            return;

	    // ***** Recursive implementation. This is excessively expensive,
	    // and should be replaced in conjunction with optimization
	    // mentioned above.
	    n.dispatchEvent(e);
	    if(n.getNodeType()==Node.ELEMENT_NODE)
	    {
	        NamedNodeMap a=n.getAttributes();
	        for(int i=a.getLength()-1;i>=0;--i)
	            dispatchEventToSubtree(((NodeImpl)a.item(i)),e);
	    }
	    dispatchEventToSubtree(n.firstChild,e);
	    dispatchEventToSubtree(n.nextSibling,e);
	  }
	} // dispatchEventToSubtree(NodeImpl,Event) :void

    /** NON-DOM INTERNAL: Return object for getEnclosingAttr. Carries
     * (two values, the Attr node affected (if any) and its previous 
     * string value. Simple struct, no methods.
     */
	class EnclosingAttr
	{
	    AttrImpl node;
	    String oldvalue;
	} //EnclosingAttr
	
	/** NON-DOM INTERNAL: Pre-mutation context check, in
	 * preparation for later generating DOMAttrModified events.
	 * Determines whether this node is within an Attr
	 * @return either a description of that Attr, or Null
	 * if none such. 
	 */
	EnclosingAttr getEnclosingAttr()
	{
      if(MUTATIONEVENTS)
      {
        NodeImpl eventAncestor=this;
        while(true)
        {
            if(eventAncestor==null)
                return null;
            int type=eventAncestor.getNodeType();
            if(type==Node.ATTRIBUTE_NODE)
            {
                EnclosingAttr retval=new EnclosingAttr();
                retval.node=(AttrImpl)eventAncestor;
                retval.oldvalue=retval.node.getNodeValue();
                return retval;
            }    
            else if(type==Node.ENTITY_REFERENCE_NODE)
                eventAncestor=eventAncestor.parentNode;
            else 
                return null;
                // Any other parent means we're not in an Attr
        }
      }
      return null; // Safety net, should never be reached
	} // getEnclosingAttr() :EnclosingAttr 

	
	/** NON-DOM INTERNAL: Convenience wrapper for calling
	 * dispatchAggregateEvents when the context was established
	 * by <code>getEnclosingAttr</code>.
	 * @param ea description of Attr affected by current operation
	 */
	void dispatchAggregateEvents(EnclosingAttr ea)
	{
	    if(ea!=null)
	        dispatchAggregateEvents(ea.node,ea.oldvalue);
        else
	        dispatchAggregateEvents(null,null);
	        
	} // dispatchAggregateEvents(EnclosingAttr) :void

	/** NON-DOM INTERNAL: Generate the "aggregated" post-mutation events
	 * DOMAttrModified and DOMSubtreeModified.
	 * Both of these should be issued only once for each user-requested
	 * mutation operation, even if that involves multiple changes to
	 * the DOM.
	 * For example, if a DOM operation makes multiple changes to a single
	 * Attr before returning, it would be nice to generate only one 
	 * DOMAttrModified, and multiple changes over larger scope but within
	 * a recognizable single subtree might want to generate only one 
	 * DOMSubtreeModified, sent to their lowest common ancestor. 
	 * <p>
	 * To manage this, use the "internal" versions of insert and remove
	 * with MUTATION_LOCAL, then make an explicit call to this routine
	 * at the higher level. Some examples now exist in our code.
	 *
	 * @param enclosingAttr The Attr node (if any) whose value has
	 * been changed as a result of the DOM operation. Null if none such.
	 * @param oldValue The String value previously held by the
	 * enclosingAttr. Ignored if none such.
	 */
	void dispatchAggregateEvents(AttrImpl enclosingAttr,String oldvalue)
	{
      if(MUTATIONEVENTS)
      {
	    if(nodeListeners==null)
            return;

	    // If we have to send DOMAttrModified (determined earlier),
	    // do so.
	    NodeImpl owner=null;
	    if(enclosingAttr!=null)
	    {
            LCount lc=LCount.lookup(MutationEventImpl.DOM_ATTR_MODIFIED);
	        if(lc.captures+lc.bubbles+lc.defaults>0)
	        {
                owner=((NodeImpl)(enclosingAttr.getOwnerElement()));
                if(owner!=null)
                {
                    MutationEvent me=
                        new MutationEventImpl();
                    //?????ownerDocument.createEvent("MutationEvents");
                    me.initMutationEvent(MutationEventImpl.DOM_ATTR_MODIFIED,true,false,
                       null,oldvalue,enclosingAttr.getNodeValue(),enclosingAttr.getNodeName());
                    owner.dispatchEvent(me);
                }
            }
        }
    
        // DOMSubtreeModified gets sent to the lowest common root of a
        // set of changes. 
        // "This event is dispatched after all other events caused by the
        // mutation have been fired."
        LCount lc=LCount.lookup(MutationEventImpl.DOM_SUBTREE_MODIFIED);
        if(lc.captures+lc.bubbles+lc.defaults>0)
        {
            MutationEvent me=
                    new MutationEventImpl();
                //?????ownerDocument.createEvent("MutationEvents");
            me.initMutationEvent(MutationEventImpl.DOM_SUBTREE_MODIFIED,true,false,
               null,null,null,null);
            
            
            // If we're within an Attr, DStM gets sent to the Attr
            // and to its owningElement. Otherwise we dispatch it
            // locally.
    	    if(enclosingAttr!=null)
    	    {
    	        enclosingAttr.dispatchEvent(me);
    	        if(owner!=null)
    	            owner.dispatchEvent(me);
    	    }
            else
                dispatchEvent(me);
        }
      }
	} //dispatchAggregateEvents(AttrImpl,String) :void


    //
    // Public methods
    //

    /**
     * NON-DOM: PR-DOM-Level-1-19980818 mentions readonly nodes in conjunction
     * with Entities, but provides no API to support this.
     * <P>
     * Most DOM users should not touch this method. Its anticpated use
     * is during construction of EntityRefernces, where it will be used to
     * lock the contents replicated from Entity so they can't be casually
     * altered. It _could_ be published as a DOM extension, if desired.
     *
     * @param readOnly True or false as desired.
     * @param deep If true, children are also toggled. Note that this will
     *	not change the state of an EntityReference or its children,
     *  which are always read-only.
     */
    public void setReadOnly(boolean readOnly, boolean deep) {

        if (syncData) {
            synchronizeData();
        }

    	this.readOnly = readOnly;

    	if (deep) {

            if (syncChildren) {
                synchronizeChildren();
            }

    		// Recursively set kids
    		for (NodeImpl mykid = firstChild;
    			mykid != null;
    			mykid = mykid.nextSibling) {
    			if(!(mykid instanceof EntityReference)) {
    				mykid.setReadOnly(readOnly,true);
                }
            }
        }

    } // setReadOnly(boolean,boolean)

    /**
     * NON-DOM: Returns true if this node is read-only. This is a
     * shallow check.
     */
    public boolean getReadOnly() {

        if (syncData) {
            synchronizeData();
        }

        return readOnly;

    } // getReadOnly():boolean

    /**
     * NON-DOM: As an alternative to subclassing the DOM, this implementation
     * has been extended with the ability to attach an object to each node.
     * (If you need multiple objects, you can attach a collection such as a
     * vector or hashtable, then attach your application information to that.)
     *
     * @returns the previous user object, or null if none.
     */
    public void setUserData(Object data) {
        userData = data;
    }

    /** Returns the user data associated to this node. */
    public Object getUserData() {
        return userData;
    }

    //
    // Protected methods
    //

    /**
     * Override this method in subclass to hook in efficient
     * internal data structure.
     */
    protected void synchronizeChildren() {}

    /**
     * Override this method in subclass to hook in efficient
     * internal data structure.
     */
    protected void synchronizeData() {}

    /** Denotes that this node has changed. */
    protected void changed() {
    	++changes;
    	if (parentNode != null) {
            parentNode.changed();
        }
    }

    //
    // Object methods
    //

    /** NON-DOM method for debugging convenience. */
    public String toString() {
        return "["+getNodeName()+": "+getNodeValue()+"]";
    }

    //
    // Serialization methods
    //

    /** Serialize object. */
    private void writeObject(ObjectOutputStream out) throws IOException {

        // synchronize data and chilren
        if (syncData) {
            synchronizeData();
        }
        if (syncChildren) {
            synchronizeChildren();
        }

        // write object
        out.defaultWriteObject();

    } // writeObject(ObjectOutputStream)

} // class NodeImpl
