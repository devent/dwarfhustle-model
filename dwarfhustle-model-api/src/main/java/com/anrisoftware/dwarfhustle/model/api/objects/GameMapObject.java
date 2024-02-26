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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Game object on the game map.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
public abstract class GameMapObject extends GameObject {

    private static final long serialVersionUID = 1L;

    public static final String OBJECT_TYPE = GameMapObject.class.getSimpleName();

    /**
     * ID of the {@link GameMap}.
     */
    public long map = 0;

    /**
     * Sets the X, Y and Z position of a {@link GameMapObject} on the game map.
     */
    public GameBlockPos pos = new GameBlockPos();

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

    @Override
    public String getObjectType() {
        return OBJECT_TYPE;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeLong(map);
        pos.writeExternal(out);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        this.map = in.readLong();
        pos.readExternal(in);
    }

}
