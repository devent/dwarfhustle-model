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
package com.anrisoftware.dwarfhustle.model.db.buffers

import static org.apache.commons.lang3.StringUtils.replace
import static org.junit.jupiter.params.provider.Arguments.of

import java.nio.ByteBuffer
import java.util.stream.Stream

import org.agrona.concurrent.UnsafeBuffer
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

import com.anrisoftware.dwarfhustle.model.api.objects.GameChunkPos
import com.anrisoftware.dwarfhustle.model.api.objects.MapChunk

import groovy.util.logging.Slf4j

/**
 * @see MapChunkBuffer
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
class MapChunkBufferTest {

    static Stream write_read_map_chunk() {
        def args = []
        def offset = 0
        def neighbors = []
        (1..26).each {
            neighbors[it - 1] = it
        }
        def chunks = []
        def b = ByteBuffer.allocate(offset + MapChunkBuffer.SIZE)
        args << of(b, offset, 2, 4, 2, 0, 0, 0, 2, 2, 2, neighbors, chunks, '02000400 02000000 00000000 02000200 02000100 02000300 04000500 06000700 08000900 0a000b00 0c000d00 0e000f00 10001100 12001300 14001500 16001700 18001900 1a000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000')
        //
        offset = 0
        chunks = []
        chunks.add([10, 1, 2, 3, 4, 5, 6])
        chunks.add([11, 1, 2, 3, 4, 5, 6])
        chunks.add([12, 1, 2, 3, 4, 5, 6])
        chunks.add([13, 1, 2, 3, 4, 5, 6])
        chunks.add([14, 1, 2, 3, 4, 5, 6])
        chunks.add([15, 1, 2, 3, 4, 5, 6])
        chunks.add([16, 1, 2, 3, 4, 5, 6])
        chunks.add([17, 1, 2, 3, 4, 5, 6])
        b = ByteBuffer.allocate(offset + MapChunkBuffer.SIZE)
        args << of(b, offset, 2, 4, 2, 0, 0, 0, 4, 4, 4, neighbors, chunks, '02000400 02000000 00000000 04000400 04000100 02000300 04000500 06000700 08000900 0a000b00 0c000d00 0e000f00 10001100 12001300 14001500 16001700 18001900 1a001000 01000200 03000400 05000600 11000100 02000300 04000500 06000a00 01000200 03000400 05000600 0b000100 02000300 04000500 06000c00 01000200 03000400 05000600 0d000100 02000300 04000500 06000e00 01000200 03000400 05000600 0f000100 02000300 04000500 06000000 00000000 00000000 00000000')
        //
        offset = 3
        b = ByteBuffer.allocate(offset + MapChunkBuffer.SIZE)
        args << of(b, offset, 2, 4, 2, 0, 0, 0, 4, 4, 4, neighbors, chunks, '00000002 00040002 00000000 00000004 00040004 00010002 00030004 00050006 00070008 0009000a 000b000c 000d000e 000f0010 00110012 00130014 00150016 00170018 0019001a 00100001 00020003 00040005 00060011 00010002 00030004 00050006 000a0001 00020003 00040005 0006000b 00010002 00030004 00050006 000c0001 00020003 00040005 0006000d 00010002 00030004 00050006 000e0001 00020003 00040005 0006000f 00010002 00030004 00050006 00000000 00000000 00000000 000000')
        //
        offset = 3
        b = ByteBuffer.allocate(offset + MapChunkBuffer.SIZE)
        args << of(b, offset, 2, 4, 2, 0, 0, 0, 4, 4, 4, neighbors, chunks, '00000002 00040002 00000000 00000004 00040004 00010002 00030004 00050006 00070008 0009000a 000b000c 000d000e 000f0010 00110012 00130014 00150016 00170018 0019001a 00100001 00020003 00040005 00060011 00010002 00030004 00050006 000a0001 00020003 00040005 0006000b 00010002 00030004 00050006 000c0001 00020003 00040005 0006000d 00010002 00030004 00050006 000e0001 00020003 00040005 0006000f 00010002 00030004 00050006 00000000 00000000 00000000 000000')
        //
        Stream.of(args as Object[])
    }

    @ParameterizedTest
    @MethodSource
    void write_read_map_chunk(ByteBuffer b, int offset, int cid, int parent, int csize, int sx, int sy, int sz, int ex, int ey, int ez, List neighbors, List chunks, def expected) {
        def chunk = new MapChunk(cid, parent, csize, new GameChunkPos(sx, sy, sz, ex, ey, ez))
        chunk.setNeighbors(neighbors as int[])
        for (int i = 0; i < chunks.size(); i++) {
            chunk.chunks.put(chunks[i][0], new GameChunkPos(
                    chunks[i][1], chunks[i][2], chunks[i][3], chunks[i][4], chunks[i][5], chunks[i][6]))
        }
        MapChunkBuffer.write(new UnsafeBuffer(b), offset, chunk)
        log.debug("write_read_map_chunk {}", HexFormat.of().formatHex(b.array()))
        assert HexFormat.of().formatHex(b.array()) == replace(expected, " ", "")
        MapChunk thatChunk = MapChunkBuffer.read(new UnsafeBuffer(b), offset)
        assert thatChunk.cid == cid
        assert thatChunk.parent == parent
        assert thatChunk.chunkSize == csize
        assert thatChunk.pos.x == sx
        assert thatChunk.pos.y == sy
        assert thatChunk.pos.z == sz
        assert thatChunk.pos.ep.x == ex
        assert thatChunk.pos.ep.y == ey
        assert thatChunk.pos.ep.z == ez
        thatChunk.neighbors.eachWithIndex { it, i ->
            assert it == neighbors[i]
        }
        thatChunk.chunks.keyValuesView().eachWithIndex { it, i ->
            if (it.one != 0) {
                def list = [
                    it.one,
                    it.two.x,
                    it.two.y,
                    it.two.z,
                    it.two.ep.x,
                    it.two.ep.y,
                    it.two.ep.z
                ]
                assert chunks.contains(list)
            }
        }
    }
}
