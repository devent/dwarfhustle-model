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
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Game object of the game.
 * <p>
 * Size 8 bytes
 * <ul>
 * <li>8(id)
 * </ul>
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@NoArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
public abstract class GameObject implements Externalizable, StreamStorage {

    /**
     * Converts the byte array to an Id.
     */
    public static long toId(byte[] buf) {
        return ((buf[7] & 0xFFL) << 56) | //
                ((buf[6] & 0xFFL) << 48) | //
                ((buf[5] & 0xFFL) << 40) | //
                ((buf[4] & 0xFFL) << 32) | //
                ((buf[3] & 0xFFL) << 24) | //
                ((buf[2] & 0xFFL) << 16) | //
                ((buf[1] & 0xFFL) << 8) | //
                ((buf[0] & 0xFFL) << 0);
    }

    /**
     * Unique ID of the object.
     */
    @EqualsAndHashCode.Include
    public long id;

    public GameObject(long id) {
        this.id = id;
    }

    public GameObject(byte[] idbuf) {
        this(toId(idbuf));
    }

    public abstract int getObjectType();

    @SuppressWarnings("unchecked")
    public <T extends GameObject> T getAsType() {
        return (T) this;
    }

    public <T extends GameObject> T getAs(Class<T> type) {
        return type.cast(this);
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
        out.writeLong(id);
    }

    @Override
    public void readStream(DataInput in) throws IOException {
        this.id = in.readLong();
    }
}
