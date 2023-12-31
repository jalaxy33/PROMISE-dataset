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

import java.io.StringWriter;
import java.util.List;

import javax.xml.bind.Marshaller;

import org.apache.camel.model.language.GroovyExpression;

/**
 * @version $Revision: 1.1 $
 */
public class GenerateXmlTest extends XmlTestSupport {
    public void testCreateSimpleXml() throws Exception {
        RoutesType context = new RoutesType();
        RouteType route = context.route();
        route.from("seda:a");
        route.filter("juel", "in.header.foo == 'bar'").
                to("seda:b");

        dump(context);
    }

    public void testGroovyFilterXml() throws Exception {
        RoutesType context = new RoutesType();
        RouteType route = context.route();
        route.from("seda:a");
        route.interceptors("interceptor1", "interceptor2");
        route.filter(new GroovyExpression("in.headers.any { h -> h.startsWith('foo') }")).
                to("seda:b");

        List<ProcessorType> list = route.getOutputs();
        assertEquals("Size of list: " + list, 1, list.size());

        dump(context);
    }

    protected void dump(RouteContainer context) throws Exception {
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        StringWriter buffer = new StringWriter();
        marshaller.marshal(context, buffer);
        log.info("Created: " + buffer);
    }
}
