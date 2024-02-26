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

import static org.junit.jupiter.params.provider.Arguments.of

import java.util.stream.Stream

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

/**
 * @see MapBlocksStore
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
class MapBlocksStoreTest {

    static Stream calculate_index_from_pos() {
        int w = 2
        int h = 2
        int chunkSize = 2
        Stream.of(
                of(w, h, 0, 0, 0, 0),
                of(w, h, 1, 0, 0, 1),
                of(w, h, 0, 1, 0, 2),
                of(w, h, 1, 1, 0, 3),
                of(w, h, 0, 0, 1, 4),
                of(w, h, 1, 0, 1, 5),
                of(w, h, 0, 1, 1, 6),
                of(w, h, 1, 1, 1, 7),
                //
                of(w, h, 0, 4, 0, 8),
                of(w, h, 1, 4, 0, 9),
                of(w, h, 0, 5, 0, 10),
                of(w, h, 1, 5, 0, 11),
                of(w, h, 0, 4, 1, 12),
                of(w, h, 1, 4, 1, 13),
                of(w, h, 0, 5, 1, 14),
                of(w, h, 1, 5, 1, 15),
                //
                )
    }

    @ParameterizedTest
    @MethodSource
    void calculate_index_from_pos(int w, int h, int x, int y, int z, int expected) {
        assert MapBlocksStore.calcIndex(w, h, x, y, z) == expected
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

    static Stream put_and_get_map_block() {
        int chunkSize = 2
        MapBlocksStore store
        def args = []
        store = new MapBlocksStore(chunkSize)
        args.addAll([
            of(store, 0, 0, 1),
            of(store, 0, 0, 0),
            of(store, 1, 1, 0),
            of(store, 1, 0, 0),
            of(store, 1, 1, 1),
            of(store, 0, 1, 1),
            of(store, 0, 1, 0),
            of(store, 1, 0, 1),
            //
        ])
        chunkSize = 2
        store = new MapBlocksStore(chunkSize)
        args.addAll([
            of(store, 1, 5, 1),
            of(store, 1, 5, 0),
            of(store, 1, 4, 1),
            of(store, 0, 5, 1),
            of(store, 0, 5, 0),
            of(store, 0, 4, 0),
            of(store, 0, 4, 1),
            of(store, 1, 4, 0),
            //
        ])
        chunkSize = 4
        store = new MapBlocksStore(chunkSize)
        args.addAll([
            of(store, 14, 12, 29),
            of(store, 15, 14, 29),
            of(store, 14, 13, 31),
            of(store, 14, 12, 28),
            of(store, 14, 13, 30),
            of(store, 15, 15, 29),
            of(store, 15, 15, 28),
            of(store, 15, 14, 28),
            of(store, 15, 15, 31),
            of(store, 15, 15, 30),
            of(store, 15, 14, 31),
            of(store, 14, 13, 29),
            of(store, 14, 12, 30),
            of(store, 15, 14, 30),
            of(store, 14, 13, 28),
            of(store, 15, 12, 29),
            of(store, 15, 12, 28),
            of(store, 15, 13, 28),
            of(store, 15, 13, 29),
            of(store, 14, 12, 31),
            of(store, 13, 14, 30),
            of(store, 13, 14, 31),
            of(store, 15, 13, 30),
            of(store, 13, 14, 28),
            of(store, 15, 12, 31),
            of(store, 15, 12, 30),
            of(store, 13, 14, 29),
            of(store, 12, 14, 31),
            of(store, 12, 12, 31),
            of(store, 12, 12, 30),
            of(store, 12, 14, 30),
            of(store, 15, 13, 31),
            of(store, 14, 15, 28),
            of(store, 14, 15, 29),
            of(store, 14, 15, 31),
            of(store, 14, 15, 30),
            of(store, 12, 12, 28),
            of(store, 12, 14, 29),
            of(store, 12, 14, 28),
            of(store, 12, 12, 29),
            of(store, 13, 12, 28),
            of(store, 14, 14, 29),
            of(store, 12, 15, 31),
            of(store, 13, 15, 30),
            of(store, 14, 14, 28),
            of(store, 12, 15, 30),
            of(store, 12, 13, 30),
            of(store, 12, 13, 31),
            of(store, 13, 13, 30),
            of(store, 13, 15, 31),
            of(store, 13, 13, 31),
            of(store, 13, 12, 29),
            of(store, 14, 14, 30),
            of(store, 13, 12, 30),
            of(store, 14, 14, 31),
            of(store, 13, 12, 31),
            of(store, 12, 15, 28),
            of(store, 12, 13, 28),
            of(store, 12, 15, 29),
            of(store, 13, 15, 28),
            of(store, 13, 15, 29),
            of(store, 13, 13, 29),
            of(store, 13, 13, 28),
            of(store, 12, 13, 29),
            //
        ])
        Stream.of(args as Arguments[])
    }

    @ParameterizedTest
    @MethodSource
    void put_and_get_map_block(MapBlocksStore store, int x, int y, int z) {
        def mb = MapBlockTest.createTestBlock()
        mb.pos = new GameBlockPos(x, y, z)
        store.setBlock(mb)
        def mbret = store.getBlock(mb.pos)
        assert mb == mbret
        assert mb.pos.x == x
        assert mb.pos.y == y
        assert mb.pos.z == z
    }

    @Test
    void put_and_get_map_block_benchmark() {
        int chunkSize = 4
        def store = new MapBlocksStore(chunkSize)
        def mb = MapBlockTest.createTestBlock()
        for (int i = 0; i < 100; i++) {
            for (int x = 0; x < 32; x++) {
                for (int y = 0; y < 32; y++) {
                    for (int z = 0; z < 32; z++) {
                        mb.pos = new GameBlockPos(x, y, z)
                        store.setBlock(mb)
                        def mbret = store.getBlock(mb.pos)
                        assert mb.pos.x == x
                        assert mb.pos.y == y
                        assert mb.pos.z == z
                    }
                }
            }
        }
    }
}
