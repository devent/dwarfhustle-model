/*
 * dwarfhustle-model-generate-map - Manages the compile dependencies for the model.
 * Copyright © 2022-2024 Erwin Müller (erwin.mueller@anrisoftware.com)
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
import static com.anrisoftware.dwarfhustle.model.api.objects.WorldMap.getWorldMap;
import static com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.KnowledgeGetMessage.askKnowledgeObjects;
import static java.lang.String.format;
import static java.time.Duration.ofSeconds;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.CompletionStage;

import org.lable.oss.uniqueid.IDGenerator;

import com.anrisoftware.dwarfhustle.model.actor.ActorSystemProvider;
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.actor.ShutdownMessage;
import com.anrisoftware.dwarfhustle.model.api.objects.IdsObjectsProvider.IdsObjects;
import com.anrisoftware.dwarfhustle.model.api.objects.ObjectsGetter;
import com.anrisoftware.dwarfhustle.model.db.cache.AbstractJcsCacheActor;
import com.anrisoftware.dwarfhustle.model.db.cache.MapChunksJcsCacheActor;
import com.anrisoftware.dwarfhustle.model.db.lmbd.MapChunksLmbdStorage.MapChunksLmbdStorageFactory;
import com.anrisoftware.dwarfhustle.model.knowledge.evrete.TerrainKnowledge;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.DefaultLoadKnowledges;
import com.anrisoftware.dwarfhustle.model.terrainimage.ImportImageMessage.ImportImageErrorMessage;
import com.anrisoftware.dwarfhustle.model.terrainimage.ImportImageMessage.ImportImageSuccessMessage;
import com.anrisoftware.dwarfhustle.model.terrainimage.TerrainImageCreateMap.TerrainImageCreateMapFactory;
import com.google.inject.Injector;
import com.google.inject.assistedinject.Assisted;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.BehaviorBuilder;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.receptionist.ServiceKey;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
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

    /**
     * Factory to create {@link AbstractJcsCacheActor}.
     *
     * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
     */
    public interface ImporterMapImage2DbActorFactory {

        ImporterMapImage2DbActor create(ActorContext<Message> context, ObjectsGetter og);
    }

    public static Behavior<Message> create(Injector injector, ActorSystemProvider actor,
            ImporterMapImage2DbActorFactory actorFactory, CompletionStage<ObjectsGetter> og) {
        return Behaviors.setup(context -> {
            return actorFactory.create(context, og.toCompletableFuture().get(15, SECONDS)).start(injector);
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
    private ObjectsGetter og;

    @Inject
    private ActorSystemProvider actor;

    @Inject
    @IdsObjects
    private IDGenerator gen;

    @Inject
    private MapChunksLmbdStorageFactory storageFactory;

    @Inject
    private TerrainImageCreateMapFactory terrainImageCreateMap;

    private Injector injector;

    /**
     * @see #getInitialBehavior()
     */
    public Behavior<Message> start(Injector injector) {
        this.injector = injector;
        return getInitialBehavior().build();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Behavior<Message> onImportImage(ImportImageMessage m) {
        log.debug("onImportImage {}", m);
        try {
            importImage(m);
        } catch (Exception e) {
            log.error("onImportImage", e);
            m.replyTo.tell(new ImportImageErrorMessage(e));
            return Behaviors.stopped();
        }
        return Behaviors.same();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @SneakyThrows
    private void importImage(ImportImageMessage m) {
        var gm = getGameMap(og, m.mapid);
        var wm = getWorldMap(og, gm.world);
        var path = Path.of(m.root, format("%d-%d", wm.id, gm.id));
        path.toFile().mkdir();
        var storage = storageFactory.create(path, gm.chunkSize);
        ImporterChunksJcsCacheActor.create(injector, ofSeconds(1), storage, storage).whenComplete((cache, ex) -> {
            if (ex != null) {
                log.error("ChunksJcsCacheActor", ex);
            }
        }).toCompletableFuture().get();
        var knowledge = new TerrainKnowledge();
        var loaded = new DefaultLoadKnowledges();
        loaded.loadKnowledges((timeout, type) -> askKnowledgeObjects(actor.getActorSystem(), timeout, type));
        knowledge.setLoadedKnowledges(loaded);
        terrainImageCreateMap.create(actor.getObjectGetterAsync(MapChunksJcsCacheActor.ID).toCompletableFuture().get(),
                actor.getObjectSetterAsync(MapChunksJcsCacheActor.ID).toCompletableFuture().get(), storage, knowledge)
                .startImportMapping(m.url, m.image, gm);
        storage.close();
        m.replyTo.tell(new ImportImageSuccessMessage());
    }

    /**
     * <ul>
     * <li>
     * </ul>
     */
    private Behavior<Message> onShutdown(ShutdownMessage m) {
        log.debug("onShutdown {}", m);
        return Behaviors.stopped();
    }

    /**
     * Returns the behaviors after the actor was initialized. Returns a behavior for
     * the messages:
     *
     * <ul>
     * <li>{@link ImportImageMessage}
     * <li>{@link ShutdownMessage}
     * </ul>
     */
    private BehaviorBuilder<Message> getInitialBehavior() {
        return Behaviors.receive(Message.class)//
                .onMessage(ImportImageMessage.class, this::onImportImage)//
                .onMessage(ShutdownMessage.class, this::onShutdown)//
        ;
    }
}
