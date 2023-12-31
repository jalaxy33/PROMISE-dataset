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

import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.endpoints.WSDLEndpoint;
import org.apache.synapse.Constants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.endpoints.utils.EndpointDefinition;
import org.apache.synapse.config.xml.endpoints.utils.WSDL11EndpointBuilder;
import org.apache.synapse.config.xml.endpoints.utils.WSDL20EndpointBuilder;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.synapse.config.Util;

import javax.xml.namespace.QName;
import java.net.URL;
import java.net.URLConnection;
import java.io.InputStream;
import java.io.IOException;

/**
 * Creates an WSDL based endpoint from a XML configuration.
 *
 * <endpoint [name="name"]>
 *    <suspendDurationOnFailue>suspend-duration</suspendDurationOnFailue>
 *    <wsdl uri="wsdl uri" service="service name" port="port name">
 *       .. extensibility ..
 *    </wsdl>
 * </endpoint>
 */
public class WSDLEndpointFactory implements EndpointFactory {

    private static Log log = LogFactory.getLog(WSDLEndpointFactory.class);

    private static WSDLEndpointFactory instance = new WSDLEndpointFactory();

    private WSDLEndpointFactory() {}

    public static WSDLEndpointFactory getInstance() {
        return instance;
    }

    public Object getObjectFromOMNode(OMNode om) {
        if (om instanceof OMElement) {
            return createEndpoint((OMElement) om, false);
        } else {
            handleException("Invalid XML configuration for an Endpoint. OMElement expected");
        }
        return null;
    }

    public Endpoint createEndpoint(OMElement epConfig, boolean anonymousEndpoint) {

        WSDLEndpoint wsdlEndpoint = new WSDLEndpoint();

        if (!anonymousEndpoint) {
            OMAttribute name = epConfig.getAttribute(new QName(
                    org.apache.synapse.config.xml.Constants.NULL_NAMESPACE, "name"));

            if (name != null) {
                wsdlEndpoint.setName(name.getAttributeValue());
            }
        }

        OMElement wsdlElement = epConfig.getFirstChildWithName
                (new QName(Constants.SYNAPSE_NAMESPACE, "wsdl"));

        if (wsdlElement != null) {

            // set the suspend on fail duration.
            OMElement suspendElement = wsdlElement.getFirstChildWithName(new QName(
                    Constants.SYNAPSE_NAMESPACE,
                    org.apache.synapse.config.xml.Constants.SUSPEND_DURATION_ON_FAILURE));

            if (suspendElement != null) {
                String suspend = suspendElement.getText();

                try {
                    if (suspend != null) {
                        long suspendDuration = Long.parseLong(suspend.trim());
                        wsdlEndpoint.setSuspendOnFailDuration(suspendDuration * 1000);
                    }

                } catch (NumberFormatException e) {
                    handleException("suspendDurationOnFailure should be valid number.");
                }
            }

            EndpointDefinition endpoint = null;

            // get the service name and port name. at this point we should not worry about the presence
            // of those parameters. they are handled by corresponding WSDL builders.
            String serviceName = wsdlElement.getAttributeValue
                    (new QName(org.apache.synapse.config.xml.Constants.NULL_NAMESPACE,"service"));

            String portName = wsdlElement.getAttributeValue
                    (new QName(org.apache.synapse.config.xml.Constants.NULL_NAMESPACE,"port"));

            // check if wsdl is supplied as a URI
            String wsdlURI = wsdlElement.getAttributeValue
                    (new QName(org.apache.synapse.config.xml.Constants.NULL_NAMESPACE,"uri"));

            // set serviceName and portName in the endpoint. it does not matter if these are
            // null at this point. we are setting them only for serialization purpose.
            wsdlEndpoint.setServiceName(serviceName);
            wsdlEndpoint.setPortName(portName);

            if (wsdlURI != null) {
                wsdlEndpoint.setWsdlURI(wsdlURI.trim());

                try {
                    String ns = Util.getOMElementFromURL(new URL(wsdlURI).toString())
                        .getNamespace().getNamespaceURI();

                    if (org.apache.axis2.namespace.Constants.NS_URI_WSDL11.equals(ns)) {
                        endpoint = new WSDL11EndpointBuilder().
                                createEndpointDefinitionFromWSDL(wsdlURI, serviceName, portName);

                    } else if (WSDL2Constants.WSDL_NAMESPACE.equals(ns)) {
                        //endpoint = new WSDL20EndpointBuilder().
                        //        createEndpointDefinitionFromWSDL(wsdlURI, serviceName, portName);

                        handleException("WSDL 2.0 Endpoints are currently not supported");
                    }

                } catch (Exception e) {
                    handleException("Couldn't create endpoint from the given WSDL URI : "
                        + e.getMessage(), e);
                }
            }

            // check if the wsdl 1.1 document is suppled inline
            OMElement definitionElement = wsdlElement.getFirstChildWithName
                    (new QName(org.apache.axis2.namespace.Constants.NS_URI_WSDL11, "definitions"));
            if (endpoint == null && definitionElement != null) {
                wsdlEndpoint.setWsdlDoc(definitionElement);

                endpoint = new WSDL11EndpointBuilder().
                        createEndpointDefinitionFromWSDL(definitionElement, serviceName, portName);
            }

            // check if a wsdl 2.0 document is supplied inline
            OMElement descriptionElement = wsdlElement.getFirstChildWithName
                    (new QName(org.apache.axis2.namespace.Constants.NS_URI_WSDL11, "description"));
            if (endpoint == null && descriptionElement != null) {
                wsdlEndpoint.setWsdlDoc(descriptionElement);
                handleException("WSDL 2.0 Endpoints are currently not supported.");
            }
            if (endpoint != null) {
                // for now, QOS information has to be provided explicitly.
                extractQOSInformation(endpoint, wsdlElement);
                OMAttribute statistics = epConfig.getAttribute(
                        new QName(org.apache.synapse.config.xml.Constants.NULL_NAMESPACE,
                                org.apache.synapse.config.xml.Constants.STATISTICS_ATTRIB_NAME));
                if (statistics != null) {
                    String statisticsValue = statistics.getAttributeValue();
                    if (statisticsValue != null) {
                        if (org.apache.synapse.config.xml.Constants.STATISTICS_ENABLE.equals(
                                statisticsValue)) {
                            endpoint.setStatisticsEnable(org.apache.synapse.Constants.STATISTICS_ON);
                        } else if (org.apache.synapse.config.xml.Constants.STATISTICS_DISABLE.equals(
                                statisticsValue)) {
                            endpoint.setStatisticsEnable(org.apache.synapse.Constants.STATISTICS_OFF);
                        }
                    }
                }
                wsdlEndpoint.setEndpointDefinition(endpoint);
            } else {
                handleException("WSDL is not specified for WSDL endpoint.");
            }
        }

        return wsdlEndpoint;
    }

    private static void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }

    private static void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new SynapseException(msg, e);
    }

    private void extractQOSInformation(EndpointDefinition endpointDefinition, OMElement wsdlElement) {

        OMAttribute format = wsdlElement.getAttribute(new QName(
                org.apache.synapse.config.xml.Constants.NULL_NAMESPACE, "format"));
        OMAttribute optimize = wsdlElement.getAttribute(new QName(
                org.apache.synapse.config.xml.Constants.NULL_NAMESPACE, "optimize"));

        if (format != null)
        {
            String forceValue = format.getAttributeValue().trim().toLowerCase();
            if (forceValue.equals("pox")) {
                endpointDefinition.setForcePOX(true);
            } else if (forceValue.equals("soap")) {
                endpointDefinition.setForceSOAP(true);
            } else {
                handleException("force value -\""+forceValue+"\" not yet implemented");
            }
        }

        if (optimize != null && optimize.getAttributeValue().length() > 0) {
            String method = optimize.getAttributeValue().trim();
            if ("mtom".equalsIgnoreCase(method)) {
                endpointDefinition.setUseMTOM(true);
            } else if ("swa".equalsIgnoreCase(method)) {
                endpointDefinition.setUseSwa(true);
            }
        }

        OMElement wsAddr = wsdlElement.getFirstChildWithName(new QName(
                org.apache.synapse.config.xml.Constants.SYNAPSE_NAMESPACE, "enableAddressing"));
        if (wsAddr != null) {
            endpointDefinition.setAddressingOn(true);
            String useSepList = wsAddr.getAttributeValue(new QName(
                    "separateListener"));
            if (useSepList != null) {
                if (useSepList.trim().toLowerCase().startsWith("tr")
                        || useSepList.trim().startsWith("1")) {
                    endpointDefinition.setUseSeparateListener(true);
                }
            }
        }

        OMElement wsSec = wsdlElement.getFirstChildWithName(new QName(
                org.apache.synapse.config.xml.Constants.SYNAPSE_NAMESPACE, "enableSec"));
        if (wsSec != null) {
            endpointDefinition.setSecurityOn(true);
            OMAttribute policy = wsSec.getAttribute(new QName(
                    org.apache.synapse.config.xml.Constants.NULL_NAMESPACE, "policy"));
            if (policy != null) {
                endpointDefinition.setWsSecPolicyKey(policy.getAttributeValue());
            }
        }
        OMElement wsRm = wsdlElement.getFirstChildWithName(new QName(
                org.apache.synapse.config.xml.Constants.SYNAPSE_NAMESPACE, "enableRM"));
        if (wsRm != null) {
            endpointDefinition.setReliableMessagingOn(true);
            OMAttribute policy = wsRm.getAttribute(new QName(
                    org.apache.synapse.config.xml.Constants.NULL_NAMESPACE, "policy"));
            if (policy != null) {
                endpointDefinition.setWsRMPolicyKey(policy.getAttributeValue());
            }
        }

        // set the timeout configuration
        OMElement timeout = wsdlElement.getFirstChildWithName(new QName(
                org.apache.synapse.config.xml.Constants.SYNAPSE_NAMESPACE, "timeout"));
        if (timeout != null) {
            OMElement duration = timeout.getFirstChildWithName(new QName(
                    org.apache.synapse.config.xml.Constants.SYNAPSE_NAMESPACE, "duration"));
            if (duration != null) {
                String d = duration.getText();
                if (d != null) {
                    long timeoutSeconds = new Long(d.trim()).longValue();
                    endpointDefinition.setTimeoutDuration(timeoutSeconds * 1000);
                }
            }

            OMElement action = timeout.getFirstChildWithName(new QName(
                    org.apache.synapse.config.xml.Constants.SYNAPSE_NAMESPACE, "action"));
            if (action != null) {
                String a = action.getText();
                if (a != null) {
                    if ((a.trim()).equalsIgnoreCase("discard")) {
                        endpointDefinition.setTimeoutAction(Constants.DISCARD);

                        // set timeout duration to 30 seconds, if it is not set explicitly
                        if (endpointDefinition.getTimeoutDuration() == 0) {
                            endpointDefinition.setTimeoutDuration(30000);
                        }
                    } else if ((a.trim()).equalsIgnoreCase("fault")) {
                        endpointDefinition.setTimeoutAction(Constants.DISCARD_AND_FAULT);

                        // set timeout duration to 30 seconds, if it is not set explicitly
                        if (endpointDefinition.getTimeoutDuration() == 0) {
                            endpointDefinition.setTimeoutDuration(30000);
                        }
                    }
                }
            }
        }
    }
}
