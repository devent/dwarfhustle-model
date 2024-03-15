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
import java.util.function.Consumer

import org.apache.commons.io.IOUtils
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

/**
 * @see MapChunksStore
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
class MapChunksStoreTest {

    @Test
    void put_chunks(@TempDir Path tmp) {
        int map = 0
        new ChunksPosList().run().each { stores ->
            def chunkSize = stores.chunkSize
            def chunksCount = stores.chunksCount
            def store = new MapChunksStore(tmp.resolve("${map++}.map"), chunkSize, chunksCount)
            createChunks(stores.chunks, { store.setChunk(it) }, 0, 0, chunkSize)
            store.close()
        }
        println "done"
    }

    def createChunks(List chunks, Consumer store, long parent, long chunkId, int chunkSize) {
        chunks.each {
            def chunk = MapChunkTest.createTestChunk(chunkId++, chunkSize, new GameChunkPos(it.chunk[0], it.chunk[1], it.chunk[2], it.chunk[3], it.chunk[4], it.chunk[5]))
            chunk.parent = parent
            for (int i = 0; i < it.blocks.size(); i += 3) {
                def block = MapBlockTest.createTestBlock()
                block.parent = chunk.id
                block.pos = new GameBlockPos(it.blocks[i + 0], it.blocks[i + 1], it.blocks[i + 2])
                chunk.setBlock(block)
                //println block
            }
            println chunk
            store.accept(chunk)
            createChunks(it.chunks, store, chunk.id, chunkId, chunkSize)
        }
    }

    @Test
    void load_get_chunks(@TempDir Path tmp) {
        new ChunksPosList().run().each { stores ->
            def chunkSize = stores.chunkSize
            def chunksCount = stores.chunksCount
            def fileName = "chunk_size_${chunkSize}_count_${chunksCount}_0.map.txt"
            def stream = MapChunksStoreTest.class.getResourceAsStream(fileName)
            def file = tmp.resolve("0.map")
            IOUtils.copy(MapChunksStoreTest.class.getResource(fileName), file.toFile())
            def store = new MapChunksStore(file, chunkSize, chunksCount)
            def expectedList = []
            createChunks(stores.chunks, { expectedList << it }, 0, 0, chunkSize)
            def list = []
            (0..<chunksCount).each {
                println it
                def chunk = store.getChunk(it)
                println chunk
                list << chunk
            }
            store.close()
        }
    }

    @Test
    void load_for_each_chunks(@TempDir Path tmp) {
        def chunkSize = 2
        def chunksCount = 9
        def fileName = "chunk_size_${chunkSize}_count_${chunksCount}_0.map.txt"
        def stream = MapChunksStoreTest.class.getResourceAsStream(fileName)
        def file = tmp.resolve("0.map")
        IOUtils.copy(MapChunksStoreTest.class.getResource(fileName), file.toFile())
        def store = new MapChunksStore(file, chunkSize, chunksCount)
        def list = []
        store.forEachValue {
            assert it.id != -1
            println it
            list << it
        }
        store.close()
        assert list.size() == chunksCount
    }

    @Test
    void put_and_get_map_chunks_benchmark(@TempDir Path tmp) {
        def chunkSize = 2
        def chunksCount = 1
        def fileName = "chunk_size_${chunkSize}_count_${chunksCount}_0.map.txt"
        def file = tmp.resolve("0.map")
        def store = new MapChunksStore(file, chunkSize, chunksCount)
        for (int i = 0; i < chunksCount; i++) {
            for (int x = 0; x < 32; x++) {
                for (int y = 0; y < 32; y++) {
                    for (int z = 0; z < 32; z++) {
                        def mc = MapChunkTest.createTestChunk(i, chunkSize)
                        mc.pos = new GameChunkPos(0, 0, 0, x, y, z)
                        store.setChunk(mc)
                        def mcret = store.getChunk(mc.cid)
                        assert mcret.pos.ep.x == x
                        assert mcret.pos.ep.y == y
                        assert mcret.pos.ep.z == z
                    }
                }
            }
        }
        store.close()
        println "done"
    }
}
