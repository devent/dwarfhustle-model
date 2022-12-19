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

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;

import org.apache.commons.jcs3.JCS;
import org.apache.commons.jcs3.access.CacheAccess;
import org.apache.commons.jcs3.access.exception.CacheException;

import com.anrisoftware.dwarfhustle.model.actor.ActorSystemProvider;
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.api.GameObject;
import com.anrisoftware.dwarfhustle.model.api.GameObjectStorage;
import com.anrisoftware.dwarfhustle.model.db.cache.AbstractCacheReplyMessage.CacheErrorMessage;
import com.anrisoftware.dwarfhustle.model.db.cache.AbstractCacheReplyMessage.CacheSuccessMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.AbstractDbReplyMessage.DbErrorMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.AbstractDbReplyMessage.DbResponseMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DbCommandMessage;
import com.google.inject.Injector;
import com.google.inject.assistedinject.Assisted;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.id.ORID;

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
public class JcsCacheActor {

	public static final ServiceKey<Message> KEY = ServiceKey.create(Message.class, JcsCacheActor.class.getSimpleName());

	public static final String NAME = JcsCacheActor.class.getSimpleName();

	public static final int ID = KEY.hashCode();

	@RequiredArgsConstructor
	@ToString(callSuper = true)
	private static class InitialStateMessage extends Message {

		public final CacheAccess<ORID, GameObject> cache;
	}

	@RequiredArgsConstructor
	@ToString(callSuper = true)
	private static class SetupErrorMessage extends Message {

		public final Throwable cause;
	}

	@RequiredArgsConstructor
	@ToString(callSuper = true)
	private static class RequestErrorMessage extends Message {

		public final AbstractCacheReplyMessage originalMessage;

		public final Throwable cause;
	}

	/**
	 * Factory to create {@link JcsCacheActor}.
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	public interface JcsCacheActorFactory {

		JcsCacheActor create(ActorContext<Message> context, StashBuffer<Message> stash, ActorRef<Message> db);
	}

	public static Behavior<Message> create(Injector injector, ActorRef<Message> db) {
		return Behaviors.withStash(100, stash -> Behaviors.setup((context) -> {
			initCache(injector, context);
			return injector.getInstance(JcsCacheActorFactory.class).create(context, stash, db).start();
		}));
	}

	private static void initCache(Injector injector, ActorContext<Message> context) {
		context.pipeToSelf(initCache0(injector), (result, cause) -> {
			if (cause == null) {
				return new InitialStateMessage(result);
			} else {
				return new SetupErrorMessage(cause);
			}
		});
	}

	private static CompletionStage<CacheAccess<ORID, GameObject>> initCache0(Injector injector) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				return JCS.getInstance("default");
			} catch (CacheException e) {
				throw new RuntimeException(e);
			}
		});
	}

	/**
	 * Creates the {@link JcsCacheActor}.
	 */
	public static CompletionStage<ActorRef<Message>> create(Injector injector, Duration timeout, ActorRef<Message> db) {
		var system = injector.getInstance(ActorSystemProvider.class).getActorSystem();
		return createNamedActor(system, timeout, ID, KEY, NAME, create(injector, db));
	}

	private final Duration timeout = Duration.ofSeconds(300);

	@Inject
	@Assisted
	private ActorContext<Message> context;

	@Inject
	@Assisted
	private StashBuffer<Message> buffer;

	@Inject
	@Assisted
	private ActorRef<Message> db;

	@Inject
	private Map<String, GameObjectStorage> storages;

	private CacheAccess<ORID, GameObject> cache;

	/**
	 * Stash behavior. Returns a behavior for the messages:
	 *
	 * <ul>
	 * <li>{@link InitialStateMessage}
	 * <li>{@link Message}
	 * </ul>
	 */
	public Behavior<Message> start() {
		return Behaviors.receive(Message.class)//
				.onMessage(InitialStateMessage.class, this::onInitialState)//
				.onMessage(Message.class, this::stashOtherCommand)//
				.build();
	}

	private Behavior<Message> stashOtherCommand(Message m) {
		log.debug("stashOtherCommand: {}", m);
		buffer.stash(m);
		return Behaviors.same();
	}

	/**
	 * Returns a behavior for the messages:
	 *
	 * <ul>
	 * <li>{@link PutMessage}
	 * <li>{@link RequestErrorMessage}
	 * </ul>
	 */
	private Behavior<Message> onInitialState(InitialStateMessage m) {
		log.debug("onInitialState");
		this.cache = m.cache;
		return buffer.unstashAll(getInitialBehavior()//
				.build());
	}

	/**
	 * Returns a behavior for the messages:
	 *
	 * <ul>
	 * <li>{@link PutMessage}
	 * <li>{@link RequestErrorMessage}
	 * </ul>
	 */
	private Behavior<Message> onPut(PutMessage m) {
		log.debug("onPut {}", m);
		var id = (ORID) m.go.id;
		if (id == null) {
			context.ask(DbResponseMessage.class, db, timeout,
					(ActorRef<DbResponseMessage> ref) -> new DbCommandMessage(ref, db -> {
						createNewVertex(m, db);
					}), (response, throwable) -> {
						if (throwable != null) {
							return new RequestErrorMessage(m, throwable);
						} else {
							return translateDbResponse(response, m);
						}
					});
			return Behaviors.same();
		}
		try {
			cache.put(id, m.go);
			m.replyTo.tell(new CacheSuccessMessage(m));
		} catch (CacheException e) {
			m.replyTo.tell(new CacheErrorMessage(m, e));
		}
		return Behaviors.same();
	}

	private void createNewVertex(PutMessage m, ODatabaseDocument db) {
		var doc = db.newVertex(m.go.getType());
		db.begin();
		storages.get(m.go.getType()).save(doc, m.go);
		doc.save();
		db.commit();
		m.go.id = doc.getIdentity();
	}

	private Message translateDbResponse(DbResponseMessage response, PutMessage m) {
		if (response instanceof DbErrorMessage) {
			var dm = (DbErrorMessage) response;
			return new RequestErrorMessage(m, dm.error);
		}
		return m;
	}

	/**
	 * There was an error with a request. Stops the actor.
	 */
	private Behavior<Message> onRequestError(RequestErrorMessage m) {
		log.debug("onRequestError {}", m);
		m.originalMessage.replyTo.tell(new CacheErrorMessage(m.originalMessage, m.cause));
		return Behaviors.stopped();
	}

	private BehaviorBuilder<Message> getInitialBehavior() {
		return Behaviors.receive(Message.class)//
				.onMessage(PutMessage.class, this::onPut)//
				.onMessage(RequestErrorMessage.class, this::onRequestError);
	}
}
