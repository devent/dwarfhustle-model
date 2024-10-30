package com.anrisoftware.dwarfhustle.model.knowledge.evrete

import static com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.KnowledgeGetMessage.askKnowledgeObjects
import static java.time.Duration.ofSeconds

import java.nio.file.Path

import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.lable.oss.uniqueid.IDGenerator
import org.lable.oss.uniqueid.LocalUniqueIDGeneratorFactory
import org.lable.oss.uniqueid.bytes.Mode

import com.anrisoftware.dwarfhustle.model.actor.ActorSystemProvider
import com.anrisoftware.dwarfhustle.model.actor.DwarfhustleModelActorsModule
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message
import com.anrisoftware.dwarfhustle.model.api.objects.DwarfhustleModelApiObjectsModule
import com.anrisoftware.dwarfhustle.model.api.objects.GameMap
import com.anrisoftware.dwarfhustle.model.api.objects.MapChunk
import com.anrisoftware.dwarfhustle.model.api.vegetations.KnowledgeTree
import com.anrisoftware.dwarfhustle.model.api.vegetations.KnowledgeVegetation
import com.anrisoftware.dwarfhustle.model.api.vegetations.Vegetation
import com.anrisoftware.dwarfhustle.model.db.buffers.MapChunkBuffer
import com.anrisoftware.dwarfhustle.model.db.lmbd.DwarfhustleModelDbLmbdModule
import com.anrisoftware.dwarfhustle.model.db.lmbd.MapChunksLmbdStorage.MapChunksLmbdStorageFactory
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.DwarfhustlePowerloomModule
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.KnowledgeJcsCacheActor
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.PowerLoomKnowledgeActor
import com.google.inject.Guice
import com.google.inject.Injector

import akka.actor.typed.ActorRef
import groovy.util.logging.Slf4j

@Slf4j
class PineRulesTest {

    static Injector injector

    static ActorRef<Message> knowledgeActor

    static ActorSystemProvider actor

    static AskKnowledge askKnowledge

    static MapChunksLmbdStorageFactory chunksLmbdStorageFactory

    static IDGenerator generator

    @BeforeAll
    static void setupActor() {
        injector = Guice.createInjector(
                new DwarfhustleModelActorsModule(),
                new DwarfhustlePowerloomModule(),
                new DwarfhustleModelApiObjectsModule(),
                new DwarfhustleModelDbLmbdModule())
        actor = injector.getInstance(ActorSystemProvider.class)
        KnowledgeJcsCacheActor.create(injector, ofSeconds(1)).whenComplete({ it, ex ->
            log.debug("KnowledgeJcsCacheActor {} {}", it, ex)
        } ).get()
        PowerLoomKnowledgeActor.create(injector, ofSeconds(1), actor.getActorAsync(KnowledgeJcsCacheActor.ID)).whenComplete({ it, ex ->
            log.debug("PowerLoomKnowledgeActor {} {}", it, ex)
            knowledgeActor = it
        } ).get()
        askKnowledge = { timeout, type -> askKnowledgeObjects(actor.actorSystem, timeout, type) }
        chunksLmbdStorageFactory = injector.getInstance(MapChunksLmbdStorageFactory)
        generator = LocalUniqueIDGeneratorFactory.generatorFor(0, 0, Mode.SPREAD);
    }

    @AfterAll
    static void closeActor() {
        actor.shutdownWait()
    }

    @TempDir
    static Path tmpdir

    Path terrainPath

    @BeforeEach
    void setUp() {
        def terrain = "terrain_32_32_32_4_585"
        def path = Path.of("/home/devent/Projects/dwarf-hustle/terrain-maps", terrain);
        def tmp = tmpdir.resolve(terrain)
        FileUtils.copyDirectory(path.toFile(), tmp.toFile())
        terrainPath = tmp
    }

    @Test
    void pine_rules() {
        def chunks = chunksLmbdStorageFactory.create(terrainPath, 4)
        def vkn = new VegetationKnowledge()
        def k = vkn.createKnowledgeService()
        vkn.loadKnowledges(askKnowledge)
        def knowledge = vkn.createRulesKnowledgeFromSource(k, "PineRules.java")
        def gm = new GameMap(1)
        gm.width = 32
        gm.height = 32
        gm.depth = 32
        Vegetation v
        KnowledgeVegetation kv
        askKnowledge.doAskAsync(ofSeconds(1), KnowledgeTree.TYPE).whenComplete({ it, ex ->
            kv = it.find { it.name == "PINE" }
        } ).get()
        v = kv.createObject(generator.generate())
        v.map = gm.id
        v.pos.x = 11
        v.pos.y = 15
        v.pos.z = 9
        println v
        def root = MapChunk.getChunk(chunks, 0)
        def block = MapChunkBuffer.findBlock(root, 11, 15, 9, chunks)
        println block
        println block.isEmpty()
        vkn.run(askKnowledge, knowledge, v, kv, chunks, chunks, gm)
        chunks.close()
    }
}
