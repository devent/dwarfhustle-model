package com.anrisoftware.dwarfhustle.model.db.orientdb.actor

import java.time.Duration

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message
import com.anrisoftware.dwarfhustle.model.api.GameMapPosition
import com.anrisoftware.dwarfhustle.model.api.MapTile
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.ConnectDbMessage.ConnectDbSuccessMessage
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DbResponseMessage.DbErrorMessage
import com.anrisoftware.dwarfhustle.model.db.orientdb.objects.CreateSchemasMessage
import com.anrisoftware.dwarfhustle.model.db.orientdb.objects.MapTileStorage
import com.orientechnologies.orient.core.db.ODatabaseType
import com.orientechnologies.orient.core.db.OrientDB

import akka.actor.typed.ActorRef
import akka.actor.typed.javadsl.AskPattern
import groovy.util.logging.Slf4j

/**
 * Utilities to connect, create and delete a database for testing.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
class DbTestUtils {

	static boolean fillDatabase = true

	static final url = "remote:localhost"

	static final database = "test"

	static final user = "root"

	static final password = "admin"

	static final type = ODatabaseType.PLOCAL

	static OrientDB db

	static Duration timeout = Duration.ofSeconds(600)

	static void connectCreateDatabase(ActorRef<Message> orientDbActor, ActorRef<Message> objectsActor, def testKit, def generator, def initDatabaseLock) {
		def result =
				AskPattern.ask(
				orientDbActor,
				{replyTo -> new ConnectDbMessage(replyTo, "remote:localhost", "test", "root", "admin")},
				timeout,
				testKit.scheduler())
		result.whenComplete({reply, failure ->
			log_reply_failure "connectCreateDatabase", reply, failure
			switch (reply) {
				case ConnectDbSuccessMessage:
					db = reply.db
					createDatabase(orientDbActor, objectsActor, testKit, generator, initDatabaseLock)
					break
				case DbErrorMessage:
					throw reply.error
					break
			}
		})
	}

	static void createDatabase(ActorRef<Message> orientDbActor, ActorRef<Message> objectsActor, def testKit, def generator, def initDatabaseLock) {
		def result =
				AskPattern.ask(
				orientDbActor,
				{replyTo -> new CreateDbMessage(replyTo, type)},
				timeout,
				testKit.scheduler())
		result.whenComplete( {reply, failure ->
			log_reply_failure "createDatabase", reply, failure
			createSchemas(orientDbActor, objectsActor, testKit, generator, initDatabaseLock)
		})
	}

	static void createSchemas(ActorRef<Message> orientDbActor, ActorRef<Message> objectsActor, def testKit, def generator, def initDatabaseLock) {
		def result =
				AskPattern.ask(
				objectsActor,
				{replyTo -> new CreateSchemasMessage(replyTo)},
				timeout,
				testKit.scheduler())
		result.whenComplete( {reply, failure ->
			log_reply_failure "createSchemas", reply, failure
			if (fillDatabase) {
				fillDatabase(orientDbActor, testKit, generator, initDatabaseLock)
			} else {
				initDatabaseLock.countDown()
			}
		})
	}

	static void fillDatabase(ActorRef<Message> orientDbActor, def testKit, def generator, def initDatabaseLock) {
		def result =
				AskPattern.ask(
				orientDbActor, {replyTo ->
					new DbCommandReplyMessage(replyTo, { db ->
						def go = new MapTile(generator.generate())
						go.pos = new GameMapPosition(0, 10, 20, 2)
						go.material = "Sandstone"
						def doc = db.newVertex(go.getType());
						db.begin();
						new MapTileStorage().save(doc, go)
						doc.save();
						db.commit();
					})
				},
				timeout,
				testKit.scheduler())
		result.whenComplete( {reply, failure ->
			log_reply_failure "fillDatabase", reply, failure
			initDatabaseLock.countDown()
		})
	}

	static void deleteDatabase(ActorRef<Message> orientDbActor, def testKit, def deleteDatabaseLock) {
		def result =
				AskPattern.ask(
				orientDbActor,
				{replyTo -> new DeleteDbMessage(replyTo)},
				timeout,
				testKit.scheduler())
		result.whenComplete( {reply, failure ->
			log_reply_failure "deleteDatabase", reply, failure
			deleteDatabaseLock.countDown()
		})
	}

	static void log_reply_failure(def name, def reply, def failure) {
		log.info "$name reply ${reply} failure ${failure}"
	}
}
