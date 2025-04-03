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

import static java.nio.ByteBuffer.allocate
import static org.apache.commons.lang3.StringUtils.replace
import static org.junit.jupiter.params.provider.Arguments.of

import java.time.ZoneOffset
import java.util.stream.Stream

import org.agrona.concurrent.UnsafeBuffer
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

import com.anrisoftware.dwarfhustle.model.api.objects.GameMap
import com.anrisoftware.dwarfhustle.model.api.objects.MapArea
import com.anrisoftware.dwarfhustle.model.api.objects.MapCoordinate

/**
 * @see GameMapBuffer
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
class GameMapBufferTest {

    static Stream set_get_gamemap_properties() {
        int offset = 0
        def b = new UnsafeBuffer(allocate(offset + GameMapBuffer.SIZE));
        def a = [:]
        a.id = 200
        a.world = 100
        a.w = 512
        a.h = 512
        a.d = 512
        a.cs = 16
        a.cc = 10825
        a.nwlat = 50.99819f
        a.nwlon = 10.98348f
        a.selat = 50.96610f
        a.selon = 11.05610f
        a.cpos = [10f, 11f, 12f]
        a.crot = [13f, 14f, 15f, 16f]
        a.cx = 1
        a.cy = 2
        a.cz = 3
        a.s = [17f, 18f, 19f]
        a.climate = 500
        a.time = 3600
        a.name = 55
        a.co = 140914002486087
        Stream.of(
                of(b, offset, a, "c800 0000 0000 0000 6400 0000 0000 0000 47e3 0f19 2980 0000 0002 0002 0002 1000 492a 26fe 4b42 56bc 2f41 49dd 4b42 c9e5 3041 0000 2041 0000 3041 0000 4041 0000 5041 0000 6041 0000 7041 0000 8041 0100 0200 0300 0000 8841 0000 9041 0000 9841 f401 100e 0000 3700 0000 0000 0000"),
                )
    }

    @ParameterizedTest
    @MethodSource("set_get_gamemap_properties")
    void set_get_gamemap(UnsafeBuffer b, int offset, Map a, def expected) {
        def gm = new GameMap(a.id, a.w, a.h, a.d)
        gm.world = a.world
        gm.chunkSize = a.cs
        gm.chunksCount = a.cc
        gm.area = new MapArea(new MapCoordinate(a.nwlat, a.nwlon), new MapCoordinate(a.selat, a.selon))
        gm.cameraPos = a.cpos
        gm.cameraRot = a.crot
        gm.cursor.x = a.cx
        gm.cursor.y = a.cy
        gm.cursor.z = a.cz
        gm.sunPos = a.s
        gm.climateZone = a.climate
        gm.timeZone = ZoneOffset.ofTotalSeconds(a.time)
        gm.name = a.name
        gm.cursorObject = a.co
        GameMapBuffer.setGameMap(b, offset, gm)
        assert BufferUtils.toHex(b) == replace(expected, " ", "")
        def thatWm = GameMapBuffer.getGameMap(b, offset, new GameMap())
        assert gm == thatWm
    }
}
