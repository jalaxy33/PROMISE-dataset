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
package org.apache.xalan.templates;

import javax.xml.transform.TransformerException;

import org.apache.xalan.extensions.ExtensionNamespaceSupport;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xpath.VariableStack;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XObject;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * <meta name="usage" content="advanced"/>
 * Implement func:function.
 */
public class ElemExsltFunction extends ElemTemplate
{
  
  // A flag indicating whether the return result is set
  private boolean m_isResultSet = false;
  
  // The return result
  private XObject m_result;
  
  // The frame size of the current caller
  private int m_callerFrameSize = 0;
  
  /**
   * Get an integer representation of the element type.
   *
   * @return An integer representation of the element, defined in the
   *     Constants class.
   * @see org.apache.xalan.templates.Constants
   */
  public int getXSLToken()
  {
    return Constants.EXSLT_ELEMNAME_FUNCTION;
  }

   /**
   * Return the node name, defined in the
   *     Constants class.
   * @see org.apache.xalan.templates.Constants.
   * @return The node name
   * 
   */ 
  public String getNodeName()
  {
    return Constants.EXSLT_ELEMNAME_FUNCTION_STRING;
  }
  
  public void execute(TransformerImpl transformer, XObject[] args)
          throws TransformerException
  {
    // Reset the result before starting a function execution.
    m_isResultSet = false;
    m_result = null;
    
    XPathContext xctxt = transformer.getXPathContext();
    VariableStack vars = xctxt.getVarStack();
    
    // Increment the frame bottom of the variable stack by the
    // frame size of the caller template or EXSLT function.
    int oldStackFrame = vars.getStackFrame();
    vars.setStackFrame(m_callerFrameSize + oldStackFrame);
    
    // Set parameters.
    NodeList children = this.getChildNodes();
    int numparams =0;
    for (int i = 0; i < args.length; i ++)
    {
      Node child = children.item(i);
      if (children.item(i) instanceof ElemParam)
      {
        numparams++;
        ElemParam param = (ElemParam)children.item(i);
        vars.setLocalVariable (param.getIndex(), args[i]);
      }
    }
    if (numparams < args.length)
      throw new TransformerException ("function called with too many args");

    //  Removed ElemTemplate 'push' and 'pop' of RTFContext, in order to avoid losing the RTF context 
    //  before a value can be returned. ElemExsltFunction operates in the scope of the template that called 
    //  the function.
    //  xctxt.pushRTFContext();
    
    if (TransformerImpl.S_DEBUG)
      transformer.getTraceManager().fireTraceEvent(this);
    
    transformer.executeChildTemplates(this, true);
    
    // Reset the stack frame after the function call
    vars.setStackFrame(oldStackFrame);
    m_callerFrameSize = 0;

    if (TransformerImpl.S_DEBUG)
      transformer.getTraceManager().fireTraceEndEvent(this);
    
    // Following ElemTemplate 'pop' removed -- see above.
    // xctxt.popRTFContext(); 
    
  }
  
  /**
   * Called after everything else has been
   * recomposed, and allows the function to set remaining
   * values that may be based on some other property that
   * depends on recomposition.
   */
  public void compose(StylesheetRoot sroot) throws TransformerException
  {
    super.compose(sroot);
    
    // Register the function namespace (if not already registered).
    String namespace = getName().getNamespace();
    String handlerClass = "org.apache.xalan.extensions.ExtensionHandlerExsltFunction";    
    Object[] args ={namespace, sroot};
    ExtensionNamespaceSupport extNsSpt = 
                         new ExtensionNamespaceSupport(namespace, handlerClass, args);
    sroot.getExtensionNamespacesManager().registerExtension(extNsSpt);
    // Make sure there is a handler for the EXSLT functions namespace
    // -- for isElementAvailable().    
    if (!(namespace.equals(Constants.S_EXSLT_FUNCTIONS_URL)))
    {
      namespace = Constants.S_EXSLT_FUNCTIONS_URL;
      args = new Object[]{namespace, sroot};
      extNsSpt = new ExtensionNamespaceSupport(namespace, handlerClass, args);
      sroot.getExtensionNamespacesManager().registerExtension(extNsSpt);
    }
  }
  
  /**
   * Return the result of this EXSLT function
   *
   * @return The result of this EXSLT function
   */
  public XObject getResult()
  {
    return m_result;
  }
  
  /**
   * Set the return result of this EXSLT function
   *
   * @param result The return result
   */
  public void setResult(XObject result)
  {
    m_isResultSet = true;
    m_result = result;
  }
  
  /**
   * Return true if the result has been set
   *
   * @return true if the result has been set
   */
  public boolean isResultSet()
  {
    return m_isResultSet;
  }
  
  /**
   * Clear the return result of this EXSLT function
   */
  public void clearResult()
  {
    m_isResultSet = false;
    m_result = null;    
  }
  
  /**
   * Set the frame size of the current caller for use in the variable stack.
   *
   * @param callerFrameSize The frame size of the caller
   */
  public void setCallerFrameSize(int callerFrameSize)
  {
    m_callerFrameSize = callerFrameSize;
  }
}