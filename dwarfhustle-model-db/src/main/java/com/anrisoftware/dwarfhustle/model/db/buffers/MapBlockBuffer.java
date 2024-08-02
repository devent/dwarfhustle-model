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

import static com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos.calcIndex;
import static com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos.calcX;
import static com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos.calcY;
import static com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos.calcZ;
import static com.anrisoftware.dwarfhustle.model.db.buffers.BufferUtils.readInt;
import static com.anrisoftware.dwarfhustle.model.db.buffers.BufferUtils.readShort;
import static com.anrisoftware.dwarfhustle.model.db.buffers.BufferUtils.writeInt;
import static com.anrisoftware.dwarfhustle.model.db.buffers.BufferUtils.writeShort;

import java.nio.ByteBuffer;
import java.util.function.Function;

import com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos;
import com.anrisoftware.dwarfhustle.model.api.objects.MapBlock;
import com.anrisoftware.dwarfhustle.model.api.objects.MapChunk;
import com.anrisoftware.dwarfhustle.model.api.objects.NeighboringDir;
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
     * Size in bytes.
     */
    public static final int SIZE = 4 + 2 + 2 + 2 + 2 + 2;

    private static final int PARENT_INDEX = 2 * 2;

    private static final int MATERIAL_INDEX = 3 * 2;

    private static final int OBJECT_INDEX = 4 * 2;

    private static final int PROP_INDEX = 0 * 4;

    private static final int TEMP_INDEX = 5 * 2;

    private static final int LUX_INDEX = 6 * 2;

    /**
     * Returns the size of the {@link MapBlock}'s buffer for the chunk width, height
     * and depth.
     */
    public static int calcMapBufferSize(int w, int h, int d) {
        return SIZE * w * h * d;
    }

    public static void setParent(ByteBuffer b, int off, int p) {
        writeShort(b.position(PARENT_INDEX + off), (short) p);
    }

    public static int getParent(ByteBuffer b, int off) {
        return readShort(b.position(PARENT_INDEX + off));
    }

    public static void setMaterial(ByteBuffer b, int off, int m) {
        writeShort(b.position(MATERIAL_INDEX + off), (short) m);
    }

    public static int getMaterial(ByteBuffer b, int off) {
        return readShort(b.position(MATERIAL_INDEX + off));
    }

    public static void setObject(ByteBuffer b, int off, int o) {
        writeShort(b.position(OBJECT_INDEX + off), (short) o);
    }

    public static int getObject(ByteBuffer b, int off) {
        return readShort(b.position(OBJECT_INDEX + off));
    }

    public static void setProp(ByteBuffer b, int off, int p) {
        writeInt(b.position(PROP_INDEX + off), p);
    }

    public static int getProp(ByteBuffer b, int off) {
        return readInt(b.position(PROP_INDEX + off));
    }

    public static void setTemp(ByteBuffer b, int off, int t) {
        writeShort(b.position(TEMP_INDEX + off), (short) (t - 32_768));
    }

    public static int getTemp(ByteBuffer b, int off) {
        return readShort(b.position(TEMP_INDEX + off)) + 32_768;
    }

    public static void setLux(ByteBuffer b, int off, int l) {
        writeShort(b.position(LUX_INDEX + off), (short) (l - 32_768));
    }

    public static int getLux(ByteBuffer b, int off) {
        return readShort(b.position(LUX_INDEX + off)) + 32_768;
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
        writeInt(b, block.p.bits);
        writeShort(b, (short) block.parent);
        writeShort(b, (short) block.material);
        writeShort(b, (short) block.object);
        writeShort(b, (short) (block.temp - 32_768));
        writeShort(b, (short) (block.lux - 32_768));
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
        block.pos = new GameBlockPos(calcX(i, cw, sx), calcY(i, cw, sy), calcZ(i, cw, ch, sz));
        block.p = new PropertiesSet(readInt(b));
        block.parent = readShort(b);
        block.material = readShort(b);
        block.object = readShort(b);
        block.temp = readShort(b) + 32_768;
        block.lux = readShort(b) + 32_768;
        return block;
    }

    public static MapBlock getNeighbor(MapBlock mb, NeighboringDir dir, MapChunk chunk,
            Function<Integer, MapChunk> retriever) {
        var dirpos = mb.pos.add(dir.pos);
        if (dirpos.isNegative()) {
            return null;
        }
        if (chunk.isInside(dirpos)) {
            return MapChunkBuffer.getBlock(chunk, dirpos);
        } else {
            var parent = retriever.apply(chunk.parent);
            return MapChunkBuffer.findBlock(parent, dirpos, retriever);
        }
    }

    public static MapBlock getNeighborNorth(MapBlock mb, MapChunk chunk, Function<Integer, MapChunk> retriever) {
        return getNeighbor(mb, NeighboringDir.N, chunk, retriever);
    }

    public static MapBlock getNeighborSouth(MapBlock mb, MapChunk chunk, Function<Integer, MapChunk> retriever) {
        return getNeighbor(mb, NeighboringDir.S, chunk, retriever);
    }

    public static MapBlock getNeighborEast(MapBlock mb, MapChunk chunk, Function<Integer, MapChunk> retriever) {
        return getNeighbor(mb, NeighboringDir.E, chunk, retriever);
    }

    public static MapBlock getNeighborWest(MapBlock mb, MapChunk chunk, Function<Integer, MapChunk> retriever) {
        return getNeighbor(mb, NeighboringDir.W, chunk, retriever);
    }

    public static MapBlock getNeighborUp(MapBlock mb, MapChunk chunk, Function<Integer, MapChunk> retriever) {
        return getNeighbor(mb, NeighboringDir.U, chunk, retriever);
    }

    public static boolean isNeighborsUpEmptyContinuously(MapBlock mb, MapChunk chunk,
            Function<Integer, MapChunk> retriever) {
        MapBlock up = getNeighbor(mb, NeighboringDir.U, chunk, retriever);
        while (up != null) {
            if (!up.isEmpty()) {
                return false;
            }
            if (mb.parent != up.parent) {
                chunk = retriever.apply(up.parent);
            }
            up = getNeighbor(up, NeighboringDir.U, chunk, retriever);
        }
        return true;
    }

}
