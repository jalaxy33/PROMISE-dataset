/*
 * Copyright (c) 2000 World Wide Web Consortium,
 * (Massachusetts Institute of Technology, Institut National de
 * Recherche en Informatique et en Automatique, Keio University). All
 * Rights Reserved. This program is distributed under the W3C's Software
 * Intellectual Property License. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See W3C License http://www.w3.org/Consortium/Legal/ for more
 * details.
 */

package org.w3c.dom.events;

import org.w3c.dom.Node;

/**
 *  The <code>MutationEvent</code> interface provides specific contextual  
 * information associated with Mutation events. 
 * @since DOM Level 2
 */
public interface MutationEvent extends Event {
    /**
     *  <code>relatedNode</code> is used to identify a secondary node related 
     * to a mutation event. For example, if a mutation event is dispatched to 
     * a node indicating that its parent has changed, the 
     * <code>relatedNode</code> is the changed parent.  If an event is instead
     *  dispatch to a subtree indicating a node was changed within it, the 
     * <code>relatedNode</code> is the changed node. 
     */
    public Node getRelatedNode();

    /**
     *  <code>prevValue</code> indicates the previous value of the 
     * <code>Attr</code> node in DOMAttrModified events, and of the 
     * <code>CharacterData</code> node in DOMCharDataModified events. 
     */
    public String getPrevValue();

    /**
     *  <code>newValue</code> indicates the new value of the <code>Attr</code> 
     * node in DOMAttrModified events, and of the <code>CharacterData</code> 
     * node in DOMCharDataModified events. 
     */
    public String getNewValue();

    /**
     *  <code>attrName</code> indicates the name of the changed 
     * <code>Attr</code> node in a DOMAttrModified event. 
     */
    public String getAttrName();

    /**
     *  The <code>initMutationEvent</code> method is used to initialize the 
     * value of a <code>MutationEvent</code> created through the 
     * <code>DocumentEvent</code> interface.  This method may only be called 
     * before the <code>MutationEvent</code> has been dispatched via the 
     * <code>dispatchEvent</code> method, though it may be called multiple 
     * times during that phase if necessary.  If called multiple times, the 
     * final invocation takes precedence.
     * @param typeArg  Specifies the event type.
     * @param canBubbleArg  Specifies whether or not the event can bubble.
     * @param cancelableArg  Specifies whether or not the event's default  
     *   action can be prevented.
     * @param relatedNodeArg  Specifies the <code>Event</code> 's related Node
     * @param prevValueArg  Specifies the <code>Event</code> 's 
     *   <code>prevValue</code> property
     * @param newValueArg  Specifies the <code>Event</code> 's 
     *   <code>newValue</code> property
     * @param attrNameArg  Specifies the <code>Event</code> 's 
     *   <code>attrName</code> property
     */
    public void initMutationEvent(String typeArg, 
                                  boolean canBubbleArg, 
                                  boolean cancelableArg, 
                                  Node relatedNodeArg, 
                                  String prevValueArg, 
                                  String newValueArg, 
                                  String attrNameArg);

}

