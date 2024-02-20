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

import java.io.Serializable;
import java.util.function.Function;

import org.eclipse.collections.api.map.primitive.IntLongMap;
import org.eclipse.collections.api.map.primitive.MutableIntLongMap;
import org.eclipse.collections.api.map.primitive.ObjectLongMap;
import org.eclipse.collections.impl.factory.primitive.IntLongMaps;
import org.eclipse.collections.impl.factory.primitive.ObjectLongMaps;
import org.eclipse.collections.impl.map.mutable.primitive.ObjectLongHashMap;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Collection of map tile chunks and blocks.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@NoArgsConstructor
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
    public Serializable rid;

    @ToString.Exclude
    public ObjectLongMap<GameChunkPos> chunks = ObjectLongMaps.immutable.empty();

    @ToString.Exclude
    public ObjectLongMap<GameBlockPos> blocks = ObjectLongMaps.immutable.empty();

    /**
     * Contains the IDs of the chunks in each direction that are neighboring this
     * chunk.
     *
     * @see NeighboringDir
     */
    @ToString.Exclude
    public IntLongMap chunkDir = IntLongMaps.mutable.empty();

    public boolean root = false;

    /**
     * ID of the parent chunk.
     */
    public long parent;

    @ToString.Exclude
    public CenterExtent centerExtent;

    public MapChunk(long id) {
        super(id);
    }

    public MapChunk(byte[] idbuf) {
        super(idbuf);
    }

    public MapChunk(long id, GameChunkPos pos) {
        super(id);
        this.pos = pos;
    }

    public MapChunk(byte[] idbuf, GameChunkPos pos) {
        super(idbuf);
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

    public long getBlock(GameBlockPos pos) {
        return blocks.get(pos);
    }

    public boolean haveBlock(GameBlockPos pos) {
        return blocks.containsKey(pos);
    }

    public long getBlocksEmptyValue() {
        return ObjectLongHashMap.EMPTY_VALUE;
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

    public long findMapBlock(int x, int y, int z, Function<Long, MapChunk> retriever) {
        return findMapBlock(new GameBlockPos(x, y, z), retriever);
    }

    public long findMapBlock(GameBlockPos pos, Function<Long, MapChunk> retriever) {
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
        return blocks.get(pos);
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

}
