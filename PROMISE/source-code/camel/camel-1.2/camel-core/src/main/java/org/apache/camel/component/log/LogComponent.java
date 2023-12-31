/**
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
package org.apache.camel.component.log;

import java.util.Map;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultComponent;
import org.apache.camel.impl.ProcessorEndpoint;
import org.apache.camel.processor.Logger;
import org.apache.camel.processor.LoggingLevel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @version $Revision: $
 */
public class LogComponent extends DefaultComponent<Exchange> {
    private static final Log LOG = LogFactory.getLog(LogComponent.class);

    protected Endpoint<Exchange> createEndpoint(String uri, String remaining, Map parameters) throws Exception {
        LoggingLevel level = getLoggingLevel(parameters);
        Logger logger = new Logger(remaining, level);

        return new ProcessorEndpoint(uri, this, logger);
    }

    protected LoggingLevel getLoggingLevel(Map parameters) {
        String levelText = (String) parameters.get("level");
        LoggingLevel level = null;
        if (levelText != null) {
            level = LoggingLevel.valueOf(levelText.toUpperCase());
            if (level == null) {
                LOG.warn("Could not convert level text: " + levelText + " to a valid logging level so defaulting to WARN");
            }
        }
        if (level == null) {
            level = LoggingLevel.INFO;
        }
        return level;
    }
}
