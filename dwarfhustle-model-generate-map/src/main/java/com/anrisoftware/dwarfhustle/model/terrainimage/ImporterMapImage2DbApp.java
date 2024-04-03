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
import static com.anrisoftware.dwarfhustle.model.terrainimage.ImporterStartEmbeddedServerMessage.askImporterStartEmbeddedServer;
import static com.anrisoftware.dwarfhustle.model.terrainimage.ImporterStopEmbeddedServerMessage.askImporterStopEmbeddedServer;
import static java.time.Duration.ofSeconds;

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

import org.lable.oss.uniqueid.IDGenerator;

import com.anrisoftware.dwarfhustle.model.actor.ActorSystemProvider;
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.actor.ShutdownMessage;
import com.anrisoftware.dwarfhustle.model.api.objects.GameMap;
import com.anrisoftware.dwarfhustle.model.api.objects.IdsObjectsProvider.IdsObjects;
import com.anrisoftware.dwarfhustle.model.api.objects.MapArea;
import com.anrisoftware.dwarfhustle.model.api.objects.ObjectsGetter;
import com.anrisoftware.dwarfhustle.model.api.objects.WorldMap;
import com.anrisoftware.dwarfhustle.model.db.cache.StoredObjectsJcsCacheActor;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DbMessage.DbErrorMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.OrientDbActor;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.KnowledgeJcsCacheActor;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.PowerLoomKnowledgeActor;
import com.anrisoftware.dwarfhustle.model.terrainimage.ImportImageMessage.ImportImageErrorMessage;
import com.anrisoftware.dwarfhustle.model.terrainimage.ImportImageMessage.ImportImageSuccessMessage;
import com.anrisoftware.dwarfhustle.model.terrainimage.ImporterStartEmbeddedServerMessage.ImporterStartEmbeddedServerSuccessMessage;
import com.anrisoftware.dwarfhustle.model.terrainimage.ImporterStopEmbeddedServerMessage.ImporterStopEmbeddedServerSuccessMessage;
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

    private static final Duration START_EMBEDDED_TIMEOUT = Duration.ofMinutes(10);

    private static final Duration IMPORT_IMAGE_TIMEOUT = Duration.ofMinutes(30);

    @Inject
    private ActorSystemProvider actor;

    @Inject
    @IdsObjects
    private IDGenerator gen;

    /**
     * Initiate the importer.
     *
     * @param injector the {@link Injector} with external modules.
     */
    public CompletableFuture<Void> init(Injector injector) {
        var childInjector = injector.createChildInjector(new AbstractModule() {
            @Override
            protected void configure() {
            }

        });
        return CompletableFuture.allOf( //
                createDb(injector).thenAccept((adb) -> { //
                    createObjectsCache(injector, actor.getObjectGetterAsync(OrientDbActor.ID)).thenAccept((a) -> {
                        createPowerLoom(injector, actor).thenAccept((aa) -> {
                            createKnowledgeCache(injector, actor, aa).whenComplete((ret, ex) -> {
                            });
                        });
                    });
                }).toCompletableFuture(), //
                createImporter(childInjector).toCompletableFuture()).toCompletableFuture()
                .exceptionally(this::errorInit);
    }

    /**
     * Init with embedded database.
     *
     * @param injector  the {@link Injector} with external modules.
     * @param parentDir the {@link File} of the database parent directory.
     * @param database  the {@link String} database name.
     * @param user      the {@link String} user name.
     * @param password  the {@link String} password.
     */
    public CompletableFuture<Void> initEmbedded(File parentDir, String database, String user, String password) {
        return CompletableFuture.runAsync(() -> {
            connectDbEmbedded(parentDir, database, user, password);
        });
    }

    private CompletionStage<ActorRef<Message>> createImporter(Injector injector) {
        return ImporterMapImage2DbActor
                .create(injector, ofSeconds(1), actor.getObjectGetterAsync(StoredObjectsJcsCacheActor.ID))
                .whenComplete((ret, ex) -> {
                    if (ex != null) {
                        log.error("ImporterMapImage2DbActor.create", ex);
                    } else {
                        log.debug("ImporterMapImage2DbActor created");
                    }
                });
    }

    private static CompletionStage<ActorRef<Message>> createDb(Injector injector) {
        return OrientDbActor.create(injector, ofSeconds(1)).whenComplete((ret, ex) -> {
            if (ex != null) {
                log.error("OrientDbActor.create", ex);
            } else {
                log.debug("OrientDbActor created");
            }
        });
    }

    private static CompletionStage<ActorRef<Message>> createObjectsCache(Injector injector,
            CompletionStage<ObjectsGetter> og) {
        var task = ImporterObjectsJcsCacheActor.create(injector, Duration.ofSeconds(30), og);
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
        return KnowledgeJcsCacheActor
                .create(injector, ofSeconds(10), actor.getObjectGetterAsync(PowerLoomKnowledgeActor.ID))
                .whenComplete((ret, ex) -> {
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
        shutdownEmbedded().toCompletableFuture().get();
        return null;
    }

    @SuppressWarnings("unused")
    @SneakyThrows
    private void connectDbEmbedded(File root, String database, String user, String password) {
        URL config = ImporterMapImage2DbApp.class.getResource("orientdb-config.xml");
        var lock = new CountDownLatch(1);
        String rootPath = root.getAbsolutePath();
        var ret = askImporterStartEmbeddedServer(actor.getActorSystem(), START_EMBEDDED_TIMEOUT, rootPath, config,
                database, user, password);
        ret.whenComplete((res, ex) -> {
            if (ex != null) {
                logError("ImporterStartEmbeddedServerMessage", ex);
            } else if (res instanceof DbErrorMessage<?> rm) {
                logError("ImporterStartEmbeddedServerMessage", ex);
            } else if (res instanceof ImporterStartEmbeddedServerSuccessMessage<?> rm) {
                log.info("ImporterStartEmbeddedServerMessage Success");
                lock.countDown();
            }
        });
        ret.toCompletableFuture().get();
        lock.await();
    }

    /**
     * Shutdowns the importer.
     */
    @SuppressWarnings("unused")
    @SneakyThrows
    public CompletionStage<Done> shutdownEmbedded() {
        var lock = new CountDownLatch(1);
        var ret = askImporterStopEmbeddedServer(actor.getActorSystem(), IMPORT_IMAGE_TIMEOUT);
        ret.whenComplete((res, ex) -> {
            if (ex != null) {
                logError("ImporterStopEmbeddedServerMessage", ex);
            } else if (res instanceof DbErrorMessage<?> rm) {
                logError("ImporterStopEmbeddedServerMessage", ex);
            } else if (res instanceof ImporterStopEmbeddedServerSuccessMessage<?> rm) {
                log.info("ImporterStopEmbeddedServerMessage Success");
                lock.countDown();
            }
        });
        ret.toCompletableFuture().get();
        lock.await();
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
    public void startImport(URL url, TerrainLoadImage image, long map) {
        var lock = new CountDownLatch(1);
        var ret = askImportImage(actor.getActorSystem(), IMPORT_IMAGE_TIMEOUT, url, image, map);
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
     * Creates the {@link WorldMap} and {@link GameMap} according the the terrain
     * image and map properties.
     *
     * @param image the {@link TerrainLoadImage}
     * @return the ID of the {@link GameMap}
     */
    @SneakyThrows
    public long createGameMap(TerrainLoadImage image, Properties mapProperties, int chunksCount) {
        var gm = new GameMap(gen.generate());
        var wm = new WorldMap(gen.generate());
        wm.setName(mapProperties.getProperty("world_name"));
        wm.addMap(gm);
        wm.currentMap = gm.id;
        wm.time = LocalDateTime.of(2023, Month.APRIL, 15, 12, 0);
        wm.distanceLat = 100f;
        wm.distanceLon = 100f;
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
        gm.setName(mapProperties.getProperty("map_name"));
        askCachePut(actor.getActorSystem(), ofSeconds(333331), gm.id, gm).toCompletableFuture().get();
        askCachePut(actor.getActorSystem(), ofSeconds(333331), wm.id, wm).toCompletableFuture().get();
        return gm.id;
    }
}
