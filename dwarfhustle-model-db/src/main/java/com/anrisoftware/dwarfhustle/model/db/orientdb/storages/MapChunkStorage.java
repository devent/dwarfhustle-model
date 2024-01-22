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

import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapChunkSchema.BLOCKS_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapChunkSchema.BLOCK_ID_CLASS;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapChunkSchema.CHUNKS_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapChunkSchema.CHUNK_ID_CLASS;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapChunkSchema.MAP_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapChunkSchema.OBJECTID_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapChunkSchema.PARENT_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapChunkSchema.POS_END_X_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapChunkSchema.POS_END_Y_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapChunkSchema.POS_END_Z_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapChunkSchema.POS_START_X_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapChunkSchema.POS_START_Y_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapChunkSchema.POS_START_Z_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapChunkSchema.ROOT_FIELD;

import java.util.Map;

import org.eclipse.collections.api.map.primitive.MutableObjectLongMap;
import org.eclipse.collections.impl.factory.primitive.ObjectLongMaps;

import com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos;
import com.anrisoftware.dwarfhustle.model.api.objects.GameChunkPos;
import com.anrisoftware.dwarfhustle.model.api.objects.MapChunk;
import com.anrisoftware.dwarfhustle.model.api.objects.NeighboringDir;
import com.anrisoftware.dwarfhustle.model.api.objects.StoredObject;
import com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapChunkSchema;
import com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.NeighboringSchema;
import com.orientechnologies.orient.core.record.OElement;

/**
 * Stores and retrieves the properties of a {@link MapChunk} to/from the
 * database. Does not commit the changes into the database.
 *
 * @see MapChunk
 * @see MapChunkSchema
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public class MapChunkStorage extends AbstractGameObjectStorage {

    @Override
    public void store(Object db, Object o, StoredObject go) {
        var v = (OElement) o;
        var mc = (MapChunk) go;
        storePosIdsMap(v, mc, mc.getChunks(), CHUNK_ID_CLASS, CHUNKS_FIELD, OBJECTID_FIELD);
        storePosIdsMap(v, mc, mc.getBlocks(), BLOCK_ID_CLASS, BLOCKS_FIELD, OBJECTID_FIELD);
        v.setProperty(MAP_FIELD, mc.map);
        v.setProperty(POS_START_X_FIELD, mc.pos.x);
        v.setProperty(POS_START_Y_FIELD, mc.pos.y);
        v.setProperty(POS_START_Z_FIELD, mc.pos.z);
        v.setProperty(POS_END_X_FIELD, mc.pos.ep.getX());
        v.setProperty(POS_END_Y_FIELD, mc.pos.ep.getY());
        v.setProperty(POS_END_Z_FIELD, mc.pos.ep.getZ());
        v.setProperty(ROOT_FIELD, mc.isRoot());
        for (var n : NeighboringDir.values()) {
            v.setProperty(NeighboringSchema.getName(n), mc.chunkDir.get(n.ordinal()));
        }
        v.setProperty(PARENT_FIELD, mc.getParent());
        super.store(db, o, go);
    }

    @Override
    public StoredObject retrieve(Object db, Object o, StoredObject go) {
        var v = (OElement) o;
        var mc = (MapChunk) go;
        retrieveChunks(v, mc);
        retrieveBlocks(db, v, mc);
        mc.map = v.getProperty(MAP_FIELD);
        mc.setPos(new GameChunkPos(v.getProperty(POS_START_X_FIELD), v.getProperty(POS_START_Y_FIELD),
                v.getProperty(POS_START_Z_FIELD), v.getProperty(POS_END_X_FIELD), v.getProperty(POS_END_Y_FIELD),
                v.getProperty(POS_END_Z_FIELD)));
        mc.setRoot(v.getProperty(ROOT_FIELD));
        for (var n : NeighboringDir.values()) {
            mc.setNeighbor(n, v.getProperty(NeighboringSchema.getName(n)));
        }
        mc.setParent(v.getProperty(PARENT_FIELD));
        return super.retrieve(db, o, go);
    }

    private void retrieveBlocks(Object db, OElement v, MapChunk mc) {
        Map<String, OElement> omap = v.getProperty(BLOCKS_FIELD);
        MutableObjectLongMap<GameBlockPos> ids = ObjectLongMaps.mutable.ofInitialCapacity(omap.size());
        for (Map.Entry<String, OElement> entry : omap.entrySet()) {
            var pos = GameBlockPos.parse(entry.getKey());
            ids.put(pos, entry.getValue().getProperty(OBJECTID_FIELD));
        }
        mc.setBlocks(ids.asUnmodifiable());
    }

    private void retrieveChunks(OElement v, MapChunk mc) {
        Map<String, OElement> omap = v.getProperty(CHUNKS_FIELD);
        MutableObjectLongMap<GameChunkPos> ids = ObjectLongMaps.mutable.ofInitialCapacity(omap.size());
        for (Map.Entry<String, OElement> entry : omap.entrySet()) {
            var pos = GameChunkPos.parse(entry.getKey());
            ids.put(pos, entry.getValue().getProperty(OBJECTID_FIELD));
        }
        mc.setChunks(ids.asUnmodifiable());
    }

    @Override
    public StoredObject create() {
        return new MapChunk();
    }
}
