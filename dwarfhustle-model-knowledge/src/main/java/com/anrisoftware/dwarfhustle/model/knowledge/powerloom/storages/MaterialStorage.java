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
package com.anrisoftware.dwarfhustle.model.knowledge.powerloom.storages;

import static com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.PowerLoomKnowledgeActor.WORKING_MODULE;

import com.anrisoftware.dwarfhustle.model.api.materials.KnowledgeObject;
import com.anrisoftware.dwarfhustle.model.api.materials.Material;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;

import edu.isi.powerloom.PLI;
import edu.isi.powerloom.logic.LogicObject;
import edu.isi.stella.FloatWrapper;

/**
 * Storage for {@link Material}.
 *
 * @see Material
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public class MaterialStorage implements GameObjectKnowledge {

    public static float retrieveFloat(String function, String name) {
        var buff = new StringBuilder();
        buff.append("?x (");
        buff.append(function);
        buff.append(" ");
        buff.append(name);
        buff.append(" ?x)");
        var answer = PLI.sRetrieve(buff.toString(), WORKING_MODULE, null);
        FloatWrapper next;
        while ((next = (FloatWrapper) answer.pop()) != null) {
            return (float) next.wrapperValue;
        }
        return -1;
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
