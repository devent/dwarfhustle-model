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
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapSchema.BLOCK_SIZE_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapSchema.CAMERA_POS_X_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapSchema.CAMERA_POS_Y_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapSchema.CAMERA_POS_Z_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapSchema.CAMERA_ROT_W_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapSchema.CAMERA_ROT_X_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapSchema.CAMERA_ROT_Y_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapSchema.CAMERA_ROT_Z_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapSchema.DEPTH_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapSchema.HEIGHT_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapSchema.MAPID_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapSchema.NAME_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapSchema.TIME_ZONE_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapSchema.WIDTH_FIELD;

import java.time.ZoneOffset;

import com.anrisoftware.dwarfhustle.model.api.objects.GameMap;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;
import com.anrisoftware.dwarfhustle.model.api.objects.MapArea;
import com.anrisoftware.dwarfhustle.model.api.objects.MapCoordinate;
import com.orientechnologies.orient.core.record.OElement;

/**
 * Stores and retrieves the properties of a {@link GameMap} to/from the
 * database. Does not commit the changes into the database.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public class GameMapStorage extends AbstractGameObjectStorage {

	@Override
	public void store(Object db, Object o, GameObject go) {
		if (!go.isDirty()) {
			return;
		}
		var v = (OElement) o;
		var mb = (GameMap) go;
		v.setProperty(NAME_FIELD, mb.getName());
		v.setProperty(MAPID_FIELD, mb.getMapid());
		v.setProperty(WIDTH_FIELD, mb.getWidth());
		v.setProperty(HEIGHT_FIELD, mb.getHeight());
		v.setProperty(DEPTH_FIELD, mb.getDepth());
		v.setProperty(BLOCK_SIZE_FIELD, mb.getBlockSize());
		v.setProperty(TIME_ZONE_FIELD, mb.getTimeZone().getTotalSeconds());
		v.setProperty(AREA_NW_LAT_FIELD, mb.getArea().nw.lat);
		v.setProperty(AREA_NW_LON_FIELD, mb.getArea().nw.lon);
		v.setProperty(AREA_SE_LAT_FIELD, mb.getArea().se.lat);
		v.setProperty(AREA_SE_LON_FIELD, mb.getArea().se.lon);
		v.setProperty(CAMERA_POS_X_FIELD, mb.getCameraPos()[0]);
		v.setProperty(CAMERA_POS_Y_FIELD, mb.getCameraPos()[1]);
		v.setProperty(CAMERA_POS_Z_FIELD, mb.getCameraPos()[2]);
		v.setProperty(CAMERA_ROT_X_FIELD, mb.getCameraRot()[0]);
		v.setProperty(CAMERA_ROT_Y_FIELD, mb.getCameraRot()[1]);
		v.setProperty(CAMERA_ROT_Z_FIELD, mb.getCameraRot()[2]);
		v.setProperty(CAMERA_ROT_W_FIELD, mb.getCameraRot()[3]);
		super.store(db, o, go);
	}

	@Override
	public GameObject retrieve(Object db, Object o, GameObject go) {
		var v = (OElement) o;
		var mb = (GameMap) go;
		mb.setName(v.getProperty(NAME_FIELD));
		mb.setMapid(v.getProperty(MAPID_FIELD));
		mb.setWidth(v.getProperty(WIDTH_FIELD));
		mb.setHeight(v.getProperty(HEIGHT_FIELD));
		mb.setDepth(v.getProperty(DEPTH_FIELD));
		mb.setBlockSize(v.getProperty(BLOCK_SIZE_FIELD));
		mb.setTimeZone(ZoneOffset.ofTotalSeconds(v.getProperty(TIME_ZONE_FIELD)));
		mb.setArea(new MapArea(new MapCoordinate(v.getProperty(AREA_NW_LAT_FIELD), v.getProperty(AREA_NW_LON_FIELD)),
				new MapCoordinate(v.getProperty(AREA_SE_LAT_FIELD), v.getProperty(AREA_SE_LON_FIELD))));
		mb.setCameraPos(v.getProperty(CAMERA_POS_X_FIELD), v.getProperty(CAMERA_POS_Y_FIELD),
				v.getProperty(CAMERA_POS_Z_FIELD));
		mb.setCameraRot(v.getProperty(CAMERA_ROT_X_FIELD), v.getProperty(CAMERA_ROT_Y_FIELD),
				v.getProperty(CAMERA_ROT_Z_FIELD), v.getProperty(CAMERA_ROT_W_FIELD));
		return super.retrieve(db, o, go);
	}

	@Override
	public GameObject create() {
		return new GameMap();
	}
}
