/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999-2003 The Apache Software Foundation.  All rights 
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

import javax.xml.transform.TransformerException;

import org.apache.xalan.templates.ElemAttributeSet;
import org.apache.xalan.templates.ElemTemplateElement;

import org.xml.sax.Attributes;

/**
 * This class processes parse events for an xsl:attribute-set.
 * @see <a href="http://www.w3.org/TR/xslt#dtd">XSLT DTD</a>
 * @see <a href="http://www.w3.org/TR/xslt#attribute-sets">attribute-sets in XSLT Specification</a>
 */
class ProcessorAttributeSet extends XSLTElementProcessor
{

  /**
   * Receive notification of the start of an xsl:attribute-set element.
   *
   * @param handler The calling StylesheetHandler/TemplatesBuilder.
   * @param uri The Namespace URI, or the empty string if the
   *        element has no Namespace URI or if Namespace
   *        processing is not being performed.
   * @param localName The local name (without prefix), or the
   *        empty string if Namespace processing is not being
   *        performed.
   * @param rawName The raw XML 1.0 name (with prefix), or the
   *        empty string if raw names are not available.
   * @param attributes The attributes attached to the element.  If
   *        there are no attributes, it shall be an empty
   *        Attributes object.
   * 
   * @see org.apache.xalan.processor.StylesheetHandler#startElement
   * @see org.xml.sax.ContentHandler#startElement
   * @see org.xml.sax.ContentHandler#endElement
   * @see org.xml.sax.Attributes
   */
  public void startElement(
          StylesheetHandler handler, String uri, String localName, String rawName, Attributes attributes)
            throws org.xml.sax.SAXException
  {

    ElemAttributeSet eat = new ElemAttributeSet();

    eat.setLocaterInfo(handler.getLocator());
    try
    {
      eat.setPrefixes(handler.getNamespaceSupport());
    }
    catch(TransformerException te)
    {
      throw new org.xml.sax.SAXException(te);
    }

    eat.setDOMBackPointer(handler.getOriginatingNode());
    setPropertiesFromAttributes(handler, rawName, attributes, eat);
    handler.getStylesheet().setAttributeSet(eat);

    // handler.pushElemTemplateElement(eat);
    ElemTemplateElement parent = handler.getElemTemplateElement();

    parent.appendChild(eat);
    handler.pushElemTemplateElement(eat);
  }

  /**
   * Receive notification of the end of an element.
   *
   * @param name The element type name.
   * @param attributes The specified or defaulted attributes.
   *
   * @param handler non-null reference to current StylesheetHandler that is constructing the Templates.
   * @param uri The Namespace URI, or an empty string.
   * @param localName The local name (without prefix), or empty string if not namespace processing.
   * @param rawName The qualified name (with prefix).
   */
  public void endElement(
          StylesheetHandler handler, String uri, String localName, String rawName)
            throws org.xml.sax.SAXException
  {
    handler.popElemTemplateElement();
  }
}
