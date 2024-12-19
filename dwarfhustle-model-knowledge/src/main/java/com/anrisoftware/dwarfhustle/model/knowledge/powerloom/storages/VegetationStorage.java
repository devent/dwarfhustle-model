/*
 * dwarfhustle-model-knowledge - Manages the compile dependencies for the model.
 * Copyright © 2022-2024 Erwin Müller (erwin.mueller@anrisoftware.com)
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
import static com.anrisoftware.dwarfhustle.model.knowledge.powerloom.storages.PowerLoomUtils.retrieveInt;
import static com.anrisoftware.dwarfhustle.model.knowledge.powerloom.storages.PowerLoomUtils.retrieveIntSet;

import org.eclipse.collections.api.factory.primitive.IntSets;

import com.anrisoftware.dwarfhustle.model.api.objects.KnowledgeObject;
import com.anrisoftware.dwarfhustle.model.api.vegetations.KnowledgeVegetation;

/**
 * Grasses, shrubs, trees.
 *
 * @see KnowledgeVegetation
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public abstract class VegetationStorage extends AbstractObjectTypeStorage {

    @Override
    public String getType() {
        return KnowledgeVegetation.TYPE;
    }

    @Override
    public KnowledgeObject retrieve(Object o, KnowledgeObject go) {
        super.retrieve(o, go);
        var m = (KnowledgeVegetation) go;
        retrieveProperties(m, m.getName());
        return go;
    }

    @Override
    public KnowledgeObject overrideProperties(String parent, KnowledgeObject go) {
        super.overrideProperties(parent, go);
        var m = (KnowledgeVegetation) go;
        retrieveProperties(m, parent);
        return go;
    }

    private void retrieveProperties(KnowledgeVegetation m, String name) {
        m.growingSeason = retrieveIntSet("growing-season", name, IntSets.mutable.empty());
        m.growingSpeed = retrieveFloat("growing-speed", name);
        m.growingMinTemp = retrieveFloat("growing-min-temp", name);
        m.growingMaxTemp = retrieveFloat("growing-max-temp", name);
        m.growingOptTemp = retrieveFloat("growing-opt-temp", name);
        m.growingSoil = retrieveIntSet("growing-soil", name, IntSets.mutable.empty());
        m.floweringMonths = retrieveIntSet("flowering-months", name, IntSets.mutable.empty());
        m.growingClimate = retrieveIntSet("growing-climate", name, IntSets.mutable.empty());
        m.rootMaxSize = retrieveInt("root-max-size", name);
        m.widthMax = retrieveInt("width-max", name);
        m.heightMax = retrieveInt("height-max", name);
        m.depthMax = retrieveInt("depth-max", name);
    }
}
