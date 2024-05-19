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

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.eclipse.collections.api.factory.primitive.IntObjectMaps;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.evrete.api.StatefulSession;
import org.lable.oss.uniqueid.GeneratorException;

import com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos;
import com.anrisoftware.dwarfhustle.model.api.objects.GameChunkPos;
import com.anrisoftware.dwarfhustle.model.api.objects.GameMap;
import com.anrisoftware.dwarfhustle.model.api.objects.MapBlock;
import com.anrisoftware.dwarfhustle.model.api.objects.MapChunk;
import com.anrisoftware.dwarfhustle.model.api.objects.MapChunksStore;
import com.anrisoftware.dwarfhustle.model.api.objects.NeighboringDir;
import com.anrisoftware.dwarfhustle.model.knowledge.evrete.TerrainFact;
import com.google.inject.assistedinject.Assisted;

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

        TerrainImageCreateMap create(MapChunksStore store);
    }

    @Inject
    @Assisted
    private MapChunksStore store;

    private AtomicInteger cidCounter;

    private int[][][] terrain;

    private MapChunk mcRoot;

    private GameMap gm;

    private int chunksCount;

    private int blocksCount;

    private int chunkSize;

    private StatefulSession knowledge;

    public void startImport(URL url, TerrainLoadImage image, GameMap gm, StatefulSession knowledge)
            throws IOException, GeneratorException {
        this.cidCounter = new AtomicInteger(0);
        this.terrain = image.load(url);
        this.chunkSize = image.chunkSize;
        this.knowledge = knowledge;
        this.mcRoot = new MapChunk(nextCid(), 0, chunkSize, new GameChunkPos(0, 0, 0, gm.width, gm.height, gm.depth));
        putObjectToBackend(mcRoot);
        this.gm = gm;
        this.chunksCount = 0;
        this.blocksCount = 0;
        chunksCount++;
        createMap(mcRoot, 0, 0, 0, gm.width, gm.height, gm.depth);
        createNeighbors(mcRoot);
        updateBlocks();
        log.debug("startImport chunks {} blocks {}", chunksCount, blocksCount);
        gm.chunksCount = chunksCount;
        gm.blocksCount = blocksCount;
    }

    private void updateBlocks() {
        Function<Integer, MapChunk> retriever = store::getChunk;
        store.forEachValue((c) -> {
            if (c.isBlocksNotEmpty()) {
                updateBlocks(c, retriever);
            }
        });
    }

    private void updateBlocks(MapChunk chunk, Function<Integer, MapChunk> retriever) {
        chunk.forEachBlocks((b) -> {
            var n = new MapBlock[NeighboringDir.values().length];
            for (var dir : NeighboringDir.values()) {
                n[dir.ordinal()] = b.getNeighbor(dir, chunk, retriever);
            }
            var fact = new TerrainFact(terrain[b.pos.z][b.pos.y][b.pos.x], b, n);
            knowledge.insert(fact);
        });
        knowledge.fire();
        knowledge.forEachFact(TerrainFact.class, (fact) -> {
            chunk.setBlock(fact.block);
        });
        knowledge.clear();
    }

    private int nextCid() {
        return cidCounter.getAndIncrement();
    }

    @SneakyThrows
    private void createMap(MapChunk chunk, int sx, int sy, int sz, int ex, int ey, int ez) throws GeneratorException {
        MutableIntObjectMap<GameChunkPos> chunks = IntObjectMaps.mutable.empty();
        int cx = (ex - sx) / 2;
        int cy = (ey - sy) / 2;
        int cz = ez - sz > chunkSize ? (ez - sz) / 2 : ez - sz;
        for (int xx = sx; xx < ex; xx += cx) {
            for (int yy = sy; yy < ey; yy += cy) {
                for (int zz = sz; zz < ez; zz += cz) {
                    createChunk(chunk, chunks, xx, yy, zz, xx + cx, yy + cy, zz + cz);
                }
            }
        }
        chunk.setChunks(chunks);
        putObjectToBackend(chunk);
    }

    @SneakyThrows
    private void createChunk(MapChunk parent, MutableIntObjectMap<GameChunkPos> chunks, int x, int y, int z, int ex,
            int ey, int ez) throws GeneratorException {
        var chunk = new MapChunk(nextCid(), parent.cid, chunkSize, new GameChunkPos(x, y, z, ex, ey, ez));
        chunksCount++;
        if (chunk.isLeaf()) {
            var bbuffer = store.getBlocksBuffer(chunk);
            chunk.setBlocksBuffer(bbuffer);
            for (int xx = x; xx < ex; xx++) {
                for (int yy = y; yy < ey; yy++) {
                    for (int zz = z; zz < ez; zz++) {
                        blocksCount++;
                        var mb = new MapBlock(chunk.cid, new GameBlockPos(xx, yy, zz));
                        chunk.setBlock(mb);
                    }
                }
            }
        } else {
            putObjectToBackend(chunk);
            createMap(chunk, x, y, z, ex, ey, ez);
        }
        chunks.put(chunk.cid, chunk.getPos());
        putObjectToBackend(chunk);
    }

    private void createNeighbors(MapChunk rootc) {
        var pos = rootc.getPos();
        int xs = (pos.ep.x - pos.x) / 2;
        int ys = (pos.ep.y - pos.y) / 2;
        int zs = (pos.ep.z - pos.z) > gm.chunkSize ? (pos.ep.z - pos.z) / 2 : gm.chunkSize;
        Function<Integer, MapChunk> r = store::getChunk;
        for (int x = pos.x; x < pos.ep.x; x += xs) {
            for (int y = pos.y; y < pos.ep.y; y += ys) {
                for (int z = pos.z; z < pos.ep.z; z += zs) {
                    int chunkid = rootc.getChunk(x, y, z, x + xs, y + ys, z + zs);
                    assert chunkid != 0;
                    var chunk = store.getChunk(chunkid);
                    assert chunk != null;
                    if (xs > gm.chunkSize || ys > gm.chunkSize || zs > gm.chunkSize) {
                        createNeighbors(chunk);
                    }
                    int[] neighbors = new int[NeighboringDir.values().length];
                    int b = 0;
                    for (NeighboringDir dir : NeighboringDir.values()) {
                        var dp = (GameChunkPos) chunk.pos.add(dir.pos.mul(gm.chunkSize));
                        if ((b = mcRoot.findChild(dp.x, dp.y, dp.z, dp.ep.x, dp.ep.y, dp.ep.z, r)) != 0) {
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
        store.setChunks(values);
    }

    @SneakyThrows
    private void putObjectToBackend(MapChunk chunk) {
        store.setChunk(chunk);
    }

}
