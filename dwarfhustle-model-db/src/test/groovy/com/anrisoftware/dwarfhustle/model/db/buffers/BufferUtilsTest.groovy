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

import java.nio.ByteBuffer

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

import com.anrisoftware.dwarfhustle.model.db.buffers.BufferUtils

/**
 * @see BufferUtils
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
class BufferUtilsTest {

    @ParameterizedTest
    @CsvSource([
        "0,2,456",
        "1,3,456",
        "1,23,456"
    ])
    void read_short(int offset, int size, short d) {
        def b = ByteBuffer.wrap(new byte[offset + size])
        b.position(offset)
        b.asShortBuffer().put(d)
        b.position(offset)
        assert BufferUtils.readShort(b) == d
    }

    @ParameterizedTest
    @CsvSource([
        "0,4,456",
        "1,5,456",
        "1,23,456",
        "0,4,16909060",
        "0,4,1024"
    ])
    void read_int(int offset, int size, int d) {
        def b = ByteBuffer.wrap(new byte[offset + size])
        b.position(offset)
        b.asIntBuffer().put(d)
        b.position(offset)
        assert BufferUtils.readInt(b) == d
    }

    @ParameterizedTest
    @CsvSource([
        "0,8,456",
        "1,9,456",
        "1,23,456"
    ])
    void read_long(int offset, int size, long d) {
        def b = ByteBuffer.wrap(new byte[offset + size])
        b.position(offset)
        b.asLongBuffer().put(d)
        b.position(offset)
        assert BufferUtils.readLong(b) == d
    }
}
