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
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.apache.commons.jcs3.JCS;
import org.apache.commons.jcs3.access.CacheAccess;
import org.apache.commons.jcs3.access.exception.CacheException;
import org.apache.commons.jcs3.engine.control.event.behavior.IElementEvent;
import org.eclipse.collections.api.set.primitive.MutableLongSet;
import org.eclipse.collections.impl.factory.primitive.LongSets;

import com.anrisoftware.dwarfhustle.model.actor.ActorSystemProvider;
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.api.GameMapPos;
import com.anrisoftware.dwarfhustle.model.api.MapTile;
import com.google.inject.Injector;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.StashBuffer;
import akka.actor.typed.receptionist.ServiceKey;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
public class MapTilesJcsCacheActor extends AbstractJcsCacheActor<GameMapPos, MapTile> {

	public static final ServiceKey<Message> KEY = ServiceKey.create(Message.class,
			MapTilesJcsCacheActor.class.getSimpleName());

	public static final String NAME = MapTilesJcsCacheActor.class.getSimpleName();

	public static final int ID = KEY.hashCode();

	/**
	 * Factory to create {@link MapTilesJcsCacheActor}.
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	public interface MapTilesJcsCacheActorFactory extends AbstractJcsCacheActorFactory {

		@Override
		MapTilesJcsCacheActor create(ActorContext<Message> context, StashBuffer<Message> stash,
				Map<String, Object> params);
	}

	public static <K, V> Behavior<Message> create(Injector injector, AbstractJcsCacheActorFactory actorFactory,
			CompletionStage<CacheAccess<K, V>> initCacheAsync, Map<String, Object> params) {
		return AbstractJcsCacheActor.create(injector, actorFactory, initCacheAsync, params);
	}

	/**
	 * Creates the {@link MapTilesJcsCacheActor}.
	 */
	public static CompletionStage<ActorRef<Message>> create(Injector injector, Duration timeout,
			Map<String, Object> params) {
		var system = injector.getInstance(ActorSystemProvider.class).getActorSystem();
		var actorFactory = injector.getInstance(MapTilesJcsCacheActorFactory.class);
		var initCache = createInitCacheAsync(params);
		return createNamedActor(system, timeout, ID, KEY, NAME, create(injector, actorFactory, initCache, params));
	}

	public static CompletableFuture<CacheAccess<Object, Object>> createInitCacheAsync(Map<String, Object> params) {
		var initCache = CompletableFuture.supplyAsync(() -> {
			return createCache(params);
		});
		return initCache;
	}

	private static CacheAccess<Object, Object> createCache(Map<String, Object> params) {
		try {
			var mapid = params.get("mapid");
			var cacheName = "mapTilesCache_" + mapid;
			var width = (int) params.get("width");
			var height = (int) params.get("height");
			var depth = (int) params.get("depth");
			params.put("cache_name", cacheName);
			params.put("max_objects", width * height * depth);
			params.put("is_eternal", true);
			params.put("max_key_size", width * height * depth);
			var config = new Properties();
			createFileAuxCache(config, params);
			config.put("jcs.region." + cacheName, cacheName + "_file");
			createCache(config, params);
			JCS.setConfigProperties(config);
			return JCS.getInstance(cacheName);
		} catch (CacheException e) {
			throw new RuntimeException(e);
		}
	}

	private int mapid;

	private int width;

	private int height;

	private int depth;

	private MutableLongSet ids;

	@Override
	protected Behavior<Message> initialStage(InitialStateMessage<GameMapPos, MapTile> m) {
		log.debug("initialStage {}", m);
		this.mapid = (int) params.get("mapid");
		this.width = (int) params.get("width");
		this.height = (int) params.get("height");
		this.depth = (int) params.get("depth");
		this.ids = LongSets.mutable.withInitialCapacity(width * height * depth);
		return super.initialStage(m);
	}

	@Override
	public <T> void handleElementEvent(IElementEvent<T> event) {
	}

	@Override
	protected MapTile retrieveValueFromDb(AbstractGetMessage<?> m) {
		return null;
	}

	@Override
	protected void storeValueDb(AbstractPutMessage<?> m) {
	}
}
