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
import org.eclipse.collections.api.factory.primitive.LongSets
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

import com.anrisoftware.dwarfhustle.model.api.objects.TableObject

/**
 * @see TableObjectBuffer
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
class TableObjectBufferTest {

    static Stream set_get_tableobject() {
        def args = []
        def offset = 0
        def table = []
        def b = ByteBuffer.allocate(offset + TableObjectBuffer.calcSize(table.size()))
        args << of(b, offset, 100, 200, table, '6400 0000 0000 0000 c800 0000 0000 0000 0000 0000')
        //
        table = [10l, 20l, 30l]
        b = ByteBuffer.allocate(offset + TableObjectBuffer.calcSize(table.size()))
        args << of(b, offset, 100, 200, table, '6400 0000 0000 0000 c800 0000 0000 0000 0300 0000 0a00 0000 0000 0000 1400 0000 0000 0000 1e00 0000 0000 0000')
        //
        offset = 4
        b = ByteBuffer.allocate(offset + TableObjectBuffer.calcSize(table.size()))
        args << of(b, offset, 100, 200, table, '00000000 6400 0000 0000 0000 c800 0000 0000 0000 0300 0000 0a00 0000 0000 0000 1400 0000 0000 0000 1e00 0000 0000 0000')
        //
        Stream.of(args as Object[])
    }

    @ParameterizedTest
    @MethodSource()
    void set_get_tableobject(ByteBuffer b, int offset, long id, long parent, def table, def expected) {
        def mb = new UnsafeBuffer(b)
        def tableset = LongSets.mutable.ofAll(table)
        TableObjectBuffer.setId(mb, offset, id)
        TableObjectBuffer.setParent(mb, offset, parent)
        TableObjectBuffer.setTable(mb, offset, tableset)
        assert HexFormat.of().formatHex(b.array()) == replace(expected, " ", "")
        assert TableObjectBuffer.getId(mb, offset) == id
        assert TableObjectBuffer.getParent(mb, offset) == parent
        assert TableObjectBuffer.getTable(mb, offset) == tableset
    }

    @ParameterizedTest
    @MethodSource("set_get_tableobject")
    void write_read_tableobject(ByteBuffer b, int offset, long id, long parent, def table, def expected) {
        def tableset = LongSets.mutable.ofAll(table)
        def to = new TableObject(id, parent, tableset)
        TableObjectBuffer.writeTableObject(new UnsafeBuffer(b), offset, to)
        assert HexFormat.of().formatHex(b.array()) == replace(expected, " ", "")
        def thatTo = TableObjectBuffer.readTableObject(new UnsafeBuffer(b), offset, new TableObject())
        assert thatTo.id == id
        assert thatTo.parent == parent
        assert thatTo.table == to.table
    }
}
