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

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

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
     * Size in bytes.
     */
    public static final int SIZE = 4 + 2 + 2 + 2 + 2 + 2;

    private static final int PARENT_SHORT_INDEX = 2;

    private static final int MATERIAL_SHORT_INDEX = 3;

    private static final int OBJECT_SHORT_INDEX = 4;

    private static final int PROP_INT_INDEX = 0;

    private static final int TEMP_SHORT_INDEX = 5;

    private static final int LUX_SHORT_INDEX = 6;

    /**
     * Returns the index from the x/y/z position.
     */
    public static int calcIndex(int w, int h, int d, int sx, int sy, int sz, int x, int y, int z) {
        return (z - sz) * w * h + (y - sy) * w + x - sx;
    }

    /**
     * Returns the X position from the index.
     */
    public static int calcX(int i, int w, int sx) {
        return i % w + sx;
    }

    /**
     * Returns the Y position from the index.
     */
    public static int calcY(int i, int w, int sy) {
        return Math.floorMod(i / w, w) + sy;
    }

    /**
     * Returns the Z position from the index.
     */
    public static int calcZ(int i, int w, int h, int sz) {
        return (int) Math.floor(i / w / h) + sz;
    }

    /**
     * Returns the size of the {@link MapBlock}'s buffer for the chunk width, height
     * and depth.
     */
    public static int calcMapBufferSize(int w, int h, int d) {
        return SIZE * w * h * d;
    }

    public static void setParent(ShortBuffer b, int off, int p) {
        b.put(PARENT_SHORT_INDEX + off, (short) p);
    }

    public static int getParent(ShortBuffer b, int off) {
        return b.get(PARENT_SHORT_INDEX + off);
    }

    public static void setMaterial(ShortBuffer b, int off, int m) {
        b.put(MATERIAL_SHORT_INDEX + off, (short) m);
    }

    public static int getMaterial(ShortBuffer b, int off) {
        return b.get(MATERIAL_SHORT_INDEX + off);
    }

    public static void setObject(ShortBuffer b, int off, int o) {
        b.put(OBJECT_SHORT_INDEX + off, (short) o);
    }

    public static int getObject(ShortBuffer b, int off) {
        return b.get(OBJECT_SHORT_INDEX + off);
    }

    public static void setProp(IntBuffer b, int off, int p) {
        b.put(PROP_INT_INDEX + off, p);
    }

    public static int getProp(IntBuffer b, int off) {
        return b.get(PROP_INT_INDEX + off);
    }

    public static void setTemp(ShortBuffer b, int off, int t) {
        b.put(TEMP_SHORT_INDEX + off, (short) (t - 32_769));
    }

    public static int getTemp(ShortBuffer b, int off) {
        return b.get(TEMP_SHORT_INDEX + off) + 32_769;
    }

    public static void setLux(ShortBuffer b, int off, int l) {
        b.put(LUX_SHORT_INDEX + off, (short) (l - 32_769));
    }

    public static int getLux(ShortBuffer b, int off) {
        return b.get(LUX_SHORT_INDEX + off) + 32_769;
    }

    /**
     * Writes the {@link MapBlock} to the buffer at the block index.
     * 
     * @param b      the output {@link ByteBuffer}.
     * @param offset the offset of the output buffer to write to.
     * @param block  the {@link MapBlock} to write.
     * @param cw     the chunk width.
     * @param ch     the chunk height.
     * @param cd     the chunk depth.
     * @param sx     the {@link MapChunk} position start X.
     * @param sy     the {@link MapChunk} position start Y.
     * @param sz     the {@link MapChunk} position start Z.
     */
    public static void writeMapBlockIndex(ByteBuffer b, int offset, MapBlock block, int cw, int ch, int cd, int sx,
            int sy, int sz) {
        int index = calcIndex(cw, ch, cd, sx, sy, sz, block.pos.x, block.pos.y, block.pos.z);
        b.position(offset + index * SIZE);
        var bs = b.asShortBuffer();
        var bi = b.asIntBuffer();
        bi.put(PROP_INT_INDEX, block.p.bits);
        bs.put(PARENT_SHORT_INDEX, (short) block.parent);
        bs.put(MATERIAL_SHORT_INDEX, (short) block.material);
        bs.put(OBJECT_SHORT_INDEX, (short) block.object);
        bs.put(TEMP_SHORT_INDEX, (short) (block.temp - 32_768));
        bs.put(LUX_SHORT_INDEX, (short) (block.lux - 32_768));
    }

    /**
     * Reads the {@link MapBlock} from the buffer at the block index.
     * 
     * @param b      the source {@link ByteBuffer}.
     * @param offset the offset of the source buffer to read from.
     * @param i      the index of the block in the buffer.
     * @param cw     the chunk width.
     * @param ch     the chunk height.
     * @param sx     the {@link MapChunk} position start X.
     * @param sy     the {@link MapChunk} position start Y.
     * @param sz     the {@link MapChunk} position start Z.
     * @return the {@link MapBlock}.
     */
    public static MapBlock readMapBlockIndex(ByteBuffer b, int offset, int i, int cw, int ch, int sx, int sy, int sz) {
        var block = new MapBlock();
        b.position(offset + i * SIZE);
        var bs = b.asShortBuffer();
        var bi = b.asIntBuffer();
        block.pos = new GameBlockPos(calcX(i, cw, sx), calcY(i, cw, sy), calcZ(i, cw, ch, sz));
        block.p = new PropertiesSet(bi.get(PROP_INT_INDEX));
        block.parent = bs.get(PARENT_SHORT_INDEX);
        block.material = bs.get(MATERIAL_SHORT_INDEX);
        block.object = bs.get(OBJECT_SHORT_INDEX);
        block.temp = bs.get(TEMP_SHORT_INDEX) + 32_768;
        block.lux = bs.get(LUX_SHORT_INDEX) + 32_768;
        return block;
    }

}
