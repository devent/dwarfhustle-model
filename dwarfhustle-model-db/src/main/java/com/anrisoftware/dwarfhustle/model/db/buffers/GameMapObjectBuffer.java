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

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;

import com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos;
import com.anrisoftware.dwarfhustle.model.api.objects.GameMapObject;

/**
 * Writes and reads {@link GameMapObject} in a byte buffer.
 * 
 * <ul>
 * <li>@{code i} the KID;
 * <li>@{code g} the growth;
 * </ul>
 * 
 * <pre>
 * long  0                   1                   2
 * int   0         1         2         3         4         5
 * short 0    1    2    3    4    5    6    7    8    9    10
 *       iiii iiii iiii iiii mmmm mmmm mmmm mmmm xxxx yyyy zzzz
 * </pre>
 */
public class GameMapObjectBuffer extends GameObjectBuffer {

    /**
     * Size in bytes.
     */
    public static final int SIZE = GameObjectBuffer.SIZE + 8 + GameBlockPosBuffer.SIZE;

    private static final int MAP_BYTES = 1 * 8;

    private static final int POS_BYTES = 8 * 2;

    public static void setMap(MutableDirectBuffer b, int off, long id) {
        b.putLong(MAP_BYTES + off, id);
    }

    public static long getMap(DirectBuffer b, int off) {
        return b.getLong(MAP_BYTES + off);
    }

    public static void setX(MutableDirectBuffer b, int off, int x) {
        GameBlockPosBuffer.setX(b, POS_BYTES + off, x);
    }

    public static int getX(DirectBuffer b, int off) {
        return GameBlockPosBuffer.getX(b, POS_BYTES + off);
    }

    public static void setY(MutableDirectBuffer b, int off, int y) {
        GameBlockPosBuffer.setY(b, POS_BYTES + off, y);
    }

    public static int getY(DirectBuffer b, int off) {
        return GameBlockPosBuffer.getY(b, POS_BYTES + off);
    }

    public static void setZ(MutableDirectBuffer b, int off, int z) {
        GameBlockPosBuffer.setZ(b, POS_BYTES + off, z);
    }

    public static int getZ(DirectBuffer b, int off) {
        return GameBlockPosBuffer.getZ(b, POS_BYTES + off);
    }

    public static void setPos(MutableDirectBuffer b, int off, GameBlockPos pos) {
        GameBlockPosBuffer.write(b, POS_BYTES + off, pos);
    }

    public static GameBlockPos getPos(DirectBuffer b, int off) {
        return GameBlockPosBuffer.read(b, POS_BYTES + off);
    }

    public static void writeObject(MutableDirectBuffer b, int off, GameMapObject o) {
        GameObjectBuffer.writeObject(b, off, o);
        setMap(b, off, o.map);
        setPos(b, off, o.pos);
    }

    public static GameMapObject readObject(DirectBuffer b, int off, GameMapObject o) {
        GameObjectBuffer.readObject(b, off, o);
        o.map = getMap(b, off);
        GameBlockPosBuffer.read(b, POS_BYTES + off, o.pos);
        return o;
    }
}
