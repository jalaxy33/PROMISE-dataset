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
 * 4. The names "Xalan" and "Apache Software Foundation" must
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
 * originally based on software copyright (c) 1999, Lotus
 * Development Corporation., http://www.lotus.com.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.xalan.processor;

import org.apache.xalan.templates.ElemLiteralResult;
import org.apache.xalan.templates.ElemElement;
import org.apache.xalan.templates.ElemTemplateElement;
import org.apache.xalan.templates.Stylesheet;
import org.apache.xalan.templates.ElemExtensionCall;
import org.apache.xalan.templates.ElemTemplate;
import org.apache.xalan.templates.ElemExsltFunction;
import org.apache.xalan.templates.ElemExsltFuncResult;
import org.apache.xalan.templates.ElemFallback;
import org.apache.xalan.templates.ElemVariable;
import org.apache.xalan.templates.ElemParam;
import org.apache.xalan.templates.ElemValueOf;
import org.apache.xalan.templates.Constants;
import org.apache.xpath.XPath;
import org.apache.xalan.templates.StylesheetRoot;

import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.apache.xalan.res.XSLTErrorResources;


/**
 * <meta name="usage" content="internal"/>
 * This class processes parse events for an exslt func:function element.
 */
public class ProcessorExsltFunction extends ProcessorTemplateElem
{
  /**
   * Start an ElemExsltFunction. Verify that it is top level and that it has a name attribute with a
   * namespace.
   */
  public void startElement(
          StylesheetHandler handler, String uri, String localName, String rawName, Attributes attributes)
            throws SAXException
  {
    //System.out.println("ProcessorFunction.startElement()");
    String msg = "";
    if (!(handler.getElemTemplateElement() instanceof StylesheetRoot))
    {
      msg = "func:function element must be top level.";
      handler.error(msg, new SAXException(msg));
    }
    super.startElement(handler, uri, localName, rawName, attributes);
       
    String val = attributes.getValue("name");
    int indexOfColon = val.indexOf(":");
    if (indexOfColon > 0)
    {
      String prefix = val.substring(0, indexOfColon);
      String localVal = val.substring(indexOfColon + 1);
      String ns = handler.getNamespaceSupport().getURI(prefix);
      //if (ns.length() > 0)
      //  System.out.println("fullfuncname " + ns + localVal);
    }
    else
    {
      msg = "func:function name must have namespace";
      handler.error(msg, new SAXException(msg));
    }
  }
  
  /**
   * Must include; super doesn't suffice!
   */
  protected void appendAndPush(
          StylesheetHandler handler, ElemTemplateElement elem)
            throws SAXException
  {
    //System.out.println("ProcessorFunction appendAndPush()" + elem);
    super.appendAndPush(handler, elem);
    //System.out.println("originating node " + handler.getOriginatingNode());
    elem.setDOMBackPointer(handler.getOriginatingNode());
    handler.getStylesheet().setTemplate((ElemTemplate) elem);
  }
    
  /**
   * End an ElemExsltFunction, and verify its validity.
   */
  public void endElement(
          StylesheetHandler handler, String uri, String localName, String rawName)
            throws SAXException
  {
   ElemTemplateElement function = handler.getElemTemplateElement();
   SourceLocator locator = handler.getLocator();
   validate(function, handler); // may throw exception
   super.endElement(handler, uri, localName, rawName);   
  }
  
  /**
   * Non-recursive traversal of FunctionElement tree based on TreeWalker to verify that
   * there are no literal result elements except within a func:result element and that
   * the func:result element does not contain any following siblings except xsl:fallback.
   */
  public void validate(ElemTemplateElement elem, StylesheetHandler handler)
    throws SAXException
  {
    String msg = "";
    while (elem != null)
    { 
      //System.out.println("elem " + elem);
      if (elem instanceof ElemExsltFuncResult 
          && elem.getNextSiblingElem() != null 
          && !(elem.getNextSiblingElem() instanceof ElemFallback))
      {
        msg = "func:result has an illegal following sibling (only xsl:fallback allowed)";
        handler.error(msg, new SAXException(msg));
      }
      if (elem instanceof ElemValueOf ||
          (elem instanceof ElemLiteralResult || elem instanceof ElemElement)
          && !(ancestorIsOk(elem)))
      {
        msg ="misplaced literal result in a func:function container.";
        handler.error(msg, new SAXException(msg));
      }
      ElemTemplateElement nextElem = elem.getFirstChildElem();
      while (nextElem == null)
      {
        nextElem = elem.getNextSiblingElem();
        if (nextElem == null)
          elem = elem.getParentElem();
        if (elem == null || elem instanceof ElemExsltFunction)
          return; // ok
      }  
      elem = nextElem;
    }
  }
  
  /**
   * Verify that a literal result belongs to a result element, a variable, 
   * or a parameter.
   */
  
  boolean ancestorIsOk(ElemTemplateElement child)
  {
    while (child.getParentElem() != null && !(child.getParentElem() instanceof ElemExsltFunction))
    {
      ElemTemplateElement parent = child.getParentElem();
      if (parent instanceof ElemExsltFuncResult 
          || parent instanceof ElemVariable
          || parent instanceof ElemParam)
        return true;
      child = parent;      
    }
    return false;
  }
  
}