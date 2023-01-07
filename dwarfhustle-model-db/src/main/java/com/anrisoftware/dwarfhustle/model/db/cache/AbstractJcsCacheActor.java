/*
 * Copyright (C) 2021-2022 Erwin Müller <erwin@muellerpublic.de>
 * Released as open-source under the Apache License, Version 2.0.
 *
 * ****************************************************************************
 * ANL-OpenCL :: JME3 - App - Model
 * ****************************************************************************
 *
 * Copyright (C) 2021-2022 Erwin Müller <erwin@muellerpublic.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ****************************************************************************
 * ANL-OpenCL :: JME3 - App - Model is a derivative work based on Josua Tippetts' C++ library:
 * http://accidentalnoise.sourceforge.net/index.html
 * ****************************************************************************
 *
 * Copyright (C) 2011 Joshua Tippetts
 *
 *   This software is provided 'as-is', without any express or implied
 *   warranty.  In no event will the authors be held liable for any damages
 *   arising from the use of this software.
 *
 *   Permission is granted to anyone to use this software for any purpose,
 *   including commercial applications, and to alter it and redistribute it
 *   freely, subject to the following restrictions:
 *
 *   1. The origin of this software must not be misrepresented; you must not
 *      claim that you wrote the original software. If you use this software
 *      in a product, an acknowledgment in the product documentation would be
 *      appreciated but is not required.
 *   2. Altered source versions must be plainly marked as such, and must not be
 *      misrepresented as being the original software.
 *   3. This notice may not be removed or altered from any source distribution.
 *
 *
 * ****************************************************************************
 * ANL-OpenCL :: JME3 - App - Model bundles and uses the RandomCL library:
 * https://github.com/bstatcomp/RandomCL
 * ****************************************************************************
 *
 * BSD 3-Clause License
 *
 * Copyright (c) 2018, Tadej Ciglarič, Erik Štrumbelj, Rok Češnovar. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.anrisoftware.dwarfhustle.model.db.cache;

import static com.anrisoftware.dwarfhustle.model.actor.CreateActorMessage.createNamedActor;

import java.io.File;
import java.time.Duration;
import java.util.EventObject;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;

import org.apache.commons.jcs3.access.CacheAccess;
import org.apache.commons.jcs3.access.exception.CacheException;
import org.apache.commons.jcs3.engine.CacheElement;
import org.apache.commons.jcs3.engine.control.event.behavior.IElementEvent;
import org.apache.commons.jcs3.engine.control.event.behavior.IElementEventHandler;

import com.anrisoftware.dwarfhustle.model.actor.ActorSystemProvider;
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.api.GameObject;
import com.anrisoftware.dwarfhustle.model.db.cache.AbstractGetMessage.GetMessage;
import com.anrisoftware.dwarfhustle.model.db.cache.AbstractGetMessage.GetReplyMessage;
import com.anrisoftware.dwarfhustle.model.db.cache.AbstractGetMessage.GetSuccessMessage;
import com.anrisoftware.dwarfhustle.model.db.cache.AbstractPutMessage.PutMessage;
import com.anrisoftware.dwarfhustle.model.db.cache.AbstractPutMessage.PutReplyMessage;
import com.anrisoftware.dwarfhustle.model.db.cache.CacheResponseMessage.CacheErrorMessage;
import com.anrisoftware.dwarfhustle.model.db.cache.CacheResponseMessage.CacheSuccessMessage;
import com.anrisoftware.dwarfhustle.model.db.cache.RetrieveCacheMessage.RetrieveCacheResponseMessage;
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
public abstract class AbstractJcsCacheActor<K, V> implements IElementEventHandler {

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
		return Behaviors.withStash(100, stash -> Behaviors.setup((context) -> {
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

	public static void createCache(Properties config, Map<String, Object> params) {
		var cacheName = params.get("cache_name");
		var maxObjects = params.get("max_objects");
		var isEternal = params.get("is_eternal");
		String region = "jcs.region." + cacheName;
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

	public static void createFileAuxCache(Properties config, Map<String, Object> params) {
		var gameName = params.get("game_name");
		var cacheName = params.get("cache_name") + "_file";
		var maxKeySize = params.get("max_key_size");
		var parentDir = (File) params.get("parent_dir");
		String aux = "jcs.auxiliary." + cacheName;
		config.put(aux, "org.apache.commons.jcs3.auxiliary.disk.indexed.IndexedDiskCacheFactory");
		config.put(aux + ".attributes", "org.apache.commons.jcs3.auxiliary.disk.indexed.IndexedDiskCacheAttributes");
		config.put(aux + ".attributes.DiskPath",
				parentDir.getAbsolutePath() + "/dwarfhustle_jcs_swap_" + gameName + "_" + cacheName);
		config.put(aux + ".attributes.MaxPurgatorySize", toString(maxKeySize));
		config.put(aux + ".attributes.MaxKeySize", toString(maxKeySize));
		config.put(aux + ".attributes.OptimizeAtRemoveCount", "300000");
		config.put(aux + ".attributes.ShutdownSpoolTimeLimit", "60");
		config.put(aux + ".attributes.OptimizeOnShutdown", toString(true));
		config.put(aux + ".attributes.DiskLimitType", "COUNT");
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

	protected Behavior<Message> onSetupError(SetupErrorMessage m) {
		log.debug("onSetupError: {}", m);
		return Behaviors.stopped();
	}

	protected Behavior<Message> stashOtherCommand(Message m) {
		log.debug("stashOtherCommand: {}", m);
		buffer.stash(m);
		return Behaviors.same();
	}

	/**
	 * Returns a behavior for the messages:
	 *
	 * <ul>
	 * <li>{@link PutReplyMessage}
	 * <li>{@link GetReplyMessage}
	 * <li>{@link RetrieveCacheMessage}
	 * <li>{@link CacheElementEventMessage}
	 * </ul>
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
	 * Returns a behavior for the messages:
	 *
	 * <ul>
	 * <li>{@link PutReplyMessage}
	 * <li>{@link PutMessage}
	 * <li>{@link GetReplyMessage}
	 * <li>{@link GetMessage}
	 * <li>{@link RetrieveCacheMessage}
	 * <li>{@link CacheElementEventMessage}
	 * </ul>
	 */
	private Behavior<Message> onPutReply(PutReplyMessage m) {
		log.debug("onPutReply {}", m);
		return doPut(m);
	}

	/**
	 * Returns a behavior for the messages:
	 *
	 * <ul>
	 * <li>{@link PutReplyMessage}
	 * <li>{@link PutMessage}
	 * <li>{@link GetReplyMessage}
	 * <li>{@link GetMessage}
	 * <li>{@link RetrieveCacheMessage}
	 * <li>{@link CacheElementEventMessage}
	 * </ul>
	 */
	private Behavior<Message> onPut(PutMessage m) {
		log.debug("onPut {}", m);
		return doPut(m);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Behavior<Message> doPut(AbstractPutMessage m) {
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
	 * Returns a behavior for the messages:
	 *
	 * <ul>
	 * <li>{@link PutReplyMessage}
	 * <li>{@link PutMessage}
	 * <li>{@link GetReplyMessage}
	 * <li>{@link GetMessage}
	 * <li>{@link RetrieveCacheMessage}
	 * <li>{@link CacheElementEventMessage}
	 * </ul>
	 */
	private Behavior<Message> onGetReply(GetReplyMessage m) {
		log.debug("onGetReply {}", m);
		return doGet(m);
	}

	/**
	 * Returns a behavior for the messages:
	 *
	 * <ul>
	 * <li>{@link PutReplyMessage}
	 * <li>{@link PutMessage}
	 * <li>{@link GetReplyMessage}
	 * <li>{@link GetMessage}
	 * <li>{@link RetrieveCacheMessage}
	 * <li>{@link CacheElementEventMessage}
	 * </ul>
	 */
	private Behavior<Message> onGet(GetMessage m) {
		log.debug("onGet {}", m);
		return doGet(m);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Behavior<Message> doGet(AbstractGetMessage m) {
		try {
			var v = cache.get((K) m.key);
			if (v == null) {
				v = retrieveValueFromDb(m);
			} else {
				m.replyTo.tell(new GetSuccessMessage(m, (GameObject) v));
			}
		} catch (CacheException e) {
			m.replyTo.tell(new CacheErrorMessage(m, e));
		}
		return Behaviors.same();
	}

	/**
	 * Handle {@link CacheElementEventMessage}. Returns a behavior for the messages:
	 *
	 * <ul>
	 * <li>{@link PutReplyMessage}
	 * <li>{@link PutMessage}
	 * <li>{@link GetReplyMessage}
	 * <li>{@link GetMessage}
	 * <li>{@link RetrieveCacheMessage}
	 * <li>{@link CacheElementEventMessage}
	 * </ul>
	 */
	protected Behavior<Message> onCacheElementEvent(CacheElementEventMessage m) {
		log.debug("onCacheElementEvent {}", m);
		return Behaviors.same();
	}

	/**
	 * Handle {@link CacheElementEventMessage}. Replies with a
	 * {@link RetrieveCacheResponseMessage} message. Returns a behavior for the
	 * messages:
	 *
	 * <ul>
	 * <li>{@link PutReplyMessage}
	 * <li>{@link PutMessage}
	 * <li>{@link GetReplyMessage}
	 * <li>{@link GetMessage}
	 * <li>{@link RetrieveCacheMessage}
	 * <li>{@link CacheElementEventMessage}
	 * </ul>
	 */
	protected Behavior<Message> onRetrieveCache(RetrieveCacheMessage<K, V> m) {
		log.debug("onRetrieveCache {}", m);
		m.replyTo.tell(new RetrieveCacheResponseMessage<>(m, cache));
		return Behaviors.same();
	}

	/**
	 * Unstash all messages kept in the buffer and return the initial behavior.
	 * Returns a behavior for the messages:
	 *
	 * <ul>
	 * <li>{@link PutReplyMessage}
	 * <li>{@link PutMessage}
	 * <li>{@link GetReplyMessage}
	 * <li>{@link GetMessage}
	 * <li>{@link RetrieveCacheMessage}
	 * <li>{@link CacheElementEventMessage}
	 * </ul>
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
	 * <li>{@link PutReplyMessage}
	 * <li>{@link PutMessage}
	 * <li>{@link GetReplyMessage}
	 * <li>{@link GetMessage}
	 * <li>{@link RetrieveCacheMessage}
	 * <li>{@link CacheElementEventMessage}
	 * </ul>
	 */
	protected BehaviorBuilder<Message> getInitialBehavior() {
		return Behaviors.receive(Message.class)//
				.onMessage(PutReplyMessage.class, this::onPutReply)//
				.onMessage(PutMessage.class, this::onPut)//
				.onMessage(GetReplyMessage.class, this::onGetReply)//
				.onMessage(GetMessage.class, this::onGet)//
				.onMessage(RetrieveCacheMessage.class, this::onRetrieveCache)//
				.onMessage(CacheElementEventMessage.class, this::onCacheElementEvent)//
		;
	}

	/**
	 * Stores the put value in the database.
	 */
	protected abstract void storeValueDb(AbstractPutMessage<?> m);

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
	protected abstract V retrieveValueFromDb(AbstractGetMessage<?> m);

}
