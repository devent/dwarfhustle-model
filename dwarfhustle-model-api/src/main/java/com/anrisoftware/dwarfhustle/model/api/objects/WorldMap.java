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

import java.time.LocalDateTime;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.collections.api.map.primitive.MutableIntLongMap;
import org.eclipse.collections.impl.factory.primitive.IntLongMaps;

import lombok.EqualsAndHashCode;
import lombok.Getter;
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
@Getter
public class WorldMap extends GameObject {

	private static final long serialVersionUID = 1L;

	public static final String OBJECT_TYPE = WorldMap.class.getSimpleName();

	/**
	 * The name of the world.
	 */
	private String name;

	/**
	 * The distance of 1° latitude in km.
	 */
	private float distanceLat;

	/**
	 * The distance of 1° longitude in km.
	 */
	private float distanceLon;

	/**
	 * The world time at 0° 0′ 0″ N, 0° 0′ 0″ E. Each game map must convert the time
	 * to the specific time zone.
	 */
	private LocalDateTime time = LocalDateTime.now();

	/**
	 * Mapid:=id map of {@link GameMap} game maps of the world.
	 */
	private MutableIntLongMap maps = IntLongMaps.mutable.empty();

	/**
	 * The current {@link GameMap} mapid.
	 */
	private int currentMapid = 0;

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
	 * Sets the world's name.
	 */
	public void setName(String name) {
		if (!StringUtils.equals(this.name, name)) {
			setDirty(true);
			this.name = name;
		}
	}

	/**
	 * Sets the distance of 1° latitude and longitude in km.
	 */
	public void setDistance(float lat, float lon) {
		if (this.distanceLat != lat) {
			setDirty(true);
			this.distanceLat = lat;
		}
		if (this.distanceLon != lon) {
			setDirty(true);
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

	/**
	 * Sets the world's time.
	 */
	public void setTime(LocalDateTime time) {
		if (!this.time.equals(time)) {
			this.time = time;
			setDirty(true);
		}
	}

	public void addMap(GameMap map) {
		this.maps.put(map.getMapid(), map.getId());
		map.setWorld(this);
	}

	public void setCurrentMapid(int currentMapid) {
		if (this.currentMapid != currentMapid) {
			this.currentMapid = currentMapid;
			setDirty(true);
		}
	}
}
