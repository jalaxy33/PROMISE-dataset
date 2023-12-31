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
package org.apache.camel.impl;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.PollingConsumer;
import org.apache.camel.Processor;
import org.apache.camel.RuntimeExchangeException;
import org.apache.camel.util.ServiceHelper;

/**
 * A simple implementation of {@link PollingConsumer} which just uses
 * a {@link Processor}. This implementation does not support timeout based
 * receive methods such as {@link #receive(long)}
 *
 * @version $Revision: 1.1 $
 */
public class ProcessorPollingConsumer extends PollingConsumerSupport {
    private Processor processor;

    public ProcessorPollingConsumer(Endpoint endpoint, Processor processor) {
        super(endpoint);
        this.processor = processor;
    }

    protected void doStart() throws Exception {
        ServiceHelper.startService(processor);
    }

    protected void doStop() throws Exception {
        ServiceHelper.stopService(processor);
    }

    public Exchange receive() {
        Exchange exchange = getEndpoint().createExchange();
        try {
            processor.process(exchange);
        }
        catch (Exception e) {
            throw new RuntimeExchangeException(e, exchange);
        }
        return exchange;
    }

    public Exchange receiveNoWait() {
        return receive();
    }

    public Exchange receive(long timeout) {
        return receive();
    }
}
