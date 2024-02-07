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
package com.anrisoftware.dwarfhustle.model.db.orientdb.storages;

import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapSchema.AREA_NW_LAT_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapSchema.AREA_NW_LON_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapSchema.AREA_SE_LAT_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapSchema.AREA_SE_LON_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapSchema.BLOCKS_COUNT_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapSchema.CAMERA_POS_X_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapSchema.CAMERA_POS_Y_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapSchema.CAMERA_POS_Z_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapSchema.CAMERA_ROT_W_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapSchema.CAMERA_ROT_X_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapSchema.CAMERA_ROT_Y_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapSchema.CAMERA_ROT_Z_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapSchema.CHUNKS_COUNT_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapSchema.CHUNK_SIZE_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapSchema.CURSOR_X_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapSchema.CURSOR_Y_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapSchema.CURSOR_Z_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapSchema.DEPTH_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapSchema.HEIGHT_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapSchema.NAME_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapSchema.ROOT_CHUNK_CLASS;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapSchema.TIME_ZONE_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapSchema.WIDTH_FIELD;

import java.time.ZoneOffset;

import com.anrisoftware.dwarfhustle.model.api.objects.GameMap;
import com.anrisoftware.dwarfhustle.model.api.objects.MapArea;
import com.anrisoftware.dwarfhustle.model.api.objects.MapChunk;
import com.anrisoftware.dwarfhustle.model.api.objects.MapCursor;
import com.anrisoftware.dwarfhustle.model.api.objects.StoredObject;
import com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.WorldMapSchema;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.record.OElement;

/**
 * Stores and retrieves the properties of a {@link GameMap} to/from the
 * database. Does not commit the changes into the database.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public class GameMapStorage extends AbstractGameObjectStorage {

    @Override
    public void store(Object db, Object o, StoredObject go) {
        var v = (OElement) o;
        var gm = (GameMap) go;
        var odb = (ODatabaseDocument) db;
        v.setProperty(NAME_FIELD, gm.name);
        storeRootChunk(odb, v, gm);
        v.setProperty(WIDTH_FIELD, gm.width);
        v.setProperty(HEIGHT_FIELD, gm.height);
        v.setProperty(DEPTH_FIELD, gm.depth);
        v.setProperty(CHUNK_SIZE_FIELD, gm.chunkSize);
        v.setProperty(TIME_ZONE_FIELD, gm.timeZone.getTotalSeconds());
        v.setProperty(AREA_NW_LAT_FIELD, gm.area.nw.lat);
        v.setProperty(AREA_NW_LON_FIELD, gm.area.nw.lon);
        v.setProperty(AREA_SE_LAT_FIELD, gm.area.se.lat);
        v.setProperty(AREA_SE_LON_FIELD, gm.area.se.lon);
        v.setProperty(CAMERA_POS_X_FIELD, gm.cameraPos[0]);
        v.setProperty(CAMERA_POS_Y_FIELD, gm.cameraPos[1]);
        v.setProperty(CAMERA_POS_Z_FIELD, gm.cameraPos[2]);
        v.setProperty(CAMERA_ROT_X_FIELD, gm.cameraRot[0]);
        v.setProperty(CAMERA_ROT_Y_FIELD, gm.cameraRot[1]);
        v.setProperty(CAMERA_ROT_Z_FIELD, gm.cameraRot[2]);
        v.setProperty(CAMERA_ROT_W_FIELD, gm.cameraRot[3]);
        v.setProperty(CURSOR_X_FIELD, gm.cursor.x);
        v.setProperty(CURSOR_Y_FIELD, gm.cursor.y);
        v.setProperty(CURSOR_Z_FIELD, gm.cursor.z);
        v.setProperty(CHUNKS_COUNT_FIELD, gm.chunksCount);
        v.setProperty(BLOCKS_COUNT_FIELD, gm.blocksCount);
        super.store(db, o, go);
    }

    private void storeRootChunk(ODatabaseDocument odb, OElement v, GameMap gm) {
        try (var rs = queryByObjectId(odb, MapChunk.OBJECT_TYPE, gm.root)) {
            if (rs.hasNext()) {
                var vv = rs.next().getVertex();
                if (vv.isPresent()) {
                    odb.newEdge(v.asVertex().get(), vv.get(), ROOT_CHUNK_CLASS).save();
                }
            }
        }
    }

    @Override
    public StoredObject retrieve(Object db, Object o, StoredObject go) {
        var v = (OElement) o;
        var gm = (GameMap) go;
        gm.name = v.getProperty(NAME_FIELD);
        gm.width = v.getProperty(WIDTH_FIELD);
        gm.height = v.getProperty(HEIGHT_FIELD);
        gm.depth = v.getProperty(DEPTH_FIELD);
        gm.chunkSize = v.getProperty(CHUNK_SIZE_FIELD);
        gm.root = retrieveEdgeOutToId(v, ROOT_CHUNK_CLASS);
        gm.world = retrieveEdgeInFromId(v, WorldMapSchema.MAP_CLASS);
        gm.timeZone = ZoneOffset.ofTotalSeconds(v.getProperty(TIME_ZONE_FIELD));
        gm.area = MapArea.create(v.getProperty(AREA_NW_LAT_FIELD), v.getProperty(AREA_NW_LON_FIELD),
                v.getProperty(AREA_SE_LAT_FIELD), v.getProperty(AREA_SE_LON_FIELD));
        gm.setCameraPos(v.getProperty(CAMERA_POS_X_FIELD), v.getProperty(CAMERA_POS_Y_FIELD),
                v.getProperty(CAMERA_POS_Z_FIELD));
        gm.setCameraRot(v.getProperty(CAMERA_ROT_X_FIELD), v.getProperty(CAMERA_ROT_Y_FIELD),
                v.getProperty(CAMERA_ROT_Z_FIELD), v.getProperty(CAMERA_ROT_W_FIELD));
        gm.setCursor(new MapCursor(v.getProperty(CURSOR_X_FIELD), v.getProperty(CURSOR_Y_FIELD),
                v.getProperty(CURSOR_Z_FIELD)));
        gm.chunksCount = v.getProperty(CHUNKS_COUNT_FIELD);
        gm.blocksCount = v.getProperty(BLOCKS_COUNT_FIELD);
        return super.retrieve(db, o, go);
    }

    @Override
    public StoredObject create() {
        return new GameMap();
    }
}
