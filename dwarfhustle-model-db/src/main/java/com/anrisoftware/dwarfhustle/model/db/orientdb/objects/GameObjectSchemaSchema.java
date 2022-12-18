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

	@Override
	public void createSchema(Object db) {
		var odb = (ODatabaseDocument) db;
		var c = odb.createVertexClass("GameObject");
		c.createProperty("x", OType.INTEGER);
		c.createProperty("y", OType.INTEGER);
		c.createProperty("z", OType.INTEGER);
	}

}
