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
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapChunkSchema.CHUNK_ID_CLASS;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapChunkSchema.END_X_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapChunkSchema.END_Y_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapChunkSchema.END_Z_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapChunkSchema.MAPID_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapChunkSchema.NEIGHBOR_B_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapChunkSchema.NEIGHBOR_E_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapChunkSchema.NEIGHBOR_N_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapChunkSchema.NEIGHBOR_S_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapChunkSchema.NEIGHBOR_T_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapChunkSchema.NEIGHBOR_W_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapChunkSchema.OBJECTID_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapChunkSchema.PARENT_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapChunkSchema.ROOT_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapChunkSchema.START_X_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapChunkSchema.START_Y_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapChunkSchema.START_Z_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapChunkSchema.TILES_FIELD;

import java.util.Map;

import jakarta.inject.Inject;

import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.map.primitive.MutableObjectLongMap;
import org.eclipse.collections.impl.factory.Maps;
import org.eclipse.collections.impl.factory.primitive.ObjectLongMaps;

import com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos;
import com.anrisoftware.dwarfhustle.model.api.objects.GameChunkPos;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObjectStorage;
import com.anrisoftware.dwarfhustle.model.api.objects.MapBlock;
import com.anrisoftware.dwarfhustle.model.api.objects.MapChunk;
import com.anrisoftware.dwarfhustle.model.api.objects.StoredObject;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.OElement;

/**
 * Stores and retrieves the properties of a {@link MapChunk} to/from the
 * database. Does not commit the changes into the database.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public class MapChunkStorage extends AbstractGameObjectStorage {

    private GameObjectStorage mapTileStorage;

    @Inject
    public void setStorages(Map<String, GameObjectStorage> storages) {
        this.mapTileStorage = storages.get(MapBlock.OBJECT_TYPE);
    }

    @Override
    public void store(Object db, Object o, StoredObject go) {
        var v = (OElement) o;
        var mc = (MapChunk) go;
        var odb = (ODatabaseDocument) db;
        Map<String, OElement> blocks = Maps.mutable.ofInitialCapacity(mc.getBlocks().size());
        mc.getChunks().forEachKeyValue((pos, id) -> {
            var blockid = odb.newElement(CHUNK_ID_CLASS);
            blockid.setProperty(OBJECTID_FIELD, id);
            blocks.put(pos.toSaveString(), blockid);
        });
        v.setProperty(BLOCKS_FIELD, blocks, OType.EMBEDDEDMAP);
        Map<String, OElement> tiles = Maps.mutable.ofInitialCapacity(mc.getBlocks().size());
        mc.getBlocks().forEachKeyValue((pos, tile) -> {
            var vtile = odb.newVertex(MapBlock.OBJECT_TYPE);
            mapTileStorage.store(db, vtile, tile);
            tiles.put(tile.getPos().toSaveString(), vtile);
        });
        v.setProperty(TILES_FIELD, tiles, OType.EMBEDDEDMAP);
        v.setProperty(MAPID_FIELD, mc.getPos().getMapid());
        v.setProperty(START_X_FIELD, mc.getPos().getX());
        v.setProperty(START_Y_FIELD, mc.getPos().getY());
        v.setProperty(START_Z_FIELD, mc.getPos().getZ());
        v.setProperty(END_X_FIELD, mc.getPos().getEp().getX());
        v.setProperty(END_Y_FIELD, mc.getPos().getEp().getY());
        v.setProperty(END_Z_FIELD, mc.getPos().getEp().getZ());
        v.setProperty(ROOT_FIELD, mc.isRoot());
        v.setProperty(NEIGHBOR_T_FIELD, mc.getNeighborTop());
        v.setProperty(NEIGHBOR_B_FIELD, mc.getNeighborBottom());
        v.setProperty(NEIGHBOR_S_FIELD, mc.getNeighborSouth());
        v.setProperty(NEIGHBOR_E_FIELD, mc.getNeighborEast());
        v.setProperty(NEIGHBOR_N_FIELD, mc.getNeighborNorth());
        v.setProperty(NEIGHBOR_W_FIELD, mc.getNeighborWest());
        v.setProperty(PARENT_FIELD, mc.getParent());
        super.store(db, o, go);
    }

    @Override
    public StoredObject retrieve(Object db, Object o, StoredObject go) {
        var v = (OElement) o;
        var mc = (MapChunk) go;
        Map<String, OElement> oblocks = v.getProperty(BLOCKS_FIELD);
        MutableObjectLongMap<GameChunkPos> blocks = ObjectLongMaps.mutable.ofInitialCapacity(oblocks.size());
        for (Map.Entry<String, OElement> oblock : oblocks.entrySet()) {
            var pos = GameChunkPos.parse(oblock.getKey());
            long id = oblock.getValue().getProperty(OBJECTID_FIELD);
            blocks.put(pos, id);
        }
        Map<String, OElement> otiles = v.getProperty(TILES_FIELD);
        MutableMap<GameBlockPos, MapBlock> tiles = Maps.mutable.ofInitialCapacity(otiles.size());
        for (Map.Entry<String, OElement> otile : otiles.entrySet()) {
            var pos = GameBlockPos.parse(otile.getKey());
            var tile = (MapBlock) mapTileStorage.retrieve(db, otile.getValue(), mapTileStorage.create());
            tiles.put(pos, tile);
        }
        mc.setChunks(blocks.asUnmodifiable());
        mc.setBlocks(tiles.asUnmodifiable());
        mc.setPos(new GameChunkPos(v.getProperty(MAPID_FIELD), v.getProperty(START_X_FIELD),
                v.getProperty(START_Y_FIELD), v.getProperty(START_Z_FIELD), v.getProperty(END_X_FIELD),
                v.getProperty(END_Y_FIELD), v.getProperty(END_Z_FIELD)));
        mc.setRoot(v.getProperty(ROOT_FIELD));
        mc.setNeighborTop(v.getProperty(NEIGHBOR_T_FIELD));
        mc.setNeighborBottom(v.getProperty(NEIGHBOR_B_FIELD));
        mc.setNeighborSouth(v.getProperty(NEIGHBOR_S_FIELD));
        mc.setNeighborEast(v.getProperty(NEIGHBOR_E_FIELD));
        mc.setNeighborNorth(v.getProperty(NEIGHBOR_N_FIELD));
        mc.setNeighborWest(v.getProperty(NEIGHBOR_W_FIELD));
        mc.setParent(v.getProperty(PARENT_FIELD));
        return super.retrieve(db, o, go);
    }

    @Override
    public StoredObject create() {
        return new MapChunk();
    }
}
