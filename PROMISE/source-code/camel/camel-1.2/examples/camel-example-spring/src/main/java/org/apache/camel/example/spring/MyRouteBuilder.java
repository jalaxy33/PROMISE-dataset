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
package org.apache.camel.example.spring;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.spring.Main;

/**
 * A simple example router from an ActiveMQ queue to a file system
 *
 * @version $Revision: 1.1 $
 */
public class MyRouteBuilder extends RouteBuilder {
    /**
     * Allow this route to be run as an application
     *
     * @param args
     */
    public static void main(String[] args) {
        new Main().run(args);
    }

    public void configure() {
        // lets populate the message queue with some messages
        from("file:src/data?noop=true").
                to("jms:test.MyQueue");

        from("jms:test.MyQueue").
                to("file://target/test?noop=true");

        // set up a listener on the file component
        from("file://target/test?noop=true").
                bean(new SomeBean());
    }

    public static class SomeBean {

        public void someMethod(String body) {
            System.out.println("Received: " + body);
        }
    }
}
