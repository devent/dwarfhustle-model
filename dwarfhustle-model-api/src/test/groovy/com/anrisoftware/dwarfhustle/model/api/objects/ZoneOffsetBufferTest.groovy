/*
 * dwarfhustle-model-api - Manages the compile dependencies for the model.
 * Copyright © 2022-2025 Erwin Müller (erwin.mueller@anrisoftware.com)
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

import java.time.ZoneOffset

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

/**
 * @see ZoneOffsetBuffer
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
class ZoneOffsetBufferTest {

    @ParameterizedTest
    @CsvSource([
        "+1,+01:00",
        "Z,Z",
        "+02:30,+02:30",
        "+02:30:45,+02:30:45",
        "+0230,+02:30",
    ])
    void get_zone_ids_from_string(def offsetId, def expected) {
        def zone = ZoneOffset.of(offsetId)
        assert zone.getId() == expected
    }

    @ParameterizedTest
    @CsvSource([
        "3600,+01:00",
        "0,Z",
        "5560,+01:32:40",
        "-3600,-01:00",
    ])
    void get_zone_ids_from_seconds(int seconds, def expected) {
        def zone = ZoneOffset.ofTotalSeconds(seconds)
        assert zone.getId() == expected
    }
}
