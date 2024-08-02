/*
 * dwarfhustle-model-db - Manages the compile dependencies for the model.
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
package com.anrisoftware.dwarfhustle.model.db.buffers;

import static com.anrisoftware.dwarfhustle.model.db.buffers.BufferUtils.readShort;
import static com.anrisoftware.dwarfhustle.model.db.buffers.BufferUtils.writeShort;

import java.nio.ByteBuffer;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;

import com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos;

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
 * int   0         1
 * short 0    1    2
 *       xxxx yyyy zzzz
 * </pre>
 */
public class GameBlockPosBuffer {

    /**
     * Size in bytes.
     */
    public static final int SIZE = 3 * 2;

    protected static final int X_BYTE = 0 * 2;

    protected static final int Y_BYTE = 1 * 2;

    protected static final int Z_BYTE = 2 * 2;

    public static void setX(ByteBuffer b, int off, int x) {
        writeShort(b.position(X_BYTE + off), (short) x);
    }

    public static int getX(ByteBuffer b, int off) {
        return readShort(b.position(X_BYTE + off));
    }

    public static void setXyz(MutableDirectBuffer b, int off, int x, int y, int z) {
        b.putShort(X_BYTE + off, (short) x);
        b.putShort(Y_BYTE + off, (short) y);
        b.putShort(Z_BYTE + off, (short) z);
    }

    public static void setX(MutableDirectBuffer b, int off, int x) {
        b.putShort(X_BYTE + off, (short) x);
    }

    public static int getX(DirectBuffer b, int off) {
        return b.getShort(X_BYTE + off);
    }

    public static void setY(ByteBuffer b, int off, int y) {
        writeShort(b.position(Y_BYTE + off), (short) y);
    }

    public static int getY(ByteBuffer b, int off) {
        return readShort(b.position(Y_BYTE + off));
    }

    public static void setY(MutableDirectBuffer b, int off, int y) {
        b.putShort(Y_BYTE + off, (short) y);
    }

    public static int getY(DirectBuffer b, int off) {
        return b.getShort(Y_BYTE + off);
    }

    public static void setZ(ByteBuffer b, int off, int z) {
        writeShort(b.position(Z_BYTE + off), (short) z);
    }

    public static int getZ(ByteBuffer b, int off) {
        return readShort(b.position(Z_BYTE + off));
    }

    public static void setZ(MutableDirectBuffer b, int off, int z) {
        b.putShort(Z_BYTE + off, (short) z);
    }

    public static int getZ(DirectBuffer b, int off) {
        return b.getShort(Z_BYTE + off);
    }

    public static void writeGameBlockPos(ByteBuffer b, int off, GameBlockPos p) {
        b.position(off);
        writeShort(b, (short) p.x);
        writeShort(b, (short) p.y);
        writeShort(b, (short) p.z);
    }

    public static GameBlockPos readGameBlockPos(ByteBuffer b, int off) {
        b.position(off);
        return new GameBlockPos(readShort(b), readShort(b), readShort(b));
    }

    public static void writeGameBlockPos(MutableDirectBuffer b, int off, GameBlockPos p) {
        setXyz(b, off, p.x, p.y, p.z);
    }

    public static GameBlockPos readGameBlockPos(DirectBuffer b, int off) {
        return readGameBlockPos(b, off, new GameBlockPos());
    }

    public static GameBlockPos readGameBlockPos(DirectBuffer b, int off, GameBlockPos pos) {
        pos.x = b.getShort(X_BYTE + off);
        pos.y = b.getShort(Y_BYTE + off);
        pos.z = b.getShort(Z_BYTE + off);
        return pos;
    }

}
