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

import org.apache.camel.Exchange;
import org.apache.camel.Endpoint;
import org.apache.camel.Component;
import org.apache.camel.Consumer;
import org.apache.camel.util.IntrospectionSupport;

import java.util.Map;

/**
 * A base class for {@link Endpoint} which creates a {@link ScheduledPollConsumer}
 *
 * @version $Revision: 1.1 $
 */
public abstract class ScheduledPollEndpoint<E extends Exchange> extends DefaultEndpoint<E> {
    private Map consumerProperties;

    protected ScheduledPollEndpoint(String endpointUri, Component component) {
        super(endpointUri, component);
    }

    public Map getConsumerProperties() {
        return consumerProperties;
    }

    public void setConsumerProperties(Map consumerProperties) {
        this.consumerProperties = consumerProperties;
    }

    protected void configureConsumer(Consumer<E> consumer) {
        if (consumerProperties != null) {
            IntrospectionSupport.setProperties(consumer, consumerProperties);
        }
    }

    public void configureProperties(Map options) {
        Map consumerProperties = IntrospectionSupport.extractProperties(options, "consumer.");
        if (consumerProperties != null) {
            setConsumerProperties(consumerProperties);
        }
    }

}
