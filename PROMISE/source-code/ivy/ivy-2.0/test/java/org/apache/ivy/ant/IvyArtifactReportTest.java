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
package org.apache.ivy.ant;

import java.io.File;

import junit.framework.TestCase;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Delete;

public class IvyArtifactReportTest extends TestCase {
    private File cache;

    private IvyArtifactReport prop;

    private Project project;

    protected void setUp() throws Exception {
        createCache();
        project = new Project();
        project.setProperty("ivy.settings.file", "test/repositories/ivysettings.xml");

        prop = new IvyArtifactReport();
        prop.setProject(project);
        System.setProperty("ivy.cache.dir", cache.getAbsolutePath());
    }

    private void createCache() {
        cache = new File("build/cache");
        cache.mkdirs();
    }

    protected void tearDown() throws Exception {
        cleanCache();
    }

    private void cleanCache() {
        Delete del = new Delete();
        del.setProject(new Project());
        del.setDir(cache);
        del.execute();
    }

    public void testSimple() throws Exception {
        prop.setTofile(new File("build/test-artifact-report.xml"));
        prop.setFile(new File("test/java/org/apache/ivy/ant/ivy-simple.xml"));
        prop.execute();

        assertTrue(new File("build/test-artifact-report.xml").exists());
    }
}
