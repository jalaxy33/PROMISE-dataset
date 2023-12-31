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

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.runtime.log.NullLogChute;
import org.apache.velocity.runtime.resource.loader.FileResourceLoader;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;

/**
 * Test that an instance of a ResourceLoader can be successfully passed in.
 *
 * @author <a href="mailto:wglass@apache.org">Will Glass-Husain</a>
 * @version $Id: ResourceLoaderInstanceTestCase.java 477002 2006-11-20 01:07:43Z henning $
 */
public class ResourceLoaderInstanceTestCase extends BaseTestCase
{
    /**
     * VTL file extension.
     */
    private static final String TMPL_FILE_EXT = "vm";

    /**
     * Comparison file extension.
     */
    private static final String CMP_FILE_EXT = "cmp";

    /**
     * Comparison file extension.
     */
    private static final String RESULT_FILE_EXT = "res";

    /**
     * Path for templates. This property will override the
     * value in the default velocity properties file.
     */
    private final static String FILE_RESOURCE_LOADER_PATH = TEST_COMPARE_DIR + "/resourceinstance";

    /**
     * Results relative to the build directory.
     */
    private static final String RESULTS_DIR = TEST_RESULT_DIR + "/resourceinstance";

    /**
     * Results relative to the build directory.
     */
    private static final String COMPARE_DIR = TEST_COMPARE_DIR + "/resourceinstance/compare";

    /**
     * Default constructor.
     */
    public ResourceLoaderInstanceTestCase(String name)
    {
        super(name);
    }

    public void setUp()
            throws Exception
    {

        ResourceLoader rl = new FileResourceLoader();

        // pass in an instance to Velocity
        Velocity.addProperty( "resource.loader", "testrl" );
        Velocity.setProperty( "testrl.resource.loader.instance", rl );
        Velocity.setProperty( "testrl.resource.loader.path", FILE_RESOURCE_LOADER_PATH );

        Velocity.setProperty(
                Velocity.RUNTIME_LOG_LOGSYSTEM_CLASS, NullLogChute.class.getName());

        Velocity.init();
    }

    public static Test suite ()
    {
        return new TestSuite(ResourceLoaderInstanceTestCase.class);
    }

    /**
     * Runs the test.
     */
    public void testResourceLoaderInstance ()
            throws Exception
    {
        assureResultsDirectoryExists(RESULTS_DIR);

        Template template = RuntimeSingleton.getTemplate(
                getFileName(null, "testfile", TMPL_FILE_EXT));

        FileOutputStream fos =
                new FileOutputStream (
                        getFileName(RESULTS_DIR, "testfile", RESULT_FILE_EXT));


        Writer writer = new BufferedWriter(new OutputStreamWriter(fos));

        /*
         *  put the Vector into the context, and merge both
         */

        VelocityContext context = new VelocityContext();

        template.merge(context, writer);
        writer.flush();
        writer.close();

        if ( !isMatch(RESULTS_DIR, COMPARE_DIR, "testfile",
                        RESULT_FILE_EXT, CMP_FILE_EXT) )
        {
            fail("Output incorrect.");
        }
    }
}
