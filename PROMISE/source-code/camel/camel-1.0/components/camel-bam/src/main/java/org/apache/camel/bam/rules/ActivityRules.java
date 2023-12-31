/*
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
package org.apache.camel.bam.rules;

import org.apache.camel.Exchange;
import org.apache.camel.bam.ProcessBuilder;
import org.apache.camel.bam.model.ActivityDefinition;
import org.apache.camel.bam.model.ActivityState;
import org.apache.camel.bam.model.ProcessInstance;
import org.apache.camel.impl.ServiceSupport;
import org.apache.camel.util.ServiceHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a activity which is typically a system or could be an endpoint
 *
 * @version $Revision: $
 */
public class ActivityRules extends ServiceSupport {
    private static final transient Log log = LogFactory.getLog(ActivityRules.class);
    private int expectedMessages = 1;
    private ProcessRules processRules;
    private List<TemporalRule> rules = new ArrayList<TemporalRule>();
    private ActivityDefinition activityDefinition;
    private String activityName;
    private final org.apache.camel.bam.ProcessBuilder builder;

    public ActivityRules(ProcessBuilder builder) {
        this.builder = builder;
        this.processRules = builder.getProcessRules();
        processRules.getActivities().add(this);
    }

    public void addRule(TemporalRule rule) {
        rules.add(rule);
    }

    /**
     * Handles overdue activities
     */
    public void processExpired(ActivityState activityState) throws Exception {
        for (TemporalRule rule : rules) {
            rule.processExpired(activityState);
        }
    }

    public void processExchange(Exchange exchange, ProcessInstance process) {
        for (TemporalRule rule : rules) {
            rule.processExchange(exchange, process);
        }
    }

    // Properties
    //-------------------------------------------------------------------------

    public ActivityDefinition getActivityDefinition() {
        if (activityDefinition == null) {
            activityDefinition = builder.findOrCreateActivityDefinition(activityName);
        }
        return activityDefinition;
    }

    public void setActivityDefinition(ActivityDefinition activityDefinition) {
        this.activityDefinition = activityDefinition;
    }

    public int getExpectedMessages() {
        return expectedMessages;
    }

    public void setExpectedMessages(int expectedMessages) {
        this.expectedMessages = expectedMessages;
    }

    public ProcessRules getProcessRules() {
        return processRules;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    // Implementation methods
    //-------------------------------------------------------------------------
    protected void doStart() throws Exception {
        ServiceHelper.startServices(rules);
    }

    protected void doStop() throws Exception {
        ServiceHelper.stopServices(rules);
    }
}
