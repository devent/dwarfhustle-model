package com.anrisoftware.dwarfhustle.model.db.orientdb.actor

import java.time.Duration

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message
import com.anrisoftware.dwarfhustle.model.api.GameMapPos
import com.anrisoftware.dwarfhustle.model.api.MapTile
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

	boolean fillDatabase = true

	final url = "remote:localhost"

	final database = "test"

	final user = "root"

	final password = "admin"

	final type = ODatabaseType.MEMORY

	OrientDB db

	Duration timeout = Duration.ofSeconds(600)

	ActorRef<Message> orientDbActor

	ActorRef<Message> objectsActor

	def testKit

	def generator

	DbTestUtils(ActorRef<Message> orientDbActor, ActorRef<Message> objectsActor, def testKit, def generator) {
		this.orientDbActor = orientDbActor
		this.objectsActor = objectsActor
		this.testKit = testKit
		this.generator = generator
	}

	void connectCreateDatabaseRemote(def initDatabaseLock) {
		def result =
				AskPattern.ask(
				orientDbActor,
				{replyTo -> new ConnectDbRemoteMessage(replyTo, "remote:localhost", "test", "root", "admin")},
				timeout,
				testKit.scheduler())
		result.whenComplete({reply, failure ->
			log_reply_failure "connectCreateDatabaseRemote", reply, failure
			switch (reply) {
				case ConnectDbSuccessMessage:
					db = reply.db
					createDatabase(initDatabaseLock)
					break
				case DbErrorMessage:
					throw reply.error
					break
			}
		})
	}

	void connectCreateDatabaseEmbedded(def server, def initDatabaseLock) {
		def result =
				AskPattern.ask(
				orientDbActor,
				{replyTo -> new ConnectDbEmbeddedMessage(replyTo, server, "test", "root", "admin")},
				timeout,
				testKit.scheduler())
		result.whenComplete({reply, failure ->
			log_reply_failure "connectCreateDatabaseEmbedded", reply, failure
			switch (reply) {
				case ConnectDbSuccessMessage:
					db = reply.db
					createDatabase(initDatabaseLock)
					break
				case DbErrorMessage:
					throw reply.error
					break
			}
		})
	}

	void createDatabase(def initDatabaseLock) {
		def result =
				AskPattern.ask(
				orientDbActor,
				{replyTo -> new CreateDbMessage(replyTo, type)},
				timeout,
				testKit.scheduler())
		result.whenComplete( {reply, failure ->
			log_reply_failure "createDatabase", reply, failure
			createSchemas(initDatabaseLock)
		})
	}

	void createSchemas(def initDatabaseLock) {
		def result =
				AskPattern.ask(
				objectsActor,
				{replyTo -> new CreateSchemasMessage(replyTo)},
				timeout,
				testKit.scheduler())
		result.whenComplete( {reply, failure ->
			log_reply_failure "createSchemas", reply, failure
			if (fillDatabase) {
				fillDatabase(initDatabaseLock)
			} else {
				initDatabaseLock.countDown()
			}
		})
	}

	void fillDatabase(def initDatabaseLock) {
		def result =
				AskPattern.ask(
				orientDbActor, {replyTo ->
					new DbCommandReplyMessage(replyTo, { db ->
						def go = new MapTile(generator.generate())
						go.pos = new GameMapPos(0, 10, 20, 2)
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

	void deleteDatabase(def deleteDatabaseLock) {
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
