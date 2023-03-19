/*
 * dwarfhustle-model-db - Manages the compile dependencies for the model.
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
package com.anrisoftware.dwarfhustle.model.db.cache;

import java.time.Duration;
import java.util.EventObject;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

import javax.inject.Inject;

import org.apache.commons.jcs3.access.CacheAccess;
import org.apache.commons.jcs3.access.exception.CacheException;
import org.apache.commons.jcs3.engine.CacheElement;
import org.apache.commons.jcs3.engine.control.event.behavior.IElementEvent;
import org.apache.commons.jcs3.engine.control.event.behavior.IElementEventHandler;

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;
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
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
public abstract class AbstractJcsCacheActor<K, V extends GameObject> implements IElementEventHandler {

    @RequiredArgsConstructor
    @ToString(callSuper = true)
    public static class InitialStateMessage<K, V> extends Message {

        public final CacheAccess<K, V> cache;
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

        @SuppressWarnings("rawtypes")
        AbstractJcsCacheActor create(ActorContext<Message> context, StashBuffer<Message> stash, Class<?> keyType,
                Map<String, Object> params);
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Behavior<Message> create(Injector injector, AbstractJcsCacheActorFactory actorFactory,
            CompletionStage<CacheAccess<K, V>> initCacheAsync, Class<?> keyType, Map<String, Object> params) {
        return Behaviors.withStash(100, stash -> Behaviors.setup(context -> {
            initCache(context, initCacheAsync);
            return actorFactory.create(context, stash, keyType, params).start();
        }));
    }

    private static <K, V> void initCache(ActorContext<Message> context,
            CompletionStage<CacheAccess<K, V>> initCacheAsync) {
        context.pipeToSelf(initCacheAsync, (result, cause) -> {
            if (cause == null) {
                return new InitialStateMessage<>(result);
            } else {
                return new SetupErrorMessage(cause);
            }
        });
    }

    protected final Duration timeout = Duration.ofSeconds(300);

    @Inject
    @Assisted
    protected ActorContext<Message> context;

    @Inject
    @Assisted
    protected StashBuffer<Message> buffer;

    @Inject
    @Assisted
    protected Class<?> keyType;

    protected CacheAccess<K, V> cache;

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
    private Behavior<Message> onInitialState(InitialStateMessage<K, V> m) {
        log.debug("onInitialState {}", m);
        this.cache = m.cache;
        var attributes = cache.getDefaultElementAttributes();
        attributes.addElementEventHandler(this);
        cache.setDefaultElementAttributes(attributes);
        return initialStage(m);
    }

    @Override
    public <T> void handleElementEvent(IElementEvent<T> event) {
        log.debug("handleElementEvent {} {}", event.getElementEvent(), event);
        @SuppressWarnings("unchecked")
        var e = (CacheElement<Object, GameObject>) ((EventObject) event).getSource();
        var val = e.getVal();
        context.getSelf().tell(new CacheElementEventMessage(e, val));
    }

    /**
     * Returns a behavior for the messages from {@link #getInitialBehavior()}.
     */
    @SuppressWarnings("unchecked")
    private Behavior<Message> onCachePut(@SuppressWarnings("rawtypes") CachePutMessage m) {
        log.debug("onCachePut {}", m);
        if (!keyType.isInstance(m.key)) {
            log.warn("Cache key type not match {}!={}", keyType, m.key.getClass());
        }
        try {
            cache.put((K) m.key, (V) m.value);
            storeValueDb(m);
            m.replyTo.tell(new CacheSuccessMessage(m));
        } catch (CacheException e) {
            m.replyTo.tell(new CacheErrorMessage(m, e));
        }
        return Behaviors.same();
    }

    /**
     * Returns a behavior for the messages from {@link #getInitialBehavior()}.
     */
    @SuppressWarnings("unchecked")
    private Behavior<Message> onCacheGet(@SuppressWarnings("rawtypes") CacheGetMessage m) {
        log.debug("onCacheGet {}", m);
        try {
            var v = cache.get((K) m.key);
            if (v == null) {
                context.getSelf().tell(new CacheRetrieveFromBackendMessage(m, go -> {
                    var vgo = (V) go;
                    cache.put((K) m.key, vgo);
                    m.replyTo.tell(new CacheGetSuccessMessage<>(m, vgo));
                }));
            } else {
                m.replyTo.tell(new CacheGetSuccessMessage<>(m, v));
            }
        } catch (CacheException e) {
            m.replyTo.tell(new CacheErrorMessage(m, e));
        }
        return Behaviors.same();
    }

    /**
     * Handle {@link CacheElementEventMessage}. Returns a behavior for the messages
     * from {@link #getInitialBehavior()}
     */
    protected Behavior<Message> onCacheElementEvent(Object m) {
        log.debug("onCacheElementEvent {}", m);
        return Behaviors.same();
    }

    /**
     * Handle {@link CacheElementEventMessage}. Replies with a
     * {@link CacheRetrieveResponseMessage} message. Returns a behavior for the
     * messages from {@link #getInitialBehavior()}
     */
    protected Behavior<Message> onCacheRetrieve(CacheRetrieveMessage<K, V> m) {
        log.debug("onCacheRetrieve {}", m);
        if (m.id == getId()) {
            m.replyTo.tell(new CacheRetrieveResponseMessage<>(m, cache));
        }
        return Behaviors.same();
    }

    /**
     * Handle {@link CacheRetrieveFromBackendMessage}. Returns a behavior for the
     * messages from {@link #getInitialBehavior()}
     */
    protected Behavior<Message> onCacheRetrieveFromBackend(CacheRetrieveFromBackendMessage m) {
        log.debug("onCacheRetrieveFromBackend {}", m);
        retrieveValueFromDb(m.m, m.consumer);
        return Behaviors.same();
    }

    /**
     * Unstash all messages kept in the buffer and return the initial behavior.
     * Returns a behavior for the messages from {@link #getInitialBehavior()}
     */
    protected Behavior<Message> initialStage(InitialStateMessage<K, V> m) {
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
     * <li>{@link CacheGetMessage}
     * <li>{@link CacheRetrieveFromBackendMessage}
     * <li>{@link CacheRetrieveMessage}
     * <li>{@link CacheElementEventMessage}
     * </ul>
     */
    protected BehaviorBuilder<Message> getInitialBehavior() {
        return Behaviors.receive(Message.class)//
                .onMessage(CachePutMessage.class, this::onCachePut)//
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
    protected abstract void storeValueDb(CachePutMessage<?, K, V> m);

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
    protected abstract void retrieveValueFromDb(CacheGetMessage<?> m, Consumer<GameObject> consumer);

    /**
     * Retrieves the value from the database.
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
    protected abstract V retrieveValueFromDb(String type, K key);

    /**
     * Returns the value for the key directly from the cache without sending of
     * messages. Should be used for performance critical code.
     */
    public V get(String type, K key) {
        return cache.get(key, () -> supplyValue(type, key));
    }

    private V supplyValue(String type, K key) {
        return retrieveValueFromDb(type, key);
    }

}
