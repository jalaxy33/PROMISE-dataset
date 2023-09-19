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

package org.apache.synapse.config.xml.rest;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.synapse.config.xml.AbstractTestCase;
import org.apache.synapse.rest.API;

public class APISerializationTest extends AbstractTestCase {

    public void testAPISerialization1() throws Exception {
        String xml = "<api name=\"test\" context=\"/dictionary\" xmlns=\"http://ws.apache.org/ns/synapse\">" +
                "<resource url-mapping=\"/admin/view\" inSequence=\"in\" outSequence=\"out\"/></api>";
        OMElement om = AXIOMUtil.stringToOM(xml);
        API api = APIFactory.createAPI(om);
        OMElement out = APISerializer.serializeAPI(api);
        assertXMLEqual(xml, out.toString());
    }

    public void testAPISerialization2() throws Exception {
        String xml = "<api name=\"test\" context=\"/dictionary\" hostname=\"apache.org\" port=\"8243\"" +
                " xmlns=\"http://ws.apache.org/ns/synapse\"><resource url-mapping=\"/admin/view\" " +
                "inSequence=\"in\" outSequence=\"out\"/></api>";
        OMElement om = AXIOMUtil.stringToOM(xml);
        API api = APIFactory.createAPI(om);
        OMElement out = APISerializer.serializeAPI(api);
        assertXMLEqual(xml, out.toString());
    }

    public void testAPISerialization3() throws Exception {
        String xml = "<api name=\"test\" context=\"/dictionary\" hostname=\"apache.org\" port=\"8243\"" +
                " xmlns=\"http://ws.apache.org/ns/synapse\"><resource url-mapping=\"/admin/view\" " +
                "inSequence=\"in\"><outSequence><log/><send/></outSequence></resource></api>";
        OMElement om = AXIOMUtil.stringToOM(xml);
        API api = APIFactory.createAPI(om);
        OMElement out = APISerializer.serializeAPI(api);
        assertXMLEqual(xml, out.toString());
    }

    public void testAPISerialization4() throws Exception {
        String xml = "<api name=\"test\" context=\"/dictionary\" hostname=\"apache.org\" port=\"8243\"" +
                " xmlns=\"http://ws.apache.org/ns/synapse\"><resource url-mapping=\"/admin/view\" " +
                "outSequence=\"out\"><inSequence><log/><send/></inSequence></resource></api>";
        OMElement om = AXIOMUtil.stringToOM(xml);
        API api = APIFactory.createAPI(om);
        OMElement out = APISerializer.serializeAPI(api);
        assertXMLEqual(xml, out.toString());
    }

    public void testAPISerialization5() throws Exception {
        String xml = "<api name=\"test\" context=\"/dictionary\" hostname=\"apache.org\" port=\"8243\"" +
                " xmlns=\"http://ws.apache.org/ns/synapse\"><resource url-mapping=\"/admin/view/*\" " +
                "><inSequence><log/><send/></inSequence><outSequence><log/><send/></outSequence></resource>" +
                "<resource url-mapping=\"/admin/*\"><inSequence><log/><send/></inSequence><outSequence>" +
                "<log/><send/></outSequence></resource><resource uri-template=\"/{char}/{word}\">" +
                "<inSequence><send/></inSequence><faultSequence><log level=\"full\"/></faultSequence>" +
                "</resource></api>";
        OMElement om = AXIOMUtil.stringToOM(xml);
        API api = APIFactory.createAPI(om);
        OMElement out = APISerializer.serializeAPI(api);
        assertXMLEqual(xml, out.toString());
    }

    public void testAPISerialization6() throws Exception {
        String xml = "<api name=\"test\" context=\"/dictionary\" hostname=\"apache.org\" port=\"8243\"" +
                " xmlns=\"http://ws.apache.org/ns/synapse\"><resource url-mapping=\"/admin/view/*\" " +
                "><inSequence><log/><send/></inSequence><outSequence><log/><send/></outSequence></resource>" +
                "<resource url-mapping=\"/admin/*\"><inSequence><log/><send/></inSequence><outSequence>" +
                "<log/><send/></outSequence></resource><resource uri-template=\"/{char}/{word}\">" +
                "<inSequence><send/></inSequence><faultSequence><log level=\"full\"/></faultSequence>" +
                "</resource><handlers><handler class=\"org.apache.synapse.config.xml.rest.TestHandler\"/>" +
                "</handlers></api>";
        OMElement om = AXIOMUtil.stringToOM(xml);
        API api = APIFactory.createAPI(om);
        OMElement out = APISerializer.serializeAPI(api);
        assertXMLEqual(xml, out.toString());
    }

    public void testAPISerialization7() throws Exception {
        String xml = "<api name=\"test\" transport=\"http\" context=\"/dictionary\" xmlns=\"http://ws.apache.org/ns/synapse\">" +
                "<resource url-mapping=\"/admin/view\" inSequence=\"in\" outSequence=\"out\"/></api>";
        OMElement om = AXIOMUtil.stringToOM(xml);
        API api = APIFactory.createAPI(om);
        OMElement out = APISerializer.serializeAPI(api);
        assertXMLEqual(xml, out.toString());
    }

    public void testAPISerialization8() throws Exception {
        String xml = "<api name=\"test\" transport=\"https\" context=\"/dictionary\" hostname=\"apache.org\" port=\"8243\"" +
                " xmlns=\"http://ws.apache.org/ns/synapse\"><resource url-mapping=\"/admin/view/*\" " +
                "><inSequence><log/><send/></inSequence><outSequence><log/><send/></outSequence></resource>" +
                "<resource url-mapping=\"/admin/*\"><inSequence><log/><send/></inSequence><outSequence>" +
                "<log/><send/></outSequence></resource><resource uri-template=\"/{char}/{word}\">" +
                "<inSequence><send/></inSequence><faultSequence><log level=\"full\"/></faultSequence>" +
                "</resource><handlers><handler class=\"org.apache.synapse.config.xml.rest.TestHandler\"/>" +
                "</handlers></api>";
        OMElement om = AXIOMUtil.stringToOM(xml);
        API api = APIFactory.createAPI(om);
        OMElement out = APISerializer.serializeAPI(api);
        assertXMLEqual(xml, out.toString());
    }
}
