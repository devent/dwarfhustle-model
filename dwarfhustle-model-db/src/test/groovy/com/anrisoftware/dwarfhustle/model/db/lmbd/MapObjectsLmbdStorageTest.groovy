package com.anrisoftware.dwarfhustle.model.db.lmbd

import static java.lang.Math.round

import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos
import com.anrisoftware.dwarfhustle.model.api.objects.GameMap
import com.anrisoftware.dwarfhustle.model.api.vegetations.Grass
import com.anrisoftware.dwarfhustle.model.api.vegetations.VegetationBuffer

import groovy.util.logging.Slf4j

/**
 * @see MapObjectsLmbdStorage
 */
@Slf4j
class MapObjectsLmbdStorageTest {

    @Test
    void putObject_test(@TempDir Path tmp) {
        def gm = new GameMap(1)
        gm.width = 32
        gm.height = 32
        gm.depth = 32
        def grass = new Grass()
        grass.id = 100
        grass.map = gm.id
        grass.pos.x = 10
        grass.pos.y = 11
        grass.pos.z = 12
        grass.kid = 800
        grass.growth = 0.3
        def storage = new MapObjectsLmbdStorage(tmp, gm)
        assert VegetationBuffer.SIZE == 28
        storage.putObject(grass.pos.x, grass.pos.y, grass.pos.z, VegetationBuffer.SIZE, grass.id, { b ->
            VegetationBuffer.writeObject(b, 0, grass)
        })
        def thatGrass = storage.getObject(grass.id, { b ->
            VegetationBuffer.readObject(b, 0, new Grass())
        })
        storage.close()
        assert thatGrass.id == grass.id
        assert thatGrass.map == grass.map
        assert thatGrass.pos == grass.pos
        assert thatGrass.kid == grass.kid
        assert thatGrass.growth == grass.growth
    }

    @Test
    void putObject_benchmark(@TempDir Path tmp) {
        def gm = new GameMap(1)
        gm.width = 512
        gm.height = 512
        gm.depth = 512
        def storage = new MapObjectsLmbdStorage(tmp, gm)
        int zz = 32
        int yy = 256
        int xx = 256
        log.info "putObject_benchmark {} objects", zz * yy * xx
        def timeNow = System.currentTimeMillis()
        for (int z = 0; z < zz; z++) {
            for (int y = 0; y < yy; y++) {
                for (int x = 0; x < xx; x++) {
                    def grass = new Grass()
                    grass.id = 100 + z + y + x
                    grass.map = gm.id
                    grass.pos.x = x
                    grass.pos.y = y
                    grass.pos.z = z
                    grass.kid = 800
                    grass.growth = 0.3
                    storage.putObject(grass.pos.x, grass.pos.y, grass.pos.z, VegetationBuffer.SIZE, grass.id, { b ->
                        VegetationBuffer.writeObject(b, 0, grass)
                    })
                }
            }
        }
        storage.close()
        log.info "putObject_benchmark done in {}.", (System.currentTimeMillis() - timeNow) / 1000f
        Files.list(tmp).withCloseable {
            it.filter({file -> !Files.isDirectory(file)})
            .collect(Collectors.toSet()).each {
                def size = it.toFile().size()
                log.info "Size {} = {} B, {} KB, {} MB, {} GB.", it, size, round(size / 1024), round(size / 1024 / 1024), round(size / 1024 / 1024 / 1024)
            }
        }
    }

    @Test
    void putObjects_benchmark(@TempDir Path tmp) {
        int zz = 32
        int yy = 256
        int xx = 256
        def gm = new GameMap(1)
        gm.width = 512
        gm.height = 512
        gm.depth = 512
        def objects = createObjects(gm, xx, yy, zz)
        def storage = new MapObjectsLmbdStorage(tmp, gm)
        log.info "putObjects_benchmark {} objects", objects.size()
        def timeNow = System.currentTimeMillis()
        storage.putObjects(VegetationBuffer.SIZE, objects, { o, b ->
            VegetationBuffer.writeObject(b, 0, o)
        })
        storage.close()
        log.info "putObjects_benchmark done in {}.", (System.currentTimeMillis() - timeNow) / 1000f
        Files.list(tmp).withCloseable {
            it.filter({file -> !Files.isDirectory(file)})
            .collect(Collectors.toSet()).each {
                def size = it.toFile().size()
                log.info "Size {} = {} B, {} KB, {} MB, {} GB.", it, size, round(size / 1024), round(size / 1024 / 1024), round(size / 1024 / 1024 / 1024)
            }
        }
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
        def storage = new MapObjectsLmbdStorage(tmp, gm)
        log.info "putObjects_benchmark {} objects", objects.size()
        def timeNow = System.currentTimeMillis()
        storage.putObjects(VegetationBuffer.SIZE, objects, { o, b ->
            VegetationBuffer.writeObject(b, 0, o)
        })
        storage.close()
        log.info "putObjects_benchmark done in {}.", (System.currentTimeMillis() - timeNow) / 1000f
        Files.list(tmp).withCloseable {
            it.filter({file -> !Files.isDirectory(file)})
            .collect(Collectors.toSet()).each {
                def size = it.toFile().size()
                log.info "Size {} = {} B, {} KB, {} MB, {} GB.", it, size, round(size / 1024), round(size / 1024 / 1024), round(size / 1024 / 1024 / 1024)
            }
        }
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
        def storage = new MapObjectsLmbdStorage(tmp, gm)
        storage.putObjects(VegetationBuffer.SIZE, objects, { o, b ->
            VegetationBuffer.writeObject(b, 0, o)
        })
        log.info "getObjects_benchmark {} objects", objects.size()
        def rnd = new Random()
        for (int z = 0; z < zz; z++) {
            for (int y = 0; y < yy; y++) {
                for (int x = 0; x < xx; x++) {
                    int pz = rnd.nextInt(zz)
                    int py = rnd.nextInt(yy)
                    int px = rnd.nextInt(xx)
                    def posObjects = []
                    storage.getObjects(px, py, pz, { VegetationBuffer.readObject(it, 0, new Grass()) }, { posObjects << it })
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
