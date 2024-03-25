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
 * @see GameBlockPosBuffer
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
class GameBlockPosBufferTest {

    static Stream set_get_x_y_z() {
        def b = ByteBuffer.allocate(GameBlockPosBuffer.SIZE)
        Stream.of(
                of(b, 1, 2, 3, [
                    0,
                    0,
                    0,
                    1,
                    0,
                    0,
                    0,
                    2,
                    0,
                    0,
                    0,
                    3
                ]),
                )
    }

    @ParameterizedTest
    @MethodSource()
    void set_get_x_y_z(ByteBuffer b, int x, int y, int z, def expected) {
        GameBlockPosBuffer.setX(b, 0, x)
        GameBlockPosBuffer.setY(b, 0, y)
        GameBlockPosBuffer.setZ(b, 0, z)
        b.rewind()
        assert b.array() == expected
        assert GameBlockPosBuffer.getX(b, 0) == x
        assert GameBlockPosBuffer.getY(b, 0) == y
        assert GameBlockPosBuffer.getZ(b, 0) == z
    }
}
