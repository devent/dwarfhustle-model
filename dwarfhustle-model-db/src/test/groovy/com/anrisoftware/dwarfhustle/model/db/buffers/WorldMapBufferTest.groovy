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

import java.time.LocalDateTime
import java.util.stream.Stream

import org.agrona.concurrent.UnsafeBuffer
import org.eclipse.collections.api.factory.primitive.LongSets
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

import com.anrisoftware.dwarfhustle.model.api.objects.WorldMap

/**
 * @see WorldMapBuffer
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
class WorldMapBufferTest {

    static Stream set_get_worldmap_properties() {
        int offset = 0
        def maps = LongSets.mutable.of(5000100)
        def b = new UnsafeBuffer(allocate(offset + WorldMapBuffer.MIN_SIZE + maps.size() * 8));
        Stream.of(
                of(b, offset, 100, 55, 1.0, 2.0, LocalDateTime.of(3033, 5, 21, 13, 30, 5), 5000100, maps, "6400 0000 0000 0000 3700 0000 0000 0000 0000 803f 0000 0040 5d4b 2ad0 0700 0000 a44b 4c00 0000 0000 0100 0000 a44b 4c00 0000 0000"),
                )
    }

    @ParameterizedTest
    @MethodSource("set_get_worldmap_properties")
    void set_get_worldmap(UnsafeBuffer b, int offset, long id, long name, def dlat, def dlon, def time, long currentMap, def maps, def expected) {
        def wm = new WorldMap(id)
        wm.maps = maps
        wm.name = name
        wm.distanceLat = dlat
        wm.distanceLon = dlon
        wm.time = time
        wm.currentMap = currentMap
        WorldMapBuffer.setWorldMap(b, offset, wm)
        assert BufferUtils.toHex(b) == replace(expected, " ", "")
        def thatWm = WorldMapBuffer.getWorldMap(b, offset, new WorldMap())
        assert wm == thatWm
        assert wm.maps == thatWm.maps
        assert wm.name == thatWm.name
        assert wm.distanceLat == thatWm.distanceLat
        assert wm.distanceLon == thatWm.distanceLon
        assert wm.time == thatWm.time
        assert wm.currentMap == thatWm.currentMap
    }
}
