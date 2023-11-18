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
 * @see GameBlockPos
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
class KnowledgeObjectTest {

    static Stream test_rid2Id() {
        Stream.of(
                of(0x00, 0x01),
                of(0x10, 0x1000000001),
                of(0xffff, 0xFFFF00000001),
                of(0xffff, 0xFFFF00000001),
                )
    }

    @ParameterizedTest
    @MethodSource
    void test_rid2Id(long rid, def expected) {
        assert KnowledgeObject.kid2Id(rid) == expected
    }

    @ParameterizedTest
    @MethodSource("test_rid2Id")
    void test_id2Rid(long expected, def id) {
        assert KnowledgeObject.id2Kid(id) == expected
    }
}
