/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.model;

import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.camel.TestSupport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @version $Revision: 1.1 $
 */
public class XmlTestSupport extends TestSupport {
    protected final transient Log log = LogFactory.getLog(getClass());
    protected JAXBContext jaxbContext;

    protected RouteContainer assertParseAsJaxb(String uri) throws JAXBException {
        Object value = parseUri(uri);
        RouteContainer context = assertIsInstanceOf(RouteContainer.class, value);
        log.info("Found: " + context);
        return context;
    }

    protected Object parseUri(String uri) throws JAXBException {
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        URL resource = getClass().getResource(uri);
        assertNotNull("Cannot find resource on the classpath: " + uri, resource);
        Object value = unmarshaller.unmarshal(resource);
        return value;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        jaxbContext = JAXBContext.newInstance("org.apache.camel.model:org.apache.camel.model.config:org.apache.camel.model.dataformat:org.apache.camel.model.language");
    }
}
