package com.anrisoftware.dwarfhustle.model.db.orientdb.actor

import java.time.Duration
import java.util.concurrent.CountDownLatch

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

import com.anrisoftware.dwarfhustle.model.actor.MainActorsModule
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DbResponseMessage.DbErrorMessage
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DbResponseMessage.DbSuccessMessage
import com.google.inject.Guice
import com.google.inject.Injector
import com.orientechnologies.orient.core.db.ODatabaseType

import akka.actor.testkit.typed.javadsl.ActorTestKit
import akka.actor.typed.ActorRef
import akka.actor.typed.javadsl.AskPattern
import groovy.util.logging.Slf4j

/**
 * @see OrientDbActor
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
class OrientDbActorTest {

	static final ActorTestKit testKit = ActorTestKit.create()

	static Injector injector

	static ActorRef<Message> orientDbActor

	static timeout = Duration.ofMinutes(10)

	@BeforeAll
	static void setupActor() {
		injector = Guice.createInjector(new MainActorsModule(), new OrientDbModule())
		orientDbActor = testKit.spawn(OrientDbActor.create(injector), "OrientDbActor");
	}

	@AfterAll
	static void closeDb() {
		deleteDatabase()
		orientDbActor.tell(new CloseDbMessage())
		testKit.shutdown(testKit.system(), timeout)
	}

	static void deleteDatabase() {
		def lock = new CountDownLatch(1)
		def result =
				AskPattern.ask(
				orientDbActor,
				{replyTo -> new DeleteDbMessage(replyTo)},
				timeout,
				testKit.scheduler())
		result.whenComplete( {reply, failure ->
			log.info "Delete database reply {} failure {}", reply, failure
			lock.countDown()
		})
		lock.await()
	}

	@Test
	void connect_db_message_create_db() {
		def lock = new CountDownLatch(1)
		def result =
				AskPattern.ask(
				orientDbActor,
				{replyTo -> new ConnectDbMessage(replyTo, "remote:localhost", "test", "root", "admin")},
				timeout,
				testKit.scheduler())
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

	void createDatabase(def lock) {
		def result =
				AskPattern.ask(
				orientDbActor,
				{replyTo -> new CreateDbMessage(replyTo, ODatabaseType.MEMORY)},
				timeout,
				testKit.scheduler())
		result.whenComplete( {reply, failure ->
			log.info "Create database reply {} failure {}", reply, failure
			executeCommand(lock)
		})
	}

	void executeCommand(def lock) {
		def result =
				AskPattern.ask(
				orientDbActor, {replyTo ->
					new DbCommandReplyMessage(replyTo, { db ->
						def cl = db.createClassIfNotExist("Person", "V")
						db.begin();
						def doc = db.newVertex(cl);
						doc.setProperty("name", "John");
						doc.save();
						db.commit()
					})
				},
				timeout,
				testKit.scheduler())
		result.whenComplete( {reply, failure ->
			log.info "Execute command reply {} failure {}", reply, failure
			lock.countDown()
		})
	}
}
