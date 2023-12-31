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

package org.apache.synapse.config.xml.endpoints.utils;

import org.apache.synapse.endpoints.utils.EndpointDefinition;
import org.apache.synapse.SynapseException;
import org.apache.axiom.om.OMElement;
import org.apache.woden.WSDLFactory;
import org.apache.woden.WSDLReader;
import org.apache.woden.WSDLException;
import org.apache.woden.types.NCName;
import org.apache.woden.wsdl20.xml.DescriptionElement;
import org.apache.woden.wsdl20.Description;
import org.apache.woden.wsdl20.Service;
import org.apache.woden.wsdl20.Endpoint;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;

/**
 * Currently this class is not used as woden is dependent on xerces, which is not included in the
 * current release.
 *  
 * Builder for WSDL 2.0 endpoints. This class extracts endpoint information from the given WSDL 2.0
 * documents.
 */
public class WSDL20EndpointBuilder {

/*  COMMENT DUE TO BUILD FAILURE - TO BE FIXED LATER WHEN WSDL 2.0 SUPPORT IS OFFICIALLY IN 
    private static Log log = LogFactory.getLog(WSDL20EndpointBuilder.class);

    public EndpointDefinition createEndpointDefinitionFromWSDL
            (String wsdlURI, String serviceName, String portName) {

        try {
            WSDLFactory fac = WSDLFactory.newInstance();
            WSDLReader reader = fac.newWSDLReader();
            reader.setFeature(WSDLReader.FEATURE_VALIDATION, false);

            DescriptionElement descriptionElement = reader.readWSDL(wsdlURI);
            return createEndpointDefinitionFromWSDL(descriptionElement, serviceName, portName);

        } catch (WSDLException e) {
            handleException("Couldn't process the given WSDL document.");
        }

        return null;
    }

    private EndpointDefinition createEndpointDefinitionFromWSDL
            (DescriptionElement dElement, String serviceName, String portName) {

        if (dElement == null) {
            throw new SynapseException("WSDL is not specified.");
        }

        if (serviceName == null) {
            throw new SynapseException("Service is not specified.");
        }

        if (portName == null) {
            throw new SynapseException("Port is not specified.");
        }

        Description description = dElement.toComponent();
        String tns = dElement.getTargetNamespace().toString();
        Service service = description.getService(new QName(tns, serviceName));
        if (service != null) {
            Endpoint wsdlEndpoint = service.getEndpoint(new NCName(portName));
            if (wsdlEndpoint != null) {
                String serviceURL = wsdlEndpoint.getAddress().toString();

                EndpointDefinition endpointDefinition = new EndpointDefinition();
                endpointDefinition.setAddress(serviceURL);

                return endpointDefinition;
            } else {
                handleException("Specified port is not defined in the given WSDL.");
            }
        } else {
            handleException("Specified service is not defined in the given WSDL.");
        }

        return null;
    }

    private static void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }*/
}
