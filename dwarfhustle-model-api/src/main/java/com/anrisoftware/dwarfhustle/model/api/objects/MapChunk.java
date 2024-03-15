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

import static com.anrisoftware.dwarfhustle.model.api.objects.ExternalizableUtils.readExternalObjectLongMap;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.collections.api.factory.primitive.ObjectLongMaps;
import org.eclipse.collections.api.map.primitive.MutableObjectLongMap;
import org.eclipse.collections.impl.map.mutable.primitive.ObjectLongHashMap;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.ToString;

/**
 * Collection of map tile chunks and blocks.
 * <p>
 * Size 537 bytes without blocks.
 * <ul>
 * <li>8(id)
 * <li>8(parent)
 * <li>24(pos)
 * <li>4(chunkSize)
 * <li>24(centerExtent)
 * <li>26*8(dir)
 * <li>4+8*24+8*8(chunks)
 * <li>1(haveBlocks)
 * <li>chunkSize*chunkSize*chunkSize*512(blocks)
 * </ul>
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
public class MapChunk implements Externalizable, StreamStorage {

    private static final long serialVersionUID = 1L;

    public static final String OBJECT_TYPE = MapChunk.class.getSimpleName();

    public static final long ID_FLAG = 2;

    public static final int SIZE = 537;

    /**
     * Marker that the neighbor in the direction is empty.
     */
    public static final long DIR_EMPTY = -1;

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
     * Serial object ID of the chunk, beginning with 0 for the root chunk and
     * numbering the child chunks in clockwise order.
     */
    @EqualsAndHashCode.Include
    public long id;

    /**
     * ID of the parent chunk. 0 if this is the root chunk.
     */
    public long parent;

    /**
     * The {@link GameChunkPos} of the chunk.
     */
    public GameChunkPos pos;

    @ToString.Exclude
    public int chunkSize;

    /**
     * The {@link CenterExtent} of the chunk.
     */
    @ToString.Exclude
    public CenterExtent centerExtent;

    /**
     * Contains the IDs of the chunks in each direction that are neighboring this
     * chunk. The size is always 26. Empty directions are marked with
     * {@link #DIR_EMPTY}.
     *
     * @see NeighboringDir
     */
    @ToString.Exclude
    public long[] dir = new long[26];

    /**
     * Map of {@link GameChunkPos} := ID of the children chunks.
     * <p>
     * Use a mutable map to use write and read external.
     */
    @ToString.Exclude
    public MutableObjectLongMap<GameChunkPos> chunks = ObjectLongMaps.mutable.empty();

    /**
     * The {@link MapBlock}s in the chunk if the chunk is a leaf.
     * <p>
     * Use a mutable map to use write and read external.
     */
    @ToString.Exclude
    public Optional<MapBlocksStore> blocks;

    @ToString.Exclude
    private Consumer<ObjectOutput> writeExternalBlock = (o) -> {
    };

    @ToString.Exclude
    private Consumer<ObjectInput> readExternalBlock = (i) -> {
    };

    @ToString.Exclude
    private Consumer<DataOutput> writeStreamBlock = (o) -> {
    };

    @ToString.Exclude
    private Consumer<DataInput> readStreamBlock = (i) -> {
    };

    public MapChunk() {
        this.pos = new GameChunkPos();
        this.centerExtent = new CenterExtent();
    }

    public MapChunk(long cid, int chunkSize, GameChunkPos pos) {
        this.id = cid2Id(cid);
        this.chunkSize = chunkSize;
        this.pos = pos;
        boolean leaf = pos.getSizeX() <= chunkSize;
        if (leaf) {
            this.blocks = Optional.of(new MapBlocksStore(chunkSize));
            this.writeExternalBlock = this::writeExternalBlock;
            this.readExternalBlock = this::readExternalBlock;
            this.writeStreamBlock = this::writeStreamBlock;
            this.readStreamBlock = this::readStreamBlock;
        } else {
            this.blocks = Optional.empty();
        }
    }

    public long getCid() {
        return id2Cid(id);
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

    public MapBlock getBlock(GameBlockPos pos) {
        return blocks.orElseThrow().getBlock(pos);
    }

    public void setBlock(MapBlock block) {
        blocks.orElseThrow().setBlock(block);
    }

    public void setBlocks(Iterable<MapBlock> blocks) {
        this.blocks.orElseThrow().setBlocks(blocks);
    }

    public boolean haveBlock(GameBlockPos p) {
        return getPos().contains(p);
    }

    public boolean getBlocksNotEmpty() {
        return !blocks.isEmpty();
    }

    public void setNeighbor(NeighboringDir dir, long id) {
        this.dir[dir.ordinal()] = id;
    }

    public long getNeighbor(NeighboringDir dir) {
        return this.dir[dir.ordinal()];
    }

    public long getNeighborTop() {
        return dir[NeighboringDir.U.ordinal()];
    }

    public void setNeighborTop(long id) {
        setNeighbor(NeighboringDir.U, id);
    }

    public long getNeighborBottom() {
        return dir[NeighboringDir.D.ordinal()];
    }

    public void setNeighborBottom(long id) {
        setNeighbor(NeighboringDir.D, id);
    }

    public long getNeighborSouth() {
        return dir[NeighboringDir.S.ordinal()];
    }

    public void setNeighborSouth(long id) {
        setNeighbor(NeighboringDir.S, id);
    }

    public long getNeighborEast() {
        return dir[NeighboringDir.E.ordinal()];
    }

    public void setNeighborEast(long id) {
        setNeighbor(NeighboringDir.E, id);
    }

    public long getNeighborSouthEast() {
        return dir[NeighboringDir.SE.ordinal()];
    }

    public void setNeighborSouthEast(long id) {
        setNeighbor(NeighboringDir.SE, id);
    }

    public long getNeighborNorth() {
        return dir[NeighboringDir.N.ordinal()];
    }

    public void setNeighborNorth(long id) {
        setNeighbor(NeighboringDir.N, id);
    }

    public long getNeighborWest() {
        return dir[NeighboringDir.W.ordinal()];
    }

    public void setNeighborWest(long id) {
        setNeighbor(NeighboringDir.W, id);
    }

    public long getNeighborSouthWest() {
        return dir[NeighboringDir.SW.ordinal()];
    }

    public void setNeighborSouthWest(long id) {
        setNeighbor(NeighboringDir.SW, id);
    }

    public MapChunk findMapChunk(int x, int y, int z, Function<Long, MapChunk> retriever) {
        return findMapChunk(new GameBlockPos(x, y, z), retriever);
    }

    public MapChunk findMapChunk(GameBlockPos pos, Function<Long, MapChunk> retriever) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        if (blocks.isEmpty()) {
            for (GameChunkPos b : chunks.keysView()) {
                int bx = b.getX();
                int by = b.getY();
                int bz = b.getZ();
                var ep = b.getEp();
                int ebx = ep.getX();
                int eby = ep.getY();
                int ebz = ep.getZ();
                if (x >= bx && y >= by && z >= bz && x < ebx && y < eby && z < ebz) {
                    long id = chunks.get(b);
                    var mb = retriever.apply(id);
                    return mb.findMapChunk(pos, retriever);
                }
            }
        }
        return this;
    }

    public MapBlock findMapBlock(int x, int y, int z, Function<Long, MapChunk> retriever) {
        return findMapBlock(new GameBlockPos(x, y, z), retriever);
    }

    public MapBlock findMapBlock(GameBlockPos pos, Function<Long, MapChunk> retriever) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        if (blocks.isEmpty()) {
            for (GameChunkPos b : chunks.keysView()) {
                int bx = b.getX();
                int by = b.getY();
                int bz = b.getZ();
                var ep = b.getEp();
                int ebx = ep.getX();
                int eby = ep.getY();
                int ebz = ep.getZ();
                if (x >= bx && y >= by && z >= bz && x < ebx && y < eby && z < ebz) {
                    long id = chunks.get(b);
                    var mb = retriever.apply(id);
                    return mb.findMapBlock(pos, retriever);
                }
            }
        }
        return blocks.orElseThrow().getBlock(pos);
    }

    /**
     * Finds the child chunk with the start and end coordinates.
     *
     * @return the ID of the chunk or 0.
     */
    public long findChild(int x, int y, int z, int ex, int ey, int ez, Function<Long, MapChunk> retriever) {
        if (x < 0 || y < 0 || z < 0) {
            return 0;
        }
        if (blocks.isEmpty()) {
            long id = chunks.get(new GameChunkPos(x, y, z, ex, ey, ez));
            if (id != 0) {
                return id;
            }
            for (GameChunkPos b : chunks.keysView()) {
                int bx = b.getX();
                int by = b.getY();
                int bz = b.getZ();
                var ep = b.getEp();
                int ebx = ep.getX();
                int eby = ep.getY();
                int ebz = ep.getZ();
                if (x >= bx && y >= by && z >= bz && x < ebx && y < eby && z < ebz) {
                    id = chunks.get(b);
                    var mb = retriever.apply(id);
                    return mb.findChild(x, y, z, ex, ey, ez, retriever);
                }
            }
        }
        return 0;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(id);
        out.writeLong(parent);
        pos.writeExternal(out);
        out.writeInt(chunkSize);
        centerExtent.writeExternal(out);
        for (long id : dir) {
            out.writeLong(id);
        }
        ((ObjectLongHashMap<GameChunkPos>) chunks).writeExternal(out);
        writeExternalBlock.accept(out);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.id = in.readLong();
        this.parent = in.readLong();
        pos.readExternal(in);
        this.chunkSize = in.readInt();
        this.centerExtent.readExternal(in);
        for (int i = 0; i < dir.length; i++) {
            this.dir[i] = in.readLong();
        }
        this.chunks = readExternalObjectLongMap(in);
        readExternalBlock.accept(in);
    }

    @Override
    public void writeStream(DataOutput out) throws IOException {
        out.writeLong(id);
        out.writeLong(parent);
        pos.writeStream(out);
        out.writeInt(chunkSize);
        centerExtent.writeStream(out);
        for (long id : dir) {
            out.writeLong(id);
        }
        out.writeInt(chunks.size());
        for (var v : chunks.keyValuesView()) {
            v.getOne().writeStream(out);
            out.writeLong(v.getTwo());
        }
        writeStreamBlock.accept(out);
    }

    @Override
    public void readStream(DataInput in) throws IOException {
        this.id = in.readLong();
        this.parent = in.readLong();
        pos.readStream(in);
        this.chunkSize = in.readInt();
        this.centerExtent.readStream(in);
        for (int i = 0; i < dir.length; i++) {
            this.dir[i] = in.readLong();
        }
        int size = in.readInt();
        this.chunks = ObjectLongMaps.mutable.ofInitialCapacity(size);
        for (int i = 0; i < size; i++) {
            var p = new GameChunkPos();
            p.readStream(in);
            this.chunks.put(pos, in.readLong());
        }
        readStreamBlock.accept(in);
    }

    @SneakyThrows
    private void writeExternalBlock(ObjectOutput out) {
        blocks.get().writeExternal(out);
    }

    @SneakyThrows
    private void readExternalBlock(ObjectInput in) {
        blocks.get().readExternal(in);
    }

    @SneakyThrows
    private void writeStreamBlock(DataOutput out) {
        blocks.get().writeStream(out);
    }

    @SneakyThrows
    private void readStreamBlock(DataInput in) {
        blocks.get().readStream(in);
    }

}
