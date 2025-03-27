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

import static com.anrisoftware.dwarfhustle.model.api.objects.ExternalizableUtils.readStreamLongIntMap;
import static com.anrisoftware.dwarfhustle.model.api.objects.ExternalizableUtils.writeStreamLongIntMap;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

import org.eclipse.collections.api.map.primitive.LongIntMap;

import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;
import com.anrisoftware.dwarfhustle.model.api.objects.StoredObject;
import com.google.auto.service.AutoService;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Work job.
 */
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@AutoService(StoredObject.class)
public class WorkJob extends GameObject implements StoredObject {

    public static final int OBJECT_TYPE = "work-job".hashCode();

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Serializable rid;

    private long building;

    private LongIntMap inputUnits;

    private LongIntMap outputUnits;

    private Duration duration;

    public WorkJob(long id) {
        super(id);
    }

    public WorkJob(byte[] idbuf) {
        super(idbuf);
    }

    @Override
    public int getObjectType() {
        return OBJECT_TYPE;
    }

    @Override
    public void writeStream(DataOutput out) throws IOException {
        super.writeStream(out);
        out.writeLong(building);
        writeStreamLongIntMap(out, inputUnits);
        writeStreamLongIntMap(out, outputUnits);
        out.writeInt((int) duration.getSeconds());
    }

    @Override
    public void readStream(DataInput in) throws IOException {
        super.readStream(in);
        this.building = in.readLong();
        this.inputUnits = readStreamLongIntMap(in);
        this.outputUnits = readStreamLongIntMap(in);
        this.duration = Duration.of(in.readInt(), ChronoUnit.SECONDS);
    }
}
