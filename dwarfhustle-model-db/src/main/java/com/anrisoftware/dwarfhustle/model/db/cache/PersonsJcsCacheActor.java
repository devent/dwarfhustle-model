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

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

import javax.inject.Inject;

import org.apache.commons.jcs3.JCS;
import org.apache.commons.jcs3.access.CacheAccess;
import org.apache.commons.jcs3.access.exception.CacheException;
import org.eclipse.collections.api.set.primitive.MutableLongSet;
import org.eclipse.collections.impl.factory.primitive.LongSets;

import com.anrisoftware.dwarfhustle.model.actor.ActorSystemProvider;
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.api.objects.GameMapPos;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObjectStorage;
import com.anrisoftware.dwarfhustle.model.api.objects.MapTile;
import com.anrisoftware.dwarfhustle.model.db.cache.CacheResponseMessage.CacheErrorMessage;
import com.anrisoftware.dwarfhustle.model.db.cache.CacheResponseMessage.CacheSuccessMessage;
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
public class PersonsJcsCacheActor extends AbstractJcsCacheActor<GameMapPos, MapTile> {

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
		PersonsJcsCacheActor create(ActorContext<Message> context, StashBuffer<Message> stash,
				Map<String, Object> params);
	}

	public static <K, V> Behavior<Message> create(Injector injector, ActorRef<Message> db,
			AbstractJcsCacheActorFactory actorFactory, CompletionStage<CacheAccess<K, V>> initCacheAsync,
			Map<String, Object> params) {
		params.put("db", db);
		return AbstractJcsCacheActor.create(injector, actorFactory, initCacheAsync, params);
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
		var initCache = CompletableFuture.supplyAsync(PersonsJcsCacheActor::loadCache);
		return initCache;
	}

	private static CacheAccess<Object, Object> loadCache() {
		try {
			return JCS.getInstance("personsCache");
		} catch (CacheException e) {
			throw new RuntimeException(e);
		}
	}

	@Inject
	private Map<String, GameObjectStorage> storages;

	private int mapid;

	private MutableLongSet goids;

	private GameObjectStorage storage;

	private ActorRef<Message> db;

	@SuppressWarnings("unchecked")
	@Override
	protected Behavior<Message> initialStage(InitialStateMessage<GameMapPos, MapTile> m) {
		log.debug("initialStage {}", m);
		this.db = (ActorRef<Message>) params.get("db");
		this.mapid = (int) params.get("mapid");
		this.goids = LongSets.mutable.withInitialCapacity(1000);
		this.storage = storages.get(MapTile.OBJECT_TYPE);
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
		CompletionStage<DbResponseMessage> stage = AskPattern.ask(db,
				replyTo -> new DbCommandReplyMessage(replyTo, ex -> null, this::loadMapTilesAsync), timeout, context.getSystem().scheduler());
		context.pipeToSelf(stage, (result, cause) -> {
			if (cause != null) {
				return new SetupErrorMessage(cause);
			} else {
				if (result instanceof DbErrorMessage m) {
					return new SetupErrorMessage(m.error);
				} else {
					return new MapTilesInitialLoadDoneMessage();
				}
			}
		});
	}

	private Void loadMapTilesAsync(ODatabaseDocument db) {
		var rs = db.query("SELECT FROM ? where objecttype = ? and mapid = ?", MapTile.OBJECT_TYPE, MapTile.OBJECT_TYPE,
				mapid);
		while (rs.hasNext()) {
			var item = rs.next();
			// var go = storage.load(item.toElement(), storage.create());
			// cache.put(go.getPos(), (MapTile) go);
			// goids.add(go.getId());
		}
		rs.close();
		return null;
	}

	@Override
	protected void retrieveValueFromDb(CacheGetMessage<?> m, Consumer<GameObject> consumer) {
		CompletionStage<DbResponseMessage> result = AskPattern.ask(db,
				replyTo -> new DbCommandReplyMessage(replyTo, ex -> null, db -> retrieveGameObjectAsync(db, m)), timeout, context.getSystem().scheduler());
		var future = result.toCompletableFuture();
		result.whenComplete((response, throwable) -> {
			translateDbResponse(m, response, throwable);
		});
		future.join();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void translateDbResponse(CacheGetMessage m, DbResponseMessage response, Throwable throwable) {
		if (throwable != null) {
			m.replyTo.tell(new CacheErrorMessage(m, throwable));
		} else {
			if (response instanceof DbSuccessMessage) {
				m.replyTo.tell(new CacheSuccessMessage(m));
			} else if (response instanceof DbErrorMessage ret) {
				m.replyTo.tell(new CacheErrorMessage(m, ret.error));
			}
		}
	}

	private MapTile retrieveGameObjectAsync(ODatabaseDocument db, CacheGetMessage<?> m) {
		var pos = (GameMapPos) m.key;
		var rs = db.query("SELECT FROM ? where objecttype = ? and mapid = ? and x = ? and y = ? and z = ?",
				MapTile.OBJECT_TYPE, pos.getMapid(), pos.getX(), pos.getY(), pos.getZ());
		MapTile go = null;
		while (rs.hasNext()) {
			var item = rs.next();
			// go = (MapTile) storage.load(item, storage.create());
			cache.put(go.getPos(), go);
		}
		rs.close();
		return go;
	}

	@Override
	protected void storeValueDb(CachePutMessage<?> m) {
		CompletionStage<DbResponseMessage> result = AskPattern.ask(db,
				replyTo -> new DbCommandReplyMessage(replyTo, ex -> null, db -> storeValueDbAsync(db, m)), timeout, context.getSystem().scheduler());
		result.whenComplete((response, throwable) -> {
			if (throwable != null) {
				tellCacheError(m, throwable);
			}
		});
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void tellCacheError(CachePutMessage m, Throwable throwable) {
		m.replyTo.tell(new CacheErrorMessage(m, throwable));
	}

	private Void storeValueDbAsync(ODatabaseDocument db, CachePutMessage<?> m) {
		var go = (MapTile) m.value;
		if (!go.isDirty()) {
			return null;
		}
		var rid = (ORID) go.getRid();
		OVertex v = null;
		if (rid == null) {
			v = db.newVertex(go.getObjectType());
		}
		// storage.save(v, go);
		return null;
	}
}
