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

import static com.anrisoftware.dwarfhustle.model.api.objects.ExternalizableUtils.readExternalIntLongMap;
import static com.anrisoftware.dwarfhustle.model.api.objects.ExternalizableUtils.readExternalObjectLongMap;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.function.Function;

import org.eclipse.collections.api.map.primitive.IntLongMap;
import org.eclipse.collections.api.map.primitive.MutableIntLongMap;
import org.eclipse.collections.api.map.primitive.ObjectLongMap;
import org.eclipse.collections.impl.factory.primitive.IntLongMaps;
import org.eclipse.collections.impl.factory.primitive.ObjectLongMaps;
import org.eclipse.collections.impl.map.mutable.primitive.IntLongHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.ObjectLongHashMap;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Collection of map tile chunks and blocks.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class MapChunk extends GameMapObject implements StoredObject {

    private static final long serialVersionUID = 1L;

    public static final String OBJECT_TYPE = MapChunk.class.getSimpleName();

    public static final MapChunk getMapChunk(ObjectsGetter og, Object key) {
        return og.get(MapChunk.class, OBJECT_TYPE, key);
    }

    /**
     * Record ID set after the object was once stored in the backend.
     */
    public transient Serializable rid;

    /**
     * Map of GameChunkPos := ID of the children chunks.
     * <p>
     * Use a mutable map to use write and read external.
     */
    @ToString.Exclude
    public ObjectLongMap<GameChunkPos> chunks = ObjectLongMaps.mutable.empty();

    @ToString.Exclude
    public int chunkSize;

    /**
     * The {@link MapBlock}s in the chunk if the chunk is a leaf.
     * <p>
     * Use a mutable map to use write and read external.
     */
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public MapBlocksStore blocks;

    /**
     * Contains the IDs of the chunks in each direction that are neighboring this
     * chunk.
     *
     * @see NeighboringDir
     */
    @ToString.Exclude
    public IntLongMap chunkDir = IntLongMaps.mutable.empty();

    /**
     * True if this the root chunk.
     */
    public boolean root = false;

    /**
     * ID of the parent chunk.
     */
    public long parent;

    /**
     * The {@link CenterExtent} of the chunk.
     */
    @ToString.Exclude
    public CenterExtent centerExtent;

    public MapChunk() {
        this.pos = new GameChunkPos();
        this.centerExtent = new CenterExtent();
    }

    public MapChunk(long id, int chunkSize) {
        super(id);
        this.pos = new GameChunkPos();
        this.chunkSize = chunkSize;
        this.blocks = new MapBlocksStore(chunkSize);
    }

    public MapChunk(byte[] idbuf, int chunkSize) {
        super(idbuf);
        this.pos = new GameChunkPos();
        this.chunkSize = chunkSize;
        this.blocks = new MapBlocksStore(chunkSize);
    }

    public MapChunk(long id, GameChunkPos pos, int chunkSize) {
        super(id);
        this.chunkSize = chunkSize;
        this.blocks = new MapBlocksStore(chunkSize);
        this.pos = pos;
    }

    public MapChunk(byte[] idbuf, GameChunkPos pos, int chunkSize) {
        super(idbuf);
        this.chunkSize = chunkSize;
        this.blocks = new MapBlocksStore(chunkSize);
        this.pos = pos;
    }

    @Override
    public String getObjectType() {
        return OBJECT_TYPE;
    }

    /**
     * Updates the world coordinates center and extend of this chunk.
     */
    public void updateCenterExtent(float w, float h, float d) {
        float tx = -w + 2f * pos.x + getPos().getSizeX();
        float ty = h - 2f * pos.y - getPos().getSizeY();
        this.centerExtent = new CenterExtent(tx, ty, 0, getPos().getSizeX(), getPos().getSizeY(), getPos().getSizeZ());
    }

    @Override
    public GameChunkPos getPos() {
        return (GameChunkPos) pos;
    }

    public MapBlock getBlock(GameBlockPos pos) {
        return blocks.getBlock(pos);
    }

    public void setBlock(MapBlock block) {
        blocks.setBlock(block);
    }

    public void setBlocks(Iterable<MapBlock> blocks) {
        this.blocks.setBlocks(blocks);
    }

    public boolean haveBlock(GameBlockPos p) {
        return getPos().contains(p);
    }

    public boolean getBlocksNotEmpty() {
        return !blocks.isEmpty();
    }

    public void setNeighbor(NeighboringDir dir, long id) {
        var m = (MutableIntLongMap) chunkDir;
        m.put(dir.ordinal(), id);
    }

    public long getNeighbor(NeighboringDir dir) {
        return chunkDir.get(dir.ordinal());
    }

    public long getNeighborTop() {
        return chunkDir.get(NeighboringDir.U.ordinal());
    }

    public void setNeighborTop(long id) {
        setNeighbor(NeighboringDir.U, id);
    }

    public long getNeighborBottom() {
        return chunkDir.get(NeighboringDir.D.ordinal());
    }

    public void setNeighborBottom(long id) {
        setNeighbor(NeighboringDir.D, id);
    }

    public long getNeighborSouth() {
        return chunkDir.get(NeighboringDir.S.ordinal());
    }

    public void setNeighborSouth(long id) {
        setNeighbor(NeighboringDir.S, id);
    }

    public long getNeighborEast() {
        return chunkDir.get(NeighboringDir.E.ordinal());
    }

    public void setNeighborEast(long id) {
        setNeighbor(NeighboringDir.E, id);
    }

    public long getNeighborSouthEast() {
        return chunkDir.get(NeighboringDir.SE.ordinal());
    }

    public void setNeighborSouthEast(long id) {
        setNeighbor(NeighboringDir.SE, id);
    }

    public long getNeighborNorth() {
        return chunkDir.get(NeighboringDir.N.ordinal());
    }

    public void setNeighborNorth(long id) {
        setNeighbor(NeighboringDir.N, id);
    }

    public long getNeighborWest() {
        return chunkDir.get(NeighboringDir.W.ordinal());
    }

    public void setNeighborWest(long id) {
        setNeighbor(NeighboringDir.W, id);
    }

    public long getNeighborSouthWest() {
        return chunkDir.get(NeighboringDir.SW.ordinal());
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
        return blocks.getBlock(pos);
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
        super.writeExternal(out);
        out.writeInt(chunkSize);
        blocks.writeExternal(out);
        centerExtent.writeExternal(out);
        ((IntLongHashMap) chunkDir).writeExternal(out);
        ((ObjectLongHashMap<GameChunkPos>) chunks).writeExternal(out);
        out.writeLong(parent);
        out.writeBoolean(root);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        this.chunkSize = in.readInt();
        this.blocks = new MapBlocksStore(chunkSize);
        blocks.readExternal(in);
        this.centerExtent.readExternal(in);
        this.chunkDir = readExternalIntLongMap(in);
        this.chunks = readExternalObjectLongMap(in, GameChunkPos::new);
        this.parent = in.readLong();
        this.root = in.readBoolean();
    }

}
