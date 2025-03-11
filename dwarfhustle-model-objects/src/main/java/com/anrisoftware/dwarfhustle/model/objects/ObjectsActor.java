/*
 * dwarfhustle-model-objects - Manages the compile dependencies for the model.
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
package com.anrisoftware.dwarfhustle.model.objects;

import static com.anrisoftware.dwarfhustle.model.actor.CreateActorMessage.createNamedActor;
import static com.anrisoftware.dwarfhustle.model.api.objects.GameMap.getGameMap;
import static com.anrisoftware.dwarfhustle.model.db.cache.MapObject.getMapObject;
import static com.anrisoftware.dwarfhustle.model.db.cache.MapObject.setMapObject;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.lable.oss.uniqueid.IDGenerator;

import com.anrisoftware.dwarfhustle.model.actor.ActorSystemProvider;
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.actor.ShutdownMessage;
import com.anrisoftware.dwarfhustle.model.api.objects.GameMapObject;
import com.anrisoftware.dwarfhustle.model.api.objects.IdsObjectsProvider.IdsObjects;
import com.anrisoftware.dwarfhustle.model.api.objects.ObjectsGetter;
import com.anrisoftware.dwarfhustle.model.api.objects.ObjectsSetter;
import com.anrisoftware.dwarfhustle.model.db.cache.CacheResponseMessage;
import com.anrisoftware.dwarfhustle.model.db.cache.MapChunksJcsCacheActor;
import com.anrisoftware.dwarfhustle.model.db.cache.MapObject;
import com.anrisoftware.dwarfhustle.model.db.cache.MapObjectsJcsCacheActor;
import com.anrisoftware.dwarfhustle.model.db.cache.StoredObjectsJcsCacheActor;
import com.anrisoftware.dwarfhustle.model.objects.DeleteBulkObjectsMessage.DeleteBulkObjectsSuccessMessage;
import com.anrisoftware.dwarfhustle.model.objects.DeleteObjectMessage.DeleteObjectSuccessMessage;
import com.anrisoftware.dwarfhustle.model.objects.InsertObjectMessage.InsertObjectSuccessMessage;
import com.anrisoftware.dwarfhustle.model.objects.RetrieveObjectsMessage.RetrieveObjectsSuccessMessage;
import com.google.inject.Injector;
import com.google.inject.assistedinject.Assisted;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.BehaviorBuilder;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.StashBuffer;
import akka.actor.typed.receptionist.ServiceKey;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

/**
 * @see InsertObjectMessage
 * @see DeleteObjectMessage
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
public class ObjectsActor {

    public static final ServiceKey<Message> KEY = ServiceKey.create(Message.class, ObjectsActor.class.getSimpleName());

    public static final String NAME = ObjectsActor.class.getSimpleName();

    public static final int ID = KEY.hashCode();

    @RequiredArgsConstructor
    @ToString(callSuper = true)
    private static class InitialStateMessage extends Message {
        public final ObjectsGetter og;
        public final ObjectsSetter os;
        public final ObjectsGetter chunks;
        public final ObjectsGetter mg;
        public final ObjectsSetter ms;
    }

    @RequiredArgsConstructor
    @ToString(callSuper = true)
    private static class SetupErrorMessage extends Message {
        public final Throwable cause;
    }

    @RequiredArgsConstructor
    @ToString(callSuper = true)
    private static class WrappedCacheResponse extends Message {
        public final CacheResponseMessage<?> res;
    }

    /**
     * Factory to create {@link ObjectsActor}.
     *
     * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
     */
    public interface ObjectsActorFactory {
        ObjectsActor create(ActorContext<Message> context, StashBuffer<Message> stash);
    }

    /**
     * Creates the {@link ObjectsActor}.
     */
    private static Behavior<Message> create(Injector injector, ActorSystemProvider actor) {
        return Behaviors.withStash(100, stash -> Behaviors.setup(context -> {
            context.pipeToSelf(CompletableFuture.supplyAsync(() -> returnInitialState(injector, actor)),
                    (result, cause) -> {
                        if (cause == null) {
                            return result;
                        } else {
                            return new SetupErrorMessage(cause);
                        }
                    });
            return injector.getInstance(ObjectsActorFactory.class).create(context, stash).start(injector);
        }));
    }

    /**
     * Creates the {@link ObjectsActor}.
     */
    public static CompletionStage<ActorRef<Message>> create(Injector injector, Duration timeout) {
        final var actor = injector.getInstance(ActorSystemProvider.class);
        return createNamedActor(actor.getActorSystem(), timeout, ID, KEY, NAME, create(injector, actor));
    }

    private static Message returnInitialState(Injector injector, ActorSystemProvider actor) {
        try {
            final var og = actor.getObjectGetterAsyncNow(StoredObjectsJcsCacheActor.ID);
            final var os = actor.getObjectSetterAsyncNow(StoredObjectsJcsCacheActor.ID);
            final var chunks = actor.getObjectGetterAsyncNow(MapChunksJcsCacheActor.ID);
            final var mg = actor.getObjectGetterAsyncNow(MapObjectsJcsCacheActor.ID);
            final var ms = actor.getObjectSetterAsyncNow(MapObjectsJcsCacheActor.ID);
            return new InitialStateMessage(og, os, chunks, mg, ms);
        } catch (final Exception ex) {
            return new SetupErrorMessage(ex);
        }
    }

    @Inject
    @Assisted
    private ActorContext<Message> context;

    @Inject
    @Assisted
    private StashBuffer<Message> buffer;

    @Inject
    @IdsObjects
    private IDGenerator ids;

    private InitialStateMessage is;

    /**
     * Stash behavior. Returns a behavior for the messages:
     *
     * <ul>
     * <li>{@link InitialStateMessage}
     * <li>{@link SetupErrorMessage}
     * <li>{@link Message}
     * </ul>
     */
    @SneakyThrows
    public Behavior<Message> start(Injector injector) {
        return Behaviors.receive(Message.class)//
                .onMessage(InitialStateMessage.class, this::onInitialState)//
                .onMessage(SetupErrorMessage.class, this::onSetupError)//
                .onMessage(Message.class, this::stashOtherCommand)//
                .build();
    }

    private Behavior<Message> stashOtherCommand(Message m) {
        log.debug("stashOtherCommand: {}", m);
        buffer.stash(m);
        return Behaviors.same();
    }

    private Behavior<Message> onSetupError(SetupErrorMessage m) {
        log.debug("onSetupError: {}", m);
        return Behaviors.stopped();
    }

    /**
     * Returns a behavior for the messages from {@link #getInitialBehavior()}
     */
    private Behavior<Message> onInitialState(InitialStateMessage m) {
        log.debug("onInitialState");
        is = m;
        return buffer.unstashAll(getInitialBehavior()//
                .build());
    }

    /**
     * @see ShutdownMessage
     */
    private Behavior<Message> onShutdown(ShutdownMessage m) {
        log.debug("onShutdown {}", m);
        return Behaviors.stopped();
    }

    /**
     * @see InsertObjectMessage
     */
    @SneakyThrows
    private Behavior<Message> onInsertObject(InsertObjectMessage<? super ObjectResponseMessage> m) {
        final GameMapObject go = m.ko.createObject(ids.generate());
        go.setMap(m.gm);
        go.setPos(m.pos);
        go.setKid(m.ko.getKid());
        go.setOid(m.ko.getKnowledgeType().hashCode());
        go.setVisible(true);
        go.setCanSelect(true);
        m.consumer.accept(go);
        is.os.set(go.getObjectType(), go);
        final var gm = getGameMap(is.og, m.gm);
        try (var lock = gm.acquireLockMapObjects()) {
            final var mo = getMapObject(is.mg, gm, go.getPos());
            mo.setCid(m.cid);
            mo.addObject(go.getObjectType(), go.getId());
            setMapObject(is.ms, mo);
            gm.addFilledBlock(mo.getCid(), mo.getIndex());
            is.os.set(gm.getObjectType(), gm);
        }
        m.onInserted.run();
        m.replyTo.tell(new InsertObjectSuccessMessage(go));
        return Behaviors.same();
    }

    /**
     * @see DeleteObjectSuccessMessage
     */
    @SneakyThrows
    private Behavior<Message> onDeleteObject(Object om) {
        @SuppressWarnings("unchecked")
        var m = (DeleteObjectMessage<? super DeleteObjectSuccessMessage>) om;
        final GameMapObject go = is.og.get(m.type, m.id);
        val gm = getGameMap(is.og, m.gm);
        try (var lock = gm.acquireLockMapObjects()) {
            val mo = getMapObject(is.mg, gm, go.getPos());
            if (!mo.getOids().isEmpty()) {
                mo.removeObject(m.id);
                is.ms.set(mo.getObjectType(), mo);
                is.os.remove(go.getObjectType(), go);
                if (mo.isEmpty()) {
                    gm.removeFilledBlock(mo.getCid(), mo.getIndex());
                    is.os.set(gm.getObjectType(), gm);
                    is.ms.remove(MapObject.OBJECT_TYPE, mo);
                }
            }
        }
        m.onDeleted.run();
        m.replyTo.tell(new DeleteObjectSuccessMessage());
        return Behaviors.same();
    }

    /**
     * @see DeleteBulkObjectsMessage
     */
    @SneakyThrows
    private Behavior<Message> onDeleteBulkObjectsMessage(DeleteBulkObjectsMessage<? super ObjectResponseMessage> m) {
        val gm = getGameMap(is.og, m.gm);
        try (var lock = gm.acquireLockMapObjects()) {
            for (var it = m.ids.longIterator(); it.hasNext();) {
                final long id = it.next();
                final GameMapObject go = is.og.get(m.type, id);
                val mo = getMapObject(is.mg, gm, go.getPos());
                if (!mo.getOids().isEmpty()) {
                    mo.removeObject(id);
                    is.ms.set(mo.getObjectType(), mo);
                    is.os.remove(go.getObjectType(), go);
                    if (mo.isEmpty()) {
                        gm.removeFilledBlock(mo.getCid(), mo.getIndex());
                        is.os.set(gm.getObjectType(), gm);
                        is.ms.remove(MapObject.OBJECT_TYPE, mo);
                    }
                }
            }
        }
        m.onDeleted.run();
        m.replyTo.tell(new DeleteBulkObjectsSuccessMessage());
        return Behaviors.same();
    }

    /**
     * @see RetrieveObjectsMessage
     */
    @SneakyThrows
    private Behavior<Message> onRetrieveObjects(RetrieveObjectsMessage<? super ObjectResponseMessage> m) {
        val gm = getGameMap(is.og, m.gm);
        MutableList<GameMapObject> objects = Lists.mutable.empty();
        try (var lock = gm.acquireLockMapObjects()) {
            val mo = getMapObject(is.mg, gm, m.pos.getX(), m.pos.getY(), m.pos.getZ());
            mo.getOids().forEachKeyValue((id, type) -> {
                final GameMapObject go = is.og.get(type, id);
                objects.add(go);
            });
        }
        m.consumer.accept(objects);
        m.replyTo.tell(new RetrieveObjectsSuccessMessage(objects));
        return Behaviors.same();
    }

    /**
     * Returns a behavior for the messages:
     *
     * <ul>
     * <li>{@link ShutdownMessage}
     * </ul>
     */
    private BehaviorBuilder<Message> getInitialBehavior() {
        return Behaviors.receive(Message.class)//
                .onMessage(ShutdownMessage.class, this::onShutdown)//
                .onMessage(InsertObjectMessage.class, this::onInsertObject)//
                .onMessage(DeleteObjectMessage.class, this::onDeleteObject)//
                .onMessage(DeleteBulkObjectsMessage.class, this::onDeleteBulkObjectsMessage)//
                .onMessage(RetrieveObjectsMessage.class, this::onRetrieveObjects)//
        ;
    }

}
