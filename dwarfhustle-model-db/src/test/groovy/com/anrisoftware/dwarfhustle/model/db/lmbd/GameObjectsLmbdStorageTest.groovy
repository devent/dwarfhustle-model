package com.anrisoftware.dwarfhustle.model.db.lmbd

import static com.anrisoftware.dwarfhustle.model.db.lmbd.TypeReadBuffers.TYPE_READ_BUFFERS

import java.nio.file.Path

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import com.anrisoftware.dwarfhustle.model.api.objects.GameMap
import com.anrisoftware.dwarfhustle.model.api.objects.GameMapBuffer
import com.anrisoftware.dwarfhustle.model.api.objects.WorldMap
import com.anrisoftware.dwarfhustle.model.api.objects.WorldMapBuffer

import groovy.util.logging.Slf4j

/**
 * @see GameObjectsLmbdStorage
 */
@Slf4j
class GameObjectsLmbdStorageTest {

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
        def storage = new GameObjectsLmbdStorage(tmp, ObjectTypes.OBJECT_TYPES, TYPE_READ_BUFFERS)
        storage.putObject(WorldMap.OBJECT_TYPE, wm.id, WorldMapBuffer.getSize(wm), { b ->
            WorldMapBuffer.setWorldMap(b, 0, wm)
        })
        storage.putObject(GameMap.OBJECT_TYPE, gm.id, GameMapBuffer.getSize(gm), { b ->
            GameMapBuffer.setGameMap(b, 0, gm)
        })
        def that_wm = storage.getObject(WorldMap.OBJECT_TYPE, wm.id)
        def that_gm = storage.get(GameMap.OBJECT_TYPE, gm.id)
        storage.close()
        assert that_wm.id == wm.id
        assert that_wm.name == wm.name
        assert that_gm.id == gm.id
        assert that_gm.name == gm.name
        Utils.listFiles log, tmp
    }
}
