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
import static com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DbTestUtils.log_reply_failure
import static java.time.Duration.ofSeconds

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

import com.anrisoftware.dwarfhustle.model.actor.ActorSystemProvider
import com.anrisoftware.dwarfhustle.model.actor.CreateActorMessage
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message
import com.anrisoftware.dwarfhustle.model.actor.ModelActorsModule
import com.anrisoftware.dwarfhustle.model.actor.ShutdownMessage
import com.anrisoftware.dwarfhustle.model.api.objects.ApiModule
import com.anrisoftware.dwarfhustle.model.api.objects.GameMap
import com.anrisoftware.dwarfhustle.model.api.objects.MapArea
import com.anrisoftware.dwarfhustle.model.api.objects.WorldMap
import com.anrisoftware.dwarfhustle.model.db.cache.JcsCacheModule
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DbServerUtils
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DbTestUtils
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.OrientDbActor
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.OrientDbModule
import com.anrisoftware.dwarfhustle.model.db.orientdb.objects.ObjectsDbActor
import com.anrisoftware.dwarfhustle.model.db.orientdb.objects.ObjectsDbModule
import com.anrisoftware.dwarfhustle.model.generate.GenerateMapMessage.GenerateProgressMessage
import com.anrisoftware.dwarfhustle.model.generate.WorkerBlocks.WorkerBlocksFactory
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.KnowledgeBaseActor
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.KnowledgeJcsCacheActor
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.PowerLoomKnowledgeActor
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.PowerloomModule
import com.anrisoftware.globalpom.threads.properties.internal.PropertiesThreadsModule
import com.google.inject.Guice
import com.google.inject.Injector
import com.orientechnologies.orient.core.db.ODatabaseType

import akka.actor.typed.ActorRef
import akka.actor.typed.javadsl.AskPattern
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.receptionist.ServiceKey
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

    static Injector injector

    static ActorSystemProvider actor

    static DbServerUtils dbServerUtils

    static DbTestUtils dbTestUtils

    static ActorRef<Message> powerLoomKnowledgeActor

    static ActorRef<Message> knowledgeBaseActor

    static WorkerBlocksFactory workerFactory

    static IDGenerator gen

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
        def p = [:]
        p.ground_level_percent = 0.4f
        p.soil_level_percent = 0.1f
        p.sedimentary_level_percent = 0.1f
        p.igneous_level_percent = 0.1f
        p.magma_level_percent = 0.1f
        mapTilesParams = [parent_dir: parentDir, game_name: "Endless World", mapid: 1, width: s, height: s, depth: s, block_size: blockSize, p: p]
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
        actor = injector.getInstance(ActorSystemProvider)
        workerFactory = injector.getInstance(WorkerBlocksFactory)
        KnowledgeJcsCacheActor.create(injector, ofSeconds(1)).whenComplete({ret, ex ->
            log_reply_failure "KnowledgeJcsCacheActor.create", ret, ex
        })
        PowerLoomKnowledgeActor.create(injector, ofSeconds(1)).whenComplete({ret, ex ->
            log_reply_failure "PowerLoomKnowledgeActor.create", ret, ex
        })
        actor.waitMainActor()
        actor.getMainActor().waitActor(PowerLoomKnowledgeActor.ID)
        actor.getMainActor().waitActor(KnowledgeJcsCacheActor.ID)
        KnowledgeBaseActor.create(injector, ofSeconds(1), actor.getMainActor().getActor(PowerLoomKnowledgeActor.ID), actor.getMainActor().getActor(KnowledgeJcsCacheActor.ID)).whenComplete({ret, ex ->
            log_reply_failure "KnowledgeBaseActor.create", ret, ex
        })
        OrientDbActor.create(injector, ofSeconds(1)).whenComplete({ret, ex ->
            log_reply_failure "OrientDbActor.create", ret, ex
        })
        actor.getMainActor().waitActor(OrientDbActor.ID)
        def dbActor = actor.getMainActor().getActor(OrientDbActor.ID)
        ObjectsDbActor.create(injector, ofSeconds(1), dbActor).whenComplete({ret, ex ->
            log_reply_failure "OrientDbActor.create", ret, ex
        })
        actor.getMainActor().waitActor(ObjectsDbActor.ID)
        def objectsActor = actor.getMainActor().getActor(ObjectsDbActor.ID)
        gen = injector.getInstance(IDGenerator)
        dbTestUtils = new DbTestUtils(dbActor, objectsActor, actor.scheduler, gen)
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
        actor.getMainActor().tell(new ShutdownMessage())
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
        mapTilesParams.gameMap = gm
        GenerateMapActor.create(injector, ofSeconds(1), dbTestUtils.db, actor.getMainActor().getActor(KnowledgeBaseActor.ID)).whenComplete({ret, ex ->
            log_reply_failure "GenerateMapActor.create", ret, ex
        })
        actor.getMainActor().waitActor(GenerateMapActor.ID)
        CreateActorMessage.createNamedActor(actor.actorSystem, ofSeconds(1), "progressActor".hashCode(), ServiceKey.create(Message.class, "progressActor"), "progressActor", Behaviors.setup({ context ->
            Behaviors.receive(GenerateProgressMessage.class).onMessage(GenerateProgressMessage.class, { m ->
                log.info "Progress: $m"
                return Behaviors.same();
            }).build()
        })).whenComplete({ ret, ex ->
            log_reply_failure "progressActor.create", ret, ex
        })
        actor.getMainActor().waitActor("progressActor".hashCode())
        def progressActor = actor.getMainActor().getActor("progressActor".hashCode())
        def generateLock = new CountDownLatch(1)
        def result =
                AskPattern.ask(
                actor.getMainActor().getActor(GenerateMapActor.ID),
                { replyTo -> new GenerateMapMessage(replyTo, progressActor, gm, mapTilesParams.p, mapTilesParams.block_size, dbTestUtils.user, dbTestUtils.password, dbTestUtils.database) },
                Duration.ofMinutes(30),
                actor.scheduler)
        result.whenComplete({ ret, ex ->
            log_reply_failure "GenerateMapMessage", ret, ex
            generateLock.countDown()
        })
        generateLock.await()
        log.info("generate done {}")
    }
}
