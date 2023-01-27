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

import com.anrisoftware.dwarfhustle.model.api.objects.GameMapObject;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.metadata.schema.OType;

/**
 * Creates the schema for the {@link GameMapObject}.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public class GameMapObjectSchema implements GameObjectSchema {

	public static final String Z_FIELD = "z";

	public static final String Y_FIELD = "y";

	public static final String X_FIELD = "x";

	public static final String MAPID_FIELD = "mapid";

	@Override
	public void createSchema(Object db) {
		var odb = (ODatabaseDocument) db;
		var c = odb.createClass(GameMapObject.OBJECT_TYPE, GameObject.OBJECT_TYPE);
		c.createProperty(MAPID_FIELD, OType.INTEGER);
		c.createProperty(X_FIELD, OType.INTEGER);
		c.createProperty(Y_FIELD, OType.INTEGER);
		c.createProperty(Z_FIELD, OType.INTEGER);
		try (var q = odb.command(
				"CREATE INDEX GameMapObject_type_pos ON GameMapObject (objecttype, mapid, x, y, z) NOTUNIQUE METADATA {ignoreNullValues: false}")) {
		}
	}

}
