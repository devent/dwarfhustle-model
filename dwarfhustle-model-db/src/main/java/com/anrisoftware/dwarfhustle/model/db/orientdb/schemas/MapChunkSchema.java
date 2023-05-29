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

import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;
import com.anrisoftware.dwarfhustle.model.api.objects.MapBlock;
import com.anrisoftware.dwarfhustle.model.api.objects.MapChunk;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.metadata.schema.OType;

/**
 * Creates the schema for the {@link MapChunk}.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public class MapChunkSchema implements GameObjectSchema {

	public static final String OBJECTID_FIELD = "objectid";

    public static final String CHUNK_ID_CLASS = "MapChunkId";

	public static final String BLOCKS_FIELD = "blocks";

	public static final String TILES_FIELD = "tiles";

	public static final String MAPID_FIELD = "mapid";

	public static final String START_X_FIELD = "sx";

	public static final String START_Y_FIELD = "sy";

	public static final String START_Z_FIELD = "sz";

	public static final String END_X_FIELD = "ex";

	public static final String END_Y_FIELD = "ey";

	public static final String END_Z_FIELD = "ez";

    public static final String ROOT_FIELD = "root";

    public static final String NEIGHBOR_T_FIELD = "nt";

    public static final String NEIGHBOR_B_FIELD = "nb";

    public static final String NEIGHBOR_S_FIELD = "ns";

    public static final String NEIGHBOR_E_FIELD = "ne";

    public static final String NEIGHBOR_N_FIELD = "nn";

    public static final String NEIGHBOR_W_FIELD = "nw";

    public static final String PARENT_FIELD = "parent";

	@Override
	public void createSchema(Object db) {
		var odb = (ODatabaseDocument) db;
		var c = odb.createClass(MapChunk.OBJECT_TYPE, GameObject.OBJECT_TYPE);
		var cid = odb.createClass(CHUNK_ID_CLASS);
		cid.createProperty(OBJECTID_FIELD, OType.LONG);
		c.createProperty(BLOCKS_FIELD, OType.EMBEDDEDMAP, cid);
		var mapTile = odb.getClass(MapBlock.OBJECT_TYPE);
		c.createProperty(TILES_FIELD, OType.EMBEDDEDMAP, mapTile);
		c.createProperty(MAPID_FIELD, OType.INTEGER);
		c.createProperty(START_X_FIELD, OType.INTEGER);
		c.createProperty(START_Y_FIELD, OType.INTEGER);
		c.createProperty(START_Z_FIELD, OType.INTEGER);
		c.createProperty(END_X_FIELD, OType.INTEGER);
		c.createProperty(END_Y_FIELD, OType.INTEGER);
		c.createProperty(END_Z_FIELD, OType.INTEGER);
        c.createProperty(ROOT_FIELD, OType.BOOLEAN);
        c.createProperty(NEIGHBOR_T_FIELD, OType.LONG);
        c.createProperty(NEIGHBOR_B_FIELD, OType.LONG);
        c.createProperty(NEIGHBOR_S_FIELD, OType.LONG);
        c.createProperty(NEIGHBOR_E_FIELD, OType.LONG);
        c.createProperty(NEIGHBOR_N_FIELD, OType.LONG);
        c.createProperty(NEIGHBOR_W_FIELD, OType.LONG);
        c.createProperty(PARENT_FIELD, OType.LONG);
		try (var q = odb.command(
                "CREATE INDEX MapChunk_type_pos ON MapChunk (objecttype, mapid, sx, sy, sz, ex, ey, ez) UNIQUE METADATA {ignoreNullValues: false}")) {
		}
	}

}
