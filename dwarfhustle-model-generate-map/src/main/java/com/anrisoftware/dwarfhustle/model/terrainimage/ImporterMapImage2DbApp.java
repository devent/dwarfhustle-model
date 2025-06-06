/*
 * dwarfhustle-model-generate-map - Manages the compile dependencies for the model.
 * Copyright © 2022-2025 Erwin Müller (erwin.mueller@anrisoftware.com)
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
package com.anrisoftware.dwarfhustle.model.terrainimage;

import static com.anrisoftware.dwarfhustle.model.db.cache.CachePutMessage.askCachePut;
import static com.anrisoftware.dwarfhustle.model.terrainimage.ImportImageMessage.askImportImage;
import static java.lang.Math.pow;
import static java.time.Duration.ofSeconds;
import static java.util.concurrent.CompletableFuture.supplyAsync;

import java.io.File;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;

import org.lable.oss.uniqueid.GeneratorException;
import org.lable.oss.uniqueid.IDGenerator;

import com.anrisoftware.dwarfhustle.model.actor.ActorSystemProvider;
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.actor.ShutdownMessage;
import com.anrisoftware.dwarfhustle.model.api.objects.GameMap;
import com.anrisoftware.dwarfhustle.model.api.objects.IdsObjectsProvider.IdsObjects;
import com.anrisoftware.dwarfhustle.model.api.objects.MapArea;
import com.anrisoftware.dwarfhustle.model.api.objects.ObjectsGetter;
import com.anrisoftware.dwarfhustle.model.api.objects.ObjectsSetter;
import com.anrisoftware.dwarfhustle.model.api.objects.StringObject;
import com.anrisoftware.dwarfhustle.model.api.objects.WorldMap;
import com.anrisoftware.dwarfhustle.model.db.buffers.GameMapBuffer;
import com.anrisoftware.dwarfhustle.model.db.buffers.WorldMapBuffer;
import com.anrisoftware.dwarfhustle.model.db.cache.MapObjectsJcsCacheActor;
import com.anrisoftware.dwarfhustle.model.db.cache.StoredObjectsJcsCacheActor;
import com.anrisoftware.dwarfhustle.model.db.lmbd.GameObjectsLmbdStorage;
import com.anrisoftware.dwarfhustle.model.db.lmbd.GameObjectsLmbdStorage.GameObjectsLmbdStorageFactory;
import com.anrisoftware.dwarfhustle.model.db.lmbd.MapObjectsLmbdStorage;
import com.anrisoftware.dwarfhustle.model.db.lmbd.MapObjectsLmbdStorage.MapObjectsLmbdStorageFactory;
import com.anrisoftware.dwarfhustle.model.db.strings.StringsLuceneStorage.StringsLuceneStorageFactory;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.KnowledgeJcsCacheActor;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.PowerLoomKnowledgeActor;
import com.anrisoftware.dwarfhustle.model.terrainimage.ImportImageMessage.ImportImageErrorMessage;
import com.anrisoftware.dwarfhustle.model.terrainimage.ImportImageMessage.ImportImageSuccessMessage;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;

import akka.Done;
import akka.actor.typed.ActorRef;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

/**
 * Imports a map from an image file to the database.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
public class ImporterMapImage2DbApp {

    private static final Duration IMPORT_IMAGE_TIMEOUT = Duration.ofMinutes(20);

    @Inject
    private ActorSystemProvider actor;

    @Inject
    @IdsObjects
    private IDGenerator gen;

    @Inject
    private GameObjectsLmbdStorageFactory goStorageFactory;

    @Inject
    private MapObjectsLmbdStorageFactory moStorageFactory;

    @Inject
    private StringsLuceneStorageFactory soStorageFactory;

    private GameObjectsLmbdStorage gameObjectsStorage;

    private MapObjectsLmbdStorage mapObjectsStorage;

    private ActorRef<Message> importActor;

    private ActorRef<Message> cacheActor;

    /**
     * Initiate the importer.
     *
     * @param injector the {@link Injector} with external modules.
     */
    public CompletableFuture<Void> init(Injector injector, File root, WorldMap wm, GameMap gm,
            Iterable<StringObject> strings) {
        initStorage(root, wm, gm, strings);
        var childInjector = injector.createChildInjector(new AbstractModule() {
            @Override
            protected void configure() {
            }

        });
        return CompletableFuture.allOf( //
                createObjectsCacheStage(injector).thenAccept(a -> {
                    this.cacheActor = a;
                    createMapObjectsCacheWait(injector, mapObjectsStorage, mapObjectsStorage);
                    createKnowledgeWait(injector, wm, gm);
                    cacheMapWait(wm, gm);
                }).toCompletableFuture(), //
                createImporter(childInjector).toCompletableFuture()).toCompletableFuture()
                .exceptionally(this::errorInit);
    }

    @SneakyThrows
    private void createKnowledgeWait(Injector injector, WorldMap wm, GameMap gm) {
        createKnowledgeCache(injector, actor).thenAccept((aa) -> {
            createPowerLoomWait(injector);
        }).toCompletableFuture().get();
    }

    @SneakyThrows
    private void createPowerLoomWait(Injector injector) {
        createPowerLoom(injector, actor).toCompletableFuture().get();
    }

    private CompletionStage<ActorRef<Message>> createObjectsCacheStage(Injector injector) {
        return createObjectsCache(injector, supplyAsync(() -> gameObjectsStorage),
                supplyAsync(() -> gameObjectsStorage));
    }

    private static CompletionStage<ActorRef<Message>> createObjectsCache(Injector injector,
            CompletionStage<ObjectsGetter> og, CompletionStage<ObjectsSetter> os) {
        var task = StoredObjectsJcsCacheActor.create(injector, Duration.ofSeconds(30), og, os);
        return task.whenComplete((ret, ex) -> {
            if (ex != null) {
                log.error("ObjectsJcsCacheActor.create", ex);
            } else {
                log.debug("ObjectsJcsCacheActor created");
            }
        });
    }

    private static CompletionStage<ActorRef<Message>> createPowerLoom(Injector injector, ActorSystemProvider actor) {
        return PowerLoomKnowledgeActor.create(injector, ofSeconds(1), actor.getActorAsync(KnowledgeJcsCacheActor.ID),
                actor.getObjectGetterAsync(KnowledgeJcsCacheActor.ID)).whenComplete((ret, ex) -> {
                    if (ex != null) {
                        log.error("PowerLoomKnowledgeActor.create", ex);
                    } else {
                        log.debug("PowerLoomKnowledgeActor created");
                    }
                });
    }

    private static CompletionStage<ActorRef<Message>> createKnowledgeCache(Injector injector,
            ActorSystemProvider actor) {
        return KnowledgeJcsCacheActor.create(injector, ofSeconds(10)).whenComplete((ret, ex) -> {
            if (ex != null) {
                log.error("KnowledgeJcsCacheActor.create", ex);
            } else {
                log.debug("KnowledgeJcsCacheActor created");
            }
        });
    }

    @SneakyThrows
    private static void createMapObjectsCacheWait(Injector injector, ObjectsGetter mg, ObjectsSetter ms) {
        CompletionStage<ActorRef<Message>> res = MapObjectsJcsCacheActor.create(injector, ofSeconds(1), mg, ms)
                .whenComplete((ret, ex) -> {
                    if (ex != null) {
                        log.error("MapObjectsJcsCacheActor.create", ex);
                    } else {
                        log.debug("MapObjectsJcsCacheActor created");
                    }
                });
        res.toCompletableFuture().get();
    }

    @SneakyThrows
    private void cacheMapWait(WorldMap wm, GameMap gm) {
        askCachePut(cacheActor, actor.getScheduler(), ofSeconds(1), gm).toCompletableFuture().get();
        askCachePut(cacheActor, actor.getScheduler(), ofSeconds(1), wm).toCompletableFuture().get();
    }

    /**
     * Init storages.
     */
    private void initStorage(File root, WorldMap wm, GameMap gm, Iterable<StringObject> strings) {
        var gameObjectsPath = new File(root, "objects");
        if (!gameObjectsPath.isDirectory()) {
            gameObjectsPath.mkdir();
        }
        final long mapSize = 200 * (long) pow(10, 6);
        this.gameObjectsStorage = goStorageFactory.create(gameObjectsPath.toPath(), mapSize);
        var mapObjectsPath = new File(root, "map-" + gm.getId());
        if (!mapObjectsPath.isDirectory()) {
            mapObjectsPath.mkdir();
        }
        this.mapObjectsStorage = moStorageFactory.create(mapObjectsPath.toPath(), gm, mapSize);
        gameObjectsStorage.putObject(WorldMap.OBJECT_TYPE, wm.getId(), WorldMapBuffer.calcSize(wm),
                b -> WorldMapBuffer.setWorldMap(b, 0, wm));
        gameObjectsStorage.putObject(GameMap.OBJECT_TYPE, gm.getId(), GameMapBuffer.SIZE,
                b -> GameMapBuffer.setGameMap(b, 0, gm));
        var stringsPath = new File(root, "strings");
        if (!stringsPath.isDirectory()) {
            stringsPath.mkdir();
        }
        val soStorage = soStorageFactory.create(stringsPath.toPath());
        soStorage.addObject(strings);
        soStorage.close();
    }

    private CompletionStage<ActorRef<Message>> createImporter(Injector injector) {
        return ImporterMapImage2DbActor
                .create(injector, ofSeconds(1), actor.getObjectGetterAsync(StoredObjectsJcsCacheActor.ID))
                .whenComplete((ret, ex) -> {
                    if (ex != null) {
                        log.error("ImporterMapImage2DbActor.create", ex);
                    } else {
                        this.importActor = ret;
                        log.debug("ImporterMapImage2DbActor created");
                    }
                });
    }

    private Void errorInit(Throwable ex) {
        return logError("Init", ex);
    }

    @SneakyThrows
    private Void logError(String msg, Throwable ex) {
        log.error(msg, ex);
        shutdownImporter().toCompletableFuture().get();
        close();
        return null;
    }

    /**
     * Closes the storages.
     */
    @SneakyThrows
    public void close() {
        shutdownImporter().toCompletableFuture().get();
        gameObjectsStorage.close();
        mapObjectsStorage.close();
    }

    /**
     * Shutdowns the importer.
     */
    @SneakyThrows
    public CompletionStage<Done> shutdownImporter() {
        actor.getMainActor().tell(new ShutdownMessage());
        return actor.shutdown();
    }

    /**
     * Starts the import from the image to the database.
     *
     * @param url   the {@link URL} to the image resource.
     * @param image the {@link TerrainLoadImage} that loads the image.
     * @param root  the {@link File} of the root directory.
     * @param gm    the ID of the {@link GameMap}.
     */
    @SuppressWarnings("unused")
    @SneakyThrows
    public void startImport(URL url, TerrainLoadImage image, File root, long gm) {
        var lock = new CountDownLatch(1);
        var ret = askImportImage(importActor, actor.getScheduler(), IMPORT_IMAGE_TIMEOUT, root.getAbsolutePath(), url,
                image, gm);
        ret.whenComplete((res, ex) -> {
            if (ex != null) {
                logError("ImportImageMessage", ex);
            } else if (res instanceof ImportImageSuccessMessage rm) {
                log.info("ImportImageMessage Success");
            } else if (res instanceof ImportImageErrorMessage rm) {
                log.error("ImportImageMessage Error", rm.e);
            }
            lock.countDown();
        });
        ret.toCompletableFuture().get();
        lock.await();
    }

    /**
     * Creates the {@link GameMap} according the the map properties and terrain
     * image.
     *
     * @param image the {@link TerrainLoadImage}
     * @return the ID of the {@link GameMap}
     */
    public GameMap createGameMap(Properties p, TerrainLoadImage image, int chunksCount, WorldMap wm, long name)
            throws GeneratorException {
        var gm = new GameMap(gen.generate(), image.width, image.height, image.depth);
        wm.addMap(gm);
        wm.setCurrentMap(gm.getId());
        gm.setWorld(wm.getId());
        gm.setChunkSize(image.chunkSize);
        gm.setChunksCount(chunksCount);
        gm.setArea(MapArea.create(50.99819f, 10.98348f, 50.96610f, 11.05610f));
        gm.setTimeZone(ZoneOffset.ofHours(1));
        gm.setCameraPos(0.0f, 0.0f, 83.0f);
        // gm.setCameraPos(0.0f, 0.0f, 12.0f);
        gm.setCameraRot(0.0f, 1.0f, 0.0f, 0.0f);
        gm.setCursorZ(0);
        gm.setName(name);
        // gm.setName(p.getProperty("map_name"));
        return gm;
    }

    /**
     * Creates the {@link WorldMap} map properties.
     */
    public WorldMap createWorldMap(Properties p, long name) throws GeneratorException {
        var wm = new WorldMap(gen.generate());
        wm.setName(name);
        // wm.setName(p.getProperty("world_name"));
        wm.setTime(LocalDateTime.of(2023, Month.APRIL, 15, 12, 0));
        wm.setDistanceLat(100f);
        wm.setDistanceLon(100f);
        return wm;
    }

    /**
     * Creates a {@link StringObject}.
     */
    public StringObject createString(String s) throws GeneratorException {
        var so = new StringObject(gen.generate(), s);
        return so;
    }
}
