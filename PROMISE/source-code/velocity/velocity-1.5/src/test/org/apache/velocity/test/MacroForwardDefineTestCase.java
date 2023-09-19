package org.apache.velocity.test;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.test.misc.TestLogChute;

/**
 * Make sure that a forward referenced macro inside another macro definition does
 * not report an error in the log.
 * (VELOCITY-71).
 * 
 * @author <a href="mailto:henning@apache.org">Henning P. Schmiedehausen</a>
 * @version $Id: MacroForwardDefineTestCase.java 491369 2006-12-31 02:52:05Z wglass $
 */
public class MacroForwardDefineTestCase 
        extends BaseTestCase
{
   /**
    * Path for templates. This property will override the
    * value in the default velocity properties file.
    */
   private final static String FILE_RESOURCE_LOADER_PATH = TEST_COMPARE_DIR + "/macroforwarddefine";

    /**
     * Results relative to the build directory.
     */
    private static final String RESULTS_DIR = TEST_RESULT_DIR + "/macroforwarddefine";

    /**
     * Results relative to the build directory.
     */
    private static final String COMPARE_DIR = TEST_COMPARE_DIR + "/macroforwarddefine/compare";

    /**
     * Collects the log messages.
     */
    private TestLogChute logger = new TestLogChute();
    
    /**
     * Default constructor.
     */
    public MacroForwardDefineTestCase(String name)
    {
        super(name);
    }

    public void setUp()
        throws Exception
    {
        assureResultsDirectoryExists(RESULTS_DIR);
                
        // use Velocity.setProperty (instead of properties file) so that we can use actual instance of log
        Velocity.setProperty(RuntimeConstants.RESOURCE_LOADER,"file");
        Velocity.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, FILE_RESOURCE_LOADER_PATH );
        Velocity.setProperty(RuntimeConstants.RUNTIME_LOG_REFERENCE_LOG_INVALID,"true");
        Velocity.setProperty(RuntimeConstants.VM_LIBRARY, "macros.vm");

        // actual instance of logger
        logger = new TestLogChute();
        Velocity.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM,logger);
        Velocity.setProperty("runtime.log.logsystem.test.level", "error");

        Velocity.init();
    }

    public static Test suite()
    {
       return new TestSuite(MacroForwardDefineTestCase.class);
    }

    public void testLogResult()
        throws Exception
    {
        if ( !isMatch(logger.getLog(), COMPARE_DIR, "velocity.log", "cmp"))
        {
            fail("Output incorrect.");
        }
    }
}
