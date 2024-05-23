package com.anrisoftware.dwarfhustle.model.knowledge.evrete;

import static com.anrisoftware.dwarfhustle.model.api.objects.MapBlock.*;
import static com.anrisoftware.dwarfhustle.model.knowledge.evrete.BlockArray.*;

import org.evrete.api.Environment;
import org.evrete.api.RhsContext;
import org.evrete.dsl.Phase;
import org.evrete.dsl.annotation.PhaseListener;
import org.evrete.dsl.annotation.Rule;
import org.evrete.dsl.annotation.Where;

import com.anrisoftware.dwarfhustle.model.api.objects.MapBlock;
import com.anrisoftware.dwarfhustle.model.api.objects.MapChunk;

import org.evrete.dsl.annotation.MethodPredicate;

public class BlockArrayRules {

    public static final short OXYGEN_ID = 923;

    public static final short STONE_ID = 852;

    public static final short LIQUID_ID = 930;

    @PhaseListener(Phase.CREATE)
    public void initResources(Environment env) {
    }

    @Rule(salience = 1000000)
    @Where("$f.z == 0")
    public void block_discovered_z_0(BlockFact $f, RhsContext ctx) {
        System.out.printf("block_discovered_z_0 %d/%d/%d \n", $f.x, $f.y, $f.z); // TODO
        addProp($f.chunk, $f.x, $f.y, $f.z, DISCOVERED);
    }

    @Rule(salience = 1000000)
    @Where(value = "$f.z > 0", methods = { @MethodPredicate(method = "test_up_empty", args = { "$f" }) })
    public void block_discovered_above_empty(BlockFact $f, RhsContext ctx) {
        //System.out.printf("block_discovered_above_empty(%d/%d/%d)%n",$f.x, $f.y, $f.z); // TODO
        addProp($f.chunk, $f.x, $f.y, $f.z, DISCOVERED);
    }

    @Rule(salience = 10000000)
    public void chunk_done(BlockFact $f, RhsContext ctx) {
        $f.chunk.changed = false;
    }

    /**
     * Returns true if every block above the fact block is empty, i.e. there is
     * natural light above the fact block.
     */
    public boolean test_up_empty(BlockFact f) {
        //System.out.printf("test_up_empty(%d/%d/%d)%n",f.x, f.y, f.z); // TODO
        var chunk = f.chunk;
        for (int z = f.z - 1; z > 0; z--) {
            if (chunk.isInside(f.x, f.y, z)) {
                //System.out.printf("%d/%d/%d %s\n", f.x, f.y, z, isProp(chunk, f.x, f.y, z, EMPTY)); // TODO
                if (!isProp(chunk, f.x, f.y, z, EMPTY)) {
                    return false;
                }
            } else {
                chunk = f.retriever.apply(chunk.parent);
                chunk = chunk.findChunk(f.x, f.y, z, f.retriever);
            }
        }
        return true;
    }
}
