/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.synapse.config.xml;

import org.apache.axiom.om.OMElement;
import org.apache.synapse.Mediator;
import org.apache.synapse.mediators.filters.InMediator;

import javax.xml.namespace.QName;

/**
 * Creates an In mediator instance
 *
 * <pre>
 * &lt;in&gt;
 *    mediator+
 * &lt;/in&gt;
 * </pre>
 */
public class InMediatorFactory extends AbstractListMediatorFactory {

    private static final QName IN_Q = new QName(Constants.SYNAPSE_NAMESPACE, "in");

    public Mediator createMediator(OMElement elem) {
        InMediator filter = new InMediator();
        // after successfully creating the mediator
        // set its common attributes such as tracing etc
        initMediator(filter,elem);
        addChildren(elem, filter);
        return filter;
    }

    public QName getTagQName() {
        return IN_Q;
    }
}