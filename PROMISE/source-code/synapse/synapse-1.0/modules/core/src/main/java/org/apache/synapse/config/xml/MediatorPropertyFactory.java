/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.synapse.config.xml;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.apache.synapse.mediators.MediatorProperty;
import org.jaxen.JaxenException;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A utility class capable of creating instances of MediatorProperty objects by reading
 * through a given XML configuration
 *
 * <pre>
 * &lt;element&gt;
 *    &lt;property name="string" (value="literal" | expression="xpath")/&gt;*
 * &lt;/element&gt;
 * </pre>
 */
public class MediatorPropertyFactory {

    private static final Log log = LogFactory.getLog(MediatorPropertyFactory.class);

    public static List getMediatorProperties(OMElement elem) {

        List propertyList = new ArrayList();

        Iterator iter = elem.getChildrenWithName(new QName(Constants.SYNAPSE_NAMESPACE, "property"));
        while (iter.hasNext()) {

            OMElement propEle = (OMElement) iter.next();
            OMAttribute attName  = propEle.getAttribute(MediatorProperty.ATT_NAME_Q);
            OMAttribute attValue = propEle.getAttribute(MediatorProperty.ATT_VALUE_Q);
            OMAttribute attExpr  = propEle.getAttribute(MediatorProperty.ATT_EXPR_Q);

            MediatorProperty prop = new MediatorProperty();

            if (attName == null || attName.getAttributeValue() == null ||
                attName.getAttributeValue().trim().length() == 0) {
                String msg = "Entry name is a required attribute for a Log property";
                log.error(msg);
                throw new SynapseException(msg);
            } else {
                prop.setName(attName.getAttributeValue());
            }

            // if a value is specified, use it, else look for an expression
            if (attValue != null) {
                if (attValue.getAttributeValue() == null || attValue.getAttributeValue().trim().length() == 0) {
                    String msg = "Entry attribute value (if specified) is required for a Log property";
                    log.error(msg);
                    throw new SynapseException(msg);
                } else {
                    prop.setValue(attValue.getAttributeValue());
                }

            } else if (attExpr != null) {

                if (attExpr.getAttributeValue() == null || attExpr.getAttributeValue().trim().length() == 0) {
                    String msg = "Entry attribute expression (if specified) is required for a mediator property";
                    log.error(msg);
                    throw new SynapseException(msg);

                } else {
                    try {
                        AXIOMXPath xp = new AXIOMXPath(attExpr.getAttributeValue());
                        OMElementUtils.addNameSpaces(xp, propEle, log);
                        prop.setExpression(xp);

                    } catch (JaxenException e) {
                        String msg = "Invalid XPapth expression : " + attExpr.getAttributeValue();
                        log.error(msg);
                        throw new SynapseException(msg, e);
                    }
                }

            } else {
                String msg = "Entry attribute value OR expression must be specified for a mediator property";
                log.error(msg);
                throw new SynapseException(msg);
            }

            propertyList.add(prop);
        }

        return propertyList;
    }
}
