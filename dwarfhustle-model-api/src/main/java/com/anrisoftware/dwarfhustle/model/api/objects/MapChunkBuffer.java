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

import org.eclipse.collections.api.factory.primitive.IntObjectMaps;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;

/**
 * Writes and reads {@link MapChunk} in a byte buffer.
 * 
 * <ul>
 * <li>@{code i} chunk CID;
 * <li>@{code P} parent chunk CID;
 * <li>@{code c} chunk size;
 * <li>@{code xyzXYZ} chunk position;
 * <li>@{code C} {@link CidGameChunkPosMapBuffer};
 * <li>@{code b} optionally {@link MapBlockBuffer};
 * </ul>
 * 
 * <pre>
 * long 0         1         2         3         4         5         6         7         8         9         10        11        12        13        14        15        16        17        18        19        20
 * int  0    1    2    3    4    5    6    7    8    9    10   11   12   13   14   15   16   17   18   19   20   21   22   23   24   24   25   26   27   28   29   30   31   32   33   34   35   36   37   38   39
 * byte 0    4    8    12   16   20   24   28   32   36   40   44   48   52   56   58   60   64   68   72   76   80   84   88   92   96   100  104  108  112  116  120  124  128  132  136  140
 *      iiii PPPP cccc xxxx yyyy zzzz XXXX YYYY ZZZZ NNNN NNNN NNNN NNNN NNNN NNNN NNNN NNNN NNNN NNNN NNNN NNNN NNNN NNNN NNNN NNNN NNNN NNNN NNNN NNNN NNNN NNNN NNNN NNNN NNNN NNNN CCCC .... bbbb ....
 * </pre>
 */
public class MapChunkBuffer {

    /**
     * Size in bytes for non-leafs.
     */
    public static final int SIZE_MIN = 4 + 4 + 4 + //
            GameChunkPosBuffer.SIZE + //
            26 * 4 + //
            CidGameChunkPosMapBuffer.SIZE_MIN + //
            8 * CidGameChunkPosMapBuffer.SIZE_ENTRY;

    /**
     * Size in bytes for leafs.
     */
    public static final int SIZE_LEAF_MIN = 4 + 4 + 4 + //
            GameChunkPosBuffer.SIZE + //
            26 * 4 + //
            CidGameChunkPosMapBuffer.SIZE_MIN + //
            0 * CidGameChunkPosMapBuffer.SIZE_ENTRY;

    private static final int ID_INT_INDEX = 0;

    private static final int PARENT_INT_INDEX = 1;

    private static final int POS_OFFSET = 12;

    private static final int CHUNK_SIZE_INT_INDEX = 2;

    private static final int CHUNKS_OFFSET = 140;

    public static void setCid(ByteBuffer b, int offset, int id) {
        b.position(offset);
        var buffer = b.asIntBuffer();
        buffer.put(ID_INT_INDEX, id);
    }

    public static int getCid(ByteBuffer b, int offset) {
        return b.position(offset).asIntBuffer().get(ID_INT_INDEX);
    }

    public static void setParent(ByteBuffer b, int offset, int p) {
        b.position(offset);
        var buffer = b.asIntBuffer();
        buffer.put(PARENT_INT_INDEX, p);
    }

    public static int getParent(ByteBuffer b, int offset) {
        return b.position(offset).asIntBuffer().get(PARENT_INT_INDEX);
    }

    public static void setPos(ByteBuffer b, int offset, int sx, int sy, int sz, int ex, int ey, int ez) {
        offset += POS_OFFSET;
        GameChunkPosBuffer.setX(b, offset, sx);
        GameChunkPosBuffer.setY(b, offset, sy);
        GameChunkPosBuffer.setZ(b, offset, sz);
        GameChunkPosBuffer.setEx(b, offset, ex);
        GameChunkPosBuffer.setEy(b, offset, ey);
        GameChunkPosBuffer.setEz(b, offset, ez);
    }

    public static int getSx(ByteBuffer b, int offset) {
        offset += POS_OFFSET;
        return GameChunkPosBuffer.getX(b, offset);
    }

    public static int getSy(ByteBuffer b, int offset) {
        offset += POS_OFFSET;
        return GameChunkPosBuffer.getY(b, offset);
    }

    public static int getSz(ByteBuffer b, int offset) {
        offset += POS_OFFSET;
        return GameChunkPosBuffer.getZ(b, offset);
    }

    public static int getEx(ByteBuffer b, int offset) {
        offset += POS_OFFSET;
        return GameChunkPosBuffer.getEx(b, offset);
    }

    public static int getEy(ByteBuffer b, int offset) {
        offset += POS_OFFSET;
        return GameChunkPosBuffer.getEy(b, offset);
    }

    public static int getEz(ByteBuffer b, int offset) {
        offset += POS_OFFSET;
        return GameChunkPosBuffer.getEz(b, offset);
    }

    public static void setChunkSize(ByteBuffer b, int offset, int c) {
        b.position(offset);
        var buffer = b.asIntBuffer();
        buffer.put(CHUNK_SIZE_INT_INDEX, c);
    }

    public static int getChunkSize(ByteBuffer b, int offset) {
        return b.position(offset).asIntBuffer().get(CHUNK_SIZE_INT_INDEX);
    }

    public static void setChunksCount(ByteBuffer b, int offset, int c) {
        offset += CHUNKS_OFFSET;
        CidGameChunkPosMapBuffer.setCount(b, offset, c);
    }

    public static int getChunksCount(ByteBuffer b, int offset) {
        offset += CHUNKS_OFFSET;
        return CidGameChunkPosMapBuffer.getCount(b, offset);
    }

    public static void setChunks(ByteBuffer b, int offset, int count, int[] entries) {
        offset += CHUNKS_OFFSET;
        CidGameChunkPosMapBuffer.setEntries(b, offset, count, entries);
    }

    public static int[] getChunks(ByteBuffer b, int offset, int[] dest) {
        offset += CHUNKS_OFFSET;
        return CidGameChunkPosMapBuffer.getEntries(b, offset, dest);
    }

    public static void writeMapChunk(ByteBuffer b, int offset, MapChunk chunk) {
        b.position(offset);
        var bi = b.asIntBuffer();
        bi.put(chunk.cid);
        bi.put(chunk.parent);
        bi.put(chunk.chunkSize);
        GameChunkPosBuffer.putGameChunkPos(bi, chunk.pos);
        bi.put(chunk.neighbors);
        int chunksCount = chunk.getChunksCount();
        if (!chunk.isLeaf() && chunksCount == 0) {
            writeEmptyChunks(bi, 8);
        } else {
            writeChunks(bi, chunk, chunksCount);
        }
    }

    private static void writeChunks(IntBuffer bi, MapChunk chunk, int chunksCount) {
        var entries = new int[chunksCount * 7];
        var viewChunks = chunk.getChunks().keyValuesView().iterator();
        for (int i = 0; i < chunk.getChunks().size(); i++) {
            var next = viewChunks.next();
            entries[i * 7 + 0] = next.getOne();
            entries[i * 7 + 1] = next.getTwo().x;
            entries[i * 7 + 2] = next.getTwo().y;
            entries[i * 7 + 3] = next.getTwo().z;
            entries[i * 7 + 4] = next.getTwo().ep.x;
            entries[i * 7 + 5] = next.getTwo().ep.y;
            entries[i * 7 + 6] = next.getTwo().ep.z;
        }
        CidGameChunkPosMapBuffer.putEntries(bi, chunksCount, entries);
    }

    private static void writeEmptyChunks(IntBuffer bi, int chunksCount) {
        bi.put(chunksCount);
        var entries = new int[8 * 7];
        bi.put(entries);
    }

    public static MapChunk readMapChunk(ByteBuffer b, int offset) {
        b.position(offset);
        var bi = b.asIntBuffer();
        var chunk = new MapChunk(bi.get(), bi.get(), bi.get(), GameChunkPosBuffer.getGameChunkPos(bi));
        bi.get(chunk.neighbors);
        var chunkEntries = CidGameChunkPosMapBuffer.getEntries(b, offset + CHUNKS_OFFSET, null);
        if (!chunk.isLeaf()) {
            MutableIntObjectMap<GameChunkPos> chunks = IntObjectMaps.mutable.ofInitialCapacity(chunkEntries.length / 7);
            for (int i = 0; i < chunkEntries.length / 7; i++) {
                chunks.put(chunkEntries[i * 7 + 0], //
                        new GameChunkPos(chunkEntries[i * 7 + 1], //
                                chunkEntries[i * 7 + 2], //
                                chunkEntries[i * 7 + 3], //
                                chunkEntries[i * 7 + 4], //
                                chunkEntries[i * 7 + 5], //
                                chunkEntries[i * 7 + 6]));
            }
            chunk.setChunks(chunks);
        } else {
            offset += SIZE_LEAF_MIN;
            int size = MapBlockBuffer.calcMapBufferSize(chunk.pos.getSizeX(), chunk.pos.getSizeY(),
                    chunk.pos.getSizeZ());
            var bb = b.slice(offset, size);
            chunk.setBlocksBuffer(bb);
        }
        return chunk;
    }
}
