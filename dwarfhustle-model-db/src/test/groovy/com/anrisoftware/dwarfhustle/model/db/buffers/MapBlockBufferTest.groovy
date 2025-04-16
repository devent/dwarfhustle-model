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

import com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos
import com.anrisoftware.dwarfhustle.model.api.objects.MapBlock
import com.anrisoftware.dwarfhustle.model.api.objects.PropertiesSet

/**
 * @see MapBlockBuffer
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
class MapBlockBufferTest {

    static Stream set_get_map_block() {
        def args = []
        def offset = 0
        def b = ByteBuffer.allocate(offset + MapBlockBuffer.SIZE)
        args << of(b, offset, 0, 1, 2, 3, 4, 0b10000101, 6, 7, 4, 4, 4, 0, 0, 0, '8500 0000 0100 0200 0300 0400 0680 0780')
        //
        offset = 4
        b = ByteBuffer.allocate(offset + MapBlockBuffer.SIZE)
        args << of(b, offset, 0, 1, 2, 3, 4, 0b10000101, 6, 7, 4, 4, 4, 0, 0, 0, '00000000 8500 0000 0100 0200 0300 0400 0680 0780')
        //
        Stream.of(args as Object[])
    }

    @ParameterizedTest
    @MethodSource()
    void set_get_map_block(ByteBuffer b, int offset, int i, int parent, int t, int m, int o, int p, int temp, int lux, int w, int h, int d, int sx, int sy, int sz, def expected) {
        def mb = new UnsafeBuffer(b)
        MapBlockBuffer.setParent(mb, offset, parent)
        MapBlockBuffer.setType(mb, offset, t)
        MapBlockBuffer.setMaterial(mb, offset, m)
        MapBlockBuffer.setObject(mb, offset, o)
        MapBlockBuffer.setProp(mb, offset, p)
        MapBlockBuffer.setTemp(mb, offset, temp)
        MapBlockBuffer.setLux(mb, offset, lux)
        assert HexFormat.of().formatHex(b.array()) == replace(expected, " ", "")
        assert MapBlockBuffer.getParent(mb, offset) == parent
        assert MapBlockBuffer.getType(mb, offset) == t
        assert MapBlockBuffer.getMaterial(mb, offset) == m
        assert MapBlockBuffer.getObject(mb, offset) == o
        assert MapBlockBuffer.getProp(mb, offset) == p
        assert MapBlockBuffer.getTemp(mb, offset) == temp
        assert MapBlockBuffer.getLux(mb, offset) == lux
    }

    @ParameterizedTest
    @MethodSource("set_get_map_block")
    void write_read_map_block(ByteBuffer b, int offset, int i, int parent, int t, int m, int o, int p, int temp, int lux, int w, int h, int d, int sx, int sy, int sz, def expected) {
        def block = new MapBlock(parent, new GameBlockPos(GameBlockPos.calcX(i, w, sx), GameBlockPos.calcY(i, w, sy), GameBlockPos.calcZ(i, w, h, sz)))
        block.type = t
        block.material = m
        block.object = o
        block.p = new PropertiesSet(p)
        block.temp = temp
        block.lux = lux
        MapBlockBuffer.writeMapBlockIndex(new UnsafeBuffer(b), offset, block, i, w, h, d, sx, sy, sz)
        assert HexFormat.of().formatHex(b.array()) == replace(expected, " ", "")
        def thatBlock = MapBlockBuffer.readMapBlockIndex(new UnsafeBuffer(b), offset, i, w, h, d, sx, sy, sz)
        assert thatBlock.parent == parent
        assert thatBlock.pos == block.pos
        assert thatBlock.type == t
        assert thatBlock.material == m
        assert thatBlock.object == o
        assert thatBlock.p.bits == p
        assert thatBlock.temp == temp
        assert thatBlock.lux == lux
    }
}
