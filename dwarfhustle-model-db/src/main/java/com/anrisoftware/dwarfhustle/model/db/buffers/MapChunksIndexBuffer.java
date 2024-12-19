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

import java.nio.ByteBuffer;

import com.anrisoftware.dwarfhustle.model.api.objects.MapChunk;

/**
 * Index of {@link MapChunk}s that stores the chunk position and chunk size.
 * 
 * <ul>
 * <li>@{code c} index entries count;
 * <li>@{code p0..pN} chunk position;
 * <li>@{code s0..sN} chunk size;
 * </ul>
 * 
 * <pre>
 * int   0         1         2         3         4         5...
 * short 0    1    2    3    4    5    6    7    8    9...
 *       cccc cccc ppp0 ppp0 sss0 sss0 ppp1 ppp1 sss1 sss1 pppN sssN pppN sssN
 * </pre>
 */
public class MapChunksIndexBuffer {

    /**
     * Size in bytes without chunk entries.
     */
    public static final int SIZE_MIN = 4;

    /**
     * Size of one entry.
     */
    public static final int SIZE_ENTRY = 2 * 4;

    private static final int COUNT_INT_INDEX = 0;

    private static final int ENTRIES_OFFSET = 4;

    private static final int POS_OFFSET = 4;

    private static final int SIZE_OFFSET = 8;

    public static void setCount(ByteBuffer b, int offset, int c) {
        b.position(offset);
        var bi = b.asIntBuffer();
        bi.put(COUNT_INT_INDEX, c);
    }

    public static int getCount(ByteBuffer b, int offset) {
        return b.position(offset).asIntBuffer().get(COUNT_INT_INDEX);
    }

    public static void setEntry(ByteBuffer b, int offset, int cid, int[] entry) {
        assert entry.length == 2;
        setEntry(b, offset, cid, entry[0], entry[1]);
    }

    public static void setEntry(ByteBuffer b, int offset, int cid, int pos, int size) {
        offset += ENTRIES_OFFSET + cid * SIZE_ENTRY;
        b.position(offset);
        var bi = b.asIntBuffer();
        bi.put(pos);
        bi.put(size);
    }

    public static void setEntries(ByteBuffer b, int offset, int count, int[] entries) {
        b.position(offset);
        var bi = b.asIntBuffer();
        bi.put(count);
        for (int i = 0; i < count; i++) {
            bi.put(entries[i * 2 + 0]);
            bi.put(entries[i * 2 + 1]);
        }
    }

    public static int[] getEntries(ByteBuffer b, int offset, int[] dest) {
        b.position(offset);
        var bi = b.asIntBuffer();
        int count = bi.get();
        if (dest == null) {
            dest = new int[count * 2];
        }
        for (int i = 0; i < count; i++) {
            dest[i * 2 + 0] = bi.get();
            dest[i * 2 + 1] = bi.get();
        }
        return dest;
    }

    public static int getPos(ByteBuffer b, int offset, int cid) {
        offset += POS_OFFSET + cid * SIZE_ENTRY;
        b.position(offset);
        var buffer = b.asIntBuffer();
        return buffer.get();
    }

    public static int getSize(ByteBuffer b, int offset, int cii) {
        offset += SIZE_OFFSET + cii * SIZE_ENTRY;
        b.position(offset);
        var buffer = b.asIntBuffer();
        return buffer.get();
    }

}
