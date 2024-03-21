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

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.factory.primitive.ObjectLongMaps;
import org.eclipse.collections.api.list.ListIterable;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.primitive.MutableObjectLongMap;
import org.lable.oss.uniqueid.GeneratorException;
import org.lable.oss.uniqueid.IDGenerator;

import com.anrisoftware.dwarfhustle.model.api.materials.Gas;
import com.anrisoftware.dwarfhustle.model.api.materials.IgneousIntrusive;
import com.anrisoftware.dwarfhustle.model.api.materials.Material;
import com.anrisoftware.dwarfhustle.model.api.materials.Sedimentary;
import com.anrisoftware.dwarfhustle.model.api.materials.Soil;
import com.anrisoftware.dwarfhustle.model.api.materials.SpecialStoneLayer;
import com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos;
import com.anrisoftware.dwarfhustle.model.api.objects.GameChunkPos;
import com.anrisoftware.dwarfhustle.model.api.objects.GameMap;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObjectStorage;
import com.anrisoftware.dwarfhustle.model.api.objects.IdsObjectsProvider.IdsObjects;
import com.anrisoftware.dwarfhustle.model.api.objects.KnowledgeObject;
import com.anrisoftware.dwarfhustle.model.api.objects.MapBlock;
import com.anrisoftware.dwarfhustle.model.api.objects.MapChunk;
import com.anrisoftware.dwarfhustle.model.api.objects.WorldMap;
import com.google.inject.assistedinject.Assisted;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.intent.OIntentMassiveInsert;

import jakarta.inject.Inject;
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

        WorkerBlocks create(OrientDB orientdb, Map<String, ListIterable<KnowledgeObject>> materials,
                Map<String, Object> p);
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

    private GameObjectStorage mapChunkStore;

    private GameObjectStorage gameMapStore;

    private GameObjectStorage worldMapStore;

    private boolean rootset;

    private int ground_level;

    private int magma_level;

    private int soil_level;

    private int sedimentary_level;

    private int igneous_level;

    private boolean cancelled = false;

    private long rootid;

    private int chunkSize;

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
        this.mapChunkStore = storages.get(MapChunk.OBJECT_TYPE);
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
        this.chunkSize = m.gameMap.chunkSize;
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
            db.declareIntent(new OIntentMassiveInsert());
            generateMapBlock(m, db, createChunksMap(), pos, endPos);
            saveGameMap(m, db);
            db.declareIntent(null);
        }
        this.generateDone = true;
        log.trace("generate done {}", m);
    }

    private void saveGameMap(GenerateMapMessage m, ODatabaseSession db) throws GeneratorException {
        var gmv = db.newVertex(GameMap.OBJECT_TYPE);
        m.gameMap.root = rootid;
        gameMapStore.store(db, gmv, m.gameMap);
        gmv.save();
        var wmv = db.newVertex(WorldMap.OBJECT_TYPE);
        worldMapStore.store(db, wmv, m.worldMap);
        wmv.save();
    }

    private MapChunk generateMapBlock(GenerateMapMessage m, ODatabaseSession db,
            MutableObjectLongMap<GameChunkPos> parent, GameBlockPos pos, GameBlockPos endPos)
            throws GeneratorException {
        var w1 = endPos.getDiffX(pos);
        var h1 = endPos.getDiffY(pos);
        var d1 = endPos.getDiffZ(pos);
        var w2 = endPos.getDiffX(pos) / 2;
        var h2 = endPos.getDiffY(pos) / 2;
        var d2 = endPos.getDiffZ(pos) / 2;
        if (w2 == m.blockSize / 2) {
            var block = createChunk(m, db, pos, endPos);
            createMapBlocks(db, block);
            saveChunk(db, block);
            blocksDone++;
            return block;
        }
        var chunk = createChunk(m, db, pos, endPos);
        if (!rootset) {
            chunk.setRoot(true);
            rootid = chunk.getId();
            rootset = true;
        }
        parent.put(chunk.getPos(), chunk.getId());
        var map = createChunksMap();
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
        chunk.setChunks(map.asUnmodifiable());
        blocksDone++;
        saveChunk(db, chunk);
        return chunk;
    }

    private void saveChunk(ODatabaseSession db, MapChunk block) {
        var v = db.newVertex(MapChunk.OBJECT_TYPE);
        mapChunkStore.store(db, v, block);
        v.save();
    }

    private void createMapBlocks(ODatabaseSession db, MapChunk chunk) throws GeneratorException {
        var w = chunk.getPos().ep.getDiffX(chunk.getPos());
        var h = chunk.getPos().ep.getDiffY(chunk.getPos());
        var d = chunk.getPos().ep.getDiffZ(chunk.getPos());
        var blocks = createBlocksMap(w * h * d);
        var ids = generator.batch(w * h * d);
        for (var z = 0; z < d; z++) {
            if (cancelled) {
                return;
            }
            for (var y = 0; y < h; y++) {
                for (var x = 0; x < w; x++) {
                    var xx = x + chunk.getPos().getX();
                    var yy = y + chunk.getPos().getY();
                    var zz = z + chunk.getPos().getZ();
                    var block = new MapBlock(ids.pop());
                    setMaterial(zz, block);
                    block.setPos(new GameBlockPos(xx, yy, zz));
                    blocks.add(block);
                }
            }
        }
        chunk.setBlocks(blocks);
    }

    private void setMaterial(int z, MapBlock block) {
        if (z <= ground_level) {
            block.setMined(true);
            block.setNaturalFloor(false);
            block.setNaturalRoof(false);
            block.setMaterial(materials.get(Gas.TYPE).get("OXYGEN").getId());
        } else {
            block.setMined(false);
            block.setNaturalFloor(true);
            block.setNaturalRoof(true);
            if (z <= soil_level) {
                block.setMaterial(materials.get(Soil.TYPE).get("LOAM").getId());
            } else if (z <= sedimentary_level) {
                block.setMaterial(materials.get(Sedimentary.TYPE).get("SANDSTONE").getId());
            } else if (z <= igneous_level) {
                block.setMaterial(materials.get(IgneousIntrusive.TYPE).get("GRANITE").getId());
            } else if (z <= magma_level) {
                block.setMaterial(materials.get(SpecialStoneLayer.TYPE).get("MAGMA").getId());
            }
        }
    }

    private GameBlockPos pos(GenerateMapMessage m, int x, int y, int z) {
        return new GameBlockPos(x, y, z);
    }

    private MutableObjectLongMap<GameChunkPos> createChunksMap() {
        return ObjectLongMaps.mutable.ofInitialCapacity(8);
    }

    private MapChunk createChunk(GenerateMapMessage m, ODatabaseSession db, GameBlockPos pos, GameBlockPos endPos)
            throws GeneratorException {
        var block = new MapChunk(generator.generate(), chunkSize, new GameChunkPos(pos, endPos));
        return block;
    }

    private MutableList<MapBlock> createBlocksMap(int n) {
        return Lists.mutable.ofInitialCapacity(n);
    }

}
