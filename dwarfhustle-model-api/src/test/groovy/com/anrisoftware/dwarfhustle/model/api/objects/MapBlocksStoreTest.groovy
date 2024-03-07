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

    static Stream put_and_get_map_block() {
        def args = []
        List positionsList = new BlocksPosList().run()
        positionsList.each { List positions ->
            int chunkSize = Math.cbrt(positions.size() / 3) as int
            MapBlocksStore store = new MapBlocksStore(chunkSize)
            for (int i = 0; i < positions.size(); i += 3) {
                args.add(of(store, positions[i + 0], positions[i + 1], positions[i + 2]))
            }
        }
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
    void for_each_blocks() {
        def positionsList = new BlocksPosList().run()
        positionsList.each { List positions ->
            def store = new MapBlocksStore(Math.cbrt(positions.size() / 3) as int)
            def mb = MapBlockTest.createTestBlock()
            for (int i = 0; i < positions.size(); i += 3) {
                mb.pos = new GameBlockPos(positions[i + 0], positions[i + 1], positions[i + 2])
                store.setBlock(mb)
            }
            def list = []
            store.forEachValue({ list.add(it) })
            assert list.size() == positions.size() / 3
            list.eachWithIndex { MapBlock block, int i ->
                def blockpos = [
                    block.pos.x,
                    block.pos.y,
                    block.pos.z
                ]
                assert positions.containsAll(blockpos)
            }
        }
    }

    @Test
    void put_and_get_map_block_benchmark() {
        int chunkSize = 4
        def store = new MapBlocksStore(chunkSize)
        def mb = MapBlockTest.createTestBlock()
        int blocksCount = 100
        for (int i = 0; i < blocksCount; i++) {
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
