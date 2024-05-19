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

import static org.apache.commons.lang3.StringUtils.replace
import static org.junit.jupiter.params.provider.Arguments.of

import java.nio.ByteBuffer
import java.util.stream.Stream

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

import groovy.util.logging.Slf4j

/**
 * @see MapChunkBuffer
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
class MapChunkBufferTest {

    static Stream set_get_map_chunk() {
        def args = []
        def offset = 0 * 4
        def chunks = []
        def neighbors = []
        (1..26).each {
            neighbors[it - 1] = it
        }
        def b = ByteBuffer.allocate(offset + MapChunkBuffer.SIZE_MIN)
        args << of(b, offset, 2, 4, 4, 1, 2, 3, 4, 5, 6, neighbors, chunks, '00020004 00040001 00020003 00040005 0006 000100020003000400050006000700080009000a000b000c000d000e000f0010001100120013001400150016001700180019001a000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000')
        //
        offset = 0 * 4
        chunks = []
        chunks.addAll([7, 1, 2, 3, 4, 5, 6])
        chunks.addAll([8, 1, 2, 3, 4, 5, 6])
        b = ByteBuffer.allocate(offset + MapChunkBuffer.SIZE_MIN)
        args << of(b, offset, 2, 4, 4, 1, 2, 3, 4, 5, 6, neighbors, chunks, '000200040004000100020003000400050006000100020003000400050006000700080009000a000b000c000d000e000f0010001100120013001400150016001700180019001a000200070001000200030004000500060008000100020003000400050006000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000')
        //
        offset = 2 * 4
        chunks = []
        chunks.addAll([7, 1, 2, 3, 4, 5, 6])
        chunks.addAll([8, 1, 2, 3, 4, 5, 6])
        b = ByteBuffer.allocate(offset + MapChunkBuffer.SIZE_MIN)
        args << of(b, offset, 2, 4, 4, 1, 2, 3, 4, 5, 6, neighbors, chunks, '0000000000000000000200040004000100020003000400050006000100020003000400050006000700080009000a000b000c000d000e000f0010001100120013001400150016001700180019001a000200070001000200030004000500060008000100020003000400050006000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000')
        //
        Stream.of(args as Object[])
    }

    @ParameterizedTest
    @MethodSource()
    void set_get_map_chunk(ByteBuffer b, int offset, int cid, int parent, int csize, int sx, int sy, int sz, int ex, int ey, int ez, List neighbors, List chunks, def expected) {
        def sb = b.asShortBuffer()
        def ib = b.asIntBuffer()
        int soffset = offset / 2
        MapChunkBuffer.setCid(sb, soffset, cid)
        MapChunkBuffer.setParent(sb, soffset, parent)
        MapChunkBuffer.setChunkSize(sb, soffset, csize)
        MapChunkBuffer.setPos(sb, soffset, sx, sy, sz, ex, ey, ez)
        MapChunkBuffer.setNeighbors(sb, soffset, neighbors as int[])
        MapChunkBuffer.setChunksCount(sb, soffset, chunks.size())
        if (chunks.size() > 0) {
            MapChunkBuffer.setChunks(sb, soffset, chunks.size() / 7 as int, chunks as int[])
        }
        log.debug("set_get_map_chunk {}", HexFormat.of().formatHex(b.array()))
        assert HexFormat.of().formatHex(b.array()) == replace(expected, " ", "")
        assert MapChunkBuffer.getCid(sb, soffset) == cid
        assert MapChunkBuffer.getParent(sb, soffset) == parent
        assert MapChunkBuffer.getChunkSize(sb, soffset) == csize
        assert MapChunkBuffer.getSx(sb, soffset) == sx
        assert MapChunkBuffer.getSy(sb, soffset) == sy
        assert MapChunkBuffer.getSz(sb, soffset) == sz
        assert MapChunkBuffer.getEx(sb, soffset) == ex
        assert MapChunkBuffer.getEy(sb, soffset) == ey
        assert MapChunkBuffer.getEz(sb, soffset) == ez
        def n = new int[26]
        MapChunkBuffer.getNeighbors(sb, soffset, n)
        assert n as List == neighbors
        assert MapChunkBuffer.getChunksCount(sb, soffset) == chunks.size() / 7
        assert MapChunkBuffer.getChunks(sb, soffset, null) == chunks
    }

    static Stream write_read_map_chunk() {
        def args = []
        def offset = 0
        def neighbors = []
        (1..26).each {
            neighbors[it - 1] = it
        }
        def chunks = []
        def blocksBuffer = ByteBuffer.allocate(0 * MapBlockBuffer.SIZE)
        def b = ByteBuffer.allocate(offset + MapChunkBuffer.SIZE_MIN + blocksBuffer.capacity())
        args << of(b, offset, 2, 4, 4, 1, 2, 3, 4, 5, 6, neighbors, chunks, blocksBuffer, '00020004 00040001 0002000300040005000600000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000')
        //
        offset = 0
        chunks = []
        chunks.addAll([7, 1, 2, 3, 4, 5, 6])
        chunks.addAll([8, 1, 2, 3, 4, 5, 6])
        b = ByteBuffer.allocate(offset + MapChunkBuffer.SIZE_MIN + blocksBuffer.capacity())
        args << of(b, offset, 2, 4, 4, 1, 2, 3, 4, 5, 6, neighbors, chunks, blocksBuffer, '00020004 00040001 0002000300040005000600000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000200080001000200030004000500060007000100020003000400050006000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000')
        //
        offset = 4*2
        chunks = []
        chunks.addAll([7, 1, 2, 3, 4, 5, 6])
        chunks.addAll([8, 1, 2, 3, 4, 5, 6])
        b = ByteBuffer.allocate(offset + MapChunkBuffer.SIZE_MIN + blocksBuffer.capacity())
        args << of(b, offset, 2, 4, 4, 1, 2, 3, 4, 5, 6, neighbors, chunks, blocksBuffer, '000000000000000000020004000400010002000300040005000600000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000200080001000200030004000500060007000100020003000400050006000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000')
        //
        offset = 4*2
        chunks = []
        chunks.addAll([7, 1, 2, 3, 4, 5, 6])
        chunks.addAll([8, 1, 2, 3, 4, 5, 6])
        blocksBuffer = ByteBuffer.allocate(1 * MapBlockBuffer.SIZE)
        def blocksBufferI = blocksBuffer.asIntBuffer()
        (0..<blocksBufferI.capacity()).each {
            blocksBufferI.put(0xFF)
        }
        b = ByteBuffer.allocate(offset + MapChunkBuffer.SIZE_MIN + blocksBuffer.capacity())
        args << of(b, offset, 2, 4, 4, 1, 2, 3, 4, 5, 6, neighbors, chunks, blocksBuffer, '0000000000000000000200040004000100020003000400050006000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000002000800010002000300040005000600070001000200030004000500060000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000')
        //
        Stream.of(args as Object[])
    }

    @ParameterizedTest
    @MethodSource
    void write_read_map_chunk(ByteBuffer b, int offset, int cid, int parent, int csize, int sx, int sy, int sz, int ex, int ey, int ez, List neighbors, List chunks, def blocksBuffer, def expected) {
        def chunk = new MapChunk(cid, parent, csize, new GameChunkPos(sx, sy, sz, ex, ey, ez))
        chunk.setBlocksBuffer(blocksBuffer)
        for (int i = 0; i < chunks.size() / 7; i++) {
            chunk.chunks.put(chunks[i * 7 + 0], new GameChunkPos(chunks[i * 7 + 1], chunks[i * 7 + 2], chunks[i * 7 + 3], chunks[i * 7 + 4], chunks[i * 7 + 5], chunks[i * 7 + 6]))
        }
        MapChunkBuffer.writeMapChunk(b, offset, chunk)
        log.debug("write_read_map_chunk {}", HexFormat.of().formatHex(b.array()))
        assert HexFormat.of().formatHex(b.array()) == replace(expected, " ", "")
    }
}
