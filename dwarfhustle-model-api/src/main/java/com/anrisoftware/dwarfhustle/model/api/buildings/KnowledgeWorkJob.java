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

import static com.anrisoftware.dwarfhustle.model.api.objects.ExternalizableUtils.readStreamIntCollection;
import static com.anrisoftware.dwarfhustle.model.api.objects.ExternalizableUtils.readStreamIntIntMap;
import static com.anrisoftware.dwarfhustle.model.api.objects.ExternalizableUtils.writeStreamIntCollection;
import static com.anrisoftware.dwarfhustle.model.api.objects.ExternalizableUtils.writeStreamIntIntMap;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.time.Duration;

import org.eclipse.collections.api.factory.primitive.IntSets;
import org.eclipse.collections.api.map.primitive.IntIntMap;
import org.eclipse.collections.api.set.primitive.IntSet;

import com.anrisoftware.dwarfhustle.model.api.map.Block;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;
import com.anrisoftware.dwarfhustle.model.api.objects.ObjectType;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Work job.
 *
 * @see Block
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class KnowledgeWorkJob extends ObjectType {

    public static final String TYPE = "work-job";

    public static final int OBJECT_TYPE = TYPE.hashCode();

    private int building;

    private IntIntMap inputUnits;

    private IntSet inputTypes;

    private IntIntMap outputUnits;

    private Duration duration;

    public KnowledgeWorkJob(int kid) {
        super(kid);
    }

    @Override
    public int getObjectType() {
        return KnowledgeWorkJob.OBJECT_TYPE;
    }

    @Override
    public String getKnowledgeType() {
        return KnowledgeWorkJob.TYPE;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends GameObject> T createObject(byte[] id) {
        var go = new WorkJob(id);
        go.setBuilding(building);
        go.setDuration(duration);
        return (T) go;
    }

    @Override
    public void writeStream(DataOutput out) throws IOException {
        super.writeStream(out);
        out.writeInt(building);
        writeStreamIntIntMap(out, inputUnits);
        writeStreamIntIntMap(out, outputUnits);
        writeStreamIntCollection(out, inputTypes.size(), inputUnits);
        out.writeLong(duration.getSeconds());
    }

    @Override
    public void readStream(DataInput in) throws IOException {
        super.readStream(in);
        this.building = in.readInt();
        this.inputUnits = readStreamIntIntMap(in);
        this.outputUnits = readStreamIntIntMap(in);
        this.inputTypes = IntSets.immutable.ofAll(readStreamIntCollection(in));
        this.duration = Duration.ofSeconds(in.readLong());
    }

}
