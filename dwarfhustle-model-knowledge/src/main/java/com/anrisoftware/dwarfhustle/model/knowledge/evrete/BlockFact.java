/*
 * dwarfhustle-model-knowledge - Manages the compile dependencies for the model.
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
package com.anrisoftware.dwarfhustle.model.knowledge.evrete;

import static com.anrisoftware.dwarfhustle.model.api.objects.MapBlockBuffer.calcIndex;
import static com.anrisoftware.dwarfhustle.model.api.objects.MapBlockFlags.EMPTY;
import static com.anrisoftware.dwarfhustle.model.api.objects.MapBlockFlags.FILLED;
import static com.anrisoftware.dwarfhustle.model.api.objects.MapBlockFlags.RAMP;

import java.util.function.Function;

import com.anrisoftware.dwarfhustle.model.api.objects.MapBlockBuffer;
import com.anrisoftware.dwarfhustle.model.api.objects.MapChunk;
import com.anrisoftware.dwarfhustle.model.api.objects.NeighboringDir;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Terrain block fact.
 * <p>
 * 
 */
@RequiredArgsConstructor
@ToString
public class BlockFact {

    public final MapChunk chunk;

    public final int x;

    public final int y;

    public final int z;

    public final int w;

    public final int h;

    public final int d;

    @ToString.Exclude
    public final Function<Integer, MapChunk> retriever;

    public static void setMaterial(MapChunk chunk, int x, int y, int z, int m) {
        int off = getByteOffset(chunk, x, y, z);
        MapBlockBuffer.setMaterial(chunk.getBlocksBuffer(), off, m);
    }

    public void setMaterial(int x, int y, int z, int m) {
        setMaterial(chunk, x, y, z, m);
    }

    public void setMaterial(int m) {
        setMaterial(x, y, z, m);
    }

    public static int getMaterial(MapChunk chunk, int x, int y, int z) {
        int off = getByteOffset(chunk, x, y, z);
        return MapBlockBuffer.getMaterial(chunk.getBlocksBuffer(), off);
    }

    public int getMaterial(int x, int y, int z) {
        return getMaterial(chunk, x, y, z);
    }

    public int getMaterial() {
        return getMaterial(x, y, z);
    }

    public void setObject(int x, int y, int z, int o) {
        int off = getByteOffset(x, y, z);
        MapBlockBuffer.setObject(chunk.getBlocksBuffer(), off, o);
    }

    public void setObject(int o) {
        setObject(x, y, z, o);
    }

    public int getObject(int x, int y, int z) {
        int off = getByteOffset(x, y, z);
        return MapBlockBuffer.getObject(chunk.getBlocksBuffer(), off);
    }

    public int getObject() {
        return getObject(x, y, z);
    }

    public static void setProp(MapChunk chunk, int x, int y, int z, int p) {
        int off = getByteOffset(chunk, x, y, z);
        MapBlockBuffer.setProp(chunk.getBlocksBuffer(), off, p);
    }

    public void setProp(int x, int y, int z, int p) {
        setProp(chunk, x, y, z, p);
    }

    public void setProp(int p) {
        setProp(x, y, z, p);
    }

    public static int getProp(MapChunk chunk, int x, int y, int z) {
        int off = getByteOffset(chunk, x, y, z);
        return MapBlockBuffer.getProp(chunk.getBlocksBuffer(), off);
    }

    public int getProp(int x, int y, int z) {
        return getProp(chunk, x, y, z);
    }

    public int getProp() {
        return getProp(x, y, z);
    }

    public static boolean isProp(MapChunk chunk, int x, int y, int z, int flags) {
        return (getProp(chunk, x, y, z) & flags) == flags;
    }

    public boolean isProp(int x, int y, int z, int flags) {
        return isProp(chunk, x, y, z, flags);
    }

    public boolean isProp(int flags) {
        return isProp(x, y, z, flags);
    }

    public static void addProp(MapChunk chunk, int x, int y, int z, int flags) {
        int p = getProp(chunk, x, y, z);
        setProp(chunk, x, y, z, p | flags);
    }

    public void addProp(int x, int y, int z, int flags) {
        addProp(chunk, x, y, z, flags);
    }

    public void addProp(int flags) {
        addProp(x, y, z, flags);
    }

    public static void removeProp(MapChunk chunk, int x, int y, int z, int flags) {
        int p = getProp(chunk, x, y, z);
        setProp(chunk, x, y, z, p & ~flags);
    }

    public void removeProp(int x, int y, int z, int flags) {
        removeProp(chunk, x, y, z, flags);
    }

    public void removeProp(int flags) {
        removeProp(x, y, z, flags);
    }

    public static void setTemp(MapChunk chunk, int x, int y, int z, int t) {
        int off = getByteOffset(chunk, x, y, z);
        MapBlockBuffer.setTemp(chunk.getBlocksBuffer(), off, t);
    }

    public void setTemp(int x, int y, int z, int t) {
        setTemp(chunk, x, y, z, t);
    }

    public void setTemp(int t) {
        setTemp(x, y, z, t);
    }

    public static int getTemp(MapChunk chunk, int x, int y, int z) {
        int off = getByteOffset(chunk, x, y, z);
        return MapBlockBuffer.getTemp(chunk.getBlocksBuffer(), off);
    }

    public int getTemp(int x, int y, int z) {
        return getTemp(chunk, x, y, z);
    }

    public int getTemp() {
        return getTemp(x, y, z);
    }

    public static void setLux(MapChunk chunk, int x, int y, int z, int l) {
        int off = getByteOffset(chunk, x, y, z);
        MapBlockBuffer.setLux(chunk.getBlocksBuffer(), off, l);
    }

    public void setLux(int x, int y, int z, int l) {
        setLux(chunk, x, y, z, l);
    }

    public void setLux(int l) {
        setLux(x, y, z, l);
    }

    public static int getLux(MapChunk chunk, int x, int y, int z) {
        int off = getByteOffset(chunk, x, y, z);
        return MapBlockBuffer.getTemp(chunk.getBlocksBuffer(), off);
    }

    public int getLux(int x, int y, int z) {
        return getLux(chunk, x, y, z);
    }

    public int getLux() {
        return getLux(x, y, z);
    }

    private static int getByteOffset(MapChunk chunk, int x, int y, int z) {
        return calcIndex(chunk.pos.getSizeX(), chunk.pos.getSizeY(), chunk.pos.getSizeZ(), chunk.pos.x, chunk.pos.y,
                chunk.pos.z, x, y, z) * MapBlockBuffer.SIZE;
    }

    private int getByteOffset(int x, int y, int z) {
        return getByteOffset(chunk, x, y, z);
    }

    /**
     * Returns true if the neighbors exist.
     */
    public boolean isNeighborsExist(NeighboringDir... dirs) {
        for (var dir : dirs) {
            int dx = x + dir.pos.x;
            int dy = y + dir.pos.y;
            int dz = z + dir.pos.z;
            if (dx < 0 || dy < 0 || dz < 0) {
                return false;
            }
            if (dx >= w || dy >= h || dz >= d) {
                return false;
            }
        }
        return true;
    }

    public boolean isNeighborsSameLevelExist() {
        return isNeighborsExist(NeighboringDir.DIRS_SAME_LEVEL);
    }

    /**
     * Returns true if all the neighbors are filled.
     */
    public boolean isNeighborsFilled(NeighboringDir... dirs) {
        return isNeighborsFlag(FILLED.flag, dirs);
    }

    /**
     * Returns true if all the neighbors are filled.
     */
    public boolean isNeighborsEmpty(NeighboringDir... dirs) {
        return isNeighborsFlag(EMPTY.flag, dirs);
    }

    /**
     * Returns true if all the neighbors are ramps.
     */
    public boolean isNeighborsRamp(NeighboringDir... dirs) {
        return isNeighborsFlag(RAMP.flag, dirs);
    }

    private boolean isNeighborsFlag(int flag, NeighboringDir... dirs) {
        for (var dir : dirs) {
            if (!isNeighborFlag(chunk, dir, flag)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if the neighbors on the same level are empty.
     */
    public boolean isNeighborsSameLevelEmpty() {
        return isNeighborsFlag(EMPTY.flag, NeighboringDir.DIRS_SAME_LEVEL);
    }

    /**
     * Returns true if the neighbors on the same level are filled.
     */
    public boolean isNeighborsSameLevelFilled() {
        return isNeighborsFlag(FILLED.flag, NeighboringDir.DIRS_SAME_LEVEL);
    }

    private boolean isNeighborFlag(MapChunk chunk, NeighboringDir dir, int flag) {
        int dx = x + dir.pos.x;
        int dy = y + dir.pos.y;
        int dz = z + dir.pos.z;
        if (dx < chunk.pos.x) {
            chunk = retriever.apply(chunk.neighbors[NeighboringDir.W.ordinal()]);
            return isNeighborFlag(chunk, dir, flag);
        }
        if (dy < chunk.pos.y) {
            chunk = retriever.apply(chunk.neighbors[NeighboringDir.N.ordinal()]);
            return isNeighborFlag(chunk, dir, flag);
        }
        if (dz < chunk.pos.z) {
            chunk = retriever.apply(chunk.neighbors[NeighboringDir.U.ordinal()]);
            return isNeighborFlag(chunk, dir, flag);
        }
        if (dx >= chunk.pos.ep.x) {
            chunk = retriever.apply(chunk.neighbors[NeighboringDir.E.ordinal()]);
            return isNeighborFlag(chunk, dir, flag);
        }
        if (dy >= chunk.pos.ep.y) {
            chunk = retriever.apply(chunk.neighbors[NeighboringDir.S.ordinal()]);
            return isNeighborFlag(chunk, dir, flag);
        }
        if (dz >= chunk.pos.ep.z) {
            chunk = retriever.apply(chunk.neighbors[NeighboringDir.D.ordinal()]);
            return isNeighborFlag(chunk, dir, flag);
        }
        return isProp(chunk, dx, dy, dz, flag);
    }

    /**
     * Returns true if every block above the fact block is empty, i.e. there is
     * natural light above the fact block.
     */
    public boolean isLineOfSightUp() {
        var c = chunk;
        for (int zz = z - 1; zz >= 0;) {
            if (c.isInside(x, y, zz)) {
                if (!isProp(c, x, y, zz, EMPTY.flag)) {
                    return false;
                } else {
                    zz--;
                    continue;
                }
            } else {
                c = retriever.apply(c.neighbors[NeighboringDir.U.ordinal()]);
            }
        }
        return true;
    }

}
