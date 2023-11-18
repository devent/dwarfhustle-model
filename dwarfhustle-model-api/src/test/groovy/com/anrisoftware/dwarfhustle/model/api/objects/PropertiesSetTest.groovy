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

import groovy.util.logging.Slf4j

/**
 * @see PropertiesSet
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
class PropertiesSetTest {

    static Stream set_pos_compare() {
        Stream.of(
                of(0, (int)0x00000001), //
                of(1, (int)0x00000002), //
                of(2, (int)0x00000004), //
                )
    }

    @ParameterizedTest
    @MethodSource
    void set_pos_compare(int pos, int expected) {
        def s = new PropertiesSet().set(pos)
        log.info "set_pos_compare: pos {} expected {} set {}", pos, expected, s
        assert s == expected
    }

    static Stream sets_compare() {
        Stream.of(
                of(0x00000001, 0x00000001), //
                of(0x00000002, 0x00000002), //
                of(0x00000004, 0x00000004), //
                of(0x00000006, 0x00000006), //
                of((int)0x80808001, (int)0x80808001), //
                )
    }

    @ParameterizedTest
    @MethodSource
    void sets_compare(int setbits, int expected) {
        def s = new PropertiesSet().sets(setbits)
        log.info "sets_compare: setbits {} expected {} set {}", setbits, expected, s
        assert s == expected
    }

    static Stream override_sets_compare() {
        Stream.of(
                of(0x00000001, 0x00000001, 0x00000001), //
                of(0x00000001, 0x00000002, 0x00000003), //
                )
    }

    @ParameterizedTest
    @MethodSource
    void override_sets_compare(int initial, int setbits, int expected) {
        def s = new PropertiesSet(initial).sets(setbits)
        log.info "override_sets_compare: initial {} setbits {} expected {} set {}", initial, setbits, expected, s
        assert s == expected
    }

    static Stream initial_clear_pos_compare() {
        Stream.of(
                of(0x0000F00F, 0, 0x0000F00E), //
                )
    }

    @ParameterizedTest
    @MethodSource
    void initial_clear_pos_compare(int initial, int pos, int expected) {
        def s = new PropertiesSet(initial).clear(pos)
        log.info "initial_clear_pos_compare: pos {} expected {} set {}", pos, expected, s
        assert s == expected
    }

    static Stream contains_compare() {
        Stream.of(
                of(0x00000001, 0x00000001, true), //
                of(0x0000000F, 0x00000002, true), //
                of(0x0000000B, 0x00000004, false), //
                )
    }

    @ParameterizedTest
    @MethodSource
    void contains_compare(int initial, int otherbits, boolean expected) {
        def s = new PropertiesSet(initial).contains(otherbits)
        log.info "contains_compare: initial {} otherbits {} expected {} set {}", initial, otherbits, expected, s
        assert s == expected
    }

    static Stream get_pos_compare() {
        Stream.of(
                of(0x00000001, 0, true), //
                of(0x0000000F, 1, true), //
                of(0x0000000B, 2, false), //
                )
    }

    @ParameterizedTest
    @MethodSource
    void get_pos_compare(int initial, int pos, boolean expected) {
        def s = new PropertiesSet(initial).get(pos)
        log.info "get_pos_compare: initial {} pos {} expected {} set {}", initial, pos, expected, s
        assert s == expected
    }

    static Stream equals_hashcode_set() {
        Stream.of(
                of(new PropertiesSet().sets((int)0x808F0001), new PropertiesSet().sets((int)0x808F0001), true), //
                )
    }

    @ParameterizedTest
    @MethodSource
    void equals_hashcode_set(PropertiesSet a, PropertiesSet b, boolean expected) {
        log.info "equals_hashcode_set: a {} b {} expected {}", a, b, expected
        assert a == b
    }
}
