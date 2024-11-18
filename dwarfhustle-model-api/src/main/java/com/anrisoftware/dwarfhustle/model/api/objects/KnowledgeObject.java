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

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Base for all knowledge objects.
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
    public int kid;

    public String name;

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
    public abstract GameObject createObject(byte[] id);

}
