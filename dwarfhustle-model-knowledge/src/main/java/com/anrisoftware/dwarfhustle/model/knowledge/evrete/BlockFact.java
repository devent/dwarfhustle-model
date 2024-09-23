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

import static com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos.calcIndex;
import static com.anrisoftware.dwarfhustle.model.api.objects.MapBlockFlags.EMPTY;
import static com.anrisoftware.dwarfhustle.model.api.objects.MapBlockFlags.FILLED;
import static com.anrisoftware.dwarfhustle.model.api.objects.MapBlockFlags.LIQUID;
import static com.anrisoftware.dwarfhustle.model.api.objects.MapBlockFlags.RAMP;
import static com.anrisoftware.dwarfhustle.model.api.objects.MapChunk.cid2Id;
import static com.anrisoftware.dwarfhustle.model.api.objects.NeighboringDir.D;
import static com.anrisoftware.dwarfhustle.model.api.objects.NeighboringDir.E;
import static com.anrisoftware.dwarfhustle.model.api.objects.NeighboringDir.N;
import static com.anrisoftware.dwarfhustle.model.api.objects.NeighboringDir.S;
import static com.anrisoftware.dwarfhustle.model.api.objects.NeighboringDir.SE;
import static com.anrisoftware.dwarfhustle.model.api.objects.NeighboringDir.U;
import static com.anrisoftware.dwarfhustle.model.api.objects.NeighboringDir.W;

import com.anrisoftware.dwarfhustle.model.api.objects.GameChunkPos;
import com.anrisoftware.dwarfhustle.model.api.objects.MapChunk;
import com.anrisoftware.dwarfhustle.model.api.objects.NeighboringDir;
import com.anrisoftware.dwarfhustle.model.api.objects.ObjectsGetter;
import com.anrisoftware.dwarfhustle.model.api.objects.ObjectsSetter;
import com.anrisoftware.dwarfhustle.model.db.buffers.MapBlockBuffer;

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

    @ToString.Exclude
    public final ObjectsGetter getter;

    @ToString.Exclude
    public final ObjectsSetter setter;

    public final MapChunk chunk;

    public final int x;

    public final int y;

    public final int z;

    public final int w;

    public final int h;

    public final int d;

    public int getW1() {
        return w - 1;
    }

    public int getH1() {
        return h - 1;
    }

    public int getD1() {
        return d - 1;
    }

    public static void setMaterial(BlockFact fact, int x, int y, int z, int m) {
        final int off = getByteOffset(fact.chunk.pos, x, y, z);
        MapBlockBuffer.setMaterial(fact.chunk.getBlocks(), off, m);
    }

    public void setMaterial(int x, int y, int z, int m) {
        setMaterial(this, x, y, z, m);
    }

    public void setMaterial(int m) {
        setMaterial(x, y, z, m);
    }

    public static int getMaterial(BlockFact fact, int x, int y, int z) {
        final int off = getByteOffset(fact.chunk.pos, x, y, z);
        return MapBlockBuffer.getMaterial(fact.chunk.getBlocks(), off);
    }

    public int getMaterial(int x, int y, int z) {
        return getMaterial(this, x, y, z);
    }

    public int getMaterial() {
        return getMaterial(x, y, z);
    }

    public static void setObject(BlockFact fact, int x, int y, int z, int o) {
        final int off = getByteOffset(fact.chunk.pos, x, y, z);
        MapBlockBuffer.setObject(fact.chunk.getBlocks(), off, o);
    }

    public void setObject(int x, int y, int z, int o) {
        setObject(this, x, y, z, o);
    }

    public void setObject(int o) {
        setObject(x, y, z, o);
    }

    public static int getObject(BlockFact fact, int x, int y, int z) {
        final int off = getByteOffset(fact.chunk.pos, x, y, z);
        return MapBlockBuffer.getObject(fact.chunk.getBlocks(), off);
    }

    public int getObject(int x, int y, int z) {
        return getObject(this, x, y, z);
    }

    public int getObject() {
        return getObject(x, y, z);
    }

    public static void setProp(BlockFact fact, int x, int y, int z, int p) {
        final int off = getByteOffset(fact.chunk.pos, x, y, z);
        MapBlockBuffer.setProp(fact.chunk.getBlocks(), off, p);
    }

    public void setProp(int x, int y, int z, int p) {
        setProp(this, x, y, z, p);
    }

    public void setProp(int p) {
        setProp(x, y, z, p);
    }

    public static int getProp(BlockFact fact, int x, int y, int z) {
        final int off = getByteOffset(fact.chunk.pos, x, y, z);
        return MapBlockBuffer.getProp(fact.chunk.getBlocks(), off);
    }

    public static int getProp(MapChunk chunk, int x, int y, int z) {
        final int off = getByteOffset(chunk.pos, x, y, z);
        return MapBlockBuffer.getProp(chunk.getBlocks(), off);
    }

    public int getProp(int x, int y, int z) {
        return getProp(this, x, y, z);
    }

    public int getProp() {
        return getProp(x, y, z);
    }

    public static boolean isProp(BlockFact fact, int x, int y, int z, int flags) {
        return isFlag(getProp(fact, x, y, z), flags);
    }

    public static boolean isProp(MapChunk chunk, int x, int y, int z, int flags) {
        return isFlag(getProp(chunk, x, y, z), flags);
    }

    private static boolean isFlag(int p, int flags) {
        return (p & flags) == flags;
    }

    public boolean isProp(int x, int y, int z, int flags) {
        return isProp(this, x, y, z, flags);
    }

    public boolean isProp(int flags) {
        return isProp(x, y, z, flags);
    }

    public static void addProp(BlockFact fact, int x, int y, int z, int flags) {
        int p = getProp(fact, x, y, z);
        setProp(fact, x, y, z, p | flags);
    }

    public void addProp(int x, int y, int z, int flags) {
        addProp(this, x, y, z, flags);
    }

    public void addProp(int flags) {
        addProp(x, y, z, flags);
    }

    public static void removeProp(BlockFact fact, int x, int y, int z, int flags) {
        int p = getProp(fact, x, y, z);
        setProp(fact, x, y, z, p & ~flags);
    }

    public void removeProp(int x, int y, int z, int flags) {
        removeProp(this, x, y, z, flags);
    }

    public void removeProp(int flags) {
        removeProp(x, y, z, flags);
    }

    public static void setTemp(BlockFact fact, int x, int y, int z, int t) {
        final int off = getByteOffset(fact.chunk.pos, x, y, z);
        MapBlockBuffer.setTemp(fact.chunk.getBlocks(), off, t);
    }

    public void setTemp(int x, int y, int z, int t) {
        setTemp(this, x, y, z, t);
    }

    public void setTemp(int t) {
        setTemp(x, y, z, t);
    }

    public static int getTemp(BlockFact fact, int x, int y, int z) {
        final int off = getByteOffset(fact.chunk.pos, x, y, z);
        return MapBlockBuffer.getTemp(fact.chunk.getBlocks(), off);
    }

    public int getTemp(int x, int y, int z) {
        return getTemp(this, x, y, z);
    }

    public int getTemp() {
        return getTemp(x, y, z);
    }

    public static void setLux(BlockFact fact, int x, int y, int z, int l) {
        final int off = getByteOffset(fact.chunk.pos, x, y, z);
        MapBlockBuffer.setLux(fact.chunk.getBlocks(), off, l);
    }

    public void setLux(int x, int y, int z, int l) {
        setLux(this, x, y, z, l);
    }

    public void setLux(int l) {
        setLux(x, y, z, l);
    }

    public static int getLux(BlockFact fact, int x, int y, int z) {
        final int off = getByteOffset(fact.chunk.pos, x, y, z);
        return MapBlockBuffer.getLux(fact.chunk.getBlocks(), off);
    }

    public int getLux(int x, int y, int z) {
        return getLux(this, x, y, z);
    }

    public int getLux() {
        return getLux(x, y, z);
    }

    private static int getByteOffset(GameChunkPos pos, int x, int y, int z) {
        return calcIndex(pos.getSizeX(), pos.getSizeY(), pos.getSizeZ(), pos.x, pos.y, pos.z, x, y, z)
                * MapBlockBuffer.SIZE;
    }

    /**
     * Returns true if the current block is not on the map edge.
     */
    public boolean isNotEdge() {
        return x > 0 && x < w && y > 0 && y < h;
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

    public boolean isNeighborsSameLevelPerpExist() {
        return isNeighborsExist(NeighboringDir.DIRS_PERPENDICULAR_SAME_LEVEL);
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
    public boolean isNeighborsFilledIfExist(NeighboringDir... dirs) {
        return isNeighborsExist(dirs) && isNeighborsFlag(FILLED.flag, dirs);
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
     * Returns true if the perpendicular neighbors on the same level are empty.
     */
    public boolean isNeighborsSameLevelPerpEmpty() {
        return isNeighborsFlag(EMPTY.flag, NeighboringDir.DIRS_PERPENDICULAR_SAME_LEVEL);
    }

    /**
     * Returns true if the neighbors on the same level are filled.
     */
    public boolean isNeighborsSameLevelFilled() {
        return isNeighborsFlag(FILLED.flag, NeighboringDir.DIRS_SAME_LEVEL);
    }

    private boolean isNeighborFlag(MapChunk chunk, NeighboringDir dir, int flag) {
        if (!isNeighborsExist(dir)) {
            return false;
        }
        int dx = x + dir.pos.x;
        int dy = y + dir.pos.y;
        int dz = z + dir.pos.z;
        if (dx < chunk.pos.x) {
            chunk = getter.get(MapChunk.OBJECT_TYPE, cid2Id(chunk.neighbors[NeighboringDir.W.ordinal()]));
            return isNeighborFlag(chunk, dir, flag);
        }
        if (dy < chunk.pos.y) {
            chunk = getter.get(MapChunk.OBJECT_TYPE, cid2Id(chunk.neighbors[NeighboringDir.N.ordinal()]));
            return isNeighborFlag(chunk, dir, flag);
        }
        if (dz < chunk.pos.z) {
            chunk = getter.get(MapChunk.OBJECT_TYPE, cid2Id(chunk.neighbors[NeighboringDir.U.ordinal()]));
            return isNeighborFlag(chunk, dir, flag);
        }
        if (dx >= chunk.pos.ep.x) {
            chunk = getter.get(MapChunk.OBJECT_TYPE, cid2Id(chunk.neighbors[NeighboringDir.E.ordinal()]));
            return isNeighborFlag(chunk, dir, flag);
        }
        if (dy >= chunk.pos.ep.y) {
            chunk = getter.get(MapChunk.OBJECT_TYPE, cid2Id(chunk.neighbors[NeighboringDir.S.ordinal()]));
            return isNeighborFlag(chunk, dir, flag);
        }
        if (dz >= chunk.pos.ep.z) {
            chunk = getter.get(MapChunk.OBJECT_TYPE, cid2Id(chunk.neighbors[D.ordinal()]));
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
                final int p = getProp(c, x, y, zz);
                if (!isFlag(p, EMPTY.flag) && !isFlag(p, LIQUID.flag)) {
                    return false;
                } else {
                    zz--;
                    continue;
                }
            } else {
                c = getter.get(MapChunk.OBJECT_TYPE, cid2Id(c.neighbors[NeighboringDir.U.ordinal()]));
            }
        }
        return true;
    }

    public boolean testCornerNw() {
        boolean ret = z > 0;
        ret &= isProp(FILLED.flag);
        ret &= isNotEdge();
        ret &= !isNeighborsFilled(U);
        ret &= isNeighborsExist(N, SE, W, E, S);
        ret &= isNeighborsEmpty(N, SE, W);
        ret &= isNeighborsFilled(E, S);
        return ret;
    }
}
