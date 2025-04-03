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

import static com.anrisoftware.dwarfhustle.model.api.objects.ExternalizableUtils.readStreamLongCollection;
import static com.anrisoftware.dwarfhustle.model.api.objects.ExternalizableUtils.writeStreamLongCollection;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.eclipse.collections.api.factory.primitive.LongSets;
import org.eclipse.collections.api.set.primitive.MutableLongSet;

import com.google.auto.service.AutoService;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Information about the world.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@AutoService(StoredObject.class)
public class WorldMap extends GameObject implements StoredObject {

    public static final int OBJECT_TYPE = WorldMap.class.getSimpleName().hashCode();

    public static WorldMap getWorldMap(ObjectsGetter og, long id) {
        return og.get(OBJECT_TYPE, id);
    }

    /**
     * Record ID set after the object was once stored in the backend.
     */
    private Serializable rid;

    /**
     * The name of the world.
     */
    private long name;

    /**
     * The distance of 1° latitude in km.
     */
    private float distanceLat;

    /**
     * The distance of 1° longitude in km.
     */
    private float distanceLon;

    /**
     * The world time at UTC+00:00. Each game map must convert the time to the
     * specific time zone.
     */
    private LocalDateTime time = LocalDateTime.now();

    /**
     * Ids of the {@link GameMap} game maps of the world.
     */
    private MutableLongSet maps = LongSets.mutable.empty();

    /**
     * The current {@link GameMap} id.
     */
    private long currentMap = 0;

    public WorldMap(long id) {
        super(id);
    }

    public WorldMap(byte[] idbuf) {
        super(idbuf);
    }

    @Override
    public int getObjectType() {
        return OBJECT_TYPE;
    }

    /**
     * Sets the distance of 1° latitude and longitude in km.
     */
    public void setDistance(float lat, float lon) {
        if (this.distanceLat != lat) {
            this.distanceLat = lat;
        }
        if (this.distanceLon != lon) {
            this.distanceLon = lon;
        }
    }

    /**
     * Returns the vertical size in km.
     */
    public float getSizeVertical() {
        return 360f * distanceLat;
    }

    /**
     * Returns the horizontal size in km.
     */
    public float getSizeHorizontal() {
        return 360f * distanceLon;
    }

    public void addMap(GameMap map) {
        this.maps.add(map.getId());
        map.setWorld(getId());
    }

    @Override
    public void writeStream(DataOutput out) throws IOException {
        super.writeStream(out);
        out.writeLong(name);
        out.writeFloat(distanceLat);
        out.writeFloat(distanceLon);
        out.writeLong(time.toInstant(ZoneOffset.UTC).getEpochSecond());
        writeStreamLongCollection(out, maps.size(), maps);
    }

    @Override
    public void readStream(DataInput in) throws IOException {
        super.readStream(in);
        this.name = in.readLong();
        this.distanceLat = in.readFloat();
        this.distanceLon = in.readFloat();
        this.time = LocalDateTime.ofEpochSecond(in.readLong(), 0, ZoneOffset.UTC);
        this.maps = LongSets.mutable.ofAll(readStreamLongCollection(in));
    }
}
