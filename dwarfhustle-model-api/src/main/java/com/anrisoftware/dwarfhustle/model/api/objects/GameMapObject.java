/*
 * dwarfhustle-model-api - Manages the compile dependencies for the model.
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
package com.anrisoftware.dwarfhustle.model.api.objects;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Game object on the game map.
 * <p>
 * Size 28 bytes
 * <ul>
 * <li>8(super)
 * <li>8(map)
 * <li>12(pos)
 * </ul>
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
public abstract class GameMapObject extends GameObject implements StoredObject {

    public static final int VISIBLE_POS = 0;

    public static final int FORBIDDEN_POS = 1;

    public static final int MODEL_POS = 2;

    public static final int TEX_POS = 3;

    public static final int SELECT_POS = 9;

    public static final int ELEVATED_POS = 10;

    /**
     * Record ID set after the object was once stored in the backend.
     */
    private Serializable rid;

    /**
     * ID of the {@link GameMap}.
     */
    private long map = 0;

    /**
     * Sets the X, Y and Z position of a {@link GameMapObject} on the game map.
     */
    private GameBlockPos pos = new GameBlockPos();

    /**
     * Knowledge ID.
     */
    private int kid;

    /**
     * Knowledge object ID.
     */
    private int oid;

    /**
     * Properties of the object.
     * <p>
     * Exclusive Flags:
     *
     * <pre>
     * 00000000 00000000 hidden.
     * 00000000 00000001 visible.
     * 00000000 00000010 forbidden.
     * 00000000 00000100 have model.
     * 00000000 00001000 have texture.
     * </pre>
     * <p>
     * Inclusive Flags:
     *
     * <pre>
     * 00000001 00000000 can be selected.
     * 00000010 00000000 elevated from terrain.
     * </pre>
     */
    private PropertiesSet p = new PropertiesSet();

    /**
     * Temperature.
     */
    private int temp;

    /**
     * Light lux.
     */
    private int lux;

    public GameMapObject(long id) {
        super(id);
    }

    public GameMapObject(byte[] idbuf) {
        super(idbuf);
    }

    public GameMapObject(long id, GameBlockPos pos) {
        super(id);
        this.pos = pos;
    }

    public GameMapObject(byte[] idbuf, GameBlockPos pos) {
        super(idbuf);
        this.pos = pos;
    }

    public void setPos(GameBlockPos pos) {
        this.pos.set(pos);
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

    public void setForbidden(boolean flag) {
        if (flag) {
            p.set(FORBIDDEN_POS);
        } else {
            p.clear(FORBIDDEN_POS);
        }
    }

    public boolean isForbidden() {
        return p.get(FORBIDDEN_POS);
    }

    public void setHaveModel(boolean flag) {
        if (flag) {
            p.set(MODEL_POS);
        } else {
            p.clear(MODEL_POS);
        }
    }

    public boolean isHaveModel() {
        return p.get(MODEL_POS);
    }

    public void setHaveTex(boolean flag) {
        if (flag) {
            p.set(TEX_POS);
        } else {
            p.clear(TEX_POS);
        }
    }

    public boolean isHaveTex() {
        return p.get(TEX_POS);
    }

    public void setCanSelect(boolean flag) {
        if (flag) {
            p.set(SELECT_POS);
        } else {
            p.clear(SELECT_POS);
        }
    }

    public boolean isCanSelect() {
        return p.get(SELECT_POS);
    }

    public void setElevated(boolean flag) {
        if (flag) {
            p.set(ELEVATED_POS);
        } else {
            p.clear(ELEVATED_POS);
        }
    }

    public boolean isElevated() {
        return p.get(ELEVATED_POS);
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
        super.writeStream(out);
        out.writeLong(map);
        getPos().writeStream(out);
        out.writeInt(kid);
        out.writeInt(oid);
        out.writeInt(temp);
        out.writeInt(lux);
    }

    @Override
    public void readStream(DataInput in) throws IOException {
        super.readStream(in);
        this.map = in.readLong();
        this.pos.readStream(in);
        this.kid = in.readInt();
        this.oid = in.readInt();
        this.temp = in.readInt();
        this.lux = in.readInt();
    }

}
