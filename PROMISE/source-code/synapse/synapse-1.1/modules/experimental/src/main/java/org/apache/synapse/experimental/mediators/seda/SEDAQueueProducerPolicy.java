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
package org.apache.synapse.experimental.mediators.seda;

/**
 *
 */
public class SEDAQueueProducerPolicy {

    public static final String ADD = "add";
    public static final String OFFER = "offer";
    public static final String PUT = "put";
    private long timeoutOnInsert = -1;
    private String action = OFFER;

    public long getTimeoutOnInsert() {
        return timeoutOnInsert;
    }

    public void setTimeoutOnInsert(long timeoutOnInsert) {
        this.timeoutOnInsert = timeoutOnInsert;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
