/*
 * dwarfhustle-model-db - Manages the compile dependencies for the model.
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
package com.anrisoftware.dwarfhustle.model.db.lmbd

import java.nio.file.Path

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos
import com.anrisoftware.dwarfhustle.model.api.objects.GameChunkPos
import com.anrisoftware.dwarfhustle.model.api.objects.GameMap
import com.anrisoftware.dwarfhustle.model.api.objects.MapBlock
import com.anrisoftware.dwarfhustle.model.api.objects.MapChunk
import com.anrisoftware.dwarfhustle.model.api.objects.PropertiesSet
import com.anrisoftware.dwarfhustle.model.db.lmbd.MapChunksLmbdStorage.MapChunksLmbdStorageFactory
import com.google.inject.Guice
import com.google.inject.Injector

import groovy.util.logging.Slf4j

/**
 * @see MapChunksLmbdStorage
 */
@Slf4j
class MapChunksLmbdStorageTest {

    static Injector injector

    @BeforeAll
    static void setupInjector() {
        injector = Guice.createInjector(new DwarfhustleModelDbLmbdModule())
    }

    static List createChunks() {
        def chunks = []
        def root = new MapChunk(0, 0, 16, new GameChunkPos(0, 0, 0, 32, 32, 32))
        chunks << root
        def chunk = new MapChunk(1, root.cid, 16, new GameChunkPos(0, 0, 0, 16, 16, 16))
        chunks << chunk
        root.chunks.put(chunk.cid, chunk.pos)
        chunk = new MapChunk(2, root.cid, 16, new GameChunkPos(16, 0, 0, 32, 16, 16))
        chunks << chunk
        root.chunks.put(chunk.cid, chunk.pos)
        chunk = new MapChunk(3, root.cid, 16, new GameChunkPos(16, 16, 0, 32, 32, 16))
        chunks << chunk
        root.chunks.put(chunk.cid, chunk.pos)
        return chunks
    }

    @Test
    void putChunk_test(@TempDir Path tmp) {
        def gm = new GameMap(1)
        gm.width = 32
        gm.height = 32
        gm.depth = 32
        int chunkSize = 16
        def storage = injector.getInstance(MapChunksLmbdStorageFactory).create(tmp, chunkSize)
        def chunks = createChunks()
        chunks.each {
            storage.putChunk(it)
        }
        chunks.each {
            def thatChunk = storage.getChunk(it.cid)
            println thatChunk
            println thatChunk.chunks
            assert thatChunk.cid == it.cid
            assert thatChunk.parent == it.parent
            assert thatChunk.chunkSize == it.chunkSize
            assert thatChunk.pos == it.pos
        }
        def block = new MapBlock()
        block.parent = 2
        block.pos = new GameBlockPos(0, 0, 0)
        block.material = 200
        block.object = 300
        block.p = new PropertiesSet()
        block.temp = 25
        block.lux = 150
        storage.putBlock(chunks[1], block)
        block = new MapBlock()
        block.parent = 2
        block.pos = new GameBlockPos(1, 0, 0)
        block.material = 200
        block.object = 300
        block.p = new PropertiesSet()
        block.temp = 25
        block.lux = 150
        storage.putBlock(chunks[1], block)
        storage.close()
        TestUtils.listFiles log, tmp
    }

    @Test
    void forEachValue_test(@TempDir Path tmp) {
        def gm = new GameMap(1)
        gm.width = 32
        gm.height = 32
        gm.depth = 32
        int chunkSize = 16
        def storage = injector.getInstance(MapChunksLmbdStorageFactory).create(tmp, chunkSize)
        def chunks = createChunks()
        storage.putChunks(chunks)
        def thatChunks = []
        storage.forEachValue({ thatChunks << it })
        assert thatChunks.size() == chunks.size()
        storage.close()
    }

    @Test
    void putBlocks_test(@TempDir Path tmp) {
        def gm = new GameMap(1)
        gm.width = 32
        gm.height = 32
        gm.depth = 32
        int chunkSize = 16
        def storage = injector.getInstance(MapChunksLmbdStorageFactory).create(tmp, chunkSize)
        def chunks = createChunks()
        chunks.each {
            storage.putChunk(it)
        }
        def blocks = []
        (0..15).each { x ->
            (0..15).each { y ->
                (0..15).each { z ->
                    def block = new MapBlock()
                    block.parent = 1
                    block.pos = new GameBlockPos(x, y, z)
                    block.material = 200
                    block.object = 300
                    block.p = new PropertiesSet()
                    block.temp = 25
                    block.lux = 150
                    blocks << block
                }
            }
        }
        storage.putBlocks(chunks[1], blocks)
        storage.close()
        TestUtils.listFiles log, tmp
    }
}
