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

import java.time.ZoneOffset;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;

/**
 * Writes and reads {@link GameMap} in a byte buffer.
 * 
 * 
 * <ul>
 * <li>@{code i} the ID;
 * <li>@{code w} the ID of the {@link GameMap#world};
 * <li>@{code W} the {@link GameMap#width};
 * <li>@{code H} the {@link GameMap#height};
 * <li>@{code D} the {@link GameMap#depth};
 * <li>@{code S} the {@link GameMap#chunkSize};
 * <li>@{code C} the {@link GameMap#chunksCount};
 * <li>@{code a} the {@link GameMap#area};
 * <li>@{code p} the {@link GameMap#cameraPos};
 * <li>@{code r} the {@link GameMap#cameraRot};
 * <li>@{code c} the {@link GameMap#cursor};
 * <li>@{code s} the {@link GameMap#sunPos};
 * <li>@{code t} the {@link GameMap#timeZone};
 * <li>@{code n} the {@link GameMap#name};
 * </ul>
 * 
 * <pre>
 * long  0                   1                   2                   3                   4                   5                   6
 * int   0         1         2         3         4         5         6         7         8         9         10        11        12
 * short 0    1    2    3    4    5    6    7    8    9    10   11   12   13   14   15   16   17   18   19   20   21   22   23
 *       iiii iiii iiii iiii wwww wwww wwww wwww WWWW HHHH DDDD SSSS CCCC aaaa aaaa aaaa aaaa aaaa aaaa aaaa aaaa pppp pppp pppp pppp pppp pppp rrrr rrrr rrrr rrrr rrrr rrrr rrrr rrrr cccc cccc cccc ssss ssss ssss ssss ssss ssss tttt tttt nnnn nnnn ....
 * </pre>
 */
public class GameMapBuffer extends GameObjectBuffer {

    /**
     * Size in bytes.
     */
    public static final int MIN_SIZE = //
            GameObjectBuffer.SIZE + //
                    8 + //
                    5 * 2 + //
                    MapAreaBuffer.SIZE + //
                    3 * 4 + //
                    4 * 4 + //
                    3 * 4 + //
                    3 * 4 + //
                    ZoneOffsetBuffer.SIZE + //
                    1 * 4;

    private static final int WORLD_BYTES = 4 * 2;

    private static final int WIDTH_BYTES = 8 * 2;

    private static final int HEIGHT_BYTES = 9 * 2;

    private static final int DEPTH_BYTES = 10 * 2;

    private static final int CHUNK_SIZE_BYTES = 11 * 2;

    private static final int CHUNKS_COUNT_BYTES = 12 * 2;

    private static final int AREA_BYTES = 13 * 2;

    private static final int CAMERA_POS_BYTES = 21 * 2;

    private static final int CAMERA_ROT_BYTES = 27 * 2;

    private static final int CURSOR_BYTES = 35 * 2;

    private static final int SUN_POS_BYTES = 38 * 2;

    private static final int TIME_BYTES = 44 * 2;

    private static final int NAME_BYTES = 46 * 2;

    /**
     * Calculates the size in bytes.
     */
    public static int getSize(GameMap gm) {
        return MIN_SIZE + BufferUtils.getSizeStringUtf8(gm.name);
    }

    public static void setWorld(MutableDirectBuffer b, int off, long w) {
        b.putLong(WORLD_BYTES + off, w);
    }

    public static long getWorld(DirectBuffer b, int off) {
        return b.getLong(WORLD_BYTES + off);
    }

    public static void setWidth(MutableDirectBuffer b, int off, int w) {
        b.putShort(WIDTH_BYTES + off, (short) w);
    }

    public static int getWidth(DirectBuffer b, int off) {
        return b.getShort(WIDTH_BYTES + off);
    }

    public static void setHeight(MutableDirectBuffer b, int off, int h) {
        b.putShort(HEIGHT_BYTES + off, (short) h);
    }

    public static int getHeight(DirectBuffer b, int off) {
        return b.getShort(HEIGHT_BYTES + off);
    }

    public static void setDepth(MutableDirectBuffer b, int off, int w) {
        b.putShort(DEPTH_BYTES + off, (short) w);
    }

    public static int getDepth(DirectBuffer b, int off) {
        return b.getShort(DEPTH_BYTES + off);
    }

    public static void setChunkSize(MutableDirectBuffer b, int off, int w) {
        b.putShort(CHUNK_SIZE_BYTES + off, (short) w);
    }

    public static int getChunkSize(DirectBuffer b, int off) {
        return b.getShort(CHUNK_SIZE_BYTES + off);
    }

    public static void setChunksCount(MutableDirectBuffer b, int off, int w) {
        b.putShort(CHUNKS_COUNT_BYTES + off, (short) w);
    }

    public static int getChunksCount(DirectBuffer b, int off) {
        return b.getShort(CHUNKS_COUNT_BYTES + off);
    }

    public static void setArea(MutableDirectBuffer b, int off, MapArea a) {
        MapAreaBuffer.setArea(b, AREA_BYTES + off, a);
    }

    public static MapArea getArea(DirectBuffer b, int off, MapArea a) {
        return MapAreaBuffer.getArea(b, AREA_BYTES + off, a);
    }

    public static void setArea(MutableDirectBuffer b, int off, float nwlat, float nwlon, float selat, float selon) {
        MapAreaBuffer.setNwLat(b, AREA_BYTES + off, nwlat);
        MapAreaBuffer.setNwLon(b, AREA_BYTES + off, nwlon);
        MapAreaBuffer.setSeLat(b, AREA_BYTES + off, selat);
        MapAreaBuffer.setSeLon(b, AREA_BYTES + off, selon);
    }

    public static void setCameraPos(MutableDirectBuffer b, int off, float[] p) {
        b.putFloat(CAMERA_POS_BYTES + off + 0 * 4, p[0]);
        b.putFloat(CAMERA_POS_BYTES + off + 1 * 4, p[1]);
        b.putFloat(CAMERA_POS_BYTES + off + 2 * 4, p[2]);
    }

    public static float[] getCameraPos(DirectBuffer b, int off, float[] p) {
        p[0] = b.getFloat(CAMERA_POS_BYTES + off + 0 * 4);
        p[1] = b.getFloat(CAMERA_POS_BYTES + off + 1 * 4);
        p[2] = b.getFloat(CAMERA_POS_BYTES + off + 2 * 4);
        return p;
    }

    public static void setCameraRot(MutableDirectBuffer b, int off, float[] r) {
        b.putFloat(CAMERA_ROT_BYTES + off + 0 * 4, r[0]);
        b.putFloat(CAMERA_ROT_BYTES + off + 1 * 4, r[1]);
        b.putFloat(CAMERA_ROT_BYTES + off + 2 * 4, r[2]);
        b.putFloat(CAMERA_ROT_BYTES + off + 3 * 4, r[3]);
    }

    public static float[] getCameraRot(DirectBuffer b, int off, float[] r) {
        r[0] = b.getFloat(CAMERA_ROT_BYTES + off + 0 * 4);
        r[1] = b.getFloat(CAMERA_ROT_BYTES + off + 1 * 4);
        r[2] = b.getFloat(CAMERA_ROT_BYTES + off + 2 * 4);
        r[3] = b.getFloat(CAMERA_ROT_BYTES + off + 3 * 4);
        return r;
    }

    public static void setCursor(MutableDirectBuffer b, int off, GameBlockPos c) {
        b.putShort(CURSOR_BYTES + off + 0 * 2, (short) c.x);
        b.putShort(CURSOR_BYTES + off + 1 * 2, (short) c.y);
        b.putShort(CURSOR_BYTES + off + 2 * 2, (short) c.z);
    }

    public static void setCursor(MutableDirectBuffer b, int off, int x, int y, int z) {
        b.putShort(CURSOR_BYTES + off + 0 * 2, (short) x);
        b.putShort(CURSOR_BYTES + off + 1 * 2, (short) y);
        b.putShort(CURSOR_BYTES + off + 2 * 2, (short) z);
    }

    public static GameBlockPos getCursor(DirectBuffer b, int off, GameBlockPos c) {
        c.x = b.getShort(CURSOR_BYTES + off + 0 * 2);
        c.y = b.getShort(CURSOR_BYTES + off + 1 * 2);
        c.z = b.getShort(CURSOR_BYTES + off + 2 * 2);
        return c;
    }

    public static void setSunPos(MutableDirectBuffer b, int off, float[] p) {
        b.putFloat(SUN_POS_BYTES + off + 0 * 4, p[0]);
        b.putFloat(SUN_POS_BYTES + off + 1 * 4, p[1]);
        b.putFloat(SUN_POS_BYTES + off + 2 * 4, p[2]);
    }

    public static float[] getSunPos(DirectBuffer b, int off, float[] p) {
        p[0] = b.getFloat(SUN_POS_BYTES + off + 0 * 4);
        p[1] = b.getFloat(SUN_POS_BYTES + off + 1 * 4);
        p[2] = b.getFloat(SUN_POS_BYTES + off + 2 * 4);
        return p;
    }

    public static void setTimeZone(MutableDirectBuffer b, int off, ZoneOffset z) {
        ZoneOffsetBuffer.setZoneOffset(b, TIME_BYTES + off, z);
    }

    public static void setTimeZone(MutableDirectBuffer b, int off, int seconds) {
        ZoneOffsetBuffer.setZoneOffset(b, TIME_BYTES + off, seconds);
    }

    public static ZoneOffset getTimeZone(DirectBuffer b, int off) {
        return ZoneOffsetBuffer.getZoneOffset(b, TIME_BYTES + off);
    }

    public static void setName(MutableDirectBuffer b, int off, String name) {
        b.putStringUtf8(NAME_BYTES + off, name);
    }

    public static String getName(DirectBuffer b, int off) {
        return b.getStringUtf8(NAME_BYTES + off);
    }

    public static void setGameMap(MutableDirectBuffer b, int off, GameMap gm) {
        setId(b, off, gm.id);
        setWorld(b, off, gm.world);
        setWidth(b, off, gm.width);
        setHeight(b, off, gm.height);
        setDepth(b, off, gm.depth);
        setChunkSize(b, off, gm.chunkSize);
        setChunksCount(b, off, gm.chunksCount);
        setArea(b, off, gm.area);
        setCameraPos(b, off, gm.cameraPos);
        setCameraRot(b, off, gm.cameraRot);
        setCursor(b, off, gm.cursor);
        setSunPos(b, off, gm.sunPos);
        setTimeZone(b, off, gm.timeZone);
        setName(b, off, gm.name);
    }

    public static GameMap getGameMap(DirectBuffer b, int off, GameMap gm) {
        gm.id = getId(b, off);
        gm.world = getWorld(b, off);
        gm.width = getWidth(b, off);
        gm.height = getHeight(b, off);
        gm.depth = getDepth(b, off);
        gm.chunkSize = getChunkSize(b, off);
        gm.chunksCount = getChunksCount(b, off);
        getArea(b, off, gm.area);
        getCameraPos(b, off, gm.cameraPos);
        getCameraRot(b, off, gm.cameraRot);
        getCursor(b, off, gm.cursor);
        getSunPos(b, off, gm.sunPos);
        gm.timeZone = getTimeZone(b, off);
        gm.name = getName(b, off);
        return gm;
    }

}
