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

import java.util.Objects;
import java.util.function.Function;

import org.eclipse.collections.api.map.MapIterable;
import org.eclipse.collections.api.map.primitive.IntLongMap;
import org.eclipse.collections.api.map.primitive.MutableIntLongMap;
import org.eclipse.collections.api.map.primitive.ObjectLongMap;
import org.eclipse.collections.impl.factory.Maps;
import org.eclipse.collections.impl.factory.primitive.IntLongMaps;
import org.eclipse.collections.impl.factory.primitive.ObjectLongMaps;

import lombok.EqualsAndHashCode;
import lombok.Getter;
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
@Getter
public class MapChunk extends StoredObject {

    private static final long serialVersionUID = 1L;

    public static final String OBJECT_TYPE = MapChunk.class.getSimpleName();

    private ObjectLongMap<GameChunkPos> chunks = ObjectLongMaps.immutable.empty();

    private MapIterable<GameBlockPos, MapBlock> blocks = Maps.immutable.empty();

    private GameChunkPos pos;

    private boolean root = false;

    /**
     * Contains the IDs of the chunks in each direction that are neighboring this
     * chunk.
     */
    private IntLongMap chunkDir = IntLongMaps.mutable.empty();

    /**
     * ID of the parent chunk.
     */
    private long parent;

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

    public void setChunks(ObjectLongMap<GameChunkPos> chunks) {
        this.chunks = chunks;
        setDirty(true);
    }

    public void setBlocks(MapIterable<GameBlockPos, MapBlock> blocks) {
        this.blocks = blocks;
        setDirty(true);
    }

    /**
     * Sets the X, Y and Z start position and end position of a {@link MapChunk} on
     * the game map.
     */
    public void setPos(GameChunkPos pos) {
        if (!Objects.equals(this.pos, pos)) {
            setDirty(true);
            this.pos = pos;
        }
    }

    public int getMapId() {
        return pos.mapid;
    }

    /**
     * Returns the start {@link GameBlockPos} position of the chunk.
     */
    public GameBlockPos getSp() {
        return pos;
    }

    /**
     * Returns the end {@link GameBlockPos} position of the chunk.
     */
    public GameBlockPos getEp() {
        return pos.getEp();
    }

    public float getWidth() {
        return getEp().getDiffX(getSp());
    }

    public float getHeight() {
        return getEp().getDiffY(getSp());
    }

    public float getDepth() {
        return getEp().getDiffZ(getSp());
    }

    /**
     * Sets that this block is the top most block.
     */
    public void setRoot(boolean root) {
        if (this.root != root) {
            setDirty(true);
            this.root = root;
        }
    }

    public void setParent(long parent) {
        if (this.parent != parent) {
            this.parent = parent;
            setDirty(true);
        }
    }

    public void setNeighbor(MapChunkDir dir, long id) {
        var m = (MutableIntLongMap) chunkDir;
        m.put(dir.ordinal(), id);
        setDirty(true);
    }

    public long getNeighbor(MapChunkDir dir) {
        return chunkDir.get(dir.ordinal());
    }

    public long getNeighborTop() {
        return chunkDir.get(MapChunkDir.T.ordinal());
    }

    public void setNeighborTop(long id) {
        setNeighbor(MapChunkDir.T, id);
    }

    public long getNeighborBottom() {
        return chunkDir.get(MapChunkDir.B.ordinal());
    }

    public void setNeighborBottom(long id) {
        setNeighbor(MapChunkDir.B, id);
    }

    public long getNeighborSouth() {
        return chunkDir.get(MapChunkDir.S.ordinal());
    }

    public void setNeighborSouth(long id) {
        setNeighbor(MapChunkDir.S, id);
    }

    public long getNeighborEast() {
        return chunkDir.get(MapChunkDir.E.ordinal());
    }

    public void setNeighborEast(long id) {
        setNeighbor(MapChunkDir.E, id);
    }

    public long getNeighborNorth() {
        return chunkDir.get(MapChunkDir.N.ordinal());
    }

    public void setNeighborNorth(long id) {
        setNeighbor(MapChunkDir.N, id);
    }

    public long getNeighborWest() {
        return chunkDir.get(MapChunkDir.W.ordinal());
    }

    public void setNeighborWest(long id) {
        setNeighbor(MapChunkDir.W, id);
    }

    public MapChunk findMapChunk(int x, int y, int z, Function<Long, MapChunk> retriever) {
        return findMapChunk(new GameBlockPos(pos.getMapid(), x, y, z), retriever);
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
        return findMapBlock(new GameBlockPos(pos.getMapid(), x, y, z), retriever);
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
            long id = chunks.get(new GameChunkPos(getMapId(), x, y, z, ex, ey, ez));
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
