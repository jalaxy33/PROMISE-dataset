/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.apache.ivy.util.url;

import java.io.File;
import java.net.URL;

import junit.framework.TestCase;

/**
 * Test BasicURLHandler
 */
public class BasicURLHandlerTest extends TestCase {
    // remote.test
    public void testIsReachable() throws Exception {
        URLHandler handler = new BasicURLHandler();
        assertTrue(handler.isReachable(new URL("http://www.google.fr/")));
        assertFalse(handler.isReachable(new URL("http://www.google.fr/unknownpage.html")));

        assertTrue(handler.isReachable(new File("build.xml").toURL()));
        assertFalse(handler.isReachable(new File("unknownfile.xml").toURL()));

        // to test ftp we should know of an anonymous ftp site... !
        // assertTrue(handler.isReachable(new URL("ftp://ftp.mozilla.org/pub/dir.sizes")));
        assertFalse(handler.isReachable(new URL("ftp://ftp.mozilla.org/unknown.file")));
    }
}
