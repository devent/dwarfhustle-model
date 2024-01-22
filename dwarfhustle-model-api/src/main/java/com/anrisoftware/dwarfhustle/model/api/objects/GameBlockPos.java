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

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * X, Y and Z position of a {@link GameObject} on the game map.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
@Getter
public class GameBlockPos implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * X position on the game map
     */
    public final int x;

    /**
     * Y position on the game map
     */
    public final int y;

    /**
     * Z position on the game map
     */
    public final int z;

    public int getDiffX(GameBlockPos pos) {
        return x - pos.x;
    }

    public int getDiffY(GameBlockPos pos) {
        return y - pos.y;
    }

    public int getDiffZ(GameBlockPos pos) {
        return z - pos.z;
    }

    /**
     * Returns string that can be used to store the block position: {@code X/Y/Z}
     */
    public String toSaveString() {
        return getX() + "/" + getY() + "/" + getZ();
    }

    /**
     * Returns the {@link GameBlockPos} parsed from the string.
     */
    public static GameBlockPos parse(String s) {
        var split = StringUtils.split(s, "/");
        var pos = new GameBlockPos(toInt(split[0]), toInt(split[1]), toInt(split[2]));
        return pos;
    }

    private static int toInt(String s) {
        return Integer.parseInt(s);
    }

    public GameBlockPos add(GameBlockPos p) {
        return new GameBlockPos(x + p.x, y + p.y, z + p.z);
    }

    public GameBlockPos addX(int n) {
        return new GameBlockPos(x + n, y, z);
    }

    public GameBlockPos addY(int n) {
        return new GameBlockPos(x, y + n, z);
    }

    public GameBlockPos addZ(int n) {
        return new GameBlockPos(x, y, z + n);
    }

    public boolean isEqual(int x2, int y2, int z2) {
        return x == x2 && y == y2 && z == z2;
    }
}
