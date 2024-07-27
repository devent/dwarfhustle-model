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

import static com.anrisoftware.dwarfhustle.model.knowledge.powerloom.storages.PowerLoomUtils.retrieveFloat;
import static com.anrisoftware.dwarfhustle.model.knowledge.powerloom.storages.PowerLoomUtils.retrieveIntSet;

import org.eclipse.collections.api.factory.primitive.IntSets;

import com.anrisoftware.dwarfhustle.model.api.objects.KnowledgeObject;
import com.anrisoftware.dwarfhustle.model.api.vegetations.KnowledgeVegetation;

import edu.isi.powerloom.logic.LogicObject;

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
        var next = (LogicObject) o;
        var m = (KnowledgeVegetation) go;
        m.setName(next.surrogateValueInverse.symbolName);
        m.growingSeason = retrieveIntSet("growing-season", m.getName(), IntSets.mutable.empty());
        m.growingSpeed = retrieveFloat("growing-speed", m.getName());
        m.growingMinTemp = retrieveFloat("growing-min-temp", m.getName());
        m.growingMaxTemp = retrieveFloat("growing-max-temp", m.getName());
        m.growingOptTemp = retrieveFloat("growing-opt-temp", m.getName());
        m.growingSoil = retrieveIntSet("growing-soil", m.getName(), IntSets.mutable.empty());
        m.floweringMonths = retrieveIntSet("flowering-months", m.getName(), IntSets.mutable.empty());
        m.growingClimate = retrieveIntSet("growing-climate", m.getName(), IntSets.mutable.empty());
        return go;
    }
}
