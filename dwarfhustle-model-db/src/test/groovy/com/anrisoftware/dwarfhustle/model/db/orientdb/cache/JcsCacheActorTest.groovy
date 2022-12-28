package com.anrisoftware.dwarfhustle.model.db.orientdb.cache

import static com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DbTestUtils.*

import java.time.Duration
import java.util.concurrent.CountDownLatch

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.lable.oss.uniqueid.IDGenerator

import com.anrisoftware.dwarfhustle.model.actor.MainActorsModule
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message
import com.anrisoftware.dwarfhustle.model.api.ApiModule
import com.anrisoftware.dwarfhustle.model.api.GameMapPosition
import com.anrisoftware.dwarfhustle.model.api.MapTile
import com.anrisoftware.dwarfhustle.model.db.cache.GetMessage
import com.anrisoftware.dwarfhustle.model.db.cache.GetMessage.GetSuccessMessage
import com.anrisoftware.dwarfhustle.model.db.cache.JcsCacheModule
import com.anrisoftware.dwarfhustle.model.db.cache.MapTilesJcsCacheActor
import com.anrisoftware.dwarfhustle.model.db.cache.MapTilesJcsCacheActor.MapTilesJcsCacheActorFactory
import com.anrisoftware.dwarfhustle.model.db.cache.PutMessage
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.CloseDbMessage
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.OrientDbActor
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.OrientDbModule
import com.anrisoftware.dwarfhustle.model.db.orientdb.objects.ObjectsActor
import com.anrisoftware.dwarfhustle.model.db.orientdb.objects.ObjectsModule
import com.google.inject.Guice
import com.google.inject.Injector

import akka.actor.testkit.typed.javadsl.ActorTestKit
import akka.actor.typed.ActorRef
import akka.actor.typed.javadsl.AskPattern
import groovy.util.logging.Slf4j

/**
 * @see JcsCacheActor
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
@TestMethodOrder(OrderAnnotation.class)
class JcsCacheActorTest {

	static final ActorTestKit testKit = ActorTestKit.create()

	static Injector injector

	static ActorRef<Message> orientDbActor

	static ActorRef<Message> mapTilesCacheActor

	static ActorRef<Message> objectsActor

	static IDGenerator generator

	static timeout = Duration.ofSeconds(15)

	static mapTilesParams

	static initDatabaseLock = new CountDownLatch(1)

	static deleteDatabaseLock = new CountDownLatch(1)

	@BeforeAll
	static void setupActor() {
		mapTilesParams = [mapid: 0, width: 16, height: 16, depth: 10]
		injector = Guice.createInjector(new MainActorsModule(), new OrientDbModule(), new JcsCacheModule(), new ObjectsModule(), new ApiModule())
		orientDbActor = testKit.spawn(OrientDbActor.create(injector), "OrientDbActor");
		objectsActor = testKit.spawn(ObjectsActor.create(injector, orientDbActor), "objectsActor");
		generator = injector.getInstance(IDGenerator.class)
		connectCreateDatabase(orientDbActor, objectsActor, timeout, testKit, generator, initDatabaseLock)
		initDatabaseLock.await()
		mapTilesCacheActor = testKit.spawn(MapTilesJcsCacheActor.create(injector, orientDbActor, injector.getInstance(MapTilesJcsCacheActorFactory), MapTilesJcsCacheActor.createInitCacheAsync(), mapTilesParams), "JcsCacheActor");
	}

	@AfterAll
	static void closeDb() {
		deleteDatabase(orientDbActor, timeout, testKit, deleteDatabaseLock)
		orientDbActor.tell(new CloseDbMessage())
		testKit.shutdown(testKit.system(), timeout)
		deleteDatabaseLock.await()
	}

	@Test
	@Order(1)
	void put_map_tile_by_id() {
		def go = new MapTile(generator.generate())
		go.pos = new GameMapPosition(0, 10, 20, 1)
		go.material = "Sandstone"
		def result =
				AskPattern.ask(
				mapTilesCacheActor,
				{replyTo -> new PutMessage(replyTo, go.id, go)},
				timeout,
				testKit.scheduler())
		def future = result.toCompletableFuture()
		result.whenComplete( {reply, failure ->
			log_reply_failure "put_map_tile_by_id", reply, failure
		})
		future.join()
	}

	@Test
	@Order(2)
	void put_map_tile_by_pos_index() {
		def go = new MapTile(generator.generate())
		go.pos = new GameMapPosition(0, 11, 20, 1)
		go.material = "Sandstone"
		def result =
				AskPattern.ask(
				mapTilesCacheActor,
				{replyTo -> new PutMessage(replyTo, go.pos, go)},
				timeout,
				testKit.scheduler())
		def future = result.toCompletableFuture()
		result.whenComplete( {reply, failure ->
			log_reply_failure "put_map_tile_by_pos_index", reply, failure
		})
		future.join()
	}

	@Test
	@Order(10)
	void get_map_tile_by_pos_index() {
		def result =
				AskPattern.ask(
				mapTilesCacheActor,
				{replyTo -> new GetMessage(replyTo, MapTile.TYPE, new GameMapPosition(0, 11, 20, 1))},
				timeout,
				testKit.scheduler())
		def future = result.toCompletableFuture()
		result.whenComplete( {reply, failure ->
			log_reply_failure "get_map_tile_by_pos_index", reply, failure
			switch (reply) {
				case GetSuccessMessage:
					assert reply.go.pos == new GameMapPosition(0, 11, 20, 1)
					assert reply.go.material == "Sandstone"
					break
				default:
					assert false
					break
			}
		})
		future.join()
	}

	@Test
	@Order(10)
	void get_map_tile_from_db_by_pos_index() {
		def result =
				AskPattern.ask(
				mapTilesCacheActor,
				{replyTo -> new GetMessage(replyTo, MapTile.TYPE, new GameMapPosition(0, 10, 20, 2))},
				timeout,
				testKit.scheduler())
		def future = result.toCompletableFuture()
		result.whenComplete( {reply, failure ->
			log_reply_failure "get_map_tile_from_db_by_pos_index", reply, failure
		})
		future.join()
	}
}
