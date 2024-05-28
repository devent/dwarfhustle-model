package com.anrisoftware.dwarfhustle.model.knowledge.evrete;

import static com.anrisoftware.dwarfhustle.model.api.objects.MapBlockFlags.*;
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
public class TerrainUpdateRules extends AbstractTerrainRules {

    @Rule(salience = 1)
    public void chunk_done(BlockFact $f, RhsContext ctx) {
        $f.chunk.changed = false;
    }

    //
    // Block hidden or visible based on neighbors.
    //

    @Rule(salience = 10)
    @Where(value = { "$f.z == 0" })
    public void block_z_0_is_visible_discovered(BlockFact $f, RhsContext ctx) {
        $f.addProp(VISIBLE.flag);
        $f.addProp(DISCOVERED.flag);
    }

    @Rule(salience = 10)
    @Where(value = { "$f.z > 0 && $f.isNeighborsFilled(U)" })
    public void block_is_hidden(BlockFact $f, RhsContext ctx) {
        $f.addProp(HIDDEN.flag);
    }

    @Rule(salience = 10)
    @Where(value = "$f.z > 0 && $f.isLineOfSightUp()")
    public void block_discovered_above_empty(BlockFact $f, RhsContext ctx) {
        //System.out.printf("block_discovered_above_empty(%d/%d/%d)%n",$f.x, $f.y, $f.z); // TODO
        $f.addProp(DISCOVERED.flag);
    }

    //
    // Object type based on block status and neighbors.
    //

//    @Rule(salience = 100)
//    @Where(value = { "$f.isProp(MapBlockFlags.EMPTY) || $f.isProp(MapBlockFlags.LIQUID)" })
//    public void object_set_block_on_empty_liquid(BlockFact $f, RhsContext ctx) {
//        $f.setObject(block);
//    }
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

}
