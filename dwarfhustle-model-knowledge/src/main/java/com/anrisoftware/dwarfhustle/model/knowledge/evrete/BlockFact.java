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
import static com.anrisoftware.dwarfhustle.model.db.buffers.MapBlockBuffer.calcOff;
import static com.anrisoftware.dwarfhustle.model.db.buffers.MapChunkBuffer.findChunk;

import com.anrisoftware.dwarfhustle.model.api.objects.MapChunk;
import com.anrisoftware.dwarfhustle.model.api.objects.NeighboringDir;
import com.anrisoftware.dwarfhustle.model.api.objects.ObjectsGetter;
import com.anrisoftware.dwarfhustle.model.api.objects.ObjectsSetter;
import com.anrisoftware.dwarfhustle.model.db.buffers.MapBlockBuffer;
import com.anrisoftware.dwarfhustle.model.db.buffers.MapChunkBuffer;

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

    @ToString.Exclude
    public final MapChunk root;

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

    private MapChunk getCorrectChunk(MapChunk chunk, int x, int y, int z) {
        if (!chunk.isInside(x, y, z)) {
            chunk = findChunk(chunk, x, y, z, getter);
        }
        return chunk;
    }

    private void setMaterialDirect(MapChunk chunk, int x, int y, int z, int m) {
        final int off = calcOff(chunk, x, y, z);
        MapBlockBuffer.setMaterial(chunk.getBlocks(), off, m);
    }

    public void setMaterial(int x, int y, int z, int m) {
        var chunk = findChunk(root, x, y, z, getter);
        setMaterialDirect(chunk, x, y, z, m);
    }

    public void setMaterial(int m) {
        setMaterial(x, y, z, m);
    }

    private int getMaterialDirect(MapChunk chunk, int x, int y, int z) {
        final int off = calcOff(chunk, x, y, z);
        return MapBlockBuffer.getMaterial(chunk.getBlocks(), off);
    }

    public int getMaterial(MapChunk chunk, int x, int y, int z) {
        chunk = getCorrectChunk(chunk, x, y, z);
        return getMaterialDirect(chunk, x, y, z);
    }

    public int getMaterial() {
        return getMaterialDirect(chunk, x, y, z);
    }

    private void setObjectDirect(MapChunk chunk, int x, int y, int z, int o) {
        final int off = calcOff(chunk, x, y, z);
        MapBlockBuffer.setObject(chunk.getBlocks(), off, o);
    }

    public void setObject(MapChunk chunk, int x, int y, int z, int o) {
        chunk = getCorrectChunk(chunk, x, y, z);
        setObjectDirect(chunk, x, y, z, o);
    }

    public void setObject(int o) {
        setObjectDirect(chunk, x, y, z, o);
    }

    private int getObjectDirect(MapChunk chunk, int x, int y, int z) {
        final int off = calcOff(chunk, x, y, z);
        return MapBlockBuffer.getObject(chunk.getBlocks(), off);
    }

    public int getObject(MapChunk chunk, int x, int y, int z) {
        chunk = getCorrectChunk(chunk, x, y, z);
        return getObjectDirect(chunk, x, y, z);
    }

    public int getObject() {
        return getObjectDirect(chunk, x, y, z);
    }

    private void setPropDirect(MapChunk chunk, int x, int y, int z, int p) {
        final int off = calcOff(chunk, x, y, z);
        MapBlockBuffer.setProp(chunk.getBlocks(), off, p);
    }

    public void setProp(MapChunk chunk, int x, int y, int z, int p) {
        chunk = getCorrectChunk(chunk, x, y, z);
        setPropDirect(chunk, x, y, z, p);
    }

    public void setProp(int p) {
        setPropDirect(chunk, x, y, z, p);
    }

    private int getPropDirect(MapChunk chunk, int x, int y, int z) {
        final int off = calcOff(chunk, x, y, z);
        return MapBlockBuffer.getProp(chunk.getBlocks(), off);
    }

    public int getProp(MapChunk chunk, int x, int y, int z) {
        chunk = getCorrectChunk(chunk, x, y, z);
        return getPropDirect(chunk, x, y, z);
    }

    public int getProp() {
        return getPropDirect(chunk, x, y, z);
    }

    private boolean isPropDirect(MapChunk chunk, int x, int y, int z, int flags) {
        return isFlag(getProp(chunk, x, y, z), flags);
    }

    private boolean isFlag(int p, int flags) {
        return (p & flags) == flags;
    }

    public boolean isProp(MapChunk chunk, int x, int y, int z, int flags) {
        chunk = getCorrectChunk(chunk, x, y, z);
        return isPropDirect(chunk, x, y, z, flags);
    }

    public boolean isProp(int x, int y, int z, int flags) {
        return isPropDirect(chunk, x, y, z, flags);
    }

    public boolean isProp(int flags) {
        return isProp(x, y, z, flags);
    }

    private void addPropDirect(MapChunk chunk, int x, int y, int z, int flags) {
        int p = getPropDirect(chunk, x, y, z);
        setPropDirect(chunk, x, y, z, p | flags);
    }

    public void addProp(MapChunk chunk, int x, int y, int z, int flags) {
        chunk = getCorrectChunk(chunk, x, y, z);
        addPropDirect(chunk, x, y, z, flags);
    }

    public void addProp(int flags) {
        addPropDirect(chunk, x, y, z, flags);
    }

    private void removePropDirect(MapChunk chunk, int x, int y, int z, int flags) {
        int p = getPropDirect(chunk, x, y, z);
        setPropDirect(chunk, x, y, z, p & ~flags);
    }

    public void removeProp(MapChunk chunk, int x, int y, int z, int flags) {
        chunk = getCorrectChunk(chunk, x, y, z);
        removePropDirect(chunk, x, y, z, flags);
    }

    public void removeProp(int flags) {
        removePropDirect(chunk, x, y, z, flags);
    }

    private void setTempDirect(MapChunk chunk, int x, int y, int z, int t) {
        final int off = calcOff(chunk, x, y, z);
        MapBlockBuffer.setTemp(chunk.getBlocks(), off, t);
    }

    public void setTemp(MapChunk chunk, int x, int y, int z, int t) {
        chunk = getCorrectChunk(chunk, x, y, z);
        setTempDirect(chunk, x, y, z, t);
    }

    public void setTemp(int t) {
        setTempDirect(chunk, x, y, z, t);
    }

    private int getTempDirect(MapChunk chunk, int x, int y, int z) {
        final int off = calcOff(chunk, x, y, z);
        return MapBlockBuffer.getTemp(chunk.getBlocks(), off);
    }

    public int getTemp(MapChunk chunk, int x, int y, int z) {
        chunk = getCorrectChunk(chunk, x, y, z);
        return getTempDirect(chunk, x, y, z);
    }

    public int getTemp() {
        return getTempDirect(chunk, x, y, z);
    }

    private void setLuxDirect(MapChunk chunk, int x, int y, int z, int l) {
        final int off = calcOff(chunk, x, y, z);
        MapBlockBuffer.setLux(chunk.getBlocks(), off, l);
    }

    public void setLux(MapChunk chunk, int x, int y, int z, int l) {
        chunk = getCorrectChunk(chunk, x, y, z);
        setLuxDirect(chunk, x, y, z, l);
    }

    public void setLux(int l) {
        setLuxDirect(chunk, x, y, z, l);
    }

    private int getLuxDirect(MapChunk chunk, int x, int y, int z) {
        final int off = calcOff(chunk, x, y, z);
        return MapBlockBuffer.getLux(chunk.getBlocks(), off);
    }

    public int getLux(MapChunk chunk, int x, int y, int z) {
        chunk = getCorrectChunk(chunk, x, y, z);
        return getLuxDirect(chunk, x, y, z);
    }

    public int getLux() {
        return getLuxDirect(chunk, x, y, z);
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

    /**
     * Returns the index of the neighbor block.
     */
    public int getNeighbor(MapChunk chunk, NeighboringDir dir) {
        if (!isNeighborsExist(dir)) {
            return -1;
        }
        var res = MapChunkBuffer.findBlockIndex(chunk, dir.pos, getter);
        if (res.isValid()) {
            return res.index;
        } else {
            return -1;
        }
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
