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

import org.eclipse.collections.api.list.ListIterable;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Stores loaded knowledge. The ID is generated from the
 * {@link KnowledgeObject#getKnowledgeType()}.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class KnowledgeLoadedObject extends GameObject {

    public static final long ID_FLAG = 0;

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

    private static final long serialVersionUID = 1L;

    public static final int OBJECT_TYPE = KnowledgeLoadedObject.class.getSimpleName().hashCode();

    public ListIterable<KnowledgeObject> objects;

    public KnowledgeLoadedObject(long kid) {
        super(kid2Id(kid));
    }

    public KnowledgeLoadedObject(long kid, ListIterable<KnowledgeObject> objects) {
        super(kid2Id(kid));
        this.objects = objects;
    }

    @Override
    public int getObjectType() {
        return OBJECT_TYPE;
    }

    public int getKid() {
        return id2Kid(getId());
    }
}
