/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *  
 */
package org.apache.camel.component.mina;

import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;

/**
 * {@link IoHandler} implementation of reverser server protocol.
 * 
 * @author The Apache Directory Project (mina-dev@directory.apache.org)
 * @version $Rev: 555855 $, $Date: 2007-07-12 20:19:00 -0700 (Thu, 12 Jul 2007) $,
 */
public class ReverseProtocolHandler extends IoHandlerAdapter {
    public void exceptionCaught(IoSession session, Throwable cause) {
        cause.printStackTrace();
        // Close connection when unexpected exception is caught.
        session.close();
    }

    public void messageReceived(IoSession session, Object message) {
        // Reverse reveiced string
        String str = message.toString();
        StringBuffer buf = new StringBuffer(str.length());
        for (int i = str.length() - 1; i >= 0; i--) {
            buf.append(str.charAt(i));
        }

        // and write it back.
        session.write(buf.toString());
    }
}
