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
package com.anrisoftware.dwarfhustle.model.api.miscobjects;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos;
import com.anrisoftware.dwarfhustle.model.api.objects.GameMapMaterialObject;
import com.anrisoftware.dwarfhustle.model.api.objects.StoredObject;
import com.google.auto.service.AutoService;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Furniture.
 */
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@AutoService(StoredObject.class)
public class Furniture extends GameMapMaterialObject {

    public static final String TYPE = "Furniture";

    public static final int OBJECT_TYPE = TYPE.hashCode();

    public Furniture(long id) {
        super(id);
    }

    public Furniture(byte[] idbuf) {
        super(idbuf);
    }

    public Furniture(long id, GameBlockPos pos, long material, int type) {
        super(id, pos, material, type);
    }

    public Furniture(byte[] idbuf, GameBlockPos pos, long material, int type) {
        super(idbuf, pos, material, type);
    }

    @Override
    public int getObjectType() {
        return OBJECT_TYPE;
    }

    @Override
    public void writeStream(DataOutput out) throws IOException {
        super.writeStream(out);
    }

    @Override
    public void readStream(DataInput in) throws IOException {
        super.readStream(in);
    }

}
