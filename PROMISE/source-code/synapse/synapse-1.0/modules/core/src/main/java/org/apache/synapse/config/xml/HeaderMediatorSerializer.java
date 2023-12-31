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

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.apache.synapse.Mediator;
import org.apache.synapse.mediators.transform.HeaderMediator;

import javax.xml.namespace.QName;

/**
 * Set header
 *   <pre>
 *      &lt;header name="qname" (value="literal" | expression="xpath")/&gt;
 *   </pre>
 *
 * Remove header
 *   <pre>
 *      &lt;header name="qname" action="remove"/&gt;
 *   </pre>
 */
public class HeaderMediatorSerializer extends AbstractMediatorSerializer
     {
    private static final Log log = LogFactory.getLog(FilterMediatorSerializer.class);

    public OMElement serializeMediator(OMElement parent, Mediator m) {

        if (!(m instanceof HeaderMediator)) {
            handleException("Unsupported mediator passed in for serialization : " + m.getType());
        }

        HeaderMediator mediator = (HeaderMediator) m;
        OMElement header = fac.createOMElement("header", synNS);
        finalizeSerialization(header,mediator);

        QName qName = mediator.getQName();
        if (qName != null) {
            if (qName.getNamespaceURI() != null) {
                header.addAttribute(fac.createOMAttribute(
                    "name", nullNS,
                    (qName.getPrefix() != null && !"".equals(qName.getPrefix())
                        ? qName.getPrefix() + ":" : "") + 
                    qName.getLocalPart()));
                header.declareNamespace(qName.getNamespaceURI(), qName.getPrefix());
            } else {
                header.addAttribute(fac.createOMAttribute(
                    "name", nullNS, qName.getLocalPart()));
            }
        }

        if (mediator.getAction() == HeaderMediator.ACTION_REMOVE) {
            header.addAttribute(fac.createOMAttribute(
                "action", nullNS, "remove"));
        } else {
            if (mediator.getValue() != null) {
                header.addAttribute(fac.createOMAttribute(
                    "value", nullNS, mediator.getValue()));

            } else if (mediator.getExpression() != null) {
                header.addAttribute(fac.createOMAttribute(
                    "expression", nullNS, mediator.getExpression().toString()));
                super.serializeNamespaces(header, mediator.getExpression());

            } else {
                handleException("Value or expression required for a set header mediator");
            }
        }

        if (parent != null) {
            parent.addChild(header);
        }
        return header;
    }

    public String getMediatorClassName() {
        return HeaderMediator.class.getName();
    }

    private void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }

}
