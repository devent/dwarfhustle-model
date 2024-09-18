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

import static com.anrisoftware.dwarfhustle.model.db.buffers.BufferUtils.int2short;
import static com.anrisoftware.dwarfhustle.model.db.buffers.BufferUtils.short2int;

import java.nio.ByteBuffer;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;

import com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos;
import com.anrisoftware.dwarfhustle.model.api.objects.MapBlock;
import com.anrisoftware.dwarfhustle.model.api.objects.PropertiesSet;

/**
 * Writes and reads {@link MapBlock} in a byte buffer.
 * 
 * <ul>
 * <li>@{code p} tile properties;
 * <li>@{code P} parent chunk CID;
 * <li>@{code m} material KID;
 * <li>@{code o} object KID;
 * <li>@{code t} temperature;
 * <li>@{code t} light lux;
 * </ul>
 * 
 * <pre>
 * int   0         1         2         3
 * short 0    1    2    3    4    5    6
 *       pppp pppp PPPP mmmm oooo tttt llll
 * </pre>
 */
public class MapBlockBuffer {

    /**
     * Size in bytes. 14 bytes.
     */
    public static final int SIZE = 4 + 2 + 2 + 2 + 2 + 2;

    private static final int PARENT_BYTE = 2 * 2;

    private static final int MATERIAL_BYTE = 3 * 2;

    private static final int OBJECT_BYTE = 4 * 2;

    private static final int PROP_BYTE = 0 * 4;

    private static final int TEMP_BYTE = 5 * 2;

    private static final int LUX_BYTE = 6 * 2;

    /**
     * Returns the size of the {@link MapBlock}'s buffer for the chunk width, height
     * and depth.
     */
    public static int calcMapBufferSize(int w, int h, int d) {
        return SIZE * w * h * d;
    }

    public static void setParent(MutableDirectBuffer b, int off, int p) {
        b.putShort(PARENT_BYTE + off, (short) p);
    }

    public static int getParent(DirectBuffer b, int off) {
        return b.getShort(PARENT_BYTE + off);
    }

    public static void setMaterial(MutableDirectBuffer b, int off, int m) {
        b.putShort(MATERIAL_BYTE + off, (short) m);
    }

    public static int getMaterial(DirectBuffer b, int off) {
        return b.getShort(MATERIAL_BYTE + off);
    }

    public static void setObject(MutableDirectBuffer b, int off, int o) {
        b.putShort(OBJECT_BYTE + off, (short) o);
    }

    public static int getObject(DirectBuffer b, int off) {
        return b.getShort(OBJECT_BYTE + off);
    }

    public static void setProp(MutableDirectBuffer b, int off, int p) {
        b.putInt(PROP_BYTE + off, p);
    }

    public static int getProp(DirectBuffer b, int off) {
        return b.getInt(PROP_BYTE + off);
    }

    public static void setTemp(MutableDirectBuffer b, int off, int t) {
        b.putShort(TEMP_BYTE + off, int2short(t));
    }

    public static int getTemp(DirectBuffer b, int off) {
        return short2int(b.getShort(TEMP_BYTE + off));
    }

    public static void setLux(MutableDirectBuffer b, int off, int l) {
        b.putShort(LUX_BYTE + off, int2short(l));
    }

    public static int getLux(DirectBuffer b, int off) {
        return short2int(b.getShort(LUX_BYTE + off));
    }

    /**
     * Writes the {@link MapBlock} to the buffer.
     * 
     * @param b      the output {@link ByteBuffer}.
     * @param offset the offset of the output buffer to write to.
     * @param block  the {@link MapBlock} to write.
     */
    public static void write(MutableDirectBuffer b, int offset, MapBlock block) {
        b.putInt(PROP_BYTE + offset, block.p.bits);
        b.putShort(PARENT_BYTE + offset, (short) block.parent);
        b.putShort(MATERIAL_BYTE + offset, (short) block.material);
        b.putShort(OBJECT_BYTE + offset, (short) block.object);
        b.putShort(TEMP_BYTE + offset, (int2short(block.temp)));
        b.putShort(LUX_BYTE + offset, (int2short(block.lux)));
    }

    /**
     * Reads the {@link MapBlock} from the buffer.
     * 
     * @param b      the source {@link ByteBuffer}.
     * @param offset the offset of the source buffer to read from.
     * @return the {@link MapBlock}.
     */
    public static MapBlock read(DirectBuffer b, int offset, GameBlockPos pos) {
        var block = new MapBlock(pos);
        block.p = new PropertiesSet(b.getInt(PROP_BYTE + offset));
        block.parent = b.getShort(PARENT_BYTE + offset);
        block.material = b.getShort(MATERIAL_BYTE + offset);
        block.object = b.getShort(OBJECT_BYTE + offset);
        block.temp = short2int(b.getShort(TEMP_BYTE + offset));
        block.lux = short2int(b.getShort(LUX_BYTE + offset));
        return block;
    }

}
