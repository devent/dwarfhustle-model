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
package com.anrisoftware.dwarfhustle.model.db.orientdb.storages;

import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;
import com.anrisoftware.dwarfhustle.model.api.objects.MapTile;
import com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapTileSchema;
import com.orientechnologies.orient.core.record.OElement;

/**
 * Saves and loads the attributes of a {@link MapTile} from the database.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public class MapTileStorage extends AbstractGameMapObjectStorage {

	@Override
	public void store(Object db, Object o, GameObject go) {
		var v = (OElement) o;
		var mt = (MapTile) go;
		v.setProperty(MapTileSchema.MATERIAL_FIELD, mt.getMaterial());
		super.store(db, o, go);
	}

	@Override
	public GameObject retrieve(Object db, Object o, GameObject go) {
		var v = (OElement) o;
		var mt = (MapTile) go;
		mt.setMaterial(v.getProperty("material"));
		return super.retrieve(db, o, go);
	}

	@Override
	public GameObject create() {
		return new MapTile();
	}
}
