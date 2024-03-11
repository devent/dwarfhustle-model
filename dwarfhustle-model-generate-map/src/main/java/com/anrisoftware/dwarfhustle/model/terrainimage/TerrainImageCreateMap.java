package com.anrisoftware.dwarfhustle.model.terrainimage;

import static java.time.Duration.ofSeconds;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.primitive.MutableObjectLongMap;
import org.eclipse.collections.impl.factory.primitive.ObjectLongMaps;
import org.lable.oss.uniqueid.GeneratorException;

import com.anrisoftware.dwarfhustle.model.actor.ActorSystemProvider;
import com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos;
import com.anrisoftware.dwarfhustle.model.api.objects.GameChunkPos;
import com.anrisoftware.dwarfhustle.model.api.objects.GameMap;
import com.anrisoftware.dwarfhustle.model.api.objects.MapBlock;
import com.anrisoftware.dwarfhustle.model.api.objects.MapChunk;
import com.anrisoftware.dwarfhustle.model.api.objects.MapChunksStore;
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
    private ActorSystemProvider actor;

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
        this.mcRoot = new MapChunk(nextCid(), chunkSize);
        this.gm = gm;
        this.chunksCount = 0;
        this.blocksCount = 0;
        gm.root = mcRoot.id;
        chunksCount++;
        createMap(mcRoot, 0, 0, 0, gm.width, gm.height, gm.depth);
        // createNeighbors(mcRoot);
        gm.chunksCount = chunksCount;
        gm.blocksCount = blocksCount;
        out.close();
    }

    private long nextCid() {
        return cidCounter.getAndIncrement();
    }

    @SneakyThrows
    private void createMap(MapChunk chunk, int sx, int sy, int sz, int ex, int ey, int ez) throws GeneratorException {
        chunk.setPos(new GameChunkPos(sx, sy, sz, ex, ey, ez));
        MutableObjectLongMap<GameChunkPos> chunks = ObjectLongMaps.mutable.empty();
        int cx = (ex - sx) / 2;
        int cy = (ey - sy) / 2;
        int cz = (ez - sz) / 2;
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
    private void createChunk(long[][][] terrain, MapChunk parent, MutableObjectLongMap<GameChunkPos> chunks, int x,
            int y, int z, int ex, int ey, int ez) throws GeneratorException {
        var chunk = new MapChunk(nextCid(), chunkSize);
        chunksCount++;
        chunk.setParent(parent.getId());
        chunk.setPos(new GameChunkPos(x, y, z, ex, ey, ez));
        chunk.updateCenterExtent(gm.width, gm.height, gm.depth);
        int csize = gm.getChunkSize();
        if (ex - x == csize || ey - y == csize || ez - z == csize) {
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
                        // System.out.println(mb); // TODO
                    }
                }
            }
            chunk.setBlocks(blocks);
        } else {
            createMap(chunk, x, y, z, ex, ey, ez);
        }
        chunks.put(chunk.getPos(), chunk.getId());
        putObjectToBackend(chunk);
    }

    private void createNeighbors(MapChunk rootc) {
//        var pos = rootc.getPos();
//        int xs = (pos.ep.x - pos.x) / 2;
//        int ys = (pos.ep.y - pos.y) / 2;
//        int zs = (pos.ep.z - pos.z) / 2;
//        Function<Long, MapChunk> r = id -> getMapChunk(og, id);
//        MutableList<MapChunk> chunks = Lists.mutable.empty();
//        for (int x = pos.x; x < pos.ep.x; x += xs) {
//            for (int y = pos.y; y < pos.ep.y; y += ys) {
//                for (int z = pos.z; z < pos.ep.z; z += zs) {
//                    var chunk = getMapChunk(og, rootc.chunks.get(new GameChunkPos(x, y, z, x + xs, y + ys, z + zs)));
//                    assert chunk != null;
//                    if (xs > gm.chunkSize && ys > gm.chunkSize && zs > gm.chunkSize) {
//                        createNeighbors(chunk);
//                    }
//                    int bz = z + zs;
//                    int tz = z - zs;
//                    int sy = y + zs;
//                    int ny = y - zs;
//                    int ex = x + zs;
//                    int wx = x - zs;
//                    long b, t, s, n, e, w;
//                    if ((b = mcRoot.findChild(x, y, bz, x + xs, y + ys, bz + zs, r)) != 0) {
//                        chunk.setNeighborBottom(b);
//                    }
//                    if ((t = mcRoot.findChild(x, y, tz, x + xs, y + ys, tz + zs, r)) != 0) {
//                        chunk.setNeighborTop(t);
//                    }
//                    if ((s = mcRoot.findChild(x, sy, z, x + xs, sy + ys, z + zs, r)) != 0) {
//                        chunk.setNeighborSouth(s);
//                    }
//                    if ((n = mcRoot.findChild(x, ny, z, x + xs, ny + ys, z + zs, r)) != 0) {
//                        chunk.setNeighborNorth(n);
//                    }
//                    if ((e = mcRoot.findChild(ex, y, z, ex + xs, y + ys, z + zs, r)) != 0) {
//                        chunk.setNeighborEast(e);
//                    }
//                    if ((e = mcRoot.findChild(ex, sy, z, ex + xs, y + ys, z + zs, r)) != 0) {
//                        chunk.setNeighborSouthEast(e);
//                    }
//                    if ((w = mcRoot.findChild(wx, y, z, wx + xs, y + ys, z + zs, r)) != 0) {
//                        chunk.setNeighborWest(w);
//                    }
//                    if ((w = mcRoot.findChild(wx, sy, z, wx + xs, y + ys, z + zs, r)) != 0) {
//                        chunk.setNeighborSouthWest(w);
//                    }
//                    if (chunk.getBlocksNotEmpty()) {
//                        chunk.getBlocks().forEachValue(mb -> setupBlockNeighbors(parent, mb));
//                    }
//                    chunks.add(chunk);
//                }
//                putObjectsToBackend(MapChunk.OBJECT_TYPE, chunks);
//            }
//        }
    }

    private void setupBlockNeighbors(MapChunk chunk, MapBlock mb) {
//        var pos = mb.pos;
//        var t = pos.addZ(-1);
//        var tb = chunk.getBlock(t);
//        if (chunk.haveBlock(t) && checkSetNeighbor(tb)) {
//            mb.setNeighborTop(tb.id);
//        } else {
//            long chunkid;
//            if ((chunkid = chunk.getNeighborTop()) != 0) {
//                var c = getMapChunk(og, chunkid);
//                tb = c.getBlock(t);
//                if (chunk.haveBlock(t) && checkSetNeighbor(tb)) {
//                    mb.setNeighborTop(tb.id);
//                }
//            }
//        }
//        for (var d : NeighboringDir.values()) {
//            var b = pos.add(d.pos);
//            var bb = chunk.getBlock(b);
//            if (chunk.haveBlock(b) && checkSetNeighbor(bb)) {
//                mb.setNeighbor(d, bb.id);
//            } else {
//                long chunkid;
//                if ((chunkid = chunk.getNeighbor(d)) != 0) {
//                    var c = getMapChunk(og, chunkid);
//                    bb = c.getBlock(b);
//                    if (chunk.haveBlock(b) && checkSetNeighbor(bb)) {
//                        mb.setNeighbor(d, bb.id);
//                    }
//                }
//            }
//        }
    }

    private boolean checkSetNeighbor(MapBlock mb) {
        return !mb.isMined();
    }

    @SneakyThrows
    private void putObjectsToBackend(Iterable<MapChunk> values) {
        values.forEach((mc) -> {
            try {
                out.append(mc.toString());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
        // values.forEach(System.out::println); // TODO
        // store.setChunks(values);
    }

    @SneakyThrows
    private void putObjectToBackend(MapChunk chunk) {
        out.append(chunk.toString());
        // System.out.println(chunk); // TODO
        // store.setChunk(chunk);
    }

}
