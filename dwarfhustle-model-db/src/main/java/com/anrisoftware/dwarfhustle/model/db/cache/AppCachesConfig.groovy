package com.anrisoftware.dwarfhustle.model.db.cache

import org.apache.commons.jcs3.JCS

/**
 * Creates application caches configuration based on parameters.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
class AppCachesConfig {

    /**
     * Creates the caches configuration.
     */
    def create(File parentDir) {
        def config = new Properties()
        def params = [objects: [:], knowledge: [:], assets: [:]]
        params.objects.cache_name = "objects"
        params.objects.max_objects = 10000
        params.objects.is_eternal = false
        params.objects.max_idle = 10
        params.objects.max_life = 5
        params.objects.max_key_size = 10000
        params.objects.have_file_aux = true
        params.objects.parent_dir = parentDir
        //
        params.knowledge.cache_name = "knowledge"
        params.knowledge.max_objects = 1000
        params.knowledge.is_eternal = false
        params.knowledge.max_idle = 10
        params.knowledge.max_life = 5
        params.knowledge.max_key_size = 1000
        params.knowledge.have_file_aux = true
        params.knowledge.parent_dir = parentDir
        //
        params.assets.cache_name = "assets"
        params.assets.max_objects = 1000
        params.assets.is_eternal = false
        params.assets.max_idle = 10
        params.assets.max_life = 5
        params.assets.max_key_size = 1000
        params.assets.have_file_aux = true
        params.assets.parent_dir = parentDir
        JcsCacheConfig.createCaches(config, params)
        JCS.setConfigProperties(config);
    }
}
