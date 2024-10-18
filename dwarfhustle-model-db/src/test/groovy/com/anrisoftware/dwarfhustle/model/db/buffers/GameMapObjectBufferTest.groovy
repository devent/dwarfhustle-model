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

import static org.apache.commons.lang3.StringUtils.replace
import static org.junit.jupiter.params.provider.Arguments.of

import java.nio.ByteBuffer
import java.util.stream.Stream

import org.agrona.concurrent.UnsafeBuffer
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

import com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos
import com.anrisoftware.dwarfhustle.model.api.objects.GameMapObject

/**
 * @see GameChunkPosBuffer
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
class GameMapObjectBufferTest {

    static Stream write_read_gamemapobject() {
        def args = []
        int offset = 0
        def b = ByteBuffer.allocate(offset + GameMapObjectBuffer.SIZE)
        args << of(b, offset, 1, 2, 3, 10, 20, 30, "01000000 00000000 02000000 00000000 03000000 00000000 0a001400 1e00")
        offset = 3
        b = ByteBuffer.allocate(offset + GameMapObjectBuffer.SIZE)
        args << of(b, offset, 1, 2, 3, 10, 20, 30, "000000 01000000 00000000 02000000 00000000 03000000 00000000 0a001400 1e00")
        Stream.of(args as Object[])
    }

    @ParameterizedTest
    @MethodSource()
    void write_read_gamemapobject(ByteBuffer b, int offset, long id, long kid, long map, int x, int y, int z, def expected) {
        def o = new GameMapObject(id, new GameBlockPos(x, y, z)) {

                    @Override
                    public int getObjectType() {
                        return 0;
                    }
                }
        o.kid = kid
        o.map = map
        GameMapObjectBuffer.writeObject(new UnsafeBuffer(b), offset, o)
        assert HexFormat.of().formatHex(b.array()) == replace(expected, " ", "")
        def thato = new GameMapObject() {

                    @Override
                    public int getObjectType() {
                        return 0;
                    }
                }
        GameMapObjectBuffer.readObject(new UnsafeBuffer(b), offset, thato)
        assert thato.id == id
        assert thato.kid == kid
        assert thato.map == map
        assert thato.pos.x == x
        assert thato.pos.y == y
        assert thato.pos.z == z
    }
}
