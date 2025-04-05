/*
 * dwarfhustle-model-db - Manages the compile dependencies for the model.
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
package com.anrisoftware.dwarfhustle.model.db.cache;

import static com.anrisoftware.dwarfhustle.model.actor.CreateActorMessage.createNamedActor;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.apache.commons.jcs3.JCS;
import org.apache.commons.jcs3.access.CacheAccess;
import org.apache.commons.jcs3.access.exception.CacheException;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.primitive.LongObjectMaps;
import org.eclipse.collections.api.map.primitive.MutableLongObjectMap;

import com.anrisoftware.dwarfhustle.model.actor.ActorSystemProvider;
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.actor.ShutdownMessage;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;
import com.anrisoftware.dwarfhustle.model.api.objects.ObjectsGetter;
import com.anrisoftware.dwarfhustle.model.api.objects.ObjectsSetter;
import com.anrisoftware.dwarfhustle.model.api.objects.StoredObject;
import com.google.inject.Injector;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.BehaviorBuilder;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.StashBuffer;
import akka.actor.typed.javadsl.TimerScheduler;
import akka.actor.typed.receptionist.ServiceKey;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * Cache for {@link StoredObject} backend stored game objects.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
public class StoredObjectsJcsCacheActor extends AbstractJcsCacheActor {

    public static final ServiceKey<Message> KEY = ServiceKey.create(Message.class,
            StoredObjectsJcsCacheActor.class.getSimpleName());

    public static final String NAME = StoredObjectsJcsCacheActor.class.getSimpleName();

    public static final int ID = KEY.hashCode();

    @RequiredArgsConstructor
    @ToString(callSuper = true)
    private static class StoreObjectsMessage extends Message {
        public static final String KEY = StoreObjectsMessage.class.getName();

    }

    /**
     * Factory to create {@link StoredObjectsJcsCacheActor}.
     *
     * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
     */
    public interface StoredObjectsJcsCacheActorFactory extends AbstractJcsCacheActorFactory {

        @Override
        StoredObjectsJcsCacheActor create(ActorContext<Message> context, StashBuffer<Message> stash,
                TimerScheduler<Message> timer, ObjectsGetter og, ObjectsSetter os);
    }

    private static Behavior<Message> create(Injector injector, StoredObjectsJcsCacheActorFactory actorFactory,
            CompletionStage<ObjectsGetter> og, CompletionStage<ObjectsSetter> os,
            CompletionStage<CacheAccess<Object, GameObject>> initCacheAsync) {
        return AbstractJcsCacheActor.create(injector, actorFactory, og, os, initCacheAsync);
    }

    private static Behavior<Message> create(Injector injector, StoredObjectsJcsCacheActorFactory actorFactory,
            ObjectsGetter og, ObjectsSetter os, CompletionStage<CacheAccess<Object, GameObject>> initCacheAsync) {
        return AbstractJcsCacheActor.create(injector, actorFactory, og, os, initCacheAsync);
    }

    /**
     * Creates the {@link StoredObjectsJcsCacheActor}.
     */
    public static CompletionStage<ActorRef<Message>> create(Injector injector, Duration timeout,
            CompletionStage<ObjectsGetter> og, CompletionStage<ObjectsSetter> os) {
        final var system = injector.getInstance(ActorSystemProvider.class).getActorSystem();
        final var actorFactory = injector.getInstance(StoredObjectsJcsCacheActorFactory.class);
        final var initCache = createInitCacheAsync();
        return createNamedActor(system, timeout, ID, KEY, NAME, create(injector, actorFactory, og, os, initCache));
    }

    /**
     * Creates the {@link StoredObjectsJcsCacheActor}.
     */
    public static CompletionStage<ActorRef<Message>> create(Injector injector, Duration timeout, ObjectsGetter og,
            ObjectsSetter os) {
        final var system = injector.getInstance(ActorSystemProvider.class).getActorSystem();
        final var actorFactory = injector.getInstance(StoredObjectsJcsCacheActorFactory.class);
        final var initCache = createInitCacheAsync();
        return createNamedActor(system, timeout, ID, KEY, NAME, create(injector, actorFactory, og, os, initCache));
    }

    public static CompletableFuture<CacheAccess<Object, GameObject>> createInitCacheAsync() {
        final CompletableFuture<CacheAccess<Object, GameObject>> initCache = CompletableFuture.supplyAsync(() -> {
            try {
                return JCS.getInstance("stored_objects");
            } catch (final CacheException e) {
                throw new RuntimeException(e);
            }
        });
        return initCache;
    }

    private final MutableLongObjectMap<GameObject> queue = LongObjectMaps.mutable.ofInitialCapacity(1000);

    private final Semaphore queueLock = new Semaphore(1);

    @Override
    protected Behavior<Message> initialStage(InitialStateMessage m) {
        log.debug("initialStage {}", m);
        timer.startTimerAtFixedRate(StoreObjectsMessage.KEY, new StoreObjectsMessage(), Duration.ofSeconds(30));
        return super.initialStage(m);
    }

    @Override
    protected int getId() {
        return ID;
    }

    /**
     */
    protected Behavior<Message> onStoreObjects(StoreObjectsMessage m) {
        storeObjects();
        return Behaviors.same();
    }

    /**
     */
    protected Behavior<Message> onShutdown(ShutdownMessage m) {
        timer.cancelAll();
        storeObjects();
        return Behaviors.stopped();
    }

    private void storeObjects() {
        try {
            if (queueLock.tryAcquire(1, TimeUnit.SECONDS)) {
                final List<GameObject> list = Lists.mutable.ofAll(queue.values());
                queue.clear();
                for (final var go : list) {
                    System.out.println(go); // TODO
                    os.set(go.getObjectType(), go);
                }
                queueLock.release();
                // System.out.println(list); // TODO
            }
        } catch (final InterruptedException e) {
            log.error("storeObjects", e);
        }
        // System.out.println("StoredObjectsJcsCacheActor.storeObjects()"); // TODO
    }

    @Override
    protected void storeValueBackend(GameObject go) {
        try {
            if (queueLock.tryAcquire(1, TimeUnit.SECONDS)) {
                queue.put(go.getId(), go);
                queueLock.release();
            }
        } catch (final InterruptedException e) {
            log.error("storeValueBackend", e);
        }
    }

    @Override
    protected void storeValuesBackend(int type, Iterable<GameObject> values) {
        try {
            if (queueLock.tryAcquire(1, TimeUnit.SECONDS)) {
                for (final GameObject go : values) {
                    queue.put(go.getId(), go);
                }
                queueLock.release();
            }
        } catch (final InterruptedException e) {
            log.error("storeValuesBackend", e);
        }
    }

    @Override
    protected void retrieveValueFromBackend(CacheGetMessage<?> m, Consumer<GameObject> consumer) {
        consumer.accept(og.get(m.type, m.key));
    }

    @Override
    protected <T extends GameObject> T getValueFromBackend(int type, long key) {
        return og.get(type, key);
    }

    @Override
    protected BehaviorBuilder<Message> getInitialBehavior() {
        return super.getInitialBehavior()//
                .onMessage(StoreObjectsMessage.class, this::onStoreObjects)//
                .onMessage(ShutdownMessage.class, this::onShutdown)//
        ;
    }

}
