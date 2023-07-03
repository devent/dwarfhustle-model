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

import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapObjectSchema.MAPID_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapObjectSchema.X_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapObjectSchema.Y_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.GameMapObjectSchema.Z_FIELD;

import com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos;
import com.anrisoftware.dwarfhustle.model.api.objects.GameMapObject;
import com.anrisoftware.dwarfhustle.model.api.objects.MapBlock;
import com.anrisoftware.dwarfhustle.model.api.objects.StoredObject;
import com.orientechnologies.orient.core.record.OElement;

/**
 * Stores and retrieves the properties of a {@link GameMapObject} to/from the
 * database. Does not commit the changes into the database.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public class AbstractGameMapObjectStorage extends AbstractGameObjectStorage {

    @Override
    public void store(Object db, Object o, StoredObject go) {
        var v = (OElement) o;
        var gmo = (GameMapObject) go;
        v.setProperty(MAPID_FIELD, gmo.pos.mapid);
        v.setProperty(X_FIELD, gmo.pos.x);
        v.setProperty(Y_FIELD, gmo.pos.y);
        v.setProperty(Z_FIELD, gmo.pos.z);
        super.store(db, o, go);
    }

    @Override
    public StoredObject retrieve(Object db, Object o, StoredObject go) {
        var v = (OElement) o;
        var gmo = (GameMapObject) go;
        gmo.pos = new GameBlockPos(v.getProperty(MAPID_FIELD), v.getProperty(X_FIELD), v.getProperty(Y_FIELD),
                v.getProperty(Z_FIELD));
        return super.retrieve(db, o, go);
    }

    @Override
    public StoredObject create() {
        return new MapBlock();
    }
}
