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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.Mediator;
import org.apache.synapse.SynapseException;
import org.apache.synapse.mediators.MediatorProperty;

import java.util.Collection;
import java.util.Iterator;

public abstract class AbstractMediatorSerializer implements MediatorSerializer {

    protected static final OMFactory fac = OMAbstractFactory.getOMFactory();
    protected static final OMNamespace synNS = fac.createOMNamespace(Constants.SYNAPSE_NAMESPACE, "syn");
    protected static final OMNamespace nullNS = fac.createOMNamespace(Constants.NULL_NAMESPACE, "");
    private static final Log log = LogFactory.getLog(AbstractMediatorSerializer.class);

    /**
     * Perform common functions and finalize the mediator serialization.
     * i.e. process any common attributes
     *
     * @param mediatorOmElement the OMElement being created
     * @param mediator          the Mediator instance being serialized
     */
    protected static void finalizeSerialization(OMElement mediatorOmElement, Mediator mediator) {
        int traceState = mediator.getTraceState();
        String traceValue = null;
        if (traceState == org.apache.synapse.Constants.TRACING_ON) {
            traceValue = Constants.TRACE_ENABLE;
        } else if (traceState == org.apache.synapse.Constants.TRACING_OFF) {
            traceValue = Constants.TRACE_DISABLE;
        }
        if (traceValue != null) {
            mediatorOmElement.addAttribute(fac.createOMAttribute(
                Constants.TRACE_ATTRIB_NAME, nullNS, traceValue));
        }

    }

    public void serializeMediatorProperties(OMElement parent, Collection props) {

        Iterator iter = props.iterator();
        while (iter.hasNext()) {
            MediatorProperty mp = (MediatorProperty) iter.next();
            OMElement prop = fac.createOMElement("property", synNS, parent);
            if (mp.getName() != null) {
                prop.addAttribute(fac.createOMAttribute("name", nullNS, mp.getName()));
            } else {
                handleException("Mediator property name missing");
            }

            if (mp.getValue() != null) {
                prop.addAttribute(fac.createOMAttribute("value", nullNS, mp.getValue()));

            } else if (mp.getExpression() != null) {
                prop.addAttribute(fac.createOMAttribute("expression", nullNS,
                    mp.getExpression().toString()));
                serializeNamespaces(prop, mp.getExpression());

            } else {
                handleException("Mediator property must have a literal value or be an expression");
            }
        }
    }

    public void serializeProperties(OMElement parent, Collection props) {
        serializeMediatorProperties(parent, props);
    }

    public void serializeNamespaces(OMElement elem, AXIOMXPath xpath) {
        Iterator iter = xpath.getNamespaces().keySet().iterator();
        while (iter.hasNext()) {
            String prefix = (String) iter.next();
            String uri = xpath.getNamespaceContext().translateNamespacePrefixToUri(prefix);
            if (!Constants.SYNAPSE_NAMESPACE.equals(uri)) {
                elem.declareNamespace(uri, prefix);
            }
        }
    }

    private static void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }

}
