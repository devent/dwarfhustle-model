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

    private static final int EX_SHORT_INDEX = 3;

    private static final int EY_SHORT_INDEX = 4;

    private static final int EZ_SHORT_INDEX = 5;

    public static void setEx(ShortBuffer b, int offset, int ex) {
        b.put(EX_SHORT_INDEX + offset, (short) ex);
    }

    public static int getEx(ShortBuffer b, int offset) {
        return b.get(EX_SHORT_INDEX + offset);
    }

    public static void setEy(ShortBuffer b, int offset, int ey) {
        b.put(EY_SHORT_INDEX + offset, (short) ey);
    }

    public static int getEy(ShortBuffer b, int offset) {
        return b.get(EY_SHORT_INDEX + offset);
    }

    public static void setEz(ShortBuffer b, int offset, int ez) {
        b.put(EZ_SHORT_INDEX + offset, (short) ez);
    }

    public static int getEz(ShortBuffer b, int offset) {
        return b.get(EZ_SHORT_INDEX + offset);
    }

    public static void writeGameChunkPos(ShortBuffer b, int offset, GameChunkPos p) {
        b.position(offset);
        putGameChunkPos(b, p);
    }

    public static void putGameChunkPos(ShortBuffer b, GameChunkPos p) {
        b.put((short) p.x);
        b.put((short) p.y);
        b.put((short) p.z);
        b.put((short) p.ep.x);
        b.put((short) p.ep.y);
        b.put((short) p.ep.z);
    }

    public static GameChunkPos readGameChunkPos(ShortBuffer b, int offset) {
        b.position(offset);
        return getGameChunkPos(b);
    }

    public static GameChunkPos getGameChunkPos(ShortBuffer b) {
        return new GameChunkPos(b.get(), b.get(), b.get(), b.get(), b.get(), b.get());
    }

}
