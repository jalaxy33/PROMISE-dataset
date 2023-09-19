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
package org.apache.ivy.plugins.parser.m2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import junit.framework.TestCase;

import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.util.FileUtil;

public class PomModuleDescriptorWriterTest extends TestCase {
    private static String LICENSE;
    static {
        try {
            LICENSE = FileUtil.readEntirely(new BufferedReader(new InputStreamReader(
                PomModuleDescriptorWriterTest.class.getResourceAsStream("license.xml"))));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private File _dest = new File("build/test/test-write.xml");
    
    public void testSimple() throws Exception {
        ModuleDescriptor md = PomModuleDescriptorParser.getInstance().parseDescriptor(
            new IvySettings(), getClass().getResource("test-simple.pom"), false);
        PomModuleDescriptorWriter.write(md, LICENSE, PomModuleDescriptorWriter.DEFAULT_MAPPING, _dest);
        assertTrue(_dest.exists());

        String wrote = FileUtil.readEntirely(new BufferedReader(new FileReader(_dest))).replaceAll(
            "\r\n", "\n").replace('\r', '\n');
        String expected = readEntirely("test-write-simple.xml").replaceAll("\r\n", "\n").replace(
            '\r', '\n');
        assertEquals(expected, wrote);
    }
    
    public void testSimpleDependencies() throws Exception {
        ModuleDescriptor md = PomModuleDescriptorParser.getInstance().parseDescriptor(
            new IvySettings(), getClass().getResource("test-dependencies.pom"), false);
        PomModuleDescriptorWriter.write(md, LICENSE, PomModuleDescriptorWriter.DEFAULT_MAPPING, _dest);
        assertTrue(_dest.exists());

        String wrote = FileUtil.readEntirely(new BufferedReader(new FileReader(_dest))).replaceAll(
            "\r\n", "\n").replace('\r', '\n');
        String expected = readEntirely("test-write-simple-dependencies.xml")
            .replaceAll("\r\n", "\n").replace('\r', '\n');
        assertEquals(expected, wrote);
    }
    
    public void testDependenciesWithScope() throws Exception {
        ModuleDescriptor md = PomModuleDescriptorParser.getInstance().parseDescriptor(
            new IvySettings(), getClass().getResource("test-dependencies-with-scope.pom"), false);
        PomModuleDescriptorWriter.write(md, LICENSE, PomModuleDescriptorWriter.DEFAULT_MAPPING, _dest);
        assertTrue(_dest.exists());

        String wrote = FileUtil.readEntirely(new BufferedReader(new FileReader(_dest))).replaceAll(
            "\r\n", "\n").replace('\r', '\n');
        String expected = readEntirely("test-write-dependencies-with-scope.xml")
            .replaceAll("\r\n", "\n").replace('\r', '\n');
        assertEquals(expected, wrote);
    }
    
    public void testOptional() throws Exception {
        ModuleDescriptor md = PomModuleDescriptorParser.getInstance().parseDescriptor(
            new IvySettings(), getClass().getResource("test-optional.pom"), false);
        PomModuleDescriptorWriter.write(md, LICENSE, PomModuleDescriptorWriter.DEFAULT_MAPPING, _dest);
        assertTrue(_dest.exists());

        String wrote = FileUtil.readEntirely(new BufferedReader(new FileReader(_dest))).replaceAll(
            "\r\n", "\n").replace('\r', '\n');
        String expected = readEntirely("test-write-dependencies-optional.xml")
            .replaceAll("\r\n", "\n").replace('\r', '\n');
        assertEquals(expected, wrote);
    }
    
    private String readEntirely(String resource) throws IOException {
        return FileUtil.readEntirely(new BufferedReader(new InputStreamReader(
            PomModuleDescriptorWriterTest.class.getResource(resource).openStream())));
    }

    public void setUp() {
        // don't add ivy version to se static files for comparison
        PomModuleDescriptorWriter.setAddIvyVersion(false);
        if (_dest.exists()) {
            _dest.delete();
        }
        if (!_dest.getParentFile().exists()) {
            _dest.getParentFile().mkdirs();
        }
    }

    protected void tearDown() throws Exception {
        if (_dest.exists()) {
            _dest.delete();
        }
    }
}
