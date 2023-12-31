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
package org.apache.camel.example.bam;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.bam.ActivityBuilder;
import org.apache.camel.bam.ProcessBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.orm.jpa.JpaTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import static org.apache.camel.builder.xml.XPathBuilder.xpath;
import static org.apache.camel.util.Time.seconds;

/**
 * @version $Revision: $
 */
// START SNIPPET: demo
public class MyActivities extends ProcessBuilder {

    public MyActivities(JpaTemplate jpaTemplate, TransactionTemplate transactionTemplate) {
        super(jpaTemplate, transactionTemplate);
    }

    public void configure() throws Exception {

        // lets define some activities, correlating on an XPath on the message bodies
        ActivityBuilder purchaseOrder = activity("file:src/data/purchaseOrders?noop=true")
                .correlate(xpath("/purchaseOrder/@id").stringResult());

        ActivityBuilder invoice = activity("file:src/data/invoices?noop=true")
                .correlate(xpath("/invoice/@purchaseOrderId").stringResult());

        // now lets add some BAM rules
        invoice.starts().after(purchaseOrder.completes())
                .expectWithin(seconds(1))
                .errorIfOver(seconds(2)).to("log:org.apache.camel.example.bam.BamFailures?level=error");
    }
}
// END SNIPPET: demo
