package com.anrisoftware.dwarfhustle.model.db.orientdb.objects;

import com.anrisoftware.dwarfhustle.model.api.GameMapObject;
import com.anrisoftware.dwarfhustle.model.api.GameObject;
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
