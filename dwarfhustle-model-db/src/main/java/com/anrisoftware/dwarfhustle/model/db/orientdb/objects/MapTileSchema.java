package com.anrisoftware.dwarfhustle.model.db.orientdb.objects;

import com.anrisoftware.dwarfhustle.model.api.GameMapObject;
import com.anrisoftware.dwarfhustle.model.api.MapTile;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.metadata.schema.OType;

/**
 * Creates the schema for the {@link MapTile}.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public class MapTileSchema implements GameObjectSchema {

	public static final String MATERIAL_FIELD = "material";

	@Override
	public void createSchema(Object db) {
		var odb = (ODatabaseDocument) db;
		var c = odb.createClass(MapTile.OBJECT_TYPE, GameMapObject.OBJECT_TYPE);
		c.createProperty(MATERIAL_FIELD, OType.STRING);
	}

}
