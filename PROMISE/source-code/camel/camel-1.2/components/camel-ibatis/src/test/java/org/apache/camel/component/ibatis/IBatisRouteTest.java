/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.ibatis;

import java.util.List;
import java.sql.Statement;
import java.sql.Connection;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.Exchange;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.builder.RouteBuilder;

/**
 * @version $Revision: 1.1 $
 */
public class IBatisRouteTest extends ContextTestSupport {
    public void testSendAccountBean() throws Exception {
        MockEndpoint endpoint = getMockEndpoint("mock:results");
        endpoint.expectedMinimumMessageCount(1);

        Account account = new Account();
        account.setId(123);
        account.setFirstName("James");
        account.setLastName("Strachan");
        account.setEmailAddress("TryGuessing@gmail.com");

        template.sendBody("direct:start", account);

        assertMockEndpointsSatisifed();

        List<Exchange> list = endpoint.getReceivedExchanges();
        Exchange exchange = list.get(list.size() - 1);
        List body = exchange.getIn().getBody(List.class);
        assertNotNull("Should have returned a List!", body);
        assertEquals("Wrong size: " + body, 1, body.size());
        Account actual = assertIsInstanceOf(Account.class, body.get(0));

        assertEquals("Account.getFirstName()", "James", actual.getFirstName());
        assertEquals("Account.getLastName()", "Strachan", actual.getLastName());

        log.info("Found: " + actual);
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() throws Exception {
                from("ibatis:selectAllAccounts").to("mock:results");

                from("direct:start").to("ibatis:insertAccount");
            }
        };
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // lets create the database...
        IBatisEndpoint endpoint = resolveMandatoryEndpoint("ibatis:Account", IBatisEndpoint.class);
        Connection connection = endpoint.getSqlClient().getDataSource().getConnection();
        Statement statement = connection.createStatement();
        statement.execute("create table ACCOUNT ( ACC_ID INTEGER , ACC_FIRST_NAME VARCHAR(255), ACC_LAST_NAME VARCHAR(255), ACC_EMAIL VARCHAR(255)  )");
        connection.close();
    }
}
