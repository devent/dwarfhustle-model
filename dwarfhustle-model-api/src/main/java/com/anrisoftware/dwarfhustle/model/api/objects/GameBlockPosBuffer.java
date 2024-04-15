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

import java.nio.ByteBuffer;

/**
 * Writes and reads {@link GameBlockPos} in a byte buffer.
 * <p>
 * <code>[xxxx][yyyy][zzzz]</code>
 */
public class GameBlockPosBuffer {

    /**
     * Size in bytes.
     */
    public static final int SIZE = 3 * 4;

    protected static final int X_INDEX = 0;

    protected static final int Y_INDEX = 1;

    protected static final int Z_INDEX = 2;

    public static void setX(ByteBuffer b, int offset, int x) {
        b.position(offset);
        var buffer = b.asIntBuffer();
        buffer.put(X_INDEX, x);
    }

    public static int getX(ByteBuffer b, int offset) {
        return b.position(offset).asIntBuffer().get(X_INDEX);
    }

    public static void setY(ByteBuffer b, int offset, int y) {
        b.position(offset);
        var buffer = b.asIntBuffer();
        buffer.put(Y_INDEX, y);
    }

    public static int getY(ByteBuffer b, int offset) {
        return b.position(offset).asIntBuffer().get(Y_INDEX);
    }

    public static void setZ(ByteBuffer b, int offset, int z) {
        b.position(offset);
        var buffer = b.asIntBuffer();
        buffer.put(Z_INDEX, z);
    }

    public static int getZ(ByteBuffer b, int offset) {
        return b.position(offset).asIntBuffer().get(Z_INDEX);
    }

    public static void writeGameBlockPos(ByteBuffer b, int offset, GameBlockPos p) {
        b.position(offset);
        var bi = b.asIntBuffer();
        bi.put(X_INDEX, p.x);
        bi.put(Y_INDEX, p.y);
        bi.put(Z_INDEX, p.z);
    }

    public static GameBlockPos readGameBlockPos(ByteBuffer b, int offset) {
        b.position(offset);
        var bi = b.asIntBuffer();
        return new GameBlockPos(bi.get(X_INDEX), bi.get(Y_INDEX), bi.get(Z_INDEX));
    }

}
