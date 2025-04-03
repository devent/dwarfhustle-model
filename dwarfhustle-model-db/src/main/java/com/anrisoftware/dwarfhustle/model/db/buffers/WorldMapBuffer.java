/*
 * dwarfhustle-model-db - Manages the compile dependencies for the model.
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
package com.anrisoftware.dwarfhustle.model.db.buffers;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.eclipse.collections.api.factory.primitive.LongSets;
import org.eclipse.collections.api.set.primitive.LongSet;
import org.eclipse.collections.api.set.primitive.MutableLongSet;

import com.anrisoftware.dwarfhustle.model.api.objects.StoredObject;
import com.anrisoftware.dwarfhustle.model.api.objects.WorldMap;
import com.google.auto.service.AutoService;

/**
 * Writes and reads {@link WorldMap} in a byte buffer.
 *
 * <ul>
 * <li>@{code i} the ID;
 * <li>@{code N} the {@link WorldMap#getName()}
 * <li>@{code d} the {@link WorldMap#distanceLat};
 * <li>@{code D} the {@link WorldMap#distanceLon};
 * <li>@{code T} the year of {@link WorldMap#time};
 * <li>@{code C} the ID of {@link WorldMap#currentMap};
 * <li>@{code M} the {@link WorldMap#maps};
 * </ul>
 *
 * <pre>
 * long  0                   1                   2                   3                   4                   5
 * int   0         1         2         3         4         5         6         7         8         9         10
 * short 0    1    2    3    4    5    6    7    8    9    10   11   12   13   14   15   16   17   18   19   20   21
 *       iiii iiii iiii iiii NNNN NNNN NNNN NNNN dddd dddd DDDD DDDD TTTT TTTT TTTT TTTT CCCC CCCC CCCC CCCC MMMM MMMM ....
 * </pre>
 */
@AutoService(StoredObjectBuffer.class)
public class WorldMapBuffer extends GameObjectBuffer implements StoredObjectBuffer {

    /**
     * Size in bytes.
     */
    public static final int MIN_SIZE = //
            GameObjectBuffer.SIZE + //
                    8 + // name ID
                    8 + // distance lat, lon
                    8 + // time
                    8 + // current map ID
                    4 // maps count
    ;;

    private static final int NAME_BYTES = 1 * 8;

    private static final int DLAT_BYTES = 4 * 4;

    private static final int DLON_BYTES = 5 * 4;

    private static final int TIME_BYTES = 3 * 8;

    private static final int CURRENT_MAP_BYTES = 4 * 8;

    private static final int MAPS_BYTES = 10 * 4;

    /**
     * Calculates the size in bytes.
     */
    public static int calcSize(WorldMap wm) {
        return MIN_SIZE + wm.getMaps().size() * 8;
    }

    public static void setDistanceLat(MutableDirectBuffer b, int off, float d) {
        b.putFloat(DLAT_BYTES + off, d);
    }

    public static float getDistanceLat(DirectBuffer b, int off) {
        return b.getFloat(DLAT_BYTES + off);
    }

    public static void setDistanceLon(MutableDirectBuffer b, int off, float d) {
        b.putFloat(DLON_BYTES + off, d);
    }

    public static float getDistanceLon(DirectBuffer b, int off) {
        return b.getFloat(DLON_BYTES + off);
    }

    public static long getTime(DirectBuffer b, int off) {
        return b.getLong(TIME_BYTES + off);
    }

    public static void setTime(MutableDirectBuffer b, int off, long time) {
        b.putLong(TIME_BYTES + off, time);
    }

    public static void setCurrentMap(MutableDirectBuffer b, int off, long id) {
        b.putLong(CURRENT_MAP_BYTES + off, id);
    }

    public static long getCurrentMap(DirectBuffer b, int off) {
        return b.getLong(CURRENT_MAP_BYTES + off);
    }

    public static void setMaps(MutableDirectBuffer b, int off, LongSet maps) {
        b.putInt(MAPS_BYTES + off, maps.size());
        var it = maps.longIterator();
        int i = 0;
        while (it.hasNext()) {
            long id = it.next();
            b.putLong(MAPS_BYTES + off + 4 + i * 8, id);
            i++;
        }
    }

    public static MutableLongSet getMaps(DirectBuffer b, int off, MutableLongSet store) {
        int count = b.getInt(MAPS_BYTES + off);
        if (store == null) {
            store = LongSets.mutable.withInitialCapacity(count);
        }
        for (int i = 0; i < count; i++) {
            store.add(b.getLong(MAPS_BYTES + off + 4 + i * 8));
        }
        return store;
    }

    public static void setName(MutableDirectBuffer b, int off, long name) {
        b.putLong(NAME_BYTES + off, name);
    }

    public static long getName(DirectBuffer b, int off) {
        return b.getLong(NAME_BYTES + off);
    }

    public static void setWorldMap(MutableDirectBuffer b, int off, WorldMap wm) {
        GameObjectBuffer.writeObject(b, off, wm);
        setMaps(b, off, wm.getMaps());
        setName(b, off, wm.getName());
        setDistanceLat(b, off, wm.getDistanceLat());
        setDistanceLon(b, off, wm.getDistanceLon());
        setTime(b, off, wm.getTime().toEpochSecond(ZoneOffset.UTC));
        setCurrentMap(b, off, wm.getCurrentMap());
    }

    public static WorldMap getWorldMap(DirectBuffer b, int off, WorldMap wm) {
        GameObjectBuffer.readObject(b, off, wm);
        wm.setMaps(getMaps(b, off, null));
        wm.setName(getName(b, off));
        wm.setDistanceLat(getDistanceLat(b, off));
        wm.setDistanceLon(getDistanceLon(b, off));
        wm.setTime(LocalDateTime.ofEpochSecond(getTime(b, off), 0, ZoneOffset.UTC));
        wm.setCurrentMap(getCurrentMap(b, off));
        return wm;
    }

    @Override
    public StoredObject read(DirectBuffer b) {
        return WorldMapBuffer.getWorldMap(b, 0, new WorldMap());
    }

    @Override
    public int getObjectType() {
        return WorldMap.OBJECT_TYPE;
    }

    @Override
    public int getSize(StoredObject go) {
        return calcSize((WorldMap) go);
    }

    @Override
    public void write(MutableDirectBuffer b, StoredObject go) {
        setWorldMap(b, 0, (WorldMap) go);
    }

}
