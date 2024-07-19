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
package com.anrisoftware.dwarfhustle.model.terrainimage

import static com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.KnowledgeGetMessage.askKnowledgeObjects
import static java.lang.String.format
import static java.time.Duration.ofSeconds
import static java.util.concurrent.CompletableFuture.supplyAsync
import static org.junit.jupiter.params.provider.Arguments.of

import java.nio.file.Path
import java.util.concurrent.TimeUnit
import java.util.stream.Stream

import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

import com.anrisoftware.dwarfhustle.model.actor.ActorSystemProvider
import com.anrisoftware.dwarfhustle.model.actor.DwarfhustleModelActorsModule
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message
import com.anrisoftware.dwarfhustle.model.api.objects.DwarfhustleModelApiObjectsModule
import com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos
import com.anrisoftware.dwarfhustle.model.api.objects.GameMap
import com.anrisoftware.dwarfhustle.model.api.objects.MapChunksStore
import com.anrisoftware.dwarfhustle.model.knowledge.evrete.TerrainKnowledge
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.DwarfhustlePowerloomModule
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.KnowledgeJcsCacheActor
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.PowerLoomKnowledgeActor
import com.anrisoftware.dwarfhustle.model.terrainimage.TerrainImageCreateMap.TerrainImageCreateMapFactory
import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.assistedinject.FactoryModuleBuilder

import akka.actor.typed.ActorRef

/**
 * @see TerrainImageCreateMap
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
class TerrainImageCreateMapTest {

    static Injector injector

    static ActorSystemProvider actor

    static ActorRef<Message> knowledgeActor

    static ActorRef<Message> cacheActor

    static TerrainKnowledge terrainKnowledge

    @BeforeAll
    static void setupActor() {
        this.injector = Guice.createInjector(
                new DwarfhustleModelActorsModule(), new DwarfhustlePowerloomModule(), new DwarfhustleModelApiObjectsModule(),
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        install(new FactoryModuleBuilder().implement(TerrainImageCreateMap.class, TerrainImageCreateMap.class)
                                .build(TerrainImageCreateMapFactory.class))
                    }
                }
                )
        actor = injector.getInstance(ActorSystemProvider.class)
        KnowledgeJcsCacheActor.create(injector, ofSeconds(1), actor.getObjectGetterAsync(PowerLoomKnowledgeActor.ID)).whenComplete({ it, ex ->
            cacheActor = it
        } ).get()
        PowerLoomKnowledgeActor.create(injector, ofSeconds(1), supplyAsync({cacheActor})).whenComplete({ it, ex ->
            knowledgeActor = it
        } ).get()
        this.terrainKnowledge = new TerrainKnowledge({ timeout, type -> askKnowledgeObjects(actor.actorSystem, timeout, type) })
    }

    @AfterAll
    static void testsFinished() {
        FileUtils.copyDirectory(tmp, new File("/home/devent/Projects/dwarf-hustle/docu/terrain-maps/"))
        println tmp
    }

    static Stream test_start_import_terrain() {
        def args = []
        //        args << of(TerrainImage.terrain_4_4_4_2, true, new Terrain_4_4_4_2_blocks_expected().run())
        //        args << of(TerrainImage.terrain_8_8_8_4, true, new Terrain_8_8_8_4_blocks_expected().run())
        //
        args << of(TerrainImage.terrain_4_4_4_2, false, new Terrain_4_4_4_2_blocks_expected().run())
        args << of(TerrainImage.terrain_8_8_8_4, false, new Terrain_8_8_8_4_blocks_expected().run())
        args << of(TerrainImage.terrain_32_32_32_4, false, null)
        args << of(TerrainImage.terrain_32_32_32_8, false, null)
        args << of(TerrainImage.terrain_512_512_128_16, false, null)
        args << of(TerrainImage.terrain_512_512_128_32, false, null)
        args << of(TerrainImage.terrain_512_512_128_64, false, null)
        //
        //        args << of(TerrainImage.terrain_128_128_128_16, false, null)
        //        args << of(TerrainImage.terrain_128_128_128_32, false, null)
        //        args << of(TerrainImage.terrain_256_256_128_16, false, null)
        //        args << of(TerrainImage.terrain_256_256_128_32, false, null)
        //        args << of(TerrainImage.terrain_256_256_128_64, false, null)
        Stream.of(args as Object[])
    }

    @TempDir
    static File tmp

    @ParameterizedTest
    @MethodSource()
    @Timeout(value = 60, unit = TimeUnit.MINUTES)
    void test_start_import_terrain(TerrainImage image, boolean printBlocks, List blocksExpected) {
        def terrain = image.terrain
        def gm = new GameMap(1)
        gm.chunkSize = image.chunkSize
        gm.chunksCount = image.chunksCount
        gm.width = terrain.width
        gm.height = terrain.height
        gm.depth = terrain.depth
        def file = format("terrain_%d_%d_%d_%d_%d.map", gm.width, gm.height, gm.depth, gm.chunkSize, gm.chunksCount)
        def store = new MapChunksStore(Path.of(tmp.absolutePath, file), gm.width, gm.height, gm.chunkSize, gm.chunksCount);
        def createMap = injector.getInstance(TerrainImageCreateMapFactory).create(store, terrainKnowledge)
        createMap.startImportMapping(TerrainImageCreateMapTest.class.getResource(image.imageName), terrain, gm)
        store.close()
        if (printBlocks) {
            println "["
            for (int z = 0; z < terrain.depth; z++) {
                for (int y = 0; y < terrain.height; y++) {
                    for (int x = 0; x < terrain.width; x++) {
                        def b = store.findBlock(new GameBlockPos(x, y, z)).get().getTwo()
                        println "[$b.pos.x,$b.pos.y,$b.pos.z,$b.parent,$b.material,$b.object,$b.temp,$b.lux,0b$b.p],"
                    }
                }
            }
            println "]"
        }
        if (blocksExpected) {
            int i = 0
            for (int z = 0; z < terrain.depth; z++) {
                for (int y = 0; y < terrain.height; y++) {
                    for (int x = 0; x < terrain.width; x++) {
                        def b = store.findBlock(new GameBlockPos(x, y, z)).get().getTwo()
                        assert blocksExpected[i][0] == b.pos.x
                        assert blocksExpected[i][1] == b.pos.y
                        assert blocksExpected[i][2] == b.pos.z
                        assert blocksExpected[i][3] == b.parent
                        assert blocksExpected[i][4] == b.material
                        assert blocksExpected[i][5] == b.object
                        assert blocksExpected[i][6] == b.temp
                        assert blocksExpected[i][7] == b.lux
                        assert blocksExpected[i][8] == b.p.bits
                        i++
                    }
                }
            }
        }
        //        def b = store.findBlock(new GameBlockPos(21, 1, 8)).get().getTwo()
        //        println "[$b.pos.x,$b.pos.y,$b.pos.z,$b.parent,$b.material,$b.object,$b.temp,$b.lux,0b$b.p],"
        //        b = store.findBlock(new GameBlockPos(21, 1, 9)).get().getTwo()
        //        println "[$b.pos.x,$b.pos.y,$b.pos.z,$b.parent,$b.material,$b.object,$b.temp,$b.lux,0b$b.p],"
        println "$image done"
    }
}
