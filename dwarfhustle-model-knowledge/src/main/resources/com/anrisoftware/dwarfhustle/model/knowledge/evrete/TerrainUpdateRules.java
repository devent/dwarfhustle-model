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

/**
 * Updates terrain blocks.
 * 
 * <ul>
 * </ul>
 */
public class BlockArrayRules extends AbstractTerrainRules {

    private static final int SALIENCE_BLOCK_VISIBLE = 1000;
    
    private static final int SALIENCE_LOWEST = 1;

    @Rule(salience = SALIENCE_LOWEST)
    public void chunk_done(BlockFact $f, RhsContext ctx) {
        $f.chunk.changed = false;
    }

    //
    // Block hidden or visible based on neighbors.
    //

//    @Rule(salience = SALIENCE_BLOCK_VISIBLE)
//    @Where(value = { "$f.block.isNeighborsFilled($f.neighbors, U)" })
//    public void block_is_hidden(BlockFact $f, RhsContext ctx) {
//        $f.block.setHidden(true);
//    }

    @Rule(salience = SALIENCE_BLOCK_VISIBLE)
    @Where("$f.z == 0")
    public void block_discovered_z_0(BlockFact $f, RhsContext ctx) {
        //System.out.printf("block_discovered_z_0 %d/%d/%d \n", $f.x, $f.y, $f.z); // TODO
        $f.addProp(DISCOVERED);
    }

    @Rule(salience = SALIENCE_BLOCK_VISIBLE)
    @Where(value = "$f.z > 0", methods = { @MethodPredicate(method = "test_up_empty", args = { "$f" }) })
    public void block_discovered_above_empty(BlockFact $f, RhsContext ctx) {
        //System.out.printf("block_discovered_above_empty(%d/%d/%d)%n",$f.x, $f.y, $f.z); // TODO
        $f.addProp(DISCOVERED);
    }

    //
    // Object type based on block status and neighbors.
    //

    @Rule(salience = 100)
    @Where(value = { "$f.isProp(EMPTY) || $f.isProp(LIQUID)" })
    public void object_set_block_on_empty_liquid(BlockFact $f, RhsContext ctx) {
        $f.setObject(block);
    }
//
//    @Rule(salience = 100)
//    @Where(value = { "$f.block.filled", "!$f.block.isNeighborsSameLevelExist($f.neighbors)" })
//    public void object_set_block_on_map_edge(BlockFact $f, RhsContext ctx) {
//        $f.block.setObject(block);
//    }
//
//    @Rule(salience = 100)
//    @Where(value = { "$f.block.filled", "$f.block.isNeighborsSameLevelExist($f.neighbors)",
//            "$f.block.isNeighborsSameLevelEmpty($f.neighbors)" })
//    public void object_set_ramp_single_on_neighbors_same_level_empty(BlockFact $f, RhsContext ctx) {
//        $f.block.setObject(ramp_single);
//        $f.block.setRamp(true);
//    }
//
//    @Rule(salience = 100)
//    @Where(value = { "!$f.block.empty", "$f.block.isNeighborsSameLevelFilled($f.neighbors)" })
//    public void block_set_block_on_neighbors_same_level_filled(BlockFact $f, RhsContext ctx) {
//        $f.block.setObject(block);
//    }
//
//    @Rule(salience = 100)
//    @Where(value = { "$f.block.filled", "$f.block.isNeighborsExist($f.neighbors, N, E, S, W)",
//            "$f.block.isNeighborsEmpty($f.neighbors, E, S, W)", "$f.block.isNeighborsFilled($f.neighbors, N)" })
//    public void ramp_tri_s(BlockFact $f, RhsContext ctx) {
//        $f.block.setObject(ramp_tri_s);
//        $f.block.setRamp(true);
//    }
//
//    @Rule(salience = 100)
//    @Where(value = { "$f.block.filled", "$f.block.isNeighborsExist($f.neighbors, N, E, S, W)",
//            "$f.block.isNeighborsEmpty($f.neighbors, N, S, W)", "$f.block.isNeighborsFilled($f.neighbors, E)" })
//    public void ramp_tri_w(BlockFact $f, RhsContext ctx) {
//        $f.block.setObject(ramp_tri_w);
//        $f.block.setRamp(true);
//    }
//
//    @Rule(salience = 100)
//    @Where(value = { "$f.block.filled", "$f.block.isNeighborsExist($f.neighbors, N, E, S, W)",
//            "$f.block.isNeighborsEmpty($f.neighbors, N, E, W)", "$f.block.isNeighborsFilled($f.neighbors, S)" })
//    public void ramp_tri_n(BlockFact $f, RhsContext ctx) {
//        $f.block.setObject(ramp_tri_n);
//        $f.block.setRamp(true);
//    }
//
//    @Rule(salience = 100)
//    @Where(value = { "$f.block.filled", "$f.block.isNeighborsExist($f.neighbors, N, E, S, W)",
//            "$f.block.isNeighborsEmpty($f.neighbors, N, E, S)", "$f.block.isNeighborsFilled($f.neighbors, W)" })
//    public void ramp_tri_e(BlockFact $f, RhsContext ctx) {
//        $f.block.setObject(ramp_tri_e);
//        $f.block.setRamp(true);
//    }
//
//    @Rule(salience = 100)
//    @Where(value = { "$f.block.filled", "$f.block.isNeighborsExist($f.neighbors, N, SE, W, E, S)",
//            "$f.block.isNeighborsEmpty($f.neighbors, N, SE, W)", "$f.block.isNeighborsFilled($f.neighbors, E, S)" })
//    public void ramp_corner_nw(BlockFact $f, RhsContext ctx) {
//        $f.block.setObject(ramp_corner_nw);
//        $f.block.setRamp(true);
//    }
//
//    @Rule(salience = 100)
//    @Where(value = { "$f.block.filled", "$f.block.isNeighborsExist($f.neighbors, N, E, SW, W, S)",
//            "$f.block.isNeighborsEmpty($f.neighbors, N, E, SW)", "$f.block.isNeighborsFilled($f.neighbors, W, S)" })
//    public void ramp_corner_ne(BlockFact $f, RhsContext ctx) {
//        $f.block.setObject(ramp_corner_ne);
//        $f.block.setRamp(true);
//    }
//
//    @Rule(salience = 100)
//    @Where(value = { "$f.block.filled", "$f.block.isNeighborsExist($f.neighbors, NE, S, W, N, E)",
//            "$f.block.isNeighborsEmpty($f.neighbors, NE, S, W)", "$f.block.isNeighborsFilled($f.neighbors, N, E)" })
//    public void ramp_corner_sw(BlockFact $f, RhsContext ctx) {
//        $f.block.setObject(ramp_corner_sw);
//        $f.block.setRamp(true);
//    }
//
//    @Rule(salience = 100)
//    @Where(value = { "$f.block.filled", "$f.block.isNeighborsExist($f.neighbors, S, NW, E, W)",
//            "$f.block.isNeighborsEmpty($f.neighbors, S, NW, E)", "$f.block.isNeighborsFilled($f.neighbors, N, W)" })
//    public void ramp_corner_se(BlockFact $f, RhsContext ctx) {
//        $f.block.setObject(ramp_corner_se);
//        $f.block.setRamp(true);
//    }
//
//    @Rule(salience = 100)
//    @Where(value = { "$f.block.filled", "$f.block.isNeighborsExist($f.neighbors, S, N, E, W)",
//            "$f.block.isNeighborsEmpty($f.neighbors, S)", "$f.block.isNeighborsFilled($f.neighbors, N)",
//            "$f.block.isNeighborsRamp($f.neighbors, E, W)" })
//    public void ramp_perp_s(BlockFact $f, RhsContext ctx) {
//        $f.block.setObject(ramp_perp_s);
//        $f.block.setRamp(true);
//    }
//
//    @Rule(salience = 100)
//    @Where(value = { "$f.block.filled", "$f.block.isNeighborsExist($f.neighbors, E, W, N, S)",
//            "$f.block.isNeighborsEmpty($f.neighbors, E)", "$f.block.isNeighborsFilled($f.neighbors, W)",
//            "$f.block.isNeighborsRamp($f.neighbors, N, S)" })
//    public void ramp_perp_e(BlockFact $f, RhsContext ctx) {
//        $f.block.setObject(ramp_perp_e);
//        $f.block.setRamp(true);
//    }
//
//    @Rule(salience = 100)
//    @Where(value = { "$f.block.filled", "$f.block.isNeighborsExist($f.neighbors, N, S, E, W)",
//            "$f.block.isNeighborsEmpty($f.neighbors, N)", "$f.block.isNeighborsFilled($f.neighbors, S)",
//            "$f.block.isNeighborsRamp($f.neighbors, E, W)" })
//    public void ramp_perp_n(BlockFact $f, RhsContext ctx) {
//        $f.block.setObject(ramp_perp_n);
//        $f.block.setRamp(true);
//    }
//
//    @Rule(salience = 100)
//    @Where(value = { "$f.block.filled", "$f.block.isNeighborsExist($f.neighbors, W, E, N, S)",
//            "$f.block.isNeighborsEmpty($f.neighbors, W)", "$f.block.isNeighborsFilled($f.neighbors, E)",
//            "$f.block.isNeighborsRamp($f.neighbors, N, S)" })
//    public void ramp_perp_w(BlockFact $f, RhsContext ctx) {
//        $f.block.setObject(ramp_perp_w);
//        $f.block.setRamp(true);
//    }
//
//    @Rule(salience = 100)
//    @Where(value = { "$f.block.filled", "$f.block.isNeighborsExist($f.neighbors, E, S, NW, N, W)",
//            "$f.block.isNeighborsEmpty($f.neighbors, E, S)", "$f.block.isNeighborsFilled($f.neighbors, NW)",
//            "$f.block.isNeighborsRamp($f.neighbors, N, W)" })
//    public void ramp_edge_out_se(BlockFact $f, RhsContext ctx) {
//        $f.block.setObject(ramp_edge_out_se);
//        $f.block.setRamp(true);
//    }
//
//    @Rule(salience = 100)
//    @Where(value = { "$f.block.filled", "$f.block.isNeighborsExist($f.neighbors, N, W, SE, E, S)",
//            "$f.block.isNeighborsEmpty($f.neighbors, N, W)", "$f.block.isNeighborsFilled($f.neighbors, SE)",
//            "$f.block.isNeighborsRamp($f.neighbors, E, S)" })
//    public void ramp_edge_out_nw(BlockFact $f, RhsContext ctx) {
//        $f.block.setObject(ramp_edge_out_nw);
//        $f.block.setRamp(true);
//    }
//
//    @Rule(salience = 100)
//    @Where(value = { "$f.block.filled", "$f.block.isNeighborsExist($f.neighbors, S, W, NE, N, E)",
//            "$f.block.isNeighborsEmpty($f.neighbors, S, W)", "$f.block.isNeighborsFilled($f.neighbors, NE)",
//            "$f.block.isNeighborsRamp($f.neighbors, N, E)" })
//    public void ramp_edge_out_sw(BlockFact $f, RhsContext ctx) {
//        $f.block.setObject(ramp_edge_out_sw);
//        $f.block.setRamp(true);
//    }
//
//    @Rule(salience = 100)
//    @Where(value = { "$f.block.filled", "$f.block.isNeighborsExist($f.neighbors, N, E, SW, S, W)",
//            "$f.block.isNeighborsEmpty($f.neighbors, N, E)", "$f.block.isNeighborsFilled($f.neighbors, SW)",
//            "$f.block.isNeighborsRamp($f.neighbors, S, W)" })
//    public void ramp_edge_out_ne(BlockFact $f, RhsContext ctx) {
//        $f.block.setObject(ramp_edge_out_ne);
//        $f.block.setRamp(true);
//    }
//
//    @Rule(salience = 100)
//    @Where(value = { "$f.block.filled", "$f.block.isNeighborsExist($f.neighbors, E, N, W, NW, SW)",
//            "$f.block.isNeighborsEmpty($f.neighbors, E)", "$f.block.isNeighborsFilled($f.neighbors, N, W, NW)",
//            "$f.block.isNeighborsRamp($f.neighbors, S, SW)" })
//    public void ramp_edge_in_se(BlockFact $f, RhsContext ctx) {
//        $f.block.setObject(ramp_edge_in_se);
//        $f.block.setRamp(true);
//    }
//
//    @Rule(salience = 100)
//    @Where(value = { "$f.block.filled", "$f.block.isNeighborsExist($f.neighbors, W, E, SE, S, N, NE)",
//            "$f.block.isNeighborsEmpty($f.neighbors, W)", "$f.block.isNeighborsFilled($f.neighbors, E, SE, S)",
//            "$f.block.isNeighborsRamp($f.neighbors, N, NE)" })
//    public void ramp_edge_in_nw(BlockFact $f, RhsContext ctx) {
//        $f.block.setObject(ramp_edge_in_nw);
//        $f.block.setRamp(true);
//    }
//
//    @Rule(salience = 100)
//    @Where(value = { "$f.block.filled", "$f.block.isNeighborsExist($f.neighbors, S, N, NE, E, W, NW)",
//            "$f.block.isNeighborsEmpty($f.neighbors, S)", "$f.block.isNeighborsFilled($f.neighbors, N, NE, E)",
//            "$f.block.isNeighborsRamp($f.neighbors, W, NW)" })
//    public void ramp_edge_in_sw(BlockFact $f, RhsContext ctx) {
//        $f.block.setObject(ramp_edge_in_sw);
//        $f.block.setRamp(true);
//    }
//
//    @Rule(salience = 100)
//    @Where(value = { "$f.block.filled", "$f.block.isNeighborsExist($f.neighbors, E, S, SW, W, N, NW)",
//            "$f.block.isNeighborsEmpty($f.neighbors, E)", "$f.block.isNeighborsFilled($f.neighbors, S, SW, W)",
//            "$f.block.isNeighborsRamp($f.neighbors, N, NW)" })
//    public void ramp_edge_in_ne(BlockFact $f, RhsContext ctx) {
//        $f.block.setObject(ramp_edge_in_ne);
//        $f.block.setRamp(true);
//    }

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
