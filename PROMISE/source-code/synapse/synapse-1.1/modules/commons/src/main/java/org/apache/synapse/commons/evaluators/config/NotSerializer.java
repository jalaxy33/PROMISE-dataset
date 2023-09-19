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

package org.apache.synapse.commons.evaluators.config;

import org.apache.axiom.om.OMElement;
import org.apache.synapse.commons.evaluators.Evaluator;
import org.apache.synapse.commons.evaluators.EvaluatorException;
import org.apache.synapse.commons.evaluators.NotEvaluator;
import org.apache.synapse.commons.evaluators.EvaluatorConstants;

import javax.xml.namespace.QName;

/**
 * Serialize the {@link NotEvaluator} to the XML configuration defined in
 * the {@link NotFactory}. 
 */
public class NotSerializer extends AbstractEvaluatorSerializer {

    public OMElement serialize(OMElement parent, Evaluator evaluator) throws EvaluatorException {
        if (!(evaluator instanceof NotEvaluator)) {
            throw new IllegalArgumentException("Evaluator should be a NotEvaluator");
        }

        NotEvaluator notEvaluator = (NotEvaluator) evaluator;

        OMElement notElement  = fac.createOMElement(EvaluatorConstants.NOT,
                EvaluatorConstants.SYNAPSE_NAMESPACE, EvaluatorConstants.EMPTY_PREFIX);
        serializeChild(notElement, notEvaluator.getEvaluator());

        if (parent != null) {
            parent.addChild(notElement);
        }
        
        return notElement;
    }
}
