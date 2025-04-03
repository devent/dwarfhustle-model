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
import java.io.Serializable;

import com.google.auto.service.AutoService;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * String object. Contains a {@link String}.
 */
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@AutoService(StoredObject.class)
public class StringObject extends GameObject implements StoredObject {

    public static final int OBJECT_TYPE = "string".hashCode();

    public static StringObject getStringObject(ObjectsGetter og, long id) {
        return og.get(OBJECT_TYPE, id);
    }

    public static void setStringObject(ObjectsSetter os, StringObject gm) {
        os.set(OBJECT_TYPE, gm);
    }

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Serializable rid;

    private String s;

    public StringObject(long id) {
        super(id);
    }

    public StringObject(byte[] idbuf) {
        super(idbuf);
    }

    public StringObject(long id, String s) {
        super(id);
        this.s = s;
    }

    public StringObject(byte[] idbuf, String s) {
        super(idbuf);
        this.s = s;
    }

    @Override
    public int getObjectType() {
        return OBJECT_TYPE;
    }

    @Override
    public void writeStream(DataOutput out) throws IOException {
        super.writeStream(out);
        out.writeUTF(s);
    }

    @Override
    public void readStream(DataInput in) throws IOException {
        super.readStream(in);
        this.s = in.readUTF();
    }

}
