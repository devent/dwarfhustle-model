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

import org.junit.jupiter.api.Test

/**
 * @see MapBlock
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
class MapBlockTest {

    static MapBlock createTestBlock() {
        def go = new MapBlock()
        go.id = 23423234
        go.map = 6666666
        go.pos = new GameBlockPos(10, 10, 10)
        go.updateCenterExtent(4, 4, 4)
        go.chunk = 7777777
        go.material = 8888888
        go.setNaturalFloor(true)
        go.setNaturalRoof(true)
        NeighboringDir.values().each { go.setNeighbor(it, 11111111) }
        return go
    }

    @Test
    void map_tile_type() {
        def go = new MapBlock()
        assert go.objectType == "MapBlock"
    }

    @Test
    void map_tile_byte_size() {
        def stream = new ByteArrayOutputStream()
        def ostream = new ObjectOutputStream(stream)
        ostream.writeObject(go)
        assert stream.size() == 1340
    }
}
