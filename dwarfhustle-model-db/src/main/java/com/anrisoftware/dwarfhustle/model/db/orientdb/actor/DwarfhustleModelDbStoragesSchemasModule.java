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
package com.anrisoftware.dwarfhustle.model.db.orientdb.actor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.anrisoftware.dwarfhustle.model.api.objects.GameMap;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObjectStorage;
import com.anrisoftware.dwarfhustle.model.api.objects.WorldMap;
import com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapObjectSchema;
import com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapSchema;
import com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameObjectSchema;
import com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameObjectSchemaSchema;
import com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.WorldMapSchema;
import com.anrisoftware.dwarfhustle.model.db.orientdb.storages.GameMapStorage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.storages.WorldMapStorage;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import jakarta.inject.Singleton;

/**
 * @author Erwin Müller
 */
public class DwarfhustleModelDbStoragesSchemasModule extends AbstractModule {

    @Override
    protected void configure() {
    }

    @Singleton
    @Provides
    public Map<String, GameObjectStorage> getStorages() {
        var map = new HashMap<String, GameObjectStorage>();
        map.put(GameMap.OBJECT_TYPE, new GameMapStorage());
        var worldMapStorage = new WorldMapStorage();
        map.put(WorldMap.OBJECT_TYPE, worldMapStorage);
        return map;
    }

    @Singleton
    @Provides
    public List<GameObjectSchema> getSchemas() {
        var list = new ArrayList<GameObjectSchema>();
        list.add(new GameObjectSchemaSchema());
        list.add(new GameMapObjectSchema());
        list.add(new WorldMapSchema());
        list.add(new GameMapSchema());
        return list;
    }

}
