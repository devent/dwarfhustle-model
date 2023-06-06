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

import org.eclipse.collections.api.map.primitive.IntLongMap;
import org.eclipse.collections.api.map.primitive.MutableIntLongMap;
import org.eclipse.collections.impl.factory.primitive.IntLongMaps;

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
public class MapBlock extends GameMapObject {

    private static final int MINED_POS = 0;

    private static final int NATURAL_ROOF_POS = 1;

    private static final int NATURAL_FLOOR_POS = 2;

    private static final int RAMP_POS = 3;

    private static final long serialVersionUID = 1L;

    public static final String OBJECT_TYPE = MapBlock.class.getSimpleName();

    /**
     * ID of the material.
     */
    private long material = -1;

    /**
     * ID of the object.
     */
    private long object = -1;

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

    /**
     * Contains the IDs of the blocks in each direction that are neighboring this
     * chunk.
     *
     * @see NeighboringDir
     */
    private IntLongMap blockDir = IntLongMaps.mutable.empty();

    public MapBlock(long id) {
        super(id);
    }

    public MapBlock(byte[] idbuf) {
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

    /**
     * Sets the ID of the object.
     */
    public void setObject(long object) {
        if (this.object != object) {
            setDirty(true);
            this.object = object;
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

    public boolean isSolid() {
        return !p.get(MINED_POS);
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

    public void setNeighbor(NeighboringDir dir, long id) {
        var m = (MutableIntLongMap) blockDir;
        m.put(dir.ordinal(), id);
        setDirty(true);
    }

    public long getNeighbor(NeighboringDir dir) {
        return blockDir.get(dir.ordinal());
    }

    public long getNeighborTop() {
        return blockDir.get(NeighboringDir.U.ordinal());
    }

    public void setNeighborTop(long id) {
        setNeighbor(NeighboringDir.U, id);
    }

    public long getNeighborBottom() {
        return blockDir.get(NeighboringDir.D.ordinal());
    }

    public void setNeighborBottom(long id) {
        setNeighbor(NeighboringDir.D, id);
    }

    public long getNeighborSouth() {
        return blockDir.get(NeighboringDir.S.ordinal());
    }

    public void setNeighborSouth(long id) {
        setNeighbor(NeighboringDir.S, id);
    }

    public long getNeighborEast() {
        return blockDir.get(NeighboringDir.E.ordinal());
    }

    public void setNeighborEast(long id) {
        setNeighbor(NeighboringDir.E, id);
    }

    public long getNeighborNorth() {
        return blockDir.get(NeighboringDir.N.ordinal());
    }

    public void setNeighborNorth(long id) {
        setNeighbor(NeighboringDir.N, id);
    }

    public long getNeighborWest() {
        return blockDir.get(NeighboringDir.W.ordinal());
    }

    public void setNeighborWest(long id) {
        setNeighbor(NeighboringDir.W, id);
    }

}
