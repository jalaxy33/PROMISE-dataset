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
import org.apache.synapse.mediators.builtin.DropMediator;

import javax.xml.namespace.QName;

/**
 * This creates a drop mediator instance
 *
 * <pre>
 * &lt;drop/&gt;
 * </pre>
 */
public class DropMediatorFactory extends AbstractMediatorFactory  {

    private static final QName DROP_Q = new QName(Constants.SYNAPSE_NAMESPACE, "drop");

    public Mediator createMediator(OMElement el) {

        Mediator dropMediator = new DropMediator();
        // after successfully creating the mediator
        // set its common attributes such as tracing etc
        initMediator(dropMediator,el);

        return dropMediator;
    }

    public QName getTagQName() {
        return DROP_Q;
    }
}
