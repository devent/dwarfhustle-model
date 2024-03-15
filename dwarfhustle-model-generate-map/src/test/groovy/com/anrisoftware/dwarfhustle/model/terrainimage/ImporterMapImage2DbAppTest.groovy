/*
 * dwarfhustle-model-generate-map - Manages the compile dependencies for the model.
 * Copyright © 2023 Erwin Müller (erwin.mueller@anrisoftware.com)
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
package com.anrisoftware.dwarfhustle.model.terrainimage

import static java.time.Duration.ofSeconds

import java.util.concurrent.TimeUnit
import java.util.function.Consumer

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.io.TempDir

import com.anrisoftware.dwarfhustle.model.actor.ActorSystemProvider
import com.anrisoftware.dwarfhustle.model.actor.DwarfhustleModelActorsModule
import com.anrisoftware.dwarfhustle.model.api.objects.DwarfhustleModelApiObjectsModule
import com.anrisoftware.dwarfhustle.model.api.objects.GameMap
import com.anrisoftware.dwarfhustle.model.api.objects.WorldMap
import com.anrisoftware.dwarfhustle.model.db.cache.CacheGetMessage
import com.anrisoftware.dwarfhustle.model.db.cache.CacheGetMessage.CacheGetSuccessMessage
import com.anrisoftware.dwarfhustle.model.db.cache.StoredObjectsJcsCacheActor
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DwarfhustleModelDbOrientdbModule
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.DwarfhustlePowerloomModule
import com.google.inject.Guice
import com.google.inject.Injector

/**
 * @see ImporterMapImage2DbApp
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
class ImporterMapImage2DbAppTest {

    static Injector injector

    @BeforeAll
    static void setupActor() {
        this.injector = Guice.createInjector(
                new DwarfhustleModelActorsModule(),
                new DwarfhustlePowerloomModule(),
                new DwarfhustleModelDbOrientdbModule(),
                new DwarfhustleModelApiObjectsModule(),
                new DwarfhustleModelTerrainimageModule(),
                )
    }

    @Test
    @Timeout(60)
    void test_create_game_map(@TempDir File tmp) {
        def importer = injector.getInstance(ImporterMapImage2DbApp)
        importer.initEmbedded(injector, tmp, "test", "root", "admin").get()
        long gmid = importer.createGameMap(TerrainImage.terrain_32_32_32.terrain)
        def actor = injector.getInstance(ActorSystemProvider)
        actor.getActorAsync(StoredObjectsJcsCacheActor.ID).get()
        cacheGet actor, GameMap, GameMap.OBJECT_TYPE, gmid, { gm ->
            assert gm.id == gmid
            cacheGet actor, WorldMap, WorldMap.OBJECT_TYPE, gm.world, { wm ->
                assert wm.id == gm.world
                assert gmid == wm.currentMap
            }
        }
    }

    void cacheGet(ActorSystemProvider actor, def type, def otype, long key, Consumer consumer) {
        CacheGetMessage.askCacheGet(actor.actorSystem, type, otype, key, ofSeconds(1)).whenComplete({ ret, ex ->
            assert ex == null
            assert ret.class == CacheGetSuccessMessage
            consumer.accept(ret.go)
        }).get()
    }

    /**
     * chunksCount = 9
     * blocksCount = 64
     */
    @Test
    @Timeout(600)
    void test_start_import_4_4_4(@TempDir File tmp) {
        def image = TerrainImage.terrain_4_4_4
        def importer = injector.getInstance(ImporterMapImage2DbApp)
        importer.initEmbedded(injector, tmp, image.name(), "root", "admin").get()
        long gmid = importer.createGameMap(image.terrain)
        importer.startImport(ImporterMapImage2DbAppTest.class.getResource(image.imageName), image.terrain, gmid)
        def actor = injector.getInstance(ActorSystemProvider)
        importer.shutdownEmbedded().get()
        println "done"
    }

    /**
     * chunksCount = 73
     * blocksCount = 512
     */
    @Test
    @Timeout(600)
    void test_start_import_8_8_8(@TempDir File tmp) {
        def image = TerrainImage.terrain_8_8_8
        def importer = injector.getInstance(ImporterMapImage2DbApp)
        importer.initEmbedded(injector, tmp, image.name(), "root", "admin").get()
        long gmid = importer.createGameMap(image.terrain)
        importer.startImport(ImporterMapImage2DbAppTest.class.getResource(image.imageName), image.terrain, gmid)
        def actor = injector.getInstance(ActorSystemProvider)
        importer.shutdownEmbedded().get()
        println "done"
    }

    /**
     * chunksCount = 585
     * blocksCount = 32768
     */
    @Test
    @Timeout(600)
    void test_start_import_32_32_32(@TempDir File tmp) {
        def image = TerrainImage.terrain_32_32_32
        def importer = injector.getInstance(ImporterMapImage2DbApp)
        importer.initEmbedded(injector, tmp, image.name(), "root", "admin").get()
        long gmid = importer.createGameMap(image.terrain)
        importer.startImport(ImporterMapImage2DbAppTest.class.getResource(image.imageName), image.terrain, gmid)
        def actor = injector.getInstance(ActorSystemProvider)
        importer.shutdownEmbedded().get()
        println "done"
    }

    /**
     * chunksCount = 585
     * blocksCount = 2097152
     */
    @Test
    @Timeout(value = 10, unit = TimeUnit.MINUTES)
    void test_start_import_128_128_128() {
        def image = TerrainImage.terrain_128_128_128
        def importer = injector.getInstance(ImporterMapImage2DbApp)
        def out = new File("db", image.name())
        assert out.deleteDir()
        importer.initEmbedded(injector, out, image.name(), "root", "admin").get()
        long gmid = importer.createGameMap(image.terrain)
        importer.startImport(ImporterMapImage2DbAppTest.class.getResource(image.imageName), image.terrain, gmid)
        def actor = injector.getInstance(ActorSystemProvider)
        importer.shutdownEmbedded().get()
        println "done in ${out}"
    }

    /**
     * chunksCount = 585
     * blocksCount = 8388608
     */
    @Test
    @Timeout(value = 10, unit = TimeUnit.MINUTES)
    void test_start_import_256_256_128() {
        def image = TerrainImage.terrain_256_256_128
        def importer = injector.getInstance(ImporterMapImage2DbApp)
        def out = new File("db", image.name())
        assert out.deleteDir()
        importer.initEmbedded(injector, out, image.name(), "root", "admin").get()
        long gmid = importer.createGameMap(image.terrain)
        importer.startImport(ImporterMapImage2DbAppTest.class.getResource(image.imageName), image.terrain, gmid)
        def actor = injector.getInstance(ActorSystemProvider)
        importer.shutdownEmbedded().get()
        println "done in ${out}"
    }
}
