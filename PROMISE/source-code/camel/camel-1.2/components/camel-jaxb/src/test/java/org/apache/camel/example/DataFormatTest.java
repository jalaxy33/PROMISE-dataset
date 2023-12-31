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
package org.apache.camel.example;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.spi.DataFormat;

/**
 * @version $Revision: 1.1 $
 */
public class DataFormatTest extends ContextTestSupport {

    public void testMarshalThenUnmarshalBean() throws Exception {
        PurchaseOrder bean = new PurchaseOrder();
        bean.setName("Beer");
        bean.setAmount(23);
        bean.setPrice(2.5);

        MockEndpoint resultEndpoint = resolveMandatoryEndpoint("mock:result", MockEndpoint.class);
        resultEndpoint.expectedBodiesReceived(bean);

        template.sendBody("direct:start", bean);

        resultEndpoint.assertIsSatisfied();
    }


    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {

                DataFormat jaxb = new JaxbDataFormat("org.apache.camel.example");

                from("direct:start").
                        marshal(jaxb).
                        to("direct:marshalled");

                from("direct:marshalled").
                        unmarshal(jaxb).
                        to("mock:result");
            }
        };
    }

}
