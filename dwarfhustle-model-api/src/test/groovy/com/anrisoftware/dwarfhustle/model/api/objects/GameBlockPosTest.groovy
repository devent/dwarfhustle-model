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
    ])
    void calculate_index_from_pos(int w, int h, int d, int sx, int sy, int sz, int x, int y, int z, int expected) {
        assert GameBlockPos.calcIndex(w, h, d, sx, sy, sz, x, y, z) == expected
    }
}
