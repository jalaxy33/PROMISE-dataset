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
package org.apache.camel.processor;

import java.util.List;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;

public class StreamResequencerTest extends ContextTestSupport {

    protected MockEndpoint resultEndpoint;

    public void sendBodyAndHeader(String endpointUri, final Object body, 
            final String headerName, final Object headerValue) {
        template.send(endpointUri, new Processor() {
            public void process(Exchange exchange) {
                Message in = exchange.getIn();
                in.setBody(body);
                in.setHeader(headerName, headerValue);
                in.setHeader("testCase", getName());
            }
        });        
    }
    
    public void testSendMessagesInWrongOrderButReceiveThemInCorrectOrder() throws Exception {
        resultEndpoint.expectedBodiesReceived("msg1", "msg2", "msg3", "msg4");
        sendBodyAndHeader("direct:start", "msg4", "seqnum", 4L);
        sendBodyAndHeader("direct:start", "msg1", "seqnum", 1L);
        sendBodyAndHeader("direct:start", "msg3", "seqnum", 3L);
        sendBodyAndHeader("direct:start", "msg2", "seqnum", 2L);
        resultEndpoint.assertIsSatisfied();
        List<Exchange> list = resultEndpoint.getReceivedExchanges();
        for (Exchange exchange : list) {
            log.debug("Received: " + exchange);
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        resultEndpoint = getMockEndpoint("mock:result");
    }

    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                // START SNIPPET: example
                from("direct:start").resequencer(header("seqnum")).stream().to("mock:result");
                // END SNIPPET: example
            }
        };
    }
}
