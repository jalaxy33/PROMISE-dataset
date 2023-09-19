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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import junit.framework.TestCase;

import org.apache.ivy.util.FileUtil;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Delete;

public class IvyRepositoryReportTest extends TestCase {
    private File cache;

    private IvyRepositoryReport report;

    protected void setUp() throws Exception {
        createCache();
        Project project = new Project();
        project.setProperty("ivy.settings.file", "test/repositories/ivysettings-1.xml");

        report = new IvyRepositoryReport();
        report.setProject(project);
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
        report.setOrganisation("org1");
        report.setOutputname("testsimple");
        report.setTodir(cache);
        report.execute();

        File reportFile = new File(cache, "testsimple.xml");
        assertTrue(reportFile.exists());
        String g = FileUtil.readEntirely(new BufferedReader(new FileReader(reportFile)));
        
        // check presence of the modules
        assertTrue(g.indexOf("<module organisation=\"org1\" name=\"mod1.1\"") != -1);
        assertTrue(g.indexOf("<module organisation=\"org1\" name=\"mod1.2\"") != -1);
        assertTrue(g.indexOf("<module organisation=\"org1\" name=\"mod1.3\"") != -1);
        assertTrue(g.indexOf("<module organisation=\"org1\" name=\"mod1.4\"") != -1);
        assertTrue(g.indexOf("<module organisation=\"org1\" name=\"mod1.5\"") != -1);
        assertTrue(g.indexOf("<module organisation=\"org1\" name=\"mod1.6\"") != -1);
    }
    
    public void testBranchBeforeModule() throws Exception {
        report.getProject().setProperty("ivy.settings.file", "test/repositories/IVY-716/ivysettings.xml");
        report.setOutputname("testbranch");
        report.setTodir(cache);
        report.execute();

        File reportFile = new File(cache, "testbranch.xml");
        assertTrue(reportFile.exists());
        String g = FileUtil.readEntirely(new BufferedReader(new FileReader(reportFile)));
        
        // check presence of the modules
        assertTrue(g.indexOf("<module organisation=\"org1\" name=\"mod1.1\"") != -1);
        
        // check presence of the branches
        assertTrue(g.indexOf("<revision name=\"1.0\" branch=\"branch1\"") != -1);
        assertTrue(g.indexOf("<revision name=\"1.0\" branch=\"branch2\"") != -1);
    }
    
    public void testPatternWithoutOrganisation() throws Exception {
        report.getProject().setProperty("ivy.settings.file", "test/repositories/IVY-729/ivysettings.xml");
        report.setOutputname("test-no-org");
        report.setTodir(cache);
        report.execute();

        File reportFile = new File(cache, "test-no-org.xml");
        assertTrue(reportFile.exists());
        String g = FileUtil.readEntirely(new BufferedReader(new FileReader(reportFile)));
        
        // check presence of the modules
        assertTrue(g.indexOf("<module organisation=\"null\" name=\"a\"") != -1);
        assertTrue(g.indexOf("<module organisation=\"null\" name=\"b\"") != -1);
        assertTrue(g.indexOf("<module organisation=\"null\" name=\"c\"") != -1);
    }
}
