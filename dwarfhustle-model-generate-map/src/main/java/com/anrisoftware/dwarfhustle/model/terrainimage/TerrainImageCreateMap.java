package com.anrisoftware.dwarfhustle.model.terrainimage;

import static com.anrisoftware.dwarfhustle.model.api.objects.MapBlock.getMapBlock;
import static com.anrisoftware.dwarfhustle.model.api.objects.MapChunk.getMapChunk;
import static com.anrisoftware.dwarfhustle.model.db.cache.CachePutMessage.askCachePut;
import static com.anrisoftware.dwarfhustle.model.db.cache.CachePutsMessage.askCachePuts;
import static java.time.Duration.ofSeconds;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.Deque;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.map.primitive.MutableObjectLongMap;
import org.eclipse.collections.impl.factory.Maps;
import org.eclipse.collections.impl.factory.primitive.ObjectLongMaps;
import org.lable.oss.uniqueid.GeneratorException;
import org.lable.oss.uniqueid.IDGenerator;

import com.anrisoftware.dwarfhustle.model.actor.ActorSystemProvider;
import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos;
import com.anrisoftware.dwarfhustle.model.api.objects.GameChunkPos;
import com.anrisoftware.dwarfhustle.model.api.objects.GameMap;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;
import com.anrisoftware.dwarfhustle.model.api.objects.IdsObjectsProvider.IdsObjects;
import com.anrisoftware.dwarfhustle.model.api.objects.MapBlock;
import com.anrisoftware.dwarfhustle.model.api.objects.MapChunk;
import com.anrisoftware.dwarfhustle.model.api.objects.NeighboringDir;
import com.anrisoftware.dwarfhustle.model.api.objects.ObjectsGetter;
import com.anrisoftware.dwarfhustle.model.db.cache.CacheResponseMessage;
import com.anrisoftware.dwarfhustle.model.db.cache.CacheResponseMessage.CacheErrorMessage;
import com.anrisoftware.dwarfhustle.model.db.cache.StoredObjectsJcsCacheActor;
import com.google.inject.assistedinject.Assisted;

import akka.actor.typed.ActorRef;
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

        TerrainImageCreateMap create(ObjectsGetter og);
    }

    @Inject
    @IdsObjects
    private IDGenerator gen;

    @Inject
    @Assisted
    private ObjectsGetter og;

    @Inject
    private ActorSystemProvider actor;

    private long[][][] terrain;

    private MapChunk mcRoot;

    private GameMap gm;

    private Deque<byte[]> idsBatch;

    private ActorRef<Message> cache;

    public void startImport(URL url, TerrainLoadImage image, long mapid) throws IOException, GeneratorException {
        this.cache = actor.getActor(StoredObjectsJcsCacheActor.ID);
        this.terrain = image.load(url);
        this.mcRoot = new MapChunk(gen.generate());
        this.gm = og.get(GameMap.class, GameMap.OBJECT_TYPE, mapid);
        gm.root = mcRoot.id;
        mcRoot.setRoot(true);
        createMap(mcRoot, 0, 0, 0, gm.width, gm.height, gm.depth);
        createNeighbors(mcRoot);
        putObjectToBackend(gm);
    }

    private void createMap(MapChunk chunk, int sx, int sy, int sz, int ex, int ey, int ez) throws GeneratorException {
        chunk.setPos(new GameChunkPos(sx, sy, sz, ex, ey, ez));
        MutableObjectLongMap<GameChunkPos> chunks = ObjectLongMaps.mutable.empty();
        this.idsBatch = gen.batch(128);
        Supplier<byte[]> idsSupplier = this::supplyIds;
        int cx = (ex - sx) / 2;
        int cy = (ey - sy) / 2;
        int cz = (ez - sz) / 2;
        for (int xx = sx; xx < ex; xx += cx) {
            for (int yy = sy; yy < ey; yy += cy) {
                for (int zz = sz; zz < ez; zz += cz) {
                    createChunk(idsSupplier, terrain, chunk, chunks, xx, yy, zz, xx + cx, yy + cy, zz + cz);
                }
            }
        }
        chunk.setChunks(chunks);
        chunk.updateCenterExtent(gm.width, gm.height, gm.depth);
        putObjectToBackend(chunk);
    }

    @SneakyThrows
    private byte[] supplyIds() {
        var next = idsBatch.poll();
        if (next == null) {
            idsBatch = gen.batch(128);
            next = idsBatch.poll();
        }
        return next;
    }

    private void createChunk(Supplier<byte[]> ids, long[][][] terrain, MapChunk parent,
            MutableObjectLongMap<GameChunkPos> chunks, int x, int y, int z, int ex, int ey, int ez)
            throws GeneratorException {
        var chunk = new MapChunk(gen.generate());
        chunk.map = gm.id;
        chunk.setParent(parent.getId());
        chunk.setPos(new GameChunkPos(x, y, z, ex, ey, ez));
        chunk.updateCenterExtent(gm.width, gm.height, gm.depth);
        int csize = gm.getChunkSize();
        if (ex - x == csize || ey - y == csize || ez - z == csize) {
            MutableMap<GameBlockPos, MapBlock> blocks = Maps.mutable.empty();
            MutableObjectLongMap<GameBlockPos> blocksids = ObjectLongMaps.mutable.empty();
            for (int xx = x; xx < ex; xx++) {
                for (int yy = y; yy < ey; yy++) {
                    for (int zz = z; zz < ez; zz++) {
                        var mb = new MapBlock(ids.get());
                        mb.pos = new GameBlockPos(xx, yy, zz);
                        mb.map = gm.id;
                        mb.setMaterialRid(terrain[zz][yy][xx]);
                        mb.setObjectRid(809);
                        mb.updateCenterExtent(gm.width, gm.height, gm.depth);
                        if (mb.getMaterialRid() == 0) {
                            mb.setMaterialRid(898);
                        }
                        if (mb.getMaterialRid() == 898) {
                            mb.setMined(true);
                        }
                        blocks.put(mb.pos, mb);
                        blocksids.put(mb.pos, mb.id);
                    }
                }
            }
            chunk.setBlocks(blocksids);
            putObjectsToBackend(MapBlock.class, blocks.values());
        } else {
            createMap(chunk, x, y, z, ex, ey, ez);
        }
        chunks.put(chunk.pos, chunk.getId());
        putObjectToBackend(chunk);
    }

    private void createNeighbors(MapChunk rootc) {
        var pos = rootc.pos;
        int xs = (pos.ep.x - pos.x) / 2;
        int ys = (pos.ep.y - pos.y) / 2;
        int zs = (pos.ep.z - pos.z) / 2;
        Function<Long, MapChunk> r = id -> getMapChunk(og, id);
        for (int x = pos.x; x < pos.ep.x; x += xs) {
            for (int y = pos.y; y < pos.ep.y; y += ys) {
                for (int z = pos.z; z < pos.ep.z; z += zs) {
                    var chunk = getMapChunk(og, rootc.chunks.get(new GameChunkPos(x, y, z, x + xs, y + ys, z + zs)));
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
                    if (chunk.getBlocks().notEmpty()) {
                        chunk.getBlocks().forEachValue(mb -> setupBlockNeighbors(chunk, mb));
                    }
                    putObjectToBackend(chunk);
                }
            }
        }
    }

    private void setupBlockNeighbors(MapChunk chunk, long id) {
        var mb = getMapBlock(og, id);
        var pos = mb.pos;
        var t = pos.addZ(-1);
        long tbid = chunk.getBlock(t);
        final long empty = chunk.getBlocksEmptyValue();
        if (tbid != empty && checkSetNeighbor(getMapBlock(og, tbid))) {
            mb.setNeighborTop(tbid);
        } else {
            long chunkid;
            if ((chunkid = chunk.getNeighborTop()) != 0) {
                var c = getMapChunk(og, chunkid);
                tbid = c.getBlock(t);
                if (tbid != empty && checkSetNeighbor(getMapBlock(og, tbid))) {
                    mb.setNeighborTop(tbid);
                }
            }
        }
        for (var d : NeighboringDir.values()) {
            var b = pos.add(d.pos);
            var bbid = chunk.getBlock(b);
            if (bbid != empty && checkSetNeighbor(getMapBlock(og, bbid))) {
                mb.setNeighbor(d, bbid);
            } else {
                long chunkid;
                if ((chunkid = chunk.getNeighbor(d)) != 0) {
                    var c = getMapChunk(og, chunkid);
                    bbid = c.getBlock(b);
                    if (bbid != empty && checkSetNeighbor(getMapBlock(og, bbid))) {
                        mb.setNeighbor(d, bbid);
                    }
                }
            }
        }
        putObjectToBackend(mb);
    }

    private boolean checkSetNeighbor(MapBlock mb) {
        return !mb.isMined();
    }

    @SneakyThrows
    private void putObjectsToBackend(Class<?> keyType, Iterable<? extends GameObject> values) {
        System.out.println("TerrainImageCreateMap.putObjectsToBackend()"); // TODO
        askCachePuts(actor.getActorSystem(), CACHE_TIMEOUT, keyType, (go) -> go.getId(), values)
                .whenComplete(this::putObjectToBackendCompleted).toCompletableFuture().get();
        System.out.println("END TerrainImageCreateMap.putObjectsToBackend()"); // TODO
    }

    @SneakyThrows
    private void putObjectToBackend(GameObject go) {
        System.out.println("TerrainImageCreateMap.putObjectToBackend()"); // TODO
        askCachePut(actor.getActorSystem(), CACHE_TIMEOUT, go.id, go).whenComplete(this::putObjectToBackendCompleted)
                .toCompletableFuture().get();
        System.out.println("END TerrainImageCreateMap.putObjectToBackend()"); // TODO
    }

    private void putObjectToBackendCompleted(CacheResponseMessage<?> res, Throwable ex) {
        logError(res, ex);
    }

    private void logError(CacheResponseMessage<?> res, Throwable ex) {
        if (ex != null) {
            log.error("storeValueBackend", ex);
        } else {
            if (res instanceof CacheErrorMessage m) {
                log.error("storeValueBackend", m.error);
            } else {
                // success
            }
        }
    }

}
