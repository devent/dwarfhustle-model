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

import com.anrisoftware.dwarfhustle.model.api.map.RoofType;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;
import com.anrisoftware.dwarfhustle.model.api.objects.KnowledgeObject;
import com.google.auto.service.AutoService;

import edu.isi.powerloom.logic.LogicObject;

/**
 *
 * @see RoofType
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@AutoService(GameObjectKnowledge.class)
public class RoofTypeStorage implements GameObjectKnowledge {

    @Override
    public String getType() {
        return RoofType.TYPE;
    }

    @Override
    public KnowledgeObject retrieve(Object o, GameObject go) {
        var next = (LogicObject) o;
        var ko = (RoofType) go;
        ko.setRid((long) next.surrogateValueInverse.symbolId);
        ko.setType(next.surrogateValueInverse.symbolName);
        return ko;
    }

    @Override
    public KnowledgeObject create() {
        return new RoofType();
    }
}
