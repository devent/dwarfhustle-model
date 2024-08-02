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
import java.nio.file.Path
import java.util.stream.Stream

import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource

import com.anrisoftware.dwarfhustle.model.api.objects.GameChunkPos
import com.anrisoftware.dwarfhustle.model.api.objects.MapChunk
import com.anrisoftware.dwarfhustle.model.api.objects.NeighboringDir
import com.anrisoftware.dwarfhustle.model.db.store.MapChunksStoreTest

import groovy.util.logging.Slf4j

/**
 * @see MapChunkBuffer
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
class MapChunkBufferTest {

    @TempDir
    static Path tmp

    static Stream set_get_map_chunk() {
        def args = []
        def offset = 0
        def chunks = []
        def neighbors = []
        (1..26).each {
            neighbors[it - 1] = it
        }
        def b = ByteBuffer.allocate(offset + MapChunkBuffer.SIZE_MIN)
        args << of(b, offset, 2, 4, 2, 0, 0, 0, 2, 2, 2, neighbors, chunks, '', '000200040002000000000000000200020002000100020003000400050006000700080009000a000b000c000d000e000f0010001100120013001400150016001700180019001a000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000')
        //
        offset = 0
        chunks = []
        chunks.addAll([10, 1, 2, 3, 4, 5, 6])
        chunks.addAll([11, 1, 2, 3, 4, 5, 6])
        chunks.addAll([12, 1, 2, 3, 4, 5, 6])
        chunks.addAll([13, 1, 2, 3, 4, 5, 6])
        chunks.addAll([14, 1, 2, 3, 4, 5, 6])
        chunks.addAll([15, 1, 2, 3, 4, 5, 6])
        chunks.addAll([16, 1, 2, 3, 4, 5, 6])
        chunks.addAll([17, 1, 2, 3, 4, 5, 6])
        b = ByteBuffer.allocate(offset + MapChunkBuffer.SIZE_MIN)
        args << of(b, offset, 2, 4, 2, 0, 0, 0, 4, 4, 4, neighbors, chunks, '10, 1, 2, 3, 4, 5, 6, 11, 1, 2, 3, 4, 5, 6, 12, 1, 2, 3, 4, 5, 6, 13, 1, 2, 3, 4, 5, 6, 14, 1, 2, 3, 4, 5, 6, 15, 1, 2, 3, 4, 5, 6, 16, 1, 2, 3, 4, 5, 6, 17, 1, 2, 3, 4, 5, 6', '000200040002000000000000000400040004000100020003000400050006000700080009000a000b000c000d000e000f0010001100120013001400150016001700180019001a0008000a000100020003000400050006000b000100020003000400050006000c000100020003000400050006000d000100020003000400050006000e000100020003000400050006000f00010002000300040005000600100001000200030004000500060011000100020003000400050006')
        //
        offset = 3
        b = ByteBuffer.allocate(offset + MapChunkBuffer.SIZE_MIN)
        args << of(b, offset, 2, 4, 2, 0, 0, 0, 4, 4, 4, neighbors, chunks, '10, 1, 2, 3, 4, 5, 6, 11, 1, 2, 3, 4, 5, 6, 12, 1, 2, 3, 4, 5, 6, 13, 1, 2, 3, 4, 5, 6, 14, 1, 2, 3, 4, 5, 6, 15, 1, 2, 3, 4, 5, 6, 16, 1, 2, 3, 4, 5, 6, 17, 1, 2, 3, 4, 5, 6', '000000000200040002000000000000000400040004000100020003000400050006000700080009000a000b000c000d000e000f0010001100120013001400150016001700180019001a0008000a000100020003000400050006000b000100020003000400050006000c000100020003000400050006000d000100020003000400050006000e000100020003000400050006000f00010002000300040005000600100001000200030004000500060011000100020003000400050006')
        //
        Stream.of(args as Object[])
    }

    @ParameterizedTest
    @MethodSource()
    void set_get_map_chunk(ByteBuffer b, int offset, int cid, int parent, int csize, int sx, int sy, int sz, int ex, int ey, int ez, List neighbors, List chunks, def expectedChunks, def expected) {
        MapChunkBuffer.setCid(b, offset, cid)
        MapChunkBuffer.setParent(b, offset, parent)
        MapChunkBuffer.setChunkSize(b, offset, csize)
        MapChunkBuffer.setPos(b, offset, sx, sy, sz, ex, ey, ez)
        MapChunkBuffer.setNeighbors(b, offset, neighbors as int[])
        MapChunkBuffer.setChunksCount(b, offset, chunks.size())
        if (chunks.size() > 0) {
            MapChunkBuffer.setChunks(b, offset, chunks.size() / 7 as int, chunks as int[])
        }
        log.debug("set_get_map_chunk {}", HexFormat.of().formatHex(b.array()))
        assert HexFormat.of().formatHex(b.array()) == replace(expected, " ", "")
        assert MapChunkBuffer.getCid(b, offset) == cid
        assert MapChunkBuffer.getParent(b, offset) == parent
        assert MapChunkBuffer.getChunkSize(b, offset) == csize
        assert MapChunkBuffer.getSx(b, offset) == sx
        assert MapChunkBuffer.getSy(b, offset) == sy
        assert MapChunkBuffer.getSz(b, offset) == sz
        assert MapChunkBuffer.getEx(b, offset) == ex
        assert MapChunkBuffer.getEy(b, offset) == ey
        assert MapChunkBuffer.getEz(b, offset) == ez
        def n = new int[26]
        MapChunkBuffer.getNeighbors(b, offset, n)
        assert n as List == neighbors
        assert MapChunkBuffer.getChunksCount(b, offset) == chunks.size() / 7
        def thatChunks = MapChunkBuffer.getChunks(b, offset, null)
        assert "${thatChunks}" == "[${expectedChunks}]"
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
        args << of(b, offset, 2, 4, 2, 0, 0, 0, 2, 2, 2, neighbors, chunks, blocksBuffer, '', '000200040002000000000000000200020002000100020003000400050006000700080009000a000b000c000d000e000f0010001100120013001400150016001700180019001a000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000')
        //
        offset = 0
        chunks = []
        chunks.addAll([10, 1, 2, 3, 4, 5, 6])
        chunks.addAll([11, 1, 2, 3, 4, 5, 6])
        chunks.addAll([12, 1, 2, 3, 4, 5, 6])
        chunks.addAll([13, 1, 2, 3, 4, 5, 6])
        chunks.addAll([14, 1, 2, 3, 4, 5, 6])
        chunks.addAll([15, 1, 2, 3, 4, 5, 6])
        chunks.addAll([16, 1, 2, 3, 4, 5, 6])
        chunks.addAll([17, 1, 2, 3, 4, 5, 6])
        b = ByteBuffer.allocate(offset + MapChunkBuffer.SIZE_MIN + blocksBuffer.capacity())
        args << of(b, offset, 2, 4, 2, 0, 0, 0, 4, 4, 4, neighbors, chunks, blocksBuffer, '16, 1, 2, 3, 4, 5, 6, 17, 1, 2, 3, 4, 5, 6, 10, 1, 2, 3, 4, 5, 6, 11, 1, 2, 3, 4, 5, 6, 12, 1, 2, 3, 4, 5, 6, 13, 1, 2, 3, 4, 5, 6, 14, 1, 2, 3, 4, 5, 6, 15, 1, 2, 3, 4, 5, 6', '000200040002000000000000000400040004000100020003000400050006000700080009000a000b000c000d000e000f0010001100120013001400150016001700180019001a000800100001000200030004000500060011000100020003000400050006000a000100020003000400050006000b000100020003000400050006000c000100020003000400050006000d000100020003000400050006000e000100020003000400050006000f000100020003000400050006')
        //
        offset = 3
        b = ByteBuffer.allocate(offset + MapChunkBuffer.SIZE_MIN + blocksBuffer.capacity())
        args << of(b, offset, 2, 4, 2, 0, 0, 0, 4, 4, 4, neighbors, chunks, blocksBuffer, '16, 1, 2, 3, 4, 5, 6, 17, 1, 2, 3, 4, 5, 6, 10, 1, 2, 3, 4, 5, 6, 11, 1, 2, 3, 4, 5, 6, 12, 1, 2, 3, 4, 5, 6, 13, 1, 2, 3, 4, 5, 6, 14, 1, 2, 3, 4, 5, 6, 15, 1, 2, 3, 4, 5, 6', '000000000200040002000000000000000400040004000100020003000400050006000700080009000a000b000c000d000e000f0010001100120013001400150016001700180019001a000800100001000200030004000500060011000100020003000400050006000a000100020003000400050006000b000100020003000400050006000c000100020003000400050006000d000100020003000400050006000e000100020003000400050006000f000100020003000400050006')
        //
        offset = 3
        blocksBuffer = ByteBuffer.allocate(4 * MapBlockBuffer.SIZE)
        def blocksBufferI = blocksBuffer.asIntBuffer()
        (0..<blocksBufferI.capacity()).each {
            blocksBufferI.put(0xFF)
        }
        b = ByteBuffer.allocate(offset + MapChunkBuffer.SIZE_MIN + blocksBuffer.capacity())
        args << of(b, offset, 2, 4, 2, 0, 0, 0, 4, 4, 4, neighbors, chunks, blocksBuffer, '16, 1, 2, 3, 4, 5, 6, 17, 1, 2, 3, 4, 5, 6, 10, 1, 2, 3, 4, 5, 6, 11, 1, 2, 3, 4, 5, 6, 12, 1, 2, 3, 4, 5, 6, 13, 1, 2, 3, 4, 5, 6, 14, 1, 2, 3, 4, 5, 6, 15, 1, 2, 3, 4, 5, 6', '000000000200040002000000000000000400040004000100020003000400050006000700080009000a000b000c000d000e000f0010001100120013001400150016001700180019001a000800100001000200030004000500060011000100020003000400050006000a000100020003000400050006000b000100020003000400050006000c000100020003000400050006000d000100020003000400050006000e000100020003000400050006000f0001000200030004000500060000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000')
        //
        Stream.of(args as Object[])
    }

    @ParameterizedTest
    @MethodSource
    void write_read_map_chunk(ByteBuffer b, int offset, int cid, int parent, int csize, int sx, int sy, int sz, int ex, int ey, int ez, List neighbors, List chunks, def blocksBuffer, def expectedChunks, def expected) {
        def chunk = new MapChunk(cid, parent, csize, new GameChunkPos(sx, sy, sz, ex, ey, ez))
        chunk.setNeighbors(neighbors as int[])
        for (int i = 0; i < chunks.size() / 7; i++) {
            chunk.chunks.put(chunks[i * 7 + 0], new GameChunkPos(chunks[i * 7 + 1], chunks[i * 7 + 2], chunks[i * 7 + 3], chunks[i * 7 + 4], chunks[i * 7 + 5], chunks[i * 7 + 6]))
        }
        if (blocksBuffer.capacity() > 0) {
            chunk.setBlocksBuffer(blocksBuffer)
        }
        MapChunkBuffer.writeMapChunk(b, offset, chunk)
        log.debug("write_read_map_chunk {}", HexFormat.of().formatHex(b.array()))
        assert HexFormat.of().formatHex(b.array()) == replace(expected, " ", "")
        def n = new int[26]
        MapChunkBuffer.getNeighbors(b, offset, n)
        assert n as List == neighbors
        assert MapChunkBuffer.getChunksCount(b, offset) == chunk.leaf ? 0 : 8
        def thatChunks = MapChunkBuffer.getChunks(b, offset, null)
        assert "${thatChunks}" == "[${expectedChunks}]"
        if (chunk.blocksNotEmpty) {
            log.debug("write_read_map_chunk {}", HexFormat.of().formatHex(chunk.blocksBuffer.get()))
        }
    }

    @ParameterizedTest
    @CsvSource([
        "4,4,4,2,9,0,0,0,2,2,2,U,0,0,0,4,4,4",
        "4,4,4,2,9,0,0,0,2,2,2,D,0,0,2,2,2,4",
        "4,4,4,2,9,0,0,0,2,2,2,E,2,0,0,4,2,2",
        "4,4,4,2,9,0,0,0,2,2,2,W,0,0,0,4,4,4",
        "4,4,4,2,9,0,0,0,2,2,2,N,0,0,0,4,4,4",
        "4,4,4,2,9,0,0,0,2,2,2,S,0,2,0,2,4,2",
        //
        "8,8,8,2,73,0,0,0,2,2,2,U,0,0,0,8,8,8",
        "8,8,8,2,73,0,0,0,2,2,2,D,0,0,2,2,2,4",
        "8,8,8,2,73,0,0,0,2,2,2,E,2,0,0,4,2,2",
    ])
    void getNeighbor_chunk(int w, int h, int d, int c, int n, int sx, int sy, int sz, int ex, int ey, int ez, String dir, int expectedSx, int expectedSy, int expectedSz, int expectedEx, int expectedEy, int expectedEz) {
        def store = MapChunksStoreTest.createStore(tmp, "terrain_${w}_${h}_${d}_${c}_${n}", w, h, c, n)
        def chunk = store.findChunk(new GameChunkPos(sx, sy, sz, ex, ey, ez))
        int ncid = chunk.orElseThrow().getNeighbor(NeighboringDir.valueOf(dir))
        def nchunk = store.getChunk(ncid)
        assert nchunk.pos.x == expectedSx
        assert nchunk.pos.y == expectedSy
        assert nchunk.pos.z == expectedSz
        assert nchunk.pos.ep.x == expectedEx
        assert nchunk.pos.ep.y == expectedEy
        assert nchunk.pos.ep.z == expectedEz
    }
}
