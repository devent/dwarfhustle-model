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

import jakarta.inject.Singleton;

import com.anrisoftware.dwarfhustle.model.api.objects.GameMap;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObjectStorage;
import com.anrisoftware.dwarfhustle.model.api.objects.MapBlock;
import com.anrisoftware.dwarfhustle.model.api.objects.MapChunk;
import com.anrisoftware.dwarfhustle.model.api.objects.WorldMap;
import com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapObjectSchema;
import com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapSchema;
import com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameObjectSchema;
import com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameObjectSchemaSchema;
import com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapBlockSchema;
import com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapChunkSchema;
import com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.WorldMapSchema;
import com.anrisoftware.dwarfhustle.model.db.orientdb.storages.GameMapStorage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.storages.MapBlockStorage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.storages.MapChunkStorage;
import com.anrisoftware.dwarfhustle.model.db.orientdb.storages.WorldMapStorage;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

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
        var mapTileStorage = new MapBlockStorage();
        map.put(MapBlock.OBJECT_TYPE, mapTileStorage);
        var mapBlockStorage = new MapChunkStorage();
        mapBlockStorage.setStorages(map);
        map.put(MapChunk.OBJECT_TYPE, mapBlockStorage);
        map.put(GameMap.OBJECT_TYPE, new GameMapStorage());
        var worldMapStorage = new WorldMapStorage();
        worldMapStorage.setStorages(map);
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
        list.add(new MapBlockSchema());
        list.add(new MapChunkSchema());
        return list;
    }

}
