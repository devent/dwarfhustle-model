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
import static com.anrisoftware.dwarfhustle.model.db.buffers.BufferUtils.readShort;
import static com.anrisoftware.dwarfhustle.model.db.buffers.BufferUtils.writeShort;
import static com.anrisoftware.dwarfhustle.model.db.buffers.MapBlockBuffer.readMapBlockIndex;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.collections.api.factory.primitive.IntObjectMaps;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;

import com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos;
import com.anrisoftware.dwarfhustle.model.api.objects.GameChunkPos;
import com.anrisoftware.dwarfhustle.model.api.objects.MapBlock;
import com.anrisoftware.dwarfhustle.model.api.objects.MapChunk;
import com.anrisoftware.dwarfhustle.model.api.objects.NeighboringDir;

import lombok.RequiredArgsConstructor;

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

    private static final int ID_INDEX = 0 * 2;

    private static final int PARENT_INDEX = 1 * 2;

    private static final int CHUNK_SIZE_INDEX = 2 * 2;

    private static final int POS_INDEX = 3 * 2;

    private static final int NEIGHBORS_INDEX = 9 * 2;

    private static final int CHUNKS_INDEX = 35 * 2;

    public static void setCid(ByteBuffer b, int offset, int id) {
        writeShort(b.position(ID_INDEX + offset), (short) id);
    }

    public static int getCid(ByteBuffer b, int offset) {
        return readShort(b.position(ID_INDEX + offset));
    }

    public static void setParent(ByteBuffer b, int offset, int p) {
        writeShort(b.position(PARENT_INDEX + offset), (short) p);
    }

    public static int getParent(ByteBuffer b, int offset) {
        return readShort(b.position(PARENT_INDEX + offset));
    }

    public static void setPos(ByteBuffer b, int offset, int sx, int sy, int sz, int ex, int ey, int ez) {
        offset += POS_INDEX;
        GameChunkPosBuffer.setX(b, offset, sx);
        GameChunkPosBuffer.setY(b, offset, sy);
        GameChunkPosBuffer.setZ(b, offset, sz);
        GameChunkPosBuffer.setEx(b, offset, ex);
        GameChunkPosBuffer.setEy(b, offset, ey);
        GameChunkPosBuffer.setEz(b, offset, ez);
    }

    public static int getSx(ByteBuffer b, int offset) {
        offset += POS_INDEX;
        return GameChunkPosBuffer.getX(b, offset);
    }

    public static int getSy(ByteBuffer b, int offset) {
        offset += POS_INDEX;
        return GameChunkPosBuffer.getY(b, offset);
    }

    public static int getSz(ByteBuffer b, int offset) {
        offset += POS_INDEX;
        return GameChunkPosBuffer.getZ(b, offset);
    }

    public static int getEx(ByteBuffer b, int offset) {
        offset += POS_INDEX;
        return GameChunkPosBuffer.getEx(b, offset);
    }

    public static int getEy(ByteBuffer b, int offset) {
        offset += POS_INDEX;
        return GameChunkPosBuffer.getEy(b, offset);
    }

    public static int getEz(ByteBuffer b, int offset) {
        offset += POS_INDEX;
        return GameChunkPosBuffer.getEz(b, offset);
    }

    public static void setNeighbors(ByteBuffer b, int offset, int[] neighbors) {
        b.position(NEIGHBORS_INDEX + offset);
        for (int n : neighbors) {
            writeShort(b, (short) n);
        }
    }

    public static int[] getNeighbors(ByteBuffer b, int offset, int[] neighbors) {
        b.position(NEIGHBORS_INDEX + offset);
        for (int i = 0; i < NeighboringDir.values().length; i++) {
            neighbors[i] = readShort(b);
        }
        return neighbors;
    }

    public static void setChunkSize(ByteBuffer b, int offset, int c) {
        writeShort(b.position(CHUNK_SIZE_INDEX + offset), (short) c);
    }

    public static int getChunkSize(ByteBuffer b, int offset) {
        return readShort(b.position(CHUNK_SIZE_INDEX + offset));
    }

    public static void setChunksCount(ByteBuffer b, int offset, int c) {
        offset += CHUNKS_INDEX;
        CidGameChunkPosMapBuffer.setCount(b, offset, c);
    }

    public static int getChunksCount(ByteBuffer b, int offset) {
        offset += CHUNKS_INDEX;
        return CidGameChunkPosMapBuffer.getCount(b, offset);
    }

    public static void setChunks(ByteBuffer b, int offset, int count, int[] entries) {
        offset += CHUNKS_INDEX;
        CidGameChunkPosMapBuffer.setEntries(b, offset, count, entries);
    }

    public static int[] getChunks(ByteBuffer b, int offset, int[] dest) {
        offset += CHUNKS_INDEX;
        return CidGameChunkPosMapBuffer.getEntries(b, offset, dest);
    }

    public static void writeMapChunk(ByteBuffer b, int offset, MapChunk chunk) {
        b.position(offset);
        writeShort(b, (short) chunk.cid);
        writeShort(b, (short) chunk.parent);
        writeShort(b, (short) chunk.chunkSize);
        GameChunkPosBuffer.putGameChunkPos(b, chunk.pos);
        for (int n : chunk.neighbors) {
            writeShort(b, (short) n);
        }
        if (chunk.isLeaf()) {
            writeEmptyChunks(b);
        } else {
            writeChunks(b, chunk, 8);
        }
    }

    private static void writeChunks(ByteBuffer b, MapChunk chunk, int chunksCount) {
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

    private static void writeEmptyChunks(ByteBuffer b) {
        writeShort(b, (short) 0);
    }

    public static MapChunk readMapChunk(ByteBuffer b, int offset) {
        b.position(offset);
        var chunk = new MapChunk(readShort(b), readShort(b), readShort(b), GameChunkPosBuffer.getGameChunkPos(b));
        for (int i = 0; i < 8; i++) {
            chunk.neighbors[i] = readShort(b);
        }
        var chunkEntries = CidGameChunkPosMapBuffer.getEntries(b, offset + CHUNKS_INDEX, null);
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

    /**
     * Returns an {@link Iterable} over the {@link MapBlock} of this
     * {@link MapChunk}.
     */
    public static Iterable<MapBlock> getBlocks(MapChunk mc) {
        int cw = mc.pos.getSizeX();
        int ch = mc.pos.getSizeY();
        int sx = mc.pos.x;
        int sy = mc.pos.y;
        int sz = mc.pos.z;
        var b = mc.blocks.orElseThrow();
        return () -> new Itr(cw, ch, sx, sy, sz, b, b.capacity() / MapBlockBuffer.SIZE);
    }

    @RequiredArgsConstructor
    private static class Itr implements Iterator<MapBlock> {
        final int cw;
        final int ch;
        final int sx;
        final int sy;
        final int sz;
        final ByteBuffer b;
        final int size;
        int i = 0;

        @Override
        public MapBlock next() {
            return readMapBlockIndex(b, 0, i++, cw, ch, sx, sy, sz);
        }

        @Override
        public boolean hasNext() {
            return i < size;
        }
    }

    public static void forEachBlocks(MapChunk mc, Consumer<MapBlock> consumer) {
        int cw = mc.pos.getSizeX();
        int ch = mc.pos.getSizeY();
        int sx = mc.pos.x;
        int sy = mc.pos.y;
        int sz = mc.pos.z;
        mc.blocks.ifPresent((b) -> {
            for (int i = 0; i < b.capacity() / MapBlockBuffer.SIZE; i++) {
                consumer.accept(readMapBlockIndex(b, 0, i, cw, ch, sx, sy, sz));
            }
        });
    }

    public static MapBlock getBlock(MapChunk mc, int x, int y, int z) {
        var buffer = mc.blocks.orElseThrow();
        return readMapBlock(mc, buffer, x, y, z);
    }

    public static MapBlock getBlock(MapChunk mc, GameBlockPos pos) {
        return getBlock(mc, pos.x, pos.y, pos.z);
    }

    public static void setBlock(MapChunk mc, MapBlock block) {
        var buffer = mc.blocks.orElseThrow();
        writeMapBlock(mc, buffer, block);
    }

    public static void setBlocks(MapChunk mc, Iterable<MapBlock> blocks) {
        var buffer = mc.blocks.orElseThrow();
        for (MapBlock block : blocks) {
            writeMapBlock(mc, buffer, block);
        }
    }

    private static MapBlock readMapBlock(MapChunk mc, ByteBuffer buffer, int x, int y, int z) {
        int w = mc.pos.getSizeX();
        int h = mc.pos.getSizeY();
        int d = mc.pos.getSizeZ();
        int i = calcIndex(w, h, d, mc.pos.x, mc.pos.y, mc.pos.z, x, y, z);
        return readMapBlockIndex(buffer, 0, i, w, h, mc.pos.x, mc.pos.y, mc.pos.z);
    }

    private static void writeMapBlock(MapChunk mc, ByteBuffer buffer, MapBlock block) {
        MapBlockBuffer.writeMapBlockIndex(buffer, 0, block, mc.pos.getSizeX(), mc.pos.getSizeY(), mc.pos.getSizeZ(),
                mc.pos.x, mc.pos.y, mc.pos.z);
    }

    public static MapChunk findChunk(MapChunk mc, GameBlockPos pos, Function<Integer, MapChunk> retriever) {
        return findChunk(mc, pos.x, pos.y, pos.z, retriever);
    }

    public static MapChunk findChunk(MapChunk mc, int x, int y, int z, Function<Integer, MapChunk> retriever) {
        if (mc.blocks.isEmpty()) {
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

    public static MapBlock findBlock(MapChunk mc, int x, int y, int z, Function<Integer, MapChunk> retriever) {
        return findBlock(mc, new GameBlockPos(x, y, z), retriever);
    }

    public static MapBlock findBlock(MapChunk mc, GameBlockPos pos, Function<Integer, MapChunk> retriever) {
        if (mc.blocks.isEmpty()) {
            if (!mc.isInside(pos)) {
                if (mc.cid == 0) {
                    return null;
                }
                var parent = retriever.apply(mc.parent);
                return findBlock(parent, pos, retriever);
            }
            for (var view : mc.getChunks().keyValuesView()) {
                var b = view.getTwo();
                if (b.contains(pos)) {
                    var foundChunk = retriever.apply(view.getOne());
                    return findBlock(foundChunk, pos, retriever);
                }
            }
            return null;
        }
        return getBlock(mc, pos);
    }

    /**
     * Finds the child chunk with the start and end coordinates.
     *
     * @return the ID of the chunk or 0.
     */
    public static int findChild(MapChunk mc, int x, int y, int z, int ex, int ey, int ez,
            Function<Integer, MapChunk> retriever) {
        if (x < 0 || y < 0 || z < 0) {
            return 0;
        }
        if (mc.blocks.isEmpty()) {
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
                    var foundChunk = retriever.apply(view.getOne());
                    return findChild(foundChunk, x, y, z, ex, ey, ez, retriever);
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
}
