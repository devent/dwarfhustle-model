package com.anrisoftware.dwarfhustle.model.db.orientdb.objects;

import com.anrisoftware.dwarfhustle.model.api.GameObject;
import com.anrisoftware.dwarfhustle.model.api.MapBlock;
import com.anrisoftware.dwarfhustle.model.api.MapTile;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.metadata.schema.OType;

/**
 * Creates the schema for the {@link MapBlock}.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public class MapBlockSchema implements GameObjectSchema {

	public static final String OBJECTID_FIELD = "objectid";

	public static final String BLOCK_ID_CLASS = "MapBlockId";

	public static final String BLOCKS_FIELD = "blocks";

	public static final String TILES_FIELD = "tiles";

	public static final String MAPID_FIELD = "mapid";

	public static final String START_X_FIELD = "sx";

	public static final String START_Y_FIELD = "sy";

	public static final String START_Z_FIELD = "sz";

	public static final String END_X_FIELD = "ex";

	public static final String END_Y_FIELD = "ey";

	public static final String END_Z_FIELD = "ez";

	@Override
	public void createSchema(Object db) {
		var odb = (ODatabaseDocument) db;
		var c = odb.createClass(MapBlock.OBJECT_TYPE, GameObject.OBJECT_TYPE);
		var cid = odb.createClass(BLOCK_ID_CLASS);
		cid.createProperty(OBJECTID_FIELD, OType.LONG);
		c.createProperty(BLOCKS_FIELD, OType.EMBEDDEDMAP, cid);
		var mapTile = odb.getClass(MapTile.OBJECT_TYPE);
		c.createProperty(TILES_FIELD, OType.EMBEDDEDMAP, mapTile);
		c.createProperty(MAPID_FIELD, OType.INTEGER);
		c.createProperty(START_X_FIELD, OType.INTEGER);
		c.createProperty(START_Y_FIELD, OType.INTEGER);
		c.createProperty(START_Z_FIELD, OType.INTEGER);
		c.createProperty(END_X_FIELD, OType.INTEGER);
		c.createProperty(END_Y_FIELD, OType.INTEGER);
		c.createProperty(END_Z_FIELD, OType.INTEGER);
		try (var q = odb.command(
				"CREATE INDEX MapBlock_type_pos ON MapBlock (objecttype, mapid, sx, sy, sz, ex, ey, ez) UNIQUE METADATA {ignoreNullValues: false}")) {
		}
	}

}
