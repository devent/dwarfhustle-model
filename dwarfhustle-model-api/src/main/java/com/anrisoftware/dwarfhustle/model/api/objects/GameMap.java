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

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Information about the game.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Getter
public class GameMap extends GameObject {

	private static final long serialVersionUID = 1L;

	public static final String OBJECT_TYPE = GameMap.class.getSimpleName();

	private String name;

	private int mapid;

	private int width;

	private int height;

	private int depth;

	private int blockSize;

	private WorldMap world;

	private ZoneOffset timeZone = ZoneOffset.of("Z");

	private OffsetDateTime time;

	private MapArea area;

	public GameMap(long id) {
		super(id);
	}

	public GameMap(byte[] idbuf) {
		super(idbuf);
	}

	@Override
	public String getObjectType() {
		return OBJECT_TYPE;
	}

	public void setName(String name) {
		if (!StringUtils.equals(this.name, name)) {
			setDirty(true);
			this.name = name;
		}
	}

	public void setMapid(int mapid) {
		if (this.mapid != mapid) {
			setDirty(true);
			this.mapid = mapid;
		}
	}

	public void setWidth(int width) {
		if (this.width != width) {
			setDirty(true);
			this.width = width;
		}
	}

	public void setHeight(int height) {
		if (this.height != height) {
			setDirty(true);
			this.height = height;
		}
	}

	public void setDepth(int depth) {
		if (this.depth != depth) {
			setDirty(true);
			this.depth = depth;
		}
	}

	public int getSize() {
		return depth * height * width;
	}

	public void setBlockSize(int blockSize) {
		if (this.blockSize != blockSize) {
			setDirty(true);
			this.blockSize = blockSize;
		}
	}

	public void setWorld(WorldMap world) {
		if (!Objects.equals(this.world, world)) {
			this.world = world;
			if (timeZone != null) {
				this.time = OffsetDateTime.of(world.getTime(), timeZone);
			}
			setDirty(true);
		}
	}

	public void setTimeZone(ZoneOffset timeZone) {
		if (!Objects.equals(this.timeZone, timeZone)) {
			this.timeZone = timeZone;
			if (world != null) {
				this.time = OffsetDateTime.of(world.getTime(), timeZone);
			}
			setDirty(true);
		}
	}

	public void setArea(MapArea area) {
		if (!Objects.equals(this.area, area)) {
			this.area = area;
			setDirty(true);
		}
	}
}