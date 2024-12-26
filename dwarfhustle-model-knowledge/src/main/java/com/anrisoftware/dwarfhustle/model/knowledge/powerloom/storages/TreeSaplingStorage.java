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

import static com.anrisoftware.dwarfhustle.model.knowledge.powerloom.storages.PowerLoomUtils.retrieveString;

import com.anrisoftware.dwarfhustle.model.api.objects.KnowledgeObject;
import com.anrisoftware.dwarfhustle.model.api.vegetations.KnowledgeTreeSapling;
import com.google.auto.service.AutoService;

/**
 * @see KnowledgeTreeSapling
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@AutoService(GameObjectKnowledge.class)
public class TreeSaplingStorage extends VegetationStorage {

    @Override
    public String getType() {
        return KnowledgeTreeSapling.TYPE;
    }

    @Override
    public KnowledgeObject retrieve(Object o, KnowledgeObject go) {
        super.retrieve(o, go);
        var m = (KnowledgeTreeSapling) go;
        m.growsInto = retrieveString("grows-into", m.getName());
        return go;
    }

    @Override
    public KnowledgeObject create() {
        return new KnowledgeTreeSapling();
    }
}
