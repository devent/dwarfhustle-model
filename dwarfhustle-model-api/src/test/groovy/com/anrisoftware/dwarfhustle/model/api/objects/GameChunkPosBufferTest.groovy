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
 * @see GameChunkPosBuffer
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
class GameChunkPosBufferTest {

    static Stream set_get_x_y_z() {
        def b = ByteBuffer.allocate(GameChunkPosBuffer.SIZE)
        Stream.of(
                of(b, 1, 2, 3, 4, 5, 6, '000000010000000200000003000000040000000500000006'),
                )
    }

    @ParameterizedTest
    @MethodSource()
    void set_get_x_y_z(ByteBuffer b, int sx, int sy, int sz, int ex, int ey, int ez, def expected) {
        GameChunkPosBuffer.setX(b, 0, sx)
        GameChunkPosBuffer.setY(b, 0, sy)
        GameChunkPosBuffer.setZ(b, 0, sz)
        GameChunkPosBuffer.setEx(b, 0, ex)
        GameChunkPosBuffer.setEy(b, 0, ey)
        GameChunkPosBuffer.setEz(b, 0, ez)
        assert HexFormat.of().formatHex(b.array()) == expected
        assert GameChunkPosBuffer.getX(b, 0) == sx
        assert GameChunkPosBuffer.getY(b, 0) == sy
        assert GameChunkPosBuffer.getZ(b, 0) == sz
        assert GameChunkPosBuffer.getEx(b, 0) == ex
        assert GameChunkPosBuffer.getEy(b, 0) == ey
        assert GameChunkPosBuffer.getEz(b, 0) == ez
    }

    @ParameterizedTest
    @MethodSource("set_get_x_y_z")
    void write_read_x_y_z(ByteBuffer b, int sx, int sy, int sz, int ex, int ey, int ez, def expected) {
        GameChunkPosBuffer.writeGameChunkPos(b, 0, new GameChunkPos(sx, sy, sz, ex, ey, ez))
        assert HexFormat.of().formatHex(b.array()) == expected
        def thatPos = GameChunkPosBuffer.readGameChunkPos(b, 0)
        assert thatPos.x == sx
        assert thatPos.y == sy
        assert thatPos.z == sz
        assert thatPos.ep.x == ex
        assert thatPos.ep.y == ey
        assert thatPos.ep.z == ez
    }
}
