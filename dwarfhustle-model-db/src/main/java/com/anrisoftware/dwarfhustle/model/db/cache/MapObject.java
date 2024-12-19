/*
 * dwarfhustle-model-db - Manages the compile dependencies for the model.
 * Copyright © 2022-2024 Erwin Müller (erwin.mueller@anrisoftware.com)
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
package com.anrisoftware.dwarfhustle.model.db.cache;

import static com.anrisoftware.dwarfhustle.model.api.objects.ExternalizableUtils.readStreamLongIntMap;
import static com.anrisoftware.dwarfhustle.model.api.objects.ExternalizableUtils.writeStreamLongIntMap;
import static com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos.calcIndex;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.eclipse.collections.api.factory.primitive.LongIntMaps;
import org.eclipse.collections.api.map.primitive.MutableLongIntMap;

import com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos;
import com.anrisoftware.dwarfhustle.model.api.objects.GameMap;
import com.anrisoftware.dwarfhustle.model.api.objects.GameMapObject;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;
import com.anrisoftware.dwarfhustle.model.api.objects.MapChunk;
import com.anrisoftware.dwarfhustle.model.api.objects.ObjectsGetter;
import com.anrisoftware.dwarfhustle.model.api.objects.ObjectsSetter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Cache entry for a {@link GameMapObject} on the {@link GameMap}.
 * 
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@NoArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Data
public class MapObject extends GameObject {

    public static final int OBJECT_TYPE = MapObject.class.getSimpleName().hashCode();

    public static MapObject setMapObject(ObjectsSetter os, MapObject mo) {
        os.set(MapObject.OBJECT_TYPE, mo);
        return mo;
    }

    public static MapObject getMapObject(ObjectsGetter og, GameMap gm, GameBlockPos pos) {
        return getMapObject(og, gm, pos.getX(), pos.getY(), pos.getZ());
    }

    public static MapObject getMapObject(ObjectsGetter og, GameMap gm, int x, int y, int z) {
        return getMapObject(og, calcIndex(gm, x, y, z));
    }

    public static MapObject getMapObject(ObjectsGetter og, int index) {
        return og.get(MapObject.OBJECT_TYPE, index);
    }

    /**
     * The {@link MapChunk#getCid()} CID.
     */
    public int cid;

    /**
     * Stores the {@link GameMapObject} IDs to type.
     */
    public MutableLongIntMap oids = LongIntMaps.mutable.withInitialCapacity(10).asSynchronized();

    /**
     * Stores the removed {@link GameMapObject} IDs.
     */
    public MutableLongIntMap removedOids = LongIntMaps.mutable.withInitialCapacity(10).asSynchronized();

    public MapObject(int index) {
        super(index);
    }

    public MapObject(int index, int type, long oid) {
        this(index);
        addObject(type, oid);
    }

    public MapObject(GameMap gm, GameMapObject o) {
        this(calcIndex(gm, o.getPos()), o.getObjectType(), o.getId());
    }

    public int getIndex() {
        return (int) getId();
    }

    @Override
    public int getObjectType() {
        return OBJECT_TYPE;
    }

    public void addObject(int type, long id) {
        oids.put(id, type);
        removedOids.remove(id);
    }

    public void removeObject(long id) {
        if (oids.containsKey(id)) {
            final int type = oids.get(id);
            oids.remove(id);
            removedOids.put(id, type);
        }
    }

    public boolean isEmpty() {
        return oids.isEmpty();
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
        out.writeInt(cid);
        writeStreamLongIntMap(out, oids);
        writeStreamLongIntMap(out, removedOids);
    }

    @Override
    public void readStream(DataInput in) throws IOException {
        super.readStream(in);
        this.cid = in.readInt();
        this.oids = ((MutableLongIntMap) readStreamLongIntMap(in)).asSynchronized();
        this.removedOids = ((MutableLongIntMap) readStreamLongIntMap(in)).asSynchronized();
    }

}
