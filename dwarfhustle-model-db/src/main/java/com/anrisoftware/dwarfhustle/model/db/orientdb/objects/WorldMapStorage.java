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

import static com.anrisoftware.dwarfhustle.model.db.orientdb.objects.WorldMapSchema.DIST_LAT_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.objects.WorldMapSchema.DIST_LON_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.objects.WorldMapSchema.NAME_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.objects.WorldMapSchema.TIME_FIELD;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.anrisoftware.dwarfhustle.model.api.objects.GameMap;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;
import com.anrisoftware.dwarfhustle.model.api.objects.WorldMap;
import com.orientechnologies.orient.core.record.OElement;

/**
 * Stores and retrieves the properties of a {@link WorldMap} to/from the
 * database. Does not commit the changes into the database.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public class WorldMapStorage extends AbstractGameObjectStorage {

	@Override
	public void store(Object db, Object o, GameObject go) {
		if (!go.isDirty()) {
			return;
		}
		var v = (OElement) o;
		var mb = (WorldMap) go;
		v.setProperty(NAME_FIELD, mb.getName());
		v.setProperty(DIST_LAT_FIELD, mb.getDistanceLat());
		v.setProperty(DIST_LON_FIELD, mb.getDistanceLon());
		v.setProperty(TIME_FIELD, toString(mb.getTime()));
		super.store(db, o, go);
	}

	private String toString(LocalDateTime time) {
		return time.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
	}

	@Override
	public GameObject retrieve(Object db, Object o, GameObject go) {
		var v = (OElement) o;
		var mb = (WorldMap) go;
		mb.setName(v.getProperty(NAME_FIELD));
		mb.setDistance(v.getProperty(DIST_LAT_FIELD), v.getProperty(DIST_LON_FIELD));
		mb.setTime(parseTime(v.getProperty(TIME_FIELD)));
		return super.retrieve(db, o, go);
	}

	private LocalDateTime parseTime(String s) {
		return (LocalDateTime) DateTimeFormatter.ISO_LOCAL_DATE_TIME.parse(s);
	}

	@Override
	public GameObject create() {
		return new GameMap();
	}
}
