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
import org.apache.synapse.Mediator;
import org.apache.synapse.SynapseException;
import org.apache.synapse.config.xml.endpoints.EndpointFactory;
import org.apache.synapse.config.xml.endpoints.EndpointAbstractFactory;
import org.apache.synapse.mediators.builtin.SendMediator;
import org.apache.synapse.endpoints.Endpoint;

import javax.xml.namespace.QName;

/**
 * The Send mediator factory parses a Send element and creates an instance of the mediator
 *
 * //TODO document endpoints, failover and loadbalacing
 *
 * The &lt;send&gt; element is used to send messages out of Synapse to some endpoint. In the simplest case,
 * the place to send the message to is implicit in the message (via a property of the message itself)-
 * that is indicated by the following
 * <pre>
 *  &lt;send/&gt;
 * </pre>
 *
 * If the message is to be sent to one or more endpoints, then the following is used:
 * <pre>
 *  &lt;send&gt;
 *   (endpointref | endpoint)+
 *  &lt;/send&gt;
 * </pre>
 * where the endpointref token refers to the following:
 * <pre>
 * &lt;endpoint ref="name"/&gt;
 * </pre>
 * and the endpoint token refers to an anonymous endpoint defined inline:
 * <pre>
 *  &lt;endpoint address="url"/&gt;
 * </pre>
 * If the message is to be sent to an endpoint selected by load balancing across a set of endpoints,
 * then it is indicated by the following:
 * <pre>
 * &lt;send&gt;
 *   &lt;load-balance algorithm="uri"&gt;
 *     (endpointref | endpoint)+
 *   &lt;/load-balance&gt;
 * &lt;/send&gt;
 * </pre>
 * Similarly, if the message is to be sent to an endpoint with failover semantics, then it is indicated by the following:
 * <pre>
 * &lt;send&gt;
 *   &lt;failover&gt;
 *     (endpointref | endpoint)+
 *   &lt;/failover&gt;
 * &lt;/send&gt;
 * </pre>
 */
public class SendMediatorFactory extends AbstractMediatorFactory  {

    private static final Log log = LogFactory.getLog(SendMediatorFactory.class);

    private static final QName SEND_Q = new QName(Constants.SYNAPSE_NAMESPACE, "send");

    public Mediator createMediator(OMElement elem) {

        SendMediator sm =  new SendMediator();

        // after successfully creating the mediator
        // set its common attributes such as tracing etc
        initMediator(sm,elem);

        OMElement epElement = elem.getFirstChildWithName(new QName(Constants.SYNAPSE_NAMESPACE, "endpoint"));
        if (epElement != null) {
            // get the factory for the element
            // create the endpoint and set it in the send medaitor

            EndpointFactory fac = EndpointAbstractFactory.getEndpointFactroy(epElement);
            if (fac != null) {
                Endpoint endpoint = fac.createEndpoint(epElement, true);
                if (endpoint != null) {
                    sm.setEndpoint(endpoint);
                }
            } else {
                throw new SynapseException("Invalid endpoint fromat.");
            }
        }

        return sm;
    }

    public QName getTagQName() {
        return SEND_Q;
    }
}
