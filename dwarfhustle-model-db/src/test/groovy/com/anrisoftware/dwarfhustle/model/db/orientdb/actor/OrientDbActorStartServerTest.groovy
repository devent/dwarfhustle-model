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

import static com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DbTestUtils.log_reply_failure

import java.time.Duration
import java.util.concurrent.CountDownLatch

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message
import com.anrisoftware.dwarfhustle.model.actor.DwarfhustleModelActorsModule
import com.anrisoftware.dwarfhustle.model.api.objects.DwarfhustleModelApiObjectsModule
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DbMessage.DbErrorMessage
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.StartEmbeddedServerMessage.StartEmbeddedServerSuccessMessage
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
class OrientDbActorStartServerTest {

    static final ActorTestKit testKit = ActorTestKit.create()

    static Injector injector

    static ActorRef<Message> orientDbActor

    static timeout = Duration.ofMinutes(10)

    static DbServerUtils dbServerUtils

    @TempDir
    static File tempDir

    @BeforeAll
    static void setupActor() {
        injector = Guice.createInjector(new DwarfhustleModelActorsModule(), new DwarfhustleModelDbOrientdbModule(), new DwarfhustleModelApiObjectsModule())
        orientDbActor = testKit.spawn(OrientDbActor.create(injector), "OrientDbActor");
    }

    @AfterAll
    static void closeDb() {
        AskPattern.ask(
                orientDbActor,
                {replyTo -> new CloseDbMessage(replyTo)},
                timeout,
                testKit.scheduler())
        stopServer()
        testKit.shutdown(testKit.system(), timeout)
    }

    @Test
    void start_server() {
        def lock = new CountDownLatch(1)
        def result =
                AskPattern.ask(
                orientDbActor,
                {replyTo -> new StartEmbeddedServerMessage(replyTo, tempDir.absolutePath, OrientDbActorStartServerTest.class.getResource("/orientdb-test-config.xml"))},
                timeout,
                testKit.scheduler())
        result.whenComplete( {reply, failure ->
            log_reply_failure "StartEmbeddedServerMessage", reply, failure
            switch (reply) {
                case StartEmbeddedServerSuccessMessage:
                    connectDatabase(lock, reply.server)
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

    void connectDatabase(def lock, def server) {
        def result =
                AskPattern.ask(
                orientDbActor,
                {replyTo -> new ConnectDbEmbeddedMessage(replyTo, server, "test", "root", "admin")},
                timeout,
                testKit.scheduler())
        result.whenComplete( {reply, failure ->
            log_reply_failure "ConnectDbEmbeddedMessage", reply, failure
            createDatabase(lock)
        })
    }

    void createDatabase(def lock) {
        def result =
                AskPattern.ask(
                orientDbActor,
                {replyTo -> new CreateDbMessage(replyTo, ODatabaseType.PLOCAL)},
                timeout,
                testKit.scheduler())
        result.whenComplete( {reply, failure ->
            log.info "Create database reply {} failure {}", reply, failure
            lock.countDown()
        })
    }

    static void stopServer() {
        def lock = new CountDownLatch(1)
        def result =
                AskPattern.ask(
                orientDbActor,
                {replyTo -> new StopEmbeddedServerMessage(replyTo)},
                timeout,
                testKit.scheduler())
        result.whenComplete( {reply, failure ->
            log_reply_failure "StopEmbeddedServerMessage", reply, failure
            lock.countDown()
        })
        lock.await()
    }
}
