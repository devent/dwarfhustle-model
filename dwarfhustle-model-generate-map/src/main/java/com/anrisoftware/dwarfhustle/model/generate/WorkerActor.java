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
package com.anrisoftware.dwarfhustle.model.generate;

import static com.anrisoftware.dwarfhustle.model.actor.CreateActorMessage.createNamedActor;
import static com.anrisoftware.dwarfhustle.model.api.GameObject.toId;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.objects.GameObjectSchemaSchema.MAPID_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.objects.GameObjectSchemaSchema.OBJECTID_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.objects.GameObjectSchemaSchema.OBJECTTYPE_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.objects.GameObjectSchemaSchema.X_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.objects.GameObjectSchemaSchema.Y_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.objects.GameObjectSchemaSchema.Z_FIELD;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;

import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.primitive.IntObjectMap;
import org.eclipse.collections.impl.factory.Lists;
import org.lable.oss.uniqueid.IDGenerator;

import com.anrisoftware.dwarfhustle.model.actor.ActorSystemProvider;
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.api.MapTile;
import com.anrisoftware.dwarfhustle.model.api.Material;
import com.anrisoftware.dwarfhustle.model.api.Path;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.AbstractDbReplyMessage.DbErrorMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.AbstractDbReplyMessage.DbResponseMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DbCommandMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DbCommandMessage.DbCommandSuccessMessage;
import com.google.inject.Injector;
import com.google.inject.assistedinject.Assisted;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.record.OEdge;
import com.orientechnologies.orient.core.record.OVertex;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.BehaviorBuilder;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.receptionist.ServiceKey;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
public class WorkerActor {

	public static final ServiceKey<Message> KEY = ServiceKey.create(Message.class, WorkerActor.class.getSimpleName());

	public static final String NAME = WorkerActor.class.getSimpleName();

	public static final int ID = KEY.hashCode();

	/**
	 * Message to generate game map.
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	@RequiredArgsConstructor
	@ToString(callSuper = true)
	public static class GenerateMapMessage extends Message {

		public final ActorRef<ResponseMessage> replyTo;

		public final GenerateMessage generateMessage;

		@ToString.Exclude
		public final Map<String, IntObjectMap<? extends Material>> materials;
	}

	@RequiredArgsConstructor
	@ToString(callSuper = true)
	public static class ResponseMessage extends Message {

	}

	@RequiredArgsConstructor
	@ToString(callSuper = true)
	public static class ErrorMessage extends ResponseMessage {

		@ToString.Exclude
		public final GenerateMapMessage originalMessage;

		public final Throwable error;
	}

	@RequiredArgsConstructor
	@ToString(callSuper = true)
	public static class SuccessMessage extends ResponseMessage {

		public final GenerateMapMessage originalMessage;
	}

	@RequiredArgsConstructor
	@ToString(callSuper = true)
	private static class GenerateNodesErrorMessage extends ResponseMessage {

		public final GenerateMapMessage m;

		public final Throwable error;
	}

	@RequiredArgsConstructor
	@ToString(callSuper = true)
	private static class GenerateNodesSuccessMessage extends ResponseMessage {

		public final GenerateMapMessage m;
	}

	/**
	 * Factory to create {@link WorkerActor}.
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	public interface WorkerActorFactory {

		WorkerActor create(ActorContext<Message> context, @Assisted("db") ActorRef<Message> db,
				@Assisted("knowledge") ActorRef<Message> knowledge);
	}

	public static Behavior<Message> create(Injector injector, ActorRef<Message> db, ActorRef<Message> knowledge) {
		return Behaviors.setup((context) -> {
			return injector.getInstance(WorkerActorFactory.class).create(context, db, knowledge).start();
		});
	}

	/**
	 * Creates the {@link WorkerActor}.
	 */
	public static CompletionStage<ActorRef<Message>> create(Injector injector, Duration timeout, ActorRef<Message> db,
			ActorRef<Message> knowledge) {
		var system = injector.getInstance(ActorSystemProvider.class).getActorSystem();
		return createNamedActor(system, timeout, ID, KEY, NAME, create(injector, db, knowledge));
	}

	@Inject
	@Assisted
	private ActorContext<Message> context;

	@Inject
	@Assisted("db")
	private ActorRef<Message> db;

	@Inject
	@Assisted("knowledge")
	private ActorRef<Message> knowledge;

	@Inject
	private IDGenerator generator;

	private final Duration timeout = Duration.ofSeconds(300);

	/**
	 * Initial behavior. Returns a behavior for the messages:
	 *
	 * <ul>
	 * <li>{@link GenerateMapMessage}
	 * </ul>
	 */
	public Behavior<Message> start() {
		return getInitialBehavior().build();
	}

	/**
	 * Handle {@link GenerateMapMessage}. Returns a behavior for the messages:
	 *
	 * <ul>
	 * <li>{@link GenerateMapMessage}
	 * </ul>
	 */
	private Behavior<Message> onGenerateMap(GenerateMapMessage m) {
		log.debug("onGenerateMap {}", m);
		try {
			generateNodes(m);
		} catch (Throwable e) {
			m.replyTo.tell(new ErrorMessage(m, e));
		}
		return Behaviors.receive(Message.class)//
				.onMessage(GenerateNodesErrorMessage.class, this::onGenerateNodesError)//
				.onMessage(GenerateNodesSuccessMessage.class, this::onGenerateNodesSuccess)//
				.build();
	}

	/**
	 * Handle {@link GenerateNodesErrorMessage}. Sends {@link ErrorMessage} to the
	 * caller and terminates this actor.
	 */
	protected Behavior<Message> onGenerateNodesError(GenerateNodesErrorMessage m) {
		log.debug("onGenerateNodesError {}", m);
		m.m.replyTo.tell(new ErrorMessage(m.m, m.error));
		return Behaviors.stopped();
	}

	/**
	 * Handle {@link GenerateNodesSuccessMessage}. Sends {@link SuccessMessage} to
	 * the caller and terminates this actor.
	 */
	protected Behavior<Message> onGenerateNodesSuccess(GenerateNodesSuccessMessage m) {
		log.debug("onGenerateNodesSuccess {}", m);
		m.m.replyTo.tell(new SuccessMessage(m.m));
		return Behaviors.stopped();
	}

	private void generateNodes(GenerateMapMessage m) {
		context.ask(DbResponseMessage.class, db, timeout,
				(ActorRef<DbResponseMessage> ref) -> new DbCommandMessage(ref, (db) -> {
					generateNodes(m, db);
					generatePaths(m, db);
					return null;
				}), (response, throwable) -> {
					if (throwable != null) {
						return new GenerateNodesErrorMessage(m, throwable);
					}
					if (response instanceof DbErrorMessage) {
						var dm = (DbErrorMessage) response;
						return new GenerateNodesErrorMessage(m, dm.error);
					} else {
						var dm = (DbCommandSuccessMessage) response;
						return new GenerateNodesSuccessMessage(m);
					}
				});
	}

	private List<List<List<OVertex>>> nodes;

	@SneakyThrows
	private void generateNodes(GenerateMapMessage m, ODatabaseDocument db) {
		var gm = m.generateMessage;
		MutableList<List<List<OVertex>>> nodesidsz = Lists.mutable.ofInitialCapacity(gm.depth);
		for (int z = 0; z < gm.depth; z++) {
			MutableList<List<OVertex>> nodesidsy = Lists.mutable.ofInitialCapacity(gm.height);
			nodesidsz.add(nodesidsy);
			for (int y = 0; y < gm.height; y++) {
				MutableList<OVertex> nodesidsx = Lists.mutable.ofInitialCapacity(gm.width);
				nodesidsy.add(nodesidsx);
				var ids = generator.batch(gm.width);
				for (int x = 0; x < gm.width; x++) {
					var v = db.newVertex(MapTile.TYPE);
					long id = toId(ids.pop());
					v.setProperty(OBJECTID_FIELD, id);
					v.setProperty(OBJECTTYPE_FIELD, MapTile.TYPE);
					v.setProperty(MAPID_FIELD, gm.mapid);
					v.setProperty(X_FIELD, x);
					v.setProperty(Y_FIELD, y);
					v.setProperty(Z_FIELD, z);
					v.save();
					nodesidsx.add(v);
				}
			}
		}
		this.nodes = nodesidsz.asUnmodifiable();
	}

	@SneakyThrows
	private void generatePaths(GenerateMapMessage m, ODatabaseDocument db) {
		var gm = m.generateMessage;
		for (int z = 1; z < gm.depth - 1; z++) {
			for (int y = 1; y < gm.height - 1; y++) {
				for (int x = 1; x < gm.width - 1; x++) {
					OVertex n = nodes.get(z).get(y).get(x);
					List<OEdge> e = Lists.mutable.withInitialCapacity(8 * 8 * 8);
					e.add(db.newEdge(n, nodes.get(z).get(y + 1).get(x), Path.NPath.TYPE));
					e.add(db.newEdge(n, nodes.get(z).get(y + 1).get(x + 1), Path.NePath.TYPE));
					e.add(db.newEdge(n, nodes.get(z).get(y).get(x + 1), Path.EPath.TYPE));
					e.add(db.newEdge(n, nodes.get(z).get(y - 1).get(x + 1), Path.SePath.TYPE));
					e.add(db.newEdge(n, nodes.get(z).get(y - 1).get(x), Path.SPath.TYPE));
					e.add(db.newEdge(n, nodes.get(z).get(y - 1).get(x - 1), Path.SwPath.TYPE));
					e.add(db.newEdge(n, nodes.get(z).get(y).get(x - 1), Path.WPath.TYPE));
					e.add(db.newEdge(n, nodes.get(z).get(y + 1).get(x - 1), Path.NwPath.TYPE));
					// up
					e.add(db.newEdge(n, nodes.get(z + 1).get(y + 1).get(x), Path.UnPath.TYPE));
					e.add(db.newEdge(n, nodes.get(z + 1).get(y + 1).get(x + 1), Path.UnePath.TYPE));
					e.add(db.newEdge(n, nodes.get(z + 1).get(y).get(x + 1), Path.UePath.TYPE));
					e.add(db.newEdge(n, nodes.get(z + 1).get(y - 1).get(x + 1), Path.UsePath.TYPE));
					e.add(db.newEdge(n, nodes.get(z + 1).get(y - 1).get(x), Path.UsPath.TYPE));
					e.add(db.newEdge(n, nodes.get(z + 1).get(y - 1).get(x - 1), Path.UswPath.TYPE));
					e.add(db.newEdge(n, nodes.get(z + 1).get(y).get(x - 1), Path.UwPath.TYPE));
					e.add(db.newEdge(n, nodes.get(z + 1).get(y + 1).get(x - 1), Path.UnwPath.TYPE));
					// down
					e.add(db.newEdge(n, nodes.get(z - 1).get(y + 1).get(x), Path.DnPath.TYPE));
					e.add(db.newEdge(n, nodes.get(z - 1).get(y + 1).get(x + 1), Path.DnePath.TYPE));
					e.add(db.newEdge(n, nodes.get(z - 1).get(y).get(x + 1), Path.DePath.TYPE));
					e.add(db.newEdge(n, nodes.get(z - 1).get(y - 1).get(x + 1), Path.DsePath.TYPE));
					e.add(db.newEdge(n, nodes.get(z - 1).get(y - 1).get(x), Path.DsPath.TYPE));
					e.add(db.newEdge(n, nodes.get(z - 1).get(y - 1).get(x - 1), Path.DswPath.TYPE));
					e.add(db.newEdge(n, nodes.get(z - 1).get(y).get(x - 1), Path.DwPath.TYPE));
					e.add(db.newEdge(n, nodes.get(z - 1).get(y + 1).get(x - 1), Path.DnwPath.TYPE));
					// save
					for (OEdge edge : e) {
						edge.save();
					}
				}
			}
		}
		for (int z = 1; z < gm.depth - 1; z++) {
			int y = 0;
			for (int x = 1; x < gm.width - 1; x++) {
				OVertex n = nodes.get(z).get(y).get(x);
				List<OEdge> e = Lists.mutable.withInitialCapacity(8 * 1 * 8);
				e.add(db.newEdge(n, nodes.get(z).get(y + 1).get(x), Path.NPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z).get(y + 1).get(x + 1), Path.NePath.TYPE));
				e.add(db.newEdge(n, nodes.get(z).get(y).get(x + 1), Path.EPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z).get(y).get(x - 1), Path.WPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z).get(y + 1).get(x - 1), Path.NwPath.TYPE));
				// up
				e.add(db.newEdge(n, nodes.get(z + 1).get(y + 1).get(x), Path.UnPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z + 1).get(y + 1).get(x + 1), Path.UnePath.TYPE));
				e.add(db.newEdge(n, nodes.get(z + 1).get(y).get(x + 1), Path.UePath.TYPE));
				e.add(db.newEdge(n, nodes.get(z + 1).get(y).get(x - 1), Path.UwPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z + 1).get(y + 1).get(x - 1), Path.UnwPath.TYPE));
				// down
				e.add(db.newEdge(n, nodes.get(z - 1).get(y + 1).get(x), Path.DnPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z - 1).get(y + 1).get(x + 1), Path.DnePath.TYPE));
				e.add(db.newEdge(n, nodes.get(z - 1).get(y).get(x + 1), Path.DePath.TYPE));
				e.add(db.newEdge(n, nodes.get(z - 1).get(y).get(x - 1), Path.DwPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z - 1).get(y + 1).get(x - 1), Path.DnwPath.TYPE));
				// save
				for (OEdge edge : e) {
					edge.save();
				}
			}
			y = gm.height - 1;
			for (int x = 1; x < gm.width - 1; x++) {
				OVertex n = nodes.get(z).get(y).get(x);
				List<OEdge> e = Lists.mutable.withInitialCapacity(8 * 1 * 8);
				e.add(db.newEdge(n, nodes.get(z).get(y).get(x + 1), Path.EPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z).get(y - 1).get(x + 1), Path.SePath.TYPE));
				e.add(db.newEdge(n, nodes.get(z).get(y - 1).get(x), Path.SPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z).get(y - 1).get(x - 1), Path.SwPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z).get(y).get(x - 1), Path.WPath.TYPE));
				// up
				e.add(db.newEdge(n, nodes.get(z + 1).get(y).get(x + 1), Path.UePath.TYPE));
				e.add(db.newEdge(n, nodes.get(z + 1).get(y - 1).get(x + 1), Path.UsePath.TYPE));
				e.add(db.newEdge(n, nodes.get(z + 1).get(y - 1).get(x), Path.UsPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z + 1).get(y - 1).get(x - 1), Path.UswPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z + 1).get(y).get(x - 1), Path.UwPath.TYPE));
				// down
				e.add(db.newEdge(n, nodes.get(z - 1).get(y).get(x + 1), Path.DePath.TYPE));
				e.add(db.newEdge(n, nodes.get(z - 1).get(y - 1).get(x + 1), Path.DsePath.TYPE));
				e.add(db.newEdge(n, nodes.get(z - 1).get(y - 1).get(x), Path.DsPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z - 1).get(y - 1).get(x - 1), Path.DswPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z - 1).get(y).get(x - 1), Path.DwPath.TYPE));
				// save
				for (OEdge edge : e) {
					edge.save();
				}
			}
		}
		for (int z = 1; z < gm.depth - 1; z++) {
			int x = 0;
			for (int y = 1; y < gm.height - 1; y++) {
				OVertex n = nodes.get(z).get(y).get(y);
				List<OEdge> e = Lists.mutable.withInitialCapacity(8 * 8 * 1);
				e.add(db.newEdge(n, nodes.get(z).get(y + 1).get(x), Path.NPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z).get(y + 1).get(x + 1), Path.NePath.TYPE));
				e.add(db.newEdge(n, nodes.get(z).get(y).get(x + 1), Path.EPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z).get(y - 1).get(x + 1), Path.SePath.TYPE));
				e.add(db.newEdge(n, nodes.get(z).get(y - 1).get(x), Path.SPath.TYPE));
				// up
				e.add(db.newEdge(n, nodes.get(z + 1).get(y + 1).get(x), Path.UnPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z + 1).get(y + 1).get(x + 1), Path.UnePath.TYPE));
				e.add(db.newEdge(n, nodes.get(z + 1).get(y).get(x + 1), Path.UePath.TYPE));
				e.add(db.newEdge(n, nodes.get(z + 1).get(y - 1).get(x + 1), Path.UsePath.TYPE));
				e.add(db.newEdge(n, nodes.get(z + 1).get(y - 1).get(x), Path.UsPath.TYPE));
				// down
				e.add(db.newEdge(n, nodes.get(z - 1).get(y + 1).get(x), Path.DnPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z - 1).get(y + 1).get(x + 1), Path.DnePath.TYPE));
				e.add(db.newEdge(n, nodes.get(z - 1).get(y).get(x + 1), Path.DePath.TYPE));
				e.add(db.newEdge(n, nodes.get(z - 1).get(y - 1).get(x + 1), Path.DsePath.TYPE));
				e.add(db.newEdge(n, nodes.get(z - 1).get(y - 1).get(x), Path.DsPath.TYPE));
				// save
				for (OEdge edge : e) {
					edge.save();
				}
			}
			x = gm.width - 1;
			for (int y = 1; y < gm.height - 1; y++) {
				OVertex n = nodes.get(z).get(y).get(y);
				List<OEdge> e = Lists.mutable.withInitialCapacity(8 * 8 * 1);
				e.add(db.newEdge(n, nodes.get(z).get(y + 1).get(x), Path.NPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z).get(y - 1).get(x), Path.SPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z).get(y - 1).get(x - 1), Path.SwPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z).get(y).get(x - 1), Path.WPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z).get(y + 1).get(x - 1), Path.NwPath.TYPE));
				// up
				e.add(db.newEdge(n, nodes.get(z + 1).get(y + 1).get(x), Path.UnPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z + 1).get(y - 1).get(x), Path.UsPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z + 1).get(y - 1).get(x - 1), Path.UswPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z + 1).get(y).get(x - 1), Path.UwPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z + 1).get(y + 1).get(x - 1), Path.UnwPath.TYPE));
				// down
				e.add(db.newEdge(n, nodes.get(z - 1).get(y + 1).get(x), Path.DnPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z - 1).get(y - 1).get(x), Path.DsPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z - 1).get(y - 1).get(x - 1), Path.DswPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z - 1).get(y).get(x - 1), Path.DwPath.TYPE));
				e.add(db.newEdge(n, nodes.get(z - 1).get(y + 1).get(x - 1), Path.DnwPath.TYPE));
				// save
				for (OEdge edge : e) {
					edge.save();
				}
			}
		}
		System.out.println("done"); // TODO

	}

	private BehaviorBuilder<Message> getInitialBehavior() {
		return Behaviors.receive(Message.class)//
				.onMessage(GenerateMapMessage.class, this::onGenerateMap)//
		;
	}

}
