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

    public static final String CHUNKS_FIELD = "chunks";

    public static final String BLOCKS_FIELD = "blocks";

    public static final String BLOCK_ID_CLASS = "MapBlockId";

    public static final String MAP_FIELD = "map";

    public static final String ROOT_FIELD = "root";

    public static final String PARENT_FIELD = "parent";

    public static final String POS_START_X_FIELD = "sx";

    public static final String POS_START_Y_FIELD = "sy";

    public static final String POS_START_Z_FIELD = "sz";

    public static final String POS_END_X_FIELD = "ex";

    public static final String POS_END_Y_FIELD = "ey";

    public static final String POS_END_Z_FIELD = "ez";

    @Override
    public void createSchema(Object db) {
        var odb = (ODatabaseDocument) db;
        var c = odb.createClass(MapChunk.OBJECT_TYPE, GameObject.OBJECT_TYPE);
        var chunksid = odb.createClass(CHUNK_ID_CLASS);
        chunksid.createProperty(OBJECTID_FIELD, OType.LONG);
        c.createProperty(CHUNKS_FIELD, OType.EMBEDDEDMAP, chunksid);
        var blocksid = odb.createClass(BLOCK_ID_CLASS);
        blocksid.createProperty(OBJECTID_FIELD, OType.LONG);
        c.createProperty(BLOCKS_FIELD, OType.EMBEDDEDMAP, blocksid);
        c.createProperty(MAP_FIELD, OType.LONG);
        new CenterExtentSchema().createSchema(db, c);
        c.createProperty(ROOT_FIELD, OType.BOOLEAN);
        new NeighboringSchema().createSchema(db, c);
        c.createProperty(PARENT_FIELD, OType.LONG);
        c.createProperty(POS_START_X_FIELD, OType.INTEGER);
        c.createProperty(POS_START_Y_FIELD, OType.INTEGER);
        c.createProperty(POS_START_Z_FIELD, OType.INTEGER);
        c.createProperty(POS_END_X_FIELD, OType.INTEGER);
        c.createProperty(POS_END_Y_FIELD, OType.INTEGER);
        c.createProperty(POS_END_Z_FIELD, OType.INTEGER);
        try (var q = odb.command(
                "CREATE INDEX MapChunk_type_pos ON MapChunk (objecttype, map, sx, sy, sz, ex, ey, ez) UNIQUE METADATA {ignoreNullValues: false}")) {
        }
    }

}
