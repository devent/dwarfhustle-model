/*
 * dwarfhustle-model-db - Manages the compile dependencies for the model.
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
package com.anrisoftware.dwarfhustle.model.db.lmbd

import static java.lang.Math.pow

import java.nio.file.Path

import org.eclipse.collections.api.factory.primitive.LongLists
import org.eclipse.collections.api.list.primitive.LongList
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos
import com.anrisoftware.dwarfhustle.model.api.objects.GameMap
import com.anrisoftware.dwarfhustle.model.api.vegetations.Grass
import com.anrisoftware.dwarfhustle.model.db.lmbd.MapObjectsLmbdStorage.MapObjectsLmbdStorageFactory
import com.google.inject.Guice
import com.google.inject.Injector

import groovy.util.logging.Slf4j

/**
 * @see MapObjectsLmbdStorage
 */
@Slf4j
class MapObjectsLmbdStorageTest {

    static Injector injector

    @BeforeAll
    static void setupInjector() {
        injector = Guice.createInjector(new DwarfhustleModelDbLmbdModule())
    }

    @Test
    @org.junit.jupiter.api.Disabled
    void putObject_benchmark(@TempDir Path tmp) {
        def gm = new GameMap(1, 512, 512, 128)
        long mapSize = 200 * (long) pow(10, 6);
        def storage = injector.getInstance(MapObjectsLmbdStorageFactory).create(tmp, gm, mapSize)
        int zz = 32
        int yy = 32
        int xx = 32
        int cid = 1
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
                    storage.putObject(grass.pos.x, grass.pos.y, grass.pos.z, cid, Grass.OBJECT_TYPE, grass.id)
                }
            }
        }
        storage.close()
        log.info "putObject_benchmark done in {}.", (System.currentTimeMillis() - timeNow) / 1000f
        TestUtils.listFiles log, tmp
    }

    @Test
    void putObjects_benchmark(@TempDir Path tmp) {
        int zz = 32
        int yy = 12
        int xx = 12
        def gm = new GameMap(1, 512, 512, 128)
        def objects = createObjects(gm, xx, yy, zz)
        long mapSize = 200 * (long) pow(10, 6);
        def storage = injector.getInstance(MapObjectsLmbdStorageFactory).create(tmp, gm, mapSize)
        int cid = 1
        int index = 0
        int type = Grass.OBJECT_TYPE
        log.info "putObjects_benchmark {} objects", objects.size()
        def timeNow = System.currentTimeMillis()
        storage.putObjects(cid, index, type, objects)
        storage.close()
        log.info "putObjects_benchmark done in {}.", (System.currentTimeMillis() - timeNow) / 1000f
        TestUtils.listFiles log, tmp
    }

    @Test
    void putObjectsParallel_benchmark(@TempDir Path tmp) {
        int zz = 32
        int yy = 256
        int xx = 256
        def gm = new GameMap(1, 512, 512, 128)
        def objects = createObjects(gm, xx, yy, zz)
        long mapSize = 200 * (long) pow(10, 6);
        def storage = injector.getInstance(MapObjectsLmbdStorageFactory).create(tmp, gm, mapSize)
        int cid = 1
        int index = 0
        int type = Grass.OBJECT_TYPE
        log.info "putObjects_benchmark {} objects", objects.size()
        def timeNow = System.currentTimeMillis()
        storage.putObjects(cid, index, type, objects)
        storage.close()
        log.info "putObjects_benchmark done in {}.", (System.currentTimeMillis() - timeNow) / 1000f
        TestUtils.listFiles log, tmp
    }

    @Test
    void getObjects_benchmark(@TempDir Path tmp) {
        int zz = 8
        int yy = 128
        int xx = 128
        def gm = new GameMap(1, 512, 512, 128)
        def objects = createObjects(gm, xx, yy, zz)
        long mapSize = 200 * (long) pow(10, 6);
        def storage = injector.getInstance(MapObjectsLmbdStorageFactory).create(tmp, gm, mapSize)
        int cid = 1
        int index = 0
        int type = Grass.OBJECT_TYPE
        storage.putObjects(cid, index, type, objects)
        log.info "getObjects_benchmark {} objects", objects.size()
        def rnd = new Random()
        for (int z = 0; z < zz; z++) {
            for (int y = 0; y < yy; y++) {
                for (int x = 0; x < xx; x++) {
                    int pz = rnd.nextInt(zz)
                    int py = rnd.nextInt(yy)
                    int px = rnd.nextInt(xx)
                    def posObjectsIds = []
                    def posObjectsTypes = []
                    storage.getObjects(px, py, pz, { _cid, _type, _id, _x, _y, _z ->
                        posObjectsIds << _id
                        posObjectsTypes << _type
                    })
                    //assert posObjectsIds.size() == 1
                    //assert posObjectsIds[0] == calcObjectId(gm, px, py, pz)
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
        def gm = new GameMap(1, 32, 32, 32)
        def objects = createObjects(gm, xx, yy, zz)
        long mapSize = 200 * (long) pow(10, 6);
        def storage = injector.getInstance(MapObjectsLmbdStorageFactory).create(tmp, gm, mapSize)
        int cid = 1
        int index = 0
        int type = Grass.OBJECT_TYPE
        storage.putObjects(cid, index, type, objects)
        def posObjectsIds = []
        def posObjectsTypes = []
        def o = storage.getObjectsRange(0, 0, 0, 4, 4, 4, { _cid, _type, _id, _x, _y, _z ->
            posObjectsIds << _id
            posObjectsTypes << _type
        })
        storage.close()
        assert posObjectsIds.size() == 32 * 32 * 32
        log.info "getObjectsRange_test done."
    }

    static LongList createObjects(GameMap gm, int xx, int yy, int zz) {
        def rnd = new Random()
        def objects = LongLists.mutable.withInitialCapacity(xx * yy * zz)
        for (int z = 0; z < zz; z++) {
            for (int y = 0; y < yy; y++) {
                for (int x = 0; x < xx; x++) {
                    def grass = new Grass()
                    grass.id = calcObjectId(gm, x, y, z)
                    grass.map = gm.id
                    grass.pos.x = x
                    grass.pos.y = y
                    grass.pos.z = z
                    grass.kid = rnd.nextInt(1000)
                    grass.growth = 0.3
                    objects.add grass.id
                }
            }
        }
        return objects
    }

    static long calcObjectId(GameMap gm, int x, int y, int z) {
        100 + GameBlockPos.calcIndex(gm.width, gm.height, gm.depth, 0, 0, 0, x, y, z)
    }

    @Test
    @org.junit.jupiter.api.Disabled
    void read_objects_test() {
        int xx = 32
        int yy = 32
        int zz = 32
        int cc = 8
        def gm = new GameMap(141464932948020, xx, yy, zz)
        Path tmp = Path.of("/home/devent/Projects/dwarf-hustle/terrain-maps/game/", "terrain_${xx}_${yy}_${zz}_${cc}", "map-${gm.id}")
        long mapSize = 200 * (long) pow(10, 6);
        def storage = injector.getInstance(MapObjectsLmbdStorageFactory).create(tmp, gm, mapSize)
        def posObjects = []
        def o = storage.getObjectsRange(0, 0, 0, xx, yy, zz, { _cid, _type, _id, _x, _y, _z ->
            def map = [:]
            posObjects << map
            map.id = _id
            map.type = _type
            map.x = _x
            map.y = _y
            map.z = _z
        })
        storage.close()
        posObjects.each {
            println it
        }
    }
}
