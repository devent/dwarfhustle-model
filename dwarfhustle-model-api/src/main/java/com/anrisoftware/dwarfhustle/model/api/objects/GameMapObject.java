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
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

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
public abstract class GameMapObject extends GameObject {

    public static final int OBJECT_TYPE = GameMapObject.class.getSimpleName().hashCode();

    /**
     * ID of the {@link GameMap}.
     */
    public long map = 0;

    /**
     * Sets the X, Y and Z position of a {@link GameMapObject} on the game map.
     */
    public GameBlockPos pos = new GameBlockPos();

    /**
     * Knowledge ID.
     */
    public int kid;

    /**
     * Knowledge object ID.
     */
    public int oid;

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
    }

    @Override
    public void readStream(DataInput in) throws IOException {
        super.readStream(in);
        this.map = in.readLong();
        getPos().readStream(in);
        this.kid = in.readInt();
        this.oid = in.readInt();
    }

}
