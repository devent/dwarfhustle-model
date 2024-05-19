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
import java.nio.ShortBuffer;

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
 * <li>@{code N} 26 CIDs of neighbors;
 * <li>@{code C} {@link CidGameChunkPosMapBuffer};
 * <li>@{code b} optionally {@link MapBlockBuffer};
 * </ul>
 * 
 * <pre>
 * int   0         1         2         3
 * short 0    1    2    3    4    5    6    7    8    9         35        43
 *       iiii PPPP cccc xxxx yyyy zzzz XXXX YYYY ZZZZ NNNN x26. CCCC x8.. bbbb ....
 * </pre>
 */
public class MapChunkBuffer {

    /**
     * Size in bytes for non-leafs.
     */
    public static final int SIZE_MIN = 2 + 2 + 2 + //
            GameChunkPosBuffer.SIZE + //
            26 * 2 + //
            CidGameChunkPosMapBuffer.SIZE_MIN + //
            8 * CidGameChunkPosMapBuffer.SIZE_ENTRY;

    /**
     * Size in bytes for leafs.
     */
    public static final int SIZE_LEAF_MIN = 2 + 2 + 2 + //
            GameChunkPosBuffer.SIZE + //
            26 * 2 + //
            CidGameChunkPosMapBuffer.SIZE_MIN + //
            0 * CidGameChunkPosMapBuffer.SIZE_ENTRY;

    private static final int ID_SHORT_INDEX = 0;

    private static final int PARENT_SHORT_INDEX = 1;

    private static final int CHUNK_SIZE_SHORT_INDEX = 2;

    private static final int POS_SHORT_INDEX = 3;

    private static final int NEIGHBORS_SHORT_INDEX = 9;

    private static final int CHUNKS_SHORT_INDEX = 35;

    public static void setCid(ShortBuffer b, int offset, int id) {
        b.put(ID_SHORT_INDEX + offset, (short) id);
    }

    public static int getCid(ShortBuffer b, int offset) {
        return b.get(ID_SHORT_INDEX + offset);
    }

    public static void setParent(ShortBuffer b, int offset, int p) {
        b.put(PARENT_SHORT_INDEX + offset, (short) p);
    }

    public static int getParent(ShortBuffer b, int offset) {
        return b.get(PARENT_SHORT_INDEX + offset);
    }

    public static void setPos(ShortBuffer b, int offset, int sx, int sy, int sz, int ex, int ey, int ez) {
        offset += POS_SHORT_INDEX;
        GameChunkPosBuffer.setX(b, offset, sx);
        GameChunkPosBuffer.setY(b, offset, sy);
        GameChunkPosBuffer.setZ(b, offset, sz);
        GameChunkPosBuffer.setEx(b, offset, ex);
        GameChunkPosBuffer.setEy(b, offset, ey);
        GameChunkPosBuffer.setEz(b, offset, ez);
    }

    public static int getSx(ShortBuffer b, int offset) {
        offset += POS_SHORT_INDEX;
        return GameChunkPosBuffer.getX(b, offset);
    }

    public static int getSy(ShortBuffer b, int offset) {
        offset += POS_SHORT_INDEX;
        return GameChunkPosBuffer.getY(b, offset);
    }

    public static int getSz(ShortBuffer b, int offset) {
        offset += POS_SHORT_INDEX;
        return GameChunkPosBuffer.getZ(b, offset);
    }

    public static int getEx(ShortBuffer b, int offset) {
        offset += POS_SHORT_INDEX;
        return GameChunkPosBuffer.getEx(b, offset);
    }

    public static int getEy(ShortBuffer b, int offset) {
        offset += POS_SHORT_INDEX;
        return GameChunkPosBuffer.getEy(b, offset);
    }

    public static int getEz(ShortBuffer b, int offset) {
        offset += POS_SHORT_INDEX;
        return GameChunkPosBuffer.getEz(b, offset);
    }

    public static void setNeighbors(ShortBuffer b, int offset, int[] neighbors) {
        b.position(NEIGHBORS_SHORT_INDEX + offset);
        for (int n : neighbors) {
            b.put((short) n);
        }
    }

    public static int[] getNeighbors(ShortBuffer b, int offset, int[] neighbors) {
        b.position(NEIGHBORS_SHORT_INDEX + offset);
        for (int i = 0; i < NeighboringDir.values().length; i++) {
            neighbors[i] = b.get();
        }
        return neighbors;
    }

    public static void setChunkSize(ShortBuffer b, int offset, int c) {
        b.put(CHUNK_SIZE_SHORT_INDEX + offset, (short) c);
    }

    public static int getChunkSize(ShortBuffer b, int offset) {
        return b.get(CHUNK_SIZE_SHORT_INDEX + offset);
    }

    public static void setChunksCount(ShortBuffer b, int offset, int c) {
        offset += CHUNKS_SHORT_INDEX;
        CidGameChunkPosMapBuffer.setCount(b, offset, c);
    }

    public static int getChunksCount(ShortBuffer b, int offset) {
        offset += CHUNKS_SHORT_INDEX;
        return CidGameChunkPosMapBuffer.getCount(b, offset);
    }

    public static void setChunks(ShortBuffer b, int offset, int count, int[] entries) {
        offset += CHUNKS_SHORT_INDEX;
        CidGameChunkPosMapBuffer.setEntries(b, offset, count, entries);
    }

    public static int[] getChunks(ShortBuffer b, int offset, int[] dest) {
        offset += CHUNKS_SHORT_INDEX;
        return CidGameChunkPosMapBuffer.getEntries(b, offset, dest);
    }

    public static void writeMapChunk(ByteBuffer b, int offset, MapChunk chunk) {
        b.position(offset);
        var bs = b.asShortBuffer();
        bs.put((short) chunk.cid);
        bs.put((short) chunk.parent);
        bs.put((short) chunk.chunkSize);
        GameChunkPosBuffer.putGameChunkPos(bs, chunk.pos);
        for (int n : chunk.neighbors) {
            bs.put((short) n);
        }
        if (chunk.isLeaf()) {
            writeEmptyChunks(bs);
        } else {
            writeChunks(bs, chunk, 8);
        }
    }

    private static void writeChunks(ShortBuffer b, MapChunk chunk, int chunksCount) {
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
        CidGameChunkPosMapBuffer.putEntries(b, chunksCount, entries);
    }

    private static void writeEmptyChunks(ShortBuffer b) {
        b.put((short) 0);
    }

    public static MapChunk readMapChunk(ByteBuffer b, int offset) {
        b.position(offset);
        var bs = b.asShortBuffer();
        var chunk = new MapChunk(bs.get(), bs.get(), bs.get(), GameChunkPosBuffer.getGameChunkPos(bs));
        for (int i = 0; i < 8; i++) {
            chunk.neighbors[i] = bs.get();
        }
        var chunkEntries = CidGameChunkPosMapBuffer.getEntries(bs, offset + CHUNKS_SHORT_INDEX, null);
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
