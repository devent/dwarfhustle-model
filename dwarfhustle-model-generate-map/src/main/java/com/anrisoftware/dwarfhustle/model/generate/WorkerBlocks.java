/*
 * dwarfhustle-model-generate-map - Manages the compile dependencies for the model.
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
package com.anrisoftware.dwarfhustle.model.generate;

import java.util.Map;

import javax.inject.Inject;

import org.eclipse.collections.api.list.ListIterable;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.map.primitive.MutableObjectLongMap;
import org.eclipse.collections.impl.factory.Maps;
import org.eclipse.collections.impl.factory.primitive.ObjectLongMaps;
import org.lable.oss.uniqueid.GeneratorException;
import org.lable.oss.uniqueid.IDGenerator;

import com.anrisoftware.dwarfhustle.model.api.materials.Gas;
import com.anrisoftware.dwarfhustle.model.api.materials.IgneousIntrusive;
import com.anrisoftware.dwarfhustle.model.api.materials.Material;
import com.anrisoftware.dwarfhustle.model.api.materials.Sedimentary;
import com.anrisoftware.dwarfhustle.model.api.materials.Soil;
import com.anrisoftware.dwarfhustle.model.api.materials.SpecialStoneLayer;
import com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos;
import com.anrisoftware.dwarfhustle.model.api.objects.GameMap;
import com.anrisoftware.dwarfhustle.model.api.objects.GameMapPos;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObjectStorage;
import com.anrisoftware.dwarfhustle.model.api.objects.IdsObjectsProvider.IdsObjects;
import com.anrisoftware.dwarfhustle.model.api.objects.MapBlock;
import com.anrisoftware.dwarfhustle.model.api.objects.MapTile;
import com.anrisoftware.dwarfhustle.model.api.objects.WorldMap;
import com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.WorldMapSchema;
import com.google.inject.assistedinject.Assisted;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.intent.OIntentMassiveInsert;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
public class WorkerBlocks {

    /**
     * Factory to create {@link WorkerBlocks}.
     *
     * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
     */
    public interface WorkerBlocksFactory {

        WorkerBlocks create(OrientDB orientdb, Map<String, ListIterable<GameObject>> materials, Map<String, Object> p);
    }

    @Inject
    @Assisted
    private OrientDB orientdb;

    @Inject
    @Assisted
    private Map<String, Object> p;

    @Inject
    @IdsObjects
    private IDGenerator generator;

    private Map<String, Map<String, GameObject>> materials = Maps.mutable.empty();

    private int blocksDone;

    private boolean generateDone;

    private GameObjectStorage mapBlockStore;

    private GameObjectStorage gameMapStore;

    private GameObjectStorage worldMapStore;

    private boolean rootset;

    private int ground_level;

    private int magma_level;

    private int soil_level;

    private int sedimentary_level;

    private int igneous_level;

    private boolean cancelled = false;

    @Inject
    public void setMaterials(@Assisted Map<String, ListIterable<GameObject>> materials) {
        var mm = this.materials;
        materials.forEach((type, ms) -> {
            mm.put(type, Maps.mutable.empty());
            ms.forEach(go -> {
                mm.get(type).put(((Material) (go)).getName(), go);
            });
        });
    }

    @Inject
    public void setStorages(Map<String, GameObjectStorage> storages) {
        this.worldMapStore = storages.get(WorldMap.OBJECT_TYPE);
        this.gameMapStore = storages.get(GameMap.OBJECT_TYPE);
        this.mapBlockStore = storages.get(MapBlock.OBJECT_TYPE);
    }

    public int getBlocksDone() {
        return blocksDone;
    }

    public boolean isGenerateDone() {
        return generateDone;
    }

    public void cancel() {
        this.cancelled = true;
    }

    @SneakyThrows
    public void generate(GenerateMapMessage m) {
        log.debug("generate {}", m);
        this.blocksDone = 0;
        this.generateDone = false;
        this.rootset = false;
        int w1 = m.gameMap.getWidth();
        int h1 = m.gameMap.getHeight();
        int d1 = m.gameMap.getDepth();
        this.ground_level = Math.round((Float) p.get("ground_level_percent") * d1);
        this.soil_level = Math.round((Float) p.get("soil_level_percent") * d1) + ground_level;
        this.sedimentary_level = Math.round((Float) p.get("sedimentary_level_percent") * d1) + soil_level;
        this.igneous_level = Math.round((Float) p.get("igneous_level_percent") * d1) + sedimentary_level;
        this.magma_level = Math.round((Float) p.get("magma_level_percent") * d1 + magma_level) + igneous_level;
        var pos = pos(m, 0, 0, 0);
        var endPos = pos(m, w1, h1, d1);
        try (var db = orientdb.open(m.database, m.user, m.password)) {
            saveGameMap(m, db);
            db.declareIntent(new OIntentMassiveInsert());
            generateMapBlock(m, db, createBlocksMap(), pos, endPos);
            db.declareIntent(null);
        }
        this.generateDone = true;
        log.trace("generate done {}", m);
    }

    private void saveGameMap(GenerateMapMessage m, ODatabaseSession db) throws GeneratorException {
        var gmv = db.newVertex(GameMap.OBJECT_TYPE);
        gameMapStore.store(db, gmv, m.gameMap);
        gmv.save();
        var wmv = db.newVertex(WorldMap.OBJECT_TYPE);
        worldMapStore.store(db, wmv, m.gameMap.getWorld());
        wmv.save();
        var e = wmv.addEdge(gmv, WorldMapSchema.WORLD_CLASS);
        e.save();
    }

    private MapBlock generateMapBlock(GenerateMapMessage m, ODatabaseSession db,
            MutableObjectLongMap<GameBlockPos> parent, GameMapPos pos, GameMapPos endPos) throws GeneratorException {
        var w1 = endPos.getDiffX(pos);
        var h1 = endPos.getDiffY(pos);
        var d1 = endPos.getDiffZ(pos);
        var w2 = endPos.getDiffX(pos) / 2;
        var h2 = endPos.getDiffY(pos) / 2;
        var d2 = endPos.getDiffZ(pos) / 2;
        if (w2 == m.blockSize / 2) {
            var block = createBlock(m, db, pos, endPos);
            createMapTiles(db, block);
            saveBlock(db, block);
            blocksDone++;
            return block;
        }
        var block = createBlock(m, db, pos, endPos);
        if (!rootset) {
            block.setRoot(true);
            rootset = true;
        }
        parent.put(block.getPos(), block.getId());
        var map = createBlocksMap();
        var x = pos.getX();
        var y = pos.getY();
        var z = pos.getZ();
        int xw1 = x + w1;
        int yh1 = y + h1;
        int zd1 = z + d1;
        int xw2 = x + w2;
        int yh2 = y + h2;
        int zd2 = z + d2;
        var b0 = generateMapBlock(m, db, map, pos(m, x, y, z), pos(m, xw2, yh2, zd2));
        map.put(b0.getPos(), b0.getId());
        var b1 = generateMapBlock(m, db, map, pos(m, xw2, y, z), pos(m, xw1, yh2, zd2));
        map.put(b1.getPos(), b1.getId());
        var b2 = generateMapBlock(m, db, map, pos(m, x, yh2, z), pos(m, xw2, yh1, zd2));
        map.put(b2.getPos(), b2.getId());
        var b3 = generateMapBlock(m, db, map, pos(m, xw2, yh2, z), pos(m, xw1, yh1, zd2));
        map.put(b3.getPos(), b3.getId());
        //
        var b4 = generateMapBlock(m, db, map, pos(m, x, y, zd2), pos(m, xw2, yh2, zd1));
        map.put(b4.getPos(), b4.getId());
        var b5 = generateMapBlock(m, db, map, pos(m, xw2, y, zd2), pos(m, xw1, yh2, zd1));
        map.put(b5.getPos(), b5.getId());
        var b6 = generateMapBlock(m, db, map, pos(m, x, yh2, zd2), pos(m, xw2, yh1, zd1));
        map.put(b6.getPos(), b6.getId());
        var b7 = generateMapBlock(m, db, map, pos(m, xw2, yh2, zd2), pos(m, xw1, yh1, zd1));
        map.put(b7.getPos(), b7.getId());
        //
        block.setBlocks(map.asUnmodifiable());
        blocksDone++;
        saveBlock(db, block);
        return block;
    }

    private void saveBlock(ODatabaseSession db, MapBlock block) {
        var v = db.newVertex(MapBlock.OBJECT_TYPE);
        mapBlockStore.store(db, v, block);
        v.save();
    }

    private void createMapTiles(ODatabaseSession db, MapBlock block) throws GeneratorException {
        var w = block.getEndPos().getDiffX(block.getPos());
        var h = block.getEndPos().getDiffY(block.getPos());
        var d = block.getEndPos().getDiffZ(block.getPos());
        var mapid = block.getPos().getMapid();
        var tiles = createTilesMap(w * h * d);
        var ids = generator.batch(w * h * d);
        for (var z = 0; z < d; z++) {
            if (cancelled) {
                return;
            }
            for (var y = 0; y < h; y++) {
                for (var x = 0; x < w; x++) {
                    var xx = x + block.getPos().getX();
                    var yy = y + block.getPos().getY();
                    var zz = z + block.getPos().getZ();
                    var tile = new MapTile(ids.pop());
                    setMaterial(zz, tile);
                    tile.setPos(new GameMapPos(mapid, xx, yy, zz));
                    tiles.put(tile.getPos(), tile);
                }
            }
        }
        block.setTiles(tiles.asUnmodifiable());
    }

    private void setMaterial(int z, MapTile tile) {
        if (z <= ground_level) {
            tile.setMined(true);
            tile.setNaturalFloor(false);
            tile.setNaturalRoof(false);
            tile.setMaterial(materials.get(Gas.TYPE).get("OXYGEN").getId());
        } else {
            tile.setMined(false);
            tile.setNaturalFloor(true);
            tile.setNaturalRoof(true);
            if (z <= soil_level) {
                tile.setMaterial(materials.get(Soil.TYPE).get("LOAM").getId());
            } else if (z <= sedimentary_level) {
                tile.setMaterial(materials.get(Sedimentary.TYPE).get("SANDSTONE").getId());
            } else if (z <= igneous_level) {
                tile.setMaterial(materials.get(IgneousIntrusive.TYPE).get("GRANITE").getId());
            } else if (z <= magma_level) {
                tile.setMaterial(materials.get(SpecialStoneLayer.TYPE).get("MAGMA").getId());
            }
        }
    }

    private GameMapPos pos(GenerateMapMessage m, int x, int y, int z) {
        return new GameMapPos(m.gameMap.getMapid(), x, y, z);
    }

    private MutableObjectLongMap<GameBlockPos> createBlocksMap() {
        return ObjectLongMaps.mutable.ofInitialCapacity(8);
    }

    private MapBlock createBlock(GenerateMapMessage m, ODatabaseSession db, GameMapPos pos, GameMapPos endPos)
            throws GeneratorException {
        var block = new MapBlock(generator.generate());
        block.setPos(new GameBlockPos(pos, endPos));
        return block;
    }

    private MutableMap<GameMapPos, MapTile> createTilesMap(int n) {
        return Maps.mutable.ofInitialCapacity(n);
    }

}
