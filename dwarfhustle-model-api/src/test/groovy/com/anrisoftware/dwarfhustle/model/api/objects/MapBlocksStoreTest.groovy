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
 * @see MapBlocksStore
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
class MapBlocksStoreTest {

    @Test
    void calculate_index_from_pos() {
        int w = 4
        int h = 4
        assert MapBlocksStore.calcIndex(w, h, 0, 0, 0) == 0
        assert MapBlocksStore.calcIndex(w, h, 1, 0, 0) == 1
        assert MapBlocksStore.calcIndex(w, h, 0, 1, 0) == 4
        assert MapBlocksStore.calcIndex(w, h, 1, 1, 0) == 5
        assert MapBlocksStore.calcIndex(w, h, 0, 0, 1) == 16
        assert MapBlocksStore.calcIndex(w, h, 1, 0, 1) == 17
        assert MapBlocksStore.calcIndex(w, h, 10, 0, 0) == 10
        assert MapBlocksStore.calcIndex(w, h, 0, 10, 0) == 40
        assert MapBlocksStore.calcIndex(w, h, 0, 0, 10) == 160
    }

    @Test
    void convert_int_bytes_and_revert() {
        assert MapBlocksStore.int2Bytes(new byte[4], 0) == [0, 0, 0, 0]
        assert MapBlocksStore.int2Bytes(new byte[4], 10) == [0, 0, 0, 10]
        assert MapBlocksStore.int2Bytes(new byte[4], 1024) == [0, 0, 4, 0]
        assert MapBlocksStore.int2Bytes(new byte[4], 2048) == [0, 0, 8, 0]
        assert MapBlocksStore.int2Bytes(new byte[4], 1340) == [0, 0, 5, 60]
        assert MapBlocksStore.bytes2Int([0, 0, 0, 0] as byte[]) == 0
        assert MapBlocksStore.bytes2Int([0, 0, 0, 10] as byte[]) == 10
        assert MapBlocksStore.bytes2Int([0, 0, 4, 0] as byte[]) == 1024
        assert MapBlocksStore.bytes2Int([0, 0, 8, 0] as byte[]) == 2048
        assert MapBlocksStore.bytes2Int([0, 0, 5, 60] as byte[]) == 1340
    }

    @Test
    void put_and_get_map_block() {
        int w = 4
        int h = 4
        def store = new MapBlocksStore(2)
        def mb = MapBlockTest.createTestBlock()
        mb.pos = new GameBlockPos(0, 0, 0)
        store.setBlock(w, h, mb)
        def mbret = store.getBlock(w, h, mb.pos)
    }
}
