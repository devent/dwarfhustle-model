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
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.collections.api.factory.primitive.IntObjectMaps;
import org.eclipse.collections.api.map.primitive.IntObjectMap;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;

import lombok.Data;
import lombok.EqualsAndHashCode;
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
    private MutableIntObjectMap<GameChunkPos> chunks = IntObjectMaps.mutable.empty();

    /**
     * Optionally, the {@link MapBlock}s in the chunk if the chunk is a leaf.
     */
    public Optional<ByteBuffer> blocks = Optional.empty();

    /**
     * The {@link CenterExtent} of the chunk.
     */
    public CenterExtent centerExtent;

    private boolean leaf;

    public MapChunk() {
        this.pos = new GameChunkPos();
        this.centerExtent = new CenterExtent();
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
     * Returns the chunk ID.
     */
    public long getId() {
        return cid2Id(cid);
    }

    /**
     * Updates the world coordinates center and extend of this chunk.
     */
    public void updateCenterExtent(float w, float h, float d) {
        float tx = -w + 2f * pos.x + getPos().getSizeX();
        float ty = h - 2f * pos.y - getPos().getSizeY();
        this.centerExtent = new CenterExtent(tx, ty, 0, getPos().getSizeX(), getPos().getSizeY(), getPos().getSizeZ());
    }

    public boolean isRoot() {
        return parent == 0;
    }

    public long getChunk(int x, int y, int z, int ex, int ey, int ez) {
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

    public ByteBuffer getBlocks() {
        return blocks.orElseThrow();
    }

    public MapBlock getBlock(GameBlockPos pos) {
        var buffer = blocks.orElseThrow();
        return readMapBlock(buffer, pos.x, pos.y, pos.z);
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
                consumer.accept(MapBlockBuffer.readMapBlockIndex(b, 0, i, cw, ch, sx, sy, sz));
            }
        });
    }

    private MapBlock readMapBlock(ByteBuffer buffer, int x, int y, int z) {
        int w = pos.getSizeX();
        int h = pos.getSizeY();
        int d = pos.getSizeZ();
        return MapBlockBuffer.readMapBlockIndex(buffer, 0, MapBlockBuffer.calcIndex(w, h, d, x, y, z), w, h, pos.ep.x,
                pos.ep.y, pos.ep.z);
    }

    private void writeMapBlock(ByteBuffer buffer, MapBlock block) {
        MapBlockBuffer.writeMapBlockIndex(buffer, 0, block, pos.getSizeX(), pos.getSizeY(), pos.getSizeZ());
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

    public MapChunk findMapChunk(int x, int y, int z, Function<Integer, MapChunk> retriever) {
        return findMapChunk(new GameBlockPos(x, y, z), retriever);
    }

    public MapChunk findMapChunk(GameBlockPos pos, Function<Integer, MapChunk> retriever) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
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
                    return mb.findMapChunk(pos, retriever);
                }
            }
        }
        return this;
    }

    public MapBlock findMapBlock(int x, int y, int z, Function<Integer, MapChunk> retriever) {
        return findMapBlock(new GameBlockPos(x, y, z), retriever);
    }

    public MapBlock findMapBlock(GameBlockPos pos, Function<Integer, MapChunk> retriever) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
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
                    return mb.findMapBlock(pos, retriever);
                }
            }
        }
        return getBlock(pos);
    }

    /**
     * Finds the child chunk with the start and end coordinates.
     *
     * @return the ID of the chunk or 0.
     */
    public long findChild(int x, int y, int z, int ex, int ey, int ez, Function<Integer, MapChunk> retriever) {
        if (x < 0 || y < 0 || z < 0) {
            return 0;
        }
        if (blocks.isEmpty()) {
            long id = findChunk(x, y, z, ex, ey, ez);
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

    private long findChunk(int x, int y, int z, int ex, int ey, int ez) {
        for (var view : chunks.keyValuesView()) {
            if (view.getTwo().equals(x, y, z, ex, ey, ez)) {
                return view.getOne();
            }
        }
        return 0;
    }

}
