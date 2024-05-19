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
import java.nio.ShortBuffer
import java.util.stream.Stream

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
        def b = ByteBuffer.allocate(offset + CidGameChunkPosMapBuffer.SIZE_MIN + CidGameChunkPosMapBuffer.SIZE_ENTRY * entries.size())
        def sb = b.asShortBuffer()
        args << of(b, sb, offset, entries.size(), entries, '0000')
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
        b = ByteBuffer.allocate(offset + CidGameChunkPosMapBuffer.SIZE_MIN + CidGameChunkPosMapBuffer.SIZE_ENTRY * entries.size())
        sb = b.asShortBuffer()
        args << of(b, sb, offset, entries.size(), entries, '00020001 00010002 00030004 00050006 00020007 00080009 000a000b 000c')
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
        b = ByteBuffer.allocate(offset * 2 + CidGameChunkPosMapBuffer.SIZE_MIN + CidGameChunkPosMapBuffer.SIZE_ENTRY * entries.size())
        sb = b.asShortBuffer()
        args << of(b, sb, offset, entries.size(), entries, '00000000 00000002 00010001000200030004000500060002000700080009000a000b000c')
        //
        Stream.of(args as Object[])
    }

    @ParameterizedTest
    @MethodSource()
    void set_get_x_y_z(ByteBuffer b, ShortBuffer sb, int offset, int count, List entry, def expected) {
        CidGameChunkPosMapBuffer.setCount(sb, offset, count)
        entry.eachWithIndex { it, i ->
            CidGameChunkPosMapBuffer.setEntry(sb, offset as int, i, it as int[])
        }
        sb.rewind()
        assert HexFormat.of().formatHex(b.array()) == replace(expected, " ", "")
        assert CidGameChunkPosMapBuffer.getCount(sb, offset) == count
        entry.eachWithIndex { it, i ->
            assert CidGameChunkPosMapBuffer.getCid(sb, offset, i) == it[0]
            assert CidGameChunkPosMapBuffer.getSx(sb, offset, i) == it[1]
            assert CidGameChunkPosMapBuffer.getSy(sb, offset, i) == it[2]
            assert CidGameChunkPosMapBuffer.getSz(sb, offset, i) == it[3]
            assert CidGameChunkPosMapBuffer.getEx(sb, offset, i) == it[4]
            assert CidGameChunkPosMapBuffer.getEy(sb, offset, i) == it[5]
            assert CidGameChunkPosMapBuffer.getEz(sb, offset, i) == it[6]
        }
        int[] dest = CidGameChunkPosMapBuffer.getEntries(sb, offset, null)
        entry.eachWithIndex { it, i ->
            assert dest[i * 7 + 0] == it[0]
            assert dest[i * 7 + 1] == it[1]
            assert dest[i * 7 + 2] == it[2]
            assert dest[i * 7 + 3] == it[3]
            assert dest[i * 7 + 4] == it[4]
            assert dest[i * 7 + 5] == it[5]
            assert dest[i * 7 + 6] == it[6]
        }
    }
}
