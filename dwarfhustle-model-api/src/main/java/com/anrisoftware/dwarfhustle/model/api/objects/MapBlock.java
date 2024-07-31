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

    private static final int HAVE_CEILING_POS = 8;

    private static final int HAVE_FLOOR_POS = 9;

    private static final int HAVE_NATURAL_LIGHT_POS = 10;

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
     * 00000001 00000000 have-ceiling
     * 00000010 00000000 have-floor
     * 00000100 00000000 have-natural-light
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

    public void setHaveCeiling(boolean flag) {
        if (flag) {
            p.set(HAVE_CEILING_POS);
        } else {
            p.clear(HAVE_CEILING_POS);
        }
    }

    public boolean isHaveCeiling() {
        return p.get(HAVE_CEILING_POS);
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

    public void setHaveNaturalLight(boolean flag) {
        if (flag) {
            p.set(HAVE_NATURAL_LIGHT_POS);
        } else {
            p.clear(HAVE_NATURAL_LIGHT_POS);
        }
    }

    public boolean isHaveNaturalLight() {
        return p.get(HAVE_NATURAL_LIGHT_POS);
    }

}
