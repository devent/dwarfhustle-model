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
import java.time.ZoneOffset;

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
public class GameMap extends GameObject implements StoredObject {

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

    public static GameMap getGameMap(ObjectsGetter og, long id) {
        return og.get(GameMap.class, OBJECT_TYPE, id);
    }

    private static final long serialVersionUID = 1L;

    public static final String OBJECT_TYPE = GameMap.class.getSimpleName();

    /**
     * Record ID set after the object was once stored in the backend.
     */
    public Serializable rid;

    public String name;

    public int width;

    public int height;

    public int depth;

    public int chunkSize;

    public int chunksCount;

    public int blocksCount;

    /**
     * The {@link WorldMap} ID of the map.
     */
    public long world;

    public ZoneOffset timeZone = ZoneOffset.of("Z");

    public MapArea area;

    public float[] cameraPos = new float[3];

    public float[] cameraRot = new float[4];

    public MapCursor cursor = new MapCursor(0, 0, 0);

    public float[] sunPos = new float[3];

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

    public int getSize() {
        return depth * height * width;
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

    public void setCameraRot(float[] rot) {
        this.cameraRot = rot;
    }

    public void setCameraRot(float x, float y, float z, float w) {
        this.cameraRot[0] = x;
        this.cameraRot[1] = y;
        this.cameraRot[2] = z;
        this.cameraRot[3] = w;
    }

    public void setCursor(int x, int y, int z) {
        this.cursor = new MapCursor(x, y, z);
    }

    public void addCursorZ(int dd) {
        setCursor(new MapCursor(cursor.x, cursor.y, cursor.z + dd));
    }

    public boolean isCursor(int x, int y, int z) {
        return cursor.equals(z, y, x);
    }

    public int getCursorZ() {
        return cursor.z;
    }

    public void setCursorZ(int z) {
        setCursor(new MapCursor(cursor.x, cursor.y, z));
    }

    public void setSunPosition(float x, float y, float z) {
        sunPos[0] = x;
        sunPos[1] = y;
        sunPos[2] = z;
    }
}
