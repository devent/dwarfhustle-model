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

import static com.anrisoftware.dwarfhustle.model.knowledge.powerloom.storages.PowerLoomUtils.retrieveInt;

import com.anrisoftware.dwarfhustle.model.api.buildings.KnowledgeBuilding;
import com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos;
import com.anrisoftware.dwarfhustle.model.api.objects.KnowledgeObject;
import com.google.auto.service.AutoService;

/**
 *
 *
 * @see KnowledgeBuilding
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@AutoService(GameObjectKnowledge.class)
public class BuildingStorage extends AbstractObjectTypeStorage {

    @Override
    public String getType() {
        return KnowledgeBuilding.TYPE;
    }

    @Override
    public KnowledgeObject retrieve(Object o, KnowledgeObject go) {
        super.retrieve(o, go);
        var m = (KnowledgeBuilding) go;
        retrieveProperties(m, m.getName());
        return go;
    }

    @Override
    public KnowledgeObject overrideProperties(String parent, KnowledgeObject go) {
        super.overrideProperties(parent, go);
        var m = (KnowledgeBuilding) go;
        retrieveProperties(m, parent);
        return go;
    }

    private void retrieveProperties(KnowledgeBuilding m, String name) {
        m.setSize(new GameBlockPos(retrieveInt("object-width", name), retrieveInt("object-height", name),
                retrieveInt("object-depth", name)));
    }

    @Override
    public KnowledgeObject create() {
        return new KnowledgeBuilding();
    }
}
