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

import com.google.common.base.Objects;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Tile on the game map.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Getter
public class MapTile extends GameMapObject {

    private static final int MINED_POS = 0;

    private static final int NATURAL_ROOF_POS = 1;

    private static final int NATURAL_FLOOR_POS = 2;

    private static final int RAMP_POS = 3;

    private static final long serialVersionUID = 1L;

    public static final String OBJECT_TYPE = MapTile.class.getSimpleName();

    /**
     * ID of the material.
     */
    private long material = -1;

    /**
     * Bit field that defines the properties of the map tile.
     * <ul>
     * <li>{@code 0000 0000 0000 0000 0000 0000 0000 0001} - mined
     * <li>{@code 0000 0000 0000 0000 0000 0000 0000 0010} - natural roof
     * <li>{@code 0000 0000 0000 0000 0000 0000 0000 0100} - natural floor
     * <li>{@code 0000 0000 0000 0000 0000 0000 0000 1000} - ramp
     * </ul>
     */
    private PropertiesSet p = new PropertiesSet();

    public MapTile(long id) {
        super(id);
    }

    public MapTile(byte[] idbuf) {
        super(idbuf);
    }

    @Override
    public String getObjectType() {
        return OBJECT_TYPE;
    }

    /**
     * Sets the ID of the material.
     */
    public void setMaterial(long material) {
        if (this.material != material) {
            setDirty(true);
            this.material = material;
        }
    }

    public void setP(PropertiesSet p) {
        if (!Objects.equal(this.p, p)) {
            setDirty(true);
            this.p = p;
        }
    }

    public void setMined(boolean mined) {
        p.set(MINED_POS);
        setDirty(true);
    }

    public boolean isMined() {
        return p.get(MINED_POS);
    }

    public void setNaturalRoof(boolean roof) {
        p.set(roof, NATURAL_ROOF_POS);
        setDirty(true);
    }

    public boolean isNaturalRoof() {
        return p.get(NATURAL_ROOF_POS);
    }

    public void setNaturalFloor(boolean floor) {
        p.set(floor, NATURAL_FLOOR_POS);
        setDirty(true);
    }

    public boolean isNaturalFloor() {
        return p.get(NATURAL_FLOOR_POS);
    }

    public void setRamp(boolean ramp) {
        p.set(ramp, RAMP_POS);
        setDirty(true);
    }

    public boolean isRamp() {
        return p.get(RAMP_POS);
    }
}
