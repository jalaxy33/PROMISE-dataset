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

package org.apache.camel.component.cxf;

import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.spring.SpringCamelContext;
import org.apache.camel.spring.SpringTestSupport;
import org.apache.cxf.bus.CXFBusFactory;
import org.apache.cxf.endpoint.ServerImpl;
import org.apache.cxf.frontend.ServerFactoryBean;

public class CXFRouterSpringTest extends CxfRouterTest {
    protected AbstractXmlApplicationContext applicationContext;
    
    @Override
    protected void setUp() throws Exception {
        applicationContext = createApplicationContext();
        assertNotNull("Should have created a valid spring context", applicationContext);

        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (applicationContext != null) {
            applicationContext.destroy();
        }
    }
    
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                from("cxf:bean:routerEndpoint").to("cxf:bean:serviceEndpoint");              
            }
        };
    }
    
    @Override
    protected CamelContext createCamelContext() throws Exception {
        return SpringCamelContext.springCamelContext(applicationContext);
    }
    
    
    protected ClassPathXmlApplicationContext createApplicationContext() {
        return new ClassPathXmlApplicationContext("org/apache/camel/component/cxf/spring/CxfEndpointBeans.xml");
    }

}
