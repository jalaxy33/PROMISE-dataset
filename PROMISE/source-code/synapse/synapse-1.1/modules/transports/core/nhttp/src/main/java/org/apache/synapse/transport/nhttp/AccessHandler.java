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
package org.apache.synapse.transport.nhttp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Handles the Access, providing an Access object.
 */
public class AccessHandler {

    public final static String ACCESS_LOG_ID = "org.apache.synapse.transport.nhttp.access";

    private final static Log accessLog = LogFactory.getLog(ACCESS_LOG_ID);

    private static final AccessLogger accessLogger = new AccessLogger(accessLog);

    private static final Access access = new Access(accessLog, accessLogger);

    public static Access getAccess() {
        return access;
    }

    public static Log getAccessLog() {
        return accessLog;
    }
}
