/*
 * dwarfhustle-model-db - Manages the compile dependencies for the model.
 * Copyright © 2022-2024 Erwin Müller (erwin.mueller@anrisoftware.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.anrisoftware.dwarfhustle.model.db.cache

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.hasKey

import java.time.Duration

/**
 * Creates the JCS cache configuration.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public class JcsCacheConfig {

    public static final Duration MAX_LIFE_DEFAULT = Duration.ofHours(1)

    /**
     * Creates the configuration for the caches.
     *
     * @param config the {@link Properties} with the cache configuration.
     * @param params a map with CACHE_NAME := configuration items.
     */
    public static void createCaches(Properties config, Map<String, Map<String, Object>> params) {
        params.each { key, value ->
            createCache(config, value)
            if (value.have_file_aux) {
                createFileAuxCache(config, value)
            }
        }
        config.entrySet().each {
            config[it.key] = it.value as String
        }
    }

    /**
     * Creates the cache from the configuration and additional parameters.
     *
     * @param config the {@link Properties} with the cache configuration.
     * @param params additional parameters:
     *               <ul>
     *               <li>have_file_aux
     *               <li>cache_name
     *               <li>max_objects
     *               <li>use_mem_shrinker
     *               <li>shrinker_interval
     *               <li>max_idle
     *               <li>is_eternal
     *               <li>max_life
     *               </ul>
     */
    public static void createCache(Properties config, Map<String, Object> params) {
        assertThat(params, hasKey("cache_name"))
        assertThat(params, hasKey("max_objects"))
        def region = "jcs.region.${params.cache_name}"
        if (params.have_file_aux) {
            config["${region}"] = "${params.cache_name}_file"
        } else {
            config["${region}"] = ""
        }
        config["${region}.cacheattributes"] = "org.apache.commons.jcs3.engine.CompositeCacheAttributes"
        config["${region}.cacheattributes.MaxObjects"] = "${params.max_objects}"
        config["${region}.cacheattributes.MemoryCacheName"] = "org.apache.commons.jcs3.engine.memory.lru.LRUMemoryCache"
        config["${region}.cacheattributes.UseMemoryShrinker"] = "${params.containsKey("use_mem_shrinker") ? params.use_mem_shrinker : false}"
        config["${region}.cacheattributes.ShrinkerIntervalSeconds"] = "${params.containsKey("shrinker_interval") ? params.shrinker_interval : 30}"
        config["${region}.cacheattributes.MaxMemoryIdleTimeSeconds"] = "${params.containsKey("max_idle") ? params.max_idle : 60}"
        config["${region}.cacheattributes.MaxSpoolPerRun"] = "500"
        config["${region}.elementattributes"] = "org.apache.commons.jcs3.engine.ElementAttributes"
        config["${region}.elementattributes.IsEternal"] = "${params.containsKey("is_eternal") ? params.is_eternal : false}"
        config["${region}.elementattributes.MaxLife"] = "${params.containsKey("max_life") ? params.max_life : MAX_LIFE_DEFAULT.seconds}"
        config["${region}.elementattributes.IsSpool"] = "true"
        config["${region}.elementattributes.IsLateral"] = "true"
        config["${region}.elementattributes.IsRemote"] = "false"
    }

    /**
     * Creates the auxiliary file cache from the configuration and additional
     * parameters.
     *
     * @param config the {@link Properties} with the cache configuration.
     * @param params additional parameters:
     *               <ul>
     *               <li>cache_name
     *               <li>max_key_size
     *               <li>parent_dir
     *               </ul>
     */
    public static void createFileAuxCache(Properties config, Map<String, Object> params) {
        assertThat(params, hasKey("cache_name"))
        assertThat(params, hasKey("max_key_size"))
        assertThat(params, hasKey("parent_dir"))
        def cacheName = "${params.cache_name}_file"
        def aux = "jcs.auxiliary.${cacheName}"
        config["${aux}"] = "org.apache.commons.jcs3.auxiliary.disk.indexed.IndexedDiskCacheFactory"
        config["${aux}.attributes"] = "org.apache.commons.jcs3.auxiliary.disk.indexed.IndexedDiskCacheAttributes"
        config["${aux}.attributes.DiskPath"] = "${params.parent_dir.absolutePath}/jcs_swap_${cacheName}"
        config["${aux}.attributes.MaxPurgatorySize"] = "${params.max_key_size}"
        config["${aux}.attributes.MaxKeySize"] = "${params.max_key_size}"
        config["${aux}.attributes.OptimizeAtRemoveCount"] = "300000"
        config["${aux}.attributes.ShutdownSpoolTimeLimit"] = "60"
        config["${aux}.attributes.OptimizeOnShutdown"] = "true"
        config["${aux}.attributes.DiskLimitType"] = "COUNT"
    }
}
