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

package org.apache.synapse.registry.url;

import org.apache.synapse.registry.RegistryEntry;

import java.net.URI;
import java.util.Date;

public class URLRegistryEntry implements RegistryEntry {

    private String key = null;
    private String name = null;
    private long version = Long.MIN_VALUE;
    private URI type = null;
    private String description;
    private long created;
    private long lastModified;
    private long cachableDuration;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public URI getType() {
        return type;
    }

    public void setType(URI type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public long getCachableDuration() {
        return cachableDuration;
    }

    public void setCachableDuration(long cachableDuration) {
        this.cachableDuration = cachableDuration;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("RegistryEntry {")
            .append(" Key : " + key)
            .append(" Name : " + name)
            .append(" Ver : " + version)
            .append(" Type : " + type)
            .append(" Desc : " + description)
            .append(" Created : " + new Date(created))
            .append(" Modified : " + new Date(lastModified))
            .append(" Cacheable for : " + (cachableDuration / 1000) + "sec")
            .append("}");
        return sb.toString();
    }
}
