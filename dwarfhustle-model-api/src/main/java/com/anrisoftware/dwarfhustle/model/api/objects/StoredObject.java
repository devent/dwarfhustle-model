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

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Base for all game objects that are stored in the backend.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
public abstract class StoredObject extends GameObject {

    private static final long serialVersionUID = 1L;

    public static final long ID_FLAG = 0;

    public static final String OBJECT_TYPE = StoredObject.class.getSimpleName();

    /**
     * Stores the original properties and is set after the object is saved in the
     * backend.
     */
    @ToString.Exclude
    public StoredObject old;

    /**
     * Record ID set after the object was once stored in the backend.
     */
    public Serializable rid = null;

    public StoredObject(byte[] idbuf) {
        super(idbuf);
        this.old = this;
    }

    public StoredObject(long id) {
        super(id);
        this.old = this;
    }

    @Override
    public String getObjectType() {
        return StoredObject.OBJECT_TYPE;
    }

    @SuppressWarnings("unchecked")
    public <T extends StoredObject> T getOld() {
        return (T) old;
    }

    public abstract boolean isDirty();
}
