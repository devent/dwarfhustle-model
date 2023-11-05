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

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

/**
 * @see GameChunkPos
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
class GameChunkPosTest {

    static two_chunk_pos_equals() {
        Stream.of(
                of(new GameChunkPos(0, 0, 0, 0, 0, 0, 0), new GameChunkPos(0, 0, 0, 0, 0, 0, 0), true),
                of(new GameChunkPos(0, 0, 0, 0, 4, 4, 4), new GameChunkPos(0, 0, 0, 0, 4, 4, 4), true),
                of(new GameChunkPos(1, 0, 0, 0, 4, 4, 4), new GameChunkPos(0, 0, 0, 0, 4, 4, 4), false),
                )
    }

    @ParameterizedTest
    @MethodSource
    void two_chunk_pos_equals(def a, def b, def expected) {
        if (expected) {
            assert a == b
        } else {
            assert a != b
        }
    }

    static chunk_pos_toSaveString() {
        Stream.of(
                of(new GameChunkPos(0, 0, 0, 0, 0, 0, 0), "0/0/0/0/0/0/0"),
                of(new GameChunkPos(0, 0, 0, 0, 4, 4, 4), "0/0/0/0/4/4/4"),
                of(new GameChunkPos(1, 0, 0, 0, 4, 4, 4), "1/0/0/0/4/4/4"),
                )
    }

    @ParameterizedTest
    @MethodSource
    void chunk_pos_toSaveString(GameChunkPos a, def expected) {
        assert a.toSaveString() == expected
    }

    @ParameterizedTest
    @MethodSource("chunk_pos_toSaveString")
    void chunk_pos_parse(GameChunkPos expected, def s) {
        assert GameChunkPos.parse(s) == expected
    }

    static chunk_size() {
        Stream.of(
                of(new GameChunkPos(0, 0, 0, 0, 0, 0, 0), 0, 0, 0),
                of(new GameChunkPos(0, 0, 0, 0, 64, 64, 64), 64, 64, 64),
                of(new GameChunkPos(0, 0, 0, 0, 32, 32, 32), 32, 32, 32),
                of(new GameChunkPos(0, 32, 0, 0, 64, 32, 32), 32, 32, 32),
                )
    }

    @ParameterizedTest
    @MethodSource
    void chunk_size(GameChunkPos p, float exsizex, float exsizey, float esizez) {
        assert p.sizeX == exsizex
        assert p.sizeY == exsizey
        assert p.sizeZ == esizez
    }
}
