package com.anrisoftware.dwarfhustle.model.knowledge.evrete

import java.nio.file.Path

import org.apache.commons.io.IOUtils
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import com.anrisoftware.dwarfhustle.model.api.objects.MapChunksStore

/**
 * @see BlockArray
 */
class BlockArrayTest {

    static blocksArrayFile = "/home/devent/Projects/dwarf-hustle/docu/terrain-maps/BlockArrayTest-512-512-128.txt"

    static short OXYGEN_ID = 923

    static short STONE_ID = 852

    static short LIQUID_ID = 930

    static BlockArrayRules rules

    @TempDir
    static Path tmp

    static path = "/home/devent/Projects/dwarf-hustle/docu/terrain-maps"

    static MapChunksStore createStore(Path tmp, int w, int h, int d, int chunkSize, int chunksCount) {
        def name = "terrain_${w}_${h}_${d}_${chunkSize}_${chunksCount}"
        def fileName = "${path}/${name}.map"
        def stream = BlockArrayTest.class.getResourceAsStream(fileName)
        def file = tmp.resolve("${name}.map")
        IOUtils.copy(new FileInputStream(fileName), new FileOutputStream(file.toFile()))
        return new MapChunksStore(file, w, h, chunkSize, chunksCount)
    }

    @BeforeAll
    static void setupRules() {
        this.rules = new BlockArrayRules()
        rules.setupRulesMap()
    }

    MapChunksStore store

    @BeforeEach
    void setupBlocks() {
        int w = 512
        int h = 512
        int d = 128
        int chunkSize = 32
        int chunksCount = 1353
        this.store = createStore(tmp, w, h, d, chunkSize, chunksCount)
    }

    @Test
    void up_natural_light() {
        def worker = new BlockArrayWorker(store, rules)
        worker.runRules()
    }
}