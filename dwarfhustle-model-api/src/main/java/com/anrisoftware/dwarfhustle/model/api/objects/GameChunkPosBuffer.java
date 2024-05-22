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
 * Writes and reads {@link GameChunkPos} in a byte b.
 * 
 * <ul>
 * <li>@{code x} start x;
 * <li>@{code y} start y;
 * <li>@{code z} start z;
 * <li>@{code X} end x;
 * <li>@{code Y} end y;
 * <li>@{code Z} end z;
 * </ul>
 * 
 * <pre>
 * int   0         1         2         3
 * short 0    1    2    3    4    5    6
 *       xxxx yyyy zzzz XXXX YYYY ZZZZ
 * </pre>
 */
public class GameChunkPosBuffer extends GameBlockPosBuffer {

    /**
     * Size in bytes.
     */
    public static final int SIZE = 3 * 2 + GameBlockPosBuffer.SIZE;

    private static final int EX_INDEX = 3 * 2;

    private static final int EY_INDEX = 4 * 2;

    private static final int EZ_INDEX = 5 * 2;

    public static void setEx(ByteBuffer b, int offset, int ex) {
        writeShort(b.position(EX_INDEX + offset), (short) ex);
    }

    public static int getEx(ByteBuffer b, int offset) {
        return readShort(b.position(EX_INDEX + offset));
    }

    public static void setEy(ByteBuffer b, int offset, int ey) {
        writeShort(b.position(EY_INDEX + offset), (short) ey);
    }

    public static int getEy(ByteBuffer b, int offset) {
        return readShort(b.position(EY_INDEX + offset));
    }

    public static void setEz(ByteBuffer b, int offset, int ez) {
        writeShort(b.position(EZ_INDEX + offset), (short) ez);
    }

    public static int getEz(ByteBuffer b, int offset) {
        return readShort(b.position(EZ_INDEX + offset));
    }

    public static void writeGameChunkPos(ByteBuffer b, int offset, GameChunkPos p) {
        b.position(offset);
        putGameChunkPos(b, p);
    }

    public static void putGameChunkPos(ByteBuffer b, GameChunkPos p) {
        writeShort(b, (short) p.x);
        writeShort(b, (short) p.y);
        writeShort(b, (short) p.z);
        writeShort(b, (short) p.ep.x);
        writeShort(b, (short) p.ep.y);
        writeShort(b, (short) p.ep.z);
    }

    public static GameChunkPos readGameChunkPos(ByteBuffer b, int offset) {
        b.position(offset);
        return getGameChunkPos(b);
    }

    public static GameChunkPos getGameChunkPos(ByteBuffer b) {
        return new GameChunkPos(readShort(b), readShort(b), readShort(b), readShort(b), readShort(b), readShort(b));
    }

}
