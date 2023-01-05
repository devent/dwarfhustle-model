package com.anrisoftware.dwarfhustle.model.generate

import static com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DbTestUtils.*

import java.time.Duration
import java.util.concurrent.CountDownLatch
import java.util.logging.Filter

import org.apache.commons.jcs3.JCS
import org.apache.commons.jcs3.access.CacheAccess
import org.apache.commons.jcs3.engine.control.CompositeCache
import org.apache.commons.jcs3.log.JulLogAdapter
import org.apache.commons.jcs3.log.LogManager
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.Timeout

import com.anrisoftware.dwarfhustle.model.actor.MainActorsModule
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message
import com.anrisoftware.dwarfhustle.model.api.ApiModule
import com.anrisoftware.dwarfhustle.model.api.GameMapPosition
import com.anrisoftware.dwarfhustle.model.api.MapTile
import com.anrisoftware.dwarfhustle.model.db.cache.JcsCacheModule
import com.anrisoftware.dwarfhustle.model.db.cache.MapTilesJcsCacheActor
import com.anrisoftware.dwarfhustle.model.db.cache.MapTilesJcsCacheActor.MapTilesJcsCacheActorFactory
import com.anrisoftware.dwarfhustle.model.db.cache.RetrieveCacheMessage
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.OrientDbModule
import com.anrisoftware.dwarfhustle.model.db.orientdb.objects.ObjectsModule
import com.anrisoftware.dwarfhustle.model.generate.Worker.WorkerFactory
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.KnowledgeBaseActor
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.PowerLoomKnowledgeActor
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.PowerloomModule
import com.anrisoftware.globalpom.threads.properties.internal.PropertiesThreadsModule
import com.google.inject.Guice
import com.google.inject.Injector

import akka.actor.testkit.typed.javadsl.ActorTestKit
import akka.actor.typed.ActorRef
import akka.actor.typed.javadsl.AskPattern
import groovy.util.logging.Slf4j

/**
 * @see KnowledgeBaseActor
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
@TestMethodOrder(OrderAnnotation.class)
class WorkerTest {

	static final ActorTestKit testKit = ActorTestKit.create()

	static Injector injector

	static ActorRef<Message> powerLoomKnowledgeActor

	static ActorRef<Message> knowledgeBaseActor

	static WorkerFactory workerFactory

	static timeout = Duration.ofSeconds(300)

	static mapTilesParams

	static File cacheFile

	@BeforeAll
	static void setupActor() {
		def s = 64
		def parentDir = File.createTempDir()
		mapTilesParams = [parent_dir: parentDir, game_name: "test", mapid: 0, width: s, height: s, depth: s]
		cacheFile = new File(parentDir, "dwarfhustle_jcs_swap_${mapTilesParams.game_name}_mapTilesCache_0_file")
		injector = Guice.createInjector(
				new MainActorsModule(),
				new ObjectsModule(),
				new PowerloomModule(),
				new GenerateModule(),
				new OrientDbModule(),
				new ApiModule(),
				new JcsCacheModule(),
				new PropertiesThreadsModule())
		workerFactory = injector.getInstance(WorkerFactory)
		powerLoomKnowledgeActor = testKit.spawn(PowerLoomKnowledgeActor.create(injector), "PowerLoomKnowledgeActor");
		knowledgeBaseActor = testKit.spawn(KnowledgeBaseActor.create(injector, powerLoomKnowledgeActor), "KnowledgeBaseActor");
	}

	static CacheAccess retrieveCache(ActorRef<Message> mapTilesCacheActor) {
		def cache
		def lock = new CountDownLatch(1)
		def result =
				AskPattern.ask(
				mapTilesCacheActor, { replyTo ->
					new RetrieveCacheMessage(replyTo)
				},
				timeout,
				testKit.scheduler())
		result.whenComplete( { reply, failure ->
			log_reply_failure "retrieveCache", reply, failure
			cache = reply.cache
			lock.countDown()
		})
		lock.await()
		return cache
	}

	@AfterAll
	static void shutdownTest() {
		testKit.shutdown(testKit.system(), timeout)
		cacheFile.deleteDir()
	}

	static void shutdownJcs() {
		log.info("shutdownJcs")
		((JulLogAdapter)LogManager.getLog(CompositeCache.class)).logger.setFilter({
			isLoggable: {
				!it.message.startsWith("No element event queue available for cache")
			}
		} as Filter)
		JCS.shutdown()
		log.info("shutdownJcs done.")
	}

	@Test
	@Timeout(600)
	@Order(1)
	void "test generate"() {
		def mapTilesCacheActor = testKit.spawn(MapTilesJcsCacheActor.create(injector, injector.getInstance(MapTilesJcsCacheActorFactory), MapTilesJcsCacheActor.createInitCacheAsync(mapTilesParams), mapTilesParams), "MapTilesJcsCacheActor");
		def cache = retrieveCache(mapTilesCacheActor)
		def m = new GenerateMapMessage(null, 0, mapTilesParams.width, mapTilesParams.height, mapTilesParams.depth)
		workerFactory.create(cache).generateMap(m)
		log.info("generate done")
		testKit.stop(mapTilesCacheActor)
		shutdownJcs()
	}

	@Test
	@Timeout(600)
	@Order(10)
	void "load tiles from cache"() {
		def mapTilesCacheActor = testKit.spawn(MapTilesJcsCacheActor.create(injector, injector.getInstance(MapTilesJcsCacheActorFactory), MapTilesJcsCacheActor.createInitCacheAsync(mapTilesParams), mapTilesParams), "MapTilesJcsCacheActor");
		def cache = retrieveCache(mapTilesCacheActor)
		log.info("retrieve cache done")
		for (int z = 0; z < mapTilesParams.depth; z++) {
			for (int y = 0; y < mapTilesParams.height; y++) {
				for (int x = 0; x < mapTilesParams.width; x++) {
					MapTile tile = cache.get(new GameMapPosition(mapTilesParams.mapid, x, y, z))
					assert tile.pos.mapid == mapTilesParams.mapid
					assert tile.pos.x == x
					assert tile.pos.y == y
					assert tile.pos.z == z
				}
			}
			log.trace("done check map tile z={}", z);
		}
		testKit.stop(mapTilesCacheActor)
		shutdownJcs()
	}
}
