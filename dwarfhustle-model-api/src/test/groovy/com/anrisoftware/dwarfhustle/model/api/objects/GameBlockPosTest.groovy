/*
 * dwarfhustle-model-api - Manages the compile dependencies for the model.
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
package com.anrisoftware.dwarfhustle.model.api.objects

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

/**
 * @see GameBlockPos
 */
class GameBlockPosTest {

    @ParameterizedTest
    @CsvSource([
        "0,0,0,2,2,0,0,0,0",
        "1,0,0,2,2,0,0,0,1",
        "0,1,0,2,2,0,0,0,2",
        "1,1,0,2,2,0,0,0,3",
        "0,0,1,2,2,0,0,0,4",
        "1,0,1,2,2,0,0,0,5",
        "0,1,1,2,2,0,0,0,6",
        "1,1,1,2,2,0,0,0,7",
        //
        "0,0,2,2,2,0,0,2,0",
        "1,0,2,2,2,0,0,2,1",
        "0,1,2,2,2,0,0,2,2",
        "1,1,2,2,2,0,0,2,3",
        "0,0,3,2,2,0,0,2,4",
        "1,0,3,2,2,0,0,2,5",
        "0,1,3,2,2,0,0,2,6",
        "1,1,3,2,2,0,0,2,7",
        //
        "0,0,0,4,4,0,0,0,0",
        "1,0,0,4,4,0,0,0,1",
        "2,0,0,4,4,0,0,0,2",
        "0,1,1,4,4,0,0,0,20",
        "3,0,2,4,4,0,0,0,35",
        "3,3,2,4,4,0,0,0,47",
    ])
    void calc_x_y_z_from_index(int x, int y, int z, int w, int h, int sx, int sy, int sz, int i) {
        assert GameBlockPos.calcX(i, w, sx) == x
        assert GameBlockPos.calcY(i, w, sy) == y
        assert GameBlockPos.calcZ(i, w, h, sz) == z
    }

    @ParameterizedTest
    @CsvSource([
        "2,2,2,0,0,0,0,0,0,0",
        "2,2,2,0,0,0,1,0,0,1",
        "2,2,2,0,0,0,0,1,0,2",
        "2,2,2,0,0,0,1,1,0,3",
        "2,2,2,0,0,0,0,0,1,4",
        "2,2,2,0,0,0,1,0,1,5",
        "2,2,2,0,0,0,0,1,1,6",
        "2,2,2,0,0,0,1,1,1,7",
        //
        "2,2,2,2,2,2,2,2,2,0",
        "2,2,2,2,2,2,3,2,2,1",
        "2,2,2,2,2,2,2,3,2,2",
        "2,2,2,2,2,2,3,3,2,3",
        "2,2,2,2,2,2,2,2,3,4",
        "2,2,2,2,2,2,3,2,3,5",
        "2,2,2,2,2,2,2,3,3,6",
        "2,2,2,2,2,2,3,3,3,7",
        //
        "16,16,16,0,0,0,0,0,0,0",
        "16,16,16,0,0,0,1,0,0,1",
        "16,16,16,0,0,0,0,0,1,256",
    ])
    void calculate_index_from_pos(int w, int h, int d, int sx, int sy, int sz, int x, int y, int z, int expected) {
        assert GameBlockPos.calcIndex(w, h, d, sx, sy, sz, x, y, z) == expected
    }
}
