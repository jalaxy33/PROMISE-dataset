/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.builder.xml;

import org.apache.camel.Message;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.Result;
import java.io.StringWriter;

/**
 * Processes the XSLT result as a String
 *
 * @version $Revision: 1.1 $
 */
public class StringResultHandler implements ResultHandler {
    StringWriter buffer = new StringWriter();
    StreamResult result = new StreamResult(buffer);

    public Result getResult() {
        return result;
    }

    public void setBody(Message in) {
        in.setBody(buffer.toString());
    }
}
