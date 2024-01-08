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
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapSchema.CAMERA_POS_X_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapSchema.CAMERA_POS_Y_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapSchema.CAMERA_POS_Z_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapSchema.CAMERA_ROT_W_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapSchema.CAMERA_ROT_X_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapSchema.CAMERA_ROT_Y_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapSchema.CAMERA_ROT_Z_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapSchema.CHUNK_SIZE_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapSchema.CURSOR_X_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapSchema.CURSOR_Y_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapSchema.CURSOR_Z_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapSchema.DEPTH_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapSchema.HEIGHT_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapSchema.NAME_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapSchema.ROOT_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapSchema.TIME_ZONE_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapSchema.WIDTH_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapSchema.WORLD_FIELD;

import java.time.ZoneOffset;

import com.anrisoftware.dwarfhustle.model.api.objects.GameMap;
import com.anrisoftware.dwarfhustle.model.api.objects.MapArea;
import com.anrisoftware.dwarfhustle.model.api.objects.MapCursor;
import com.anrisoftware.dwarfhustle.model.api.objects.StoredObject;
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
        var mb = (GameMap) go;
        v.setProperty(NAME_FIELD, mb.name);
        v.setProperty(ROOT_FIELD, mb.root);
        v.setProperty(WIDTH_FIELD, mb.width);
        v.setProperty(HEIGHT_FIELD, mb.height);
        v.setProperty(DEPTH_FIELD, mb.depth);
        v.setProperty(CHUNK_SIZE_FIELD, mb.chunkSize);
        v.setProperty(WORLD_FIELD, mb.world);
        v.setProperty(TIME_ZONE_FIELD, mb.timeZone.getTotalSeconds());
        v.setProperty(AREA_NW_LAT_FIELD, mb.area.nw.lat);
        v.setProperty(AREA_NW_LON_FIELD, mb.area.nw.lon);
        v.setProperty(AREA_SE_LAT_FIELD, mb.area.se.lat);
        v.setProperty(AREA_SE_LON_FIELD, mb.area.se.lon);
        v.setProperty(CAMERA_POS_X_FIELD, mb.cameraPos[0]);
        v.setProperty(CAMERA_POS_Y_FIELD, mb.cameraPos[1]);
        v.setProperty(CAMERA_POS_Z_FIELD, mb.cameraPos[2]);
        v.setProperty(CAMERA_ROT_X_FIELD, mb.cameraRot[0]);
        v.setProperty(CAMERA_ROT_Y_FIELD, mb.cameraRot[1]);
        v.setProperty(CAMERA_ROT_Z_FIELD, mb.cameraRot[2]);
        v.setProperty(CAMERA_ROT_W_FIELD, mb.cameraRot[3]);
        v.setProperty(CURSOR_X_FIELD, mb.cursor.x);
        v.setProperty(CURSOR_Y_FIELD, mb.cursor.y);
        v.setProperty(CURSOR_Z_FIELD, mb.cursor.z);
        super.store(db, o, go);
    }

    @Override
    public StoredObject retrieve(Object db, Object o, StoredObject go) {
        var v = (OElement) o;
        var mb = (GameMap) go;
        mb.name = v.getProperty(NAME_FIELD);
        mb.root = v.getProperty(ROOT_FIELD);
        mb.width = v.getProperty(WIDTH_FIELD);
        mb.height = v.getProperty(HEIGHT_FIELD);
        mb.depth = v.getProperty(DEPTH_FIELD);
        mb.chunkSize = v.getProperty(CHUNK_SIZE_FIELD);
        mb.world = v.getProperty(WORLD_FIELD);
        mb.timeZone = ZoneOffset.ofTotalSeconds(v.getProperty(TIME_ZONE_FIELD));
        mb.area = MapArea.create(v.getProperty(AREA_NW_LAT_FIELD), v.getProperty(AREA_NW_LON_FIELD),
                v.getProperty(AREA_SE_LAT_FIELD), v.getProperty(AREA_SE_LON_FIELD));
        mb.setCameraPos(v.getProperty(CAMERA_POS_X_FIELD), v.getProperty(CAMERA_POS_Y_FIELD),
                v.getProperty(CAMERA_POS_Z_FIELD));
        mb.setCameraRot(v.getProperty(CAMERA_ROT_X_FIELD), v.getProperty(CAMERA_ROT_Y_FIELD),
                v.getProperty(CAMERA_ROT_Z_FIELD), v.getProperty(CAMERA_ROT_W_FIELD));
        mb.setCursor(new MapCursor(v.getProperty(CURSOR_X_FIELD), v.getProperty(CURSOR_Y_FIELD),
                v.getProperty(CURSOR_Z_FIELD)));
        return super.retrieve(db, o, go);
    }

    @Override
    public StoredObject create() {
        return new GameMap();
    }
}
