/*
 * dwarfhustle-model-generate-map - Manages the compile dependencies for the model.
 * Copyright © 2022-2025 Erwin Müller (erwin.mueller@anrisoftware.com)
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
package com.anrisoftware.dwarfhustle.model.terrainimage;

import static com.anrisoftware.dwarfhustle.model.api.objects.MapChunk.cid2Id;
import static com.anrisoftware.dwarfhustle.model.api.objects.MapChunk.getChunk;
import static com.anrisoftware.dwarfhustle.model.api.objects.MapChunk.setChunk;
import static com.anrisoftware.dwarfhustle.model.db.buffers.MapChunkBuffer.findChild;
import static com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.DefaultLoadKnowledges.MATERIALS_GASES_NAME;
import static com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.DefaultLoadKnowledges.MATERIALS_LIQUIDS_NAME;
import static com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.DefaultLoadKnowledges.MATERIALS_SOLIDS_NAME;
import static com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.DefaultLoadKnowledges.MATERIAL_OXYGEN_NAME;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.eclipse.collections.api.factory.primitive.LongObjectMaps;
import org.eclipse.collections.api.map.primitive.MutableLongObjectMap;
import org.evrete.api.Knowledge;
import org.lable.oss.uniqueid.GeneratorException;

import com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos;
import com.anrisoftware.dwarfhustle.model.api.objects.GameChunkPos;
import com.anrisoftware.dwarfhustle.model.api.objects.GameMap;
import com.anrisoftware.dwarfhustle.model.api.objects.MapBlock;
import com.anrisoftware.dwarfhustle.model.api.objects.MapChunk;
import com.anrisoftware.dwarfhustle.model.api.objects.NeighboringDir;
import com.anrisoftware.dwarfhustle.model.api.objects.ObjectsGetter;
import com.anrisoftware.dwarfhustle.model.api.objects.ObjectsSetter;
import com.anrisoftware.dwarfhustle.model.db.api.MapChunksStorage;
import com.anrisoftware.dwarfhustle.model.db.buffers.MapBlockBuffer;
import com.anrisoftware.dwarfhustle.model.knowledge.evrete.BlockFact;
import com.anrisoftware.dwarfhustle.model.knowledge.evrete.TerrainKnowledge;
import com.google.inject.assistedinject.Assisted;

import groovy.lang.GroovyShell;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * Imports a map from an image file to the database.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
public class TerrainImageCreateMap {

    /**
     * @see TerrainImageCreateMap
     * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
     */
    public interface TerrainImageCreateMapFactory {

        TerrainImageCreateMap create(ObjectsGetter getter, ObjectsSetter setter, MapChunksStorage storage,
                TerrainKnowledge terrainKnowledge);
    }

    private static final Map<Integer, Integer> terrainImageMapping;

    static {
        terrainImageMapping = loadTerrainImageMapping();
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    private static Map<Integer, Integer> loadTerrainImageMapping() {
        var script = new GroovyShell()
                .parse(TerrainImageCreateMap.class.getResource("TerrainImageMapping.groovy").toURI());
        return (Map<Integer, Integer>) script.run();
    }

    @FunctionalInterface
    private interface UpdateTerrainBlock {
        void updateMaterialBlock(MapBlock mb, int x, int y, int z);
    }

    @Inject
    @Assisted
    private ObjectsGetter getter;

    @Inject
    @Assisted
    private MapChunksStorage storage;

    @Inject
    @Assisted
    private TerrainKnowledge terrainKnowledge;

    private AtomicInteger cidCounter;

    private int[][][] terrain;

    private MapChunk mcRoot;

    private GameMap gm;

    private int chunksCount;

    private int blocksCount;

    private int chunkSize;

    private AtomicInteger chunksDone;

    @Inject
    @Assisted
    private ObjectsSetter setter;

    public void startImport(URL url, TerrainLoadImage image, GameMap gm) throws IOException, GeneratorException {
        startImport0(url, image, gm, this::updateMaterialBlock);
    }

    public void startImportMapping(URL url, TerrainLoadImage image, GameMap gm) throws IOException, GeneratorException {
        startImport0(url, image, gm, this::updateMaterialBlockMapping);
    }

    private void startImport0(URL url, TerrainLoadImage image, GameMap gm, UpdateTerrainBlock updateTerrainBlock)
            throws IOException, GeneratorException {
        this.cidCounter = new AtomicInteger(0);
        this.terrain = image.load(url);
        this.chunkSize = image.chunkSize;
        this.mcRoot = new MapChunk(nextId(), 0, chunkSize, gm.getWidth(), gm.getHeight(),
                new GameChunkPos(0, 0, 0, gm.getWidth(), gm.getHeight(), gm.getDepth()));
        setChunk(storage, mcRoot);
        this.gm = gm;
        this.chunksCount = 0;
        this.blocksCount = 0;
        this.chunksDone = new AtomicInteger(0);
        this.chunksCount++;
        createMap(mcRoot, 0, 0, 0, gm.getWidth(), gm.getHeight(), gm.getDepth(), updateTerrainBlock);
        createNeighbors(mcRoot);
        updateTerrain();
        log.debug("startImport done chunks {} blocks {}", chunksCount, blocksCount);
        gm.setChunksCount(chunksCount);
    }

    @SneakyThrows
    private void updateTerrain() {
        var session = terrainKnowledge.createKnowledgeService();
        var terrainBlockMaterialRules = terrainKnowledge.createTerrainUpdateRulesKnowledge(session);
        var monitorExecutor = Executors.newSingleThreadScheduledExecutor();
        monitorExecutor.scheduleAtFixedRate(() -> {
            log.info("Chunks {}/{}", chunksDone, chunksCount);
        }, 3, 3, TimeUnit.SECONDS);
        try {
            updateTerrainParallel(terrainBlockMaterialRules);
        } finally {
            monitorExecutor.shutdown();
        }
    }

    @SneakyThrows
    private void updateTerrainParallel(Knowledge rules) {
        var pool = new ForkJoinPool(4);
        try {
            updateTerrainParallelAllChunks(rules, pool);
        } finally {
            pool.shutdown();
        }
    }

    @SneakyThrows
    private void updateTerrainParallelAllChunks(Knowledge rules, ForkJoinPool pool) {
        pool.submit(() -> IntStream.range(0, chunksCount).parallel().forEach((i) -> {
            var c = getChunk(getter, MapChunk.cid2Id(i));
            if (c.isLeaf()) {
                updateTerrainBlocks(rules, c);
                setChunk(storage, c);
            }
            this.chunksDone.incrementAndGet();
        })).get();
    }

    private void updateTerrainBlocks(Knowledge knowledge, MapChunk chunk) {
        var session = knowledge.newStatelessSession();
        var pos = chunk.getPos();
        for (int z = pos.z; z < pos.ep.z; z++) {
            for (int y = pos.y; y < pos.ep.y; y++) {
                for (int x = pos.x; x < pos.ep.x; x++) {
                    session.insert(new BlockFact(getter, setter, chunk, x, y, z, gm.getWidth(), gm.getHeight(),
                            gm.getDepth()));
                }
            }
        }
        session.fire();
    }

    private long nextId() {
        return MapChunk.cid2Id(cidCounter.getAndIncrement());
    }

    @SneakyThrows
    private void createMap(MapChunk chunk, int sx, int sy, int sz, int ex, int ey, int ez,
            UpdateTerrainBlock updateTerrainBlock) throws GeneratorException {
        MutableLongObjectMap<GameChunkPos> chunks = LongObjectMaps.mutable.empty();
        int cx = (ex - sx) / 2;
        int cy = (ey - sy) / 2;
        int cz = ez - sz > chunkSize ? (ez - sz) / 2 : ez - sz;
        for (int xx = sx; xx < ex; xx += cx) {
            for (int yy = sy; yy < ey; yy += cy) {
                for (int zz = sz; zz < ez; zz += cz) {
                    createChunk(chunk, chunks, xx, yy, zz, xx + cx, yy + cy, zz + cz, updateTerrainBlock);
                }
            }
        }
        chunk.setChunks(chunks);
        setChunk(storage, chunk);
    }

    @SneakyThrows
    private void createChunk(MapChunk parent, MutableLongObjectMap<GameChunkPos> chunks, int x, int y, int z, int ex,
            int ey, int ez, UpdateTerrainBlock updateTerrainBlock) throws GeneratorException {
        var chunk = new MapChunk(nextId(), parent.getCid(), chunkSize, gm.getWidth(), gm.getHeight(),
                new GameChunkPos(x, y, z, ex, ey, ez));
        storage.set(MapChunk.OBJECT_TYPE, chunk);
        chunksCount++;
        if (chunk.isLeaf()) {
            for (int xx = x; xx < ex; xx++) {
                for (int yy = y; yy < ey; yy++) {
                    for (int zz = z; zz < ez; zz++) {
                        blocksCount++;
                        var mb = new MapBlock(chunk.getCid(), new GameBlockPos(xx, yy, zz));
                        updateTerrainBlock.updateMaterialBlock(mb, xx, yy, zz);
                        final int off = GameChunkPos.calcIndex(chunk.pos.getSizeX(), chunk.pos.getSizeY(),
                                chunk.pos.getSizeZ(), chunk.pos.x, chunk.pos.y, chunk.pos.z, xx, yy, zz)
                                * MapBlockBuffer.SIZE;
                        MapBlockBuffer.write(chunk.getBlocks(), off, mb);
                    }
                }
            }
        } else {
            setChunk(storage, chunk);
            createMap(chunk, x, y, z, ex, ey, ez, updateTerrainBlock);
        }
        chunks.put(chunk.getCid(), chunk.getPos());
        setChunk(storage, chunk);
    }

    private void updateMaterialBlock(MapBlock mb, int x, int y, int z) {
        updateMaterialBlock0(mb, terrain[z][y][x], x, y, z);
    }

    private void updateMaterialBlockMapping(MapBlock mb, int x, int y, int z) {
        int t = terrainImageMapping.get(terrain[z][y][x]);
        updateMaterialBlock0(mb, t, x, y, z);
    }

    private void updateMaterialBlock0(MapBlock mb, int t, int x, int y, int z) {
        if (t == 0) {
            mb.material = terrainKnowledge.getMaterials(MATERIAL_OXYGEN_NAME).getFirst();
        } else {
            mb.material = t;
        }
        if (isMaterialGas(mb.material)) {
            mb.setEmpty(true);
        } else if (isMaterialLiquid(mb.material)) {
            mb.setLiquid(true);
        } else if (isMaterialSolid(mb.material)) {
            mb.setFilled(true);
        }
    }

    public boolean isMaterialGas(int material) {
        return terrainKnowledge.getMaterials(MATERIALS_GASES_NAME).contains(material);
    }

    public boolean isMaterialLiquid(int material) {
        return terrainKnowledge.getMaterials(MATERIALS_LIQUIDS_NAME).contains(material);
    }

    public boolean isMaterialSolid(int material) {
        return terrainKnowledge.getMaterials(MATERIALS_SOLIDS_NAME).contains(material);
    }

    private void createNeighbors(MapChunk rootc) {
        var pos = rootc.getPos();
        int xs = (pos.ep.x - pos.x) / 2;
        int ys = (pos.ep.y - pos.y) / 2;
        int zs = (pos.ep.z - pos.z) > gm.getChunkSize() ? (pos.ep.z - pos.z) / 2 : gm.getChunkSize();
        for (int x = pos.x; x < pos.ep.x; x += xs) {
            for (int y = pos.y; y < pos.ep.y; y += ys) {
                for (int z = pos.z; z < pos.ep.z; z += zs) {
                    long chunkid = rootc.getChunk(x, y, z, x + xs, y + ys, z + zs);
                    assert chunkid != 0;
                    MapChunk chunk = getter.get(MapChunk.OBJECT_TYPE, cid2Id(chunkid));
                    assert chunk != null;
                    if (xs > gm.getChunkSize() || ys > gm.getChunkSize() || zs > gm.getChunkSize()) {
                        createNeighbors(chunk);
                    }
                    long[] neighbors = new long[NeighboringDir.values().length];
                    long b = 0;
                    for (NeighboringDir dir : NeighboringDir.values()) {
                        var dp = (GameChunkPos) chunk.pos.add(dir.pos.mul(gm.getChunkSize()));
                        if ((b = findChild(mcRoot, dp.x, dp.y, dp.z, dp.ep.x, dp.ep.y, dp.ep.z, getter)) != 0) {
                            neighbors[dir.ordinal()] = b;
                        }
                    }
                    chunk.setNeighbors(neighbors);
                    setChunk(storage, chunk);
                }
            }
        }
    }
}
