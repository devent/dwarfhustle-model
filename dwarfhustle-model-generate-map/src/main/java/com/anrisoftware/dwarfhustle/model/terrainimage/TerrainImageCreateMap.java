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
package com.anrisoftware.dwarfhustle.model.terrainimage;

import static com.anrisoftware.dwarfhustle.model.db.buffers.MapChunkBuffer.findChild;
import static com.anrisoftware.dwarfhustle.model.knowledge.evrete.TerrainKnowledge.MATERIALS_GASES_NAME;
import static com.anrisoftware.dwarfhustle.model.knowledge.evrete.TerrainKnowledge.MATERIALS_LIQUIDS_NAME;
import static com.anrisoftware.dwarfhustle.model.knowledge.evrete.TerrainKnowledge.MATERIALS_SOLIDS_NAME;
import static com.anrisoftware.dwarfhustle.model.knowledge.evrete.TerrainKnowledge.MATERIAL_OXYGEN_NAME;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.IntStream;

import org.eclipse.collections.api.factory.primitive.IntObjectMaps;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.evrete.api.Knowledge;
import org.lable.oss.uniqueid.GeneratorException;

import com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos;
import com.anrisoftware.dwarfhustle.model.api.objects.GameChunkPos;
import com.anrisoftware.dwarfhustle.model.api.objects.GameMap;
import com.anrisoftware.dwarfhustle.model.api.objects.MapBlock;
import com.anrisoftware.dwarfhustle.model.api.objects.MapChunk;
import com.anrisoftware.dwarfhustle.model.api.objects.NeighboringDir;
import com.anrisoftware.dwarfhustle.model.db.buffers.MapBlockBuffer;
import com.anrisoftware.dwarfhustle.model.db.lmbd.MapChunksLmbdStorage;
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

        TerrainImageCreateMap create(MapChunksLmbdStorage storage, TerrainKnowledge terrainKnowledge);
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
    private MapChunksLmbdStorage storage;

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

    private ScheduledExecutorService monitorExecutor;

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
        this.mcRoot = new MapChunk(nextCid(), 0, chunkSize, new GameChunkPos(0, 0, 0, gm.width, gm.height, gm.depth));
        putObjectToBackend(mcRoot);
        this.gm = gm;
        this.chunksCount = 0;
        this.blocksCount = 0;
        this.monitorExecutor = Executors.newSingleThreadScheduledExecutor();
        this.chunksDone = new AtomicInteger(0);
        this.chunksCount++;
        createMap(mcRoot, 0, 0, 0, gm.width, gm.height, gm.depth, updateTerrainBlock);
        createNeighbors(mcRoot);
        updateTerrain();
        log.debug("startImport done chunks {} blocks {}", chunksCount, blocksCount);
        gm.chunksCount = chunksCount;
        gm.blocksCount = blocksCount;
    }

    @SneakyThrows
    private void updateTerrain() {
        Function<Integer, MapChunk> retriever = storage::getChunk;
        var terrainBlockMaterialRules = terrainKnowledge.createTerrainUpdateRulesKnowledge();
        monitorExecutor.scheduleAtFixedRate(() -> {
            log.info("Chunks {}/{}", chunksDone, chunksCount);
        }, 3, 3, TimeUnit.SECONDS);
        var customThreadPool = new ForkJoinPool(4);
        customThreadPool.submit(() -> IntStream.range(0, chunksCount).parallel().forEach((i) -> {
            var c = storage.getChunk(i);
            if (c.isLeaf()) {
                updateTerrainBlocks(terrainBlockMaterialRules, c, retriever);
                storage.putChunk(c);
            }
            this.chunksDone.incrementAndGet();
        })).get();
        // for (int i = 0; i < chunksCount; i++) {
        // log.info("{}/{}", i, chunksCount);
        // }
        customThreadPool.shutdown();
        monitorExecutor.shutdown();
    }

    private void updateTerrainBlocks(Knowledge knowledge, MapChunk chunk, Function<Integer, MapChunk> retriever) {
        chunk.changed = true;
        var session = knowledge.newStatelessSession();
        var pos = chunk.getPos();
        for (int z = pos.z; z < pos.ep.z; z++) {
            for (int y = pos.y; y < pos.ep.y; y++) {
                for (int x = pos.x; x < pos.ep.x; x++) {
                    session.insert(new BlockFact(storage, chunk, x, y, z, gm.width, gm.height, gm.depth));
                }
            }
        }
        session.fire();
    }

    private int nextCid() {
        return cidCounter.getAndIncrement();
    }

    @SneakyThrows
    private void createMap(MapChunk chunk, int sx, int sy, int sz, int ex, int ey, int ez,
            UpdateTerrainBlock updateTerrainBlock) throws GeneratorException {
        MutableIntObjectMap<GameChunkPos> chunks = IntObjectMaps.mutable.empty();
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
        putObjectToBackend(chunk);
    }

    @SneakyThrows
    private void createChunk(MapChunk parent, MutableIntObjectMap<GameChunkPos> chunks, int x, int y, int z, int ex,
            int ey, int ez, UpdateTerrainBlock updateTerrainBlock) throws GeneratorException {
        var chunk = new MapChunk(nextCid(), parent.cid, chunkSize, new GameChunkPos(x, y, z, ex, ey, ez));
        putObjectToBackend(chunk);
        chunksCount++;
        if (chunk.isLeaf()) {
            for (int xx = x; xx < ex; xx++) {
                for (int yy = y; yy < ey; yy++) {
                    for (int zz = z; zz < ez; zz++) {
                        blocksCount++;
                        var mb = new MapBlock(chunk.cid, new GameBlockPos(xx, yy, zz));
                        updateTerrainBlock.updateMaterialBlock(mb, xx, yy, zz);
                        final int off = GameChunkPos.calcIndex(chunk.pos.getSizeX(), chunk.pos.getSizeY(),
                                chunk.pos.getSizeZ(), chunk.pos.x, chunk.pos.y, chunk.pos.z, xx, yy, zz)
                                * MapBlockBuffer.SIZE;
                        MapBlockBuffer.write(chunk.getBlocks(), off, mb);
                    }
                }
            }
        } else {
            putObjectToBackend(chunk);
            createMap(chunk, x, y, z, ex, ey, ez, updateTerrainBlock);
        }
        chunks.put(chunk.cid, chunk.getPos());
        putObjectToBackend(chunk);
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
        int zs = (pos.ep.z - pos.z) > gm.chunkSize ? (pos.ep.z - pos.z) / 2 : gm.chunkSize;
        Function<Integer, MapChunk> r = storage::getChunk;
        for (int x = pos.x; x < pos.ep.x; x += xs) {
            for (int y = pos.y; y < pos.ep.y; y += ys) {
                for (int z = pos.z; z < pos.ep.z; z += zs) {
                    int chunkid = rootc.getChunk(x, y, z, x + xs, y + ys, z + zs);
                    assert chunkid != 0;
                    var chunk = storage.getChunk(chunkid);
                    assert chunk != null;
                    if (xs > gm.chunkSize || ys > gm.chunkSize || zs > gm.chunkSize) {
                        createNeighbors(chunk);
                    }
                    int[] neighbors = new int[NeighboringDir.values().length];
                    int b = 0;
                    for (NeighboringDir dir : NeighboringDir.values()) {
                        var dp = (GameChunkPos) chunk.pos.add(dir.pos.mul(gm.chunkSize));
                        if ((b = findChild(mcRoot, dp.x, dp.y, dp.z, dp.ep.x, dp.ep.y, dp.ep.z, r)) != 0) {
                            neighbors[dir.ordinal()] = b;
                        }
                    }
                    chunk.setNeighbors(neighbors);
                    putObjectToBackend(chunk);
                }
            }
        }
    }

    @SneakyThrows
    private void putObjectsToBackend(Iterable<MapChunk> values) {
        storage.putChunks(values);
    }

    @SneakyThrows
    private void putObjectToBackend(MapChunk chunk) {
        storage.putChunk(chunk);
    }

}
