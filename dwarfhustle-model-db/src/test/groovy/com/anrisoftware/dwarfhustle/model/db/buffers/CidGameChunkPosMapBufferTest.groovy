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

import static org.apache.commons.lang3.StringUtils.replace
import static org.junit.jupiter.params.provider.Arguments.of

import java.nio.ByteBuffer
import java.util.stream.Stream

import org.agrona.concurrent.UnsafeBuffer
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

/**
 * @see CidGameChunkPosMapBuffer
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
class CidGameChunkPosMapBufferTest {

    static Stream set_get_x_y_z() {
        def args = []
        def entries = []
        def offset = 0
        def count = 1
        def b = ByteBuffer.allocate(offset + CidGameChunkPosMapBuffer.SIZE * count)
        args << of(b, offset, entries.size(), entries, '00000000 00000000 00000000 0000')
        //
        entries = [
            [
                1,
                1,
                2,
                3,
                4,
                5,
                6
            ],
            [
                2,
                7,
                8,
                9,
                10,
                11,
                12
            ]
        ]
        count = entries.size()
        b = ByteBuffer.allocate(offset + CidGameChunkPosMapBuffer.SIZE * count)
        args << of(b, offset, entries.size(), entries, '01000100 02000300 04000500 06000200 07000800 09000a00 0b000c00')
        //
        entries = [
            [
                1,
                1,
                2,
                3,
                4,
                5,
                6
            ],
            [
                2,
                7,
                8,
                9,
                10,
                11,
                12
            ]
        ]
        offset = 3
        count = entries.size()
        b = ByteBuffer.allocate(offset + CidGameChunkPosMapBuffer.SIZE * count)
        args << of(b, offset, entries.size(), entries, '00000001 00010002 00030004 00050006 00020007 00080009 000a000b 000c00')
        //
        Stream.of(args as Object[])
    }

    @ParameterizedTest
    @MethodSource()
    void set_get_x_y_z(ByteBuffer b, int offset, int count, List entries, def expected) {
        CidGameChunkPosMapBuffer.write(new UnsafeBuffer(b), offset, count, entries.flatten() as int[])
        b.rewind()
        assert HexFormat.of().formatHex(b.array()) == replace(expected, " ", "")
        def thatEntries = CidGameChunkPosMapBuffer.read(new UnsafeBuffer(b), offset, count, null)
        entries.eachWithIndex { it, i ->
            assert thatEntries[i * 7 + 0] == it[0]
            assert thatEntries[i * 7 + 1] == it[1]
            assert thatEntries[i * 7 + 2] == it[2]
            assert thatEntries[i * 7 + 3] == it[3]
            assert thatEntries[i * 7 + 4] == it[4]
            assert thatEntries[i * 7 + 5] == it[5]
            assert thatEntries[i * 7 + 6] == it[6]
        }
    }
}
