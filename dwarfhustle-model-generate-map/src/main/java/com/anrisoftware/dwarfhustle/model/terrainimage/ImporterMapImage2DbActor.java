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

import static com.anrisoftware.dwarfhustle.model.actor.CreateActorMessage.createNamedActor;
import static com.anrisoftware.dwarfhustle.model.api.objects.GameMap.getGameMap;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.actor.StopEmbeddedServerMessage.askStopEmbeddedServer;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.CompletionStage;

import org.lable.oss.uniqueid.GeneratorException;
import org.lable.oss.uniqueid.IDGenerator;

import com.anrisoftware.dwarfhustle.model.actor.ActorSystemProvider;
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.actor.ShutdownMessage;
import com.anrisoftware.dwarfhustle.model.api.objects.IdsObjectsProvider.IdsObjects;
import com.anrisoftware.dwarfhustle.model.api.objects.MapChunksStore;
import com.anrisoftware.dwarfhustle.model.api.objects.ObjectsGetter;
import com.anrisoftware.dwarfhustle.model.db.cache.AbstractJcsCacheActor;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.CloseDbMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.CloseDbMessage.CloseDbSuccessMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.ConnectDbEmbeddedMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.ConnectDbSuccessMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.CreateDbMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.CreateDbMessage.CreateDbSuccessMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.CreateSchemasMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.CreateSchemasMessage.CreateSchemasSuccessMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DbMessage.DbErrorMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DbMessage.DbResponseMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DbMessage.DbSuccessMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.OrientDbActor;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.RebuildIndexMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.RebuildIndexMessage.RebuildIndexSuccessMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.StartEmbeddedServerMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.StartEmbeddedServerMessage.StartEmbeddedServerSuccessMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.StopEmbeddedServerMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.StopEmbeddedServerMessage.StopEmbeddedServerSuccessMessage;
import com.anrisoftware.dwarfhustle.model.terrainimage.ImportImageMessage.ImportImageSuccessMessage;
import com.anrisoftware.dwarfhustle.model.terrainimage.ImporterStartEmbeddedServerMessage.ImporterStartEmbeddedServerSuccessMessage;
import com.anrisoftware.dwarfhustle.model.terrainimage.ImporterStopEmbeddedServerMessage.ImporterStopEmbeddedServerSuccessMessage;
import com.anrisoftware.dwarfhustle.model.terrainimage.TerrainImageCreateMap.TerrainImageCreateMapFactory;
import com.google.inject.Injector;
import com.google.inject.assistedinject.Assisted;
import com.orientechnologies.orient.core.db.ODatabaseType;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.BehaviorBuilder;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.receptionist.ServiceKey;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * Imports a map from an image file to the database.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
public class ImporterMapImage2DbActor {

    public static final ServiceKey<Message> KEY = ServiceKey.create(Message.class,
            ImporterMapImage2DbActor.class.getSimpleName());

    public static final String NAME = ImporterMapImage2DbActor.class.getSimpleName();

    public static final int ID = KEY.hashCode();

    @RequiredArgsConstructor
    @ToString(callSuper = true)
    private static class WrappedDbResponse extends Message {
        private final DbResponseMessage<?> response;
    }

    /**
     * Factory to create {@link AbstractJcsCacheActor}.
     *
     * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
     */
    public interface ImporterMapImage2DbActorFactory {

        ImporterMapImage2DbActor create(ActorContext<Message> context, ActorRef<Message> dbActor, ObjectsGetter og);
    }

    public static Behavior<Message> create(Injector injector, ActorSystemProvider actor,
            ImporterMapImage2DbActorFactory actorFactory, CompletionStage<ObjectsGetter> og) {
        return Behaviors.setup(context -> {
            return actorFactory
                    .create(context, actor.getActorAsync(OrientDbActor.ID).toCompletableFuture().get(15, SECONDS),
                            og.toCompletableFuture().get(15, SECONDS))
                    .start();
        });
    }

    /**
     * Creates the {@link ImporterMapImage2DbActor}.
     *
     * @param injector the {@link Injector} injector.
     * @param timeout  the {@link Duration} timeout.
     */
    public static CompletionStage<ActorRef<Message>> create(Injector injector, Duration timeout,
            CompletionStage<ObjectsGetter> og) {
        var actor = injector.getInstance(ActorSystemProvider.class);
        var actorFactory = injector.getInstance(ImporterMapImage2DbActorFactory.class);
        return createNamedActor(actor.getActorSystem(), timeout, ID, KEY, NAME,
                create(injector, actor, actorFactory, og));
    }

    @Inject
    @Assisted
    private ActorContext<Message> context;

    @Inject
    @Assisted
    private ActorRef<Message> dbActor;

    @Inject
    @Assisted
    private ObjectsGetter og;

    @Inject
    private ActorSystemProvider actor;

    @Inject
    @IdsObjects
    private IDGenerator gen;

    @Inject
    private TerrainImageCreateMapFactory terrainImageCreateMap;

    @SuppressWarnings("rawtypes")
    private ActorRef<DbResponseMessage> dbResponseAdapter;

    private String database;

    private String user;

    private String password;

    private Runnable stopServerMessage = () -> {
    };

    @SuppressWarnings("rawtypes")
    private ActorRef startServerReplyTo;

    @SuppressWarnings("rawtypes")
    private ActorRef stopServerReplyTo;

    @SuppressWarnings("rawtypes")
    private ActorRef importImageReplyTo;

    private String root;

    /**
     * @see #getInitialBehavior()
     */
    public Behavior<Message> start() {
        this.dbResponseAdapter = context.messageAdapter(DbResponseMessage.class, WrappedDbResponse::new);
        return getInitialBehavior().build();
    }

    @SuppressWarnings({ "rawtypes" })
    private Behavior<Message> onImporterStartEmbeddedServer(ImporterStartEmbeddedServerMessage m) {
        log.debug("onImporterStartEmbeddedServer {}", m);
        this.database = m.database;
        this.user = m.user;
        this.password = m.password;
        this.root = m.root;
        dbActor.tell(new StartEmbeddedServerMessage<>(dbResponseAdapter, m.root, m.config));
        this.startServerReplyTo = m.replyTo;
        return Behaviors.same();
    }

    @SuppressWarnings({ "rawtypes" })
    private Behavior<Message> onImporterStopEmbeddedServer(ImporterStopEmbeddedServerMessage m) {
        log.debug("onImporterStopEmbeddedServer {}", m);
        this.stopServerReplyTo = m.replyTo;
        dbActor.tell(new CloseDbMessage<>(dbResponseAdapter));
        return getInitialBehavior().build();
    }

    private Behavior<Message> onImportImage(ImportImageMessage<?> m) {
        log.debug("onImportImage {}", m);
        this.importImageReplyTo = m.replyTo;
        try {
            var gm = getGameMap(og, m.mapid);
            var chunksStore = new MapChunksStore(Path.of(root, String.format("%4d.map", m.mapid)), gm.chunkSize);
            terrainImageCreateMap.create(chunksStore).startImport(m.url, m.image, gm);
            dbActor.tell(new RebuildIndexMessage<>(dbResponseAdapter));
        } catch (IOException | GeneratorException e) {
            log.error("onImportImage", e);
            return Behaviors.stopped();
        }
        return Behaviors.same();
    }

    /**
     * <ul>
     * <li>
     * </ul>
     */
    @SuppressWarnings("unchecked")
    private Behavior<Message> onWrappedDbResponse(WrappedDbResponse m) {
        var r = m.response;
        if (r instanceof DbErrorMessage<?> rm) {
            log.error("onWrappedDbResponse", rm.error);
            return Behaviors.stopped();
        } else if (r instanceof StartEmbeddedServerSuccessMessage rm) {
            this.stopServerMessage = () -> askStopEmbeddedServer(actor.getActorSystem(), Duration.ofSeconds(30));
            dbActor.tell(new ConnectDbEmbeddedMessage<>(dbResponseAdapter, rm.server, database, user, password));
        } else if (r instanceof ConnectDbSuccessMessage<?> rm) {
            dbActor.tell(new CreateDbMessage<>(dbResponseAdapter, ODatabaseType.PLOCAL));
        } else if (r instanceof CreateDbSuccessMessage rm) {
            dbActor.tell(new CreateSchemasMessage<>(dbResponseAdapter));
        } else if (r instanceof CreateSchemasSuccessMessage rm) {
            startServerReplyTo.tell(new ImporterStartEmbeddedServerSuccessMessage<>());
            return getServerStartedBehavior().build();
        } else if (r instanceof RebuildIndexSuccessMessage rm) {
            importImageReplyTo.tell(new ImportImageSuccessMessage<>());
        } else if (r instanceof CloseDbSuccessMessage rm) {
            dbActor.tell(new StopEmbeddedServerMessage<>(dbResponseAdapter));
        } else if (r instanceof StopEmbeddedServerSuccessMessage rm) {
            stopServerReplyTo.tell(new ImporterStopEmbeddedServerSuccessMessage<>());
        } else if (r instanceof DbSuccessMessage<?> rm) {
            log.info("onWrappedDbResponse", m);
        }
        return Behaviors.same();
    }

    /**
     * <ul>
     * <li>
     * </ul>
     */
    private Behavior<Message> onShutdown(ShutdownMessage m) {
        log.debug("onShutdown {}", m);
        stopServerMessage.run();
        return Behaviors.stopped();
    }

    /**
     * Returns the behaviors after the actor was initialized. Returns a behavior for
     * the messages:
     *
     * <ul>
     * <li>{@link ImporterStartEmbeddedServerMessage}
     * <li>{@link WrappedDbResponse}
     * <li>{@link ShutdownMessage}
     * </ul>
     */
    protected BehaviorBuilder<Message> getInitialBehavior() {
        return Behaviors.receive(Message.class)//
                .onMessage(ImporterStartEmbeddedServerMessage.class, this::onImporterStartEmbeddedServer)//
                .onMessage(WrappedDbResponse.class, this::onWrappedDbResponse)//
                .onMessage(ShutdownMessage.class, this::onShutdown)//
        ;
    }

    /**
     * Returns the behaviors after the database was initialized. Returns a behavior
     * for the messages:
     *
     * <ul>
     * <li>{@link ImportImageMessage}
     * <li>{@link WrappedDbResponse}
     * <li>{@link ShutdownMessage}
     * </ul>
     */
    protected BehaviorBuilder<Message> getServerStartedBehavior() {
        return Behaviors.receive(Message.class)//
                .onMessage(ImporterStopEmbeddedServerMessage.class, this::onImporterStopEmbeddedServer)//
                .onMessage(ImportImageMessage.class, this::onImportImage)//
                .onMessage(WrappedDbResponse.class, this::onWrappedDbResponse)//
                .onMessage(ShutdownMessage.class, this::onShutdown)//
        ;
    }

}
