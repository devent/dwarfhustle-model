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

import com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos
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
		def name = "Fantastic Map"
		def b = new UnsafeBuffer(allocate(offset + GameMapBuffer.MIN_SIZE + name.length() * 2));
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
		a.time = 3600
		a.name = name
		Stream.of(
				of(b, offset, a, "c800000000000000 6400000000000000 0002 0002 0002 1000 492a 26fe4b4256bc2f4149dd4b42c9e53041 000020410000304100004041 00005041000060410000704100008041 0100 0200 0300 00008841 00009041 00009841 100e0000 0d00000046616e746173746963204d61700000000000000000000000000000000000000000000000"),
				)
	}

	@ParameterizedTest
	@MethodSource()
	void set_get_gamemap_properties(UnsafeBuffer b, int offset, Map a, def expected) {
		GameMapBuffer.setId(b, offset, a.id)
		GameMapBuffer.setWorld(b, offset, a.world)
		GameMapBuffer.setWidth(b, offset, a.w)
		GameMapBuffer.setHeight(b, offset, a.h)
		GameMapBuffer.setDepth(b, offset, a.d)
		GameMapBuffer.setChunkSize(b, offset, a.cs)
		GameMapBuffer.setChunksCount(b, offset, a.cc)
		GameMapBuffer.setArea(b, offset, a.nwlat, a.nwlon, a.selat, a.selon)
		GameMapBuffer.setCameraPos(b, offset, a.cpos as float[])
		GameMapBuffer.setCameraRot(b, offset, a.crot as float[])
		GameMapBuffer.setCursor(b, offset, a.cx, a.cy, a.cz)
		GameMapBuffer.setSunPos(b, offset, a.s as float[])
		GameMapBuffer.setTimeZone(b, offset, a.time)
		GameMapBuffer.setName(b, offset, a.name)
		assert BufferUtils.toHex(b) == replace(expected, " ", "")
		assert GameMapBuffer.getId(b, offset) == a.id
		assert GameMapBuffer.getWorld(b, offset) == a.world
		assert GameMapBuffer.getWidth(b, offset) == a.w
		assert GameMapBuffer.getHeight(b, offset) == a.h
		assert GameMapBuffer.getDepth(b, offset) == a.d
		assert GameMapBuffer.getChunkSize(b, offset) == a.cs
		assert GameMapBuffer.getChunksCount(b, offset) == a.cc
		def area = GameMapBuffer.getArea(b, offset, new MapArea())
		assert area.nw.lat == a.nwlat
		assert area.nw.lon == a.nwlon
		assert area.se.lat == a.selat
		assert area.se.lon == a.selon
		assert GameMapBuffer.getCameraPos(b, offset, new float[3]) == a.cpos as float[]
		assert GameMapBuffer.getCameraRot(b, offset, new float[4]) == a.crot as float[]
		def cursor = GameMapBuffer.getCursor(b, offset, new GameBlockPos())
		assert cursor.x == a.cx
		assert cursor.y == a.cy
		assert cursor.z == a.cz
		assert GameMapBuffer.getSunPos(b, offset, a.s as float[]) == a.s as float[]
		assert GameMapBuffer.getTimeZone(b, offset).totalSeconds == a.time
		assert GameMapBuffer.getName(b, offset) == a.name
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
		gm.timeZone = ZoneOffset.ofTotalSeconds(a.time)
		gm.name = a.name
		GameMapBuffer.setGameMap(b, offset, gm)
		assert BufferUtils.toHex(b) == replace(expected, " ", "")
		def thatWm = GameMapBuffer.getGameMap(b, offset, new GameMap())
		assert gm == thatWm
	}
}
