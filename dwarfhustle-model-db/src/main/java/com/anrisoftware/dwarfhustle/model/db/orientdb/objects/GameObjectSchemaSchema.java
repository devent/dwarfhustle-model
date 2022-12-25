package com.anrisoftware.dwarfhustle.model.db.orientdb.objects;

import com.anrisoftware.dwarfhustle.model.api.GameObject;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.metadata.schema.OType;

/**
 * Creates the schema for the {@link GameObject}.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public class GameObjectSchemaSchema implements GameObjectSchema {

	public static final String Z_FIELD = "z";

	public static final String Y_FIELD = "y";

	public static final String X_FIELD = "x";

	public static final String MAPID_FIELD = "mapid";

	public static final String OBJECTTYPE_FIELD = "objecttype";

	public static final String OBJECTID_FIELD = "objectid";

	@Override
	public void createSchema(Object db) {
		var odb = (ODatabaseDocument) db;
		var c = odb.createVertexClass(GameObject.TYPE);
		c.createProperty(OBJECTID_FIELD, OType.LONG);
		c.createProperty(OBJECTTYPE_FIELD, OType.STRING);
		c.createProperty(MAPID_FIELD, OType.INTEGER);
		c.createProperty(X_FIELD, OType.INTEGER);
		c.createProperty(Y_FIELD, OType.INTEGER);
		c.createProperty(Z_FIELD, OType.INTEGER);
		try (var q = odb.command(
				"CREATE INDEX GameObject_type_pos ON GameObject (objecttype, mapid, x, y, z) NOTUNIQUE METADATA {ignoreNullValues: false}")) {
		}
		try (var q = odb.command(
				"CREATE INDEX GameObject_type_id ON GameObject (objecttype, objectid) UNIQUE METADATA {ignoreNullValues: false}")) {
		}
	}

}
