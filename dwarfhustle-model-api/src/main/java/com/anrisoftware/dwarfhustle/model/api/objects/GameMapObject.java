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
public abstract class GameMapObject extends GameObject implements StoredObject, GameMapObjectProperties {

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
     * Sets the X width, Y height and Z depth of the object.
     */
    private GameBlockPos size = new GameBlockPos(1, 1, 1);

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
        p.writeStream(out);
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
        this.p.readStream(in);
        this.temp = in.readInt();
        this.lux = in.readInt();
    }

}
