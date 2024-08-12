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

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import com.anrisoftware.dwarfhustle.model.api.objects.GameMap
import com.anrisoftware.dwarfhustle.model.api.objects.WorldMap
import com.anrisoftware.dwarfhustle.model.db.buffers.GameMapBuffer
import com.anrisoftware.dwarfhustle.model.db.buffers.WorldMapBuffer
import com.anrisoftware.dwarfhustle.model.db.lmbd.GameObjectsLmbdStorage.GameObjectsLmbdStorageFactory
import com.google.inject.Guice
import com.google.inject.Injector

import groovy.util.logging.Slf4j

/**
 * @see GameObjectsLmbdStorage
 */
@Slf4j
class GameObjectsLmbdStorageTest {

    static Injector injector

    @BeforeAll
    static void setupInjector() {
        injector = Guice.createInjector(new DwarfhustleModelDbLmbdModule())
    }

    @Test
    void putObject_test(@TempDir Path tmp) {
        def wm = new WorldMap(1)
        wm.name = "Big Endless World"
        wm.distanceLat = 1
        wm.distanceLon = 1
        def gm = new GameMap(2)
        gm.name = "Timeless Fortress"
        gm.width = 32
        gm.height = 32
        gm.depth = 32
        wm.maps.add(gm.id)
        wm.currentMap = gm.id
        def storage = injector.getInstance(GameObjectsLmbdStorageFactory).create(tmp)
        storage.putObject(WorldMap.OBJECT_TYPE, wm.id, WorldMapBuffer.calcSize(wm), { b ->
            WorldMapBuffer.setWorldMap(b, 0, wm)
        })
        storage.putObject(GameMap.OBJECT_TYPE, gm.id, GameMapBuffer.calcSize(gm), { b ->
            GameMapBuffer.setGameMap(b, 0, gm)
        })
        def that_wm = storage.getObject(WorldMap.OBJECT_TYPE, wm.id)
        def that_gm = storage.get(GameMap.OBJECT_TYPE, gm.id)
        test_getObjectsIterator(storage)
        storage.close()
        assert that_wm.id == wm.id
        assert that_wm.name == wm.name
        assert that_gm.id == gm.id
        assert that_gm.name == gm.name
        TestUtils.listFiles log, tmp
    }

    void test_getObjectsIterator(GameObjectsLmbdStorage storage) {
        storage.getObjects(WorldMap.OBJECT_TYPE).withCloseable { ite ->
            for (def go : ite) {
                println go
            }
        }
        storage.getObjects(WorldMap.OBJECT_TYPE).withCloseable { ite ->
            assert ite.hasNext()
            println ite.next()
        }
        storage.getObjects(GameMap.OBJECT_TYPE).withCloseable { ite ->
            for (def go : ite) {
                println go
            }
        }
    }
}
