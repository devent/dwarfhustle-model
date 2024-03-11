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

    static MapChunk createTestChunk(long cid = 0, int chunkSize = 2) {
        def go = new MapChunk(cid, chunkSize)
        go.pos = new GameChunkPos(0, 0, 0, 4, 4, 4)
        go.updateCenterExtent(4, 4, 4)
        go.parent = 1000001
        (0..7).each { go.chunks.put(new GameChunkPos(it, it, it, it + 1, it + 1, it + 1), 88888888) }
        NeighboringDir.values().each { go.setNeighbor(it, 11111111) }
        return go
    }

    @Test
    void serialize_deserialize_no_block() {
        def chunk = createTestChunk()
        def buffout = new ByteArrayOutputStream(1024)
        def oout = new ObjectOutputStream(buffout)
        oout.writeObject(chunk)
        oout.close()
        def buffin = new ByteArrayInputStream(buffout.toByteArray())
        def oin = new ObjectInputStream(buffin)
        def thatchunk = oin.readObject() as MapChunk
        assert chunk.id == thatchunk.id
        assert chunk.pos == thatchunk.pos
        assert chunk.blocks != thatchunk.blocks
        assert chunk.centerExtent == thatchunk.centerExtent
        assert chunk.dir == thatchunk.dir
        assert chunk.chunks == thatchunk.chunks
        assert chunk.chunkSize == thatchunk.chunkSize
        assert chunk.parent == thatchunk.parent
        assert chunk.root == thatchunk.root
        assert chunk == thatchunk
    }

    @Test
    void stream_write_read_no_block() {
        def chunk = createTestChunk()
        def buffout = new ByteArrayOutputStream(1024)
        def oout = new DataOutputStream(buffout)
        chunk.writeStream(oout)
        oout.close()
        def buffin = new ByteArrayInputStream(buffout.toByteArray())
        def oin = new DataInputStream(buffin)
        def thatchunk = new MapChunk()
        thatchunk.readStream(oin)
        assert chunk.id == thatchunk.id
        assert chunk.pos == thatchunk.pos
        assert chunk.blocks != thatchunk.blocks
        assert chunk.centerExtent == thatchunk.centerExtent
        assert chunk.dir == thatchunk.dir
        assert chunk.chunks.containsAll(thatchunk.chunks.values())
        assert chunk.chunkSize == thatchunk.chunkSize
        assert chunk.parent == thatchunk.parent
        assert chunk.root == thatchunk.root
        assert chunk == thatchunk
    }

    @Test
    void serialize_deserialize_one_block() {
        def chunk = createTestChunk()
        def block = MapBlockTest.createTestBlock()
        block.pos = new GameBlockPos(0, 0, 0)
        chunk.setBlock(block)
        def buffout = new ByteArrayOutputStream()
        def oout = new ObjectOutputStream(buffout)
        oout.writeObject(chunk)
        oout.close()
        def buffin = new ByteArrayInputStream(buffout.toByteArray())
        def oin = new ObjectInputStream(buffin)
        def thatchunk = oin.readObject() as MapChunk
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
            def chunk = new MapChunk(11111111 + i, chunkSize)
            chunk.pos = new GameChunkPos(0, 0, 0, 32, 32, 32)
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
            assert thatchunk.cid == 11111111 + i
        }
    }
}
