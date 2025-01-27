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

import static com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos.calcIndex;
import static com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos.fromIndex;
import static com.anrisoftware.dwarfhustle.model.api.objects.MapBlock.EMPTY_POS;
import static com.anrisoftware.dwarfhustle.model.api.objects.MapChunk.cid2Id;
import static com.anrisoftware.dwarfhustle.model.api.objects.MapChunk.getChunk;
import static com.anrisoftware.dwarfhustle.model.db.buffers.MapBlockBuffer.getProp;
import static com.anrisoftware.dwarfhustle.model.db.buffers.MapBlockBuffer.readMapBlockIndex;
import static java.nio.ByteBuffer.allocateDirect;

import java.util.Iterator;
import java.util.function.Consumer;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.eclipse.collections.api.factory.primitive.IntIntMaps;
import org.eclipse.collections.api.factory.primitive.LongObjectMaps;
import org.eclipse.collections.api.map.primitive.MutableIntIntMap;
import org.eclipse.collections.api.map.primitive.MutableLongObjectMap;

import com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos;
import com.anrisoftware.dwarfhustle.model.api.objects.GameChunkPos;
import com.anrisoftware.dwarfhustle.model.api.objects.GameMap;
import com.anrisoftware.dwarfhustle.model.api.objects.MapBlock;
import com.anrisoftware.dwarfhustle.model.api.objects.MapChunk;
import com.anrisoftware.dwarfhustle.model.api.objects.NeighboringDir;
import com.anrisoftware.dwarfhustle.model.api.objects.ObjectsGetter;
import com.anrisoftware.dwarfhustle.model.api.objects.PropertiesSet;
import com.anrisoftware.dwarfhustle.model.db.api.MapChunksStorage;

import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * Writes and reads {@link MapChunk} in a byte buffer.
 *
 * <ul>
 * <li>@{code i} chunk CID;
 * <li>@{code P} parent chunk CID;
 * <li>@{code c} chunk size;
 * <li>@{code w} map width;
 * <li>@{code h} map height;
 * <li>@{code xyzXYZ} chunk position;
 * <li>@{code N} 26 CIDs of neighbors;
 * <li>@{code C} 8 {@link CidGameChunkPosMapBuffer};
 * <li>@{code b} w*h*d times {@link MapBlockBuffer};
 * </ul>
 *
 * <pre>
 * int   0         1         2         3         4         5         6         7         8
 * short 0    1    2    3    4    5    6    7    8    9    10   11        37        94
 *       iiii PPPP cccc wwww hhhh xxxx yyyy zzzz XXXX YYYY ZZZZ NNNN x26. CCCC x8.. bbbb ....
 * </pre>
 */
public class MapChunkBuffer {

    private static final int CHUNKS_COUNT = 8;

    /**
     * Minimum size in bytes.
     *
     * @see #getSize(MapChunk)
     */
    public static final int SIZE_MIN = 2 + 2 + 2 + 2 + 2 + //
            GameChunkPosBuffer.SIZE + //
            26 * 2 + //
            2 + CHUNKS_COUNT * CidGameChunkPosMapBuffer.SIZE;

    /**
     * Contains the {@link MapChunk} and the {@link MapBlock} index.
     *
     * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
     */
    @Data
    public static class MapBlockResult {

        public final MapChunk c;

        public final int index;

        public final int off;

        public MapBlockResult(MapChunk c, int index) {
            this.c = c;
            this.index = index;
            this.off = index * MapBlockBuffer.SIZE;
        }

        public boolean isValid() {
            return index > -1;
        }

    }

    private static final int ID_BYTE = 0 * 2;

    private static final int PARENT_BYTE = 1 * 2;

    private static final int CHUNK_SIZE_BYTE = 2 * 2;

    private static final int WIDTH_BYTE = 3 * 2;

    private static final int HEIGHT_BYTE = 4 * 2;

    private static final int POS_BYTE = 5 * 2;

    private static final int NEIGHBORS_BYTE = 11 * 2;

    private static final int CHUNKS_BYTE = 37 * 2;

    private static final int BLOCKS_BYTE = 94 * 2;

    public static MutableDirectBuffer createBlocks(int size) {
        return new UnsafeBuffer(allocateDirect(size));
    }

    public static int getBlocksSize(int w, int h, int d) {
        return w * h * d * MapBlockBuffer.SIZE;
    }

    public static void cacheCids(GameMap gm, MapChunksStorage storage) {
        final MutableIntIntMap map = IntIntMaps.mutable.ofInitialCapacity(gm.getSize());
        storage.forEachValue(c -> {
            if (c.isLeaf()) {
                for (final var b : MapChunkBuffer.getBlocks(c)) {
                    final int index = GameBlockPos.calcIndex(gm, b.getPos());
                    map.put(index, c.getCid());
                }
            }
        });
        gm.cids = map.toImmutable();
    }

    public static void write(MutableDirectBuffer b, int offset, MapChunk chunk) {
        b.putShort(ID_BYTE + offset, (short) chunk.getCid());
        b.putShort(PARENT_BYTE + offset, (short) chunk.parent);
        b.putShort(CHUNK_SIZE_BYTE + offset, (short) chunk.chunkSize);
        b.putShort(WIDTH_BYTE + offset, (short) chunk.width);
        b.putShort(HEIGHT_BYTE + offset, (short) chunk.height);
        GameChunkPosBuffer.write(b, POS_BYTE + offset, chunk.pos);
        for (int i = 0; i < 26; i++) {
            b.putShort(offset + NEIGHBORS_BYTE + i * 2, (short) chunk.neighbors[i]);
        }
        final int[] chunks = new int[CHUNKS_COUNT * 7];
        if (!chunk.isLeaf()) {
            int i = 0;
            if (chunk.getChunks() != null) {
                for (final var pair : chunk.getChunks().keyValuesView()) {
                    chunks[i * 7 + 0] = (int) pair.getOne();
                    chunks[i * 7 + 1] = pair.getTwo().x;
                    chunks[i * 7 + 2] = pair.getTwo().y;
                    chunks[i * 7 + 3] = pair.getTwo().z;
                    chunks[i * 7 + 4] = pair.getTwo().ep.x;
                    chunks[i * 7 + 5] = pair.getTwo().ep.y;
                    chunks[i * 7 + 6] = pair.getTwo().ep.z;
                    i++;
                }
            }
            CidGameChunkPosMapBuffer.write(b, CHUNKS_BYTE + offset, CHUNKS_COUNT, chunks);
        } else {
            b.putBytes(BLOCKS_BYTE + offset, chunk.getBlocks(), 0, chunk.getBlocks().capacity());
        }
    }

    public static MapChunk read(DirectBuffer b, int offset) {
        final var chunk = new MapChunk(cid2Id(b.getShort(ID_BYTE + offset)), b.getShort(PARENT_BYTE + offset),
                b.getShort(CHUNK_SIZE_BYTE + offset), b.getShort(WIDTH_BYTE + offset), b.getShort(HEIGHT_BYTE + offset),
                GameChunkPosBuffer.read(b, POS_BYTE + offset));
        for (int i = 0; i < 26; i++) {
            chunk.neighbors[i] = b.getShort(offset + NEIGHBORS_BYTE + i * 2);
        }
        if (!chunk.isLeaf()) {
            final var chunkEntries = CidGameChunkPosMapBuffer.read(b, CHUNKS_BYTE + offset, CHUNKS_COUNT, null);
            final MutableLongObjectMap<GameChunkPos> chunks = LongObjectMaps.mutable
                    .ofInitialCapacity(chunkEntries.length / 7);
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

    public static MapChunk findChunk(MapChunk mc, GameBlockPos pos, ObjectsGetter og) {
        return findChunk(mc, pos.x, pos.y, pos.z, og);
    }

    public static MapChunk findChunk(MapChunk c, int x, int y, int z, ObjectsGetter og) {
        if (!c.isInside(x, y, z)) {
            c = getChunk(og, c.getParent());
            return findChunk(c, x, y, z, og);
        }
        if (!c.isLeaf()) {
            for (final var view : c.getChunks().keyValuesView()) {
                final var b = view.getTwo();
                final int bx = b.getX();
                final int by = b.getY();
                final int bz = b.getZ();
                final var ep = b.getEp();
                final int ebx = ep.getX();
                final int eby = ep.getY();
                final int ebz = ep.getZ();
                if (x >= bx && y >= by && z >= bz && x < ebx && y < eby && z < ebz) {
                    final var foundChunk = getChunk(og, cid2Id(view.getOne()));
                    return findChunk(foundChunk, x, y, z, og);
                }
            }
        }
        return c;
    }

    /**
     * Finds the child chunk with the start and end coordinates.
     *
     * @return the ID of the chunk or 0.
     */
    public static long findChild(MapChunk mc, int x, int y, int z, int ex, int ey, int ez, ObjectsGetter getter) {
        if (x < 0 || y < 0 || z < 0) {
            return 0;
        }
        if (!mc.isLeaf()) {
            final long id = findChunk(mc, x, y, z, ex, ey, ez);
            if (id != 0) {
                return id;
            }
            for (final var view : mc.getChunks().keyValuesView()) {
                final var b = view.getTwo();
                final int bx = b.getX();
                final int by = b.getY();
                final int bz = b.getZ();
                final var ep = b.getEp();
                final int ebx = ep.getX();
                final int eby = ep.getY();
                final int ebz = ep.getZ();
                if (x >= bx && y >= by && z >= bz && x < ebx && y < eby && z < ebz) {
                    final MapChunk foundChunk = getter.get(MapChunk.OBJECT_TYPE, cid2Id(view.getOne()));
                    return findChild(foundChunk, x, y, z, ex, ey, ez, getter);
                }
            }
        }
        return 0;
    }

    private static long findChunk(MapChunk c, int x, int y, int z, int ex, int ey, int ez) {
        for (final var view : c.getChunks().keyValuesView()) {
            if (view.getTwo().equals(x, y, z, ex, ey, ez)) {
                return view.getOne();
            }
        }
        return 0;
    }

    public static MapBlock findBlock(MapChunk c, int x, int y, int z, ObjectsGetter og) {
        return findBlock(c, new GameBlockPos(x, y, z), og);
    }

    public static MapBlock findBlock(MapChunk c, GameBlockPos pos, ObjectsGetter og) {
        final var res = findBlockIndex(c, pos, og);
        if (res.isValid()) {
            return MapBlockBuffer.read(res.c.getBlocks(), res.getOff(), pos);
        } else {
            return null;
        }
    }

    /**
     * Finds the block index
     */
    public static MapBlockResult findBlockIndex(MapChunk c, GameBlockPos pos, ObjectsGetter og) {
        if (!c.isInside(pos)) {
            final var parent = getChunk(og, cid2Id(c.parent));
            return findBlockIndex(parent, pos, og);
        }
        if (!c.isLeaf()) {
            for (final var view : c.getChunks().keyValuesView()) {
                final var b = view.getTwo();
                if (b.contains(pos)) {
                    final var foundChunk = getChunk(og, cid2Id(view.getOne()));
                    return findBlockIndex(foundChunk, pos, og);
                }
            }
            return new MapBlockResult(c, -1);
        }
        return new MapBlockResult(c, calcIndex(c, pos.x, pos.y, pos.z));
    }

    /**
     * Returns an {@link Iterable} over the {@link MapBlock} of this
     * {@link MapChunk}.
     */
    public static Iterable<MapBlock> getBlocks(MapChunk mc) {
        final int cw = mc.pos.getSizeX();
        final int ch = mc.pos.getSizeY();
        final int cd = mc.pos.getSizeZ();
        final int sx = mc.pos.x;
        final int sy = mc.pos.y;
        final int sz = mc.pos.z;
        final var b = mc.blocks.orElseThrow();
        return () -> new Itr(cw, ch, cd, sx, sy, sz, b, b.capacity() / MapBlockBuffer.SIZE);
    }

    @RequiredArgsConstructor
    private static class Itr implements Iterator<MapBlock> {
        final int cw;
        final int ch;
        final int cd;
        final int sx;
        final int sy;
        final int sz;
        final DirectBuffer b;
        final int size;
        int i = 0;

        @Override
        public MapBlock next() {
            return readMapBlockIndex(b, 0, i++, cw, ch, cd, sx, sy, sz);
        }

        @Override
        public boolean hasNext() {
            return i < size;
        }
    }

    public static void forEachBlocks(MapChunk mc, Consumer<MapBlock> consumer) {
        final int cw = mc.pos.getSizeX();
        final int ch = mc.pos.getSizeY();
        final int cd = mc.pos.getSizeZ();
        final int sx = mc.pos.x;
        final int sy = mc.pos.y;
        final int sz = mc.pos.z;
        mc.blocks.ifPresent(b -> {
            for (int i = 0; i < b.capacity() / MapBlockBuffer.SIZE; i++) {
                consumer.accept(readMapBlockIndex(b, 0, i, cw, ch, cd, sx, sy, sz));
            }
        });
    }

    public static MapBlock getNeighbor(MapBlock mb, NeighboringDir dir, MapChunk c, int w, int h, int d,
            ObjectsGetter og) {
        final var dirpos = mb.pos.add(dir.pos);
        if (dirpos.isNegative()) {
            return null;
        }
        if (dirpos.isOutBounds(w, h, d)) {
            return null;
        }
        if (c.isInside(dirpos)) {
            final int off = GameChunkPos.calcIndex(c, dirpos.x, dirpos.y, dirpos.z) * MapBlockBuffer.SIZE;
            return MapBlockBuffer.read(c.getBlocks(), off, dirpos);
        } else {
            final var parent = getChunk(og, cid2Id(c.parent));
            return findBlock(parent, dirpos, og);
        }
    }

    public static MapBlock getNeighborNorth(MapBlock mb, MapChunk chunk, int w, int h, int d, ObjectsGetter og) {
        return getNeighbor(mb, NeighboringDir.N, chunk, w, h, d, og);
    }

    public static MapBlock getNeighborSouth(MapBlock mb, MapChunk chunk, int w, int h, int d, ObjectsGetter og) {
        return getNeighbor(mb, NeighboringDir.S, chunk, w, h, d, og);
    }

    public static MapBlock getNeighborEast(MapBlock mb, MapChunk chunk, int w, int h, int d, ObjectsGetter og) {
        return getNeighbor(mb, NeighboringDir.E, chunk, w, h, d, og);
    }

    public static MapBlock getNeighborWest(MapBlock mb, MapChunk chunk, int w, int h, int d, ObjectsGetter og) {
        return getNeighbor(mb, NeighboringDir.W, chunk, w, h, d, og);
    }

    public static MapBlock getNeighborUp(MapBlock mb, MapChunk chunk, int w, int h, int d, ObjectsGetter og) {
        return getNeighbor(mb, NeighboringDir.U, chunk, w, h, d, og);
    }

    public static boolean isNeighborsUpEmptyContinuously(MapBlock mb, MapChunk chunk, int w, int h, int d,
            ObjectsGetter og) {
        MapBlock up = getNeighbor(mb, NeighboringDir.U, chunk, w, h, d, og);
        while (up != null) {
            if (!up.isEmpty()) {
                return false;
            }
            if (mb.parent != up.parent) {
                chunk = getChunk(og, cid2Id(up.parent));
            }
            up = getNeighbor(up, NeighboringDir.U, chunk, w, h, d, og);
        }
        return true;
    }

    public static MapBlockResult getNeighbor(int index, NeighboringDir dir, MapChunk c, int w, int h, int d,
            ObjectsGetter og) {
        final var dirpos = fromIndex(index, c).add(dir.pos);
        if (dirpos.isNegative()) {
            return new MapBlockResult(c, -1);
        }
        if (dirpos.isOutBounds(w, h, d)) {
            return new MapBlockResult(c, -1);
        }
        if (c.isInside(dirpos)) {
            index = calcIndex(c, dirpos.x, dirpos.y, dirpos.z);
            return new MapBlockResult(c, index);
        } else {
            final var parent = getChunk(og, cid2Id(c.parent));
            final var res = findBlockIndex(parent, dirpos, og);
            return new MapBlockResult(res.c, res.index);
        }
    }

    public static MapBlockResult getNeighborNorth(int index, MapChunk chunk, int w, int h, int d, ObjectsGetter og) {
        return getNeighbor(index, NeighboringDir.N, chunk, w, h, d, og);
    }

    public static MapBlockResult getNeighborSouth(int index, MapChunk chunk, int w, int h, int d, ObjectsGetter og) {
        return getNeighbor(index, NeighboringDir.S, chunk, w, h, d, og);
    }

    public static MapBlockResult getNeighborEast(int index, MapChunk chunk, int w, int h, int d, ObjectsGetter og) {
        return getNeighbor(index, NeighboringDir.E, chunk, w, h, d, og);
    }

    public static MapBlockResult getNeighborWest(int index, MapChunk chunk, int w, int h, int d, ObjectsGetter og) {
        return getNeighbor(index, NeighboringDir.W, chunk, w, h, d, og);
    }

    public static MapBlockResult getNeighborUp(int index, MapChunk chunk, int w, int h, int d, ObjectsGetter og) {
        return getNeighbor(index, NeighboringDir.U, chunk, w, h, d, og);
    }

    public static boolean isNeighborsUpEmptyContinuously(int index, MapChunk chunk, int w, int h, int d,
            ObjectsGetter og) {
        var up = getNeighbor(index, NeighboringDir.U, chunk, w, h, d, og);
        while (up.isValid()) {
            if (!(PropertiesSet.get(getProp(up.c.getBlocks(), up.getOff()), EMPTY_POS))) {
                return false;
            }
            final int mbparent = MapBlockBuffer.getParent(chunk.getBlocks(), index * MapBlockBuffer.SIZE);
            final int upparent = MapBlockBuffer.getParent(up.c.getBlocks(), up.getOff());
            if (mbparent != upparent) {
                chunk = getChunk(og, cid2Id(upparent));
            }
            up = getNeighbor(up.index, NeighboringDir.U, chunk, w, h, d, og);
        }
        return true;
    }

}
