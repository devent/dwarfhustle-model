/*
 * dwarfhustle-model-knowledge - Manages the compile dependencies for the model.
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
package com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl;

import org.eclipse.collections.api.list.ListIterable;

import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;
import com.anrisoftware.dwarfhustle.model.api.objects.KnowledgeObject;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Stores loaded knowledge.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class KnowledgeLoadedObject extends GameObject {

    private static final long serialVersionUID = 1L;

    public static final int OBJECT_TYPE = KnowledgeLoadedObject.class.getSimpleName().hashCode();

    public String type;

    public ListIterable<KnowledgeObject> objects;

    public KnowledgeLoadedObject(byte[] idbuf) {
        super(idbuf);
    }

    public KnowledgeLoadedObject(long id) {
        super(id);
    }

    public KnowledgeLoadedObject(byte[] idbuf, String type, ListIterable<KnowledgeObject> objects) {
        this(idbuf);
        this.type = type;
        this.objects = objects;
    }

    @Override
    public int getObjectType() {
        return OBJECT_TYPE;
    }

}
