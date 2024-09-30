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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

import org.eclipse.collections.api.factory.primitive.LongObjectMaps;
import org.eclipse.collections.api.map.primitive.LongObjectMap;
import org.eclipse.collections.api.map.primitive.MutableLongObjectMap;
import org.lable.oss.uniqueid.IDGenerator;

import com.anrisoftware.dwarfhustle.model.db.cache.AbstractJcsCacheActor;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.IdsKnowledgeProvider.IdsKnowledge;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.KnowledgeJcsCacheActor.KnowledgeJcsCacheActorFactory;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.PowerLoomKnowledgeActor.PowerLoomKnowledgeActorFactory;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.storages.GameObjectKnowledge;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.assistedinject.FactoryModuleBuilder;

import jakarta.inject.Named;
import jakarta.inject.Singleton;

/**
 * @author Erwin Müller
 */
public class DwarfhustlePowerloomModule extends AbstractModule {

    private final Map<String, GameObjectKnowledge> storages;

    private final LongObjectMap<String> tidType;

    public DwarfhustlePowerloomModule() {
        this.storages = loadStorages();
        this.tidType = loadTidType(storages);
    }

    private Map<String, GameObjectKnowledge> loadStorages() {
        var map = new HashMap<String, GameObjectKnowledge>();
        var loader = ServiceLoader.load(GameObjectKnowledge.class);
        StreamSupport.stream(loader.spliterator(), true).forEach(s -> {
            map.put(s.getType(), s);
        });
        assertThat(map.entrySet(), not(empty()));
        return map;
    }

    private LongObjectMap<String> loadTidType(Map<String, GameObjectKnowledge> storages) {
        MutableLongObjectMap<String> map = LongObjectMaps.mutable.empty();
        for (GameObjectKnowledge val : storages.values()) {
            map.put(val.getType().hashCode(), val.getType());
        }
        return map.asUnmodifiable();
    }

    @Override
    protected void configure() {
        install(new FactoryModuleBuilder().implement(PowerLoomKnowledgeActor.class, PowerLoomKnowledgeActor.class)
                .build(PowerLoomKnowledgeActorFactory.class));
        install(new FactoryModuleBuilder().implement(AbstractJcsCacheActor.class, KnowledgeJcsCacheActor.class)
                .build(KnowledgeJcsCacheActorFactory.class));
        bind(IDGenerator.class).annotatedWith(IdsKnowledge.class).toProvider(IdsKnowledgeProvider.class)
                .asEagerSingleton();
    }

    @Singleton
    @Provides
    @Named("knowledge-storages")
    public Map<String, GameObjectKnowledge> getStorages() {
        return storages;
    }

    /**
     * Returns a map from the type-ID to the named type. This is needed because the
     * cache can
     */
    @Singleton
    @Provides
    @Named("knowledge-tidTypeMap")
    public LongObjectMap<String> getTidTypeMap() {
        return tidType;
    }

}
