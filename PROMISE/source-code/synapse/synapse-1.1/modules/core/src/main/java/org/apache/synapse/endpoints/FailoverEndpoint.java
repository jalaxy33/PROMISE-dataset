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

package org.apache.synapse.endpoints;

import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.mediators.MediatorProperty;

import java.util.Map;
import java.util.Set;

/**
 * FailoverEndpoint can have multiple child endpoints. It will always try to send messages to
 * current endpoint. If the current endpoint is failing, it gets another active endpoint from the
 * list and make it the current endpoint. Then the message is sent to the current endpoint and if
 * it fails, above procedure repeats until there are no active endpoints. If all endpoints are
 * failing and parent endpoint is available, this will delegate the problem to the parent endpoint.
 * If parent endpoint is not available it will pop the next FaultHandler and delegate the problem
 * to that.
 */
public class FailoverEndpoint extends AbstractEndpoint {

    /** Endpoint for which is currently used */
    private Endpoint currentEndpoint = null;

    /** The fail-over mode supported by this endpoint. By default we do dynamic fail-over */
    private boolean dynamic = true;

    public void send(MessageContext synCtx) {

        if (log.isDebugEnabled()) {
            log.debug("Failover Endpoint : " + getName());
        }

        boolean isARetry = false;
        if (synCtx.getProperty(SynapseConstants.LAST_ENDPOINT) == null) {
            if (log.isDebugEnabled()) {
                log.debug(this + " Building the SoapEnvelope");
            }
            // If not yet a retry, we have to build the envelope since we need to support failover
            synCtx.getEnvelope().build();
        } else {
            isARetry = true;
        }

        if (getChildren().isEmpty()) {
            informFailure(synCtx, SynapseConstants.ENDPOINT_FO_NONE_READY,
                    "FailoverLoadbalance endpoint : " + getName() + " - no child endpoints");
            return;
        }

        // evaluate the endpoint properties
        evaluateProperties(synCtx);
        
        if (dynamic) {
            // Dynamic fail-over mode - Switch to a backup endpoint when an error occurs
            // in the primary endpoint. But switch back to the primary as soon as it becomes
            // active again.

            boolean foundEndpoint = false;
            for (Endpoint endpoint : getChildren()) {
                if (endpoint.readyToSend()) {
                    foundEndpoint = true;
                    if (isARetry && metricsMBean != null) {
                        metricsMBean.reportSendingFault(SynapseConstants.ENDPOINT_FO_FAIL_OVER);
                    }
                    synCtx.pushFaultHandler(this);
                    endpoint.send(synCtx);
                    break;
                }
            }

            if (!foundEndpoint) {
                String msg = "Failover endpoint : " +
                        (getName() != null ? getName() : SynapseConstants.ANONYMOUS_ENDPOINT) +
                        " - no ready child endpoints";
                log.warn(msg);
                informFailure(synCtx, SynapseConstants.ENDPOINT_FO_NONE_READY, msg);
            }

        } else {
            // Static fail-over mode - Switch to a backup endpoint when an error occurs
            // in the primary endpoint. Keep sending messages to the backup endpoint until
            // an error occurs in that endpoint.

            if (currentEndpoint == null) {
                currentEndpoint = getChildren().get(0);
            }

            if (currentEndpoint.readyToSend()) {
                if (isARetry && metricsMBean != null) {
                    metricsMBean.reportSendingFault(SynapseConstants.ENDPOINT_FO_FAIL_OVER);
                }
                synCtx.pushFaultHandler(this);
                currentEndpoint.send(synCtx);

            } else {
                boolean foundEndpoint = false;
                for (Endpoint endpoint : getChildren()) {
                    if (endpoint.readyToSend()) {
                        foundEndpoint = true;
                        currentEndpoint = endpoint;
                        if (isARetry && metricsMBean != null) {
                            metricsMBean.reportSendingFault(SynapseConstants.ENDPOINT_FO_FAIL_OVER);
                        }
                        synCtx.pushFaultHandler(this);
                        currentEndpoint.send(synCtx);
                        break;
                    }
                }

                if (!foundEndpoint) {
                    String msg = "Failover endpoint : " +
                            (getName() != null ? getName() : SynapseConstants.ANONYMOUS_ENDPOINT) +
                            " - no ready child endpoints";
                    log.warn(msg);
                    informFailure(synCtx, SynapseConstants.ENDPOINT_FO_NONE_READY, msg);
                }
            }
        }
    }

    public void onChildEndpointFail(Endpoint endpoint, MessageContext synMessageContext) {
        logOnChildEndpointFail(endpoint, synMessageContext);
        if (!((AbstractEndpoint)endpoint).isRetryDisabled(synMessageContext)) {
            if (log.isDebugEnabled()) {
                log.debug(this + " Retry Attempt for Request with [Message ID : " +
                        synMessageContext.getMessageID() + "], [To : " +
                        synMessageContext.getTo() + "]");
            }
            send(synMessageContext);
        } else {
            String msg = "Failover endpoint : " +
                    (getName() != null ? getName() : SynapseConstants.ANONYMOUS_ENDPOINT) +
                    " - one of the child endpoints encounterd a non-retry error, " +
                    "not sending message to another endpoint";
            log.warn(msg);
            informFailure(synMessageContext, SynapseConstants.ENDPOINT_FO_NONE_READY, msg);
        }
    }

    public boolean readyToSend() {
        for (Endpoint endpoint : getChildren()) {
            if (endpoint.readyToSend()) {
                currentEndpoint = endpoint;
                return true;
            }
        }
        return false;
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }
}
