/*
 * dwarfhustle-model-db - Manages the compile dependencies for the model.
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
package com.anrisoftware.dwarfhustle.model.db.lmbd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

import org.eclipse.collections.api.factory.primitive.IntObjectMaps;
import org.eclipse.collections.api.factory.primitive.IntSets;
import org.eclipse.collections.api.map.primitive.IntObjectMap;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.api.set.primitive.IntSet;
import org.eclipse.collections.api.set.primitive.MutableIntSet;

import com.anrisoftware.dwarfhustle.model.api.objects.StoredObject;
import com.anrisoftware.dwarfhustle.model.db.buffers.StoredObjectBuffer;
import com.anrisoftware.dwarfhustle.model.db.lmbd.GameObjectsLmbdStorage.GameObjectsLmbdStorageFactory;
import com.anrisoftware.dwarfhustle.model.db.lmbd.MapChunksLmbdStorage.MapChunksLmbdStorageFactory;
import com.anrisoftware.dwarfhustle.model.db.lmbd.MapObjectsLmbdStorage.MapObjectsLmbdStorageFactory;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.assistedinject.FactoryModuleBuilder;

import jakarta.inject.Singleton;

/**
 * @see GameObjectsLmbdStorageFactory
 * @see MapObjectsLmbdStorageFactory
 * @see MapChunksLmbdStorageFactory
 */
public class DwarfhustleModelDbLmbdModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new FactoryModuleBuilder().implement(GameObjectsLmbdStorage.class, GameObjectsLmbdStorage.class)
                .build(GameObjectsLmbdStorageFactory.class));
        install(new FactoryModuleBuilder().implement(MapObjectsLmbdStorage.class, MapObjectsLmbdStorage.class)
                .build(MapObjectsLmbdStorageFactory.class));
        install(new FactoryModuleBuilder().implement(MapChunksLmbdStorage.class, MapChunksLmbdStorage.class)
                .build(MapChunksLmbdStorageFactory.class));
    }

    @Singleton
    @Provides
    public IntSet getObjectTypes() {
        MutableIntSet set = IntSets.mutable.empty();
        var loader = ServiceLoader.load(StoredObject.class);
        StreamSupport.stream(loader.spliterator(), true).forEach(s -> {
            set.add(s.getObjectType());
        });
        assertThat(set.size(), is(greaterThan(0)));
        return set;
    }

    @Singleton
    @Provides
    public IntObjectMap<StoredObjectBuffer> getTypeReadBuffers() {
        MutableIntObjectMap<StoredObjectBuffer> map = IntObjectMaps.mutable.empty();
        var loader = ServiceLoader.load(StoredObjectBuffer.class);
        StreamSupport.stream(loader.spliterator(), true).forEach(s -> {
            map.put(s.getObjectType(), s);
        });
        assertThat(map.size(), is(greaterThan(0)));
        return map;
    }
}
