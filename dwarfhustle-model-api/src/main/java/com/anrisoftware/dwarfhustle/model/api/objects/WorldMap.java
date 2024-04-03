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

import java.io.Serializable;
import java.time.LocalDateTime;

import org.eclipse.collections.api.factory.primitive.LongSets;
import org.eclipse.collections.api.set.primitive.MutableLongSet;

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
public class WorldMap extends GameObject implements StoredObject {

    private static final long serialVersionUID = 1L;

    public static final String OBJECT_TYPE = WorldMap.class.getSimpleName();

    public static WorldMap getWorldMap(ObjectsGetter og, long id) {
        return og.get(WorldMap.class, OBJECT_TYPE, id);
    }

    /**
     * Record ID set after the object was once stored in the backend.
     */
    public Serializable rid;

    /**
     * The name of the world.
     */
    public String name;

    /**
     * The distance of 1° latitude in km.
     */
    public float distanceLat;

    /**
     * The distance of 1° longitude in km.
     */
    public float distanceLon;

    /**
     * The world time at 0° 0′ 0″ N, 0° 0′ 0″ E. Each game map must convert the time
     * to the specific time zone.
     */
    public LocalDateTime time = LocalDateTime.now();

    /**
     * Ids of the {@link GameMap} game maps of the world.
     */
    public MutableLongSet maps = LongSets.mutable.empty();

    /**
     * The current {@link GameMap} id.
     */
    public long currentMap = 0;

    public WorldMap(long id) {
        super(id);
    }

    public WorldMap(byte[] idbuf) {
        super(idbuf);
    }

    @Override
    public String getObjectType() {
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
        this.maps.add(map.id);
        map.setWorld(id);
    }

}
