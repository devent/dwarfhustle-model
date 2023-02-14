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

import java.time.Duration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

import javax.inject.Inject;

import org.apache.commons.jcs3.JCS;
import org.apache.commons.jcs3.access.CacheAccess;
import org.apache.commons.jcs3.access.exception.CacheException;
import org.apache.commons.jcs3.engine.control.event.behavior.IElementEvent;

import com.anrisoftware.dwarfhustle.model.actor.ActorSystemProvider;
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos;
import com.anrisoftware.dwarfhustle.model.api.objects.GameMapPos;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;
import com.anrisoftware.dwarfhustle.model.api.objects.MapBlock;
import com.anrisoftware.dwarfhustle.model.db.orientdb.objects.LoadObjectMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.objects.LoadObjectMessage.LoadObjectErrorMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.objects.LoadObjectMessage.LoadObjectSuccessMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.objects.LoadObjectsMessage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.objects.ObjectsResponseMessage;
import com.google.inject.Injector;

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
 * Cache for {@link MapBlock} map blocks.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
public class MapBlocksJcsCacheActor extends AbstractJcsCacheActor<GameMapPos, MapBlock> {

	public static final ServiceKey<Message> KEY = ServiceKey.create(Message.class,
			MapBlocksJcsCacheActor.class.getSimpleName());

	public static final String NAME = MapBlocksJcsCacheActor.class.getSimpleName();

	public static final int ID = KEY.hashCode();

	@RequiredArgsConstructor
	@ToString(callSuper = true)
	private static class WrappedObjectsResponse extends Message {
        private final ObjectsResponseMessage<?> response;
	}

	/**
	 * Factory to create {@link MapBlocksJcsCacheActor}.
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	public interface MapBlocksJcsCacheActorFactory extends AbstractJcsCacheActorFactory {

		@Override
		MapBlocksJcsCacheActor create(ActorContext<Message> context, StashBuffer<Message> stash,
				Map<String, Object> params);
	}

	public static <K, V> Behavior<Message> create(Injector injector, AbstractJcsCacheActorFactory actorFactory,
			CompletionStage<CacheAccess<K, V>> initCacheAsync, Map<String, Object> params) {
		return AbstractJcsCacheActor.create(injector, actorFactory, initCacheAsync, params);
	}

	/**
	 * Creates the {@link MapBlocksJcsCacheActor}.
	 *
	 * @param injector the {@link Injector} injector.
	 * @param timeout  the {@link Duration} timeout.
	 * @param params   additional parameters:
	 *                 <ul>
	 *                 <li>parent_dir
	 *                 <li>mapid
	 *                 <li>width
	 *                 <li>height
	 *                 <li>depth
	 *                 <li>block_size
	 *                 </ul>
	 */
	public static CompletionStage<ActorRef<Message>> create(Injector injector, Duration timeout,
			Map<String, Object> params) {
		var system = injector.getInstance(ActorSystemProvider.class).getActorSystem();
		var actorFactory = injector.getInstance(MapBlocksJcsCacheActorFactory.class);
		var initCache = createInitCacheAsync(params);
		return createNamedActor(system, timeout, ID, KEY, NAME, create(injector, actorFactory, initCache, params));
	}

	public static CompletableFuture<CacheAccess<Object, Object>> createInitCacheAsync(Map<String, Object> params) {
		var initCache = CompletableFuture.supplyAsync(() -> createCache(params));
		return initCache;
	}

	private static CacheAccess<Object, Object> createCache(Map<String, Object> params) {
		try {
            validateParams(params);
			var mapid = params.get("mapid");
			var cacheName = "mapBlocksCache_" + mapid;
			var width = (int) params.get("width");
			var height = (int) params.get("height");
			var depth = (int) params.get("depth");
			var size = (int) params.get("block_size");
			var blocks = MapBlock.calculateBlocksCount(width, height, depth, size);
			params.put("cache_name", cacheName);
			params.put("max_objects", blocks);
			params.put("is_eternal", true);
			params.put("max_key_size", blocks);
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

    private static void validateParams(Map<String, Object> params) {
        assertThat(params, hasKey("parent_dir"));
        assertThat(params, hasKey("mapid"));
        assertThat(params, hasKey("width"));
        assertThat(params, hasKey("height"));
        assertThat(params, hasKey("depth"));
        assertThat(params, hasKey("block_size"));
    }

	@Inject
	private ActorSystemProvider actor;

    @SuppressWarnings("rawtypes")
    private ActorRef<ObjectsResponseMessage> objectsResponseAdapter;

	@Override
	protected Behavior<Message> initialStage(InitialStateMessage<GameMapPos, MapBlock> m) {
		log.debug("initialStage {}", m);
		this.objectsResponseAdapter = context.messageAdapter(ObjectsResponseMessage.class, WrappedObjectsResponse::new);
		return super.initialStage(m);
	}

	@Override
	public <T> void handleElementEvent(IElementEvent<T> event) {
	}

	@Override
	protected void retrieveValueFromDb(CacheGetMessage<?> m, Consumer<GameObject> consumer) {
		if (m.key instanceof GameBlockPos p) {
			retrieveMapBlock(p, consumer);
		}
	}

	@Override
    protected void storeValueDb(CachePutMessage<?, GameMapPos, MapBlock> m) {
	}

	/**
	 * <ul>
	 * <li>
	 * </ul>
	 */
	private Behavior<Message> onWrappedObjectsResponse(WrappedObjectsResponse m) {
		log.debug("onWrappedObjectsResponse {}", m);
		var response = m.response;
        if (response instanceof LoadObjectErrorMessage<?> rm) {
			log.error("Objects error", rm);
			return Behaviors.stopped();
        } else if (response instanceof LoadObjectSuccessMessage<?> rm) {
			if (rm.go instanceof MapBlock wm) {
				if (!wm.getBlocks().isEmpty()) {
					retrieveChildMapBlocks(wm.getPos());
				}
				var lm = rm.om;
				lm.consumer.accept(wm);
			}
		}
		return Behaviors.same();
	}

	private void retrieveChildMapBlocks(GameBlockPos p) {
        actor.tell(new LoadObjectsMessage<>(objectsResponseAdapter, MapBlock.OBJECT_TYPE, go -> {
			var mb = (MapBlock) go;
			cache.put(mb.getPos(), mb);
		}, db -> {
			var query = "SELECT * from ? where mapid = ? and sx >= ? and sy >= ? and sz >= ? and ex <= ? and ey <= ? and ez <= ?";
			return db.query(query, MapBlock.OBJECT_TYPE, p.getMapid(), p.getX(), p.getY(), p.getZ(),
					p.getEndPos().getX(), p.getEndPos().getY(), p.getEndPos().getZ());
		}));
	}

	private void retrieveMapBlock(GameBlockPos p, Consumer<GameObject> consumer) {
        actor.tell(new LoadObjectMessage<>(objectsResponseAdapter, MapBlock.OBJECT_TYPE, consumer, db -> {
			var query = "SELECT * from ? where objecttype = ? and mapid = ? and sx = ? and sy = ? and sz = ? and ex = ? and ey = ? and ez = ? limit 1";
			return db.query(query, MapBlock.OBJECT_TYPE, MapBlock.OBJECT_TYPE, p.getMapid(), p.getX(), p.getY(),
					p.getZ(), p.getEndPos().getX(), p.getEndPos().getY(), p.getEndPos().getZ());
		}));
	}

	@Override
	protected BehaviorBuilder<Message> getInitialBehavior() {
		return super.getInitialBehavior()//
				.onMessage(WrappedObjectsResponse.class, this::onWrappedObjectsResponse)//
		;
	}
}
