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
 * Base class for all object properties like the material an object is made of.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@Data
public abstract class KnowledgeObject extends GameObject {

    public static final long ID_FLAG = 1;

    /**
     * Returns the game object ID from the knowledge KID.
     */
    public static long kid2Id(long kid) {
        return (kid << 32) | ID_FLAG;
    }

    /**
     * Returns the knowledge KID from the game object ID.
     */
    public static int id2Kid(long id) {
        return (int) (id >> 32);
    }

    /**
     * Knowledge KID.
     */
    private int kid;

    /**
     * The name of the object, i.e. log, block, rock, boulder, plank, sword, etc.
     */
    private String name;

    public KnowledgeObject(int kid) {
        super(kid2Id(kid));
        this.kid = kid;
    }

    public void setKid(int kid) {
        this.kid = kid;
        setId(kid2Id(kid));
    }

    /**
     * Type in the knowledge space.
     */
    public abstract String getKnowledgeType();

    /**
     * Creates the {@link GameObject} for this {@link KnowledgeObject}.
     */
    public abstract <T extends GameObject> T createObject(byte[] id);

    @Override
    public void writeStream(DataOutput out) throws IOException {
        super.writeStream(out);
        out.writeInt(kid);
        out.writeUTF(name);
    }

    @Override
    public void readStream(DataInput in) throws IOException {
        super.readStream(in);
        this.kid = in.readInt();
        this.name = in.readUTF();
    }

}
