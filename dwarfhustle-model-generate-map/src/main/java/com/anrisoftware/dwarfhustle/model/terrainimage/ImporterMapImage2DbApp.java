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
package com.anrisoftware.dwarfhustle.model.terrainimage;

import static com.anrisoftware.dwarfhustle.model.db.cache.CachePutMessage.askCachePut;
import static com.anrisoftware.dwarfhustle.model.terrainimage.ImportImageMessage.askImportImage;
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
import com.anrisoftware.dwarfhustle.model.api.objects.WorldMap;
import com.anrisoftware.dwarfhustle.model.db.cache.StoredObjectsJcsCacheActor;
import com.anrisoftware.dwarfhustle.model.db.lmbd.GameObjectsLmbdStorage;
import com.anrisoftware.dwarfhustle.model.db.lmbd.MapObjectsLmbdStorage;
import com.anrisoftware.dwarfhustle.model.db.lmbd.ObjectTypesProvider;
import com.anrisoftware.dwarfhustle.model.db.lmbd.TypeReadBuffers;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DbMessage.DbErrorMessage;
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
import lombok.extern.slf4j.Slf4j;

/**
 * Imports a map from an image file to the database.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
public class ImporterMapImage2DbApp {

    private static final Duration IMPORT_IMAGE_TIMEOUT = Duration.ofMinutes(10);

    @Inject
    private ActorSystemProvider actor;

    @Inject
    @IdsObjects
    private IDGenerator gen;

    private GameObjectsLmbdStorage gameObjectsStorage;

    private MapObjectsLmbdStorage mapObjectsStorage;

    private ActorRef<Message> importActor;

    private ActorRef<Message> cacheActor;

    /**
     * Initiate the importer.
     *
     * @param injector the {@link Injector} with external modules.
     */
    public CompletableFuture<Void> init(Injector injector, File root, WorldMap wm, GameMap gm) {
        initStorage(root, gm);
        var childInjector = injector.createChildInjector(new AbstractModule() {
            @Override
            protected void configure() {
            }

        });
        return CompletableFuture.allOf( //
                createObjectsCacheStage(injector).thenAccept((a) -> {
                    this.cacheActor = a;
                    createPowerLoom(injector, wm, gm);
                }).toCompletableFuture(), //
                createImporter(childInjector).toCompletableFuture()).toCompletableFuture()
                .exceptionally(this::errorInit);
    }

    @SneakyThrows
    private void createPowerLoom(Injector injector, WorldMap wm, GameMap gm) {
        createPowerLoom(injector, actor).thenAccept((aa) -> {
            createKnowledgeCache(injector, wm, gm, aa);
        }).toCompletableFuture().get();
    }

    @SneakyThrows
    private void createKnowledgeCache(Injector injector, WorldMap wm, GameMap gm, ActorRef<Message> aa) {
        createKnowledgeCache(injector, actor, aa).thenRunAsync(() -> {
            cacheMap0(wm, gm);
        }).toCompletableFuture().get();
    }

    private CompletionStage<ActorRef<Message>> createObjectsCacheStage(Injector injector) {
        return createObjectsCache(injector, supplyAsync(() -> gameObjectsStorage),
                supplyAsync(() -> gameObjectsStorage));
    }

    @SneakyThrows
    private void cacheMap0(WorldMap wm, GameMap gm) {
        askCachePut(cacheActor, actor.getScheduler(), ofSeconds(1), gm).toCompletableFuture().get();
        askCachePut(cacheActor, actor.getScheduler(), ofSeconds(1), wm).toCompletableFuture().get();
    }

    /**
     * Init storages.
     */
    private void initStorage(File root, GameMap gm) {
        var gameObjectsPath = new File(root, "objects");
        if (!gameObjectsPath.isDirectory()) {
            gameObjectsPath.mkdir();
        }
        this.gameObjectsStorage = new GameObjectsLmbdStorage(gameObjectsPath.toPath(), ObjectTypesProvider.OBJECT_TYPES,
                TypeReadBuffers.TYPE_READ_BUFFERS);
        var mapObjectsPath = new File(root, "map-" + gm.id);
        if (!mapObjectsPath.isDirectory()) {
            mapObjectsPath.mkdir();
        }
        this.mapObjectsStorage = new MapObjectsLmbdStorage(mapObjectsPath.toPath(), gm);
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

    private static CompletionStage<ActorRef<Message>> createObjectsCache(Injector injector,
            CompletionStage<ObjectsGetter> og, CompletionStage<ObjectsSetter> os) {
        var task = ImporterObjectsJcsCacheActor.create(injector, Duration.ofSeconds(30), og, os);
        return task.whenComplete((ret, ex) -> {
            if (ex != null) {
                log.error("ObjectsJcsCacheActor.create", ex);
            } else {
                log.debug("ObjectsJcsCacheActor created");
            }
        });
    }

    private static CompletionStage<ActorRef<Message>> createPowerLoom(Injector injector, ActorSystemProvider actor) {
        return PowerLoomKnowledgeActor
                .create(injector, ofSeconds(1), actor.getActorAsync(StoredObjectsJcsCacheActor.ID))
                .whenComplete((ret, ex) -> {
                    if (ex != null) {
                        log.error("PowerLoomKnowledgeActor.create", ex);
                    } else {
                        log.debug("PowerLoomKnowledgeActor created");
                    }
                });
    }

    private static CompletionStage<ActorRef<Message>> createKnowledgeCache(Injector injector, ActorSystemProvider actor,
            ActorRef<Message> powerLoom) {
        return KnowledgeJcsCacheActor.create(injector, ofSeconds(10)).whenComplete((ret, ex) -> {
            if (ex != null) {
                log.error("KnowledgeJcsCacheActor.create", ex);
            } else {
                log.debug("KnowledgeJcsCacheActor created");
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
        return null;
    }

    /**
     * Shutdowns the importer.
     */
    @SneakyThrows
    public CompletionStage<Done> shutdownImporter() {
        gameObjectsStorage.close();
        mapObjectsStorage.close();
        actor.getMainActor().tell(new ShutdownMessage());
        return actor.shutdown();
    }

    /**
     * Starts the import from the image to the database.
     *
     * @param url   the {@link URL} to the image resource.
     * @param image the {@link TerrainLoadImage} that loads the image.
     * @param map   the ID of the {@link GameMap}.
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
            } else if (res instanceof DbErrorMessage<?> rm) {
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
    public GameMap createGameMap(Properties p, TerrainLoadImage image, int chunksCount, WorldMap wm)
            throws GeneratorException {
        var gm = new GameMap(gen.generate());
        wm.addMap(gm);
        wm.currentMap = gm.id;
        gm.world = wm.id;
        gm.chunkSize = image.chunkSize;
        gm.chunksCount = chunksCount;
        gm.width = image.width;
        gm.height = image.height;
        gm.depth = image.depth;
        gm.area = MapArea.create(50.99819f, 10.98348f, 50.96610f, 11.05610f);
        gm.timeZone = ZoneOffset.ofHours(1);
        gm.setCameraPos(0.0f, 0.0f, 83.0f);
        // gm.setCameraPos(0.0f, 0.0f, 12.0f);
        gm.setCameraRot(0.0f, 1.0f, 0.0f, 0.0f);
        gm.setCursorZ(0);
        gm.setName(p.getProperty("map_name"));
        return gm;
    }

    /**
     * Creates the {@link WorldMap} map properties.
     */
    public WorldMap createWorldMap(Properties p) throws GeneratorException {
        var wm = new WorldMap(gen.generate());
        wm.setName(p.getProperty("world_name"));
        wm.time = LocalDateTime.of(2023, Month.APRIL, 15, 12, 0);
        wm.distanceLat = 100f;
        wm.distanceLon = 100f;
        return wm;
    }
}
