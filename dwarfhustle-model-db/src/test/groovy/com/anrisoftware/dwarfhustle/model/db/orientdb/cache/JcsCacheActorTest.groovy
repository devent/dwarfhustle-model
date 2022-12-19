package com.anrisoftware.dwarfhustle.model.db.orientdb.cache

import java.time.Duration
import java.util.concurrent.CountDownLatch

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.lable.oss.uniqueid.IDGenerator

import com.anrisoftware.dwarfhustle.model.actor.MainActorsModule
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message
import com.anrisoftware.dwarfhustle.model.api.ApiModule
import com.anrisoftware.dwarfhustle.model.api.GameMapPosition
import com.anrisoftware.dwarfhustle.model.api.GameObjectPositionIndex
import com.anrisoftware.dwarfhustle.model.api.MapTile
import com.anrisoftware.dwarfhustle.model.db.cache.GetMessage
import com.anrisoftware.dwarfhustle.model.db.cache.JcsCacheActor
import com.anrisoftware.dwarfhustle.model.db.cache.JcsCacheModule
import com.anrisoftware.dwarfhustle.model.db.cache.PutMessage
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.AbstractDbReplyMessage.DbErrorMessage
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.AbstractDbReplyMessage.DbSuccessMessage
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.CloseDbMessage
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.ConnectDbMessage
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.CreateDbMessage
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DeleteDbMessage
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.OrientDbActor
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.OrientDbModule
import com.anrisoftware.dwarfhustle.model.db.orientdb.objects.CreateSchemasMessage
import com.anrisoftware.dwarfhustle.model.db.orientdb.objects.ObjectsActor
import com.anrisoftware.dwarfhustle.model.db.orientdb.objects.ObjectsModule
import com.google.inject.Guice
import com.google.inject.Injector
import com.orientechnologies.orient.core.db.ODatabaseType

import akka.actor.testkit.typed.javadsl.ActorTestKit
import akka.actor.typed.ActorRef
import akka.actor.typed.javadsl.AskPattern
import groovy.util.logging.Slf4j

/**
 * @see JcsCacheActor
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
class JcsCacheActorTest {

	static final ActorTestKit testKit = ActorTestKit.create()

	static Injector injector

	static ActorRef<Message> orientDbActor

	static ActorRef<Message> jcsCacheActor

	static ActorRef<Message> objectsActor

	static IDGenerator generator

	@BeforeAll
	static void setupActor() {
		injector = Guice.createInjector(new MainActorsModule(), new OrientDbModule(), new JcsCacheModule(), new ObjectsModule(), new ApiModule())
		orientDbActor = testKit.spawn(OrientDbActor.create(injector), "OrientDbActor");
		jcsCacheActor = testKit.spawn(JcsCacheActor.create(injector, orientDbActor), "JcsCacheActor");
		objectsActor = testKit.spawn(ObjectsActor.create(injector, orientDbActor), "objectsActor");
		generator = injector.getInstance(IDGenerator.class)
		connectCreateDatabase()
	}

	@AfterAll
	static void closeDb() {
		deleteDatabase()
		orientDbActor.tell(new CloseDbMessage())
		testKit.shutdown(testKit.system(), Duration.ofMinutes(1))
	}

	static void connectCreateDatabase() {
		def result =
				AskPattern.ask(
				orientDbActor,
				{replyTo -> new ConnectDbMessage(replyTo, "remote:172.20.0.2", "test", "root", "admin")},
				Duration.ofSeconds(3),
				testKit.scheduler())
		def lock = new CountDownLatch(1)
		result.whenComplete( {reply, failure ->
			log.info "Connect database reply {} failure {}", reply, failure
			switch (reply) {
				case DbSuccessMessage:
					createDatabase(lock)
					break
				case DbErrorMessage:
					lock.countDown()
					break
			}
		})
		lock.await()
	}

	static void createDatabase(def lock) {
		def result =
				AskPattern.ask(
				orientDbActor,
				{replyTo -> new CreateDbMessage(replyTo, ODatabaseType.MEMORY)},
				Duration.ofSeconds(60),
				testKit.scheduler())
		result.whenComplete( {reply, failure ->
			log.info "Create database reply ${reply} failure ${failure}"
			createSchemas(lock)
		})
	}

	static void createSchemas(def lock) {
		def result =
				AskPattern.ask(
				objectsActor,
				{replyTo -> new CreateSchemasMessage(replyTo)},
				Duration.ofSeconds(15),
				testKit.scheduler())
		result.whenComplete( {reply, failure ->
			log.info "Create schemas reply {} failure {}", reply, failure
			lock.countDown()
		})
	}

	static void deleteDatabase() {
		def result =
				AskPattern.ask(
				orientDbActor,
				{replyTo -> new DeleteDbMessage(replyTo)},
				Duration.ofSeconds(15),
				testKit.scheduler())
		def lock = new CountDownLatch(1)
		result.whenComplete( {reply, failure ->
			log.info "Delete database reply {} failure {}", reply, failure
			lock.countDown()
		})
		lock.await()
	}

	@Test
	@Order(1)
	void put_map_tile_by_id() {
		def go = new MapTile(generator.generate())
		go.pos = new GameMapPosition(10, 20, 1)
		go.material = "Sandstone"
		def result =
				AskPattern.ask(
				jcsCacheActor,
				{replyTo -> new PutMessage(replyTo, go.id, go)},
				Duration.ofSeconds(300),
				testKit.scheduler())
		def lock = new CountDownLatch(1)
		result.whenComplete( {reply, failure ->
			log.info "Put map tile reply {} failure {}", reply, failure
			lock.countDown()
		})
		lock.await()
	}

	@Test
	@Order(2)
	void put_map_tile_by_pos_index() {
		def go = new MapTile(generator.generate())
		go.pos = new GameMapPosition(11, 20, 1)
		go.material = "Sandstone"
		def result =
				AskPattern.ask(
				jcsCacheActor,
				{replyTo -> new PutMessage(replyTo, new GameObjectPositionIndex(MapTile.TYPE, go.pos), go)},
				Duration.ofSeconds(300),
				testKit.scheduler())
		def lock = new CountDownLatch(1)
		result.whenComplete( {reply, failure ->
			log.info "Put map tile reply {} failure {}", reply, failure
			lock.countDown()
		})
		lock.await()
	}

	@Test
	@Order(10)
	void get_map_tile_by_pos_index() {
		def result =
				AskPattern.ask(
				jcsCacheActor,
				{replyTo -> new GetMessage(replyTo, new GameObjectPositionIndex(MapTile.TYPE, new GameMapPosition(11, 20, 1)))},
				Duration.ofSeconds(300),
				testKit.scheduler())
		def lock = new CountDownLatch(1)
		result.whenComplete( {reply, failure ->
			log.info "Get map tile reply {} failure {}", reply, failure
			lock.countDown()
		})
		lock.await()
	}
}
