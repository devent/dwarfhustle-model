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
import java.time.LocalDateTime
import java.time.Month
import java.time.ZoneOffset
import java.util.concurrent.CountDownLatch
import java.util.function.Consumer

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.lable.oss.uniqueid.IDGenerator
import org.lable.oss.uniqueid.LocalUniqueIDGeneratorFactory
import org.lable.oss.uniqueid.bytes.Mode

import com.anrisoftware.dwarfhustle.model.actor.DwarfhustleModelActorsModule
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message
import com.anrisoftware.dwarfhustle.model.api.objects.DwarfhustleModelApiObjectsModule
import com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos
import com.anrisoftware.dwarfhustle.model.api.objects.GameChunkPos
import com.anrisoftware.dwarfhustle.model.api.objects.GameMap
import com.anrisoftware.dwarfhustle.model.api.objects.MapArea
import com.anrisoftware.dwarfhustle.model.api.objects.MapBlock
import com.anrisoftware.dwarfhustle.model.api.objects.MapChunk
import com.anrisoftware.dwarfhustle.model.api.objects.WorldMap
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
@TestMethodOrder(OrderAnnotation.class)
class OrientDbActorTest {

    static final EMBEDDED_SERVER_PROPERTY = System.getProperty("com.anrisoftware.dwarfhustle.model.db.orientdb.actor.embedded-server", "yes")

    static final ActorTestKit testKit = ActorTestKit.create()

    static Injector injector

    static ActorRef<Message> orientDbActor

    static timeout = Duration.ofMinutes(10)

    static DbServerUtils dbServerUtils

    static IDGenerator gen = LocalUniqueIDGeneratorFactory.generatorFor(1, 1, Mode.SPREAD);

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
    @Order(1)
    void test_connect_db_message_create_db() {
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

    @Test
    @Order(2)
    void test_create_schemas() {
        def result =
                AskPattern.ask(
                orientDbActor,
                {replyTo -> new CreateSchemasMessage(replyTo)},
                timeout,
                testKit.scheduler())
        result.whenComplete( {reply, failure ->
            log.info "CreateSchemasMessage reply {} failure {}", reply, failure
        })
        result.get()
    }

    @Test
    @Order(10)
    void test_store_gameobjects() {
        def gm = new GameMap(gen.generate());
        def wm = new WorldMap(gen.generate());
        wm.addMap(gm);
        wm.currentMap = gm.id;
        wm.time = LocalDateTime.of(2023, Month.APRIL, 15, 12, 0);
        wm.distanceLat = 100f;
        wm.distanceLon = 100f;
        gm.world = wm.id;
        gm.chunkSize = 4
        gm.width = 32
        gm.height = 32
        gm.depth = 32
        gm.area = MapArea.create(50.99819f, 10.98348f, 50.96610f, 11.05610f);
        gm.timeZone = ZoneOffset.ofHours(1);
        gm.setCameraPos(0.0f, 0.0f, 83.0f);
        gm.setCameraRot(0.0f, 1.0f, 0.0f, 0.0f);
        gm.setCursorZ(0);
        def mchunk = new MapChunk(gen.generate())
        mchunk.map = gm.id
        mchunk.root = true
        mchunk.pos = new GameChunkPos(0, 0, 0, 32, 32, 32)
        gm.root = mchunk.id
        def mblock = new MapBlock(gen.generate())
        mblock.pos = new GameBlockPos(0, 0, 0)
        mblock.map = gm.id
        AskPattern.ask(orientDbActor, {replyTo -> new SaveObjectMessage(replyTo, gm)}, timeout, testKit.scheduler()).whenComplete( {reply, failure ->
            log.info "SaveObjectMessage reply {} failure {}", reply, failure
        }).get()
        AskPattern.ask(orientDbActor, {replyTo -> new SaveObjectMessage(replyTo, wm)}, timeout, testKit.scheduler()).whenComplete( {reply, failure ->
            log.info "SaveObjectMessage reply {} failure {}", reply, failure
        }).get()
        AskPattern.ask(orientDbActor, {replyTo -> new SaveObjectMessage(replyTo, mchunk)}, timeout, testKit.scheduler()).whenComplete( {reply, failure ->
            log.info "SaveObjectMessage reply {} failure {}", reply, failure
        }).get()
        AskPattern.ask(orientDbActor, {replyTo -> new SaveObjectMessage(replyTo, mblock)}, timeout, testKit.scheduler()).whenComplete( {reply, failure ->
            log.info "SaveObjectMessage reply {} failure {}", reply, failure
        }).get()
        AskPattern.ask(orientDbActor, {replyTo -> new SaveObjectMessage(replyTo, gm)}, timeout, testKit.scheduler()).whenComplete( {reply, failure ->
            log.info "SaveObjectMessage reply {} failure {}", reply, failure
        }).get()
    }

    @Test
    @Order(20)
    void test_retrieve_gameobjects() {
        WorldMap wm
        askLoadObject WorldMap.OBJECT_TYPE, { it.query("SELECT * from ? where objecttype = ? LIMIT 1", WorldMap.OBJECT_TYPE, WorldMap.OBJECT_TYPE) }, { wm = it }
        assert wm.id != 0
        GameMap gm
        askLoadObject GameMap.OBJECT_TYPE, { it.query("SELECT * from ? where objecttype = ? LIMIT 1", GameMap.OBJECT_TYPE, GameMap.OBJECT_TYPE) }, { gm = it }
        assert gm.id != 0
        assert wm.currentMap == gm.id
        assert gm.world == wm.id
        MapChunk rootChunk
        askLoadObject MapChunk.OBJECT_TYPE, { it.query("SELECT * from ? where objecttype = ? LIMIT 1", MapChunk.OBJECT_TYPE, MapChunk.OBJECT_TYPE) }, { rootChunk = it }
        assert gm.root == rootChunk.id
        rootChunk.blocks.forEach {
        }
    }

    def askLoadObject(def objectType, def query, Consumer consumer) {
        AskPattern.ask(orientDbActor, {replyTo ->
            new LoadObjectMessage(replyTo, objectType, query)
        }, timeout, testKit.scheduler()).whenComplete( {reply, failure ->
            log.info "LoadObjectMessage reply {} failure {}", reply, failure
            consumer.accept(reply.go)
        }).get()
    }
}
