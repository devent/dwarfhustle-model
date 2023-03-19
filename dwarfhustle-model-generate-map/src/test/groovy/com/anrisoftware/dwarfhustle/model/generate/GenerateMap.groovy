/*
 * dwarfhustle-model-generate-map - Manages the compile dependencies for the model.
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
package com.anrisoftware.dwarfhustle.model.generate

import static com.anrisoftware.dwarfhustle.model.api.objects.MapCoordinate.toDecimalDegrees

import java.time.Duration
import java.time.LocalDateTime
import java.time.Month
import java.time.ZoneOffset
import java.util.concurrent.CountDownLatch

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.lable.oss.uniqueid.IDGenerator

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message
import com.anrisoftware.dwarfhustle.model.actor.ModelActorsModule
import com.anrisoftware.dwarfhustle.model.api.objects.ApiModule
import com.anrisoftware.dwarfhustle.model.api.objects.GameMap
import com.anrisoftware.dwarfhustle.model.api.objects.MapArea
import com.anrisoftware.dwarfhustle.model.api.objects.WorldMap
import com.anrisoftware.dwarfhustle.model.db.cache.AppCachesConfig
import com.anrisoftware.dwarfhustle.model.db.cache.JcsCacheModule
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DbServerUtils
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DbTestUtils
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.OrientDbActor
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.OrientDbModule
import com.anrisoftware.dwarfhustle.model.db.orientdb.objects.ObjectsDbActor
import com.anrisoftware.dwarfhustle.model.db.orientdb.objects.ObjectsDbModule
import com.anrisoftware.dwarfhustle.model.generate.WorkerBlocks.WorkerBlocksFactory
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.KnowledgeBaseActor
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.PowerLoomKnowledgeActor
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.PowerloomModule
import com.anrisoftware.globalpom.threads.properties.internal.PropertiesThreadsModule
import com.google.inject.Guice
import com.google.inject.Injector
import com.orientechnologies.orient.core.db.ODatabaseType

import akka.actor.testkit.typed.javadsl.ActorTestKit
import akka.actor.typed.ActorRef
import groovy.util.logging.Slf4j

/**
 * Generates a map persistent.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
@TestMethodOrder(OrderAnnotation.class)
class GenerateMap {

    static final EMBEDDED_SERVER_PROPERTY = System.getProperty("com.anrisoftware.dwarfhustle.model.db.orientdb.objects.embedded-server", "yes")

    static final ActorTestKit testKit = ActorTestKit.create()

    static Injector injector

    static DbServerUtils dbServerUtils

    static DbTestUtils dbTestUtils

    static ActorRef<Message> powerLoomKnowledgeActor

    static ActorRef<Message> knowledgeBaseActor

    static WorkerBlocksFactory workerFactory

    static IDGenerator gen

    static ActorRef<Message> orientDbActor

    static ActorRef<Message> objectsDbActor

    static timeout = Duration.ofSeconds(300)

    static mapTilesParams

    static File cacheFile

    @BeforeAll
    static void setupActor() {
        def s = 8
        def blockSize = 4
        def parentDir = File.createTempDir("size_${s}_${blockSize}_")
        if (EMBEDDED_SERVER_PROPERTY == "yes") {
            dbServerUtils = new DbServerUtils()
            dbServerUtils.createServer(parentDir.absolutePath)
        }
        mapTilesParams = [parent_dir: parentDir, game_name: "Endless World", mapid: 1, width: s, height: s, depth: s, block_size: blockSize]
        cacheFile = new File(parentDir, "dwarfhustle_jcs_swap_${mapTilesParams.game_name}_mapBlocksCache_0_file")
        injector = Guice.createInjector(
                new ModelActorsModule(),
                new ObjectsDbModule(),
                new PowerloomModule(),
                new GenerateModule(),
                new OrientDbModule(),
                new ApiModule(),
                new JcsCacheModule(),
                new PropertiesThreadsModule())
        workerFactory = injector.getInstance(WorkerBlocksFactory)
        powerLoomKnowledgeActor = testKit.spawn(PowerLoomKnowledgeActor.create(injector), "PowerLoomKnowledgeActor");
        knowledgeBaseActor = testKit.spawn(KnowledgeBaseActor.create(injector, powerLoomKnowledgeActor), "KnowledgeBaseActor");
        orientDbActor = testKit.spawn(OrientDbActor.create(injector), "OrientDbActor");
        objectsDbActor = testKit.spawn(ObjectsDbActor.create(injector, orientDbActor), "ObjectsDbActor");
        gen = injector.getInstance(IDGenerator)
        dbTestUtils = new DbTestUtils(orientDbActor, objectsDbActor, testKit, gen)
        dbTestUtils.type = ODatabaseType.PLOCAL
        dbTestUtils.fillDatabase = false
        def initDatabaseLock = new CountDownLatch(1)
        if (EMBEDDED_SERVER_PROPERTY == "yes") {
            dbTestUtils.connectCreateDatabaseEmbedded(dbServerUtils.server, initDatabaseLock)
        } else {
            dbTestUtils.connectCreateDatabaseRemote(initDatabaseLock)
        }
        initDatabaseLock.await()
    }

    @AfterAll
    static void shutdownTest() {
        def closeDatabaseLock = new CountDownLatch(1)
        dbTestUtils.closeDatabase(closeDatabaseLock)
        closeDatabaseLock.await()
        testKit.shutdown(testKit.system(), timeout)
        if (EMBEDDED_SERVER_PROPERTY == "yes") {
            dbServerUtils.shutdownServer()
        }
    }

    @Test
    @Order(1)
    void "test generate"() {
        def wm = new WorldMap(gen.generate())
        wm.name = mapTilesParams.game_name
        wm.distanceLat = 1
        wm.distanceLon = 1
        wm.time = LocalDateTime.of(500, Month.APRIL, 1, 8, 0)
        def gm = new GameMap(gen.generate())
        gm.mapid = mapTilesParams.mapid
        gm.name = "Stone Fortress"
        gm.width = mapTilesParams.width
        gm.height = mapTilesParams.height
        gm.depth = mapTilesParams.depth
        gm.blockSize = mapTilesParams.block_size
        gm.timeZone = ZoneOffset.ofHours(1)
        gm.area = MapArea.create(toDecimalDegrees(54, 47, 24), toDecimalDegrees(17, 30, 12), toDecimalDegrees(54, 42, 02), toDecimalDegrees(17, 35, 22))
        gm.setCameraPos(0.0f, 0.0f, 10.0f)
        gm.setCameraRot(0.0f, 1.0f, 0.0f, 0.0f)
        wm.currentMapid = gm.mapid
        wm.addMap(gm)
        new AppCachesConfig().create(mapTilesParams.parent_dir, gm)
        mapTilesParams.gameMap = gm
        def m = new GenerateMapMessage(null, gm, mapTilesParams.block_size, dbTestUtils.user, dbTestUtils.password, dbTestUtils.database)
        def worker = workerFactory.create(dbTestUtils.db)
        def thread = Thread.start {
            worker.generate(m)
        }
        while (!worker.generateDone) {
            Thread.sleep(1000)
            log.info("Blocks done {}", worker.blocksDone)
        }
        log.info("generate done {}", worker.blocksDone)
    }
}
