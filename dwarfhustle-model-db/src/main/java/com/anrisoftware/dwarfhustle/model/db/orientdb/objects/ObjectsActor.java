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
package com.anrisoftware.dwarfhustle.model.db.orientdb.objects;

import static com.anrisoftware.dwarfhustle.model.actor.CreateActorMessage.createNamedActor;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;

import com.anrisoftware.dwarfhustle.model.actor.ActorSystemProvider;
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.AbstractDbReplyMessage.DbErrorMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.AbstractDbReplyMessage.DbResponseMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DbCommandMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.objects.AbstractObjectsReplyMessage.ObjectsErrorMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.objects.AbstractObjectsReplyMessage.ObjectsSuccessMessage;
import com.google.inject.Injector;
import com.google.inject.assistedinject.Assisted;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.BehaviorBuilder;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.receptionist.ServiceKey;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
public class ObjectsActor {

	public static final ServiceKey<Message> KEY = ServiceKey.create(Message.class, ObjectsActor.class.getSimpleName());

	public static final String NAME = ObjectsActor.class.getSimpleName();

	public static final int ID = KEY.hashCode();

	/**
	 * Factory to create {@link ObjectsActor}.
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	public interface ObjectsActorFactory {

		ObjectsActor create(ActorContext<Message> context, ActorRef<Message> db);
	}

	public static Behavior<Message> create(Injector injector, ActorRef<Message> db) {
		return Behaviors.setup((context) -> {
			return injector.getInstance(ObjectsActorFactory.class).create(context, db).start();
		});
	}

	/**
	 * Creates the {@link ObjectsActor}.
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
	private ActorRef<Message> db;

	@Inject
	private List<GameObjectSchema> schemas;

	/**
	 * Stash behavior. Returns a behavior for the messages:
	 *
	 * <ul>
	 * <li>{@link CreateSchemasMessage}
	 * <li>{@link ObjectsErrorMessage}
	 * <li>{@link ObjectsSuccessMessage}
	 * </ul>
	 */
	public Behavior<Message> start() {
		return getInitialBehavior()//
				.build();
	}

	/**
	 * Returns a behavior for the messages:
	 *
	 * <ul>
	 * <li>{@link CreateSchemasMessage}
	 * <li>{@link ObjectsErrorMessage}
	 * <li>{@link ObjectsSuccessMessage}
	 * </ul>
	 */
	private Behavior<Message> onCreateSchemas(CreateSchemasMessage m) {
		log.debug("onCreateSchemas {}", m);
		context.ask(DbResponseMessage.class, db, timeout,
				(ActorRef<DbResponseMessage> ref) -> new DbCommandMessage(ref, db -> {
					createSchemas(db);
				}), (response, throwable) -> {
					if (throwable != null) {
						return new ObjectsErrorMessage(m, throwable);
					} else {
						return translateDbResponse(response, m);
					}
				});
		return Behaviors.same();
	}

	private void createSchemas(ODatabaseDocument db) {
		for (GameObjectSchema schema : schemas) {
			schema.createSchema(db);
		}
	}

	private Message translateDbResponse(DbResponseMessage response, CreateSchemasMessage m) {
		if (response instanceof DbErrorMessage) {
			var dm = (DbErrorMessage) response;
			return new ObjectsErrorMessage(m, dm.error);
		}
		return new ObjectsSuccessMessage(m);
	}

	/**
	 * Returns a behavior for the messages:
	 *
	 * <ul>
	 * <li>{@link CreateSchemasMessage}
	 * <li>{@link ObjectsErrorMessage}
	 * <li>{@link ObjectsSuccessMessage}
	 * </ul>
	 */
	private Behavior<Message> onObjectsError(ObjectsErrorMessage m) {
		log.debug("onObjectsError {}", m);
		m.originalMessage.replyTo.tell(m);
		return Behaviors.stopped();
	}

	/**
	 * Returns a behavior for the messages:
	 *
	 * <ul>
	 * <li>{@link CreateSchemasMessage}
	 * <li>{@link ObjectsErrorMessage}
	 * <li>{@link ObjectsSuccessMessage}
	 * </ul>
	 */
	private Behavior<Message> onObjectsSuccess(ObjectsSuccessMessage m) {
		log.debug("onObjectsSuccess {}", m);
		m.originalMessage.replyTo.tell(m);
		return Behaviors.stopped();
	}

	private BehaviorBuilder<Message> getInitialBehavior() {
		return Behaviors.receive(Message.class)//
				.onMessage(CreateSchemasMessage.class, this::onCreateSchemas)//
				.onMessage(ObjectsErrorMessage.class, this::onObjectsError)//
				.onMessage(ObjectsSuccessMessage.class, this::onObjectsSuccess)//
		;
	}
}
