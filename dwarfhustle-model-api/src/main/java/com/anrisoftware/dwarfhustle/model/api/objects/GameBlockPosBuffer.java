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

import java.nio.ShortBuffer;

/**
 * Writes and reads {@link GameBlockPos} in a byte buffer.
 * 
 * <ul>
 * <li>@{code x} X;
 * <li>@{code y} Y;
 * <li>@{code z} Z;
 * </ul>
 * 
 * <pre>
 * int   0         1         2         3
 * short 0    1    2    3    4    5    6
 *       xxxx yyyy zzzz
 * </pre>
 */
public class GameBlockPosBuffer {

    /**
     * Size in bytes.
     */
    public static final int SIZE = 3 * 2;

    protected static final int X_SHORT_INDEX = 0;

    protected static final int Y_SHORT_INDEX = 1;

    protected static final int Z_SHORT_INDEX = 2;

    public static void setX(ShortBuffer b, int offset, int x) {
        b.put(X_SHORT_INDEX + offset, (short) x);
    }

    public static int getX(ShortBuffer b, int offset) {
        return b.get(X_SHORT_INDEX + offset);
    }

    public static void setY(ShortBuffer b, int offset, int y) {
        b.put(Y_SHORT_INDEX + offset, (short) y);
    }

    public static int getY(ShortBuffer b, int offset) {
        return b.get(Y_SHORT_INDEX + offset);
    }

    public static void setZ(ShortBuffer b, int offset, int z) {
        b.put(Z_SHORT_INDEX + offset, (short) z);
    }

    public static int getZ(ShortBuffer b, int offset) {
        return b.get(Z_SHORT_INDEX + offset);
    }

    public static void writeGameBlockPos(ShortBuffer b, int offset, GameBlockPos p) {
        b.put(X_SHORT_INDEX + offset, (short) p.x);
        b.put(Y_SHORT_INDEX + offset, (short) p.y);
        b.put(Z_SHORT_INDEX + offset, (short) p.z);
    }

    public static GameBlockPos readGameBlockPos(ShortBuffer b, int offset) {
        return new GameBlockPos(b.get(X_SHORT_INDEX + offset), b.get(Y_SHORT_INDEX + offset),
                b.get(Z_SHORT_INDEX + offset));
    }

}
