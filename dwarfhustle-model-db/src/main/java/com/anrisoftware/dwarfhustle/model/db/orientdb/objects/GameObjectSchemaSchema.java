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

	public static final String OBJECTTYPE_FIELD = "objecttype";

	public static final String OBJECTID_FIELD = "objectid";

	@Override
	public void createSchema(Object db) {
		var odb = (ODatabaseDocument) db;
		var c = odb.createVertexClass(GameObject.OBJECT_TYPE);
		c.createProperty(OBJECTID_FIELD, OType.LONG);
		c.createProperty(OBJECTTYPE_FIELD, OType.STRING);
		try (var q = odb.command(
				"CREATE INDEX GameObject_type_id ON GameObject (objecttype, objectid) UNIQUE METADATA {ignoreNullValues: false}")) {
		}
	}

}
