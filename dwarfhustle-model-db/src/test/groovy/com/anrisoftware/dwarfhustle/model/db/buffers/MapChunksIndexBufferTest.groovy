/*
 * dwarfhustle-model-db - Manages the compile dependencies for the model.
 * Copyright © 2022-2025 Erwin Müller (erwin.mueller@anrisoftware.com)
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

import static org.junit.jupiter.params.provider.Arguments.of

import java.nio.ByteBuffer
import java.util.stream.Stream

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

import com.anrisoftware.dwarfhustle.model.db.buffers.MapChunksIndexBuffer

import groovy.util.logging.Slf4j

/**
 * @see MapChunksIndexBuffer
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
class MapChunksIndexBufferTest {

    static Stream set_get_entries() {
        def args = []
        def count = 0
        def offset = 0
        def b = ByteBuffer.allocate(offset + MapChunksIndexBuffer.SIZE_MIN + MapChunksIndexBuffer.SIZE_ENTRY * count)
        args << of(b, offset, count, [], '00000000')
        count = 2
        b = ByteBuffer.allocate(MapChunksIndexBuffer.SIZE_MIN + MapChunksIndexBuffer.SIZE_ENTRY * count)
        args << of(b, offset, count, [
            [
                0,
                64
            ],
            [
                64,
                128
            ]
        ], '0000000200000000000000400000004000000080')
        offset = 2 * 4
        b = ByteBuffer.allocate(offset + MapChunksIndexBuffer.SIZE_MIN + MapChunksIndexBuffer.SIZE_ENTRY * count)
        args << of(b, offset, count, [
            [
                0,
                64
            ],
            [
                64,
                128
            ]
        ], '00000000000000000000000200000000000000400000004000000080')
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
        log.debug("set_get_entries {}", HexFormat.of().formatHex(b.array()))
        assert HexFormat.of().formatHex(b.array()) == expected
        assert MapChunksIndexBuffer.getCount(b, offset) == count
        entry.eachWithIndex { it, i ->
            assert MapChunksIndexBuffer.getPos(b, offset, i) == it[0]
            assert MapChunksIndexBuffer.getSize(b, offset, i) == it[1]
        }
        int[] dest = MapChunksIndexBuffer.getEntries(b, offset, null)
        entry.eachWithIndex { it, i ->
            assert dest[i * 2 + 0] == it[0]
            assert dest[i * 2 + 1] == it[1]
        }
    }
}
