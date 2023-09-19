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

package org.apache.synapse.mediators.transform;

import junit.framework.TestCase;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.http.protocol.HTTP;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.config.xml.HeaderMediatorFactory;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.TestUtils;
import org.apache.synapse.util.xpath.SynapseXPath;

import javax.xml.namespace.QName;
import java.util.Map;
import java.util.Properties;

public class HeaderMediatorTest extends TestCase {

    private static final String TEST_HEADER = "http://server/path";

    public void testSimpleHeaderSetAndRemove() throws Exception {

        HeaderMediator headerMediator = new HeaderMediator();
        headerMediator.setQName(new QName(SynapseConstants.HEADER_TO));
        headerMediator.setValue(TEST_HEADER);

        // invoke transformation, with static envelope
        MessageContext synCtx = TestUtils.getTestContext("<empty/>");
        headerMediator.mediate(synCtx);

        assertTrue(TEST_HEADER.equals(synCtx.getTo().getAddress()));

        // set the header mediator as a remove-header
        headerMediator.setAction(HeaderMediator.ACTION_REMOVE);
        headerMediator.mediate(synCtx);

        assertTrue(synCtx.getTo() == null);
    }

    public void testSimpleHeaderXPathSetAndRemove() throws Exception {

        HeaderMediator headerMediator = new HeaderMediator();
        headerMediator.setQName(new QName(SynapseConstants.HEADER_TO));
        headerMediator.setExpression(new SynapseXPath("concat('http://','server','/path')"));

        // invoke transformation, with static enveope
        MessageContext synCtx = TestUtils.getTestContext("<empty/>");
        headerMediator.mediate(synCtx);

        assertTrue(TEST_HEADER.equals(synCtx.getTo().getAddress()));

        // set the header mediator as a remove-header
        headerMediator.setAction(HeaderMediator.ACTION_REMOVE);
        headerMediator.mediate(synCtx);

        assertTrue(synCtx.getTo() == null);
    }

    /**
     * Test that adding a header without namespace triggers an error (SOAP headers MUST be
     * namespace-qualified).
     */
    public void testSetWithNoNamespace() throws Exception {
        HeaderMediator headerMediator = new HeaderMediator();
        headerMediator.setQName(new QName("onlyLocalPart"));
        headerMediator.setValue("value");

        MessageContext synCtx = TestUtils.getTestContext("<empty/>");
        try {
            headerMediator.mediate(synCtx);
            fail("HeaderMediator should not allow headers without namespace");
        } catch (Exception ex) {
            // This is expected
        }
    }

    public void testEmbeddedXml() throws Exception {
        String simpleHeader = "<header name=\"m:simpleHeader\" value=\"Simple Header\" xmlns:m=\"http://org.synapse.example\"/>";
        String complexHeader = "<header><m:complexHeader xmlns:m=\"http://org.synapse.example\"><property key=\"k1\" value=\"v1\"/><property key=\"k2\" value=\"v2\"/></m:complexHeader></header>";
        String removeHeader = "<header name=\"m:complexHeader\" action=\"remove\" xmlns:m=\"http://org.synapse.example\"/>";

        HeaderMediatorFactory fac = new HeaderMediatorFactory();
        // Adding headers.
        MessageContext synCtx = TestUtils.getTestContext("<empty/>");
        HeaderMediator headerMediator = (HeaderMediator) fac.createMediator(
                AXIOMUtil.stringToOM(simpleHeader), new Properties());
        headerMediator.mediate(synCtx);
        OMElement result = synCtx.getEnvelope().getHeader().getFirstElement();
        assertEquals("simpleHeader", result.getLocalName());

        headerMediator = (HeaderMediator) fac.createMediator(
                AXIOMUtil.stringToOM(complexHeader), new Properties());
        headerMediator.mediate(synCtx);
        result = synCtx.getEnvelope().getHeader().getFirstChildWithName(
                new QName("http://org.synapse.example", "complexHeader"));
        assertNotNull(result);

        // Removing headers.
        headerMediator = (HeaderMediator) fac.createMediator(
                AXIOMUtil.stringToOM(removeHeader), new Properties());
        headerMediator.mediate(synCtx);
        result = synCtx.getEnvelope().getHeader().getFirstChildWithName(
                new QName("http://org.synapse.example", "complexHeader"));
        assertNull(result);
    }

    public void testEmbeddedXmlClone() throws Exception {
        // Test for SYNAPSE-977
        String complexHeader = "<header><m:complexHeader xmlns:m=\"http://org.synapse.example\">TEST</m:complexHeader></header>";

        HeaderMediatorFactory fac = new HeaderMediatorFactory();
        HeaderMediator headerMediator = (HeaderMediator) fac.createMediator(
                AXIOMUtil.stringToOM(complexHeader), new Properties());

        // Adding headers.
        MessageContext synCtx = TestUtils.getTestContext("<empty/>");
        headerMediator.mediate(synCtx);
        OMElement result = synCtx.getEnvelope().getHeader().getFirstElement();
        assertEquals("complexHeader", result.getLocalName());
        assertEquals("TEST", result.getText());

        // Now mutate the result
        result.setText("TEST123");

        synCtx = TestUtils.getTestContext("<empty/>");
        headerMediator.mediate(synCtx);
        result = synCtx.getEnvelope().getHeader().getFirstElement();
        assertEquals("complexHeader", result.getLocalName());
        assertEquals("TEST", result.getText());
    }

    public void testTransportHeaderSetAndRemove() throws Exception {

        String SYNAPSE_USER = "SynapseUser";

        HeaderMediator headerMediator = new HeaderMediator();
        headerMediator.setQName(new QName(HTTP.USER_AGENT));
        headerMediator.setValue(SYNAPSE_USER);
        headerMediator.setScope(XMLConfigConstants.HEADER_SCOPE_TRANSPORT);

        // invoke transformation, with static envelope
        MessageContext synCtx = TestUtils.createLightweightSynapseMessageContext("<empty/>");
        headerMediator.mediate(synCtx);

        // get transport header and assert
        org.apache.axis2.context.MessageContext axisCtx =
                ((Axis2MessageContext) synCtx).getAxis2MessageContext();
        Object transportHeaders = axisCtx.getProperty(
                org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        if (transportHeaders == null || !(transportHeaders instanceof Map)) {
            fail("HeaderMediator Transport headers not found");
        } else {
            assertTrue(SYNAPSE_USER.equals(((Map) transportHeaders).get(HTTP.USER_AGENT)));
        }

        // Removing headers
        headerMediator.setAction(HeaderMediator.ACTION_REMOVE);
        headerMediator.mediate(synCtx);

        // get transport header and assert
        org.apache.axis2.context.MessageContext axisCtx2 =
                ((Axis2MessageContext) synCtx).getAxis2MessageContext();
        transportHeaders = axisCtx2.getProperty(
                org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        if (transportHeaders == null || !(transportHeaders instanceof Map)) {
            fail("HeaderMediator Transport headers not found");
        } else {
            assertTrue(((Map)transportHeaders).get(HTTP.USER_AGENT) == null);
        }

    }
}
