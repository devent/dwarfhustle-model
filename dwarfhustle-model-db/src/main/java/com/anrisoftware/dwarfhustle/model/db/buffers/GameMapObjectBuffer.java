/*
 * dwarfhustle-model-db - Manages the compile dependencies for the model.
 * Copyright © 2022-2024 Erwin Müller (erwin.mueller@anrisoftware.com)
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
import com.anrisoftware.dwarfhustle.model.api.objects.PropertiesSet;

/**
 * Writes and reads {@link GameMapObject} in a byte buffer.
 * 
 * <ul>
 * <li>@{code i} the object ID;
 * <li>@{code k} the knowledge ID;
 * <li>@{code o} the knowledge object ID;
 * <li>@{code m} the map ID;
 * <li>@{code x} the X position on the map;
 * <li>@{code y} the Y position on the map;
 * <li>@{code z} the Z position on the map;
 * <li>@{code p} the object properties;
 * </ul>
 * 
 * <pre>
 * long  0                   1                   2                   3                   4
 * int   0         1         2         3         4         5         6         7         8
 * short 0    1    2    3    4    5    6    7    8    9    10   11   12   13   14   15   16
 *       iiii iiii iiii iiii kkkk kkkk oooo oooo mmmm mmmm mmmm mmmm xxxx yyyy zzzz pppp pppp
 * </pre>
 */
public class GameMapObjectBuffer {

    /**
     * Size in bytes.
     */
    public static final int SIZE = GameObjectBuffer.SIZE // 8
            + 4 // k
            + 4 // o
            + 8 // m
            + GameBlockPosBuffer.SIZE // x/y/z
            + 4; // properties

    private static final int KID_BYTES = 2 * 4;

    private static final int OID_BYTES = 3 * 4;

    private static final int MAP_BYTES = 2 * 8;

    private static final int POS_BYTES = 3 * 8;

    private static final int PROPERTIES_BYTES = 15 * 2;

    public static void setKid(MutableDirectBuffer b, int off, int kid) {
        b.putInt(KID_BYTES + off, kid);
    }

    public static int getKid(DirectBuffer b, int off) {
        return b.getInt(KID_BYTES + off);
    }

    public static void setOid(MutableDirectBuffer b, int off, int oid) {
        b.putInt(OID_BYTES + off, oid);
    }

    public static int getOid(DirectBuffer b, int off) {
        return b.getInt(OID_BYTES + off);
    }

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

    public static void setP(MutableDirectBuffer b, int off, int p) {
        b.putInt(PROPERTIES_BYTES + off, p);
    }

    public static int getP(DirectBuffer b, int off) {
        return b.getInt(PROPERTIES_BYTES + off);
    }

    public static void writeObject(MutableDirectBuffer b, int off, GameMapObject o) {
        GameObjectBuffer.writeObject(b, off, o);
        setKid(b, off, o.kid);
        setOid(b, off, o.oid);
        setMap(b, off, o.map);
        setPos(b, off, o.pos);
        setP(b, off, o.p.bits);
    }

    public static GameMapObject readObject(DirectBuffer b, int off, GameMapObject o) {
        GameObjectBuffer.readObject(b, off, o);
        o.setKid(getKid(b, off));
        o.setOid(getOid(b, off));
        o.setMap(getMap(b, off));
        GameBlockPosBuffer.read(b, POS_BYTES + off, o.pos);
        o.setP(new PropertiesSet(getP(b, off)));
        return o;
    }
}
