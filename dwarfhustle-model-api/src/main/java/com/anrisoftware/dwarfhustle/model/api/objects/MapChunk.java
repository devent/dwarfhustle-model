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

import static com.anrisoftware.dwarfhustle.model.api.objects.MapBlockBuffer.calcIndex;
import static com.anrisoftware.dwarfhustle.model.api.objects.MapBlockBuffer.readMapBlockIndex;
import static com.anrisoftware.dwarfhustle.model.api.objects.MapBlockBuffer.writeMapBlockIndex;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.collections.api.factory.primitive.IntObjectMaps;
import org.eclipse.collections.api.map.primitive.IntObjectMap;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Collection of map tile chunks and blocks.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Data
public class MapChunk {

    public static final long ID_FLAG = 2;

    /**
     * Returns the game object ID from the chunk ID.
     */
    public static long cid2Id(long tid) {
        return (tid << 32) | ID_FLAG;
    }

    /**
     * Returns the chunk ID from the game object ID.
     */
    public static long id2Cid(long id) {
        return (id >> 32);
    }

    /**
     * Serial CID of the chunk, beginning with 0 for the root chunk and numbering
     * the child chunks in clockwise order.
     */
    @EqualsAndHashCode.Include
    @ToString.Include
    public int cid;

    /**
     * CID of the parent chunk. 0 if this is the root chunk.
     */
    public int parent;

    /**
     * The {@link GameChunkPos} of the chunk.
     */
    @ToString.Include
    public GameChunkPos pos;

    public int chunkSize;

    /**
     * The {@link GameChunkPos} and CIDs of the children chunks.
     */
    private final MutableIntObjectMap<GameChunkPos> chunks = IntObjectMaps.mutable.empty();

    /**
     * Optionally, the {@link MapBlock}s {@link ByteBuffer} in the chunk if the
     * chunk is a leaf.
     */
    public Optional<ByteBuffer> blocks = Optional.empty();

    /**
     * The chunk CIDs of {@link NeighboringDir} neighbors of this chunk.
     */
    public int[] neighbors = new int[NeighboringDir.values().length];

    /**
     * True if the chunk is a leaf with blocks.
     */
    @ToString.Include
    private boolean leaf;

    /**
     * Pre-calculated {@link CenterExtent} based on the chunks position.
     */
    private CenterExtent centerExtent;

    public boolean changed = false;

    public MapChunk() {
        this.pos = new GameChunkPos();
    }

    public MapChunk(int cid, int parent, int chunkSize, GameChunkPos pos) {
        this.cid = cid;
        this.parent = parent;
        this.chunkSize = chunkSize;
        this.pos = pos;
        this.leaf = calcLeaf();
    }

    private boolean calcLeaf() {
        return pos.getSizeX() <= chunkSize && pos.getSizeY() <= chunkSize && pos.getSizeZ() <= chunkSize;
    }

    /**
     * Pre-calculates the {@link CenterExtent} of the chunk. It is used to calculate
     * the bounding box around the chunk.
     */
    public void updateCenterExtent(float w, float h) {
        float tx = -w + 2f * pos.x + pos.getSizeX();
        float ty = h - 2f * pos.y - pos.getSizeY();
        float centerx = tx;
        float centery = ty;
        float centerz = 0f;
        float extentx = pos.getSizeX();
        float extenty = pos.getSizeY();
        float extentz = pos.getSizeZ();
        this.centerExtent = new CenterExtent(centerx, centery, centerz, extentx, extenty, extentz);
    }

    /**
     * Returns the chunk ID.
     */
    public long getId() {
        return cid2Id(cid);
    }

    public boolean isRoot() {
        return parent == 0;
    }

    public boolean isInside(GameBlockPos other) {
        return isInside(other.x, other.y, other.z);
    }

    public boolean isInside(int x, int y, int z) {
        return x >= pos.x && y >= pos.y && z >= pos.z && x < pos.ep.x && y < pos.ep.y && z < pos.ep.z;
    }

    public int getChunk(int x, int y, int z, int ex, int ey, int ez) {
        for (var view : chunks.keyValuesView()) {
            if (view.getTwo().equals(x, y, z, ex, ey, ez)) {
                return view.getOne();
            }
        }
        return 0;
    }

    public void setChunks(IntObjectMap<GameChunkPos> chunks) {
        this.chunks.putAll(chunks);
    }

    public int getChunksCount() {
        return chunks.size();
    }

    public ByteBuffer getBlocksBuffer() {
        return blocks.orElseThrow();
    }

    public Iterable<MapBlock> getBlocks() {
        int cw = pos.getSizeX();
        int ch = pos.getSizeY();
        int sx = pos.x;
        int sy = pos.y;
        int sz = pos.z;
        var b = blocks.orElseThrow();
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
            return MapBlockBuffer.readMapBlockIndex(b, 0, i++, cw, ch, sx, sy, sz);
        }

        @Override
        public boolean hasNext() {
            return i < size;
        }
    }

    public MapBlock getBlock(int x, int y, int z) {
        var buffer = blocks.orElseThrow();
        return readMapBlock(buffer, x, y, z);
    }

    public MapBlock getBlock(GameBlockPos pos) {
        return getBlock(pos.x, pos.y, pos.z);
    }

    public void setBlock(MapBlock block) {
        var buffer = blocks.orElseThrow();
        writeMapBlock(buffer, block);
    }

    public void setBlocks(Iterable<MapBlock> blocks) {
        var buffer = this.blocks.orElseThrow();
        for (MapBlock block : blocks) {
            writeMapBlock(buffer, block);
        }
    }

    public void forEachBlocks(Consumer<MapBlock> consumer) {
        int cw = pos.getSizeX();
        int ch = pos.getSizeY();
        int sx = pos.x;
        int sy = pos.y;
        int sz = pos.z;
        blocks.ifPresent((b) -> {
            for (int i = 0; i < b.capacity() / MapBlockBuffer.SIZE; i++) {
                consumer.accept(readMapBlockIndex(b, 0, i, cw, ch, sx, sy, sz));
            }
        });
    }

    private MapBlock readMapBlock(ByteBuffer buffer, int x, int y, int z) {
        int w = pos.getSizeX();
        int h = pos.getSizeY();
        int d = pos.getSizeZ();
        int i = calcIndex(w, h, d, pos.x, pos.y, pos.z, x, y, z);
        return readMapBlockIndex(buffer, 0, i, w, h, pos.x, pos.y, pos.z);
    }

    private void writeMapBlock(ByteBuffer buffer, MapBlock block) {
        writeMapBlockIndex(buffer, 0, block, pos.getSizeX(), pos.getSizeY(), pos.getSizeZ(), pos.x, pos.y, pos.z);
    }

    public boolean haveBlock(GameBlockPos p) {
        return getPos().contains(p);
    }

    public boolean isBlocksNotEmpty() {
        return !blocks.isEmpty();
    }

    public boolean isBlocksEmpty() {
        return blocks.isEmpty();
    }

    public void setBlocksBuffer(ByteBuffer buffer) {
        this.blocks = Optional.of(buffer);
    }

    public MapChunk findChunk(GameBlockPos pos, Function<Integer, MapChunk> retriever) {
        return findChunk(pos.x, pos.y, pos.z, retriever);
    }

    public MapChunk findChunk(int x, int y, int z, Function<Integer, MapChunk> retriever) {
        if (blocks.isEmpty()) {
            for (var view : chunks.keyValuesView()) {
                var b = view.getTwo();
                int bx = b.getX();
                int by = b.getY();
                int bz = b.getZ();
                var ep = b.getEp();
                int ebx = ep.getX();
                int eby = ep.getY();
                int ebz = ep.getZ();
                if (x >= bx && y >= by && z >= bz && x < ebx && y < eby && z < ebz) {
                    var mb = retriever.apply(view.getOne());
                    return mb.findChunk(pos, retriever);
                }
            }
        }
        return this;
    }

    public MapBlock findBlock(int x, int y, int z, Function<Integer, MapChunk> retriever) {
        return findBlock(new GameBlockPos(x, y, z), retriever);
    }

    public MapBlock findBlock(GameBlockPos pos, Function<Integer, MapChunk> retriever) {
        if (blocks.isEmpty()) {
            if (!isInside(pos)) {
                if (cid == 0) {
                    return null;
                }
                var parent = retriever.apply(this.parent);
                return parent.findBlock(pos, retriever);
            }
            for (var view : chunks.keyValuesView()) {
                var b = view.getTwo();
                if (b.contains(pos)) {
                    var mb = retriever.apply(view.getOne());
                    return mb.findBlock(pos, retriever);
                }
            }
            return null;
        }
        return getBlock(pos);
    }

    /**
     * Finds the child chunk with the start and end coordinates.
     *
     * @return the ID of the chunk or 0.
     */
    public int findChild(int x, int y, int z, int ex, int ey, int ez, Function<Integer, MapChunk> retriever) {
        if (x < 0 || y < 0 || z < 0) {
            return 0;
        }
        if (blocks.isEmpty()) {
            int id = findChunk(x, y, z, ex, ey, ez);
            if (id != 0) {
                return id;
            }
            for (var view : chunks.keyValuesView()) {
                var b = view.getTwo();
                int bx = b.getX();
                int by = b.getY();
                int bz = b.getZ();
                var ep = b.getEp();
                int ebx = ep.getX();
                int eby = ep.getY();
                int ebz = ep.getZ();
                if (x >= bx && y >= by && z >= bz && x < ebx && y < eby && z < ebz) {
                    var mb = retriever.apply(view.getOne());
                    return mb.findChild(x, y, z, ex, ey, ez, retriever);
                }
            }
        }
        return 0;
    }

    private int findChunk(int x, int y, int z, int ex, int ey, int ez) {
        for (var view : chunks.keyValuesView()) {
            if (view.getTwo().equals(x, y, z, ex, ey, ez)) {
                return view.getOne();
            }
        }
        return 0;
    }

    /**
     * Returns the CID of the {@link MapChunk} in the direction of the
     * {@link NeighboringDir} or 0.
     */
    public int getNeighbor(NeighboringDir dir) {
        return neighbors[dir.ordinal()];
    }

    public int getNeighborEast() {
        return getNeighbor(NeighboringDir.E);
    }

    public int getNeighborSouth() {
        return getNeighbor(NeighboringDir.S);
    }

    public int getNeighborDown() {
        return getNeighbor(NeighboringDir.D);
    }

}
