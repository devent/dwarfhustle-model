package com.anrisoftware.dwarfhustle.model.terrainimage;

import java.io.IOException;
import java.net.URL;
import java.util.Deque;
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
import com.anrisoftware.dwarfhustle.model.api.objects.ObjectsGetter;
import com.anrisoftware.dwarfhustle.model.db.cache.CachePutMessage;
import com.anrisoftware.dwarfhustle.model.db.cache.StoredObjectsJcsCacheActor;
import com.google.inject.assistedinject.Assisted;

import akka.actor.typed.ActorRef;
import jakarta.inject.Inject;
import lombok.SneakyThrows;

/**
 * Imports a map from an image file to the database.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public class TerrainImageCreateMap {

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
    }

    private void createMap(MapChunk chunk, int sx, int sy, int sz, int ex, int ey, int ez) throws GeneratorException {
        chunk.setPos(new GameChunkPos(gm.id, sx, sy, sz, ex, ey, ez));
        MutableObjectLongMap<GameChunkPos> chunks = ObjectLongMaps.mutable.empty();
        this.idsBatch = gen.batch(128);
        Supplier<byte[]> idsSupplier = this::supplyIds;
        int cx = (ex - sx) / 2;
        int cy = (ey - sy) / 2;
        int cz = (ez - sz) / 2;
        for (int xx = sx; xx < ex; xx += cx) {
            for (int yy = sy; yy < ey; yy += cy) {
                for (int zz = sz; zz < ez; zz += cz) {
                    createChunk(idsSupplier, terrain, chunk, chunks, gm.id, xx, yy, zz, xx + cx, yy + cy, zz + cz);
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
            MutableObjectLongMap<GameChunkPos> chunks, long map, int x, int y, int z, int ex, int ey, int ez)
            throws GeneratorException {
        var chunk = new MapChunk(gen.generate());
        chunk.setParent(parent.getId());
        chunk.setPos(new GameChunkPos(map, x, y, z, ex, ey, ez));
        chunk.updateCenterExtent(gm.width, gm.height, gm.depth);
        int csize = gm.getChunkSize();
        if (ex - x == csize || ey - y == csize || ez - z == csize) {
            MutableMap<GameBlockPos, MapBlock> blocks = Maps.mutable.empty();
            for (int xx = x; xx < ex; xx++) {
                for (int yy = y; yy < ey; yy++) {
                    for (int zz = z; zz < ez; zz++) {
                        var mb = new MapBlock(ids.get());
                        mb.pos = new GameBlockPos(map, xx, yy, zz);
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
                    }
                }
            }
            chunk.setBlocks(blocks);
        } else {
            createMap(chunk, x, y, z, ex, ey, ez);
        }
        chunks.put(chunk.pos, chunk.getId());
        putObjectToBackend(chunk);
    }

    private void putObjectToBackend(GameObject go) {
        cache.tell(new CachePutMessage<Message>(actor.get(), go, go));
    }

}
