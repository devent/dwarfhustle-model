/*
 * dwarfhustle-model-api - Manages the compile dependencies for the model.
 * Copyright © 2022-2024 Erwin Müller (erwin.mueller@anrisoftware.com)
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

/**
 * @see MapBlock
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
class MapBlockTest {

    static MapBlock createTestBlock(int parent, def pos = new GameBlockPos(10, 10, 10)) {
        def go = new MapBlock(parent, pos)
        go.pos = new GameBlockPos(10, 10, 10)
        go.parent = 7777777
        go.material = 8888888
        go.setFloor(true)
        go.setRoof(true)
        return go
    }
}
