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

    /**
     * Calculates the total count of {@link MapBlock} blocks for the specified
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

	private float[] cameraPos = new float[3];

	private float[] cameraRot = new float[4];

    private MapCursor cursor = new MapCursor(0, 0, 0);

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

    /**
     * Calculates the total count of {@link MapBlock} blocks for the specified
     * width, height, depth and block size.
     */
    public int getBlocksCount() {
        return calculateBlocksCount(width, height, depth, blockSize);
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

	public void setCameraPos(float[] pos) {
		if (!Objects.equals(this.cameraPos, pos)) {
			this.cameraPos[0] = pos[0];
			this.cameraPos[1] = pos[1];
			this.cameraPos[2] = pos[2];
			setDirty(true);
		}
	}

	public void setCameraPos(float x, float y, float z) {
		if (this.cameraPos[0] != x || this.cameraPos[1] != y || this.cameraPos[2] != z) {
			this.cameraPos[0] = x;
			this.cameraPos[1] = y;
			this.cameraPos[2] = z;
			setDirty(true);
		}
	}

	public void setCameraRot(float[] rot) {
		if (!Objects.equals(this.cameraRot, rot)) {
			this.cameraRot[0] = rot[0];
			this.cameraRot[1] = rot[1];
			this.cameraRot[2] = rot[2];
			this.cameraRot[3] = rot[3];
			setDirty(true);
		}
	}

	public void setCameraRot(float x, float y, float z, float w) {
		if (this.cameraRot[0] != x || this.cameraRot[1] != y || this.cameraRot[2] != z || this.cameraRot[3] != w) {
			this.cameraRot[0] = x;
			this.cameraRot[1] = y;
			this.cameraRot[2] = z;
			this.cameraRot[3] = w;
			setDirty(true);
		}
	}

    public void setCursor(int z, int y, int x) {
        if (this.cursor.z != z || this.cursor.y != y || this.cursor.x != x) {
            this.cursor = new MapCursor(z, y, x);
            setDirty(true);
        }
    }

    public void setCursor(MapCursor cursor) {
        if (!Objects.equals(this.cursor, cursor)) {
            this.cursor = cursor;
            setDirty(true);
        }
    }

    public void addZ(int dd) {
        setCursor(new MapCursor(cursor.z + dd, cursor.y, cursor.x));
    }

    public boolean isCursor(int z, int y, int x) {
        return cursor.equals(z, y, x);
    }

    public int getCursorZ() {
        return cursor.z;
    }
}
