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
                of(b, 0, 1, 2, 3, 4, 5, 6, '00010002 00030004 00050006'),
                )
    }

    @ParameterizedTest
    @MethodSource()
    void set_get_x_y_z(ByteBuffer b, int offset, int sx, int sy, int sz, int ex, int ey, int ez, def expected) {
        def bs = b.asShortBuffer()
        GameChunkPosBuffer.setX(bs, offset, sx)
        GameChunkPosBuffer.setY(bs, offset, sy)
        GameChunkPosBuffer.setZ(bs, offset, sz)
        GameChunkPosBuffer.setEx(bs, offset, ex)
        GameChunkPosBuffer.setEy(bs, offset, ey)
        GameChunkPosBuffer.setEz(bs, offset, ez)
        assert HexFormat.of().formatHex(b.array()) == replace(expected, " ", "")
        assert GameChunkPosBuffer.getX(bs, offset) == sx
        assert GameChunkPosBuffer.getY(bs, offset) == sy
        assert GameChunkPosBuffer.getZ(bs, offset) == sz
        assert GameChunkPosBuffer.getEx(bs, offset) == ex
        assert GameChunkPosBuffer.getEy(bs, offset) == ey
        assert GameChunkPosBuffer.getEz(bs, offset) == ez
    }

    @ParameterizedTest
    @MethodSource("set_get_x_y_z")
    void write_read_x_y_z(ByteBuffer b, int offset, int sx, int sy, int sz, int ex, int ey, int ez, def expected) {
        def bs = b.asShortBuffer()
        GameChunkPosBuffer.writeGameChunkPos(bs, offset, new GameChunkPos(sx, sy, sz, ex, ey, ez))
        assert HexFormat.of().formatHex(b.array()) == replace(expected, " ", "")
        def thatPos = GameChunkPosBuffer.readGameChunkPos(bs, offset)
        assert thatPos.x == sx
        assert thatPos.y == sy
        assert thatPos.z == sz
        assert thatPos.ep.x == ex
        assert thatPos.ep.y == ey
        assert thatPos.ep.z == ez
    }
}
