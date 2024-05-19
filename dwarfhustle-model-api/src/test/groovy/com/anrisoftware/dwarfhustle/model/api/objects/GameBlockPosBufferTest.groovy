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
 * @see GameBlockPosBuffer
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
class GameBlockPosBufferTest {

    static Stream set_get_x_y_z() {
        def b = ByteBuffer.allocate(GameBlockPosBuffer.SIZE)
        Stream.of(
                of(b, 0, 1, 2, 3, "00010002 0003"),
                )
    }

    @ParameterizedTest
    @MethodSource()
    void set_get_x_y_z(ByteBuffer b, int offset, int x, int y, int z, def expected) {
        def bs = b.asShortBuffer()
        GameBlockPosBuffer.setX(bs, offset, x)
        GameBlockPosBuffer.setY(bs, offset, y)
        GameBlockPosBuffer.setZ(bs, offset, z)
        b.rewind()
        assert HexFormat.of().formatHex(b.array()) == replace(expected, " ", "")
        assert GameBlockPosBuffer.getX(bs, offset) == x
        assert GameBlockPosBuffer.getY(bs, offset) == y
        assert GameBlockPosBuffer.getZ(bs, offset) == z
    }
}
