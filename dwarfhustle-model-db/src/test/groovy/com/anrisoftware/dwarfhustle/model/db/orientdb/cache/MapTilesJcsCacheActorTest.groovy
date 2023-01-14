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
import org.junit.jupiter.api.Timeout
import org.lable.oss.uniqueid.IDGenerator

import com.anrisoftware.dwarfhustle.model.actor.MainActorsModule
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message
import com.anrisoftware.dwarfhustle.model.api.ApiModule
import com.anrisoftware.dwarfhustle.model.api.GameMapPos
import com.anrisoftware.dwarfhustle.model.api.MapTile
import com.anrisoftware.dwarfhustle.model.db.cache.AbstractGetMessage.GetReplyMessage
import com.anrisoftware.dwarfhustle.model.db.cache.AbstractGetMessage.GetSuccessMessage
import com.anrisoftware.dwarfhustle.model.db.cache.AbstractPutMessage.PutReplyMessage
import com.anrisoftware.dwarfhustle.model.db.cache.JcsCacheModule
import com.anrisoftware.dwarfhustle.model.db.cache.MapTilesJcsCacheActor
import com.anrisoftware.dwarfhustle.model.db.cache.MapTilesJcsCacheActor.MapTilesJcsCacheActorFactory
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.OrientDbModule
import com.anrisoftware.dwarfhustle.model.db.orientdb.objects.ObjectsDbModule
import com.google.inject.Guice
import com.google.inject.Injector

import akka.actor.testkit.typed.javadsl.ActorTestKit
import akka.actor.typed.ActorRef
import akka.actor.typed.javadsl.AskPattern
import groovy.util.logging.Slf4j

/**
 * @see MapTilesJcsCacheActor
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
@TestMethodOrder(OrderAnnotation.class)
class MapTilesJcsCacheActorTest {

	static final ActorTestKit testKit = ActorTestKit.create()

	static Injector injector

	static ActorRef<Message> mapTilesCacheActor

	static ActorRef<Message> objectsActor

	static IDGenerator generator

	static timeout = Duration.ofSeconds(15)

	static mapTilesParams

	@BeforeAll
	static void setupActor() {
		mapTilesParams = [game_name: "test", mapid: 0, width: 16, height: 16, depth: 10]
		injector = Guice.createInjector(new MainActorsModule(), new OrientDbModule(), new JcsCacheModule(), new ObjectsDbModule(), new ApiModule())
		generator = injector.getInstance(IDGenerator.class)
		mapTilesCacheActor = testKit.spawn(MapTilesJcsCacheActor.create(injector, injector.getInstance(MapTilesJcsCacheActorFactory), MapTilesJcsCacheActor.createInitCacheAsync(mapTilesParams), mapTilesParams), "MapTilesJcsCacheActor");
	}

	@AfterAll
	static void shutdownTest() {
		testKit.shutdown(testKit.system(), timeout)
	}

	@Test
	@Timeout(600)
	@Order(1)
	void put_map_tile_by_pos_index() {
		def go = new MapTile(generator.generate())
		go.pos = new GameMapPos(0, 11, 20, 1)
		go.material = "Sandstone"
		def lock = new CountDownLatch(1)
		def result =
				AskPattern.ask(
				mapTilesCacheActor,
				{replyTo -> new PutReplyMessage(replyTo, go.pos, go)},
				timeout,
				testKit.scheduler())
		result.whenComplete( {reply, failure ->
			log_reply_failure "put_map_tile_by_pos_index", reply, failure
			lock.countDown()
		})
		lock.await()
	}

	@Test
	@Timeout(600)
	@Order(10)
	void get_map_tile_by_pos_index() {
		def lock = new CountDownLatch(1)
		def result =
				AskPattern.ask(
				mapTilesCacheActor,
				{replyTo -> new GetReplyMessage(replyTo, MapTile.TYPE, new GameMapPos(0, 11, 20, 1))},
				timeout,
				testKit.scheduler())
		result.whenComplete( {reply, failure ->
			log_reply_failure "get_map_tile_by_pos_index", reply, failure
			switch (reply) {
				case GetSuccessMessage:
					assert reply.go.pos == new GameMapPos(0, 11, 20, 1)
					assert reply.go.material == "Sandstone"
					break
				default:
					assert false
					break
			}
			lock.countDown()
		})
		lock.await()
	}
}
