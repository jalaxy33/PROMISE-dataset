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

package samples.userguide;

import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.AxisFault;
import org.apache.axis2.wsdl.WSDLConstants;

import javax.xml.namespace.QName;
import java.util.Random;

public class LoadbalanceFailoverClient {

    /**
     * @param args  0: simple | session
     *              1: port
     *              2: iteration
     */
    public static void main(String[] args) {

        String mode = System.getProperty("mode");

        if (mode != null) {
            if (mode.equalsIgnoreCase("session")) {
                new LoadbalanceFailoverClient().sessionfullClient();
            } else if (mode.equalsIgnoreCase("simple") || mode.equalsIgnoreCase("")) {
                new LoadbalanceFailoverClient().sessionlessClient();
            }
        } else {
            // default is simple client
            new LoadbalanceFailoverClient().sessionlessClient();
        }
    }

    private void sessionlessClient() {

        String synapsePort = "8080";
        int iterations = 100;
        boolean infinite = true;

        String pPort = System.getProperty("port");
        String pIterations = System.getProperty("i");

        if (pPort != null) {
            try {
                stringToInt(pPort);
                synapsePort = System.getProperty("port");
            } catch(NumberFormatException e) {
                // run with default value
            }
        }

        if (pIterations != null) {
            try {
                iterations = stringToInt(pIterations);
                if(iterations != -1) {
                    infinite = false;
                }
            } catch(NumberFormatException e) {
                // run with default values
            }
        }

        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMElement value = fac.createOMElement("Value", null);
        value.setText("Sample string");

        Options options = new Options();
        options.setTo(new EndpointReference("http://localhost:" + synapsePort));

        options.setAction("sampleOperation");

        try {
            ConfigurationContext configContext = ConfigurationContextFactory.
                    createConfigurationContextFromFileSystem("client_repo", null);
            ServiceClient client = new ServiceClient(configContext, null);
            options.setTimeOutInMilliSeconds(10000000);

            client.setOptions(options);
            client.engageModule("addressing");

            long i = 0;
            while(i < iterations || infinite) {
                OMElement responseElement = client.sendReceive(value);
                String response = responseElement.getText();

                i++;
                System.out.println("Request: " + i + " ==> " + response);
            }

        } catch (AxisFault axisFault) {
            System.out.println(axisFault.getMessage());
        }
    }

    /**
     * This method creates 3 soap envelopes for 3 different client based sessions. Then it randomly
     * choose one envelope for each iteration and send it to the ESB. ESB should be configured with
     * session affinity load balancer and the SampleClientInitiatedSession dispatcher. This will
     * output request number, session number and the server ID for each iteration. So it can be
     * observed that one session number always associated with one server ID.       
     */
    private void sessionfullClient() {

        String synapsePort = "8080";
        int iterations = 100;
        boolean infinite = true;

        String pPort = System.getProperty("port");
        String pIterations = System.getProperty("i");

        if (pPort != null) {
            try {
                stringToInt(pPort);
                synapsePort = System.getProperty("port");
            } catch(NumberFormatException e) {
                // run with default value
            }
        }

        if (pIterations != null) {
            try {
                iterations = stringToInt(pIterations);
                if(iterations != -1) {
                    infinite = false;
                }
            } catch(NumberFormatException e) {
                // run with default values
            }
        }

        Options options = new Options();
        options.setTo(new EndpointReference("http://localhost:" + synapsePort));
        options.setAction("sampleOperation");
        options.setTimeOutInMilliSeconds(10000000);

        try {

            SOAPEnvelope env1 = buildSoapEnvelope("c1", "v1");
            SOAPEnvelope env2 = buildSoapEnvelope("c2", "v1");
            SOAPEnvelope env3 = buildSoapEnvelope("c3", "v1");
            SOAPEnvelope[] envelopes = {env1, env2, env3};

            ConfigurationContext configContext = ConfigurationContextFactory.
                    createConfigurationContextFromFileSystem("client_repo", null);
            ServiceClient client = new ServiceClient(configContext, null);
            client.setOptions(options);
            client.engageModule("addressing");

            int i = 0;
            int sessionNumber = 0;
            while(i < iterations || infinite) {

                i++;

                MessageContext messageContext = new MessageContext();
                sessionNumber = getSessionTurn(envelopes.length);
                messageContext.setEnvelope(envelopes[sessionNumber]);

                OperationClient op = client.createClient(ServiceClient.ANON_OUT_IN_OP);
                op.addMessageContext(messageContext);
                op.execute(true);

                MessageContext responseContext =
                        op.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                SOAPEnvelope responseEnvelope = responseContext.getEnvelope();

                OMElement vElement =
                        responseEnvelope.getBody().getFirstChildWithName(new QName("Value"));
                System.out.println(
                        "Request: " + i + " Session number: " +
                                sessionNumber + " " + vElement.getText());
            }

        } catch (AxisFault axisFault) {
            System.out.println(axisFault.getMessage());
        }
    }

    private int getSessionTurn(int max) {
        Random random = new Random();
        return random.nextInt(max);
    }

    private SOAPEnvelope buildSoapEnvelope(String clientID, String value) {

        String targetEPR = "http://localhost:9000/soap/Service1";
        String opration = "sampleOperation";

        SOAPFactory soapFactory = OMAbstractFactory.getSOAP12Factory();

        OMNamespace wsaNamespace = soapFactory.
                createOMNamespace("http://www.w3.org/2005/08/addressing", "wsa");

        SOAPEnvelope envelope = soapFactory.createSOAPEnvelope();

        SOAPHeader header = soapFactory.createSOAPHeader();
        envelope.addChild(header);

        OMNamespace synNamespace = soapFactory.
                createOMNamespace("http://ws.apache.org/namespaces/synapse", "syn");
        OMElement clientIDElement = soapFactory.createOMElement("ClientID", synNamespace);
        clientIDElement.setText(clientID);
        header.addChild(clientIDElement);

        SOAPBody body = soapFactory.createSOAPBody();
        envelope.addChild(body);

        OMElement valueElement = soapFactory.createOMElement("Value", null);
        valueElement.setText(value);
        body.addChild(valueElement);

        return envelope;
    }

    private int stringToInt(String stringNumber) {

        int number = new Integer(stringNumber).intValue();

        return number;
    }
}
