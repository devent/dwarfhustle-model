/*
 * dwarfhustle-model-api - Manages the compile dependencies for the model.
 * Copyright © 2022-2024 Erwin Müller (erwin.mueller@anrisoftware.com)
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.params.provider.Arguments.of

import java.util.stream.Stream

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

/**
 * @see MapCoordinate
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
class MapCoordinateTest {

    static Stream toDecimalDegrees() {
        Stream.of(
                of(0, 0, 0, 0),
                of(30, 0, 0, 30),
                of(30, 15, 50, 30.263888889f),
                )
    }

    @ParameterizedTest
    @MethodSource
    void toDecimalDegrees(float d, float m, float s, float expected) {
        assertThat MapCoordinate.toDecimalDegrees(d, m, s), equalTo(expected)
    }

    @ParameterizedTest
    @MethodSource("toDecimalDegrees")
    void toDegreesMinSec(float exd, float exm, float exs, float dd) {
        def res = MapCoordinate.toDegreesMinSec(dd, null)
        assertThat res[0], equalTo(exd)
        assertThat res[1], equalTo(exm)
        assertThat res[2], equalTo(exs)
    }

    static Stream check_toString() {
        Stream.of(
                of(0, 0, """0°0'0"N,0°0'0"E"""),
                of(30.263888889f, 30.263888889f, """30°15'50"N,30°15'50"E"""),
                of(-30.263888889f, -30.263888889f, """30°15'50"S,30°15'50"W"""),
                )
    }

    @ParameterizedTest
    @MethodSource
    void check_toString(float lat, float lon, String expected) {
        assertThat new MapCoordinate(lat, lon).toString(), equalTo(expected)
    }
}
