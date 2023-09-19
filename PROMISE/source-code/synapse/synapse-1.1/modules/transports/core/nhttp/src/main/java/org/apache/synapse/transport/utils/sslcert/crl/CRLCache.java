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

package org.apache.synapse.transport.utils.sslcert.crl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.synapse.commons.jmx.MBeanRegistrar;
import org.apache.synapse.transport.utils.sslcert.cache.CacheController;
import org.apache.synapse.transport.utils.sslcert.cache.CacheManager;
import org.apache.synapse.transport.utils.sslcert.cache.ManageableCache;
import org.apache.synapse.transport.utils.sslcert.cache.ManageableCacheValue;

import java.security.cert.X509CRL;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Since a CRL maps to a CRL URL, the CRLCache should have x509CRL entries against CRL URLs.
 * This cache is a Singleton since it is shared by any transport which needs SSL certificate
 * validation and more than one CRLCache should not be allowed per system.
 */
public class CRLCache implements ManageableCache {

    private static final Log log = LogFactory.getLog(CRLCache.class);

    private static final CRLCache cache = new CRLCache();
    private final Map<String, CRLCacheValue> hashMap;
    private Iterator<Map.Entry<String, CRLCacheValue>> iterator;
    private volatile CacheManager cacheManager;
    private final CRLVerifier crlVerifier;

    private CRLCache() {
        hashMap = new ConcurrentHashMap<String, CRLCacheValue>();
        iterator = hashMap.entrySet().iterator();
        crlVerifier = new CRLVerifier(null);
    }

    public static CRLCache getCache() {
        return cache;
    }

    /**
     * This initialize the Cache with a CacheManager. If this method is not called, a cache manager
     * will not be used.
     *
     * @param size max size of the cache
     * @param delay defines how frequently the CacheManager will be started
     */
    public void init(int size, int delay) {
        if (cacheManager == null) {
            synchronized (CRLCache.class) {
                if (cacheManager == null) {
                    cacheManager = new CacheManager(cache, size, delay);
                    CacheController mbean = new CacheController(cache, cacheManager);
                    MBeanRegistrar.getInstance().registerMBean(mbean, "CacheController",
                            "CRLCacheController");
                }
            }
        }
    }

    /**
     * This method is needed by the cache Manager to go through the cache entries to remove
     * invalid values or to remove LRU cache values if the cache has reached its max size.
     *
     * @return next cache value of the cache.
     */
    public synchronized ManageableCacheValue getNextCacheValue() {
        //changes to the map are reflected on the keySet. And its iterator is weakly consistent.
        // so will never throw concurrent modification exception.
        if (iterator.hasNext()) {
            return hashMap.get(iterator.next().getKey());
        } else {
            resetIterator();
            return null;
        }
    }

    /**
     * To get the current cache size (size of the hash map).
     */
    public synchronized int getCacheSize() {
        return hashMap.size();
    }

    public synchronized void resetIterator() {
        iterator = hashMap.entrySet().iterator();
    }

    private synchronized void replaceNewCacheValue(CRLCacheValue cacheValue) {
        //If someone has updated with the new value before current Thread.
        if (cacheValue.isValid()) {
            return;
        }

        try {
            String crlUrl = cacheValue.crlUrl;
            X509CRL x509CRL = crlVerifier.downloadCRLFromWeb(crlUrl);
            this.setCacheValue(crlUrl, x509CRL);
        } catch (Exception e) {
            log.debug("Cant replace old CacheValue with new CacheValue. So remove", e);
            //If cant be replaced remove.
            cacheValue.removeThisCacheValue();
        }
    }

    public synchronized X509CRL getCacheValue(String crlUrl) {
        CRLCacheValue cacheValue = hashMap.get(crlUrl);
        if (cacheValue != null) {
            //If who ever gets this cache value before Cache manager task found its invalid,
            // update it and get the new value.
            if (!cacheValue.isValid()) {
                cacheValue.updateCacheWithNewValue();
                CRLCacheValue crlCacheValue = hashMap.get(crlUrl);
                return (crlCacheValue != null ? crlCacheValue.getValue() : null);
            }

            return cacheValue.getValue();
        }
        return null;
    }

    public synchronized void setCacheValue(String crlUrl, X509CRL crl) {
        CRLCacheValue cacheValue = new CRLCacheValue(crlUrl, crl);
        if (log.isDebugEnabled()) {
            log.debug("Before set - HashMap size " + hashMap.size());
        }
        hashMap.put(crlUrl, cacheValue);
        if (log.isDebugEnabled()) {
            log.debug("After set - HashMap size " + hashMap.size());
        }
    }

    public synchronized void removeCacheValue(String crlUrl) {
        if (log.isDebugEnabled()) {
            log.debug("Before remove - HashMap size " + hashMap.size());
        }
        hashMap.remove(crlUrl);
        if (log.isDebugEnabled()) {
            log.debug("After remove - HashMap size " + hashMap.size());
        }

    }

    /**
     * This is the wrapper class of the actual cache value which is a X509CRL.
     */
    private class CRLCacheValue implements ManageableCacheValue {

        private String crlUrl;
        private X509CRL crl;
        private long timeStamp = System.currentTimeMillis();

        public CRLCacheValue(String crlUrl, X509CRL crl) {
            this.crlUrl = crlUrl;
            this.crl = crl;
        }

        public String getKey() {
            return crlUrl;
        }

        public X509CRL getValue() {
            timeStamp = System.currentTimeMillis();
            return crl;
        }

        /**
         * CRL has a validity period. We can reuse a downloaded CRL within that period.
         */
        public boolean isValid() {
            Date today = new Date();
            Date nextUpdate = crl.getNextUpdate();
            return nextUpdate != null && nextUpdate.after(today);
        }

        public long getTimeStamp() {
            return timeStamp;
        }

        /**
         * Used by cacheManager to remove invalid entries.
         */
        public void removeThisCacheValue() {
            removeCacheValue(crlUrl);
        }

        public void updateCacheWithNewValue() {
            replaceNewCacheValue(this);
        }
    }
}
