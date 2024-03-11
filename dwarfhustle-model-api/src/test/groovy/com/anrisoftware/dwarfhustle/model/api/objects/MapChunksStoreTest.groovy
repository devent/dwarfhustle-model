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

/**
 * @see MapChunksStore
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
class MapChunksStoreTest {

    @Test
    void put_and_get_chunks(@TempDir Path tmp) {
        int map = 0
        long chunkId = 0
        new ChunksPosList().run().each { stores ->
            def chunkSize = stores.chunkSize
            def store = new MapChunksStore(tmp.resolve("${map++}.map"), chunkSize)
            createChunks(stores.chunks, store, chunkId, chunkId, chunkSize)
            store.close()
        }
        println "done"
    }

    def createChunks(List chunks, MapChunksStore store, long parent, long chunkId, int chunkSize) {
        chunks.each {
            def chunk = MapChunkTest.createTestChunk(chunkId++, chunkSize)
            chunk.parent = parent
            chunk.pos =  new GameChunkPos(it.chunk[0], it.chunk[1], it.chunk[2], it.chunk[3], it.chunk[4], it.chunk[5])
            for (int i = 0; i < it.blocks.size(); i += 3) {
                def block = MapBlockTest.createTestBlock()
                block.parent = chunk.id
                block.pos = new GameBlockPos(it.blocks[i + 0], it.blocks[i + 1], it.blocks[i + 2])
                chunk.setBlock(block)
                //println block
            }
            //println chunk
            store.setChunk(chunk)
            createChunks(it.chunks, store, chunk.id, chunkId, chunkSize)
        }
    }

    @Test
    void for_each_blocks() {
        def positionsList = new BlocksPosList().run()
        positionsList.each { List positions ->
            def store = new MapBlocksStore(Math.cbrt(positions.size() / 3) as int)
            def mb = MapBlockTest.createTestBlock()
            for (int i = 0; i < positions.size(); i += 3) {
                mb.pos = new GameBlockPos(positions[i + 0], positions[i + 1], positions[i + 2])
                store.setBlock(mb)
            }
            def list = []
            store.forEachValue({ list.add(it) })
            assert list.size() == positions.size() / 3
            list.eachWithIndex { MapBlock block, int i ->
                def blockpos = [
                    block.pos.x,
                    block.pos.y,
                    block.pos.z
                ]
                assert positions.containsAll(blockpos)
            }
        }
    }

    @Test
    void put_and_get_map_block_benchmark() {
        int chunkSize = 4
        def store = new MapBlocksStore(chunkSize)
        def mb = MapBlockTest.createTestBlock()
        int blocksCount = 100
        for (int i = 0; i < blocksCount; i++) {
            for (int x = 0; x < 32; x++) {
                for (int y = 0; y < 32; y++) {
                    for (int z = 0; z < 32; z++) {
                        mb.pos = new GameBlockPos(x, y, z)
                        store.setBlock(mb)
                        def mbret = store.getBlock(mb.pos)
                        assert mb.pos.x == x
                        assert mb.pos.y == y
                        assert mb.pos.z == z
                    }
                }
            }
        }
    }
}
