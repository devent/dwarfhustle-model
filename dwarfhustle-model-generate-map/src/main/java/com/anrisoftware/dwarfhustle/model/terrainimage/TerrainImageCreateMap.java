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

import static com.anrisoftware.dwarfhustle.model.api.objects.MapChunk.id2Cid;
import static java.time.Duration.ofSeconds;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.primitive.LongObjectMaps;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.primitive.MutableLongObjectMap;
import org.lable.oss.uniqueid.GeneratorException;

import com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos;
import com.anrisoftware.dwarfhustle.model.api.objects.GameChunkPos;
import com.anrisoftware.dwarfhustle.model.api.objects.GameMap;
import com.anrisoftware.dwarfhustle.model.api.objects.MapBlock;
import com.anrisoftware.dwarfhustle.model.api.objects.MapChunk;
import com.anrisoftware.dwarfhustle.model.api.objects.MapChunksStore;
import com.anrisoftware.dwarfhustle.model.api.objects.NeighboringDir;
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

    private static final Duration CACHE_TIMEOUT = ofSeconds(100);

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

    private AtomicLong cidCounter;

    private long[][][] terrain;

    private MapChunk mcRoot;

    private GameMap gm;

    private int chunksCount;

    private int blocksCount;

    private int chunkSize;

    private BufferedWriter out;

    public void startImport(URL url, TerrainLoadImage image, GameMap gm) throws IOException, GeneratorException {
        out = Files.newBufferedWriter(Path.of("chunks.txt"), StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
        this.cidCounter = new AtomicLong(0);
        this.terrain = image.load(url);
        this.chunkSize = image.chunkSize;
        this.mcRoot = new MapChunk(nextCid(), chunkSize, new GameChunkPos(0, 0, 0, gm.width, gm.height, gm.depth));
        this.mcRoot.updateCenterExtent(gm.width, gm.height, gm.depth);
        putObjectToBackend(mcRoot);
        this.gm = gm;
        this.chunksCount = 0;
        this.blocksCount = 0;
        chunksCount++;
        createMap(mcRoot, 0, 0, 0, gm.width, gm.height, gm.depth);
        System.out.println("chunks " + chunksCount + " blocks " + blocksCount);
        createNeighbors(mcRoot);
        gm.chunksCount = chunksCount;
        gm.blocksCount = blocksCount;
        out.close();
    }

    private long nextCid() {
        return cidCounter.getAndIncrement();
    }

    @SneakyThrows
    private void createMap(MapChunk chunk, int sx, int sy, int sz, int ex, int ey, int ez) throws GeneratorException {
        MutableLongObjectMap<GameChunkPos> chunks = LongObjectMaps.mutable.empty();
        int cx = (ex - sx) / 2;
        int cy = (ey - sy) / 2;
        int cz = ez - sz > chunkSize ? (ez - sz) / 2 : ez - sz;
        for (int xx = sx; xx < ex; xx += cx) {
            for (int yy = sy; yy < ey; yy += cy) {
                for (int zz = sz; zz < ez; zz += cz) {
                    createChunk(terrain, chunk, chunks, xx, yy, zz, xx + cx, yy + cy, zz + cz);
                }
            }
        }
        chunk.setChunks(chunks);
        chunk.updateCenterExtent(gm.width, gm.height, gm.depth);
        putObjectToBackend(chunk);
        System.out.println(chunksCount); // TODO
        // Thread.sleep(1000);
    }

    @SneakyThrows
    private void createChunk(long[][][] terrain, MapChunk parent, MutableLongObjectMap<GameChunkPos> chunks, int x,
            int y, int z, int ex, int ey, int ez) throws GeneratorException {
        var chunk = new MapChunk(nextCid(), chunkSize, new GameChunkPos(x, y, z, ex, ey, ez));
        chunksCount++;
        chunk.setParent(parent.getId());
        chunk.updateCenterExtent(gm.width, gm.height, gm.depth);
        if (chunk.isLeaf()) {
            MutableList<MapBlock> blocks = Lists.mutable.empty();
            for (int xx = x; xx < ex; xx++) {
                for (int yy = y; yy < ey; yy++) {
                    for (int zz = z; zz < ez; zz++) {
                        var mb = new MapBlock();
                        blocksCount++;
                        mb.pos = new GameBlockPos(xx, yy, zz);
                        mb.parent = chunk.id;
                        mb.setMaterialRid(terrain[zz][yy][xx]);
                        mb.setObjectRid(809);
                        mb.updateCenterExtent(gm.width, gm.height, gm.depth);
                        if (mb.getMaterialRid() == 0) {
                            mb.setMaterialRid(898);
                        }
                        if (mb.getMaterialRid() == 898) {
                            mb.setMined(true);
                        }
                        blocks.add(mb);
                        out.append(mb.toString());
                        out.append('\n');
                        // System.out.println(mb); // TODO
                    }
                }
            }
            chunk.setBlocks(blocks);
        } else {
            putObjectToBackend(chunk);
            createMap(chunk, x, y, z, ex, ey, ez);
        }
        chunks.put(chunk.getId(), chunk.getPos());
        putObjectToBackend(chunk);
    }

    private void createNeighbors(MapChunk rootc) {
        var pos = rootc.getPos();
        int xs = (pos.ep.x - pos.x) / 2;
        int ys = (pos.ep.y - pos.y) / 2;
        int zs = (pos.ep.z - pos.z) / 2;
        Function<Long, MapChunk> r = id -> store.getChunk(id2Cid(id));
        MutableList<MapChunk> chunks = Lists.mutable.empty();
        for (int x = pos.x; x < pos.ep.x; x += xs) {
            for (int y = pos.y; y < pos.ep.y; y += ys) {
                for (int z = pos.z; z < pos.ep.z; z += zs) {
                    long chunkid = rootc.getChunk(x, y, z, x + xs, y + ys, z + zs);
                    assert chunkid != 0;
                    var chunk = store.getChunk(id2Cid(chunkid));
                    assert chunk != null;
                    if (xs > gm.chunkSize && ys > gm.chunkSize && zs > gm.chunkSize) {
                        createNeighbors(chunk);
                    }
                    int bz = z + zs;
                    int tz = z - zs;
                    int sy = y + zs;
                    int ny = y - zs;
                    int ex = x + zs;
                    int wx = x - zs;
                    long b, t, s, n, e, w;
                    if ((b = mcRoot.findChild(x, y, bz, x + xs, y + ys, bz + zs, r)) != 0) {
                        chunk.setNeighborBottom(b);
                    }
                    if ((t = mcRoot.findChild(x, y, tz, x + xs, y + ys, tz + zs, r)) != 0) {
                        chunk.setNeighborTop(t);
                    }
                    if ((s = mcRoot.findChild(x, sy, z, x + xs, sy + ys, z + zs, r)) != 0) {
                        chunk.setNeighborSouth(s);
                    }
                    if ((n = mcRoot.findChild(x, ny, z, x + xs, ny + ys, z + zs, r)) != 0) {
                        chunk.setNeighborNorth(n);
                    }
                    if ((e = mcRoot.findChild(ex, y, z, ex + xs, y + ys, z + zs, r)) != 0) {
                        chunk.setNeighborEast(e);
                    }
                    if ((e = mcRoot.findChild(ex, sy, z, ex + xs, y + ys, z + zs, r)) != 0) {
                        chunk.setNeighborSouthEast(e);
                    }
                    if ((w = mcRoot.findChild(wx, y, z, wx + xs, y + ys, z + zs, r)) != 0) {
                        chunk.setNeighborWest(w);
                    }
                    if ((w = mcRoot.findChild(wx, sy, z, wx + xs, y + ys, z + zs, r)) != 0) {
                        chunk.setNeighborSouthWest(w);
                    }
                    if (chunk.getBlocksNotEmpty()) {
                        chunk.getBlocks().get().forEachValue(mb -> setupBlockNeighbors(chunk, mb));
                    }
                    chunks.add(chunk);
                }
                putObjectsToBackend(chunks);
            }
        }
    }

    private void setupBlockNeighbors(MapChunk chunk, MapBlock mb) {
        var pos = mb.pos;
        var t = pos.addZ(-1);
        if (chunk.haveBlock(t)) {
            var tb = chunk.getBlock(t);
            if (checkSetNeighbor(tb)) {
                mb.setNeighborTop(tb.pos);
            }
        } else {
            long chunkid;
            if ((chunkid = chunk.getNeighborTop()) != 0) {
                var c = store.getChunk(id2Cid(chunkid));
                var tb = c.getBlock(t);
                if (chunk.haveBlock(t) && checkSetNeighbor(tb)) {
                    mb.setNeighborTop(tb.pos);
                }
            }
        }
        for (var d : NeighboringDir.values()) {
            var b = pos.add(d.pos);
            if (chunk.haveBlock(b)) {
                var bb = chunk.getBlock(b);
                if (checkSetNeighbor(bb)) {
                    mb.setNeighbor(d, bb.pos);
                }
            } else {
                long chunkid;
                if ((chunkid = chunk.getNeighbor(d)) != 0) {
                    var c = store.getChunk(id2Cid(chunkid));
                    var bb = c.getBlock(b);
                    if (chunk.haveBlock(b) && checkSetNeighbor(bb)) {
                        mb.setNeighbor(d, bb.pos);
                    }
                }
            }
        }
        chunk.setBlock(mb);
    }

    private boolean checkSetNeighbor(MapBlock mb) {
        return !mb.isMined();
    }

    @SneakyThrows
    private void putObjectsToBackend(Iterable<MapChunk> values) {
        values.forEach((mc) -> {
            try {
                out.append(mc.toString());
                out.append('\n');
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
        // values.forEach(System.out::println); // TODO
        store.setChunks(values);
    }

    @SneakyThrows
    private void putObjectToBackend(MapChunk chunk) {
        out.append(chunk.toString());
        out.append('\n');
        // System.out.println(chunk); // TODO
        store.setChunk(chunk);
    }

}
