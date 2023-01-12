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
import com.anrisoftware.dwarfhustle.model.api.GameBlockPos
import com.anrisoftware.dwarfhustle.model.api.MapBlock
import com.anrisoftware.dwarfhustle.model.db.cache.JcsCacheModule
import com.anrisoftware.dwarfhustle.model.db.cache.MapBlocksJcsCacheActor
import com.anrisoftware.dwarfhustle.model.db.cache.MapBlocksJcsCacheActor.MapBlocksJcsCacheActorFactory
import com.anrisoftware.dwarfhustle.model.db.cache.RetrieveCacheMessage
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.OrientDbModule
import com.anrisoftware.dwarfhustle.model.db.orientdb.objects.ObjectsModule
import com.anrisoftware.dwarfhustle.model.generate.WorkerBlocks.WorkerBlocksFactory
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
 * @see WorkerBlocks
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
@TestMethodOrder(OrderAnnotation.class)
class WorkerBlocksTest {

	static final ActorTestKit testKit = ActorTestKit.create()

	static Injector injector

	static ActorRef<Message> powerLoomKnowledgeActor

	static ActorRef<Message> knowledgeBaseActor

	static WorkerBlocksFactory workerFactory

	static timeout = Duration.ofSeconds(300)

	static mapTilesParams

	static File cacheFile

	@BeforeAll
	static void setupActor() {
		def s = 256
		def parentDir = File.createTempDir()
		mapTilesParams = [parent_dir: parentDir, game_name: "test", mapid: 0, width: s, height: s, depth: s, block_size: 4]
		cacheFile = new File(parentDir, "dwarfhustle_jcs_swap_${mapTilesParams.game_name}_mapBlocksCache_0_file")
		injector = Guice.createInjector(
				new MainActorsModule(),
				new ObjectsModule(),
				new PowerloomModule(),
				new GenerateModule(),
				new OrientDbModule(),
				new ApiModule(),
				new JcsCacheModule(),
				new PropertiesThreadsModule())
		workerFactory = injector.getInstance(WorkerBlocksFactory)
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
		def cacheActor = testKit.spawn(MapBlocksJcsCacheActor.create(injector, injector.getInstance(MapBlocksJcsCacheActorFactory), MapBlocksJcsCacheActor.createInitCacheAsync(mapTilesParams), mapTilesParams), "MapBlocksJcsCacheActor");
		def cache = retrieveCache(cacheActor)
		def m = new GenerateMapMessage(null, 0, mapTilesParams.width, mapTilesParams.height, mapTilesParams.depth, mapTilesParams.block_size)
		def worker = workerFactory.create(cache)
		def thread = Thread.start {
			worker.generate(m)
		}
		while (!worker.generateDone) {
			Thread.sleep(1000)
			log.info("Blocks done {}", worker.blocksDone)
		}
		log.info("generate done {}", worker.blocksDone)
		testKit.stop(cacheActor)
		shutdownJcs()
	}

	@Test
	@Timeout(600)
	@Order(10)
	void "load blocks from cache"() {
		def cacheActor = testKit.spawn(MapBlocksJcsCacheActor.create(injector, injector.getInstance(MapBlocksJcsCacheActorFactory), MapBlocksJcsCacheActor.createInitCacheAsync(mapTilesParams), mapTilesParams), "MapBlocksJcsCacheActor");
		def cache = retrieveCache(cacheActor)
		def s = mapTilesParams.block_size
		def mapid = mapTilesParams.mapid
		log.info("retrieve cache done")
		for (int z = 0; z < mapTilesParams.depth; z += s) {
			for (int y = 0; y < mapTilesParams.height; y += s) {
				for (int x = 0; x < mapTilesParams.width; x += s) {
					def pos = new GameBlockPos(mapid, x, y, z, x + s, y + s, z + s)
					MapBlock block = cache.get(pos)
					if (!block) {
						log.info("No block for {}", pos)
					}
					assert block.pos.mapid == mapid
					assert block.pos.x == x
					assert block.pos.y == y
					assert block.pos.z == z
					assert block.pos.endPos.mapid == mapid
					assert block.pos.endPos.x == x + s
					assert block.pos.endPos.y == y + s
					assert block.pos.endPos.z == z + s
					assert block.blocks.empty
					assert block.tiles.size() == s * s * s
				}
			}
			log.trace("done check map tile z={}", z);
		}
		testKit.stop(cacheActor)
		shutdownJcs()
	}
}