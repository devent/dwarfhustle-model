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

import static com.anrisoftware.dwarfhustle.model.api.objects.BufferUtils.readShort;
import static com.anrisoftware.dwarfhustle.model.api.objects.BufferUtils.writeShort;

import java.nio.ByteBuffer;

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

    protected static final int X_INDEX = 0 * 2;

    protected static final int Y_INDEX = 1 * 2;

    protected static final int Z_INDEX = 2 * 2;

    public static void setX(ByteBuffer b, int offset, int x) {
        writeShort(b.position(X_INDEX + offset), (short) x);
    }

    public static int getX(ByteBuffer b, int offset) {
        return readShort(b.position(X_INDEX + offset));
    }

    public static void setY(ByteBuffer b, int offset, int y) {
        writeShort(b.position(Y_INDEX + offset), (short) y);
    }

    public static int getY(ByteBuffer b, int offset) {
        return readShort(b.position(Y_INDEX + offset));
    }

    public static void setZ(ByteBuffer b, int offset, int z) {
        writeShort(b.position(Z_INDEX + offset), (short) z);
    }

    public static int getZ(ByteBuffer b, int offset) {
        return readShort(b.position(Z_INDEX + offset));
    }

    public static void writeGameBlockPos(ByteBuffer b, int offset, GameBlockPos p) {
        b.position(offset);
        writeShort(b, (short) p.x);
        writeShort(b, (short) p.y);
        writeShort(b, (short) p.z);
    }

    public static GameBlockPos readGameBlockPos(ByteBuffer b, int offset) {
        b.position(offset);
        return new GameBlockPos(readShort(b), readShort(b), readShort(b));
    }

}
