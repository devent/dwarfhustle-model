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

import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapBlockSchema.MATERIAL_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapBlockSchema.NEIGHBOR_B_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapBlockSchema.NEIGHBOR_E_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapBlockSchema.NEIGHBOR_N_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapBlockSchema.NEIGHBOR_S_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapBlockSchema.NEIGHBOR_T_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapBlockSchema.NEIGHBOR_W_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapBlockSchema.OBJECT_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapBlockSchema.PROPERTIES_FIELD;

import com.anrisoftware.dwarfhustle.model.api.objects.MapBlock;
import com.anrisoftware.dwarfhustle.model.api.objects.PropertiesSet;
import com.anrisoftware.dwarfhustle.model.api.objects.StoredObject;
import com.orientechnologies.orient.core.record.OElement;

/**
 * Saves and loads the attributes of a {@link MapBlock} from the database.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public class MapBlockStorage extends AbstractGameMapObjectStorage {

    @Override
    public void store(Object db, Object o, StoredObject go) {
        var v = (OElement) o;
        var mb = (MapBlock) go;
        v.setProperty(MATERIAL_FIELD, mb.material);
        v.setProperty(OBJECT_FIELD, mb.object);
        v.setProperty(PROPERTIES_FIELD, mb.p.bits);
        v.setProperty(NEIGHBOR_T_FIELD, mb.getNeighborTop());
        v.setProperty(NEIGHBOR_B_FIELD, mb.getNeighborBottom());
        v.setProperty(NEIGHBOR_S_FIELD, mb.getNeighborSouth());
        v.setProperty(NEIGHBOR_E_FIELD, mb.getNeighborEast());
        v.setProperty(NEIGHBOR_N_FIELD, mb.getNeighborNorth());
        v.setProperty(NEIGHBOR_W_FIELD, mb.getNeighborWest());
        super.store(db, o, go);
    }

    @Override
    public StoredObject retrieve(Object db, Object o, StoredObject go) {
        var v = (OElement) o;
        var mb = (MapBlock) go;
        mb.material = v.getProperty(MATERIAL_FIELD);
        mb.object = v.getProperty(OBJECT_FIELD);
        mb.p = new PropertiesSet(v.getProperty(PROPERTIES_FIELD));
        mb.setNeighborTop(v.getProperty(NEIGHBOR_T_FIELD));
        mb.setNeighborBottom(v.getProperty(NEIGHBOR_B_FIELD));
        mb.setNeighborSouth(v.getProperty(NEIGHBOR_S_FIELD));
        mb.setNeighborEast(v.getProperty(NEIGHBOR_E_FIELD));
        mb.setNeighborNorth(v.getProperty(NEIGHBOR_N_FIELD));
        mb.setNeighborWest(v.getProperty(NEIGHBOR_W_FIELD));
        return super.retrieve(db, o, go);
    }

    @Override
    public StoredObject create() {
        return new MapBlock();
    }
}
