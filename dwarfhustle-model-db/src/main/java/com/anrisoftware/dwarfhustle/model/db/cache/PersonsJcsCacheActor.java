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

import org.apache.commons.jcs3.JCS;
import org.apache.commons.jcs3.access.CacheAccess;
import org.apache.commons.jcs3.access.exception.CacheException;
import org.eclipse.collections.api.set.primitive.MutableLongSet;
import org.eclipse.collections.impl.factory.primitive.LongSets;

import com.anrisoftware.dwarfhustle.model.actor.ActorSystemProvider;
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.api.GameMapPosition;
import com.anrisoftware.dwarfhustle.model.api.GameObjectStorage;
import com.anrisoftware.dwarfhustle.model.api.MapTile;
import com.anrisoftware.dwarfhustle.model.db.cache.AbstractCacheReplyMessage.CacheErrorMessage;
import com.anrisoftware.dwarfhustle.model.db.cache.AbstractCacheReplyMessage.CacheSuccessMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DbCommandReplyMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DbResponseMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DbResponseMessage.DbErrorMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DbResponseMessage.DbSuccessMessage;
import com.google.inject.Injector;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.record.OVertex;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.AskPattern;
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
public class PersonsJcsCacheActor extends AbstractJcsCacheActor<GameMapPosition, MapTile> {

	public static final ServiceKey<Message> KEY = ServiceKey.create(Message.class,
			PersonsJcsCacheActor.class.getSimpleName());

	public static final String NAME = PersonsJcsCacheActor.class.getSimpleName();

	public static final int ID = KEY.hashCode();

	/**
	 * Message that the {@link MapTile} map tiles were loaded initially from the
	 * database into the cache.
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	@RequiredArgsConstructor
	@ToString(callSuper = true)
	public static class MapTilesInitialLoadDoneMessage extends Message {

	}

	/**
	 * Factory to create {@link PersonsJcsCacheActor}.
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	public interface PersonsJcsCacheActorFactory extends AbstractJcsCacheActorFactory {

		@Override
		PersonsJcsCacheActor create(ActorContext<Message> context, StashBuffer<Message> stash, ActorRef<Message> db,
				Map<String, Object> params);
	}

	public static <K, V> Behavior<Message> create(Injector injector, ActorRef<Message> db,
			AbstractJcsCacheActorFactory actorFactory, CompletionStage<CacheAccess<K, V>> initCacheAsync,
			Map<String, Object> params) {
		return AbstractJcsCacheActor.create(injector, db, actorFactory, initCacheAsync, params);
	}

	/**
	 * Creates the {@link PersonsJcsCacheActor}.
	 */
	public static CompletionStage<ActorRef<Message>> create(Injector injector, Duration timeout, ActorRef<Message> db,
			Map<String, Object> params) {
		var system = injector.getInstance(ActorSystemProvider.class).getActorSystem();
		var actorFactory = injector.getInstance(PersonsJcsCacheActorFactory.class);
		var initCache = createInitCacheAsync();
		return createNamedActor(system, timeout, ID, KEY, NAME, create(injector, db, actorFactory, initCache, params));
	}

	public static CompletableFuture<CacheAccess<Object, Object>> createInitCacheAsync() {
		var initCache = CompletableFuture.supplyAsync(() -> {
			return loadCache();
		});
		return initCache;
	}

	private static CacheAccess<Object, Object> loadCache() {
		try {
			return JCS.getInstance("personsCache");
		} catch (CacheException e) {
			throw new RuntimeException(e);
		}
	}

	private int mapid;

	private MutableLongSet goids;

	private GameObjectStorage storage;

	@Override
	protected Behavior<Message> initialStage(InitialStateMessage<GameMapPosition, MapTile> m) {
		log.debug("initialStage {}", m);
		this.mapid = (int) params.get("mapid");
		this.goids = LongSets.mutable.withInitialCapacity(1000);
		this.storage = storages.get(MapTile.TYPE);
		loadMapTiles();
		return Behaviors.receive(Message.class)//
				.onMessage(MapTilesInitialLoadDoneMessage.class, this::onMapTilesInitialLoadDone)//
				.onMessage(SetupErrorMessage.class, this::onSetupError)//
				.onMessage(Message.class, this::stashOtherCommand)//
				.build();
	}

	private Behavior<Message> onMapTilesInitialLoadDone(MapTilesInitialLoadDoneMessage m) {
		log.debug("onMapTilesInitialLoadDone {}", m);
		return buffer.unstashAll(getInitialBehavior()//
				.build());
	}

	private void loadMapTiles() {
		CompletionStage<DbResponseMessage> stage = AskPattern.ask(db, replyTo -> new DbCommandReplyMessage(replyTo, db -> {
			return loadMapTilesAsync(db);
		}), timeout, context.getSystem().scheduler());
		context.pipeToSelf(stage, (result, cause) -> {
			if (cause != null) {
				return new SetupErrorMessage(cause);
			} else {
				if (result instanceof DbErrorMessage) {
					DbErrorMessage m = (DbErrorMessage) result;
					return new SetupErrorMessage(m.error);
				} else {
					return new MapTilesInitialLoadDoneMessage();
				}
			}
		});
	}

	private Void loadMapTilesAsync(ODatabaseDocument db) {
		var rs = db.query("SELECT FROM ? where objecttype = ? and mapid = ?", MapTile.TYPE, MapTile.TYPE, mapid);
		while (rs.hasNext()) {
			var item = rs.next();
			var go = storage.load(item.toElement(), storage.create());
			cache.put(go.getPos(), (MapTile) go);
			goids.add(go.getId());
		}
		rs.close();
		return null;
	}

	@Override
	protected MapTile retrieveValueFromDb(GetMessage m) {
		CompletionStage<DbResponseMessage> result = AskPattern.ask(db, replyTo -> new DbCommandReplyMessage(replyTo, db -> {
			return retrieveGameObjectAsync(db, m);
		}), timeout, context.getSystem().scheduler());
		var future = result.toCompletableFuture();
		result.whenComplete((response, throwable) -> {
			translateDbResponse(m, response, throwable);
		});
		future.join();
		return null;
	}

	private void translateDbResponse(GetMessage m, DbResponseMessage response, Throwable throwable) {
		if (throwable != null) {
			m.replyTo.tell(new CacheErrorMessage(m, throwable));
		} else {
			if (response instanceof DbSuccessMessage) {
				m.replyTo.tell(new CacheSuccessMessage(m));
			} else if (response instanceof DbErrorMessage) {
				var ret = (DbErrorMessage) response;
				m.replyTo.tell(new CacheErrorMessage(m, ret.error));
			}
		}
	}

	private MapTile retrieveGameObjectAsync(ODatabaseDocument db, GetMessage m) {
		var pos = (GameMapPosition) m.key;
		var rs = db.query("SELECT FROM ? where objecttype = ? and mapid = ? and x = ? and y = ? and z = ?",
				MapTile.TYPE, pos.getMapid(), pos.getX(), pos.getY(), pos.getZ());
		MapTile go = null;
		while (rs.hasNext()) {
			var item = rs.next();
			go = (MapTile) storage.load(item, storage.create());
			cache.put(go.getPos(), go);
		}
		rs.close();
		return go;
	}

	@Override
	protected void storeValueDb(PutMessage m) {
		CompletionStage<DbResponseMessage> result = AskPattern.ask(db, replyTo -> new DbCommandReplyMessage(replyTo, db -> {
			return storeValueDbAsync(db, m);
		}), timeout, context.getSystem().scheduler());
		result.whenComplete((response, throwable) -> {
			if (throwable != null) {
				m.replyTo.tell(new CacheErrorMessage(m, throwable));
			}
		});
	}

	private Void storeValueDbAsync(ODatabaseDocument db, PutMessage m) {
		var go = (MapTile) m.value;
		if (!go.isDirty()) {
			return null;
		}
		var rid = (ORID) go.getRid();
		OVertex v = null;
		if (rid == null) {
			v = db.newVertex(go.getType());
		}
		storage.save(v, go);
		return null;
	}
}
