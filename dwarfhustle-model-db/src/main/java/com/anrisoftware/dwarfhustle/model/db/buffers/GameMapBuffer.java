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

import java.time.ZoneOffset;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;

import com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos;
import com.anrisoftware.dwarfhustle.model.api.objects.GameMap;
import com.anrisoftware.dwarfhustle.model.api.objects.MapArea;
import com.anrisoftware.dwarfhustle.model.api.objects.StoredObject;
import com.google.auto.service.AutoService;

/**
 * Writes and reads {@link GameMap} in a byte buffer.
 *
 *
 * <ul>
 * <li>@{code i} the ID;
 * <li>@{code w} the ID of the {@link GameMap#world}
 * <li>@{code O} the ID of the {@link GameMap#getCursorObject()}
 * <li>@{code W} the {@link GameMap#width};
 * <li>@{code H} the {@link GameMap#height};
 * <li>@{code D} the {@link GameMap#depth};
 * <li>@{code S} the {@link GameMap#chunkSize};
 * <li>@{code C} the {@link GameMap#chunksCount};
 * <li>@{code O} the {@link GameMap#cursorObject};
 * <li>@{code a} the {@link GameMap#area};
 * <li>@{code p} the {@link GameMap#cameraPos};
 * <li>@{code r} the {@link GameMap#cameraRot};
 * <li>@{code c} the {@link GameMap#cursor};
 * <li>@{code s} the {@link GameMap#sunPos};
 * <li>@{code C} the {@link GameMap#getClimateZone()};
 * <li>@{code t} the {@link GameMap#timeZone};
 * <li>@{code n} the {@link GameMap#name};
 * </ul>
 *
 * <pre>
 * long  0                   1                   2                   3                   4                   5                   6                   7                   8                   9                   10                  11                  12                  13
 * int   0         1         2         3         4         5         6         7         8         9         10        11        12        13        14        15        16        17        18        19        20        21        22        23        24        25        26        27
 * short 0    1    2    3    4    5    6    7    8    9    10   11   12   13   14   15   16   17   18   19   20   21   22   23   24   25   26   27   28   29   30   31   32   33   34   35   36   37   38   39   40   41   42   43   44   45   46   47   48   49   50   51   52   53   54
 *       iiii iiii iiii iiii wwww wwww wwww wwww OOOO OOOO OOOO OOOO WWWW HHHH DDDD SSSS CCCC aaaa aaaa aaaa aaaa aaaa aaaa aaaa aaaa pppp pppp pppp pppp pppp pppp rrrr rrrr rrrr rrrr rrrr rrrr rrrr rrrr cccc cccc cccc ssss ssss ssss ssss ssss ssss CCCC tttt tttt nnnn nnnn nnnn nnnn
 * </pre>
 */
@AutoService(StoredObjectBuffer.class)
public class GameMapBuffer extends GameObjectBuffer implements StoredObjectBuffer {

    /**
     * Size in bytes.
     */
    public static final int SIZE = //
            GameObjectBuffer.SIZE + //
                    8 + // world
                    8 + // cursorObject
                    5 * 2 + // width, height, depth, chunk-size, chunk-count
                    MapAreaBuffer.SIZE + // area
                    3 * 4 + // cameraPos
                    4 * 4 + // cameraRot
                    GameBlockPosBuffer.SIZE + // cursor
                    3 * 4 + // sunPos
                    2 + // climate zone
                    ZoneOffsetBuffer.SIZE + //
                    8; // name

    private static final int WORLD_BYTES = 1 * 8;

    private static final int CURSOR_OBJECT_BYTES = 2 * 8;

    private static final int WIDTH_BYTES = 12 * 2;

    private static final int HEIGHT_BYTES = 13 * 2;

    private static final int DEPTH_BYTES = 14 * 2;

    private static final int CHUNK_SIZE_BYTES = 15 * 2;

    private static final int CHUNKS_COUNT_BYTES = 16 * 2;

    private static final int AREA_BYTES = 17 * 2;

    private static final int CAMERA_POS_BYTES = 25 * 2;

    private static final int CAMERA_ROT_BYTES = 31 * 2;

    private static final int CURSOR_POS_BYTES = 39 * 2;

    private static final int SUN_POS_BYTES = 42 * 2;

    private static final int CLIMATE_BYTES = 48 * 2;

    private static final int TIME_BYTES = 49 * 2;

    private static final int NAME_BYTES = 51 * 2;

    public static void setWorld(MutableDirectBuffer b, int off, long w) {
        b.putLong(WORLD_BYTES + off, w);
    }

    public static long getWorld(DirectBuffer b, int off) {
        return b.getLong(WORLD_BYTES + off);
    }

    public static void setCursorObject(MutableDirectBuffer b, int off, long id) {
        b.putLong(CURSOR_OBJECT_BYTES + off, id);
    }

    public static long getCursorObject(DirectBuffer b, int off) {
        return b.getLong(CURSOR_OBJECT_BYTES + off);
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
        b.putShort(CURSOR_POS_BYTES + off + 0 * 2, (short) c.x);
        b.putShort(CURSOR_POS_BYTES + off + 1 * 2, (short) c.y);
        b.putShort(CURSOR_POS_BYTES + off + 2 * 2, (short) c.z);
    }

    public static void setCursor(MutableDirectBuffer b, int off, int x, int y, int z) {
        b.putShort(CURSOR_POS_BYTES + off + 0 * 2, (short) x);
        b.putShort(CURSOR_POS_BYTES + off + 1 * 2, (short) y);
        b.putShort(CURSOR_POS_BYTES + off + 2 * 2, (short) z);
    }

    public static GameBlockPos getCursor(DirectBuffer b, int off, GameBlockPos c) {
        c.x = b.getShort(CURSOR_POS_BYTES + off + 0 * 2);
        c.y = b.getShort(CURSOR_POS_BYTES + off + 1 * 2);
        c.z = b.getShort(CURSOR_POS_BYTES + off + 2 * 2);
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

    public static int getClimateZone(DirectBuffer b, int off) {
        return b.getShort(CLIMATE_BYTES + off);
    }

    public static void setClimateZone(MutableDirectBuffer b, int off, int z) {
        b.putShort(CLIMATE_BYTES + off, (short) z);
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

    public static void setName(MutableDirectBuffer b, int off, long name) {
        b.putLong(NAME_BYTES + off, name);
    }

    public static long getName(DirectBuffer b, int off) {
        return b.getLong(NAME_BYTES + off);
    }

    public static void setGameMap(MutableDirectBuffer b, int off, GameMap gm) {
        setId(b, off, gm.getId());
        setWorld(b, off, gm.getWorld());
        setCursorObject(b, off, gm.getCursorObject());
        setWidth(b, off, gm.getWidth());
        setHeight(b, off, gm.getHeight());
        setDepth(b, off, gm.getDepth());
        setChunkSize(b, off, gm.getChunkSize());
        setChunksCount(b, off, gm.getChunksCount());
        setArea(b, off, gm.getArea());
        setCameraPos(b, off, gm.getCameraPos());
        setCameraRot(b, off, gm.getCameraRot());
        setCursor(b, off, gm.getCursor());
        setSunPos(b, off, gm.getSunPos());
        setClimateZone(b, off, gm.getClimateZone());
        setTimeZone(b, off, gm.getTimeZone());
        setName(b, off, gm.getName());
    }

    public static GameMap getGameMap(DirectBuffer b, int off, GameMap gm) {
        gm.setId(getId(b, off));
        gm.setWorld(getWorld(b, off));
        gm.setCursorObject(getCursorObject(b, off));
        gm.setWidth(getWidth(b, off));
        gm.setHeight(getHeight(b, off));
        gm.setDepth(getDepth(b, off));
        gm.setChunkSize(getChunkSize(b, off));
        gm.setChunksCount(getChunksCount(b, off));
        getArea(b, off, gm.getArea());
        getCameraPos(b, off, gm.getCameraPos());
        getCameraRot(b, off, gm.getCameraRot());
        getCursor(b, off, gm.getCursor());
        getSunPos(b, off, gm.getSunPos());
        gm.setClimateZone(getClimateZone(b, off));
        gm.setTimeZone(getTimeZone(b, off));
        gm.setName(getName(b, off));
        return gm;
    }

    @Override
    public StoredObject read(DirectBuffer b) {
        return GameMapBuffer.getGameMap(b, 0, new GameMap());
    }

    @Override
    public int getObjectType() {
        return GameMap.OBJECT_TYPE;
    }

    @Override
    public int getSize(StoredObject go) {
        return SIZE;
    }

    @Override
    public void write(MutableDirectBuffer b, StoredObject go) {
        GameMapBuffer.setGameMap(b, 0, (GameMap) go);
    }
}
