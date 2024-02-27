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

import org.junit.jupiter.api.Test

/**
 * @see MapChunk
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
class MapChunkTest {

    @Test
    void map_tile_type() {
        def go = new MapChunk()
        assert go.objectType == "MapChunk"
    }

    @Test
    void serialize_deserialize_no_block() {
        def chunk = new MapChunk(11111111, new GameChunkPos(0, 0, 0, 4, 4, 4), 2)
        chunk.map = 111111101
        chunk.updateCenterExtent(4, 4, 4)
        chunk.root = true
        chunk.parent = 0
        def buffout = new ByteArrayOutputStream()
        def oout = new ObjectOutputStream(buffout)
        oout.writeObject(chunk)
        oout.close()
        def buffin = new ByteArrayInputStream(buffout.toByteArray())
        def oin = new ObjectInputStream(buffin)
        def thatchunk = oin.readObject() as MapChunk
        assert chunk.id == thatchunk.id
        assert chunk.map == thatchunk.map
        assert chunk.pos == thatchunk.pos
        assert chunk.blocks != thatchunk.blocks
        assert chunk.centerExtent == thatchunk.centerExtent
        assert chunk.chunkDir == thatchunk.chunkDir
        assert chunk.chunks == thatchunk.chunks
        assert chunk.chunkSize == thatchunk.chunkSize
        assert chunk.parent == thatchunk.parent
        assert chunk.root == thatchunk.root
        assert chunk == thatchunk
    }

    @Test
    void serialize_deserialize_one_block() {
        def chunk = new MapChunk(11111111, new GameChunkPos(0, 0, 0, 4, 4, 4), 2)
        def block = MapBlockTest.createTestBlock()
        block.pos = new GameBlockPos(0, 0, 0)
        chunk.setBlock(block)
        chunk.map = 111111101
        chunk.updateCenterExtent(4, 4, 4)
        chunk.root = true
        chunk.parent = 0
        def buffout = new ByteArrayOutputStream()
        def oout = new ObjectOutputStream(buffout)
        oout.writeObject(chunk)
        oout.close()
        def buffin = new ByteArrayInputStream(buffout.toByteArray())
        def oin = new ObjectInputStream(buffin)
        def thatchunk = oin.readObject() as MapChunk
        assert chunk.id == thatchunk.id
        assert chunk.map == thatchunk.map
        assert chunk.pos == thatchunk.pos
        assert chunk.blocks != thatchunk.blocks
        assert chunk.centerExtent == thatchunk.centerExtent
        assert chunk.chunkDir == thatchunk.chunkDir
        assert chunk.chunks == thatchunk.chunks
        assert chunk.chunkSize == thatchunk.chunkSize
        assert chunk.parent == thatchunk.parent
        assert chunk.root == thatchunk.root
        assert chunk == thatchunk
        def thatblock = chunk.getBlock(new GameBlockPos(0, 0, 0))
        assert block == thatblock
    }

    @Test
    void serialize_deserialize_multiple_block_benchmark() {
        def buffout = new ByteArrayOutputStream()
        def oout = new ObjectOutputStream(buffout)
        int chunkSize = 4
        def mb = MapBlockTest.createTestBlock()
        int chunksCount = 10
        for (int i = 0; i < chunksCount; i++) {
            def chunk = new MapChunk(11111111 + i, new GameChunkPos(0, 0, 0, 32, 32, 32), chunkSize)
            chunk.updateCenterExtent(32, 32, 32)
            for (int x = 0; x < 32; x++) {
                for (int y = 0; y < 32; y++) {
                    for (int z = 0; z < 32; z++) {
                        mb.pos = new GameBlockPos(x, y, z)
                        chunk.setBlock(mb)
                    }
                }
            }
            oout.writeObject(chunk)
        }
        oout.close()
        def buffin = new ByteArrayInputStream(buffout.toByteArray())
        def oin = new ObjectInputStream(buffin)
        for (int i = 0; i < chunksCount; i++) {
            def thatchunk = oin.readObject() as MapChunk
            assert thatchunk.id == 11111111 + i
        }
    }
}
