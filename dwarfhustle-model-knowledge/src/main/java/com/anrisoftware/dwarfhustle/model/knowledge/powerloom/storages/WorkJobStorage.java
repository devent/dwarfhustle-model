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

import static com.anrisoftware.dwarfhustle.model.knowledge.powerloom.storages.PowerLoomUtils.retrieveIdFunc;
import static com.anrisoftware.dwarfhustle.model.knowledge.powerloom.storages.PowerLoomUtils.retrieveIdIntFunc;
import static com.anrisoftware.dwarfhustle.model.knowledge.powerloom.storages.PowerLoomUtils.retrieveInt;

import java.time.Duration;

import org.eclipse.collections.api.factory.primitive.IntIntMaps;

import com.anrisoftware.dwarfhustle.model.api.buildings.KnowledgeWorkJob;
import com.anrisoftware.dwarfhustle.model.api.objects.KnowledgeObject;
import com.google.auto.service.AutoService;

/**
 *
 *
 * @see KnowledgeWorkJob
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@AutoService(GameObjectKnowledge.class)
public class WorkJobStorage extends AbstractObjectTypeStorage {

    @Override
    public String getType() {
        return KnowledgeWorkJob.TYPE;
    }

    @Override
    public KnowledgeObject retrieve(Object o, KnowledgeObject go) {
        super.retrieve(o, go);
        var m = (KnowledgeWorkJob) go;
        retrieveProperties(m, m.getName());
        return go;
    }

    @Override
    public KnowledgeObject overrideProperties(String parent, KnowledgeObject go) {
        super.overrideProperties(parent, go);
        var m = (KnowledgeWorkJob) go;
        retrieveProperties(m, parent);
        return go;
    }

    private void retrieveProperties(KnowledgeWorkJob m, String name) {
        m.setBuilding(retrieveIdFunc("work-building", name));
        m.setInputUnits(retrieveIdIntFunc("work-input-units", name, IntIntMaps.mutable.empty()));
        m.setOutputUnits(retrieveIdIntFunc("work-output-units", name, IntIntMaps.mutable.empty()));
        m.setDuration(Duration.ofHours(retrieveInt("work-duration", name)));
    }

    @Override
    public KnowledgeObject create() {
        return new KnowledgeWorkJob();
    }
}
