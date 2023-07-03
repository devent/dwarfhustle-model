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

import lombok.Data;
import lombok.EqualsAndHashCode;
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
@Data
public class GameMap extends StoredObject {

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

    private static final long serialVersionUID = 1L;

    public static final String OBJECT_TYPE = GameMap.class.getSimpleName();

    public String name;

    public long rootid;

    public int mapid;

    public int width;

    public int height;

    public int depth;

    public int chunkSize;

    public float blockWidth;

    public float blockHeight;

    public float blockDepth;

    public float centerOffsetX;

    public float centerOffsetY;

    public float centerOffsetZ;

    public float blockSizeX;

    public float blockSizeY;

    public float blockSizeZ;

    public long world;

    public ZoneOffset timeZone = ZoneOffset.of("Z");

    public OffsetDateTime time;

    public MapArea area;

    public float[] cameraPos = new float[3];

    public float[] cameraRot = new float[4];

    public MapCursor cursor = new MapCursor(0, 0, 0);

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

    @Override
    public boolean isDirty() {
        GameMap old = getOld();
        return old.name != name //
                || old.rootid != rootid //
                || old.mapid != mapid //
                || old.width != width || old.height != height || old.depth != depth //
                || old.chunkSize != chunkSize //
                || old.blockWidth != blockWidth || old.blockHeight != blockHeight || old.blockDepth != blockDepth//
                || old.centerOffsetX != centerOffsetX || old.centerOffsetY != centerOffsetY
                || old.centerOffsetZ != centerOffsetZ //
                || old.world != world //
                || Objects.equals(old.timeZone, timeZone) //
                || Objects.equals(old.time, time) //
                || Objects.equals(old.area, area) //
                || Objects.equals(old.cameraPos, cameraPos) //
                || Objects.equals(old.cameraRot, cameraRot) //
                || Objects.equals(old.cursor, cursor) //
        ;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public int getSize() {
        return depth * height * width;
    }

    public void setCenterOffset(float offset) {
        this.centerOffsetX = offset;
        this.centerOffsetY = offset;
        this.centerOffsetZ = offset;
    }

    public void setBlockSize(float size) {
        this.blockSizeX = size;
        this.blockSizeY = size;
        this.blockSizeZ = size;
    }

    /**
     * Calculates the total count of {@link MapChunk} blocks for the specified
     * width, height, depth and block size.
     */
    public int getBlocksCount() {
        return calculateBlocksCount(width, height, depth, chunkSize);
    }

    public void setCameraPos(float x, float y, float z) {
        this.cameraPos[0] = x;
        this.cameraPos[1] = y;
        this.cameraPos[2] = z;
    }

    public void setCameraRot(float x, float y, float z, float w) {
        this.cameraRot[0] = x;
        this.cameraRot[1] = y;
        this.cameraRot[2] = z;
        this.cameraRot[3] = w;
    }

    public void setCursor(int z, int y, int x) {
        this.cursor = new MapCursor(z, y, x);
    }

    public void addCursorZ(int dd) {
        setCursor(new MapCursor(cursor.z + dd, cursor.y, cursor.x));
    }

    public boolean isCursor(int z, int y, int x) {
        return cursor.equals(z, y, x);
    }

    public int getCursorZ() {
        return cursor.z;
    }

    public void setCursorZ(int z) {
        setCursor(new MapCursor(z, cursor.y, cursor.x));
    }

}
