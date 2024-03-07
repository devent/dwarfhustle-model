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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Center extent of a map tile or chunk.
 * <p>
 * Size 24 bytes
 * <ul>
 * <li>6*4(x,y,z)
 * </ul>
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CenterExtent implements Externalizable, StreamStorage {

    private static final long serialVersionUID = 1L;

    public float centerx;

    public float centery;

    public float centerz;

    public float extentx;

    public float extenty;

    public float extentz;

    public float getBottomX() {
        return centerx - extentx;
    }

    public float getBottomY() {
        return centery - extenty;
    }

    public float getBottomZ() {
        return centerz - extentz;
    }

    public float getTopX() {
        return centerx + extentx;
    }

    public float getTopY() {
        return centery + extenty;
    }

    public float getTopZ() {
        return centerz + extentz;
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
        out.writeFloat(centerx);
        out.writeFloat(centery);
        out.writeFloat(centerz);
        out.writeFloat(extentx);
        out.writeFloat(extenty);
        out.writeFloat(extentz);
    }

    @Override
    public void readStream(DataInput in) throws IOException {
        this.centerx = in.readFloat();
        this.centery = in.readFloat();
        this.centerz = in.readFloat();
        this.extentx = in.readFloat();
        this.extenty = in.readFloat();
        this.extentz = in.readFloat();
    }

}
