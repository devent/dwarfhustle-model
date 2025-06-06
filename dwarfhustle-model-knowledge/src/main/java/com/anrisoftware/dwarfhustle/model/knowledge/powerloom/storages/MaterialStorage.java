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

import static com.anrisoftware.dwarfhustle.model.knowledge.powerloom.storages.PowerLoomUtils.retrieveFloat;

import com.anrisoftware.dwarfhustle.model.api.materials.KnowledgeMaterial;
import com.anrisoftware.dwarfhustle.model.api.objects.KnowledgeObject;
import com.google.auto.service.AutoService;

/**
 * Storage for {@link KnowledgeMaterial}. Retrieves the material properties:
 * <ul>
 * <li>{@code melting-point-material}
 * <li>{@code density-of-material}
 * <li>{@code specific-heat-capacity-of-material}
 * <li>{@code thermal-conductivity-of-material}
 * </ul>
 *
 * @see KnowledgeMaterial
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@AutoService(GameObjectKnowledge.class)
public class MaterialStorage extends AbstractKnowledgeObjectStorage {

    @Override
    public KnowledgeObject retrieve(Object o, KnowledgeObject go) {
        super.retrieve(o, go);
        var m = (KnowledgeMaterial) go;
        m.setMeltingPoint(retrieveFloat("melting-point-material", m.getName()));
        m.setDensity(retrieveFloat("density-of-material", m.getName()));
        m.setSpecificHeatCapacity(retrieveFloat("specific-heat-capacity-of-material", m.getName()));
        m.setThermalConductivity(retrieveFloat("thermal-conductivity-of-material", m.getName()));
        return m;
    }

    @Override
    public KnowledgeObject create() {
        return new KnowledgeMaterial();
    }

    @Override
    public String getType() {
        return KnowledgeMaterial.TYPE;
    }
}
