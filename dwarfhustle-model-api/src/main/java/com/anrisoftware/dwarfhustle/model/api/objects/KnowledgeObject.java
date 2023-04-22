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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Base for all knowledge objects.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public abstract class KnowledgeObject extends GameObject {

    private static final long serialVersionUID = 1L;

    public static final long ID_FLAG = 1;

    public static final String OBJECT_TYPE = KnowledgeObject.class.getSimpleName();

    /**
     * Returns the game object ID from the knowledge RID.
     */
    public static long rid2Id(long rid) {
        return (rid << 32) | ID_FLAG;
    }

    public KnowledgeObject(long id) {
        super(rid2Id(id));
    }

    @Override
    public void setId(long id) {
        super.setId(rid2Id(id));
    }

    @Override
    public String getObjectType() {
        return KnowledgeObject.OBJECT_TYPE;
    }

    /**
     * Type in the knowledge space.
     */
    public abstract String getKnowledgeType();

    @Override
    public void setDirty(boolean dirty) {
        // knowledge object is immutable
    }

    @Override
    public boolean isDirty() {
        return false;
    }
}
