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
package com.anrisoftware.dwarfhustle.model.api.buildings;

import static com.anrisoftware.dwarfhustle.model.api.objects.ExternalizableUtils.readStreamIntObjectMapSupplier;
import static com.anrisoftware.dwarfhustle.model.api.objects.ExternalizableUtils.writeStreamIntObjectMap;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.eclipse.collections.api.factory.primitive.IntObjectMaps;
import org.eclipse.collections.api.map.primitive.IntObjectMap;

import com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos;
import com.anrisoftware.dwarfhustle.model.api.objects.GameMapObject;
import com.anrisoftware.dwarfhustle.model.api.objects.NamedObject;
import com.anrisoftware.dwarfhustle.model.api.objects.StoredObject;
import com.google.auto.service.AutoService;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Building.
 */
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@AutoService(StoredObject.class)
public class Building extends GameMapObject implements NamedObject {

    public static final int OBJECT_TYPE = "Building".hashCode();

    private IntObjectMap<WorkJob> workJobs = IntObjectMaps.mutable.empty();

    private long name;

    public Building(long id) {
        super(id);
    }

    public Building(byte[] idbuf) {
        super(idbuf);
    }

    public Building(long id, GameBlockPos pos) {
        super(id, pos);
    }

    public Building(byte[] idbuf, GameBlockPos pos) {
        super(idbuf, pos);
    }

    @Override
    public int getObjectType() {
        return OBJECT_TYPE;
    }

    @Override
    public void writeStream(DataOutput out) throws IOException {
        super.writeStream(out);
        writeStreamIntObjectMap(out, workJobs);
        out.writeLong(name);
    }

    @Override
    public void readStream(DataInput in) throws IOException {
        super.readStream(in);
        this.workJobs = readStreamIntObjectMapSupplier(in, WorkJob::new);
        this.name = in.readLong();
    }

}
