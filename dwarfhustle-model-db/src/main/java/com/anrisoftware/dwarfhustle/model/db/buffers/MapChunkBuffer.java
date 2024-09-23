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

import static com.anrisoftware.dwarfhustle.model.api.objects.MapChunk.cid2Id;
import static java.nio.ByteBuffer.allocateDirect;

import java.util.function.Function;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.eclipse.collections.api.factory.primitive.IntObjectMaps;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;

import com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos;
import com.anrisoftware.dwarfhustle.model.api.objects.GameChunkPos;
import com.anrisoftware.dwarfhustle.model.api.objects.MapBlock;
import com.anrisoftware.dwarfhustle.model.api.objects.MapChunk;
import com.anrisoftware.dwarfhustle.model.api.objects.NeighboringDir;
import com.anrisoftware.dwarfhustle.model.api.objects.ObjectsGetter;

/**
 * Writes and reads {@link MapChunk} in a byte buffer.
 * 
 * <ul>
 * <li>@{code i} chunk CID;
 * <li>@{code P} parent chunk CID;
 * <li>@{code c} chunk size;
 * <li>@{code xyzXYZ} chunk position;
 * <li>@{code N} 26 CIDs of neighbors;
 * <li>@{code C} 9 {@link CidGameChunkPosMapBuffer};
 * <li>@{code b} w*h*d times {@link MapBlockBuffer};
 * </ul>
 * 
 * <pre>
 * int   0         1         2         3
 * short 0    1    2    3    4    5    6    7    8    9         35        98        
 *       iiii PPPP cccc xxxx yyyy zzzz XXXX YYYY ZZZZ NNNN x26. CCCC x9.. bbbb ....
 * </pre>
 */
public class MapChunkBuffer {

    private static final int CHUNKS_COUNT = 9;

    /**
     * Minimum size in bytes.
     * 
     * @see #getSize(MapChunk)
     */
    public static final int SIZE_MIN = 2 + 2 + 2 + //
            GameChunkPosBuffer.SIZE + //
            26 * 2 + //
            CHUNKS_COUNT * CidGameChunkPosMapBuffer.SIZE;

    private static final int ID_BYTE = 0 * 2;

    private static final int PARENT_BYTE = 1 * 2;

    private static final int CHUNK_SIZE_BYTE = 2 * 2;

    private static final int POS_BYTE = 3 * 2;

    private static final int NEIGHBORS_BYTE = 9 * 2;

    private static final int CHUNKS_BYTE = 35 * 2;

    private static final int BLOCKS_BYTE = 98 * 2;

    public static MutableDirectBuffer createBlocks(int size) {
        return new UnsafeBuffer(allocateDirect(size));
    }

    public static int getBlocksSize(int w, int h, int d) {
        return w * h * d * MapBlockBuffer.SIZE;
    }

    public static void write(MutableDirectBuffer b, int offset, MapChunk chunk) {
        b.putShort(ID_BYTE + offset, (short) chunk.getCid());
        b.putShort(PARENT_BYTE + offset, (short) chunk.parent);
        b.putShort(CHUNK_SIZE_BYTE + offset, (short) chunk.chunkSize);
        GameChunkPosBuffer.write(b, POS_BYTE + offset, chunk.pos);
        for (int i = 0; i < 26; i++) {
            b.putShort(offset + NEIGHBORS_BYTE + i * 2, (short) chunk.neighbors[i]);
        }
        int[] chunks = new int[9 * 7];
        if (!chunk.isLeaf()) {
            int i = 0;
            for (var pair : chunk.getChunks().keyValuesView()) {
                chunks[i * 7 + 0] = pair.getOne();
                chunks[i * 7 + 1] = pair.getTwo().x;
                chunks[i * 7 + 2] = pair.getTwo().y;
                chunks[i * 7 + 3] = pair.getTwo().z;
                chunks[i * 7 + 4] = pair.getTwo().ep.x;
                chunks[i * 7 + 5] = pair.getTwo().ep.y;
                chunks[i * 7 + 6] = pair.getTwo().ep.z;
                i++;
            }
            CidGameChunkPosMapBuffer.write(b, CHUNKS_BYTE + offset, 9, chunks);
        } else {
            b.putBytes(BLOCKS_BYTE + offset, chunk.getBlocks(), 0, chunk.getBlocks().capacity());
        }
    }

    public static MapChunk read(DirectBuffer b, int offset) {
        var chunk = new MapChunk(cid2Id(b.getShort(ID_BYTE + offset)), b.getShort(PARENT_BYTE + offset),
                b.getShort(CHUNK_SIZE_BYTE + offset), GameChunkPosBuffer.read(b, POS_BYTE + offset));
        for (int i = 0; i < 26; i++) {
            chunk.neighbors[i] = b.getShort(offset + NEIGHBORS_BYTE + i * 2);
        }
        if (!chunk.isLeaf()) {
            var chunkEntries = CidGameChunkPosMapBuffer.read(b, CHUNKS_BYTE + offset, CHUNKS_COUNT, null);
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
            chunk.getBlocks().putBytes(0, b, BLOCKS_BYTE + offset, chunk.getBlocks().capacity());
        }
        return chunk;
    }

    public static MapChunk findChunk(MapChunk mc, GameBlockPos pos, Function<Integer, MapChunk> retriever) {
        return findChunk(mc, pos.x, pos.y, pos.z, retriever);
    }

    public static MapChunk findChunk(MapChunk mc, int x, int y, int z, Function<Integer, MapChunk> retriever) {
        if (!mc.isLeaf()) {
            for (var view : mc.getChunks().keyValuesView()) {
                var b = view.getTwo();
                int bx = b.getX();
                int by = b.getY();
                int bz = b.getZ();
                var ep = b.getEp();
                int ebx = ep.getX();
                int eby = ep.getY();
                int ebz = ep.getZ();
                if (x >= bx && y >= by && z >= bz && x < ebx && y < eby && z < ebz) {
                    var foundChunk = retriever.apply(view.getOne());
                    return findChunk(foundChunk, x, y, z, retriever);
                }
            }
        }
        return mc;
    }

    /**
     * Finds the child chunk with the start and end coordinates.
     *
     * @return the ID of the chunk or 0.
     */
    public static int findChild(MapChunk mc, int x, int y, int z, int ex, int ey, int ez, ObjectsGetter getter) {
        if (x < 0 || y < 0 || z < 0) {
            return 0;
        }
        if (!mc.isLeaf()) {
            int id = findChunk(mc, x, y, z, ex, ey, ez);
            if (id != 0) {
                return id;
            }
            for (var view : mc.getChunks().keyValuesView()) {
                var b = view.getTwo();
                int bx = b.getX();
                int by = b.getY();
                int bz = b.getZ();
                var ep = b.getEp();
                int ebx = ep.getX();
                int eby = ep.getY();
                int ebz = ep.getZ();
                if (x >= bx && y >= by && z >= bz && x < ebx && y < eby && z < ebz) {
                    MapChunk foundChunk = getter.get(MapChunk.OBJECT_TYPE, cid2Id(view.getOne()));
                    return findChild(foundChunk, x, y, z, ex, ey, ez, getter);
                }
            }
        }
        return 0;
    }

    private static int findChunk(MapChunk mc, int x, int y, int z, int ex, int ey, int ez) {
        for (var view : mc.getChunks().keyValuesView()) {
            if (view.getTwo().equals(x, y, z, ex, ey, ez)) {
                return view.getOne();
            }
        }
        return 0;
    }

    public static MapBlock findBlock(MapChunk mc, int x, int y, int z, Function<Integer, MapChunk> retriever) {
        return findBlock(mc, new GameBlockPos(x, y, z), retriever);
    }

    public static MapBlock findBlock(MapChunk c, GameBlockPos pos, Function<Integer, MapChunk> retriever) {
        if (!c.isLeaf()) {
            if (!c.isInside(pos)) {
                if (c.getCid() == 0) {
                    return null;
                }
                var parent = retriever.apply(c.parent);
                return findBlock(parent, pos, retriever);
            }
            for (var view : c.getChunks().keyValuesView()) {
                var b = view.getTwo();
                if (b.contains(pos)) {
                    var foundChunk = retriever.apply(view.getOne());
                    return findBlock(foundChunk, pos, retriever);
                }
            }
            return null;
        }
        final int off = GameChunkPos.calcIndex(c.pos.getSizeX(), c.pos.getSizeY(), c.pos.getSizeZ(), c.pos.x, c.pos.y,
                c.pos.z, pos.x, pos.y, pos.z) * MapBlockBuffer.SIZE;
        return MapBlockBuffer.read(c.getBlocks(), off, pos);
    }

    public static MapBlock getNeighbor(MapBlock mb, NeighboringDir dir, MapChunk c,
            Function<Integer, MapChunk> retriever) {
        var dirpos = mb.pos.add(dir.pos);
        if (dirpos.isNegative()) {
            return null;
        }
        if (c.isInside(dirpos)) {
            final int off = GameChunkPos.calcIndex(c.pos.getSizeX(), c.pos.getSizeY(), c.pos.getSizeZ(), c.pos.x,
                    c.pos.y, c.pos.z, dirpos.x, dirpos.y, dirpos.z) * MapBlockBuffer.SIZE;
            return MapBlockBuffer.read(c.getBlocks(), off, dirpos);
        } else {
            var parent = retriever.apply(c.parent);
            return findBlock(parent, dirpos, retriever);
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
