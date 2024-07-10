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

import static java.nio.ByteBuffer.allocate
import static org.apache.commons.lang3.StringUtils.replace
import static org.junit.jupiter.params.provider.Arguments.of

import java.time.LocalDateTime
import java.util.stream.Stream

import org.agrona.concurrent.UnsafeBuffer
import org.eclipse.collections.api.factory.primitive.LongSets
import org.eclipse.collections.api.set.primitive.LongSet
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

/**
 * @see WorldMapBuffer
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
class WorldMapBufferTest {

    static Stream set_get_worldmap_properties() {
        int offset = 0
        def name = "Endless World"
        def b = new UnsafeBuffer(allocate(offset + WorldMapBuffer.MIN_SIZE + name.length() * 2));
        Stream.of(
                of(b, offset, 100, name, 1.0, 1.0, 3033, 5, 21, 13, 30, 5, 5000100, LongSets.mutable.of(5000100), "6400000000000000 0000803f 0000803f d90b 0500 1500 0d00 1e00 0500 a44b4c0000000000 01000000 a44b4c0000000000 0d000000 456e646c65737320576f726c640000000000"),
                )
    }

    @ParameterizedTest
    @MethodSource()
    void set_get_worldmap_properties(UnsafeBuffer b, int offset, long id, def name, def dlat, def dlon, int year, int month, int dayOfMonth, int hour, int minute, int second, long currentMap, LongSet maps, def expected) {
        WorldMapBuffer.setId(b, offset, id)
        WorldMapBuffer.setName(b, offset, name, maps.size())
        WorldMapBuffer.setDistanceLat(b, offset, dlat)
        WorldMapBuffer.setDistanceLon(b, offset, dlon)
        WorldMapBuffer.setLocalDateTime(b, offset, year, month, dayOfMonth, hour, minute, second)
        WorldMapBuffer.setCurrentMap(b, offset, currentMap)
        WorldMapBuffer.setMaps(b, offset, maps)
        assert BufferUtils.toHex(b) == replace(expected, " ", "")
        assert WorldMapBuffer.getId(b, offset) == id
        assert WorldMapBuffer.getName(b, offset, maps.size()) == name
        assert WorldMapBuffer.getDistanceLat(b, offset) == dlat
        assert WorldMapBuffer.getDistanceLon(b, offset) == dlon
        assert WorldMapBuffer.getLocalDateTimeYear(b, offset) == year
        assert WorldMapBuffer.getLocalDateTimeMonth(b, offset) == month
        assert WorldMapBuffer.getLocalDateTimeDay(b, offset) == dayOfMonth
        assert WorldMapBuffer.getLocalDateTimeHour(b, offset) == hour
        assert WorldMapBuffer.getLocalDateTimeMinute(b, offset) == minute
        assert WorldMapBuffer.getLocalDateTimeSecond(b, offset) == second
        assert WorldMapBuffer.getCurrentMap(b, offset) == currentMap
        assert WorldMapBuffer.getMaps(b, offset, null) == maps
    }

    @ParameterizedTest
    @MethodSource("set_get_worldmap_properties")
    void set_get_worldmap(UnsafeBuffer b, int offset, long id, def name, def dlat, def dlon, int year, int month, int dayOfMonth, int hour, int minute, int second, long currentMap, LongSet maps, def expected) {
        def wm = new WorldMap(id)
        wm.maps = maps
        wm.name = name
        wm.distanceLat = dlat
        wm.distanceLon = dlon
        wm.time = LocalDateTime.of(year, month, dayOfMonth, hour, minute, second)
        wm.currentMap = currentMap
        WorldMapBuffer.setWorldMap(b, offset, wm)
        assert BufferUtils.toHex(b) == replace(expected, " ", "")
        def thatWm = WorldMapBuffer.getWorldMap(b, offset, new WorldMap())
        assert wm == thatWm
    }
}
