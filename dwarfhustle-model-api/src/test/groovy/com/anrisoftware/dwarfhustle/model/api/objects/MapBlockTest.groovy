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

    static MapBlock createTestBlock(int parent, def pos = new GameBlockPos(10, 10, 10)) {
        def go = new MapBlock(parent, pos)
        go.pos = new GameBlockPos(10, 10, 10)
        go.updateCenterExtent(4, 4, 4)
        go.parent = 7777777
        go.material = 8888888
        go.setNaturalFloor(true)
        go.setNaturalRoof(true)
        return go
    }

    @Test
    void map_tile_byte_size_objectstream() {
        def stream = new ByteArrayOutputStream()
        def ostream = new ObjectOutputStream(stream)
        def go = createTestBlock()
        ostream.writeObject(go)
        assert stream.size() == 458
    }

    @Test
    void map_tile_byte_size_datastream() {
        def stream = new ByteArrayOutputStream()
        def ostream = new DataOutputStream(stream)
        def go = createTestBlock()
        go.writeStream(ostream)
        assert stream.size() == 376
    }
}
