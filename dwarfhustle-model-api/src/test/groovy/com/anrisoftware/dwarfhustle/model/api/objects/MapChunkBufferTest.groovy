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
        def offset = 0
        def chunks = []
        def b = ByteBuffer.allocate(offset + MapChunkBuffer.SIZE_MIN)
        args << of(b, offset, 22222, 11111, 1, 2, 3, 4, 5, 6, 4, chunks, '000056ce00002b670000000400000001000000020000000300000004000000050000000600000000')
        offset = 0
        chunks = []
        chunks.addAll([33333, 1, 2, 3, 4, 5, 6])
        chunks.addAll([44444, 1, 2, 3, 4, 5, 6])
        b = ByteBuffer.allocate(offset + MapChunkBuffer.SIZE_MIN + (chunks.size() / 7 as int) * CidGameChunkPosMapBuffer.SIZE_ENTRY)
        args << of(b, offset, 22222, 11111, 1, 2, 3, 4, 5, 6, 4, chunks, '000056ce00002b670000000400000001000000020000000300000004000000050000000600000002000082350000000100000002000000030000000400000005000000060000ad9c000000010000000200000003000000040000000500000006')
        offset = 10
        chunks = []
        chunks.addAll([33333, 1, 2, 3, 4, 5, 6])
        chunks.addAll([44444, 1, 2, 3, 4, 5, 6])
        b = ByteBuffer.allocate(offset + MapChunkBuffer.SIZE_MIN + (chunks.size() / 7 as int) * CidGameChunkPosMapBuffer.SIZE_ENTRY)
        args << of(b, offset, 22222, 11111, 1, 2, 3, 4, 5, 6, 4, chunks, '00000000000000000000000056ce00002b670000000400000001000000020000000300000004000000050000000600000002000082350000000100000002000000030000000400000005000000060000ad9c000000010000000200000003000000040000000500000006')
        Stream.of(args as Object[])
    }

    @ParameterizedTest
    @MethodSource()
    void set_get_map_chunk(ByteBuffer b, int offset, int cid, int parent, int sx, int sy, int sz, int ex, int ey, int ez, int csize, List chunks, def expected) {
        MapChunkBuffer.setCid(b, offset, cid)
        MapChunkBuffer.setParent(b, offset, parent)
        MapChunkBuffer.setPos(b, offset, sx, sy, sz, ex, ey, ez)
        MapChunkBuffer.setChunkSize(b, offset, csize)
        MapChunkBuffer.setChunksCount(b, offset, chunks.size())
        if (chunks.size() > 0) {
            MapChunkBuffer.setChunks(b, offset, chunks.size() / 7 as int, chunks as int[])
        }
        assert HexFormat.of().formatHex(b.array()) == expected
        assert MapChunkBuffer.getCid(b, offset) == cid
        assert MapChunkBuffer.getParent(b, offset) == parent
        assert MapChunkBuffer.getSx(b, offset) == sx
        assert MapChunkBuffer.getSy(b, offset) == sy
        assert MapChunkBuffer.getSz(b, offset) == sz
        assert MapChunkBuffer.getEx(b, offset) == ex
        assert MapChunkBuffer.getEy(b, offset) == ey
        assert MapChunkBuffer.getEz(b, offset) == ez
        assert MapChunkBuffer.getChunkSize(b, offset) == csize
        assert MapChunkBuffer.getChunksCount(b, offset) == chunks.size() / 7
        assert MapChunkBuffer.getChunks(b, offset, null) == chunks
    }

    static Stream write_read_map_chunk() {
        def args = []
        def offset = 0
        def chunks = []
        def blocksBuffer = ByteBuffer.allocate(0 * MapBlockBuffer.SIZE)
        def b = ByteBuffer.allocate(offset + MapChunkBuffer.SIZE_MIN + blocksBuffer.capacity())
        args << of(b, offset, 22222, 11111, 1, 2, 3, 4, 5, 6, 4, chunks, blocksBuffer, '000056ce00002b670000000400000001000000020000000300000004000000050000000600000000')
        offset = 0
        chunks = []
        chunks.addAll([33333, 1, 2, 3, 4, 5, 6])
        chunks.addAll([44444, 1, 2, 3, 4, 5, 6])
        b = ByteBuffer.allocate(offset + MapChunkBuffer.SIZE_MIN + (chunks.size() / 7 as int) * CidGameChunkPosMapBuffer.SIZE_ENTRY + blocksBuffer.capacity())
        args << of(b, offset, 22222, 11111, 1, 2, 3, 4, 5, 6, 4, chunks, blocksBuffer, '000056ce00002b6700000004000000010000000200000003000000040000000500000006000000020000ad9c00000001000000020000000300000004000000050000000600008235000000010000000200000003000000040000000500000006')
        offset = 4*2
        chunks = []
        chunks.addAll([33333, 1, 2, 3, 4, 5, 6])
        chunks.addAll([44444, 1, 2, 3, 4, 5, 6])
        b = ByteBuffer.allocate(offset + MapChunkBuffer.SIZE_MIN + (chunks.size() / 7 as int) * CidGameChunkPosMapBuffer.SIZE_ENTRY + blocksBuffer.capacity())
        args << of(b, offset, 22222, 11111, 1, 2, 3, 4, 5, 6, 4, chunks, blocksBuffer, '0000000000000000000056ce00002b6700000004000000010000000200000003000000040000000500000006000000020000ad9c00000001000000020000000300000004000000050000000600008235000000010000000200000003000000040000000500000006')
        offset = 4*2
        chunks = []
        chunks.addAll([33333, 1, 2, 3, 4, 5, 6])
        chunks.addAll([44444, 1, 2, 3, 4, 5, 6])
        blocksBuffer = ByteBuffer.allocate(1 * MapBlockBuffer.SIZE)
        def blocksBufferI = blocksBuffer.asIntBuffer()
        (0..<blocksBufferI.capacity()).each {
            blocksBufferI.put(0xFF)
        }
        b = ByteBuffer.allocate(offset + MapChunkBuffer.SIZE_MIN + (chunks.size() / 7 as int) * CidGameChunkPosMapBuffer.SIZE_ENTRY + blocksBuffer.capacity())
        args << of(b, offset, 22222, 11111, 1, 2, 3, 4, 5, 6, 4, chunks, blocksBuffer, '0000000000000000000056ce00002b6700000004000000010000000200000003000000040000000500000006000000020000ad9c00000001000000020000000300000004000000050000000600008235000000010000000200000003000000040000000500000006000000ff000000ff000000ff000000ff000000ff000000ff000000ff')
        Stream.of(args as Object[])
    }

    @ParameterizedTest
    @MethodSource
    void write_read_map_chunk(ByteBuffer b, int offset, int cid, int parent, int sx, int sy, int sz, int ex, int ey, int ez, int csize, List chunks, def blocksBuffer, def expected) {
        def chunk = new MapChunk(cid, parent, csize, new GameChunkPos(sx, sy, sz, ex, ey, ez))
        chunk.setBlocksBuffer(blocksBuffer)
        for (int i = 0; i < chunks.size() / 7; i++) {
            chunk.chunks.put(chunks[i * 7 + 0], new GameChunkPos(chunks[i * 7 + 1], chunks[i * 7 + 2], chunks[i * 7 + 3], chunks[i * 7 + 4], chunks[i * 7 + 5], chunks[i * 7 + 6]))
        }
        MapChunkBuffer.writeMapChunk(b, offset, chunk)
        log.debug(HexFormat.of().formatHex(b.array()))
        assert HexFormat.of().formatHex(b.array()) == expected
    }
}
