/*
 * dwarfhustle-model-knowledge - Manages the compile dependencies for the model.
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
import com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos
import com.anrisoftware.dwarfhustle.model.api.objects.GameMap
import com.anrisoftware.dwarfhustle.model.api.objects.MapChunk
import com.anrisoftware.dwarfhustle.model.api.vegetations.KnowledgeTree
import com.anrisoftware.dwarfhustle.model.api.vegetations.KnowledgeVegetation
import com.anrisoftware.dwarfhustle.model.api.vegetations.Vegetation
import com.anrisoftware.dwarfhustle.model.db.buffers.MapChunkBuffer
import com.anrisoftware.dwarfhustle.model.db.lmbd.DwarfhustleModelDbLmbdModule
import com.anrisoftware.dwarfhustle.model.db.lmbd.MapChunksLmbdStorage.MapChunksLmbdStorageFactory
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.DwarfhustleModelKnowledgePowerloomPlModule
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.KnowledgeJcsCacheActor
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.PowerLoomKnowledgeActor
import com.anrisoftware.dwarfhustle.model.trees.VegetationLoadKnowledges
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
                new DwarfhustleModelKnowledgePowerloomPlModule(),
                new DwarfhustleModelApiObjectsModule(),
                new DwarfhustleModelDbLmbdModule())
        actor = injector.getInstance(ActorSystemProvider.class)
        KnowledgeJcsCacheActor.create(injector, ofSeconds(1)).whenComplete({ it, ex ->
            log.debug("KnowledgeJcsCacheActor {} {}", it, ex)
        } ).get()
        PowerLoomKnowledgeActor.create(injector, ofSeconds(1),
                actor.getActorAsync(KnowledgeJcsCacheActor.ID),
                actor.getObjectGetterAsync(KnowledgeJcsCacheActor.ID)).whenComplete({ it, ex ->
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

    GameMap gm

    @BeforeEach
    void setUp() {
        def terrain = "terrain_32_32_32_4_585"
        def path = Path.of("/home/devent/Projects/dwarf-hustle/terrain-maps", terrain);
        def tmp = tmpdir.resolve(terrain)
        FileUtils.copyDirectory(path.toFile(), tmp.toFile())
        terrainPath = tmp
        gm = new GameMap(1)
        gm.width = 32
        gm.height = 32
        gm.depth = 32
    }

    @Test
    void pine_rules() {
        def chunks = chunksLmbdStorageFactory.create(terrainPath, 4)
        Vegetation v
        KnowledgeVegetation kv
        askKnowledge.doAskAsync(ofSeconds(10), KnowledgeTree.TYPE).whenComplete({ it, ex ->
            kv = it.find { it.name == "PINE" }
        } ).get()
        v = kv.createObject(generator.generate())
        v.map = gm.id
        v.pos.x = 11
        v.pos.y = 15
        v.pos.z = 9
        def loaded = new VegetationLoadKnowledges(kv)
        loaded.loadKnowledges(askKnowledge)
        def vkn = new VegetationKnowledge()
        def k = vkn.createKnowledgeService()
        vkn.setLoadedKnowledges(loaded)
        def knowledge = vkn.createRulesKnowledgeFromSource(k, "PineRules.java")
        def root = MapChunk.getChunk(chunks, 0)
        def block = MapChunkBuffer.findBlock(root, 11, 15, 9, chunks)
        vkn.run(askKnowledge, knowledge, v, kv, chunks, chunks, gm)
        int r = 2
        for (int zz = v.pos.z - r; zz < v.pos.z + r; zz++) {
            for (int yy = v.pos.y - r; yy < v.pos.y + r; yy++) {
                for (int xx = v.pos.x - r; xx < v.pos.x + r; xx++) {
                    def b = MapChunkBuffer.findBlock(root, new GameBlockPos(xx, yy, zz), chunks)
                    println "[$b.pos.x,$b.pos.y,$b.pos.z,$b.parent,$b.material,$b.object,$b.temp,$b.lux,0b$b.p],"
                }
            }
        }
        println "##### $v"
        vkn.run(askKnowledge, knowledge, v, kv, chunks, chunks, gm)
        for (int zz = v.pos.z - r; zz < v.pos.z + r; zz++) {
            for (int yy = v.pos.y - r; yy < v.pos.y + r; yy++) {
                for (int xx = v.pos.x - r; xx < v.pos.x + r; xx++) {
                    def b = MapChunkBuffer.findBlock(root, new GameBlockPos(xx, yy, zz), chunks)
                    println "[$b.pos.x,$b.pos.y,$b.pos.z,$b.parent,$b.material,$b.object,$b.temp,$b.lux,0b$b.p],"
                }
            }
        }
        chunks.close()
    }
}
