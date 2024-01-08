package com.anrisoftware.dwarfhustle.model.terrainimage

import static java.time.Duration.ofSeconds

import java.util.concurrent.CountDownLatch
import java.util.function.Consumer

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import com.anrisoftware.dwarfhustle.model.actor.ActorSystemProvider
import com.anrisoftware.dwarfhustle.model.actor.DwarfhustleModelActorsModule
import com.anrisoftware.dwarfhustle.model.api.objects.DwarfhustleModelApiObjectsModule
import com.anrisoftware.dwarfhustle.model.api.objects.GameMap
import com.anrisoftware.dwarfhustle.model.api.objects.WorldMap
import com.anrisoftware.dwarfhustle.model.db.cache.CacheGetMessage
import com.anrisoftware.dwarfhustle.model.db.cache.CacheGetMessage.CacheGetSuccessMessage
import com.anrisoftware.dwarfhustle.model.db.cache.DwarfhustleModelDbCacheModule
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DwarfhustleModelDbOrientdbModule
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.DwarfhustlePowerloomModule
import com.google.inject.Guice
import com.google.inject.Injector

/**
 * @see ImportMapImage2Db
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
class ImportMapImage2DbTest {

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
    void test_import_embedded(@TempDir File tmp) {
        def importer = injector.getInstance(ImporterMapImage2Db)
        def latch = importer.initEmbedded(injector, new CountDownLatch(1), tmp)
        long wmid = importer.createGameMap(TerrainImage.terrain_32_32_32.terrain)
        latch.await()
        def actor = injector.getInstance(ActorSystemProvider)
        cacheGet actor, WorldMap, WorldMap.OBJECT_TYPE, wmid, { wm ->
            assert wm.id == wmid
            cacheGet actor, GameMap, GameMap.OBJECT_TYPE, wm.currentMap, {
                assert it.id == wm.currentMap
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
}
