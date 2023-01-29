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

import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;
import com.anrisoftware.dwarfhustle.model.api.objects.WorldMap;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.metadata.schema.OType;

/**
 * Creates the schema for the {@link WorldMap}.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public class WorldMapSchema implements GameObjectSchema {

	public static final String NAME_FIELD = "name";

	public static final String DIST_LAT_FIELD = "distlat";

	public static final String DIST_LON_FIELD = "distlon";

	public static final String TIME_FIELD = "time";

	public static final String WORLD_CLASS = "world";

	@Override
	public void createSchema(Object db) {
		var odb = (ODatabaseDocument) db;
		var c = odb.createClass(WorldMap.OBJECT_TYPE, GameObject.OBJECT_TYPE);
		c.createProperty(NAME_FIELD, OType.STRING);
		c.createProperty(DIST_LAT_FIELD, OType.FLOAT);
		c.createProperty(DIST_LON_FIELD, OType.FLOAT);
		c.createProperty(TIME_FIELD, OType.STRING);
		odb.createEdgeClass(WORLD_CLASS);
		try (var q = odb.command(
				"CREATE INDEX WorldMap_name ON WorldMap (objecttype, name) NOTUNIQUE METADATA {ignoreNullValues: false}")) {
		}
	}

}
