package com.anrisoftware.dwarfhustle.model.knowledge.evrete

import static com.anrisoftware.dwarfhustle.model.api.objects.MapBlock.*

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * @see BlockArray
 */
class BlockArrayTest {

    static blocksArrayFile = "/home/devent/Projects/dwarf-hustle/docu/terrain-maps/BlockArrayTest-512-512-128.txt"

    static short OXYGEN_ID = 923

    static short STONE_ID = 852

    static short LIQUID_ID = 930

    static BlockArrayRules rules

    @BeforeAll
    static void setupRules() {
        this.rules = new BlockArrayRules()
        rules.setupRulesInit()
        rules.setupRulesMap()
    }

    int w = 512, h = 512, d = 128

    BlockArray array

    @BeforeEach
    void setupBlocks() {
        this.array = new BlockArray(w, h, d)
        loadBlockArray()
    }

    void loadBlockArray() {
        def fc = new FileInputStream(blocksArrayFile).getChannel();
        array.blocks.position(0);
        fc.read(array.blocks);
        fc.close();
    }

    void initBlockArray() {
        for (int z = 0; z < d; z++) {
            def session = rules.initialKn.newStatelessSession()
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    session.insert(new BlockFact(array, x, y, z))
                }
            }
            session.fire()
        }
        def fc = new FileOutputStream(blocksArrayFile).getChannel();
        array.blocks.position(0);
        fc.write(array.blocks);
        fc.close();
    }

    @Test
    void up_natural_light() {
        for (int z = 0; z < d; z++) {
            def session = rules.mapKn.newStatelessSession()
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    session.insert(new BlockFact(array, x, y, z))
                }
            }
            session.fire()
        }
        for (int z = 0; z < d; z++) {
            if (array.isProp(0, 0, z, EMPTY)) {
                array.setLux(0, 0, z, 65536)
            }
        }
    }
}
