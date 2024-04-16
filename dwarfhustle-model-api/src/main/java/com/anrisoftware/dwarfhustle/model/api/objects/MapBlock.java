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

    private static final int FILLED_POS = 0;

    private static final int EMPTY_POS = 1;

    private static final int LIQUID_POS = 2;

    private static final int RAMP_POS = 3;

    private static final int FLOOR_POS = 4;

    private static final int ROOF_POS = 5;

    private static final long serialVersionUID = 1L;

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
     * ID of the material.
     */
    public long material = -1;

    /**
     * ID of the object.
     */
    public long object = -1;

    /**
     * Bit field that defines the properties of the map block.
     * 
     * <pre>
     * 00000000 00000000 00000000 00000001} block-filled Filled with solid.
     * 00000000 00000000 00000000 00000010} block-empty with gas.
     * 00000000 00000000 00000000 00000100} block-liquid Filled with liquid.
     * 00000000 00000000 00000000 00001000} block-ramp Ramp block.
     * 00000000 00000000 00000000 00010000} block-floor Floor block.
     * 00000000 00000000 00000000 00100000} block-roof Roof block.
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
     * Sets the RID of the material.
     */
    public void setMaterialRid(long material) {
        setMaterial(KnowledgeObject.kid2Id(material));
    }

    /**
     * Returns the material RID.
     */
    public long getMaterialRid() {
        return KnowledgeObject.id2Kid(material);
    }

    /**
     * Sets the RID of the material.
     */
    public void setObjectRid(long object) {
        setObject(KnowledgeObject.kid2Id(object));
    }

    /**
     * Returns the object RID.
     */
    public long getObjectRid() {
        return KnowledgeObject.id2Kid(object);
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

}
