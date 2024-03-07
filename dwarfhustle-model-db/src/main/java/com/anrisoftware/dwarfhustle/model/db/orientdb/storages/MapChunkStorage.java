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

import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.CenterExtentSchema.CENTER_X_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.CenterExtentSchema.CENTER_Y_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.CenterExtentSchema.CENTER_Z_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.CenterExtentSchema.EXTENT_X_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.CenterExtentSchema.EXTENT_Y_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.CenterExtentSchema.EXTENT_Z_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapChunkSchema.BLOCKS_EMPTY_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapChunkSchema.BLOCKS_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapChunkSchema.CHUNKS_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapChunkSchema.CHUNK_ID_CLASS;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapChunkSchema.CHUNK_SIZE_FIELD;
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

import com.anrisoftware.dwarfhustle.model.api.objects.CenterExtent;
import com.anrisoftware.dwarfhustle.model.api.objects.GameChunkPos;
import com.anrisoftware.dwarfhustle.model.api.objects.MapBlocksStore;
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
        v.setProperty(CHUNK_SIZE_FIELD, mc.chunkSize);
        v.setProperty(BLOCKS_EMPTY_FIELD, mc.blocks.isEmpty());
        if (!mc.blocks.isEmpty()) {
            v.setProperty(BLOCKS_FIELD, mc.blocks.getData());
        }
        v.setProperty(MAP_FIELD, mc.map);
        v.setProperty(POS_START_X_FIELD, mc.pos.x);
        v.setProperty(POS_START_Y_FIELD, mc.pos.y);
        v.setProperty(POS_START_Z_FIELD, mc.pos.z);
        v.setProperty(POS_END_X_FIELD, mc.getPos().ep.x);
        v.setProperty(POS_END_Y_FIELD, mc.getPos().ep.y);
        v.setProperty(POS_END_Z_FIELD, mc.getPos().ep.z);
        v.setProperty(ROOT_FIELD, mc.isRoot());
        for (var n : NeighboringDir.values()) {
            v.setProperty(NeighboringSchema.getName(n), mc.dir.get(n.ordinal()));
        }
        v.setProperty(PARENT_FIELD, mc.getParent());
        v.setProperty(CENTER_X_FIELD, mc.centerExtent.centerx);
        v.setProperty(CENTER_Y_FIELD, mc.centerExtent.centery);
        v.setProperty(CENTER_Z_FIELD, mc.centerExtent.centerz);
        v.setProperty(EXTENT_X_FIELD, mc.centerExtent.extentx);
        v.setProperty(EXTENT_Y_FIELD, mc.centerExtent.extenty);
        v.setProperty(EXTENT_Z_FIELD, mc.centerExtent.extentz);
        super.store(db, o, go);
    }

    @Override
    public StoredObject retrieve(Object db, Object o, StoredObject go) {
        var v = (OElement) o;
        var mc = (MapChunk) go;
        retrieveChunks(v, mc);
        mc.chunkSize = v.getProperty(CHUNK_SIZE_FIELD);
        mc.blocks = new MapBlocksStore(mc.chunkSize);
        boolean blocksEmpty = v.getProperty(BLOCKS_EMPTY_FIELD);
        if (!blocksEmpty) {
            mc.blocks.setData(v.getProperty(BLOCKS_FIELD));
        }
        mc.map = v.getProperty(MAP_FIELD);
        mc.setPos(GameChunkPos.builder().sx(v.getProperty(POS_START_X_FIELD)).sy(v.getProperty(POS_START_Y_FIELD))
                .sz(v.getProperty(POS_START_Z_FIELD)).ex(v.getProperty(POS_END_X_FIELD))
                .ey(v.getProperty(POS_END_Y_FIELD)).ez(v.getProperty(POS_END_Z_FIELD)).build());
        mc.setRoot(v.getProperty(ROOT_FIELD));
        for (var n : NeighboringDir.values()) {
            mc.setNeighbor(n, v.getProperty(NeighboringSchema.getName(n)));
        }
        mc.setParent(v.getProperty(PARENT_FIELD));
        mc.centerExtent = new CenterExtent(v.getProperty(CENTER_X_FIELD), v.getProperty(CENTER_Y_FIELD),
                v.getProperty(CENTER_Z_FIELD), v.getProperty(EXTENT_X_FIELD), v.getProperty(EXTENT_Y_FIELD),
                v.getProperty(EXTENT_Z_FIELD));
        return super.retrieve(db, o, go);
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
