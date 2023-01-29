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
package com.anrisoftware.dwarfhustle.model.db.orientdb.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import com.anrisoftware.dwarfhustle.model.api.objects.GameMap;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObjectStorage;
import com.anrisoftware.dwarfhustle.model.api.objects.MapBlock;
import com.anrisoftware.dwarfhustle.model.api.objects.MapTile;
import com.anrisoftware.dwarfhustle.model.api.objects.WorldMap;
import com.anrisoftware.dwarfhustle.model.db.orientdb.objects.ObjectsDbActor.ObjectsDbActorFactory;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.assistedinject.FactoryModuleBuilder;

/**
 *
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public class ObjectsDbModule extends AbstractModule {

	@Override
	protected void configure() {
		install(new FactoryModuleBuilder().implement(ObjectsDbActor.class, ObjectsDbActor.class)
				.build(ObjectsDbActorFactory.class));
	}

	@Singleton
	@Provides
	public Map<String, GameObjectStorage> getStorages() {
		var map = new HashMap<String, GameObjectStorage>();
		var mapTileStorage = new MapTileStorage();
		map.put(MapTile.OBJECT_TYPE, mapTileStorage);
		var mapBlockStorage = new MapBlockStorage();
		mapBlockStorage.setStorages(map);
		map.put(MapBlock.OBJECT_TYPE, mapBlockStorage);
		map.put(GameMap.OBJECT_TYPE, new GameMapStorage());
		map.put(WorldMap.OBJECT_TYPE, new WorldMapStorage());
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
		list.add(new MapTileSchema());
		list.add(new MapBlockSchema());
		return list;
	}
}
