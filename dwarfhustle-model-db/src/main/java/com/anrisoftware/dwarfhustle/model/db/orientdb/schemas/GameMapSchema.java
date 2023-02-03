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
package com.anrisoftware.dwarfhustle.model.db.orientdb.schemas;

import com.anrisoftware.dwarfhustle.model.api.objects.GameMap;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.metadata.schema.OType;

/**
 * Creates the schema for the {@link GameMap}.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public class GameMapSchema implements GameObjectSchema {

	public static final String NAME_FIELD = "name";

	public static final String MAPID_FIELD = "mapid";

	public static final String WIDTH_FIELD = "width";

	public static final String HEIGHT_FIELD = "height";

	public static final String DEPTH_FIELD = "depth";

	public static final String BLOCK_SIZE_FIELD = "blockSize";

	@Override
	public void createSchema(Object db) {
		var odb = (ODatabaseDocument) db;
		var c = odb.createClass(GameMap.OBJECT_TYPE, GameObject.OBJECT_TYPE);
		c.createProperty(NAME_FIELD, OType.STRING);
		c.createProperty(MAPID_FIELD, OType.INTEGER);
		c.createProperty(WIDTH_FIELD, OType.INTEGER);
		c.createProperty(HEIGHT_FIELD, OType.INTEGER);
		c.createProperty(DEPTH_FIELD, OType.INTEGER);
		c.createProperty(BLOCK_SIZE_FIELD, OType.INTEGER);
		try (var q = odb.command(
				"CREATE INDEX GameMap_name ON GameMap (objecttype, name) NOTUNIQUE METADATA {ignoreNullValues: false}")) {
		}
	}

}
