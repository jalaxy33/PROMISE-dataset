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
package org.apache.camel.itest.jms;

import edu.emory.mathcs.backport.java.util.concurrent.CountDownLatch;
import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.ContextTestSupport;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;

import static org.apache.camel.component.jms.JmsComponent.jmsComponentClientAcknowledge;
import org.apache.camel.util.jndi.JndiContext;

import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.naming.Context;

/**
 * @version $Revision:520964 $
 */
public class JmsIntegrationTest extends ContextTestSupport {
    protected CountDownLatch receivedCountDown = new CountDownLatch(1);
    protected MyBean myBean = new MyBean();

    public void testOneWayInJmsOutPojo() throws Exception {
        // Send a message to the JMS endpoint
        template.sendBodyAndHeader("jms:test", "Hello", "cheese", 123);

        // The Activated endpoint should send it to the pojo due to the configured route.
        assertTrue("The message ware received by the Pojo", receivedCountDown.await(5, TimeUnit.SECONDS));
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                from("jms:test").to("bean:myBean");
            }
        };
    }

    @Override
    protected Context createJndiContext() throws Exception {
        JndiContext answer = new JndiContext();
        answer.bind("myBean", myBean);

        // add ActiveMQ with embedded broker
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");
        answer.bind("jms", JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));
        return answer;
    }

    protected class MyBean {
        public void onMessage(String body) {
            log.info("Received: " + body);
            receivedCountDown.countDown();
        }
    }

}
