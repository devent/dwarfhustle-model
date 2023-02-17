package com.anrisoftware.dwarfhustle.model.db.cache

import org.apache.commons.jcs3.JCS

import com.anrisoftware.dwarfhustle.model.api.objects.GameMap

/**
 * Creates application caches configuration based on parameters.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
class AppCachesConfig {

    /**
     * Creates the caches configuration.
     */
    def create(File gamedir, GameMap gm) {
        def config = new Properties()
        def params = [mapblocks: [:], objects: [:]]
        params.mapblocks.cache_name = "mapblocks_${gm.mapid}"
        params.mapblocks.max_objects = gm.blocksCount
        params.mapblocks.is_eternal = true
        params.mapblocks.max_key_size = gm.blocksCount
        params.mapblocks.have_file_aux = true
        params.mapblocks.parent_dir = gamedir
        params.objects.cache_name = "objects"
        params.objects.max_objects = 10000
        params.objects.is_eternal = false
        params.objects.max_idle = 10
        params.objects.max_life = 5
        params.objects.max_key_size = 10000
        params.objects.have_file_aux = true
        params.objects.parent_dir = gamedir
        JcsCacheConfig.createCaches(config, params)
        JCS.setConfigProperties(config);
    }
}
