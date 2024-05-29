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
    @Where(value = { "$f.z > 0 && !$f.isNeighborsFilled(U)" })
    public void block_is_visible(BlockFact $f, RhsContext ctx) {
        $f.addProp(VISIBLE.flag);
    }

    @Rule(salience = 10)
    @Where(value = "$f.z > 0 && $f.isLineOfSightUp()")
    public void block_discovered_above_empty(BlockFact $f, RhsContext ctx) {
        $f.addProp(DISCOVERED.flag);
    }

    //
    // Object type based on block status and neighbors.
    //

    @Rule(salience = 1000)
    @Where(value = { "$f.isProp(EMPTY.flag) || $f.isProp(LIQUID.flag) || $f.isProp(FILLED.flag)" })
    public void object_set_block_on_empty_liquid(BlockFact $f, RhsContext ctx) {
        $f.setObject(block);
    }

    @Rule(salience = 100)
    @Where(value = { "$f.isProp(FILLED.flag)", "!$f.isNeighborsSameLevelExist()" })
    public void object_set_block_on_map_edge(BlockFact $f, RhsContext ctx) {
        $f.setObject(block);
    }

    @Rule(salience = 100)
    @Where(value = { "$f.isProp(FILLED.flag)", "$f.isNeighborsSameLevelExist() && $f.isNeighborsSameLevelEmpty()" })
    public void object_set_ramp_single_on_neighbors_same_level_empty(BlockFact $f, RhsContext ctx) {
        $f.setObject(ramp_single);
        $f.addProp(RAMP.flag);
    }

    @Rule(salience = 100)
    @Where(value = { "!$f.isProp(EMPTY.flag)", "$f.isNeighborsSameLevelExist() && $f.isNeighborsSameLevelFilled()" })
    public void block_set_block_on_neighbors_same_level_filled(BlockFact $f, RhsContext ctx) {
        $f.setObject(block);
    }

    @Rule(salience = 100)
    @Where(value = { "$f.isProp(FILLED.flag)",
            "$f.isNeighborsExist(N, E, S, W) && $f.isNeighborsEmpty(E, S, W) && $f.isNeighborsFilled(N)" })
    public void ramp_tri_s(BlockFact $f, RhsContext ctx) {
        $f.setObject(ramp_tri_s);
        $f.addProp(RAMP.flag);
    }

    @Rule(salience = 100)
    @Where(value = { "$f.isProp(FILLED.flag)",
            "$f.isNeighborsExist(N, E, S, W) && $f.isNeighborsEmpty(N, S, W) && $f.isNeighborsFilled(E)" })
    public void ramp_tri_w(BlockFact $f, RhsContext ctx) {
        $f.setObject(ramp_tri_w);
        $f.addProp(RAMP.flag);
    }

    @Rule(salience = 100)
    @Where(value = { "$f.isProp(FILLED.flag)",
            "$f.isNeighborsExist(N, E, S, W) && $f.isNeighborsEmpty(N, E, W) && $f.isNeighborsFilled(S)" })
    public void ramp_tri_n(BlockFact $f, RhsContext ctx) {
        $f.setObject(ramp_tri_n);
        $f.addProp(RAMP.flag);
    }

    @Rule(salience = 100)
    @Where(value = { "$f.isProp(FILLED.flag)",
            "$f.isNeighborsExist(N, E, S, W) && $f.isNeighborsEmpty(N, E, S) && $f.isNeighborsFilled(W)" })
    public void ramp_tri_e(BlockFact $f, RhsContext ctx) {
        $f.setObject(ramp_tri_e);
        $f.addProp(RAMP.flag);
    }

    @Rule(salience = 100)
    @Where(value = { "$f.isProp(FILLED.flag)",
            "$f.isNeighborsExist(N, SE, W, E, S) && $f.isNeighborsEmpty(N, SE, W) && $f.isNeighborsFilled(E, S)" })
    public void ramp_corner_nw(BlockFact $f, RhsContext ctx) {
        $f.setObject(ramp_corner_nw);
        $f.addProp(RAMP.flag);
    }

    @Rule(salience = 100)
    @Where(value = { "$f.isProp(FILLED.flag)",
            "$f.isNeighborsExist(N, E, SW, W, S) && $f.isNeighborsEmpty(N, E, SW) && $f.isNeighborsFilled(W, S)" })
    public void ramp_corner_ne(BlockFact $f, RhsContext ctx) {
        $f.setObject(ramp_corner_ne);
        $f.addProp(RAMP.flag);
    }

    @Rule(salience = 100)
    @Where(value = { "$f.isProp(FILLED.flag)",
            "$f.isNeighborsExist(NE, S, W, N, E) && $f.isNeighborsEmpty(NE, S, W) && $f.isNeighborsFilled(N, E)" })
    public void ramp_corner_sw(BlockFact $f, RhsContext ctx) {
        $f.setObject(ramp_corner_sw);
        $f.addProp(RAMP.flag);
    }

    @Rule(salience = 100)
    @Where(value = { "$f.isProp(FILLED.flag)",
            "$f.isNeighborsExist(S, NW, E, W) && $f.isNeighborsEmpty(S, NW, E) && $f.isNeighborsFilled(N, W)" })
    public void ramp_corner_se(BlockFact $f, RhsContext ctx) {
        $f.setObject(ramp_corner_se);
        $f.addProp(RAMP.flag);
    }

    @Rule(salience = 100)
    @Where(value = { "$f.isProp(FILLED.flag)",
            "$f.isNeighborsExist(S, N, E, W) && $f.isNeighborsEmpty(S) && $f.isNeighborsFilled(N) && $f.isNeighborsRamp(E, W)" })
    public void ramp_perp_s(BlockFact $f, RhsContext ctx) {
        $f.setObject(ramp_perp_s);
        $f.addProp(RAMP.flag);
    }

    @Rule(salience = 100)
    @Where(value = { "$f.isProp(FILLED.flag)",
            "$f.isNeighborsExist(E, W, N, S) && $f.isNeighborsEmpty(E) && $f.isNeighborsFilled(W) && $f.isNeighborsRamp(N, S)" })
    public void ramp_perp_e(BlockFact $f, RhsContext ctx) {
        $f.setObject(ramp_perp_e);
        $f.addProp(RAMP.flag);
    }

    @Rule(salience = 100)
    @Where(value = { "$f.isProp(FILLED.flag)",
            "$f.isNeighborsExist(N, S, E, W) && $f.isNeighborsEmpty(N) && $f.isNeighborsFilled(S) && $f.isNeighborsRamp(E, W)" })
    public void ramp_perp_n(BlockFact $f, RhsContext ctx) {
        $f.setObject(ramp_perp_n);
        $f.addProp(RAMP.flag);
    }

    @Rule(salience = 100)
    @Where(value = { "$f.isProp(FILLED.flag)",
            "$f.isNeighborsExist(W, E, N, S) && $f.isNeighborsEmpty(W) && $f.isNeighborsFilled(E) && $f.isNeighborsRamp(N, S)" })
    public void ramp_perp_w(BlockFact $f, RhsContext ctx) {
        $f.setObject(ramp_perp_w);
        $f.addProp(RAMP.flag);
    }

    @Rule(salience = 100)
    @Where(value = { "$f.isProp(FILLED.flag)",
            "$f.isNeighborsExist(E, S, NW, N, W) && $f.isNeighborsEmpty(E, S) && $f.isNeighborsFilled(NW) && $f.isNeighborsRamp(N, W)" })
    public void ramp_edge_out_se(BlockFact $f, RhsContext ctx) {
        $f.setObject(ramp_edge_out_se);
        $f.addProp(RAMP.flag);
    }

    @Rule(salience = 100)
    @Where(value = { "$f.isProp(FILLED.flag)",
            "$f.isNeighborsExist(N, W, SE, E, S) && $f.isNeighborsEmpty(N, W) && $f.isNeighborsFilled(SE) && $f.isNeighborsRamp(E, S)" })
    public void ramp_edge_out_nw(BlockFact $f, RhsContext ctx) {
        $f.setObject(ramp_edge_out_nw);
        $f.addProp(RAMP.flag);
    }

    @Rule(salience = 100)
    @Where(value = { "$f.isProp(FILLED.flag)",
            "$f.isNeighborsExist(S, W, NE, N, E) && $f.isNeighborsEmpty(S, W) && $f.isNeighborsFilled(NE) && $f.isNeighborsRamp(N, E)" })
    public void ramp_edge_out_sw(BlockFact $f, RhsContext ctx) {
        $f.setObject(ramp_edge_out_sw);
        $f.addProp(RAMP.flag);
    }

    @Rule(salience = 100)
    @Where(value = { "$f.isProp(FILLED.flag)",
            "$f.isNeighborsExist(N, E, SW, S, W) && $f.isNeighborsEmpty(N, E) && $f.isNeighborsFilled(SW) && $f.isNeighborsRamp(S, W)" })
    public void ramp_edge_out_ne(BlockFact $f, RhsContext ctx) {
        $f.setObject(ramp_edge_out_ne);
        $f.addProp(RAMP.flag);
    }

    @Rule(salience = 100)
    @Where(value = { "$f.isProp(FILLED.flag)",
            "$f.isNeighborsExist(E, N, W, NW, SW) && $f.isNeighborsEmpty(E) && $f.isNeighborsFilled(N, W, NW) && $f.isNeighborsRamp(S, SW)" })
    public void ramp_edge_in_se(BlockFact $f, RhsContext ctx) {
        $f.setObject(ramp_edge_in_se);
        $f.addProp(RAMP.flag);
    }

    @Rule(salience = 100)
    @Where(value = { "$f.isProp(FILLED.flag)",
            "$f.isNeighborsExist(W, E, SE, S, N, NE) && $f.isNeighborsEmpty(W) && $f.isNeighborsFilled(E, SE, S) && $f.isNeighborsRamp(N, NE)" })
    public void ramp_edge_in_nw(BlockFact $f, RhsContext ctx) {
        $f.setObject(ramp_edge_in_nw);
        $f.addProp(RAMP.flag);
    }

    @Rule(salience = 100)
    @Where(value = { "$f.isProp(FILLED.flag)",
            "$f.isNeighborsExist(S, N, NE, E, W, NW) && $f.isNeighborsEmpty(S) && $f.isNeighborsFilled(N, NE, E) && $f.isNeighborsRamp(W, NW)" })
    public void ramp_edge_in_sw(BlockFact $f, RhsContext ctx) {
        $f.setObject(ramp_edge_in_sw);
        $f.addProp(RAMP.flag);
    }

    @Rule(salience = 100)
    @Where(value = { "$f.isProp(FILLED.flag)",
            "$f.isNeighborsExist(E, S, SW, W, N, NW) && $f.isNeighborsEmpty(E) && $f.isNeighborsFilled(S, SW, W) && $f.isNeighborsRamp(N, NW)" })
    public void ramp_edge_in_ne(BlockFact $f, RhsContext ctx) {
        $f.setObject(ramp_edge_in_ne);
        $f.addProp(RAMP.flag);
    }

}
