/*
 * dwarfhustle-model-db - Manages the compile dependencies for the model.
 * Copyright © 2022-2025 Erwin Müller (erwin.mueller@anrisoftware.com)
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

    private static final int EX_BYTE = 3 * 2;

    private static final int EY_BYTE = 4 * 2;

    private static final int EZ_BYTE = 5 * 2;

    public static GameChunkPos read(DirectBuffer b, int offset) {
        return new GameChunkPos(b.getShort(X_BYTE + offset), b.getShort(Y_BYTE + offset), b.getShort(Z_BYTE + offset),
                b.getShort(EX_BYTE + offset), b.getShort(EY_BYTE + offset), b.getShort(EZ_BYTE + offset));
    }

    public static void write(MutableDirectBuffer b, int offset, GameChunkPos p) {
        b.putShort(X_BYTE + offset, (short) p.x);
        b.putShort(Y_BYTE + offset, (short) p.y);
        b.putShort(Z_BYTE + offset, (short) p.z);
        b.putShort(EX_BYTE + offset, (short) p.ep.x);
        b.putShort(EY_BYTE + offset, (short) p.ep.y);
        b.putShort(EZ_BYTE + offset, (short) p.ep.z);
    }

}
