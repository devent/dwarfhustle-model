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

import org.lable.oss.uniqueid.IDGenerator;

import com.anrisoftware.dwarfhustle.model.api.materials.Gas;
import com.anrisoftware.dwarfhustle.model.api.materials.IgneousExtrusive;
import com.anrisoftware.dwarfhustle.model.api.materials.IgneousIntrusive;
import com.anrisoftware.dwarfhustle.model.api.materials.Material;
import com.anrisoftware.dwarfhustle.model.api.materials.Metal;
import com.anrisoftware.dwarfhustle.model.api.materials.MetalAlloy;
import com.anrisoftware.dwarfhustle.model.api.materials.MetalOre;
import com.anrisoftware.dwarfhustle.model.api.materials.Metamorphic;
import com.anrisoftware.dwarfhustle.model.api.materials.Sedimentary;
import com.anrisoftware.dwarfhustle.model.api.materials.Soil;
import com.anrisoftware.dwarfhustle.model.api.materials.SpecialStoneLayer;
import com.anrisoftware.dwarfhustle.model.api.materials.Stone;
import com.anrisoftware.dwarfhustle.model.api.materials.StoneLayer;
import com.anrisoftware.dwarfhustle.model.db.cache.AbstractJcsCacheActor;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.IdsKnowledgeProvider.IdsKnowledge;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.KnowledgeBaseActor.KnowledgeBaseActorFactory;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.KnowledgeJcsCacheActor.KnowledgeJcsCacheActorFactory;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.PowerLoomKnowledgeActor.PowerLoomKnowledgeActorFactory;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.storages.ClayStorage;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.storages.GameObjectKnowledge;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.storages.GasStorage;
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
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.storages.SpecialStoneLayerStorage;
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
        install(new FactoryModuleBuilder().implement(AbstractJcsCacheActor.class, KnowledgeJcsCacheActor.class)
                .build(KnowledgeJcsCacheActorFactory.class));
        bind(IDGenerator.class).annotatedWith(IdsKnowledge.class).toProvider(IdsKnowledgeProvider.class)
                .asEagerSingleton();
    }

    @Singleton
    @Provides
    public Map<String, GameObjectKnowledge> getStorages() {
        var map = new HashMap<String, GameObjectKnowledge>();
        var material = new MaterialStorage();
        map.put(Material.TYPE, material);
        var metalOre = new MetalOreStorage();
        map.put(MetalOre.TYPE, metalOre);
        var metal = new MetalStorage();
        map.put(Metal.TYPE, metal);
        var metalAlloy = new MaterialStorage();
        map.put(MetalAlloy.TYPE, metalAlloy);
        var soil = new SoilStorage();
        map.put(Soil.TYPE, soil);
        var clay = new ClayStorage();
        map.put(Material.TYPE, clay);
        var sand = new SandStorage();
        map.put(Material.TYPE, sand);
        var seabed = new SeabedStorage();
        map.put(Material.TYPE, seabed);
        var topsoil = new TopsoilStorage();
        map.put(Material.TYPE, topsoil);
        var stone = new StoneStorage();
        map.put(Stone.TYPE, stone);
        var stoneLayer = new StoneLayerStorage();
        map.put(StoneLayer.TYPE, stoneLayer);
        var igneousIntrusive = new IgneousIntrusiveStorage();
        map.put(IgneousIntrusive.TYPE, igneousIntrusive);
        var igneousExtrusive = new IgneousExtrusiveStorage();
        map.put(IgneousExtrusive.TYPE, igneousExtrusive);
        var metamorphic = new MetamorphicStorage();
        map.put(Metamorphic.TYPE, metamorphic);
        var sedimentary = new SedimentaryStorage();
        map.put(Sedimentary.TYPE, sedimentary);
        var specialStoneLayer = new SpecialStoneLayerStorage();
        map.put(SpecialStoneLayer.TYPE, specialStoneLayer);
        var gas = new GasStorage();
        map.put(Gas.TYPE, gas);
        return map;
    }

}
