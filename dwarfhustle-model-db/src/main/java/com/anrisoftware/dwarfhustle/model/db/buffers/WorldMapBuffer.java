/*
 * dwarfhustle-model-db - Manages the compile dependencies for the model.
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
package com.anrisoftware.dwarfhustle.model.db.buffers;

import java.time.LocalDateTime;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.eclipse.collections.api.factory.primitive.LongSets;
import org.eclipse.collections.api.set.primitive.LongSet;
import org.eclipse.collections.api.set.primitive.MutableLongSet;

import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObjectBuffer;
import com.anrisoftware.dwarfhustle.model.api.objects.WorldMap;
import com.google.auto.service.AutoService;

/**
 * Writes and reads {@link WorldMap} in a byte buffer.
 * 
 * <ul>
 * <li>@{code i} the ID;
 * <li>@{code d} the {@link WorldMap#distanceLat};
 * <li>@{code d} the {@link WorldMap#distanceLon};
 * <li>@{code Y} the year of {@link WorldMap#time};
 * <li>@{code M} the month of {@link WorldMap#time};
 * <li>@{code D} the day of {@link WorldMap#time};
 * <li>@{code h} the hour of {@link WorldMap#time};
 * <li>@{code m} the minute of {@link WorldMap#time};
 * <li>@{code s} the second of {@link WorldMap#time};
 * <li>@{code C} the ID of {@link WorldMap#currentMap};
 * <li>@{code M} the {@link WorldMap#maps};
 * </ul>
 * 
 * <pre>
 * long  0                   1                   2                   3                   4                   5                   6
 * int   0         1         2         3         4         5         6         7         8         9         10        11        12
 * short 0    1    2    3    4    5    6    7    8    9    10   11   12   13   14   15   16   17   18   19   20   21   22   23
 *       iiii iiii iiii iiii dddd dddd DDDD DDDD YYYY MMMM DDDD hhhh mmmm ssss CCCC CCCC CCCC CCCC MMMM MMMM .... NNNN NNNN ....
 * </pre>
 */
@AutoService(StoredObjectBuffer.class)
public class WorldMapBuffer extends GameObjectBuffer implements StoredObjectBuffer {

    /**
     * Minimum size in bytes.
     */
    public static final int MIN_SIZE = //
            GameObjectBuffer.SIZE + //
                    2 * 4 + //
                    6 * 2 + //
                    8 + //
                    4 + //
                    4;

    private static final int DLAT_BYTES = 2 * 4;

    private static final int DLON_BYTES = 3 * 4;

    private static final int YEAR_BYTES = 8 * 2;

    private static final int MONTH_BYTES = 9 * 2;

    private static final int DAY_BYTES = 10 * 2;

    private static final int HOUR_BYTES = 11 * 2;

    private static final int MINUTE_BYTES = 12 * 2;

    private static final int SECOND_BYTES = 13 * 2;

    private static final int CURRENT_MAP_BYTES = 7 * 4;

    private static final int MAPS_BYTES = 9 * 4;

    /**
     * Calculates the size in bytes.
     */
    public static int getSize(WorldMap wm) {
        return MIN_SIZE + wm.maps.size() * 8 + BufferUtils.getSizeStringUtf8(wm.name);
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

    public static void setLocalDateTimeYear(MutableDirectBuffer b, int off, float y) {
        b.putShort(YEAR_BYTES + off, (short) y);
    }

    public static int getLocalDateTimeYear(DirectBuffer b, int off) {
        return b.getShort(YEAR_BYTES + off);
    }

    public static void setLocalDateTimeMonth(MutableDirectBuffer b, int off, float m) {
        b.putShort(MONTH_BYTES + off, (short) m);
    }

    public static int getLocalDateTimeMonth(DirectBuffer b, int off) {
        return b.getShort(MONTH_BYTES + off);
    }

    public static void setLocalDateTimeDay(MutableDirectBuffer b, int off, float d) {
        b.putShort(DAY_BYTES + off, (short) d);
    }

    public static int getLocalDateTimeDay(DirectBuffer b, int off) {
        return b.getShort(DAY_BYTES + off);
    }

    public static void setLocalDateTimeHour(MutableDirectBuffer b, int off, float h) {
        b.putShort(HOUR_BYTES + off, (short) h);
    }

    public static int getLocalDateTimeHour(DirectBuffer b, int off) {
        return b.getShort(HOUR_BYTES + off);
    }

    public static void setLocalDateTimeMinute(MutableDirectBuffer b, int off, float m) {
        b.putShort(MINUTE_BYTES + off, (short) m);
    }

    public static int getLocalDateTimeMinute(DirectBuffer b, int off) {
        return b.getShort(MINUTE_BYTES + off);
    }

    public static void setLocalDateTimeSecond(MutableDirectBuffer b, int off, float s) {
        b.putShort(SECOND_BYTES + off, (short) s);
    }

    public static int getLocalDateTimeSecond(DirectBuffer b, int off) {
        return b.getShort(SECOND_BYTES + off);
    }

    public static void setLocalDateTime(MutableDirectBuffer b, int off, int year, int month, int day, int hour,
            int minute, int second) {
        setLocalDateTimeYear(b, off, year);
        setLocalDateTimeMonth(b, off, month);
        setLocalDateTimeDay(b, off, day);
        setLocalDateTimeHour(b, off, hour);
        setLocalDateTimeMinute(b, off, minute);
        setLocalDateTimeSecond(b, off, second);
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

    public static void setName(MutableDirectBuffer b, int off, String name, int mapsCount) {
        b.putStringUtf8(MAPS_BYTES + off + 4 + mapsCount * 8, name);
    }

    public static String getName(DirectBuffer b, int off, int mapsCount) {
        return b.getStringUtf8(MAPS_BYTES + off + 4 + mapsCount * 8);
    }

    public static void setWorldMap(MutableDirectBuffer b, int off, WorldMap wm) {
        setId(b, off, wm.id);
        setMaps(b, off, wm.maps);
        setName(b, off, wm.name, wm.maps.size());
        setDistanceLat(b, off, wm.distanceLat);
        setDistanceLon(b, off, wm.distanceLon);
        setLocalDateTime(b, off, wm.time.getYear(), wm.time.getMonthValue(), wm.time.getDayOfMonth(), wm.time.getHour(),
                wm.time.getMinute(), wm.time.getSecond());
        setCurrentMap(b, off, wm.currentMap);
    }

    public static WorldMap getWorldMap(DirectBuffer b, int off, WorldMap wm) {
        wm.id = getId(b, off);
        wm.maps = getMaps(b, off, null);
        wm.name = getName(b, off, wm.maps.size());
        wm.distanceLat = getDistanceLat(b, off);
        wm.distanceLon = getDistanceLon(b, off);
        wm.time = LocalDateTime.of(getLocalDateTimeYear(b, off), getLocalDateTimeMonth(b, off),
                getLocalDateTimeDay(b, off), getLocalDateTimeHour(b, off), getLocalDateTimeMinute(b, off),
                getLocalDateTimeSecond(b, off));
        wm.currentMap = getCurrentMap(b, off);
        return wm;
    }

    @Override
    public GameObject read(DirectBuffer b) {
        return WorldMapBuffer.getWorldMap(b, 0, new WorldMap());
    }

    @Override
    public int getObjectType() {
        return WorldMap.OBJECT_TYPE;
    }

}
