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

import java.awt.Color
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.stream.Stream

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

/**
 * @see SunModel
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
class SunModelTest {

    static Stream update_check_pos_color() {
        Stream.of(
                of(ZonedDateTime.of(2023, 3, 1, 12, 0, 0, 0, ZoneId.of("Europe/Paris")), 48.14764645f, 11.59602640f, { m ->
                    assert m.x == 11.226597f
                    assert m.y == -82.147552f
                    assert m.z == 55.908348f
                    assert m.color == [1.0, 1.0, 1.0]
                    assert m.ambientColor == [1.0, 1.0, 1.0]
                    assert m.visible == true
                }),
                of(ZonedDateTime.of(2023, 3, 1, 7, 8, 0, 0, ZoneId.of("Europe/Paris")), 48.14764645f, 11.59602640f, { m ->
                    assert m.x == 97.43707f
                    assert m.y == -22.332703f
                    assert m.z == 2.69574f
                    assert m.color == [0.8065256f, 0.8065256f, 1.0f]
                    assert m.ambientColor == [1.0, 1.0, 1.0]
                    assert m.visible == true
                }),
                of(ZonedDateTime.of(2023, 3, 1, 17, 1, 0, 0, ZoneId.of("Europe/Paris")), 48.14764645f, 11.59602640f, { m ->
                    assert m.x == -92.37911f
                    assert m.y == -35.47079f
                    assert m.z == 14.419497f
                    assert m.color == [0.9035259f, 0.9035259f, 1.0f]
                    assert m.ambientColor == [1.0, 1.0, 1.0]
                    assert m.visible == true
                }),
                )
    }

    @ParameterizedTest
    @MethodSource
    void update_check_pos_color(ZonedDateTime time, float lat, float lng, def expected) {
        def model = new SunModel()
        model.update(time, lat, lng)
        expected(model)
    }

    static Stream InterpTemp_getColor() {
        Stream.of(
                of(0f, [
                    0.78431374,
                    0.78431374,
                    1.0
                ] as float[]),
                of(30f, [
                    1.0,
                    1.0,
                    1.0
                ] as float[]),
                of(90f, [
                    1.0,
                    1.0,
                    1.0
                ] as float[]),
                )
    }

    @ParameterizedTest
    @MethodSource
    void InterpTemp_getColor(float angle, float[] expectedColor) {
        def model = new SunModel.InterpStartEndColors(0f, 15f, new Color(200, 200, 255), new Color(255, 255, 255))
        assert model.getColor(angle) == expectedColor
    }
}
