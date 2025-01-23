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

import static java.util.concurrent.TimeUnit.SECONDS;

import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

import org.apache.commons.jcs3.access.CacheAccess;
import org.apache.commons.jcs3.access.exception.CacheException;

import com.anrisoftware.dwarfhustle.model.actor.ActorSystemProvider;
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;
import com.anrisoftware.dwarfhustle.model.api.objects.ObjectsGetter;
import com.anrisoftware.dwarfhustle.model.api.objects.ObjectsSetter;
import com.anrisoftware.dwarfhustle.model.db.cache.CacheGetMessage.CacheGetSuccessMessage;
import com.anrisoftware.dwarfhustle.model.db.cache.CacheResponseMessage.CacheErrorMessage;
import com.anrisoftware.dwarfhustle.model.db.cache.CacheResponseMessage.CacheSuccessMessage;
import com.anrisoftware.dwarfhustle.model.db.cache.CacheRetrieveMessage.CacheRetrieveResponseMessage;
import com.google.inject.Injector;
import com.google.inject.assistedinject.Assisted;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.BehaviorBuilder;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.StashBuffer;
import akka.actor.typed.javadsl.TimerScheduler;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
public abstract class AbstractJcsCacheActor implements ObjectsGetter, ObjectsSetter {

    @RequiredArgsConstructor
    @ToString(callSuper = true)
    protected static class InitialStateMessage extends Message {
        public final CacheAccess<Object, GameObject> cache;
    }

    @RequiredArgsConstructor
    @ToString(callSuper = true)
    protected static class SetupErrorMessage extends Message {
        public final Throwable cause;
    }

    /**
     * Factory to create {@link AbstractJcsCacheActor}.
     *
     * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
     */
    public interface AbstractJcsCacheActorFactory {

        AbstractJcsCacheActor create(ActorContext<Message> context, StashBuffer<Message> stash,
                TimerScheduler<Message> timer, ObjectsGetter og, ObjectsSetter os);
    }

    public static Behavior<Message> create(Injector injector, AbstractJcsCacheActorFactory actorFactory,
            CompletionStage<ObjectsGetter> og, CompletionStage<ObjectsSetter> os,
            CompletionStage<CacheAccess<Object, GameObject>> initCacheAsync) {
        return Behaviors.withTimers(timer -> Behaviors.withStash(100, stash -> Behaviors.setup(context -> {
            initCache(context, initCacheAsync);
            var og0 = og.toCompletableFuture().get(15, SECONDS);
            var os0 = os.toCompletableFuture().get(15, SECONDS);
            return actorFactory.create(context, stash, timer, og0, os0).start();
        })));
    }

    public static Behavior<Message> create(Injector injector, AbstractJcsCacheActorFactory actorFactory,
            ObjectsGetter og, ObjectsSetter os, CompletionStage<CacheAccess<Object, GameObject>> initCacheAsync) {
        assert og != null : "og is null";
        assert os != null : "os is null";
        return Behaviors.withTimers(timer -> Behaviors.withStash(100, stash -> Behaviors.setup(context -> {
            initCache(context, initCacheAsync);
            return actorFactory.create(context, stash, timer, og, os).start();
        })));
    }

    private static void initCache(ActorContext<Message> context,
            CompletionStage<CacheAccess<Object, GameObject>> initCacheAsync) {
        context.pipeToSelf(initCacheAsync, (result, cause) -> {
            if (cause == null) {
                return new InitialStateMessage(result);
            } else {
                log.error("Init cache", cause);
                return new SetupErrorMessage(cause);
            }
        });
    }

    protected final Duration timeout = Duration.ofSeconds(300);

    @Inject
    protected ActorSystemProvider actor;

    @Inject
    @Assisted
    protected ActorContext<Message> context;

    @Inject
    @Assisted
    protected StashBuffer<Message> buffer;

    @Inject
    @Assisted
    protected TimerScheduler<Message> timer;

    @Inject
    @Assisted
    protected ObjectsGetter og;

    @Inject
    @Assisted
    protected ObjectsSetter os;

    protected CacheAccess<Object, GameObject> cache;

    /**
     * Stash behavior. Returns a behavior for the messages:
     *
     * <ul>
     * <li>{@link InitialStateMessage}
     * <li>{@link SetupErrorMessage}
     * <li>{@link Message}
     * </ul>
     */
    public Behavior<Message> start() {
        return Behaviors.receive(Message.class)//
                .onMessage(InitialStateMessage.class, this::onInitialState)//
                .onMessage(SetupErrorMessage.class, this::onSetupError)//
                .onMessage(Message.class, this::stashOtherCommand)//
                .build();
    }

    protected Behavior<Message> onSetupError(Object m) {
        log.debug("onSetupError: {}", m);
        return Behaviors.stopped();
    }

    protected Behavior<Message> stashOtherCommand(Object m) {
        log.debug("stashOtherCommand: {}", m);
        buffer.stash((Message) m);
        return Behaviors.same();
    }

    /**
     * Returns a behavior for the messages from {@link #getInitialBehavior()}.
     */
    private Behavior<Message> onInitialState(InitialStateMessage m) {
        log.debug("onInitialState {}", m);
        this.cache = m.cache;
        actor.registerObjectsGetter(getId(), this);
        actor.registerObjectsSetter(getId(), this);
        return initialStage(m);
    }

    /**
     * Returns a behavior for the messages from {@link #getInitialBehavior()}.
     */
    @SuppressWarnings("unchecked")
    private Behavior<Message> onCachePut(@SuppressWarnings("rawtypes") CachePutMessage m) {
        try {
            preCachePut(m.value.getId(), m.value);
            cache.put(m.value.getId(), m.value);
            storeValueBackend(m.value);
            m.replyTo.tell(new CacheSuccessMessage<>(m));
        } catch (CacheException e) {
            m.replyTo.tell(new CacheErrorMessage<>(m, e));
        }
        return Behaviors.same();
    }

    /**
     * Returns a behavior for the messages from {@link #getInitialBehavior()}.
     */
    @SuppressWarnings("unchecked")
    private Behavior<Message> onCachePuts(@SuppressWarnings("rawtypes") CachePutsMessage m) {
        try {
            for (var o : m.values) {
                var go = (GameObject) o;
                preCachePut(go.getId(), go);
                cache.put(go.getId(), go);
            }
            storeValuesBackend(m.objectType, m.values);
            m.replyTo.tell(new CacheSuccessMessage<>(m));
        } catch (CacheException e) {
            m.replyTo.tell(new CacheErrorMessage<>(m, e));
        }
        return Behaviors.same();
    }

    /**
     * Returns a behavior for the messages from {@link #getInitialBehavior()}.
     */
    @SuppressWarnings("unchecked")
    private Behavior<Message> onCacheGet(@SuppressWarnings("rawtypes") CacheGetMessage m) {
        try {
            var v = cache.get(m.key);
            if (v == null) {
                m.onMiss.run();
                handleCacheMiss(m);
            } else {
                m.consumer.accept(v);
                m.replyTo.tell(new CacheGetSuccessMessage<>(m, v));
            }
        } catch (CacheException e) {
            m.replyTo.tell(new CacheErrorMessage<>(m, e));
        }
        return Behaviors.same();
    }

    @SuppressWarnings("unchecked")
    protected void handleCacheMiss(@SuppressWarnings("rawtypes") CacheGetMessage m) {
        context.getSelf().tell(new CacheRetrieveFromBackendMessage(m, go -> {
            cache.put(m.key, go);
            m.consumer.accept(go);
            m.replyTo.tell(new CacheGetSuccessMessage<>(m, go));
        }));
    }

    /**
     * Handle {@link CacheElementEventMessage}. Returns a behavior for the messages
     * from {@link #getInitialBehavior()}
     */
    protected Behavior<Message> onCacheElementEvent(Object m) {
        return Behaviors.same();
    }

    /**
     * Handle {@link CacheElementEventMessage}. Replies with a
     * {@link CacheRetrieveResponseMessage} message. Returns a behavior for the
     * messages from {@link #getInitialBehavior()}
     */
    protected Behavior<Message> onCacheRetrieve(CacheRetrieveMessage m) {
        if (m.id == getId()) {
            m.replyTo.tell(new CacheRetrieveResponseMessage(m, cache));
        }
        return Behaviors.same();
    }

    /**
     * Handle {@link CacheRetrieveFromBackendMessage}. Returns a behavior for the
     * messages from {@link #getInitialBehavior()}
     */
    protected Behavior<Message> onCacheRetrieveFromBackend(CacheRetrieveFromBackendMessage m) {
        retrieveValueFromBackend(m.m, m.consumer);
        return Behaviors.same();
    }

    /**
     * Unstash all messages kept in the buffer and return the initial behavior.
     * Returns a behavior for the messages from {@link #getInitialBehavior()}
     */
    protected Behavior<Message> initialStage(InitialStateMessage m) {
        log.debug("initialStage {}", m);
        return buffer.unstashAll(getInitialBehavior()//
                .build());
    }

    /**
     * Returns the behaviors after the cache was initialized. Returns a behavior for
     * the messages:
     *
     * <ul>
     * <li>{@link CachePutMessage}
     * <li>{@link CachePutsMessage}
     * <li>{@link CacheGetMessage}
     * <li>{@link CacheRetrieveFromBackendMessage}
     * <li>{@link CacheRetrieveMessage}
     * <li>{@link CacheElementEventMessage}
     * </ul>
     */
    protected BehaviorBuilder<Message> getInitialBehavior() {
        return Behaviors.receive(Message.class)//
                .onMessage(CachePutMessage.class, this::onCachePut)//
                .onMessage(CachePutsMessage.class, this::onCachePuts)//
                .onMessage(CacheGetMessage.class, this::onCacheGet)//
                .onMessage(CacheRetrieveFromBackendMessage.class, this::onCacheRetrieveFromBackend)//
                .onMessage(CacheRetrieveMessage.class, this::onCacheRetrieve)//
                .onMessage(CacheElementEventMessage.class, this::onCacheElementEvent)//
        ;
    }

    /**
     * Returns the ID of the cache.
     */
    protected abstract int getId();

    /**
     * Stores the put value in the database.
     */
    protected abstract void storeValueBackend(GameObject go);

    /**
     * Stores the put values in the database.
     */
    protected abstract void storeValuesBackend(int objectType, Iterable<GameObject> values);

    /**
     * Retrieves the value from the database. Example send a database command:
     *
     * <pre>
     * actor.tell(new LoadObjectMessage&lt;&gt;(objectsResponseAdapter, OBJECT_TYPE, consumer, db -&gt; {
     *     var query = "SELECT * from ? where ...";
     *     return db.query(query, ...);
     * }));
     * </pre>
     */
    protected abstract void retrieveValueFromBackend(CacheGetMessage<?> m, Consumer<GameObject> consumer);

    /**
     * Returns the value from the database.
     *
     * <pre>
     * CompletionStage&lt;DbResponseMessage&lt;?&gt;&gt; result = AskPattern.ask(actor.get(),
     *         replyTo -&gt; new DbCommandMessage&lt;&gt;(replyTo, err -&gt; {
     *             // db error
     *             return null;
     *         }, db -&gt; {
     *             // query string
     *             return null;
     *         }), timeout, context.getSystem().scheduler());
     * var ret = (DbCommandSuccessMessage&lt;?&gt;) result.toCompletableFuture().get();
     * return ret.value;
     * </pre>
     */
    protected abstract <T extends GameObject> T getValueFromBackend(int type, long key);

    /**
     * Called before the value is cached.
     */
    protected void preCachePut(long id, GameObject value) {
        // nop
    }

    /**
     * Returns the value for the key directly from the cache without sending of
     * messages. Should be used for performance critical code.
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends GameObject> T get(int type, long key) {
        return (T) cache.get(key, () -> supplyValue(type, key));
    }

    private GameObject supplyValue(int type, long key) {
        return getValueFromBackend(type, key);
    }

    @Override
    public void set(int type, GameObject go) {
        cache.put(go.getId(), go);
        storeValueBackend(go);
    }

    @Override
    public void set(int type, Iterable<GameObject> values) {
        for (GameObject go : values) {
            cache.put(go.getId(), go);
        }
        storeValuesBackend(type, values);
    }

    @Override
    public void remove(int objectType, GameObject go) throws ObjectsSetterException {
        os.remove(objectType, go);
        cache.remove(go.getId());
    }
}
