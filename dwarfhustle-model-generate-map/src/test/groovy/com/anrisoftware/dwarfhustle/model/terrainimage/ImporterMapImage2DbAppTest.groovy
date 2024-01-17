package com.anrisoftware.dwarfhustle.model.terrainimage

import static java.time.Duration.ofSeconds

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
import com.anrisoftware.dwarfhustle.model.db.cache.DwarfhustleModelDbCacheModule
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
                new DwarfhustleModelDbCacheModule(),
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

    @Test
    @Timeout(600)
    void test_start_import(@TempDir File tmp) {
        def importer = injector.getInstance(ImporterMapImage2DbApp)
        importer.initEmbedded(injector, tmp, "test", "root", "admin").get()
        def image = TerrainImage.terrain_8_8_8
        long gmid = importer.createGameMap(image.terrain)
        importer.startImport(ImporterMapImage2DbAppTest.class.getResource(image.name), image.terrain, gmid)
        def actor = injector.getInstance(ActorSystemProvider)
        importer.shutdownEmbedded().get()
        println "done"
    }
}
