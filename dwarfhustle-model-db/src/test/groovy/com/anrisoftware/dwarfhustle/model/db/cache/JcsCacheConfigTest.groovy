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

import org.junit.jupiter.api.Test

/**
 * @see JcsCacheConfig
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
class JcsCacheConfigTest {

    @Test
    void create_cache() {
        def config = new Properties()
        def params = [mapblocks: [:]]
        params.mapblocks.cache_name = "mapblocks"
        params.mapblocks.max_objects = 100
        JcsCacheConfig.createCaches(config, params)
        assert config["jcs.region.mapblocks.cacheattributes"] == "org.apache.commons.jcs3.engine.CompositeCacheAttributes"
        assert config["jcs.region.mapblocks.cacheattributes.MaxObjects"] == "100"
        assert config["jcs.region.mapblocks.cacheattributes.MemoryCacheName"] == "org.apache.commons.jcs3.engine.memory.lru.LRUMemoryCache"
        assert config["jcs.region.mapblocks.cacheattributes.UseMemoryShrinker"] == "false"
        assert config["jcs.region.mapblocks.cacheattributes.ShrinkerIntervalSeconds"] == "30"
        assert config["jcs.region.mapblocks.cacheattributes.MaxMemoryIdleTimeSeconds"] == "60"
        assert config["jcs.region.mapblocks.cacheattributes.MaxSpoolPerRun"] == "500"
        assert config["jcs.region.mapblocks.elementattributes"] == "org.apache.commons.jcs3.engine.ElementAttributes"
        assert config["jcs.region.mapblocks.elementattributes.IsEternal"] == "false"
        assert config["jcs.region.mapblocks.elementattributes.MaxLife"] == "3600"
        assert config["jcs.region.mapblocks.elementattributes.IsSpool"] == "true"
        assert config["jcs.region.mapblocks.elementattributes.IsLateral"] == "true"
        assert config["jcs.region.mapblocks.elementattributes.IsRemote"] == "false"
    }

    @Test
    void create_cache_with_aux_file() {
        def config = new Properties()
        def params = [mapblocks: [:]]
        params.mapblocks.cache_name = "mapblocks"
        params.mapblocks.max_objects = 10000
        params.mapblocks.is_eternal = true
        params.mapblocks.max_key_size = 10000
        params.mapblocks.have_file_aux = true
        params.mapblocks.parent_dir = new File("/path")
        JcsCacheConfig.createCaches(config, params)
        assert config["jcs.region.mapblocks"] == "mapblocks_file"
        assert config["jcs.region.mapblocks.cacheattributes"] == "org.apache.commons.jcs3.engine.CompositeCacheAttributes"
        assert config["jcs.region.mapblocks.cacheattributes.MaxObjects"] == "10000"
        assert config["jcs.region.mapblocks.cacheattributes.MemoryCacheName"] == "org.apache.commons.jcs3.engine.memory.lru.LRUMemoryCache"
        assert config["jcs.region.mapblocks.cacheattributes.UseMemoryShrinker"] == "false"
        assert config["jcs.region.mapblocks.cacheattributes.ShrinkerIntervalSeconds"] == "30"
        assert config["jcs.region.mapblocks.cacheattributes.MaxMemoryIdleTimeSeconds"] == "60"
        assert config["jcs.region.mapblocks.cacheattributes.MaxSpoolPerRun"] == "500"
        assert config["jcs.region.mapblocks.elementattributes"] == "org.apache.commons.jcs3.engine.ElementAttributes"
        assert config["jcs.region.mapblocks.elementattributes.IsEternal"] == "true"
        assert config["jcs.region.mapblocks.elementattributes.MaxLife"] == "3600"
        assert config["jcs.region.mapblocks.elementattributes.IsSpool"] == "true"
        assert config["jcs.region.mapblocks.elementattributes.IsLateral"] == "true"
        assert config["jcs.region.mapblocks.elementattributes.IsRemote"] == "false"
        // aux file
        assert config["jcs.auxiliary.mapblocks_file"] == "org.apache.commons.jcs3.auxiliary.disk.indexed.IndexedDiskCacheFactory"
        assert config["jcs.auxiliary.mapblocks_file.attributes"] == "org.apache.commons.jcs3.auxiliary.disk.indexed.IndexedDiskCacheAttributes"
        assert config["jcs.auxiliary.mapblocks_file.attributes.DiskPath"] == "/path/jcs_swap_mapblocks_file"
        assert config["jcs.auxiliary.mapblocks_file.attributes.MaxPurgatorySize"] == "10000"
        assert config["jcs.auxiliary.mapblocks_file.attributes.MaxKeySize"] == "10000"
        assert config["jcs.auxiliary.mapblocks_file.attributes.OptimizeAtRemoveCount"] == "300000"
        assert config["jcs.auxiliary.mapblocks_file.attributes.ShutdownSpoolTimeLimit"] == "60"
        assert config["jcs.auxiliary.mapblocks_file.attributes.OptimizeOnShutdown"] == "true"
        assert config["jcs.auxiliary.mapblocks_file.attributes.DiskLimitType"] == "COUNT"
    }

    @Test
    void create_multiple_caches_with_aux_file() {
        def config = new Properties()
        def params = [mapblocks: [:], objects: [:]]
        params.mapblocks.cache_name = "mapblocks"
        params.mapblocks.max_objects = 10000
        params.mapblocks.is_eternal = true
        params.mapblocks.max_key_size = 10000
        params.mapblocks.have_file_aux = true
        params.mapblocks.parent_dir = new File("/path")
        params.objects.cache_name = "objects"
        params.objects.max_objects = 500
        params.objects.is_eternal = true
        params.objects.max_key_size = 500
        params.objects.have_file_aux = true
        params.objects.parent_dir = new File("/path")
        JcsCacheConfig.createCaches(config, params)
        // mapblocks
        assert config["jcs.region.mapblocks"] == "mapblocks_file"
        assert config["jcs.region.mapblocks.cacheattributes"] == "org.apache.commons.jcs3.engine.CompositeCacheAttributes"
        assert config["jcs.region.mapblocks.cacheattributes.MaxObjects"] == "10000"
        assert config["jcs.region.mapblocks.cacheattributes.MemoryCacheName"] == "org.apache.commons.jcs3.engine.memory.lru.LRUMemoryCache"
        assert config["jcs.region.mapblocks.cacheattributes.UseMemoryShrinker"] == "false"
        assert config["jcs.region.mapblocks.cacheattributes.ShrinkerIntervalSeconds"] == "30"
        assert config["jcs.region.mapblocks.cacheattributes.MaxMemoryIdleTimeSeconds"] == "60"
        assert config["jcs.region.mapblocks.cacheattributes.MaxSpoolPerRun"] == "500"
        assert config["jcs.region.mapblocks.elementattributes"] == "org.apache.commons.jcs3.engine.ElementAttributes"
        assert config["jcs.region.mapblocks.elementattributes.IsEternal"] == "true"
        assert config["jcs.region.mapblocks.elementattributes.MaxLife"] == "3600"
        assert config["jcs.region.mapblocks.elementattributes.IsSpool"] == "true"
        assert config["jcs.region.mapblocks.elementattributes.IsLateral"] == "true"
        assert config["jcs.region.mapblocks.elementattributes.IsRemote"] == "false"
        // aux file
        assert config["jcs.auxiliary.mapblocks_file"] == "org.apache.commons.jcs3.auxiliary.disk.indexed.IndexedDiskCacheFactory"
        assert config["jcs.auxiliary.mapblocks_file.attributes"] == "org.apache.commons.jcs3.auxiliary.disk.indexed.IndexedDiskCacheAttributes"
        assert config["jcs.auxiliary.mapblocks_file.attributes.DiskPath"] == "/path/jcs_swap_mapblocks_file"
        assert config["jcs.auxiliary.mapblocks_file.attributes.MaxPurgatorySize"] == "10000"
        assert config["jcs.auxiliary.mapblocks_file.attributes.MaxKeySize"] == "10000"
        assert config["jcs.auxiliary.mapblocks_file.attributes.OptimizeAtRemoveCount"] == "300000"
        assert config["jcs.auxiliary.mapblocks_file.attributes.ShutdownSpoolTimeLimit"] == "60"
        assert config["jcs.auxiliary.mapblocks_file.attributes.OptimizeOnShutdown"] == "true"
        assert config["jcs.auxiliary.mapblocks_file.attributes.DiskLimitType"] == "COUNT"
        // objects
        assert config["jcs.region.objects"] == "objects_file"
        assert config["jcs.region.objects.cacheattributes"] == "org.apache.commons.jcs3.engine.CompositeCacheAttributes"
        assert config["jcs.region.objects.cacheattributes.MaxObjects"] == "500"
        assert config["jcs.region.objects.cacheattributes.MemoryCacheName"] == "org.apache.commons.jcs3.engine.memory.lru.LRUMemoryCache"
        assert config["jcs.region.objects.cacheattributes.UseMemoryShrinker"] == "false"
        assert config["jcs.region.objects.cacheattributes.ShrinkerIntervalSeconds"] == "30"
        assert config["jcs.region.objects.cacheattributes.MaxMemoryIdleTimeSeconds"] == "60"
        assert config["jcs.region.objects.cacheattributes.MaxSpoolPerRun"] == "500"
        assert config["jcs.region.objects.elementattributes"] == "org.apache.commons.jcs3.engine.ElementAttributes"
        assert config["jcs.region.objects.elementattributes.IsEternal"] == "true"
        assert config["jcs.region.objects.elementattributes.MaxLife"] == "3600"
        assert config["jcs.region.objects.elementattributes.IsSpool"] == "true"
        assert config["jcs.region.objects.elementattributes.IsLateral"] == "true"
        assert config["jcs.region.objects.elementattributes.IsRemote"] == "false"
        // aux file
        assert config["jcs.auxiliary.objects_file"] == "org.apache.commons.jcs3.auxiliary.disk.indexed.IndexedDiskCacheFactory"
        assert config["jcs.auxiliary.objects_file.attributes"] == "org.apache.commons.jcs3.auxiliary.disk.indexed.IndexedDiskCacheAttributes"
        assert config["jcs.auxiliary.objects_file.attributes.DiskPath"] == "/path/jcs_swap_objects_file"
        assert config["jcs.auxiliary.objects_file.attributes.MaxPurgatorySize"] == "500"
        assert config["jcs.auxiliary.objects_file.attributes.MaxKeySize"] == "500"
        assert config["jcs.auxiliary.objects_file.attributes.OptimizeAtRemoveCount"] == "300000"
        assert config["jcs.auxiliary.objects_file.attributes.ShutdownSpoolTimeLimit"] == "60"
        assert config["jcs.auxiliary.objects_file.attributes.OptimizeOnShutdown"] == "true"
        assert config["jcs.auxiliary.objects_file.attributes.DiskLimitType"] == "COUNT"
    }
}
