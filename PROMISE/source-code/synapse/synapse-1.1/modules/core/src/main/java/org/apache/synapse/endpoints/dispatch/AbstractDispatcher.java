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

package org.apache.synapse.endpoints.dispatch;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.endpoints.Endpoint;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.Map;

/**
 *
 */
public abstract class AbstractDispatcher implements Dispatcher {

    protected Log log;

    protected AbstractDispatcher() {
        log = LogFactory.getLog(this.getClass());
    }

    public List<Endpoint> getEndpoints(SessionInformation sessionInformation) {
        return SALSessions.getInstance().getChildEndpoints(sessionInformation);
    }

    protected String extractSessionID(OMElement header, QName keyQName) {

        OMElement sgcIDElm = getHeaderBlock(header, keyQName);

        if (sgcIDElm != null) {
            String sgcID = sgcIDElm.getText();

            if (sgcID != null && !"".equals(sgcID)) {
                return sgcID.trim();
            } else {
                if (log.isDebugEnabled()) {
                    log.debug(keyQName + " is null or empty");
                }
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Couldn't find the " + keyQName + " SOAP header to find the session");
            }
        }
        return null;
    }

    protected String extractSessionID(MessageContext synCtx, String key) {

        if (key != null) {
            Map headerMap = getTransportHeaderMap(synCtx);

            if (headerMap != null) {
                Object hostObj = headerMap.get("Host");
                if (log.isDebugEnabled()) {
                    log.debug("A request received with the Host Name : " + hostObj);
                }
                Object cookieObj = headerMap.get(key);

                if (cookieObj instanceof String) {
                    String cookie = (String) cookieObj;
                    if (log.isDebugEnabled()) {
                        log.debug("Cookies String : " + cookie);
                    }
                    
                    // extract the first name value pair of the Set-Cookie header, which is considered
                    // as the session id which will be sent back from the client with the Cookie header
                    // for example;
                    //      Set-Cookie: JSESSIONID=760764CB72E96A7221506823748CF2AE; Path=/
                    // will result in the session id "JSESSIONID=760764CB72E96A7221506823748CF2AE"
                    String[] sessionIds = cookie.split(";");

                    if (sessionIds == null || sessionIds.length == 0) {
                        if (log.isDebugEnabled()) {
                            log.debug("Cannot find a session id for the cookie : " + cookie);
                        }
                        return null;
                    }

                    for (String sessionId : sessionIds){
                        if(sessionId != null && sessionId.indexOf("JSESSIONID") != -1) {
                            if (log.isDebugEnabled()) {
                                log.debug("Extracted SessionID : " + sessionId);
                            }
                            return sessionId.trim();
                        }
                    }

                    return null;
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Couldn't find the " + key + " header to find the session");
                    }
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Couldn't find the TRANSPORT_HEADERS to find the session");
                }

            }
        }
        return null;
    }

    protected void removeSessionID(MessageContext synCtx, String key) {

        if (key != null) {
            Map headerMap = getTransportHeaderMap(synCtx);
            if (headerMap != null) {
                headerMap.remove(key);
            }
        }
    }

    protected void removeSessionID(OMElement header, QName keyQName) {

        OMElement sgcIDElm = getHeaderBlock(header, keyQName);
        if (sgcIDElm != null) {
            sgcIDElm.detach();
        }
    }


    private Map getTransportHeaderMap(MessageContext synCtx) {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) synCtx).getAxis2MessageContext();

        Object o = axis2MessageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        if (o != null && o instanceof Map) {
            return (Map) o;
        }
        return null;
    }

    private OMElement getHeaderBlock(OMElement soapHeader, QName keyQName) {

        if (soapHeader != null) {
            return soapHeader.getFirstChildWithName(keyQName);
        }
        return null;
    }
}
