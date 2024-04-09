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
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource

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
        args << of(b, offset, 111111, 22222, 33333, 0, 10, 4, 4, 4, 0, 0, 0, '00000000000056ce00000000000082350001b207000000000000000a')
        offset = 2 * 4
        b = ByteBuffer.allocate(offset + MapBlockBuffer.SIZE)
        args << of(b, offset, 111111, 22222, 33333, 0, 10, 4, 4, 4, 0, 0, 0, '000000000000000000000000000056ce00000000000082350001b207000000000000000a')
        Stream.of(args as Object[])
    }

    @ParameterizedTest
    @MethodSource()
    void set_get_map_block(ByteBuffer b, int offset, int parent, long m, long o, int i, int p, int w, int h, int d, int sx, int sy, int sz, def expected) {
        MapBlockBuffer.setIndex(b, offset, i)
        MapBlockBuffer.setProp(b, offset, p)
        MapBlockBuffer.setParent(b, offset, parent)
        MapBlockBuffer.setMaterial(b, offset, m)
        MapBlockBuffer.setObject(b, offset, o)
        assert HexFormat.of().formatHex(b.array()) == expected
        assert MapBlockBuffer.getIndex(b, offset) == i
        assert MapBlockBuffer.getProp(b, offset) == p
        assert MapBlockBuffer.getParent(b, offset) == parent
        assert MapBlockBuffer.getMaterial(b, offset) == m
        assert MapBlockBuffer.getObject(b, offset) == o
    }

    @ParameterizedTest
    @MethodSource("set_get_map_block")
    void write_read_map_block(ByteBuffer b, int offset, int parent, long m, long o, int i, int p, int w, int h, int d, int sx, int sy, int sz, def expected) {
        def block = new MapBlock(parent, new GameBlockPos(MapBlockBuffer.calcX(i, w, sx), MapBlockBuffer.calcY(i, w, sy), MapBlockBuffer.calcZ(i, w, h, sz)))
        block.material = m
        block.object = o
        block.p = new PropertiesSet(p)
        MapBlockBuffer.writeMapBlockIndex(b, offset, block, w, h, d, sx, sy, sz)
        assert HexFormat.of().formatHex(b.array()) == expected
        def thatBlock = MapBlockBuffer.readMapBlockIndex(b, offset, 0, w, h, sx, sy, sz)
        assert thatBlock.parent == parent
        assert thatBlock.pos == block.pos
        assert thatBlock.material == m
        assert thatBlock.object == o
        assert thatBlock.p.bits == p
    }

    @ParameterizedTest
    @CsvSource([
        "0,0,0,2,2,0,0,0,0",
        "1,0,0,2,2,0,0,0,1",
        "0,1,0,2,2,0,0,0,2",
        "1,1,0,2,2,0,0,0,3",
        "0,0,1,2,2,0,0,0,4",
        "1,0,1,2,2,0,0,0,5",
        "0,1,1,2,2,0,0,0,6",
        "1,1,1,2,2,0,0,0,7",
        //
        "0,0,2,2,2,0,0,2,0",
        "1,0,2,2,2,0,0,2,1",
        "0,1,2,2,2,0,0,2,2",
        "1,1,2,2,2,0,0,2,3",
        "0,0,3,2,2,0,0,2,4",
        "1,0,3,2,2,0,0,2,5",
        "0,1,3,2,2,0,0,2,6",
        "1,1,3,2,2,0,0,2,7",
        //
        "0,0,0,4,4,0,0,0,0",
        "1,0,0,4,4,0,0,0,1",
        "2,0,0,4,4,0,0,0,2",
        "0,1,1,4,4,0,0,0,20",
        "3,0,2,4,4,0,0,0,35",
        "3,3,2,4,4,0,0,0,47",
    ])
    void calc_x_y_z_from_index(int x, int y, int z, int w, int h, int sx, int sy, int sz, int i) {
        assert MapBlockBuffer.calcX(i, w, sx) == x
        assert MapBlockBuffer.calcY(i, w, sy) == y
        assert MapBlockBuffer.calcZ(i, w, h, sz) == z
    }

    @ParameterizedTest
    @CsvSource([
        "2,2,2,0,0,0,0,0,0,0",
        "2,2,2,0,0,0,1,0,0,1",
        "2,2,2,0,0,0,0,1,0,2",
        "2,2,2,0,0,0,1,1,0,3",
        "2,2,2,0,0,0,0,0,1,4",
        "2,2,2,0,0,0,1,0,1,5",
        "2,2,2,0,0,0,0,1,1,6",
        "2,2,2,0,0,0,1,1,1,7",
        //
        "2,2,2,2,2,2,2,2,2,0",
        "2,2,2,2,2,2,3,2,2,1",
        "2,2,2,2,2,2,2,3,2,2",
        "2,2,2,2,2,2,3,3,2,3",
        "2,2,2,2,2,2,2,2,3,4",
        "2,2,2,2,2,2,3,2,3,5",
        "2,2,2,2,2,2,2,3,3,6",
        "2,2,2,2,2,2,3,3,3,7",
    ])
    void calculate_index_from_pos(int w, int h, int d, int sx, int sy, int sz, int x, int y, int z, int expected) {
        assert MapBlockBuffer.calcIndex(w, h, d, sx, sy, sz, x, y, z) == expected
    }
}
