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
package com.anrisoftware.dwarfhustle.model.knowledge.powerloom.storages;

import static com.anrisoftware.dwarfhustle.model.knowledge.powerloom.storages.GameObjectKnowledge.retrieveFloat;

import com.anrisoftware.dwarfhustle.model.api.materials.Material;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;
import com.anrisoftware.dwarfhustle.model.api.objects.KnowledgeObject;
import com.google.auto.service.AutoService;

import edu.isi.powerloom.logic.LogicObject;

/**
 * Storage for {@link Material}.
 *
 * @see Material
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@AutoService(GameObjectKnowledge.class)
public class MaterialStorage implements GameObjectKnowledge {

    @Override
    public String getType() {
        return Material.TYPE;
    }

    @Override
    public KnowledgeObject retrieve(Object o, GameObject go) {
        var next = (LogicObject) o;
        var m = (Material) go;
        m.setRid((long) next.surrogateValueInverse.symbolId);
        m.setName(next.surrogateValueInverse.symbolName);
        m.setMeltingPoint(retrieveFloat("melting-point-material", m.getName()));
        m.setDensity(retrieveFloat("density-of-material", m.getName()));
        m.setSpecificHeatCapacity(retrieveFloat("specific-heat-capacity-of-material", m.getName()));
        m.setThermalConductivity(retrieveFloat("thermal-conductivity-of-material", m.getName()));
        return m;
    }

    @Override
    public KnowledgeObject create() {
        return new Material();
    }
}
