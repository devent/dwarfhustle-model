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
package com.anrisoftware.dwarfhustle.model.api

import static org.junit.jupiter.params.provider.Arguments.of

import java.util.stream.Stream

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

/**
 * @see GameBlockPos
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
class GameBlockPosTest {

	static two_block_pos_equals() {
		Stream.of(
				of(new GameBlockPos(0, 0, 0, 0, 0, 0, 0), new GameBlockPos(0, 0, 0, 0, 0, 0, 0), true),
				of(new GameBlockPos(0, 0, 0, 0, 4, 4, 4), new GameBlockPos(0, 0, 0, 0, 4, 4, 4), true),
				of(new GameBlockPos(1, 0, 0, 0, 4, 4, 4), new GameBlockPos(0, 0, 0, 0, 4, 4, 4), false),
				)
	}

	@ParameterizedTest
	@MethodSource
	void two_block_pos_equals(def a, def b, def expected) {
		if (expected) {
			assert a == b
		} else {
			assert a != b
		}
	}

	static block_pos_toSaveString() {
		Stream.of(
				of(new GameBlockPos(0, 0, 0, 0, 0, 0, 0), "0/0/0/0/0/0/0"),
				of(new GameBlockPos(0, 0, 0, 0, 4, 4, 4), "0/0/0/0/4/4/4"),
				of(new GameBlockPos(1, 0, 0, 0, 4, 4, 4), "1/0/0/0/4/4/4"),
				)
	}

	@ParameterizedTest
	@MethodSource
	void block_pos_toSaveString(GameBlockPos a, def expected) {
		assert a.toSaveString() == expected
	}

	@ParameterizedTest
	@MethodSource("block_pos_toSaveString")
	void block_pos_parse(GameBlockPos expected, def s) {
		assert GameBlockPos.parse(s) == expected
	}
}
