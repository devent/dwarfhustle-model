/*
 * dwarfhustle-model-db - Manages the compile dependencies for the model.
 * Copyright © 2022-2024 Erwin Müller (erwin.mueller@anrisoftware.com)
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

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource

import com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos
import com.anrisoftware.dwarfhustle.model.api.objects.MapBlock
import com.anrisoftware.dwarfhustle.model.api.objects.NeighboringDir
import com.anrisoftware.dwarfhustle.model.api.objects.PropertiesSet

/**
 * @see MapBlockBuffer
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
class MapBlockBufferTest {

    static Stream set_get_map_block() {
        def args = []
        def offset = 0
        def b = ByteBuffer.allocate(offset + MapBlockBuffer.SIZE)
        args << of(b, offset, 0, 1, 2, 3, 0b10000101, 5, 6, 4, 4, 4, 0, 0, 0, '00000085 00010002 00038005 8006')
        //
        offset = 4
        b = ByteBuffer.allocate(offset + MapBlockBuffer.SIZE)
        args << of(b, offset, 0, 1, 2, 3, 0b10000101, 5, 6, 4, 4, 4, 0, 0, 0, '00000000 00000085 00010002 00038005 8006')
        Stream.of(args as Object[])
    }

    @ParameterizedTest
    @MethodSource()
    void set_get_map_block(ByteBuffer b, int offset, int i, int parent, int m, int o, int p, int t, int l, int w, int h, int d, int sx, int sy, int sz, def expected) {
        MapBlockBuffer.setParent(b, offset, parent)
        MapBlockBuffer.setMaterial(b, offset, m)
        MapBlockBuffer.setObject(b, offset, o)
        MapBlockBuffer.setProp(b, offset, p)
        MapBlockBuffer.setTemp(b, offset, t)
        MapBlockBuffer.setLux(b, offset, l)
        assert HexFormat.of().formatHex(b.array()) == replace(expected, " ", "")
        assert MapBlockBuffer.getParent(b, offset) == parent
        assert MapBlockBuffer.getMaterial(b, offset) == m
        assert MapBlockBuffer.getObject(b, offset) == o
        assert MapBlockBuffer.getProp(b, offset) == p
        assert MapBlockBuffer.getTemp(b, offset) == t
        assert MapBlockBuffer.getLux(b, offset) == l
    }

    @ParameterizedTest
    @MethodSource("set_get_map_block")
    void write_read_map_block(ByteBuffer b, int offset, int i, int parent, int m, int o, int p, int t, int l, int w, int h, int d, int sx, int sy, int sz, def expected) {
        def block = new MapBlock(parent, new GameBlockPos(GameBlockPos.calcX(i, w, sx), GameBlockPos.calcY(i, w, sy), GameBlockPos.calcZ(i, w, h, sz)))
        block.material = m
        block.object = o
        block.p = new PropertiesSet(p)
        block.temp = t
        block.lux = l
        MapBlockBuffer.writeMapBlockIndex(b, offset, block, w, h, d, sx, sy, sz)
        assert HexFormat.of().formatHex(b.array()) == replace(expected, " ", "")
        def thatBlock = MapBlockBuffer.readMapBlockIndex(b, offset, 0, w, h, sx, sy, sz)
        assert thatBlock.parent == parent
        assert thatBlock.pos == block.pos
        assert thatBlock.material == m
        assert thatBlock.object == o
        assert thatBlock.p.bits == p
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
        def nblock = MapBlockBuffer.getNeighbor(chunkBlock.orElseThrow().two, NeighboringDir.valueOf(dir), chunkBlock.orElseThrow().one, { store.getChunk(it) })
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
                        def nblock = MapBlockBuffer.getNeighbor(chunkBlock.orElseThrow().two, it, chunkBlock.orElseThrow().one, retriever)
                    }
                }
            }
        }
    }
}
