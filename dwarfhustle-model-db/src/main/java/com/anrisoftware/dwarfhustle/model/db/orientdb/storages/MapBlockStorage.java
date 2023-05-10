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
import com.anrisoftware.dwarfhustle.model.api.objects.MapBlock;
import com.anrisoftware.dwarfhustle.model.api.objects.PropertiesSet;
import com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapBlockSchema;
import com.orientechnologies.orient.core.record.OElement;

/**
 * Saves and loads the attributes of a {@link MapBlock} from the database.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public class MapBlockStorage extends AbstractGameMapObjectStorage {

	@Override
	public void store(Object db, Object o, GameObject go) {
		var v = (OElement) o;
		var mt = (MapBlock) go;
		v.setProperty(MapBlockSchema.MATERIAL_FIELD, mt.getMaterial());
        v.setProperty(MapBlockSchema.PROPERTIES_FIELD, mt.getP().bits);
		super.store(db, o, go);
	}

	@Override
	public GameObject retrieve(Object db, Object o, GameObject go) {
		var v = (OElement) o;
		var mt = (MapBlock) go;
		mt.setMaterial(v.getProperty("material"));
        mt.setP(new PropertiesSet(v.getProperty(MapBlockSchema.PROPERTIES_FIELD)));
		return super.retrieve(db, o, go);
	}

	@Override
	public GameObject create() {
		return new MapBlock();
	}
}
