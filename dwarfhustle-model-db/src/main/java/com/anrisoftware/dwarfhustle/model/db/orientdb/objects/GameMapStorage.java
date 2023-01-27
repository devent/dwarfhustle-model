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

import static com.anrisoftware.dwarfhustle.model.db.orientdb.objects.GameMapSchema.DEPTH_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.objects.GameMapSchema.HEIGHT_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.objects.GameMapSchema.MAPID_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.objects.GameMapSchema.NAME_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.objects.GameMapSchema.WIDTH_FIELD;

import com.anrisoftware.dwarfhustle.model.api.objects.GameMap;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;
import com.orientechnologies.orient.core.record.OElement;

/**
 * Saves and loads the attributes of a {@link GameMap} from the database.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public class GameMapStorage extends AbstractGameObjectStorage {

	@Override
	public void save(Object db, Object o, GameObject go) {
		if (!go.isDirty()) {
			return;
		}
		var v = (OElement) o;
		var mb = (GameMap) go;
		v.setProperty(NAME_FIELD, mb.getName());
		v.setProperty(MAPID_FIELD, mb.getMapid());
		v.setProperty(WIDTH_FIELD, mb.getWidth());
		v.setProperty(HEIGHT_FIELD, mb.getHeight());
		v.setProperty(DEPTH_FIELD, mb.getDepth());
		super.save(db, o, go);
	}

	@Override
	public GameObject load(Object db, Object o, GameObject go) {
		var v = (OElement) o;
		var mb = (GameMap) go;
		mb.setName(v.getProperty(NAME_FIELD));
		mb.setMapid(v.getProperty(MAPID_FIELD));
		mb.setWidth(v.getProperty(WIDTH_FIELD));
		mb.setHeight(v.getProperty(HEIGHT_FIELD));
		mb.setDepth(v.getProperty(DEPTH_FIELD));
		return super.load(db, o, go);
	}

	@Override
	public GameObject create() {
		return new GameMap();
	}
}
