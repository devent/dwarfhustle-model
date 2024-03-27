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

/**
 * @see MapChunksIndexBuffer
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
class MapChunksIndexBufferTest {

    static Stream set_get_entries() {
        def args = []
        def count = 0
        def offset = 0
        def b = ByteBuffer.allocate(MapChunksIndexBuffer.SIZE_MIN + MapChunksIndexBuffer.SIZE_ENTRY * count)
        args << of(b, offset, count, [], '00000000')
        count = 2
        b = ByteBuffer.allocate(MapChunksIndexBuffer.SIZE_MIN + MapChunksIndexBuffer.SIZE_ENTRY * count)
        args << of(b, offset, count, [
            [
                1111111,
                0,
                128
            ],
            [
                2222222,
                128,
                128
            ]
        ], '000000020010f44700000000000000800021e88e0000008000000080')
        b = ByteBuffer.allocate(10 + MapChunksIndexBuffer.SIZE_MIN + MapChunksIndexBuffer.SIZE_ENTRY * count)
        offset = 10
        args << of(b, offset, count, [
            [
                1111111,
                0,
                128
            ],
            [
                2222222,
                128,
                128
            ]
        ], '00000000000000000000000000020010f44700000000000000800021e88e0000008000000080')
        Stream.of(args as Object[])
    }

    @ParameterizedTest
    @MethodSource()
    void set_get_entries(ByteBuffer b, int offset, int count, List entry, def expected) {
        MapChunksIndexBuffer.setCount(b, offset, count)
        entry.eachWithIndex { it, i ->
            MapChunksIndexBuffer.setEntry(b, offset, i, it as int[])
        }
        b.rewind()
        assert HexFormat.of().formatHex(b.array()) == expected
        assert MapChunksIndexBuffer.getCount(b, offset) == count
        entry.eachWithIndex { it, i ->
            assert MapChunksIndexBuffer.getCid(b, offset, i) == it[0]
            assert MapChunksIndexBuffer.getPos(b, offset, i) == it[1]
            assert MapChunksIndexBuffer.getSize(b, offset, i) == it[2]
        }
        int[] dest = MapChunksIndexBuffer.getEntries(b, offset, null)
        entry.eachWithIndex { it, i ->
            assert dest[i * 3 + 0] == it[0]
            assert dest[i * 3 + 1] == it[1]
            assert dest[i * 3 + 2] == it[2]
        }
    }
}
