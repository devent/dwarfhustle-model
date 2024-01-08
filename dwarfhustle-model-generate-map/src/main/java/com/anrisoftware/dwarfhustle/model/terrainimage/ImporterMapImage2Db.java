package com.anrisoftware.dwarfhustle.model.terrainimage;

import static java.time.Duration.ofSeconds;
import static java.util.concurrent.CompletableFuture.supplyAsync;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.collections.api.map.primitive.LongObjectMap;
import org.eclipse.collections.impl.factory.primitive.LongObjectMaps;
import org.lable.oss.uniqueid.GeneratorException;
import org.lable.oss.uniqueid.IDGenerator;

import com.anrisoftware.dwarfhustle.model.actor.ActorSystemProvider;
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.actor.ShutdownMessage;
import com.anrisoftware.dwarfhustle.model.api.objects.GameMap;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;
import com.anrisoftware.dwarfhustle.model.api.objects.IdsObjectsProvider;
import com.anrisoftware.dwarfhustle.model.api.objects.IdsObjectsProvider.IdsObjects;
import com.anrisoftware.dwarfhustle.model.api.objects.MapArea;
import com.anrisoftware.dwarfhustle.model.api.objects.ObjectsGetter;
import com.anrisoftware.dwarfhustle.model.api.objects.WorldMap;
import com.anrisoftware.dwarfhustle.model.db.cache.CachePutMessage;
import com.anrisoftware.dwarfhustle.model.db.cache.DwarfhustleModelDbMockCacheModule;
import com.anrisoftware.dwarfhustle.model.db.cache.MockStoredObjectsJcsCacheActor;
import com.anrisoftware.dwarfhustle.model.db.cache.StoredObjectsJcsCacheActor;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DbServerUtils;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DbTestUtils;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.OrientDbActor;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.KnowledgeJcsCacheActor;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.PowerLoomKnowledgeActor;
import com.anrisoftware.dwarfhustle.model.terrainimage.TerrainImageCreateMap.TerrainImageCreateMapFactory;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.orientechnologies.orient.core.db.ODatabaseType;

import akka.actor.typed.ActorRef;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

/**
 * Imports a map from an image file to the database.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
public class ImporterMapImage2Db {

    @Inject
    private ActorSystemProvider actor;

    @Inject
    @IdsObjects
    private IDGenerator gen;

    @Inject
    private TerrainImageCreateMapFactory terrainImageCreateMap;

    private DbServerUtils dbServerUtils;

    private DbTestUtils dbTestUtils;

    private final ObjectsGetter og;

    private final LongObjectMap<GameObject> backendIdsObjects;

    public ImporterMapImage2Db() {
        this.backendIdsObjects = LongObjectMaps.mutable.empty();
        this.og = new ObjectsGetter() {

            @SuppressWarnings("unchecked")
            @Override
            public <T extends GameObject> T get(Class<T> typeClass, String type, Object key)
                    throws ObjectsGetterException {
                return (T) backendIdsObjects.get((long) key);
            }
        };
    }

    public CountDownLatch initEmbedded(Injector injector, CountDownLatch latch, File parentDir)
            throws InterruptedException, ExecutionException, TimeoutException, IOException {
        var childInjector = injector.createChildInjector(new DwarfhustleModelDbMockCacheModule(), new AbstractModule() {
            @Override
            protected void configure() {
            }

            @Provides
            public LongObjectMap<GameObject> getBackendIdsObjects() {
                return backendIdsObjects;
            }
        });
        dbServerUtils = new DbServerUtils();
        dbServerUtils.createServer(parentDir);
        createObjectsCache(childInjector, new CountDownLatch(1)).await();
        createPowerLoom(childInjector, new CountDownLatch(1)).await();
        createDb(childInjector, new CountDownLatch(1)).await();
        connectDb(latch, true);
        return latch;
    }

    private void connectDb(CountDownLatch latch, boolean embedded) {
        if (embedded) {
            dbTestUtils.connectCreateDatabaseEmbedded(dbServerUtils.getServer(), latch);
        } else {
            dbTestUtils.connectCreateDatabaseRemote(latch);
        }
    }

    private CountDownLatch createDb(Injector injector, CountDownLatch latch)
            throws InterruptedException, ExecutionException, TimeoutException {
        OrientDbActor.create(injector, ofSeconds(1)).whenComplete((ret, ex) -> {
            if (ex != null) {
                log.error("OrientDbActor.create", ex);
            } else {
                log.debug("OrientDbActor created");
                latch.countDown();
            }
        });
        latch.await();
        gen = injector.getInstance(IdsObjectsProvider.class).get();
        dbTestUtils = new DbTestUtils(actor.getActor(OrientDbActor.ID), actor.getScheduler(), gen);
        dbTestUtils.setType(ODatabaseType.PLOCAL);
        dbTestUtils.setFillDatabase(false);
        return latch;
    }

    private CountDownLatch createObjectsCache(Injector injector, CountDownLatch latch) {
        var task = MockStoredObjectsJcsCacheActor.create(injector, Duration.ofSeconds(30), supplyAsync(() -> og));
        task.whenComplete((ret, ex) -> {
            if (ex != null) {
                log.error("ObjectsJcsCacheActor.create", ex);
            } else {
                log.debug("ObjectsJcsCacheActor created");
                latch.countDown();
            }
        });
        return latch;
    }

    private CountDownLatch createPowerLoom(Injector injector, CountDownLatch latch) {
        PowerLoomKnowledgeActor.create(injector, ofSeconds(1), actor.getActorAsync(StoredObjectsJcsCacheActor.ID))
                .whenComplete((ret, ex) -> {
                    if (ex != null) {
                        log.error("PowerLoomKnowledgeActor.create", ex);
                    } else {
                        log.debug("PowerLoomKnowledgeActor created");
                        createKnowledgeCache(injector, latch, ret);
                    }
                });
        return latch;
    }

    private CountDownLatch createKnowledgeCache(Injector injector, CountDownLatch latch, ActorRef<Message> powerLoom) {
        KnowledgeJcsCacheActor.create(injector, ofSeconds(10), actor.getObjectsAsync(PowerLoomKnowledgeActor.ID))
                .whenComplete((ret, ex) -> {
                    if (ex != null) {
                        log.error("KnowledgeJcsCacheActor.create", ex);
                    } else {
                        log.debug("KnowledgeJcsCacheActor created");
                        latch.countDown();
                    }
                });
        return latch;
    }

    public CountDownLatch shutdownEmbedded(CountDownLatch latch) {
        shutdown(latch);
        dbServerUtils.shutdownServer();
        return latch;
    }

    private void shutdown(CountDownLatch latch) {
        var closeDatabaseLock = new CountDownLatch(1);
        dbTestUtils.closeDatabase(closeDatabaseLock);
        actor.getMainActor().tell(new ShutdownMessage());
    }

    public void startImport(URL url, TerrainLoadImage image, long mapid)
            throws IOException, GeneratorException {
        terrainImageCreateMap.create(og).startImport(url, image, mapid);
    }

    public long createGameMap(TerrainLoadImage image) throws GeneratorException {
        var gm = new GameMap(gen.generate());
        var wm = new WorldMap(gen.generate());
        wm.addMap(gm);
        wm.currentMap = gm.id;
        wm.time = LocalDateTime.of(2023, Month.APRIL, 15, 12, 0);
        wm.distanceLat = 100f;
        wm.distanceLon = 100f;
        gm.world = wm.id;
        gm.chunkSize = image.chunkSize;
        gm.width = image.width;
        gm.height = image.height;
        gm.depth = image.depth;
        gm.area = MapArea.create(50.99819f, 10.98348f, 50.96610f, 11.05610f);
        gm.timeZone = ZoneOffset.ofHours(1);
        gm.setCameraPos(0.0f, 0.0f, 83.0f);
        // gm.setCameraPos(0.0f, 0.0f, 12.0f);
        gm.setCameraRot(0.0f, 1.0f, 0.0f, 0.0f);
        gm.setCursorZ(0);
        CachePutMessage.askCachePut(actor.getActorSystem(), wm.id, wm, ofSeconds(1));
        CachePutMessage.askCachePut(actor.getActorSystem(), gm.id, gm, ofSeconds(1));
        return wm.id;
    }

}
