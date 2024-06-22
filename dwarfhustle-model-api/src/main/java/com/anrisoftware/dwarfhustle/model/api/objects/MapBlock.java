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
import java.util.function.Function;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Block on the game map.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@ToString
@Getter
@Setter
public class MapBlock implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final int VISIBLE_POS = 0;

    private static final int FILLED_POS = 1;

    private static final int EMPTY_POS = 2;

    private static final int LIQUID_POS = 3;

    private static final int RAMP_POS = 4;

    private static final int FLOOR_POS = 5;

    private static final int ROOF_POS = 6;

    private static final int DISCOVERED_POS = 7;

    private static final int HAVE_ROOF_POS = 8;

    private static final int HAVE_FLOOR_POS = 9;

    public static final String OBJECT_TYPE = MapBlock.class.getSimpleName();

    /**
     * The {@link GameBlockPos} of the block.
     */
    public GameBlockPos pos = new GameBlockPos();

    /**
     * CID of the parent {@link MapChunk}.
     */
    public int parent = 0;

    /**
     * RID of the material.
     */
    public int material = -1;

    /**
     * RID of the object.
     */
    public int object = -1;

    /**
     * Temperature.
     */
    public int temp;

    /**
     * Light lux.
     */
    public int lux;

    /**
     * Bit field that defines the properties of the map block.
     * 
     * <pre>
     * Exclusive Flags:
     * 00000000 00000000 Block is hidden.
     * 00000000 00000001 Block is visible.
     * 00000000 00000010 block-filled Filled with solid.
     * 00000000 00000100 block-empty Filled with gas.
     * 00000000 00001000 block-liquid Filled with liquid.
     * 00000000 00010000 block-ramp Ramp block.
     * 00000000 00100000 block-floor Floor block.
     * 00000000 01000000 block-roof Roof block.
     * Inclusive Flags:
     * 00000000 10000000 block-discovered
     * 00000001 00000000 have-roof
     * 00000010 00000000 have-floor
     * </pre>
     */
    public PropertiesSet p = new PropertiesSet();

    public MapBlock() {
        this.pos = new GameBlockPos();
        this.p = new PropertiesSet();
    }

    public MapBlock(int parent, GameBlockPos pos) {
        this.parent = parent;
        this.pos = pos;
    }

    /**
     * Sets the ID of the material.
     */
    public void setMaterialId(long material) {
        setMaterial(KnowledgeObject.id2Kid(material));
    }

    /**
     * Returns the material ID.
     */
    public long getMaterialId() {
        return KnowledgeObject.kid2Id(material);
    }

    /**
     * Sets the ID of the material.
     */
    public void setObjectId(long object) {
        setObject(KnowledgeObject.id2Kid(object));
    }

    /**
     * Returns the object ID.
     */
    public long getObjectId() {
        return KnowledgeObject.kid2Id(object);
    }

    public void setProperties(int p) {
        this.p.bits = p;
    }

    public void setHidden(boolean flag) {
        if (!flag) {
            p.set(VISIBLE_POS);
        } else {
            p.clear(VISIBLE_POS);
        }
    }

    public boolean isHidden() {
        return !p.get(VISIBLE_POS);
    }

    public void setVisible(boolean flag) {
        if (flag) {
            p.set(VISIBLE_POS);
        } else {
            p.clear(VISIBLE_POS);
        }
    }

    public boolean isVisible() {
        return p.get(VISIBLE_POS);
    }

    public void setFilled(boolean flag) {
        if (flag) {
            p.set(FILLED_POS);
        } else {
            p.clear(FILLED_POS);
        }
    }

    public boolean isFilled() {
        return p.get(FILLED_POS);
    }

    public void setEmpty(boolean flag) {
        if (flag) {
            p.set(EMPTY_POS);
        } else {
            p.clear(EMPTY_POS);
        }
    }

    public boolean isEmpty() {
        return p.get(EMPTY_POS);
    }

    public void setLiquid(boolean flag) {
        if (flag) {
            p.set(LIQUID_POS);
        } else {
            p.clear(LIQUID_POS);
        }
    }

    public boolean isLiquid() {
        return p.get(LIQUID_POS);
    }

    public void setRamp(boolean flag) {
        if (flag) {
            p.set(RAMP_POS);
        } else {
            p.clear(RAMP_POS);
        }
    }

    public boolean isRamp() {
        return p.get(RAMP_POS);
    }

    public void setFloor(boolean flag) {
        if (flag) {
            p.set(FLOOR_POS);
        } else {
            p.clear(FLOOR_POS);
        }
    }

    public boolean isFloor() {
        return p.get(FLOOR_POS);
    }

    public void setRoof(boolean flag) {
        if (flag) {
            p.set(ROOF_POS);
        } else {
            p.clear(ROOF_POS);
        }
    }

    public boolean isRoof() {
        return p.get(ROOF_POS);
    }

    public void setDiscovered(boolean flag) {
        if (flag) {
            p.set(DISCOVERED_POS);
        } else {
            p.clear(DISCOVERED_POS);
        }
    }

    public boolean isDiscovered() {
        return p.get(DISCOVERED_POS);
    }

    public void setHaveRoof(boolean flag) {
        if (flag) {
            p.set(HAVE_ROOF_POS);
        } else {
            p.clear(HAVE_ROOF_POS);
        }
    }

    public boolean isHaveRoof() {
        return p.get(HAVE_ROOF_POS);
    }

    public void setHaveFloor(boolean flag) {
        if (flag) {
            p.set(HAVE_FLOOR_POS);
        } else {
            p.clear(HAVE_FLOOR_POS);
        }
    }

    public boolean isHaveFloor() {
        return p.get(HAVE_FLOOR_POS);
    }

    public MapBlock getNeighbor(NeighboringDir dir, MapChunk chunk, Function<Integer, MapChunk> retriever) {
        var dirpos = this.pos.add(dir.pos);
        if (dirpos.isNegative()) {
            return null;
        }
        if (chunk.isInside(dirpos)) {
            return chunk.getBlock(dirpos);
        } else {
            var parent = retriever.apply(chunk.parent);
            return parent.findBlock(dirpos, retriever);
        }
    }

    public MapBlock getNeighborNorth(MapChunk chunk, Function<Integer, MapChunk> retriever) {
        return getNeighbor(NeighboringDir.N, chunk, retriever);
    }

    public MapBlock getNeighborSouth(MapChunk chunk, Function<Integer, MapChunk> retriever) {
        return getNeighbor(NeighboringDir.S, chunk, retriever);
    }

    public MapBlock getNeighborEast(MapChunk chunk, Function<Integer, MapChunk> retriever) {
        return getNeighbor(NeighboringDir.E, chunk, retriever);
    }

    public MapBlock getNeighborWest(MapChunk chunk, Function<Integer, MapChunk> retriever) {
        return getNeighbor(NeighboringDir.W, chunk, retriever);
    }

    public MapBlock getNeighborUp(MapChunk chunk, Function<Integer, MapChunk> retriever) {
        return getNeighbor(NeighboringDir.U, chunk, retriever);
    }

    public boolean isNeighborsUpEmptyContinuously(MapChunk chunk, Function<Integer, MapChunk> retriever) {
        MapBlock up = getNeighbor(NeighboringDir.U, chunk, retriever);
        while (up != null) {
            if (!up.isEmpty()) {
                return false;
            }
            if (parent != up.parent) {
                chunk = retriever.apply(up.parent);
            }
            up = up.getNeighbor(NeighboringDir.U, chunk, retriever);
        }
        return true;
    }

    /**
     * Returns true if the neighbors exist.
     */
    public boolean isNeighborsExist(MapBlock[] neighbors, NeighboringDir... dirs) {
        for (var dir : dirs) {
            if (neighbors[dir.ordinal()] == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if the neighbors on the same level (N,E,S,W) exist.
     */
    public boolean isNeighborsSameLevelExist(MapBlock[] neighbors) {
        return isNeighborsExist(neighbors, NeighboringDir.DIRS_SAME_LEVEL);
    }

    /**
     * Returns true if the neighbors on the same level (NESW) of this block are
     * empty.
     */
    public boolean isNeighborsSameLevelEmpty(MapBlock[] neighbors) {
        for (var dir : NeighboringDir.DIRS_SAME_LEVEL) {
            if (neighbors[dir.ordinal()] != null) {
                if (!neighbors[dir.ordinal()].isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns true if the neighbors on the same level (NESW) of this block are
     * filled.
     */
    public boolean isNeighborsSameLevelFilled(MapBlock[] neighbors) {
        for (var dir : NeighboringDir.DIRS_SAME_LEVEL) {
            if (neighbors[dir.ordinal()] != null) {
                if (!neighbors[dir.ordinal()].isFilled()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns true one of the N, E, S, W neighbors of this block is empty.
     */
    public boolean isOneNeighborPerpendicularEmpty(MapBlock[] neighbors) {
        for (var dir : NeighboringDir.DIRS_PERPENDICULAR_SAME_LEVEL) {
            if (neighbors[dir.ordinal()] != null) {
                if (neighbors[dir.ordinal()].isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if one the edge NE, SE, SW, NW neighbors of this block is empty.
     */
    public boolean isOneNeighborEdgeEmpty(MapBlock[] neighbors) {
        for (var dir : NeighboringDir.DIRS_EDGE_SAME_LEVEL) {
            if (neighbors[dir.ordinal()] != null) {
                if (neighbors[dir.ordinal()].isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if none the edge NE, SE, SW, NW neighbors of this block are
     * empty.
     */
    public boolean isNeighborsEdgeNotEmpty(MapBlock[] neighbors) {
        for (var dir : NeighboringDir.DIRS_EDGE_SAME_LEVEL) {
            if (neighbors[dir.ordinal()] != null) {
                if (neighbors[dir.ordinal()].isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns true if the U neighbor of this block is filled.
     */
    public boolean isNeighborUpFilled(MapBlock[] neighbors) {
        if (neighbors[NeighboringDir.U.ordinal()] != null) {
            return neighbors[NeighboringDir.U.ordinal()].isFilled();
        }
        return false;
    }

    public boolean isNeighborsEmpty(MapBlock[] neighbors, NeighboringDir... dirs) {
        for (var dir : dirs) {
            if (neighbors[dir.ordinal()] != null) {
                if (!neighbors[dir.ordinal()].isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isNeighborsEmpty(MapBlock[] neighbors, int... dirs) {
        for (var dir : dirs) {
            if (neighbors[dir] != null) {
                if (!neighbors[dir].isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isNeighborsFilled(MapBlock[] neighbors, NeighboringDir... dirs) {
        for (var dir : dirs) {
            if (neighbors[dir.ordinal()] != null) {
                if (!neighbors[dir.ordinal()].isFilled()) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isNeighborsFilled(MapBlock[] neighbors, int... dirs) {
        for (var dir : dirs) {
            if (neighbors[dir] != null) {
                if (!neighbors[dir].isFilled()) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isNeighborsRamp(MapBlock[] neighbors, NeighboringDir... dirs) {
        for (var dir : dirs) {
            if (neighbors[dir.ordinal()] != null) {
                if (!neighbors[dir.ordinal()].isRamp()) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isNeighborsRamp(MapBlock[] neighbors, int... dirs) {
        for (var dir : dirs) {
            if (neighbors[dir] != null) {
                if (!neighbors[dir].isRamp()) {
                    return false;
                }
            }
        }
        return true;
    }
}
