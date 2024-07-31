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

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

/**
 * @see MapChunk
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
class MapChunkTest {

    static MapChunk createTestChunk(int cid = 0, parent = 0, int chunkSize = 2, def pos = new GameChunkPos(0, 0, 0, 4, 4, 4)) {
        def go = new MapChunk(cid, parent, chunkSize, pos)
        go.updateCenterExtent(4, 4)
        (0..7).each { go.chunks.put(888, new GameChunkPos(it, it, it, it + 1, it + 1, it + 1)) }
        return go
    }

    @ParameterizedTest
    @CsvSource([
        "0,0,0,0,0,0,0,0,0,false",
        "0,0,0,0,0,0,1,0,0,false",
        "0,0,0,2,2,2,0,0,0,true",
        "0,0,0,2,2,2,1,0,0,true",
        "0,0,0,2,2,2,2,0,0,false",
        //
        "2,2,2,4,4,4,0,2,2,false",
        "2,2,2,4,4,4,1,2,2,false",
        "2,2,2,4,4,4,2,2,2,true",
        "2,2,2,4,4,4,3,2,2,true",
        "2,2,2,4,4,4,4,2,2,false",
    ])
    void isInside_chunk(int sx, int sy, int sz, int ex, int ey, int ez, int px, int py, int pz, boolean expected) {
        def chunk = new MapChunk()
        chunk.pos = new GameChunkPos(sx, sy, sz, ex, ey, ez)
        assert chunk.isInside(new GameBlockPos(px, py, pz)) == expected
    }
}
