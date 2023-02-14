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

import static com.anrisoftware.dwarfhustle.model.actor.CreateActorMessage.createNamedActor;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;

import java.io.File;
import java.time.Duration;
import java.util.EventObject;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

import javax.inject.Inject;

import org.apache.commons.jcs3.access.CacheAccess;
import org.apache.commons.jcs3.access.exception.CacheException;
import org.apache.commons.jcs3.engine.CacheElement;
import org.apache.commons.jcs3.engine.control.event.behavior.IElementEvent;
import org.apache.commons.jcs3.engine.control.event.behavior.IElementEventHandler;

import com.anrisoftware.dwarfhustle.model.actor.ActorSystemProvider;
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;
import com.anrisoftware.dwarfhustle.model.db.cache.CacheGetMessage.CacheGetSuccessMessage;
import com.anrisoftware.dwarfhustle.model.db.cache.CacheResponseMessage.CacheErrorMessage;
import com.anrisoftware.dwarfhustle.model.db.cache.CacheResponseMessage.CacheSuccessMessage;
import com.anrisoftware.dwarfhustle.model.db.cache.CacheRetrieveMessage.CacheRetrieveResponseMessage;
import com.google.inject.Injector;
import com.google.inject.assistedinject.Assisted;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.BehaviorBuilder;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.StashBuffer;
import akka.actor.typed.receptionist.ServiceKey;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
public abstract class AbstractJcsCacheActor<K, V extends GameObject> implements IElementEventHandler {

	public static final ServiceKey<Message> KEY = ServiceKey.create(Message.class,
			AbstractJcsCacheActor.class.getSimpleName());

	public static final String NAME = AbstractJcsCacheActor.class.getSimpleName();

	public static final int ID = KEY.hashCode();

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
		AbstractJcsCacheActor create(ActorContext<Message> context, StashBuffer<Message> stash,
				Map<String, Object> params);
	}

	@SuppressWarnings("unchecked")
	public static <K, V> Behavior<Message> create(Injector injector, AbstractJcsCacheActorFactory actorFactory,
			CompletionStage<CacheAccess<K, V>> initCacheAsync, Map<String, Object> params) {
		return Behaviors.withStash(100, stash -> Behaviors.setup(context -> {
			initCache(context, initCacheAsync);
			return actorFactory.create(context, stash, params).start();
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

	/**
	 * Creates the {@link AbstractJcsCacheActor}.
	 */
	public static <K, V> CompletionStage<ActorRef<Message>> create(Injector injector, Duration timeout,
			AbstractJcsCacheActorFactory actorFactory, CompletionStage<CacheAccess<K, V>> initCache,
			Map<String, Object> params) {
		var system = injector.getInstance(ActorSystemProvider.class).getActorSystem();
		return createNamedActor(system, timeout, ID, KEY, NAME, create(injector, actorFactory, initCache, params));
	}

    /**
     * Creates the cache from the configuration and additional parameters.
     *
     * @param config the {@link Properties} with the cache configuration.
     * @param params additional parameters:
     *               <ul>
     *               <li>cache_name
     *               <li>max_objects
     *               <li>is_eternal
     *               </ul>
     */
	public static void createCache(Properties config, Map<String, Object> params) {
        validateParamsCache(params);
		var cacheName = params.get("cache_name");
		var maxObjects = params.get("max_objects");
		var isEternal = params.get("is_eternal");
		var region = "jcs.region." + cacheName;
		config.put(region + ".cacheattributes", "org.apache.commons.jcs3.engine.CompositeCacheAttributes");
		config.put(region + ".cacheattributes.MaxObjects", toString(maxObjects));
		config.put(region + ".cacheattributes.MemoryCacheName",
				"org.apache.commons.jcs3.engine.memory.lru.LRUMemoryCache");
		config.put(region + ".cacheattributes.UseMemoryShrinker", "false");
		config.put(region + ".cacheattributes.MaxMemoryIdleTimeSeconds", "3600");
		config.put(region + ".cacheattributes.ShrinkerIntervalSeconds", "60");
		config.put(region + ".cacheattributes.MaxSpoolPerRun", "500");
		config.put(region + ".elementattributes", "org.apache.commons.jcs3.engine.ElementAttributes");
		config.put(region + ".elementattributes.IsEternal", toString(isEternal));
	}

    private static void validateParamsCache(Map<String, Object> params) {
        assertThat(params, hasKey("cache_name"));
        assertThat(params, hasKey("max_objects"));
        assertThat(params, hasKey("is_eternal"));
    }

    /**
     * Creates the auxiliary file cache from the configuration and additional
     * parameters.
     *
     * @param config the {@link Properties} with the cache configuration.
     * @param params additional parameters:
     *               <ul>
     *               <li>cache_name
     *               <li>max_key_size
     *               <li>parent_dir
     *               </ul>
     */
	public static void createFileAuxCache(Properties config, Map<String, Object> params) {
        validateParamsFile(params);
		var cacheName = params.get("cache_name") + "_file";
		var maxKeySize = params.get("max_key_size");
		var parentDir = (File) params.get("parent_dir");
		var aux = "jcs.auxiliary." + cacheName;
		config.put(aux, "org.apache.commons.jcs3.auxiliary.disk.indexed.IndexedDiskCacheFactory");
		config.put(aux + ".attributes", "org.apache.commons.jcs3.auxiliary.disk.indexed.IndexedDiskCacheAttributes");
		config.put(aux + ".attributes.DiskPath", parentDir.getAbsolutePath() + "/jcs_swap_" + cacheName);
		config.put(aux + ".attributes.MaxPurgatorySize", toString(maxKeySize));
		config.put(aux + ".attributes.MaxKeySize", toString(maxKeySize));
		config.put(aux + ".attributes.OptimizeAtRemoveCount", "300000");
		config.put(aux + ".attributes.ShutdownSpoolTimeLimit", "60");
		config.put(aux + ".attributes.OptimizeOnShutdown", toString(true));
		config.put(aux + ".attributes.DiskLimitType", "COUNT");
	}

    private static void validateParamsFile(Map<String, Object> params) {
        assertThat(params, hasKey("cache_name"));
        assertThat(params, hasKey("max_key_size"));
        assertThat(params, hasKey("parent_dir"));
    }

	private static String toString(Object v) {
		return v.toString();
	}

	protected final Duration timeout = Duration.ofSeconds(300);

	@Inject
	@Assisted
	protected Map<String, Object> params;

	@Inject
	@Assisted
	protected ActorContext<Message> context;

	@Inject
	@Assisted
	protected StashBuffer<Message> buffer;

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
		m.replyTo.tell(new CacheRetrieveResponseMessage<>(m, cache));
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
	 * Stores the put value in the database.
	 */
    protected abstract void storeValueDb(CachePutMessage<?, K, V> m);

	/**
	 * Retrieves the value from the database. Example send a database command:
	 *
	 * <pre>
	 * CompletionStage&lt;DbResponseMessage&gt; result = AskPattern.ask(db, replyTo -&gt; new DbCommandMessage(replyTo, db -&gt; {
	 * 	retrieveGameObject(m, db);
	 * }), timeout, context.getSystem().scheduler());
	 * result.whenComplete((response, throwable) -&gt; {
	 * });
	 * </pre>
	 */
	protected abstract void retrieveValueFromDb(CacheGetMessage<?> m, Consumer<GameObject> consumer);

}
