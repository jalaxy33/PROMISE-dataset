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
import org.apache.synapse.SynapseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is Serializer  for serialization of an anonymous list mediator(an unnamed list of mediators )
 */

public abstract class AnonymousListMediatorSerializer extends AbstractListMediatorSerializer {

    private static final Log log = LogFactory.getLog(AnonymousListMediatorSerializer.class);

    /**
     * To serialize an  anonymous list mediator
     *
     * @param parent
     * @param m
     * @return OMElement
     */
    public static OMElement serializeAnonymousListMediator(OMElement parent, Mediator m) {
        if (!(m instanceof AnonymousListMediator)) {
            handleException("Unsupported mediator passed in for serialization : " + m.getType());
        }
        AnonymousListMediator mediator = (AnonymousListMediator) m;
        serializeChildren(parent, mediator.getList());
        return parent;
    }

    private static void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }
}
