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

import static java.nio.ByteBuffer.allocateDirect;

import java.util.Optional;

import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
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
    public static long cid2Id(int cid) {
        return (cid << 32) | ID_FLAG;
    }

    /**
     * Returns the chunk ID from the game object ID.
     */
    public static int id2Cid(long id) {
        return (int) (id >> 32);
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

    /**
     * The {@link MapBlock}s {@link MutableDirectBuffer} in the chunk if the chunk
     * is a leaf.
     */
    public Optional<MutableDirectBuffer> blocks;

    public boolean changed = false;

    public MapChunk() {
        this.pos = new GameChunkPos();
    }

    public MapChunk(int cid, int parent, int cs, GameChunkPos pos) {
        this.cid = cid;
        this.parent = parent;
        this.chunkSize = cs;
        this.pos = pos;
        this.leaf = calcLeaf();
        if (leaf) {
            this.blocks = Optional
                    .of(new UnsafeBuffer(allocateDirect(pos.getSizeX() * pos.getSizeY() * pos.getSizeZ() * 14)));
        } else {
            this.blocks = Optional.empty();
        }
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

    public boolean haveBlock(GameBlockPos p) {
        return getPos().contains(p);
    }

    public MutableDirectBuffer getBlocks() {
        return blocks.orElseThrow();
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
