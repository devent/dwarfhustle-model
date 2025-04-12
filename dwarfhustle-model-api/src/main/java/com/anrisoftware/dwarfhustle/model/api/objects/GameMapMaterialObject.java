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

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Game map object with a material.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
public abstract class GameMapMaterialObject extends GameMapObject {

    /**
     * Material.
     */
    private long material;

    public GameMapMaterialObject(long id) {
        super(id);
    }

    public GameMapMaterialObject(byte[] idbuf) {
        super(idbuf);
    }

    public GameMapMaterialObject(long id, GameBlockPos pos, long material) {
        super(id, pos);
        this.material = material;
    }

    public GameMapMaterialObject(byte[] idbuf, GameBlockPos pos, long material) {
        super(idbuf, pos);
        this.material = material;
    }

    @Override
    public void writeStream(DataOutput out) throws IOException {
        super.writeStream(out);
        out.writeLong(material);
    }

    @Override
    public void readStream(DataInput in) throws IOException {
        super.readStream(in);
        this.material = in.readLong();
    }

}
