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

import static com.anrisoftware.dwarfhustle.model.knowledge.powerloom.storages.PowerLoomUtils.retrieve3ToStore;
import static com.anrisoftware.dwarfhustle.model.knowledge.powerloom.storages.PowerLoomUtils.retrieveIdFunc;
import static com.anrisoftware.dwarfhustle.model.knowledge.powerloom.storages.PowerLoomUtils.retrieveInt;

import java.time.Duration;

import org.eclipse.collections.api.factory.primitive.IntIntMaps;
import org.eclipse.collections.api.factory.primitive.IntObjectMaps;
import org.eclipse.collections.api.map.primitive.IntIntMap;
import org.eclipse.collections.api.map.primitive.MutableIntIntMap;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;

import com.anrisoftware.dwarfhustle.model.api.buildings.KnowledgeWorkJob;
import com.anrisoftware.dwarfhustle.model.api.objects.KnowledgeObject;
import com.google.auto.service.AutoService;

import edu.isi.powerloom.logic.LogicObject;
import edu.isi.stella.Cons;
import edu.isi.stella.IntegerWrapper;
import lombok.val;

/**
 *
 *
 * @see KnowledgeWorkJob
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@AutoService(GameObjectKnowledge.class)
public class WorkJobStorage extends AbstractKnowledgeObjectStorage {

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
        m.setInputContainers(retrieve3ToStore("work-input-container-units", name, () -> IntObjectMaps.mutable.empty(),
                this::putObjectMaterialCount));
        m.setOutputContainers(retrieve3ToStore("work-output-container-units", name, () -> IntObjectMaps.mutable.empty(),
                this::putObjectMaterialCount));
        m.setInputObjects(retrieve3ToStore("work-input-units", name, () -> IntObjectMaps.mutable.empty(),
                this::putObjectMaterialCount));
        m.setOutputObjects(retrieve3ToStore("work-output-units", name, () -> IntObjectMaps.mutable.empty(),
                this::putObjectMaterialCount));
        val sameJobs = PowerLoomUtils.retrieveStrings("work-same-job", name);
        for (String samejob : sameJobs) {
            retrieve3ToStore("work-input-container-units", samejob,
                    () -> (MutableIntObjectMap<IntIntMap>) m.getInputContainers(), this::putObjectMaterialCount);
            retrieve3ToStore("work-output-container-units", samejob,
                    () -> (MutableIntObjectMap<IntIntMap>) m.getOutputContainers(), this::putObjectMaterialCount);
            retrieve3ToStore("work-input-units", samejob, () -> (MutableIntObjectMap<IntIntMap>) m.getInputObjects(),
                    this::putObjectMaterialCount);
            retrieve3ToStore("work-output-units", samejob, () -> (MutableIntObjectMap<IntIntMap>) m.getOutputObjects(),
                    this::putObjectMaterialCount);
        }
        m.setDuration(Duration.ofHours(retrieveInt("work-duration", name)));

    }

    private void putObjectMaterialCount(MutableIntObjectMap<IntIntMap> store, Cons o) {
        val object = ((LogicObject) o.value).surrogateValueInverse.symbolId;
        MutableIntIntMap map = (MutableIntIntMap) store.getIfAbsentPut(object, () -> IntIntMaps.mutable.empty());
        o = o.rest;
        val material = ((LogicObject) o.value).surrogateValueInverse.symbolId;
        o = o.rest;
        val count = ((IntegerWrapper) o.value).wrapperValue;
        map.put(material, count);
    }

    @Override
    public KnowledgeObject create() {
        return new KnowledgeWorkJob();
    }
}
