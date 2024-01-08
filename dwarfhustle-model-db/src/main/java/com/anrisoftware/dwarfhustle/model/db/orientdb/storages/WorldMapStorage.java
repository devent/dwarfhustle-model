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

import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.WorldMapSchema.CURRENT_MAP_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.WorldMapSchema.DIST_LAT_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.WorldMapSchema.DIST_LON_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.WorldMapSchema.NAME_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.WorldMapSchema.TIME_FIELD;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import com.anrisoftware.dwarfhustle.model.api.objects.GameMap;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObjectStorage;
import com.anrisoftware.dwarfhustle.model.api.objects.StoredObject;
import com.anrisoftware.dwarfhustle.model.api.objects.WorldMap;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.record.OElement;

import jakarta.inject.Inject;

/**
 * Stores and retrieves the properties of a {@link WorldMap} to/from the
 * database. Does not commit the changes into the database.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public class WorldMapStorage extends AbstractGameObjectStorage {

    private GameObjectStorage gameMapStorage;

    @Inject
    public void setStorages(Map<String, GameObjectStorage> storages) {
        this.gameMapStorage = storages.get(GameMap.OBJECT_TYPE);
    }

    @Override
    public void store(Object db, Object o, StoredObject go) {
        var v = (OElement) o;
        var wm = (WorldMap) go;
        v.setProperty(NAME_FIELD, wm.getName());
        v.setProperty(DIST_LAT_FIELD, wm.getDistanceLat());
        v.setProperty(DIST_LON_FIELD, wm.getDistanceLon());
        v.setProperty(TIME_FIELD, toString(wm.getTime()));
        v.setProperty(CURRENT_MAP_FIELD, wm.currentMap);
        super.store(db, o, go);
    }

    private String toString(LocalDateTime time) {
        return time.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    @Override
    public StoredObject retrieve(Object db, Object o, StoredObject go) {
        var v = (OElement) o;
        var wm = (WorldMap) go;
        var odb = (ODatabaseDocument) db;
        wm.setName(v.getProperty(NAME_FIELD));
        wm.setDistance(v.getProperty(DIST_LAT_FIELD), v.getProperty(DIST_LON_FIELD));
        wm.setTime(parseTime(v.getProperty(TIME_FIELD)));
        wm.currentMap = v.getProperty(CURRENT_MAP_FIELD);
        retrieveGameMaps(v, wm, odb);
        return super.retrieve(db, o, go);
    }

    private void retrieveGameMaps(OElement v, WorldMap wm, ODatabaseDocument db) {
        var query = "select expand(out()) from ?";
        var rs = db.query(query, WorldMap.OBJECT_TYPE);
        try {
            while (rs.hasNext()) {
                var item = rs.next().getVertex();
                if (item.isPresent()) {
                    var gm = gameMapStorage.retrieve(db, item.get(), gameMapStorage.create());
                    wm.addMap((GameMap) gm);
                }
            }
        } finally {
            rs.close();
        }
    }

    private LocalDateTime parseTime(String s) {
        return LocalDateTime.parse(s, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    @Override
    public StoredObject create() {
        return new WorldMap();
    }
}
