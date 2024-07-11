package com.anrisoftware.dwarfhustle.model.db.lmbd

import java.nio.file.Path

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos
import com.anrisoftware.dwarfhustle.model.api.objects.GameMap
import com.anrisoftware.dwarfhustle.model.api.vegetations.Grass
import com.anrisoftware.dwarfhustle.model.api.vegetations.GrassBuffer
import com.anrisoftware.dwarfhustle.model.api.vegetations.VegetationBuffer

import groovy.util.logging.Slf4j

/**
 * @see MapObjectsLmbdStorage
 */
@Slf4j
class MapObjectsLmbdStorageTest {

    @Test
    void putObject_benchmark(@TempDir Path tmp) {
        def gm = new GameMap(1)
        gm.width = 512
        gm.height = 512
        gm.depth = 512
        def storage = new MapObjectsLmbdStorage(tmp, gm, { })
        int zz = 32
        int yy = 256
        int xx = 256
        log.info "putObject_benchmark {} objects", zz * yy * xx
        def timeNow = System.currentTimeMillis()
        for (int z = 0; z < zz; z++) {
            for (int y = 0; y < yy; y++) {
                for (int x = 0; x < xx; x++) {
                    def grass = new Grass()
                    grass.id = 100 + GameBlockPos.calcIndex(gm.width, gm.height, gm.depth, 0, 0, 0, x, y, z);
                    grass.map = gm.id
                    grass.pos.x = x
                    grass.pos.y = y
                    grass.pos.z = z
                    grass.kid = 800
                    grass.growth = 0.3
                    storage.putObject(grass.pos.x, grass.pos.y, grass.pos.z, Grass.OBJECT_TYPE, grass.id)
                }
            }
        }
        storage.close()
        log.info "putObject_benchmark done in {}.", (System.currentTimeMillis() - timeNow) / 1000f
        Utils.listFiles log, tmp
    }

    @Test
    void putObjects_benchmark(@TempDir Path tmp) {
        int zz = 32
        int yy = 12
        int xx = 12
        def gm = new GameMap(1)
        gm.width = 512
        gm.height = 512
        gm.depth = 512
        def objects = createObjects(gm, xx, yy, zz)
        def storage = new MapObjectsLmbdStorage(tmp, gm, { })
        log.info "putObjects_benchmark {} objects", objects.size()
        def timeNow = System.currentTimeMillis()
        storage.putObjects(objects)
        storage.close()
        log.info "putObjects_benchmark done in {}.", (System.currentTimeMillis() - timeNow) / 1000f
        Utils.listFiles log, tmp
    }

    @Test
    void putObjectsParallel_benchmark(@TempDir Path tmp) {
        int zz = 32
        int yy = 256
        int xx = 256
        def gm = new GameMap(1)
        gm.width = 512
        gm.height = 512
        gm.depth = 512
        def objects = createObjects(gm, xx, yy, zz)
        def storage = new MapObjectsLmbdStorage(tmp, gm, { })
        log.info "putObjects_benchmark {} objects", objects.size()
        def timeNow = System.currentTimeMillis()
        storage.putObjects(objects)
        storage.close()
        log.info "putObjects_benchmark done in {}.", (System.currentTimeMillis() - timeNow) / 1000f
        Utils.listFiles log, tmp
    }

    @Test
    void getObjects_benchmark(@TempDir Path tmp) {
        int zz = 8
        int yy = 128
        int xx = 128
        def gm = new GameMap(1)
        gm.width = 512
        gm.height = 512
        gm.depth = 512
        def objects = createObjects(gm, xx, yy, zz)
        def ostorage = new GameObjectsLmbdStorage(tmp.resolve("objects"), ObjectTypes.OBJECT_TYPES, TypeReadBuffers.TYPE_READ_BUFFERS)
        def storage = new MapObjectsLmbdStorage(tmp.resolve("gamemap"), gm)
        storage.putObjects(objects)
        log.info "getObjects_benchmark {} objects", objects.size()
        def rnd = new Random()
        for (int z = 0; z < zz; z++) {
            for (int y = 0; y < yy; y++) {
                for (int x = 0; x < xx; x++) {
                    int pz = rnd.nextInt(zz)
                    int py = rnd.nextInt(yy)
                    int px = rnd.nextInt(xx)
                    def posObjects = []
                    storage.getObjects(px, py, pz, { GrassBuffer.getGrass(it, 0, new Grass()) }, { posObjects << it })
                    assert posObjects.size() == 1
                    assert posObjects[0].pos.x == px
                    assert posObjects[0].pos.y == py
                    assert posObjects[0].pos.z == pz
                }
            }
        }
        storage.close()
        log.info "getObjects_benchmark done."
    }

    @Test
    void getObjectsRange_test(@TempDir Path tmp) {
        int zz = 32
        int yy = 32
        int xx = 32
        def gm = new GameMap(1)
        gm.width = 32
        gm.height = 32
        gm.depth = 32
        def objects = createObjects(gm, xx, yy, zz)
        def storage = new MapObjectsLmbdStorage(tmp, gm)
        storage.putObjects(VegetationBuffer.SIZE,objects, { o, b ->
            VegetationBuffer.writeObject(b, 0, o)
        })
        def thatObjects = []
        def o = storage.getObjectsRange(0, 0, 0, 4, 4, 4, { VegetationBuffer.readObject(it, 0, new Grass()) }, { thatObjects << it })
        storage.close()
        assert thatObjects.size() == 4 * 4 * 4
        thatObjects.each {
            println it
        }
        log.info "getObjectsRange_test done."
    }

    List createObjects(GameMap gm, int xx, int yy, int zz) {
        def rnd = new Random()
        def objects = []
        for (int z = 0; z < zz; z++) {
            for (int y = 0; y < yy; y++) {
                for (int x = 0; x < xx; x++) {
                    def grass = new Grass()
                    grass.id = 100 + GameBlockPos.calcIndex(gm.width, gm.height, gm.depth, 0, 0, 0, x, y, z);
                    grass.map = gm.id
                    grass.pos.x = x
                    grass.pos.y = y
                    grass.pos.z = z
                    grass.kid = rnd.nextInt(1000)
                    grass.growth = 0.3
                    objects << grass
                }
            }
        }
        return objects
    }
}
