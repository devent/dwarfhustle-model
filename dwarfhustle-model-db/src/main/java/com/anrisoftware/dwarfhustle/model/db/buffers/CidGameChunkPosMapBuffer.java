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
package com.anrisoftware.dwarfhustle.model.db.buffers;

import static com.anrisoftware.dwarfhustle.model.db.buffers.BufferUtils.readShort;
import static com.anrisoftware.dwarfhustle.model.db.buffers.BufferUtils.writeShort;

import java.nio.ByteBuffer;

import com.anrisoftware.dwarfhustle.model.api.objects.GameChunkPos;

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

    private static final int COUNT_INDEX = 0 * 2;

    private static final int ID_INDEX = 1 * 2;

    private static final int POS_INDEX = 2 * 2;

    public static void setCount(ByteBuffer b, int offset, int c) {
        writeShort(b.position(COUNT_INDEX + offset), (short) c);
    }

    public static int getCount(ByteBuffer b, int offset) {
        return readShort(b.position(COUNT_INDEX + offset));
    }

    public static void setEntry(ByteBuffer b, int offset, int i, int[] entry) {
        assert entry.length == 7;
        setEntry(b, offset, i, entry[0], entry[1], entry[2], entry[3], entry[4], entry[5], entry[6]);
    }

    public static void setEntry(ByteBuffer b, int offset, int i, int cid, int sx, int sy, int sz, int ex, int ey,
            int ez) {
        b.position(offset + ID_INDEX + i * SIZE_ENTRY);
        writeShort(b, (short) cid);
        writeShort(b, (short) sx);
        writeShort(b, (short) sy);
        writeShort(b, (short) sz);
        writeShort(b, (short) ex);
        writeShort(b, (short) ey);
        writeShort(b, (short) ez);
    }

    public static void setEntries(ByteBuffer b, int offset, int count, int[] entries) {
        b.position(offset);
        putEntries(b, count, entries);
    }

    public static void putEntries(ByteBuffer b, int count, int[] entries) {
        writeShort(b, (short) count);
        for (int i = 0; i < count; i++) {
            writeShort(b, (short) entries[i * 7 + 0]);
            writeShort(b, (short) entries[i * 7 + 1]);
            writeShort(b, (short) entries[i * 7 + 2]);
            writeShort(b, (short) entries[i * 7 + 3]);
            writeShort(b, (short) entries[i * 7 + 4]);
            writeShort(b, (short) entries[i * 7 + 5]);
            writeShort(b, (short) entries[i * 7 + 6]);
        }
    }

    public static int[] getEntries(ByteBuffer b, int offset, int[] dest) {
        b.position(offset);
        int count = readShort(b);
        if (dest == null) {
            dest = new int[count * 7];
        }
        for (int i = 0; i < count; i++) {
            dest[i * 7 + 0] = readShort(b);
            dest[i * 7 + 1] = readShort(b);
            dest[i * 7 + 2] = readShort(b);
            dest[i * 7 + 3] = readShort(b);
            dest[i * 7 + 4] = readShort(b);
            dest[i * 7 + 5] = readShort(b);
            dest[i * 7 + 6] = readShort(b);
        }
        return dest;
    }

    public static int getCid(ByteBuffer b, int offset, int i) {
        return readShort(b.position(offset + ID_INDEX + i * SIZE_ENTRY));
    }

    public static int getSx(ByteBuffer b, int offset, int i) {
        return GameChunkPosBuffer.getX(b, offset + POS_INDEX + i * SIZE_ENTRY);
    }

    public static int getSy(ByteBuffer b, int offset, int i) {
        return GameChunkPosBuffer.getY(b, offset + POS_INDEX + i * SIZE_ENTRY);
    }

    public static int getSz(ByteBuffer b, int offset, int i) {
        return GameChunkPosBuffer.getZ(b, offset + POS_INDEX + i * SIZE_ENTRY);
    }

    public static int getEx(ByteBuffer b, int offset, int i) {
        return GameChunkPosBuffer.getEx(b, offset + POS_INDEX + i * SIZE_ENTRY);
    }

    public static int getEy(ByteBuffer b, int offset, int i) {
        return GameChunkPosBuffer.getEy(b, offset + POS_INDEX + i * SIZE_ENTRY);
    }

    public static int getEz(ByteBuffer b, int offset, int i) {
        return GameChunkPosBuffer.getEz(b, offset + POS_INDEX + i * SIZE_ENTRY);
    }

}
