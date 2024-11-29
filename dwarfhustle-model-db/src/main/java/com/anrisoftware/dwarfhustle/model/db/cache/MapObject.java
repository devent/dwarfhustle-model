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
     * Stores the {@link GameMapObject} IDs to type.
     */
    public MutableLongIntMap oids = LongIntMaps.mutable.withInitialCapacity(10).asSynchronized();

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
    }

    public void removeObject(long id) {
        oids.remove(id);
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
        writeStreamLongIntMap(out, oids);
    }

    @Override
    public void readStream(DataInput in) throws IOException {
        super.readStream(in);
        this.oids = (MutableLongIntMap) readStreamLongIntMap(in);
    }

}
