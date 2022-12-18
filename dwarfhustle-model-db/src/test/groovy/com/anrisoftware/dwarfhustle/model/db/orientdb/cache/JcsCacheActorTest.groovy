package com.anrisoftware.dwarfhustle.model.db.orientdb.cache

import java.time.Duration
import java.util.concurrent.CountDownLatch

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

import com.anrisoftware.dwarfhustle.model.actor.MainActorsModule
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message
import com.anrisoftware.dwarfhustle.model.api.MapTile
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

	@BeforeAll
	static void setupActor() {
		injector = Guice.createInjector(new MainActorsModule(), new OrientDbModule(), new JcsCacheModule(), new ObjectsModule())
		orientDbActor = testKit.spawn(OrientDbActor.create(injector), "OrientDbActor");
		jcsCacheActor = testKit.spawn(JcsCacheActor.create(injector, orientDbActor), "JcsCacheActor");
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
					break
			}
		})
		lock.await()
	}

	static void createDatabase(def lock) {
		def result =
				AskPattern.ask(
				orientDbActor,
				{replyTo -> new CreateDbMessage(replyTo, ODatabaseType.PLOCAL)},
				Duration.ofSeconds(15),
				testKit.scheduler())
		result.whenComplete( {reply, failure ->
			log.info "Create database reply {} failure {}", reply, failure
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
	void put_map_tile() {
		def go = new MapTile()
		go.x = 10
		go.y = 20
		go.z = 1
		go.material = "Sandstone"
		def result =
				AskPattern.ask(
				jcsCacheActor,
				{replyTo -> new PutMessage(replyTo, go)},
				Duration.ofSeconds(300),
				testKit.scheduler())
		def lock = new CountDownLatch(1)
		result.whenComplete( {reply, failure ->
			log.info "Put map tile reply {} failure {}", reply, failure
			lock.countDown()
		})
		lock.await()
	}
}
