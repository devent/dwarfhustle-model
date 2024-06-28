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
package com.anrisoftware.dwarfhustle.model.api.objects;

import lombok.RequiredArgsConstructor;

/**
 * {@link MapBlock} flags.
 */
@RequiredArgsConstructor
public enum MapBlockFlags {

    HIDDEN(0b00000000_00000000),

    VISIBLE(0b00000000_00000001),

    FILLED(0b00000000_00000010),

    EMPTY(0b00000000_00000100),

    LIQUID(0b00000000_00001000),

    RAMP(0b00000000_00010000),

    FLOOR(0b00000000_00100000),

    ROOF(0b00000000_01000000),

    DISCOVERED(0b00000000_10000000),

    HAVE_CEILING(0b00000001_00000000),

    HAVE_FLOOR(0b00000010_00000000),

    HAVE_NATURAL_LIGHT(0b00000100_00000000)

    ;

    public final int flag;
}
