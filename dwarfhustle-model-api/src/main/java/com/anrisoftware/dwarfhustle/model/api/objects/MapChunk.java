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

import java.util.function.Function;

import org.eclipse.collections.api.map.MapIterable;
import org.eclipse.collections.api.map.primitive.ObjectLongMap;
import org.eclipse.collections.impl.factory.Maps;
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

    private GameChunkPos pos = new GameChunkPos();

    private boolean root = false;

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
        if (!this.pos.equals(pos)) {
            setDirty(true);
            this.pos = pos;
        }
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
}
