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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Block on the game map. The block is not stored individually but inside the
 * {@link MapChunk}.
 * <p>
 * Size 376 bytes
 * <ul>
 * <li>12(pos)
 * <li>8(parent)
 * <li>8(material)
 * <li>8(object)
 * <li>4(p)
 * <li>26*12(dir)
 * <li>24(centerExtent)
 * </ul>
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@NoArgsConstructor
@Data
public class MapBlock implements Externalizable, StreamStorage {

    private static final int MINED_POS = 0;

    private static final int NATURAL_ROOF_POS = 1;

    private static final int NATURAL_FLOOR_POS = 2;

    private static final int RAMP_POS = 3;

    private static final long serialVersionUID = 1L;

    public static final String OBJECT_TYPE = MapBlock.class.getSimpleName();

    /**
     * Marker that the neighbor in the direction is empty.
     */
    public static final GameBlockPos DIR_EMPTY = new GameBlockPos();

    /**
     * The {@link GameBlockPos} of the block.
     */
    public GameBlockPos pos = new GameBlockPos();

    /**
     * ID of the parent {@link MapChunk}.
     */
    public long parent = 0;

    /**
     * ID of the material.
     */
    public long material = -1;

    /**
     * ID of the object.
     */
    public long object = -1;

    /**
     * Bit field that defines the properties of the map tile.
     * <ul>
     * <li>{@code 0000 0000 0000 0000 0000 0000 0000 0001} - mined
     * <li>{@code 0000 0000 0000 0000 0000 0000 0000 0010} - natural roof
     * <li>{@code 0000 0000 0000 0000 0000 0000 0000 0100} - natural floor
     * <li>{@code 0000 0000 0000 0000 0000 0000 0000 1000} - ramp
     * </ul>
     */
    public PropertiesSet p = new PropertiesSet();

    /**
     * Contains the {@link GameBlockPos} positions in each direction that are
     * neighboring this block. Empty directions are marked with {@link #DIR_EMPTY}.
     * The size is always 26.
     *
     * @see NeighboringDir
     */
    @ToString.Exclude
    public GameBlockPos[] dir = new GameBlockPos[] {
            new GameBlockPos(),
            new GameBlockPos(),
            new GameBlockPos(),
            new GameBlockPos(),
            new GameBlockPos(),
            //
            new GameBlockPos(),
            new GameBlockPos(),
            new GameBlockPos(),
            new GameBlockPos(),
            new GameBlockPos(),
            //
            new GameBlockPos(),
            new GameBlockPos(),
            new GameBlockPos(),
            new GameBlockPos(),
            new GameBlockPos(),
            //
            new GameBlockPos(),
            new GameBlockPos(),
            new GameBlockPos(),
            new GameBlockPos(),
            new GameBlockPos(),
            //
            new GameBlockPos(),
            new GameBlockPos(),
            new GameBlockPos(),
            new GameBlockPos(),
            new GameBlockPos(),
            //
            new GameBlockPos(),
    };

    @ToString.Exclude
    public CenterExtent centerExtent = new CenterExtent();

    public MapBlock(GameBlockPos pos) {
        this.pos = pos;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        writeStream(out);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        readStream(in);
    }

    @Override
    public void writeStream(DataOutput out) throws IOException {
        out.writeLong(parent);
        pos.writeStream(out);
        out.writeLong(material);
        out.writeLong(object);
        p.writeStream(out);
        for (var d : dir) {
            d.writeStream(out);
        }
        centerExtent.writeStream(out);
    }

    @Override
    public void readStream(DataInput in) throws IOException {
        this.parent = in.readLong();
        this.pos.readStream(in);
        this.material = in.readLong();
        this.object = in.readLong();
        this.p.readStream(in);
        for (int i = 0; i < dir.length; i++) {
            this.dir[i].readStream(in);
        }
        this.centerExtent.readStream(in);
    }

    /**
     * Updates the world coordinates center and extend of this chunk.
     */
    public void updateCenterExtent(float w, float h, float d) {
        float tx = -w + 2f * pos.x + 1f;
        float ty = h - 2f * pos.y - 1f;
        this.centerExtent = new CenterExtent(tx, ty, 0, 1f, 1f, 1f);
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

    public void setMined(boolean mined) {
        p.set(MINED_POS);
    }

    public boolean isMined() {
        return p.get(MINED_POS);
    }

    public boolean isSolid() {
        return !p.get(MINED_POS);
    }

    public void setNaturalRoof(boolean roof) {
        p.set(roof, NATURAL_ROOF_POS);
    }

    public boolean isNaturalRoof() {
        return p.get(NATURAL_ROOF_POS);
    }

    public void setNaturalFloor(boolean floor) {
        p.set(floor, NATURAL_FLOOR_POS);
    }

    public boolean isNaturalFloor() {
        return p.get(NATURAL_FLOOR_POS);
    }

    public void setRamp(boolean ramp) {
        p.set(ramp, RAMP_POS);
    }

    public boolean isRamp() {
        return p.get(RAMP_POS);
    }

    public void setNeighbor(NeighboringDir dir, GameBlockPos pos) {
        this.dir[dir.ordinal()] = pos;
    }

    public GameBlockPos getNeighbor(NeighboringDir dir) {
        return this.dir[dir.ordinal()];
    }

    public boolean haveNeighbor(NeighboringDir dir) {
        return !getNeighbor(dir).equals(DIR_EMPTY);
    }

    public boolean haveNotNeighbor(NeighboringDir dir) {
        return getNeighbor(dir).equals(DIR_EMPTY);
    }

    public GameBlockPos getNeighborTop() {
        return dir[NeighboringDir.U.ordinal()];
    }

    public void setNeighborTop(GameBlockPos pos) {
        setNeighbor(NeighboringDir.U, pos);
    }

    public GameBlockPos getNeighborBottom() {
        return dir[NeighboringDir.D.ordinal()];
    }

    public void setNeighborBottom(GameBlockPos pos) {
        setNeighbor(NeighboringDir.D, pos);
    }

    public GameBlockPos getNeighborSouth() {
        return dir[NeighboringDir.S.ordinal()];
    }

    public void setNeighborSouth(GameBlockPos pos) {
        setNeighbor(NeighboringDir.S, pos);
    }

    public GameBlockPos getNeighborEast() {
        return dir[NeighboringDir.E.ordinal()];
    }

    public void setNeighborEast(GameBlockPos pos) {
        setNeighbor(NeighboringDir.E, pos);
    }

    public GameBlockPos getNeighborNorth() {
        return dir[NeighboringDir.N.ordinal()];
    }

    public void setNeighborNorth(GameBlockPos pos) {
        setNeighbor(NeighboringDir.N, pos);
    }

    public GameBlockPos getNeighborWest() {
        return dir[NeighboringDir.W.ordinal()];
    }

    public void setNeighborWest(GameBlockPos pos) {
        setNeighbor(NeighboringDir.W, pos);
    }

}
