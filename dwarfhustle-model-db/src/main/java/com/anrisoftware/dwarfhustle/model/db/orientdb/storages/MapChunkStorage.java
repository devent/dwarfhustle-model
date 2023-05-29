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

import javax.inject.Inject;

import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.map.primitive.MutableObjectLongMap;
import org.eclipse.collections.impl.factory.Maps;
import org.eclipse.collections.impl.factory.primitive.ObjectLongMaps;

import com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos;
import com.anrisoftware.dwarfhustle.model.api.objects.GameChunkPos;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObjectStorage;
import com.anrisoftware.dwarfhustle.model.api.objects.MapBlock;
import com.anrisoftware.dwarfhustle.model.api.objects.MapChunk;
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
    public void store(Object db, Object o, GameObject go) {
        var v = (OElement) o;
        var mb = (MapChunk) go;
        var odb = (ODatabaseDocument) db;
        Map<String, OElement> blocks = Maps.mutable.ofInitialCapacity(mb.getBlocks().size());
        mb.getChunks().forEachKeyValue((pos, id) -> {
            var blockid = odb.newElement(CHUNK_ID_CLASS);
            blockid.setProperty(OBJECTID_FIELD, id);
            blocks.put(pos.toSaveString(), blockid);
        });
        v.setProperty(BLOCKS_FIELD, blocks, OType.EMBEDDEDMAP);
        Map<String, OElement> tiles = Maps.mutable.ofInitialCapacity(mb.getBlocks().size());
        mb.getBlocks().forEachKeyValue((pos, tile) -> {
            var vtile = odb.newVertex(MapBlock.OBJECT_TYPE);
            mapTileStorage.store(db, vtile, tile);
            tiles.put(tile.getPos().toSaveString(), vtile);
        });
        v.setProperty(TILES_FIELD, tiles, OType.EMBEDDEDMAP);
        v.setProperty(MAPID_FIELD, mb.getPos().getMapid());
        v.setProperty(START_X_FIELD, mb.getPos().getX());
        v.setProperty(START_Y_FIELD, mb.getPos().getY());
        v.setProperty(START_Z_FIELD, mb.getPos().getZ());
        v.setProperty(END_X_FIELD, mb.getPos().getEp().getX());
        v.setProperty(END_Y_FIELD, mb.getPos().getEp().getY());
        v.setProperty(END_Z_FIELD, mb.getPos().getEp().getZ());
        v.setProperty(ROOT_FIELD, mb.isRoot());
        v.setProperty(NEIGHBOR_T_FIELD, mb.getNeighborTop());
        v.setProperty(NEIGHBOR_B_FIELD, mb.getNeighborBottom());
        v.setProperty(NEIGHBOR_S_FIELD, mb.getNeighborSouth());
        v.setProperty(NEIGHBOR_E_FIELD, mb.getNeighborEast());
        v.setProperty(NEIGHBOR_N_FIELD, mb.getNeighborNorth());
        v.setProperty(NEIGHBOR_W_FIELD, mb.getNeighborWest());
        v.setProperty(PARENT_FIELD, mb.getParent());
        super.store(db, o, go);
    }

    @Override
    public GameObject retrieve(Object db, Object o, GameObject go) {
        var v = (OElement) o;
        var mb = (MapChunk) go;
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
        mb.setChunks(blocks.asUnmodifiable());
        mb.setBlocks(tiles.asUnmodifiable());
        mb.setPos(new GameChunkPos(v.getProperty(MAPID_FIELD), v.getProperty(START_X_FIELD),
                v.getProperty(START_Y_FIELD), v.getProperty(START_Z_FIELD), v.getProperty(END_X_FIELD),
                v.getProperty(END_Y_FIELD), v.getProperty(END_Z_FIELD)));
        mb.setRoot(v.getProperty(ROOT_FIELD));
        mb.setNeighborTop(v.getProperty(NEIGHBOR_T_FIELD));
        mb.setNeighborBottom(v.getProperty(NEIGHBOR_B_FIELD));
        mb.setNeighborSouth(v.getProperty(NEIGHBOR_S_FIELD));
        mb.setNeighborEast(v.getProperty(NEIGHBOR_E_FIELD));
        mb.setNeighborNorth(v.getProperty(NEIGHBOR_N_FIELD));
        mb.setNeighborWest(v.getProperty(NEIGHBOR_W_FIELD));
        mb.setParent(v.getProperty(PARENT_FIELD));
        return super.retrieve(db, o, go);
    }

    @Override
    public GameObject create() {
        return new MapChunk();
    }
}
