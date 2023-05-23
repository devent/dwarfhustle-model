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
package com.anrisoftware.dwarfhustle.model.db.orientdb.actor

import java.time.Duration
import java.util.concurrent.CountDownLatch

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message
import com.anrisoftware.dwarfhustle.model.actor.DwarfhustleModelActorsModule
import com.anrisoftware.dwarfhustle.model.api.objects.DwarfhustleModelApiObjectsModule
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DbMessage.DbErrorMessage
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

    static final EMBEDDED_SERVER_PROPERTY = System.getProperty("com.anrisoftware.dwarfhustle.model.db.orientdb.objects.embedded-server", "yes")

    static final ActorTestKit testKit = ActorTestKit.create()

    static Injector injector

    static ActorRef<Message> orientDbActor

    static timeout = Duration.ofMinutes(10)

    static DbServerUtils dbServerUtils

    @BeforeAll
    static void setupActor() {
        if (EMBEDDED_SERVER_PROPERTY == "yes") {
            dbServerUtils = new DbServerUtils()
            dbServerUtils.createServer()
        }
        injector = Guice.createInjector(new DwarfhustleModelActorsModule(), new DwarfhustleModelDbOrientdbModule(), new DwarfhustleModelApiObjectsModule())
        orientDbActor = testKit.spawn(OrientDbActor.create(injector), "OrientDbActor");
    }

    @AfterAll
    static void closeDb() {
        deleteDatabase()
        orientDbActor.tell(new CloseDbMessage())
        testKit.shutdown(testKit.system(), timeout)
        if (EMBEDDED_SERVER_PROPERTY == "yes") {
            dbServerUtils.shutdownServer()
        }
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
        def result
        if (EMBEDDED_SERVER_PROPERTY == "yes") {
            result =
                    AskPattern.ask(
                    orientDbActor,
                    {replyTo -> new ConnectDbEmbeddedMessage(replyTo, dbServerUtils.server, "test", "root", "admin")},
                    timeout,
                    testKit.scheduler())
        } else {
            result =
                    AskPattern.ask(
                    orientDbActor,
                    {replyTo -> new ConnectDbRemoteMessage(replyTo, "remote:localhost", "test", "root", "admin")},
                    timeout,
                    testKit.scheduler())
        }
        result.whenComplete( {reply, failure ->
            log.info "Connect database reply {} failure {}", reply, failure
            switch (reply) {
                case ConnectDbSuccessMessage:
                    createDatabase(lock)
                    break
                case DbErrorMessage:
                    lock.countDown()
                    break
                default:
                    lock.countDown()
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
                    new DbCommandMessage(replyTo, { ex -> }, { db ->
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
