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
package org.apache.xpath.functions;

import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.DTMIterator;
import org.apache.xpath.XPathContext;
import org.apache.xpath.axes.LocPathIterator;
import org.apache.xpath.axes.PredicatedNodeTest;
import org.apache.xpath.objects.XNodeSet;
import org.apache.xpath.objects.XObject;

/**
 * <meta name="usage" content="advanced"/>
 * Execute the current() function.
 */
public class FuncCurrent extends Function
{

  /**
   * Execute the function.  The function must return
   * a valid object.
   * @param xctxt The current execution context.
   * @return A valid XObject.
   *
   * @throws javax.xml.transform.TransformerException
   */
  public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException
  {
    // If we're in a predicate, then this will return non-null.
    Object subContextList = xctxt.getSubContextList();
    int currentNode;

    // %TBD% Hack city...
    if (null != subContextList && subContextList instanceof PredicatedNodeTest)
    {
      // PredicatedNodeTest iter = (PredicatedNodeTest) xctxt.getSubContextList();
      // LocPathIterator lpi = iter.getLocPathIterator();
      LocPathIterator lpi = xctxt.getCurrentNodeList();

      currentNode = lpi.getCurrentContextNode();  
        
    }
    else if(xctxt.getIteratorRoot() != DTM.NULL)
    {
      currentNode = xctxt.getIteratorRoot();
    }
    else
    {
      DTMIterator cnl = xctxt.getContextNodeList();

      if (null != cnl)
      {
        // %REVIEW% Not so certain that this is doing the right thing?
        currentNode = cnl.getCurrentNode();
      }
      else
        currentNode = DTM.NULL;
    }
    //  if(DTM.NULL != currentNode)
    //  {
    //    DTM dtm = xctxt.getDTM(currentNode);
    //    System.err.println("current node: "+dtm.getNodeName(currentNode)+"["+Integer.toHexString(currentNode)+"]");
    //  }
    //  else
    //  {
    //     System.err.println("current node: DTM.NULL");
    //  }

    return new XNodeSet(currentNode, xctxt.getDTMManager());
  }
  
  /**
   * No arguments to process, so this does nothing.
   */
  public void fixupVariables(java.util.Vector vars, int globalsSize)
  {
    // no-op
  }

}
