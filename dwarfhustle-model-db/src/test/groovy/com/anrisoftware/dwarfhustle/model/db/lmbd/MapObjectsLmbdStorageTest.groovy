/*
 * dwarfhustle-model-db - Manages the compile dependencies for the model.
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
package com.anrisoftware.dwarfhustle.model.db.lmbd

import java.nio.file.Path

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos
import com.anrisoftware.dwarfhustle.model.api.objects.GameMap
import com.anrisoftware.dwarfhustle.model.api.vegetations.Grass

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
        def storage = new MapObjectsLmbdStorage(tmp, gm)
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
        def storage = new MapObjectsLmbdStorage(tmp, gm)
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
        def storage = new MapObjectsLmbdStorage(tmp, gm)
        storage.putObjects(objects)
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
                    storage.getObjects(px, py, pz, { type, id ->
                        posObjectsIds << id
                        posObjectsTypes << type
                    })
                    assert posObjectsIds.size() == 1
                    assert posObjectsIds[0] == calcObjectId(gm, px, py, pz)
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
        storage.putObjects(objects)
        def posObjectsIds = []
        def posObjectsTypes = []
        def o = storage.getObjectsRange(0, 0, 0, 4, 4, 4, { type, id ->
            posObjectsIds << id
            posObjectsTypes << type
        })
        storage.close()
        assert posObjectsIds.size() == 4 * 4 * 4
        posObjectsIds.each {
            println it
        }
        log.info "getObjectsRange_test done."
    }

    static List createObjects(GameMap gm, int xx, int yy, int zz) {
        def rnd = new Random()
        def objects = []
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
                    objects << grass
                }
            }
        }
        return objects
    }

    static long calcObjectId(GameMap gm, int x, int y, int z) {
        100 + GameBlockPos.calcIndex(gm.width, gm.height, gm.depth, 0, 0, 0, x, y, z)
    }
}
