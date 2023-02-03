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

import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapObjectSchema.MAPID_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapObjectSchema.X_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapObjectSchema.Y_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapObjectSchema.Z_FIELD;

import com.anrisoftware.dwarfhustle.model.api.objects.GameMapObject;
import com.anrisoftware.dwarfhustle.model.api.objects.GameMapPos;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;
import com.anrisoftware.dwarfhustle.model.api.objects.MapTile;
import com.orientechnologies.orient.core.record.OElement;

/**
 * Stores and retrieves the properties of a {@link GameMapObject} to/from the
 * database. Does not commit the changes into the database.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public class AbstractGameMapObjectStorage extends AbstractGameObjectStorage {

	@Override
	public void store(Object db, Object o, GameObject go) {
		if (!go.isDirty()) {
			return;
		}
		var v = (OElement) o;
		var gmo = (GameMapObject) go;
		v.setProperty(MAPID_FIELD, gmo.getPos().getMapid());
		v.setProperty(X_FIELD, gmo.getPos().getX());
		v.setProperty(Y_FIELD, gmo.getPos().getY());
		v.setProperty(Z_FIELD, gmo.getPos().getZ());
		super.store(db, o, go);
	}

	@Override
	public GameObject retrieve(Object db, Object o, GameObject go) {
		var v = (OElement) o;
		var gmo = (GameMapObject) go;
		gmo.setPos(new GameMapPos(v.getProperty(MAPID_FIELD), v.getProperty(X_FIELD), v.getProperty(Y_FIELD),
				v.getProperty(Z_FIELD)));
		gmo.setRid(v.getIdentity());
		return super.retrieve(db, o, go);
	}

	@Override
	public GameObject create() {
		return new MapTile();
	}
}
