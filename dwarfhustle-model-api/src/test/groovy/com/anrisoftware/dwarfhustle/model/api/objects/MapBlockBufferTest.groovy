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
        def b = ByteBuffer.allocate(MapBlockBuffer.SIZE)
        def offset = 0
        args << of(b, offset, 111111, 22222, 33333, 32, 10, '00000000000056ce00000000000082350001b207000000200000000a')
        b = ByteBuffer.allocate(10 + MapBlockBuffer.SIZE)
        offset = 10
        args << of(b, offset, 111111, 22222, 33333, 32, 10, '0000000000000000000000000000000056ce00000000000082350001b207000000200000000a')
        Stream.of(args as Object[])
    }

    @ParameterizedTest
    @MethodSource()
    void set_get_map_block(ByteBuffer b, int offset, int parent, long m, long o, int i, int p, def expected) {
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
    @CsvSource([
        "0,0,0,4,4,0",
        "1,0,0,4,4,1",
        "0,1,1,4,4,20",
        "3,0,2,4,4,35",
        "3,3,2,4,4,47",
    ])
    void calc_x_y_z_from_index(int x, int y, int z, int w, int h, int i) {
        assert MapBlockBuffer.calcX(i, w) == x
        assert MapBlockBuffer.calcY(i, w) == y
        assert MapBlockBuffer.calcZ(i, w, h) == z
    }
}
