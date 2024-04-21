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
 * Neighboring direction.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@RequiredArgsConstructor
public enum NeighboringDir {

    /**
     * Up.
     */
    U(new GameBlockPos(0, 0, -1)),

    /**
     * Down.
     */
    D(new GameBlockPos(0, 0, 1)),

    /**
     * North.
     */
    N(new GameBlockPos(0, -1, 0)),

    /**
     * North east.
     */
    NE(new GameBlockPos(1, -1, 0)),

    /**
     * East.
     */
    E(new GameBlockPos(1, 0, 0)),

    /**
     * South east.
     */
    SE(new GameBlockPos(1, 1, 0)),

    /**
     * South.
     */
    S(new GameBlockPos(0, 1, 0)),

    /**
     * South west.
     */
    SW(new GameBlockPos(-1, 1, 0)),

    /**
     * West.
     */
    W(new GameBlockPos(-1, 0, 0)),

    /**
     * North west.
     */
    NW(new GameBlockPos(-1, -1, 0)),

    /**
     * Up north.
     */
    UN(new GameBlockPos(0, -1, -1)),

    /**
     * Up north east.
     */
    UNE(new GameBlockPos(1, -1, -1)),

    /**
     * Up east.
     */
    UE(new GameBlockPos(1, 0, -1)),

    /**
     * Up south east.
     */
    USE(new GameBlockPos(1, 1, -1)),

    /**
     * Up south.
     */
    US(new GameBlockPos(0, 1, -1)),

    /**
     * Up south west.
     */
    USW(new GameBlockPos(-1, 1, -1)),

    /**
     * Up west.
     */
    UW(new GameBlockPos(-1, 0, -1)),

    /**
     * Up north west.
     */
    UNW(new GameBlockPos(-1, -1, -1)),

    /**
     * Down north.
     */
    DN(new GameBlockPos(0, -1, 1)),

    /**
     * Down north east.
     */
    DNE(new GameBlockPos(1, -1, 1)),

    /**
     * Down east.
     */
    DE(new GameBlockPos(1, 0, 1)),

    /**
     * Down south east.
     */
    DSE(new GameBlockPos(1, 1, 1)),

    /**
     * Down south.
     */
    DS(new GameBlockPos(0, 1, 1)),

    /**
     * Down south west.
     */
    DSW(new GameBlockPos(-1, 1, 1)),

    /**
     * Down west.
     */
    DW(new GameBlockPos(-1, 0, 1)),

    /**
     * Down north west.
     */
    DNW(new GameBlockPos(-1, -1, 1));

    public final GameBlockPos pos;

    /**
     * Contains the neighboring directions only on the same level.
     */
    public static final NeighboringDir[] DIRS_SAME_LEVEL = { N, NE, E, SE, S, SW, E, NW };

    /**
     * Contains the neighboring perpendicular N, E, S, W directions only on the same
     * level.
     */
    public static final NeighboringDir[] DIRS_PERPENDICULAR_SAME_LEVEL = { N, E, S, W };

    /**
     * Contains the neighboring edge NE, SE, SW, NW directions only on the same
     * level.
     */
    public static final NeighboringDir[] DIRS_EDGE_SAME_LEVEL = { NE, SE, SW, NW };
}
