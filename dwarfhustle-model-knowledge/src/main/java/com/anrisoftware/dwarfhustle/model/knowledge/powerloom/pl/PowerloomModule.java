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
package com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

import com.anrisoftware.dwarfhustle.model.api.materials.IgneousExtrusive;
import com.anrisoftware.dwarfhustle.model.api.materials.IgneousIntrusive;
import com.anrisoftware.dwarfhustle.model.api.materials.Material;
import com.anrisoftware.dwarfhustle.model.api.materials.Metal;
import com.anrisoftware.dwarfhustle.model.api.materials.MetalAlloy;
import com.anrisoftware.dwarfhustle.model.api.materials.MetalOre;
import com.anrisoftware.dwarfhustle.model.api.materials.Metamorphic;
import com.anrisoftware.dwarfhustle.model.api.materials.Sedimentary;
import com.anrisoftware.dwarfhustle.model.api.materials.Soil;
import com.anrisoftware.dwarfhustle.model.api.materials.Stone;
import com.anrisoftware.dwarfhustle.model.api.materials.StoneLayer;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.KnowledgeBaseActor.KnowledgeBaseActorFactory;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.PowerLoomKnowledgeActor.PowerLoomKnowledgeActorFactory;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.storages.ClayStorage;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.storages.GameObjectKnowledge;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.storages.IgneousExtrusiveStorage;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.storages.IgneousIntrusiveStorage;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.storages.MaterialStorage;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.storages.MetalOreStorage;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.storages.MetalStorage;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.storages.MetamorphicStorage;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.storages.SandStorage;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.storages.SeabedStorage;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.storages.SedimentaryStorage;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.storages.SoilStorage;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.storages.StoneLayerStorage;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.storages.StoneStorage;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.storages.TopsoilStorage;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.assistedinject.FactoryModuleBuilder;

/**
 * @author Erwin Müller
 */
public class PowerloomModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new FactoryModuleBuilder().implement(PowerLoomKnowledgeActor.class, PowerLoomKnowledgeActor.class)
                .build(PowerLoomKnowledgeActorFactory.class));
		install(new FactoryModuleBuilder().implement(KnowledgeBaseActor.class, KnowledgeBaseActor.class)
				.build(KnowledgeBaseActorFactory.class));
    }

    @Singleton
    @Provides
    public Map<String, GameObjectKnowledge> getStorages() {
        var map = new HashMap<String, GameObjectKnowledge>();
        var material = new MaterialStorage();
        map.put(Material.OBJECT_TYPE, material);
        var metalOre = new MetalOreStorage();
        map.put(MetalOre.OBJECT_TYPE, metalOre);
        var metal = new MetalStorage();
        map.put(Metal.OBJECT_TYPE, metal);
        var metalAlloy = new MaterialStorage();
        map.put(MetalAlloy.OBJECT_TYPE, metalAlloy);
        var soil = new SoilStorage();
        map.put(Soil.OBJECT_TYPE, soil);
        var clay = new ClayStorage();
        map.put(Material.OBJECT_TYPE, clay);
        var sand = new SandStorage();
        map.put(Material.OBJECT_TYPE, sand);
        var seabed = new SeabedStorage();
        map.put(Material.OBJECT_TYPE, seabed);
        var topsoil = new TopsoilStorage();
        map.put(Material.OBJECT_TYPE, topsoil);
        var stone = new StoneStorage();
        map.put(Stone.OBJECT_TYPE, stone);
        var stoneLayer = new StoneLayerStorage();
        map.put(StoneLayer.OBJECT_TYPE, stoneLayer);
        var igneousIntrusive = new IgneousIntrusiveStorage();
        map.put(IgneousIntrusive.OBJECT_TYPE, igneousIntrusive);
        var igneousExtrusive = new IgneousExtrusiveStorage();
        map.put(IgneousExtrusive.OBJECT_TYPE, igneousExtrusive);
        var metamorphic = new MetamorphicStorage();
        map.put(Metamorphic.OBJECT_TYPE, metamorphic);
        var sedimentary = new SedimentaryStorage();
        map.put(Sedimentary.OBJECT_TYPE, sedimentary);
        return map;
    }

}
