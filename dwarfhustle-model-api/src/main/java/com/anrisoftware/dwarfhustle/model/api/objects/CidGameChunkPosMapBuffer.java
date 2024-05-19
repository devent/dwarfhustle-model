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
 * Writes and reads CID := {@link GameChunkPos} entries in a buffer.
 * 
 * <ul>
 * <li>@{code c} entries count;
 * <li>@{code i} CID of chunk;
 * <li>@{code x} start x;
 * <li>@{code y} start y;
 * <li>@{code z} start z;
 * <li>@{code X} end x;
 * <li>@{code Y} end y;
 * <li>@{code Z} end z;
 * </ul>
 * 
 * <pre>
 * int   0         1         2         3         4
 * short 0    1    2    3    4    5    6    7    8
 *       cccc iiii xxxx yyyy zzzz XXXX YYYY ZZZZ ...
 * </pre>
 * </pre>
 */
public class CidGameChunkPosMapBuffer {

    /**
     * Size in bytes without chunks.
     */
    public static final int SIZE_MIN = 2;

    /**
     * Size of one entry in bytes.
     */
    public static final int SIZE_ENTRY = 2 + GameChunkPosBuffer.SIZE;

    /**
     * Size of one entry in shorts (2 bytes).
     */
    public static final int SIZE_ENTRY_SHORT = SIZE_ENTRY / 2;

    private static final int COUNT_SHORT_INDEX = 0;

    private static final int ID_SHORT_INDEX = 1;

    private static final int POS_SHORT_INDEX = 2;

    public static void setCount(ShortBuffer b, int offset, int c) {
        b.put(COUNT_SHORT_INDEX + offset, (short) c);
    }

    public static int getCount(ShortBuffer b, int offset) {
        return b.get(COUNT_SHORT_INDEX + offset);
    }

    public static void setEntry(ShortBuffer b, int offset, int i, int[] entry) {
        assert entry.length == 7;
        setEntry(b, offset, i, entry[0], entry[1], entry[2], entry[3], entry[4], entry[5], entry[6]);
    }

    public static void setEntry(ShortBuffer b, int offset, int i, int cid, int sx, int sy, int sz, int ex, int ey,
            int ez) {
        offset += ID_SHORT_INDEX + i * SIZE_ENTRY_SHORT;
        b.position(offset);
        b.put((short) cid);
        b.put((short) sx);
        b.put((short) sy);
        b.put((short) sz);
        b.put((short) ex);
        b.put((short) ey);
        b.put((short) ez);
    }

    public static void setEntries(ShortBuffer b, int offset, int count, int[] entries) {
        b.position(offset);
        putEntries(b, count, entries);
    }

    public static void putEntries(ShortBuffer b, int count, int[] entries) {
        b.put((short) count);
        for (int i = 0; i < count; i++) {
            b.put((short) entries[i * 7 + 0]);
            b.put((short) entries[i * 7 + 1]);
            b.put((short) entries[i * 7 + 2]);
            b.put((short) entries[i * 7 + 3]);
            b.put((short) entries[i * 7 + 4]);
            b.put((short) entries[i * 7 + 5]);
            b.put((short) entries[i * 7 + 6]);
        }
    }

    public static int[] getEntries(ShortBuffer b, int offset, int[] dest) {
        b.position(offset);
        int count = b.get();
        if (dest == null) {
            dest = new int[count * 7];
        }
        for (int i = 0; i < count; i++) {
            dest[i * 7 + 0] = b.get();
            dest[i * 7 + 1] = b.get();
            dest[i * 7 + 2] = b.get();
            dest[i * 7 + 3] = b.get();
            dest[i * 7 + 4] = b.get();
            dest[i * 7 + 5] = b.get();
            dest[i * 7 + 6] = b.get();
        }
        return dest;
    }

    public static int getCid(ShortBuffer b, int offset, int i) {
        offset += ID_SHORT_INDEX + i * SIZE_ENTRY_SHORT;
        b.position(offset);
        return b.get();
    }

    public static int getSx(ShortBuffer b, int offset, int i) {
        offset += POS_SHORT_INDEX + i * SIZE_ENTRY_SHORT;
        return GameChunkPosBuffer.getX(b, offset);
    }

    public static int getSy(ShortBuffer b, int offset, int i) {
        offset += POS_SHORT_INDEX + i * SIZE_ENTRY_SHORT;
        return GameChunkPosBuffer.getY(b, offset);
    }

    public static int getSz(ShortBuffer b, int offset, int i) {
        offset += POS_SHORT_INDEX + i * SIZE_ENTRY_SHORT;
        return GameChunkPosBuffer.getZ(b, offset);
    }

    public static int getEx(ShortBuffer b, int offset, int i) {
        offset += POS_SHORT_INDEX + i * SIZE_ENTRY_SHORT;
        return GameChunkPosBuffer.getEx(b, offset);
    }

    public static int getEy(ShortBuffer b, int offset, int i) {
        offset += POS_SHORT_INDEX + i * SIZE_ENTRY_SHORT;
        return GameChunkPosBuffer.getEy(b, offset);
    }

    public static int getEz(ShortBuffer b, int offset, int i) {
        offset += POS_SHORT_INDEX + i * SIZE_ENTRY_SHORT;
        return GameChunkPosBuffer.getEz(b, offset);
    }

}
