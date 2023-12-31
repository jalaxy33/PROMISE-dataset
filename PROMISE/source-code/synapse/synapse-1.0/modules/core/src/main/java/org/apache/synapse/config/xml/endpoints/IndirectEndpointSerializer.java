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

package org.apache.synapse.config.xml.endpoints;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMFactory;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.endpoints.IndirectEndpoint;
import org.apache.synapse.SynapseException;
import org.apache.synapse.Constants;

/**
 * Serializes an IndirectEndpoint to an XML configuration.
 *
 * <endpoint key="key" />
 */
public class IndirectEndpointSerializer implements EndpointSerializer {

    private OMFactory fac = null;

    public OMElement serializeEndpoint(Endpoint endpoint) {

        if (!(endpoint instanceof IndirectEndpoint)) {
            throw new SynapseException("Invalid endpoint type.");
        }

        fac = OMAbstractFactory.getOMFactory();
        OMElement endpointElement = fac.createOMElement("endpoint", Constants.SYNAPSE_OMNAMESPACE);

        IndirectEndpoint indirectEndpoint = (IndirectEndpoint) endpoint;
        String ref = indirectEndpoint.getKey();
        if (ref != null) {
            endpointElement.addAttribute("key", ref, null);
        }

        return endpointElement;
    }
}
