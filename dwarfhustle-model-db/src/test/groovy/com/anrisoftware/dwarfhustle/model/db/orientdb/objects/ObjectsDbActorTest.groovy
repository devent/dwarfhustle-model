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
package com.anrisoftware.dwarfhustle.model.db.orientdb.objects

import static com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DbTestUtils.log_reply_failure

import java.time.Duration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

import org.eclipse.collections.impl.factory.Maps
import org.eclipse.collections.impl.factory.primitive.ObjectLongMaps
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.io.TempDir
import org.lable.oss.uniqueid.IDGenerator

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message
import com.anrisoftware.dwarfhustle.model.actor.ModelActorsModule
import com.anrisoftware.dwarfhustle.model.api.objects.ApiModule
import com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos
import com.anrisoftware.dwarfhustle.model.api.objects.GameChunkPos
import com.anrisoftware.dwarfhustle.model.api.objects.IdsObjectsProvider
import com.anrisoftware.dwarfhustle.model.api.objects.MapBlock
import com.anrisoftware.dwarfhustle.model.api.objects.MapChunk
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DbCommandMessage
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DbServerUtils
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DbTestUtils
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.OrientDbActor
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.OrientDbModule
import com.anrisoftware.dwarfhustle.model.db.orientdb.storages.MapChunkStorage
import com.google.inject.Guice
import com.google.inject.Injector

import akka.actor.testkit.typed.javadsl.ActorTestKit
import akka.actor.typed.ActorRef
import akka.actor.typed.javadsl.AskPattern
import groovy.util.logging.Slf4j

/**
 * @see ObjectsDbActor
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
@TestMethodOrder(OrderAnnotation.class)
class ObjectsDbActorTest {

    static final EMBEDDED_SERVER_PROPERTY = System.getProperty("com.anrisoftware.dwarfhustle.model.db.orientdb.objects.embedded-server", "yes")

    static final ActorTestKit testKit = ActorTestKit.create()

    static Injector injector

    static ActorRef<Message> orientDbActor

    static ActorRef<Message> objectsDbActor

    static timeout = Duration.ofMinutes(10)

    static DbServerUtils dbServerUtils

    static DbTestUtils dbTestUtils

    static IDGenerator gen

    static MapChunkStorage mapBlockStorage

    @BeforeAll
    static void setupActor(@TempDir File tmp) {
        if (EMBEDDED_SERVER_PROPERTY == "yes") {
            dbServerUtils = new DbServerUtils()
            dbServerUtils.createServer(tmp)
        }
        injector = Guice.createInjector(new ModelActorsModule(), new OrientDbModule(), new ObjectsDbModule(), new ApiModule())
        gen = injector.getInstance(IdsObjectsProvider).get()
        orientDbActor = testKit.spawn(OrientDbActor.create(injector), "OrientDbActor");
        objectsDbActor = testKit.spawn(ObjectsDbActor.create(injector, orientDbActor), "ObjectsDbActor");
        dbTestUtils = new DbTestUtils(orientDbActor, objectsDbActor, testKit.scheduler(), gen)
        dbTestUtils.fillDatabase = false
        def initDatabaseLock = new CountDownLatch(1)
        if (EMBEDDED_SERVER_PROPERTY == "yes") {
            dbTestUtils.connectCreateDatabaseEmbedded(dbServerUtils.server, initDatabaseLock)
        } else {
            dbTestUtils.connectCreateDatabaseRemote(initDatabaseLock)
        }
        initDatabaseLock.await()
        mapBlockStorage = injector.getInstance(MapChunkStorage)
    }

    @AfterAll
    static void closeDb() {
        def deleteDatabaseLock = new CountDownLatch(1)
        dbTestUtils.deleteDatabase(deleteDatabaseLock)
        deleteDatabaseLock.await()
        testKit.shutdown(testKit.system(), timeout)
        if (EMBEDDED_SERVER_PROPERTY == "yes") {
            dbServerUtils.shutdownServer()
        }
    }

    static mapChunkId

    static mapBlockId

    @Test
    @Order(1)
    void create_objects() {
        def lock = new CountDownLatch(1)
        def result =
                AskPattern.ask(
                orientDbActor, {replyTo ->
                    new DbCommandMessage(replyTo, { ex -> }, { db ->
                        def parentMapChunk = new MapChunk(gen.generate(), new GameChunkPos(0, 0, 0, 0, 8, 8, 8))
                        def mapChunk = new MapChunk(gen.generate(), new GameChunkPos(0, 0, 0, 0, 4, 4, 4))
                        mapChunkId = mapChunk.id
                        def mapBlock = new MapBlock(gen.generate())
                        mapBlockId = mapBlock.id
                        mapBlock.pos = new GameBlockPos(0, 0, 0, 0)
                        mapBlock.material = 200
                        def blocks = Maps.mutable.empty()
                        blocks.put(mapBlock.pos, mapBlock)
                        mapChunk.blocks = blocks
                        def chunks = ObjectLongMaps.mutable.empty()
                        chunks.put(mapChunk.pos, mapChunk.id)
                        parentMapChunk.chunks = chunks
                        def v = db.newVertex(MapChunk.OBJECT_TYPE)
                        mapBlockStorage.store(db, v, parentMapChunk)
                        db.begin()
                        v.save()
                        db.commit()
                        v = db.newVertex(MapChunk.OBJECT_TYPE)
                        mapBlockStorage.store(db, v, mapChunk)
                        db.begin()
                        v.save()
                        db.commit()
                    })
                },
                timeout,
                testKit.scheduler())
        result.whenComplete( {reply, failure ->
            log_reply_failure "create_objects", reply, failure
            lock.countDown()
        })
        lock.await()
    }

    @Test
    @Order(10)
    void load_objects() {
        def lock = new CountDownLatch(1)
        def result =
                AskPattern.ask(
                orientDbActor, {replyTo ->
                    new DbCommandMessage(replyTo, { ex -> }, { db ->
                        def rs = db.query("SELECT FROM ? WHERE objecttype=? and mapid=? and sx=? and sy=? and sz=? and ex=? and ey=? and ez=?",
                                MapChunk.OBJECT_TYPE, MapChunk.OBJECT_TYPE,
                                0, 0, 0, 0, 8, 8, 8);
                        while (rs.hasNext()) {
                            def row = rs.next();
                            assert row != null
                            MapChunk chunk = mapBlockStorage.retrieve(db, row.vertex.get(), mapBlockStorage.create())
                            assert chunk.dirty == false
                            assert chunk.pos.mapid == 0
                            assert chunk.pos.x == 0
                            assert chunk.pos.y == 0
                            assert chunk.pos.z == 0
                            assert chunk.ep.x == 8
                            assert chunk.ep.y == 8
                            assert chunk.ep.z == 8
                            assert chunk.chunks.size() == 1
                            assert chunk.chunks.get(new GameChunkPos(0, 0, 0, 0, 4, 4, 4)) == mapChunkId
                            assert chunk.blocks.size() == 0
                        }
                        rs.close();
                        rs = db.query("SELECT FROM ? WHERE objecttype=? and mapid=? and sx=? and sy=? and sz=? and ex=? and ey=? and ez=?",
                                MapChunk.OBJECT_TYPE, MapChunk.OBJECT_TYPE,
                                0, 0, 0, 0, 4, 4, 4);
                        while (rs.hasNext()) {
                            def row = rs.next();
                            assert row != null
                            MapChunk chunk = mapBlockStorage.retrieve(db, row.vertex.get(), mapBlockStorage.create())
                            assert chunk.dirty == false
                            assert chunk.pos.mapid == 0
                            assert chunk.pos.x == 0
                            assert chunk.pos.y == 0
                            assert chunk.pos.z == 0
                            assert chunk.ep.x == 4
                            assert chunk.ep.y == 4
                            assert chunk.ep.z == 4
                            assert chunk.chunks.size() == 0
                            assert chunk.blocks.size() == 1
                            MapBlock block = chunk.blocks[new GameBlockPos(0, 0, 0, 0)]
                            assert block.id == mapBlockId
                        }
                        rs.close();
                    })
                },
                timeout,
                testKit.scheduler())
        result.whenComplete( {reply, failure ->
            log_reply_failure "create_objects", reply, failure
            lock.countDown()
        })
        lock.await(10, TimeUnit.SECONDS)
    }
}
