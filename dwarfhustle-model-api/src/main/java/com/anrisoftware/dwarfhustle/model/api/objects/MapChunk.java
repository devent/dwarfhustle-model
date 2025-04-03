/*
 * dwarfhustle-model-api - Manages the compile dependencies for the model.
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
package com.anrisoftware.dwarfhustle.model.api.objects;

import static java.nio.ByteBuffer.allocateDirect;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.function.Function;

import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.eclipse.collections.api.factory.primitive.LongObjectMaps;
import org.eclipse.collections.api.map.primitive.LongObjectMap;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Collection of map tile chunks and blocks.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MapChunk extends GameObject {

    public static final long ID_FLAG = 2;

    public static final int OBJECT_TYPE = MapChunk.class.getSimpleName().hashCode();

    public static Function<Long, MapChunk> getMapChunkRetriever(ObjectsGetter og) {
        return id -> og.get(OBJECT_TYPE, id);
    }

    public static MapChunk getChunk(ObjectsGetter og, long id) {
        return og.get(MapChunk.OBJECT_TYPE, id);
    }

    public static void setChunk(ObjectsSetter os, MapChunk chunk) {
        os.set(MapChunk.OBJECT_TYPE, chunk);
    }

    /**
     * Returns the game object ID from the chunk ID.
     */
    public static long cid2Id(long cid) {
        return (cid << 32) | ID_FLAG;
    }

    /**
     * Returns the chunk ID from the game object ID.
     */
    public static int id2Cid(long id) {
        return (int) (id >> 32);
    }

    /**
     * CID of the parent chunk. 0 if this is the root chunk.
     */
    public long parent;

    /**
     * The {@link GameChunkPos} of the chunk.
     */
    public GameChunkPos pos;

    public int chunkSize;

    /**
     * Map width, used to calculate the center-extend.
     */
    public int width;

    /**
     * Map height, used to calculate the center-extend.
     */
    public int height;

    /**
     * The {@link GameChunkPos} and CIDs of the children chunks.
     */
    @ToString.Exclude
    private LongObjectMap<GameChunkPos> chunks;

    /**
     * The chunk CIDs of {@link NeighboringDir} neighbours of this chunk.
     */
    @ToString.Exclude
    public long[] neighbors = new long[NeighboringDir.values().length];

    /**
     * True if the chunk is a leaf with blocks.
     */
    private boolean leaf;

    /**
     * Pre-calculated {@link CenterExtent} based on the chunks position.
     */
    @ToString.Exclude
    private CenterExtent centerExtent;

    /**
     * The {@link MapBlock}s {@link MutableDirectBuffer} in the chunk if the chunk
     * is a leaf.
     */
    @ToString.Exclude
    public Optional<MutableDirectBuffer> blocks;

    public MapChunk() {
        this.centerExtent = new CenterExtent();
        this.pos = new GameChunkPos();
    }

    public MapChunk(long id, int parent, int cs, int w, int h, GameChunkPos pos) {
        super(id);
        this.parent = parent;
        this.chunkSize = cs;
        this.width = w;
        this.height = h;
        this.pos = pos;
        updateCenterExtent(w, h);
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

    public int getCid() {
        return id2Cid(getId());
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

    public long getChunk(int x, int y, int z, int ex, int ey, int ez) {
        for (var view : chunks.keyValuesView()) {
            if (view.getTwo().equals(x, y, z, ex, ey, ez)) {
                return view.getOne();
            }
        }
        return 0;
    }

    public void setChunks(LongObjectMap<GameChunkPos> chunks) {
        this.chunks = LongObjectMaps.immutable.ofAll(chunks);
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
    public long getNeighbor(NeighboringDir dir) {
        return neighbors[dir.ordinal()];
    }

    public long getNeighborEast() {
        return getNeighbor(NeighboringDir.E);
    }

    public long getNeighborSouth() {
        return getNeighbor(NeighboringDir.S);
    }

    public long getNeighborDown() {
        return getNeighbor(NeighboringDir.D);
    }

    @Override
    public int getObjectType() {
        return OBJECT_TYPE;
    }

    @Override
    public void writeStream(DataOutput out) throws IOException {
        super.writeStream(out);
        out.writeInt(chunkSize);
        out.writeInt(width);
        out.writeInt(height);
        ExternalizableUtils.writeStreamLongObjectMap(out, chunks);
        for (int i = 0; i < 26; i++) {
            out.writeInt((int) neighbors[i]);
        }
        out.writeBoolean(leaf);
        centerExtent.writeStream(out);
        if (isLeaf()) {
            int size = blocks.get().capacity();
            var buff = new byte[size];
            blocks.get().getBytes(0, buff);
            out.writeInt(size);
            out.write(buff);
        }
    }

    @Override
    public void readStream(DataInput in) throws IOException {
        super.readStream(in);
        this.chunkSize = in.readInt();
        this.width = in.readInt();
        this.height = in.readInt();
        this.chunks = ExternalizableUtils.readStreamLongObjectMap(in, GameChunkPos::new);
        for (int i = 0; i < 26; i++) {
            this.neighbors[i] = in.readInt();
        }
        this.leaf = in.readBoolean();
        this.centerExtent.readStream(in);
        if (isLeaf()) {
            int size = in.readInt();
            var buff = new byte[size];
            in.readFully(buff);
            var bb = ByteBuffer.allocateDirect(size);
            bb.put(buff);
            blocks = Optional.of(new UnsafeBuffer(bb));
        }
    }
}
