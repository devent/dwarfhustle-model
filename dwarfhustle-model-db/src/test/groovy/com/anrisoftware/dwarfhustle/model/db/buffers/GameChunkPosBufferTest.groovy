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
import java.util.stream.Stream

import org.agrona.concurrent.UnsafeBuffer
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

import com.anrisoftware.dwarfhustle.model.api.objects.GameChunkPos

/**
 * @see GameChunkPosBuffer
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
class GameChunkPosBufferTest {

    static Stream set_get_x_y_z() {
        def args = []
        int offset = 0
        def b = ByteBuffer.allocate(offset + GameChunkPosBuffer.SIZE)
        args << of(b, offset, 1, 2, 3, 4, 5, 6, "01000200 03000400 05000600")
        offset = 3
        b = ByteBuffer.allocate(offset + GameChunkPosBuffer.SIZE)
        args << of(b, offset, 1, 2, 3, 4, 5, 6, "000000 01000200 03000400 05000600")
        Stream.of(args as Object[])
    }

    @ParameterizedTest
    @MethodSource()
    void set_get_x_y_z(ByteBuffer b, int offset, int sx, int sy, int sz, int ex, int ey, int ez, def expected) {
        GameChunkPosBuffer.write(new UnsafeBuffer(b), offset, new GameChunkPos(sx, sy, sz, ex, ey, ez))
        assert HexFormat.of().formatHex(b.array()) == replace(expected, " ", "")
        def thatPos = GameChunkPosBuffer.read(new UnsafeBuffer(b), offset)
        assert thatPos.x == sx
        assert thatPos.y == sy
        assert thatPos.z == sz
        assert thatPos.ep.x == ex
        assert thatPos.ep.y == ey
        assert thatPos.ep.z == ez
    }
}
