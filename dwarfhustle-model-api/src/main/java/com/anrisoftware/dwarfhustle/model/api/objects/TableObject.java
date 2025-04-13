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

import static com.anrisoftware.dwarfhustle.model.api.objects.ExternalizableUtils.readStreamLongCollection;
import static com.anrisoftware.dwarfhustle.model.api.objects.ExternalizableUtils.writeStreamLongCollection;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

import org.eclipse.collections.api.factory.primitive.LongSets;
import org.eclipse.collections.api.set.primitive.LongSet;
import org.eclipse.collections.api.set.primitive.MutableLongSet;

import com.google.auto.service.AutoService;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Contains a table from an object ID to multiple object IDs.
 */
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@AutoService(StoredObject.class)
public class TableObject extends GameObject implements StoredObject {

    public static final int OBJECT_TYPE = "TableObject".hashCode();

    public static TableObject getTableObject(ObjectsGetter og, long id) {
        return og.get(OBJECT_TYPE, id);
    }

    public static void setTableObject(ObjectsSetter os, TableObject gm) {
        os.set(OBJECT_TYPE, gm);
    }

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Serializable rid;

    private long parent;

    private MutableLongSet table;

    public TableObject(long id) {
        super(id);
        this.table = LongSets.mutable.empty();
    }

    public TableObject(byte[] idbuf) {
        super(idbuf);
        this.table = LongSets.mutable.empty();
    }

    public TableObject(long id, long parent, LongSet table) {
        super(id);
        this.parent = parent;
        this.table = LongSets.mutable.ofAll(table);
    }

    public TableObject(byte[] idbuf, long parent, LongSet table) {
        super(idbuf);
        this.parent = parent;
        this.table = LongSets.mutable.ofAll(table);
    }

    @Override
    public int getObjectType() {
        return OBJECT_TYPE;
    }

    @Override
    public void writeStream(DataOutput out) throws IOException {
        super.writeStream(out);
        out.writeLong(parent);
        writeStreamLongCollection(out, table.size(), table);
    }

    @Override
    public void readStream(DataInput in) throws IOException {
        super.readStream(in);
        this.parent = in.readLong();
        this.table = LongSets.mutable.ofAll(readStreamLongCollection(in));
    }

}
