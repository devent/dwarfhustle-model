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

import com.anrisoftware.dwarfhustle.model.api.objects.GameChunkPos;

/**
 * Writes and reads CID := {@link GameChunkPos} entries in a buffer.
 * 
 * <ul>
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
 * int   0         1         2         3
 * short 0    1    2    3    4    5    6
 *       iiii xxxx yyyy zzzz XXXX YYYY ZZZZ
 * </pre>
 * </pre>
 */
public class CidGameChunkPosMapBuffer {

    /**
     * Size of one entry in bytes.
     */
    public static final int SIZE = 2 + GameChunkPosBuffer.SIZE;

    private static final int ID_BYTE = 0 * 2;

    private static final int POS_BYTE = 1 * 2;

    public static void write(MutableDirectBuffer b, int offset, int count, int[] dest) {
        for (int i = 0; i < count; i++) {
            b.putShort(offset + i * SIZE + ID_BYTE, (short) dest[i * 7 + 0]);
            b.putShort(offset + i * SIZE + POS_BYTE + 0 * 2, (short) dest[i * 7 + 1]);
            b.putShort(offset + i * SIZE + POS_BYTE + 1 * 2, (short) dest[i * 7 + 2]);
            b.putShort(offset + i * SIZE + POS_BYTE + 2 * 2, (short) dest[i * 7 + 3]);
            b.putShort(offset + i * SIZE + POS_BYTE + 3 * 2, (short) dest[i * 7 + 4]);
            b.putShort(offset + i * SIZE + POS_BYTE + 4 * 2, (short) dest[i * 7 + 5]);
            b.putShort(offset + i * SIZE + POS_BYTE + 5 * 2, (short) dest[i * 7 + 6]);
        }
    }

    public static int[] read(DirectBuffer b, int offset, int count, int[] dest) {
        if (dest == null) {
            dest = new int[count * 7];
        }
        for (int i = 0; i < count; i++) {
            dest[i * 7 + 0] = b.getShort(offset + i * SIZE + ID_BYTE);
            dest[i * 7 + 1] = b.getShort(offset + i * SIZE + POS_BYTE + 0 * 2);
            dest[i * 7 + 2] = b.getShort(offset + i * SIZE + POS_BYTE + 1 * 2);
            dest[i * 7 + 3] = b.getShort(offset + i * SIZE + POS_BYTE + 2 * 2);
            dest[i * 7 + 4] = b.getShort(offset + i * SIZE + POS_BYTE + 3 * 2);
            dest[i * 7 + 5] = b.getShort(offset + i * SIZE + POS_BYTE + 4 * 2);
            dest[i * 7 + 6] = b.getShort(offset + i * SIZE + POS_BYTE + 5 * 2);
        }
        return dest;
    }

}
