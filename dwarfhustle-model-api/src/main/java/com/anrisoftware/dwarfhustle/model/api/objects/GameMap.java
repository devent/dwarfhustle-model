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

import static com.anrisoftware.dwarfhustle.model.api.objects.ExternalizableUtils.readStreamIntCollection;
import static com.anrisoftware.dwarfhustle.model.api.objects.ExternalizableUtils.readStreamIntIntMap;
import static com.anrisoftware.dwarfhustle.model.api.objects.ExternalizableUtils.readStreamIntIntMultimap;
import static com.anrisoftware.dwarfhustle.model.api.objects.ExternalizableUtils.readStreamIntLongMultimap;
import static com.anrisoftware.dwarfhustle.model.api.objects.ExternalizableUtils.readStreamIntObjectMapReader;
import static com.anrisoftware.dwarfhustle.model.api.objects.ExternalizableUtils.writeStreamIntCollection;
import static com.anrisoftware.dwarfhustle.model.api.objects.ExternalizableUtils.writeStreamIntIntMap;
import static com.anrisoftware.dwarfhustle.model.api.objects.ExternalizableUtils.writeStreamIntIntMultimap;
import static com.anrisoftware.dwarfhustle.model.api.objects.ExternalizableUtils.writeStreamIntLongMultimap;
import static com.anrisoftware.dwarfhustle.model.api.objects.ExternalizableUtils.writeStreamIntObjectMap;
import static com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos.calcIndex;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.time.ZoneOffset;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.collections.api.collection.MutableCollection;
import org.eclipse.collections.api.factory.primitive.IntIntMaps;
import org.eclipse.collections.api.factory.primitive.IntLists;
import org.eclipse.collections.api.factory.primitive.IntObjectMaps;
import org.eclipse.collections.api.factory.primitive.IntSets;
import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.api.map.primitive.IntIntMap;
import org.eclipse.collections.api.map.primitive.IntObjectMap;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.api.multimap.MutableMultimap;
import org.eclipse.collections.api.set.primitive.IntSet;
import org.eclipse.collections.api.set.primitive.MutableIntSet;
import org.eclipse.collections.impl.factory.Multimaps;

import com.google.auto.service.AutoService;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.val;

/**
 * Information about the game.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@AutoService(StoredObject.class)
public class GameMap extends GameObject implements StoredObject {

    /**
     * Calculates the total count of {@link MapChunk} blocks for the specified
     * width, height, depth and block size.
     * <ul>
     * <li>16x16x16 4 = 72+1
     * <li>32x32x32 4 = 584+1
     * <li>64x64x64 4 = 4680+1
     * <li>128x128x128 4 = 37448+1
     * <li>256x256x256 4 = 299592+1
     * </ul>
     * <ul>
     * <li>16x16x16 8 = 8+1
     * <li>32x32x32 8 = 72+1
     * <li>64x64x64 8 = 584+1
     * <li>128x128x128 8 = 4680+1
     * <li>256x256x256 8 = 37448+1
     * </ul>
     * <ul>
     * <li>32x32x32 16 = 8+1
     * <li>64x64x64 8 = 72+1
     * <li>128x128x128 8 = 584+1
     * <li>256x256x256 8 = 4680+1
     * </ul>
     */
    public static int calculateBlocksCount(int width, int height, int depth, int size) {
        var blocks = 1;
        var w = width;
        var h = height;
        var d = depth;
        while (true) {
            if (w < 8 || h < 8 || d < 8) {
                break;
            }
            blocks += w * h * d / (size * size * size);
            w /= 2;
            h /= 2;
            d /= 2;
        }
        return blocks;
    }

    public static GameMap getGameMap(ObjectsGetter og, long id) {
        return og.get(OBJECT_TYPE, id);
    }

    public static void setGameMap(ObjectsSetter os, GameMap gm) {
        os.set(OBJECT_TYPE, gm);
    }

    public static final int OBJECT_TYPE = GameMap.class.getSimpleName().hashCode();

    /**
     * Record ID set after the object was once stored in the backend.
     */
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Serializable rid;

    private long name;

    private int width;

    private int height;

    private int depth;

    private int chunkSize;

    private int chunksCount;

    /**
     * The {@link WorldMap} ID of the map.
     */
    private long world;

    private ZoneOffset timeZone = ZoneOffset.of("Z");

    private MapArea area = new MapArea();

    private float[] cameraPos = new float[3];

    private float[] cameraRot = new float[4];

    private GameBlockPos cursor = new GameBlockPos(0, 0, 0);

    private float[] sunPos = new float[3];

    private int climateZone;

    /**
     * Contains the chunks and block indices that have objects.
     */
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private MutableMultimap<Integer, Integer> filledChunks;

    /**
     * Contains the indices of blocks that have at least one {@link GameMapObject},
     * with the objects count.
     */
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private IntObjectMap<AtomicInteger> filledBlocks;

    /**
     * A list of the selected blocks indices.
     */
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private MutableIntList selectedBlocks;

    /**
     * Sets the OID of the cursor object.
     */
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private long cursorObject = 0;

    /**
     * Cashes the chunk ID for the block index.
     */
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private IntIntMap cids;

    /**
     * Lock to get game map objects.
     */
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private final Semaphore objectsLock;

    /**
     * Sets the ID of the selected object.
     */
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private long selectedObjectId = 0;

    /**
     * Sets the type of the selected object.
     */
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private int selectedObjectType = 0;

    /**
     * Cashes the existing object types to the object.
     */
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private MutableMultimap<Integer, Long> typesObjects;

    public GameMap() {
        final MutableIntObjectMap<AtomicInteger> filledBlocks = IntObjectMaps.mutable.withInitialCapacity(100);
        this.filledBlocks = filledBlocks.asSynchronized();
        final MutableMultimap<Integer, Integer> filledChunks = Multimaps.mutable.set.empty();
        this.filledChunks = filledChunks.asSynchronized();
        final MutableIntList selectedBlocks = IntLists.mutable.empty();
        this.selectedBlocks = selectedBlocks.asSynchronized();
        this.cids = IntIntMaps.immutable.empty();
        this.objectsLock = new Semaphore(1);
        final MutableMultimap<Integer, Long> typesObjects = Multimaps.mutable.set.empty();
        this.typesObjects = typesObjects.asSynchronized();
    }

    public GameMap(long id, int width, int height, int depth) {
        super(id);
        this.width = width;
        this.height = height;
        this.depth = depth;
        final MutableIntObjectMap<AtomicInteger> filledBlocks = IntObjectMaps.mutable
                .withInitialCapacity(width * height * depth);
        this.filledBlocks = filledBlocks.asSynchronized();
        final MutableMultimap<Integer, Integer> filledChunks = Multimaps.mutable.set.empty();
        this.filledChunks = filledChunks.asSynchronized();
        final MutableIntList selectedBlocks = IntLists.mutable.empty();
        this.selectedBlocks = selectedBlocks.asSynchronized();
        this.objectsLock = new Semaphore(1);
        final MutableMultimap<Integer, Long> typesObjects = Multimaps.mutable.set.empty();
        this.typesObjects = typesObjects.asSynchronized();
    }

    public GameMap(byte[] idbuf, int width, int height, int depth) {
        this(toId(idbuf), width, height, depth);
    }

    @Override
    public int getObjectType() {
        return OBJECT_TYPE;
    }

    public int getSize() {
        return depth * height * width;
    }

    public int getBlocksCount() {
        return getSize();
    }

    public void setCameraPos(float x, float y, float z) {
        this.cameraPos[0] = x;
        this.cameraPos[1] = y;
        this.cameraPos[2] = z;
    }

    public void setCameraRot(float[] rot) {
        this.cameraRot = rot;
    }

    public void setCameraRot(float x, float y, float z, float w) {
        this.cameraRot[0] = x;
        this.cameraRot[1] = y;
        this.cameraRot[2] = z;
        this.cameraRot[3] = w;
    }

    public void setCursor(int x, int y, int z) {
        this.cursor = new GameBlockPos(x, y, z);
    }

    public void addCursorZ(int dd) {
        setCursor(new GameBlockPos(this.cursor.x, this.cursor.y, this.cursor.z + dd));
    }

    public boolean isCursor(int x, int y, int z) {
        return this.cursor.equals(x, y, z);
    }

    public int getCursorZ() {
        return this.cursor.z;
    }

    public void setCursorZ(int z) {
        setCursor(new GameBlockPos(this.cursor.x, this.cursor.y, z));
    }

    public void setSunPosition(float x, float y, float z) {
        this.sunPos[0] = x;
        this.sunPos[1] = y;
        this.sunPos[2] = z;
    }

    public void addFilledBlock(int cid, int index) {
        final var filledBlocks = (MutableIntObjectMap<AtomicInteger>) this.filledBlocks;
        filledBlocks.getIfAbsentPut(index, new AtomicInteger(0)).updateAndGet(it -> {
            it++;
            this.filledChunks.put(cid, index);
            return it;
        });
    }

    public void removeFilledBlock(int cid, int index) {
        final var filledBlocks = (MutableIntObjectMap<AtomicInteger>) this.filledBlocks;
        filledBlocks.getIfAbsentPut(index, new AtomicInteger(0)).updateAndGet(it -> {
            it--;
            if (it == 0) {
                filledBlocks.remove(index);
                this.filledChunks.remove(cid, index);
            }
            return it;
        });
    }

    /**
     * Returns true if the given {@link MapBlock} have any game objects.
     */
    public boolean isFilledBlock(MapBlock mb) {
        final int index = GameBlockPos.calcIndex(this, mb.getPos());
        final var count = this.filledBlocks.get(index);
        return count != null && count.get() > 0;
    }

    /**
     * Returns the chunk ID for the block index.
     */
    public int getCid(int index) {
        return this.cids.get(index);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        writeStream(out);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        readStream(in);
    }

    @Override
    public void writeStream(DataOutput out) throws IOException {
        super.writeStream(out);
        out.writeLong(this.name);
        out.writeInt(this.width);
        out.writeInt(this.height);
        out.writeInt(this.depth);
        out.writeInt(this.chunkSize);
        out.writeInt(this.chunksCount);
        out.writeLong(this.world);
        out.writeUTF(this.timeZone.getId());
        this.area.writeStream(out);
        out.writeFloat(this.cameraPos[0]);
        out.writeFloat(this.cameraPos[1]);
        out.writeFloat(this.cameraPos[2]);
        out.writeFloat(this.cameraRot[0]);
        out.writeFloat(this.cameraRot[1]);
        out.writeFloat(this.cameraRot[2]);
        out.writeFloat(this.cameraRot[3]);
        this.cursor.writeStream(out);
        out.writeFloat(this.sunPos[0]);
        out.writeFloat(this.sunPos[1]);
        out.writeFloat(this.sunPos[2]);
        out.writeInt(this.climateZone);
        writeStreamIntObjectMap(out, this.filledBlocks, this::writeAtomicInt);
        writeStreamIntIntMultimap(out, this.filledChunks);
        writeStreamIntCollection(out, selectedBlocks.size(), selectedBlocks);
        out.writeLong(this.cursorObject);
        writeStreamIntIntMap(out, cids);
        out.writeInt(selectedObjectType);
        out.writeLong(selectedObjectId);
        writeStreamIntLongMultimap(out, typesObjects);
    }

    @SneakyThrows
    private void writeAtomicInt(DataOutput out, AtomicInteger i) {
        out.writeInt(i.get());
    }

    @Override
    public void readStream(DataInput in) throws IOException {
        super.readStream(in);
        this.name = in.readLong();
        this.width = in.readInt();
        this.height = in.readInt();
        this.depth = in.readInt();
        this.chunkSize = in.readInt();
        this.chunksCount = in.readInt();
        this.world = in.readLong();
        this.timeZone = ZoneOffset.of(in.readUTF());
        this.area.readStream(in);
        this.cameraPos[0] = in.readFloat();
        this.cameraPos[1] = in.readFloat();
        this.cameraPos[2] = in.readFloat();
        this.cameraRot[0] = in.readFloat();
        this.cameraRot[1] = in.readFloat();
        this.cameraRot[2] = in.readFloat();
        this.cameraRot[3] = in.readFloat();
        this.cursor.readStream(in);
        this.sunPos[0] = in.readFloat();
        this.sunPos[1] = in.readFloat();
        this.sunPos[2] = in.readFloat();
        this.climateZone = in.readInt();
        final var filledBlocks = readStreamIntObjectMapReader(in, this::readAtomicInt);
        this.filledBlocks = filledBlocks.asSynchronized();
        final var filledChunks = readStreamIntIntMultimap(in, () -> Multimaps.mutable.set.empty());
        this.filledChunks = filledChunks.asSynchronized();
        final MutableIntList selectedBlocks = (MutableIntList) readStreamIntCollection(in);
        this.selectedBlocks = selectedBlocks.asSynchronized();
        this.cursorObject = in.readLong();
        this.cids = readStreamIntIntMap(in);
        this.selectedObjectType = in.readInt();
        this.selectedObjectId = in.readLong();
        final var typesObjects = readStreamIntLongMultimap(in, () -> Multimaps.mutable.set.empty());
        this.typesObjects = typesObjects.asSynchronized();
    }

    @SneakyThrows
    private AtomicInteger readAtomicInt(DataInput in) {
        return new AtomicInteger(in.readInt());
    }

    @SneakyThrows
    private GameBlockPos readGameBlockPos(DataInput in) {
        var p = new GameBlockPos();
        p.readStream(in);
        return p;
    }

    public void clearSelectedBlocks() {
        selectedBlocks.clear();
    }

    public void addSelectedBlock(int index) {
        selectedBlocks.add(index);
    }

    public boolean isSelectedBlock(int x, int y, int z) {
        return !selectedBlocks.isEmpty() && selectedBlocks.contains(calcIndex(this, x, y, z));
    }

    /**
     * Returns only the indices of the blocks with at least one map object.
     */
    public IntSet getFilledBlocksIndices() {
        MutableIntSet set = IntSets.mutable.withInitialCapacity(filledBlocks.size());
        for (val entry : filledBlocks.keyValuesView()) {
            if (entry.getTwo().get() > 0) {
                set.add(entry.getOne());
            }
        }
        return set.asUnmodifiable();
    }

    /**
     * Acquires a lock over the map objects, blocks until a lock is available.
     */
    public AutoCloseable acquireLockMapObjects() throws InterruptedException {
        objectsLock.acquire();
        return () -> objectsLock.release();
    }

    /**
     * Sets the selected object.
     */
    public void setSelectedObject(int type, long id) {
        this.selectedObjectId = id;
        this.selectedObjectType = type;
    }

    /**
     * Clears any selected object.
     */
    public void clearSelectedObject() {
        this.selectedObjectId = 0;
        this.selectedObjectType = 0;
    }

    public void addTypeObject(int type, long id) {
        typesObjects.put(type, id);
    }

    public void removeTypeObject(int type, long id) {
        typesObjects.remove(type, id);
    }

    public MutableCollection<Long> getObjectForType(int type) {
        return typesObjects.get(type);
    }
}
