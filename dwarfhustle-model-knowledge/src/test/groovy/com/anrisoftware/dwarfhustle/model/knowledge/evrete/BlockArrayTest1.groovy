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

import java.nio.file.Path

import org.apache.commons.io.IOUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

import com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos
import com.anrisoftware.dwarfhustle.model.api.objects.MapChunksStore

/**
 * @see BlockArray
 */
class BlockArrayTest1 {

    static int OXYGEN_ID = 923

    static int STONE_ID = 852

    static int LIQUID_ID = 930

    @TempDir
    static Path tmp

    static path = "/home/devent/Projects/dwarf-hustle/docu/terrain-maps"

    static MapChunksStore createStore(Path tmp, int w, int h, int d, int chunkSize, int chunksCount) {
        def name = "terrain_${w}_${h}_${d}_${chunkSize}_${chunksCount}"
        def fileName = "${path}/${name}.map"
        def stream = MapBlockArrayTest.class.getResourceAsStream(fileName)
        def file = tmp.resolve("${name}.map")
        IOUtils.copy(new FileInputStream(fileName), new FileOutputStream(file.toFile()))
        return new MapChunksStore(file, w, h, chunkSize, chunksCount)
    }

    BlockArray array

    MapChunksStore store

    @BeforeEach
    void setupBlocks() {
        //        int w = 512
        //        int h = 512
        //        int d = 128
        //        int chunkSize = 32
        //        int chunksCount = 1353
    }

    @CsvSource(["4,4,4,2,9"])
    @ParameterizedTest
    void up_empty_block_discovered(int w, int h, int d, int chunkSize, int chunksCount) {
        this.store = createStore(tmp, w, h, d, chunkSize, chunksCount)
        def worker = new BlockArrayWorker(store)
        worker.createKnowledge()
        worker.runRules()
        def blocks = []
        println "["
        for (int z = 0; z < d; z++) {
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    def b = store.findBlock(new GameBlockPos(x, y, z)).get().getTwo()
                    println "[$b.pos.x,$b.pos.y,$b.pos.z,$b.parent,$b.material,$b.object,$b.temp,$b.lux,0b$b.p],"
                }
            }
        }
        println "]"
    }
}
