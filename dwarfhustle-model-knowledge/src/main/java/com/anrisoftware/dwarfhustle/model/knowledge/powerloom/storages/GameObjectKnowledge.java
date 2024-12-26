/*
 * dwarfhustle-model-knowledge - Manages the compile dependencies for the model.
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
package com.anrisoftware.dwarfhustle.model.knowledge.powerloom.storages;

import com.anrisoftware.dwarfhustle.model.api.objects.KnowledgeObject;

/**
 * Retrieves {@link KnowledgeObject} knowledge objects from the knowledge base.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public interface GameObjectKnowledge {

    /**
     * Retrieves the {@link KnowledgeObject}.
     */
    KnowledgeObject retrieve(Object o, KnowledgeObject go);

    /**
     * Overrides properties from the parent knowledge object.
     */
    KnowledgeObject overrideProperties(String parent, KnowledgeObject go);

    /**
     * Returns a new {@link KnowledgeObject}.
     */
    KnowledgeObject create();

    /**
     * Returns the knowledge object type.
     */
    String getType();

}
