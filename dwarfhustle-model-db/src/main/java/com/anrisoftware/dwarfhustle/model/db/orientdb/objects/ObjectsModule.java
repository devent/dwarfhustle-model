/*
 * dwarfhustle-model-actor - Manages the compile dependencies for the model.
 * Copyright © 2022 Erwin Müller (erwin.mueller@anrisoftware.com)
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

import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

import com.anrisoftware.dwarfhustle.model.api.GameObjectStorage;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

/**
 *
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public class ObjectsModule extends AbstractModule {

    @Override
    protected void configure() {
    }

	@Singleton
	@Provides
	public Map<String, GameObjectStorage> getStorages() {
		var map = new HashMap<String, GameObjectStorage>();
		map.put("MapTile", new MapTileStorage());
		return map;
	}
}
