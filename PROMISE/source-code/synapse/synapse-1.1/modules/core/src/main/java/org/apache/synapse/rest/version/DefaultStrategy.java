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
package org.apache.synapse.rest.version;

import org.apache.synapse.config.xml.rest.VersionStrategyFactory;
import org.apache.synapse.rest.API;

public class DefaultStrategy extends  AbstractVersionStrategy{

    public DefaultStrategy(API api) {
        super(api, "", VersionStrategyFactory.TYPE_NULL);
    }

    public boolean isMatchingVersion(Object versionInfoObj) {
        return true;
    }

    public String getVersionParam() {
        return "";
    }


}
