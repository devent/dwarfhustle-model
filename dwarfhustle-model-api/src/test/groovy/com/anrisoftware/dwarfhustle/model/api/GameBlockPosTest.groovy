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
}
