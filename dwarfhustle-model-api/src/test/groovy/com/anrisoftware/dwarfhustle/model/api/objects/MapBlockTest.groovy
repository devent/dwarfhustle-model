/*
 * dwarfhustle-model-api - Manages the compile dependencies for the model.
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
package com.anrisoftware.dwarfhustle.model.api.objects

import java.nio.file.Path

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

/**
 * @see MapBlock
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
class MapBlockTest {

    static MapBlock createTestBlock(int parent, def pos = new GameBlockPos(10, 10, 10)) {
        def go = new MapBlock(parent, pos)
        go.pos = new GameBlockPos(10, 10, 10)
        go.updateCenterExtent(4, 4, 4)
        go.parent = 7777777
        go.material = 8888888
        go.setNaturalFloor(true)
        go.setNaturalRoof(true)
        return go
    }

    @TempDir
    static Path tmp

    @ParameterizedTest
    @CsvSource([
        "4,4,4,2,9,0,0,0,U,false,0,0,0",
        "4,4,4,2,9,0,0,0,D,true,0,0,1",
        "4,4,4,2,9,0,0,0,W,false,0,0,0",
        "4,4,4,2,9,0,0,0,N,false,0,0,0",
        "4,4,4,2,9,0,0,0,S,true,0,1,0",
        //
        "4,4,4,2,9,0,0,0,E,true,1,0,0",
        "4,4,4,2,9,1,0,0,E,true,2,0,0",
        "4,4,4,2,9,2,0,0,E,true,3,0,0",
        "4,4,4,2,9,3,0,0,E,false,0,0,0",
        //
        "4,4,4,2,9,0,0,0,S,true,0,1,0",
        "4,4,4,2,9,0,1,0,S,true,0,2,0",
        "4,4,4,2,9,0,2,0,S,true,0,3,0",
        "4,4,4,2,9,0,3,0,S,false,0,0,0",
        //
        "4,4,4,2,9,0,0,0,D,true,0,0,1",
        "4,4,4,2,9,0,0,1,D,true,0,0,2",
        "4,4,4,2,9,0,0,2,D,true,0,0,3",
        "4,4,4,2,9,0,0,3,D,false,0,0,0",
        //
        "8,8,8,2,73,0,0,0,U,false,0,0,0",
        "8,8,8,2,73,0,0,0,D,true,0,0,1",
        "8,8,8,2,73,0,0,0,E,true,1,0,0",
    ])
    void getNeighbor_block(int w, int h, int d, int chunkSize, int chunksCount, int x, int y, int z, String dir, boolean found, int expectedX, int expectedY, int expectedZ) {
        def store = MapChunksStoreTest.createStore(tmp, "terrain_${w}_${h}_${d}_${chunkSize}_${chunksCount}", w, h, chunkSize, chunksCount)
        def chunkBlock = store.findBlock(new GameBlockPos(x, y, z))
        def nblock = chunkBlock.orElseThrow().two.getNeighbor(NeighboringDir.valueOf(dir), chunkBlock.orElseThrow().one, { store.getChunk(it) })
        if (found) {
            assert nblock.pos.x == expectedX
            assert nblock.pos.y == expectedY
            assert nblock.pos.z == expectedZ
        }
    }

    @Test
    void getNeighbor_block_benchmark() {
        int w = 32, h = 32, d = 32, chunkSize = 8, chunksCount = 73;
        def store = MapChunksStoreTest.createStore(tmp, "terrain_${w}_${h}_${d}_${chunkSize}_${chunksCount}", w, h, chunkSize, chunksCount)
        def retriever = { store.getChunk(it) }
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                for (int z = 0; z < d; z++) {
                    def chunkBlock = store.findBlock(new GameBlockPos(x, y, z))
                    NeighboringDir.values().each {
                        def nblock = chunkBlock.orElseThrow().two.getNeighbor(it, chunkBlock.orElseThrow().one, retriever)
                    }
                }
            }
        }
    }
}
