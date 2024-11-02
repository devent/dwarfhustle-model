package com.anrisoftware.dwarfhustle.model.knowledge.evrete;

import static com.anrisoftware.dwarfhustle.model.api.objects.MapBlockFlags.*;

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

    //
    // Block hidden or visible based on neighbors.
    //

    @Rule(salience = 10)
    @Where(value = { "$f.z == 0" })
    public void block_z_0_is_visible_discovered(BlockFact $f, RhsContext ctx) {
        $f.addProp(VISIBLE);
        $f.addProp(DISCOVERED);
        $f.addProp(HAVE_NATURAL_LIGHT);
    }

    @Rule(salience = 10)
    @Where(value = { "$f.z > 0 && !$f.isNeighborsFilled(U)" })
    public void block_is_visible(BlockFact $f, RhsContext ctx) {
        $f.addProp(VISIBLE);
    }

    @Rule(salience = 10)
    @Where(value = "$f.z > 0 && $f.isLineOfSightUp()")
    public void block_discovered_line_if_sight_from_up(BlockFact $f, RhsContext ctx) {
        $f.addProp(DISCOVERED);
        $f.addProp(HAVE_NATURAL_LIGHT);
    }

    @Rule(salience = 10)
    @Where(value = { "$f.y > 0 && $f.z > 0 && $f.isNeighborsFilled(U) && !$f.isNeighborsFilled(N)" })
    public void block_is_visible_n_empty(BlockFact $f, RhsContext ctx) {
        $f.addProp(VISIBLE);
    }

    @Rule(salience = 10)
    @Where(value = { "$f.x > 0 && $f.z > 0 && $f.isNeighborsFilled(U) && !$f.isNeighborsFilled(W)" })
    public void block_is_visible_w_empty(BlockFact $f, RhsContext ctx) {
        $f.addProp(VISIBLE);
    }

    @Rule(salience = 10)
    @Where(value = { "$f.y < $f.h1 && $f.z > 0 && $f.isNeighborsFilled(U) && !$f.isNeighborsFilled(S)" })
    public void block_is_visible_s_empty(BlockFact $f, RhsContext ctx) {
        $f.addProp(VISIBLE);
    }

    @Rule(salience = 10)
    @Where(value = { "$f.x < $f.w1 && $f.z > 0 && $f.isNeighborsFilled(U) && !$f.isNeighborsFilled(E)" })
    public void block_is_visible_e_empty(BlockFact $f, RhsContext ctx) {
        $f.addProp(VISIBLE);
    }

    //
    // Object based on the material flag.
    //

    @Rule(salience = 100000)
    @Where(value = { "$f.isProp(EMPTY)" })
    public void object_set_block_on_empty(BlockFact $f, RhsContext ctx) {
        $f.setObject(block);
    }

    @Rule(salience = 100000)
    @Where(value = { "$f.isProp(FILLED)" })
    public void object_set_block_on_filled(BlockFact $f, RhsContext ctx) {
        $f.setObject(block);
    }

    @Rule(salience = 100000)
    @Where(value = { "$f.isProp(LIQUID)" })
    public void object_set_block_on_liquid(BlockFact $f, RhsContext ctx) {
        $f.setObject(water);
    }

    //
    // Map edge is always not a ramp.
    //

    @Rule(salience = 1_000)
    @Where(value = { "$f.isProp(FILLED) && $f.x == 0 && $f.y == 0" })
    public void object_set_block_on_map_edge_up_top_left_corner(BlockFact $f, RhsContext ctx) {
        $f.setObject(block);
    }

    @Rule(salience = 1_000)
    @Where(value = { "$f.isProp(FILLED) && $f.x == $f.w && $f.y == 0" })
    public void object_set_block_on_map_edge_up_top_right_corner(BlockFact $f, RhsContext ctx) {
        $f.setObject(block);
    }

    @Rule(salience = 1_000)
    @Where(value = { "$f.isProp(FILLED) && $f.x == 0 && $f.y == $f.h" })
    public void object_set_block_on_map_edge_up_bottom_left_corner(BlockFact $f, RhsContext ctx) {
        $f.setObject(block);
    }

    @Rule(salience = 1_000)
    @Where(value = { "$f.isProp(FILLED) && $f.x == $f.w && $f.y == $f.h" })
    public void object_set_block_on_map_edge_up_bottom_right_corner(BlockFact $f, RhsContext ctx) {
        $f.setObject(block);
    }

    //
    // Ramps based on neighbors.
    //

    @Rule(salience = 1_000)
    @Where(value = { "$f.z > 0 && $f.isProp(FILLED) && $f.isNotEdge() && !$f.isNeighborsFilled(U) && $f.isNeighborsSameLevelPerpExist() && $f.isNeighborsSameLevelPerpEmpty()" })
    public void object_set_ramp_single_on_neighbors_same_level_empty(BlockFact $f, RhsContext ctx) {
        $f.setObject(ramp_single);
        $f.addProp(RAMP);
    }

    @Rule(salience = 1_000)
    @Where(value = { "$f.z > 0 && $f.isProp(FILLED) && $f.isNotEdge() && !$f.isNeighborsFilled(U) && $f.isNeighborsSameLevelExist() && $f.isNeighborsSameLevelFilled()" })
    public void block_set_block_on_neighbors_same_level_filled(BlockFact $f, RhsContext ctx) {
        $f.setObject(block);
    }

    @Rule(salience = 1_000)
    @Where(value = { "$f.z > 0 && $f.isProp(FILLED) && $f.isNotEdge() && !$f.isNeighborsFilled(U) && $f.isNeighborsEmpty(E, S, W) && $f.isNeighborsFilled(N)" })
    public void ramp_tri_s(BlockFact $f, RhsContext ctx) {
        $f.setObject(ramp_tri_s);
        $f.addProp(RAMP);
    }

    @Rule(salience = 1_000)
    @Where(value = { "$f.z > 0 && $f.isProp(FILLED) && $f.isNotEdge() && !$f.isNeighborsFilled(U) && $f.isNeighborsEmpty(N, S, W) && $f.isNeighborsFilled(E)" })
    public void ramp_tri_w(BlockFact $f, RhsContext ctx) {
        $f.setObject(ramp_tri_w);
        $f.addProp(RAMP);
    }

    @Rule(salience = 1_000)
    @Where(value = { "$f.z > 0 && $f.isProp(FILLED) && $f.isNotEdge() && !$f.isNeighborsFilled(U) && $f.isNeighborsEmpty(N, E, W) && $f.isNeighborsFilled(S)" })
    public void ramp_tri_n(BlockFact $f, RhsContext ctx) {
        $f.setObject(ramp_tri_n);
        $f.addProp(RAMP);
    }

    @Rule(salience = 1_000)
    @Where(value = { "$f.z > 0 && $f.isProp(FILLED) && $f.isNotEdge() && !$f.isNeighborsFilled(U) && $f.isNeighborsEmpty(N, E, S) && $f.isNeighborsFilled(W)" })
    public void ramp_tri_e(BlockFact $f, RhsContext ctx) {
        $f.setObject(ramp_tri_e);
        $f.addProp(RAMP);
    }

    @Rule(salience = 1_000)
    @Where(value = { "$f.z > 0 && $f.isProp(FILLED) && $f.isNotEdge() && !$f.isNeighborsFilled(U) && $f.isNeighborsEmpty(N, SE, W) && $f.isNeighborsFilled(E, S)" })
    public void ramp_corner_nw(BlockFact $f, RhsContext ctx) {
        $f.setObject(ramp_corner_nw);
        $f.addProp(RAMP);
    }

    @Rule(salience = 1_000)
    @Where(value = { "$f.z > 0 && $f.isProp(FILLED) && $f.isNotEdge() && !$f.isNeighborsFilled(U) && $f.isNeighborsEmpty(N, E, SW) && $f.isNeighborsFilled(W, S)" })
    public void ramp_corner_ne(BlockFact $f, RhsContext ctx) {
        $f.setObject(ramp_corner_ne);
        $f.addProp(RAMP);
    }

    @Rule(salience = 1_000)
    @Where(value = { "$f.z > 0 && $f.isProp(FILLED) && $f.isNotEdge() && !$f.isNeighborsFilled(U) && $f.isNeighborsEmpty(NE, S, W) && $f.isNeighborsFilled(N, E)" })
    public void ramp_corner_sw(BlockFact $f, RhsContext ctx) {
        $f.setObject(ramp_corner_sw);
        $f.addProp(RAMP);
    }

    @Rule(salience = 1_000)
    @Where(value = { "$f.z > 0 && $f.isProp(FILLED) && $f.isNotEdge() && !$f.isNeighborsFilled(U) && $f.isNeighborsEmpty(S, NW, E) && $f.isNeighborsFilled(N, W)" })
    public void ramp_corner_se(BlockFact $f, RhsContext ctx) {
        $f.setObject(ramp_corner_se);
        $f.addProp(RAMP);
    }

    //
    // ramp_perp
    //
    
    @Rule(salience = 1_000)
    @Where(value = { "$f.z > 0 && $f.isProp(FILLED) && $f.isNotEdge() && !$f.isNeighborsFilled(U) && $f.x == 0 && $f.isNeighborsEmpty(S) && $f.isNeighborsFilled(N, E)" })
    public void ramp_perp_s_left_edge(BlockFact $f, RhsContext ctx) {
        $f.setObject(ramp_perp_s);
        $f.addProp(RAMP);
    }

    @Rule(salience = 1_000)
    @Where(value = { "$f.z > 0 && $f.isProp(FILLED) && $f.isNotEdge() && !$f.isNeighborsFilled(U) && $f.x == $f.w1 && $f.isNeighborsEmpty(S) && $f.isNeighborsFilled(N, W)" })
    public void ramp_perp_s_right_edge(BlockFact $f, RhsContext ctx) {
        $f.setObject(ramp_perp_s);
        $f.addProp(RAMP);
    }

    @Rule(salience = 1_000)
    @Where(value = { "$f.z > 0 && $f.isProp(FILLED) && $f.isNotEdge() && !$f.isNeighborsFilled(U) && $f.isNeighborsEmpty(S) && $f.isNeighborsFilled(N, E, W)" })
    public void ramp_perp_s(BlockFact $f, RhsContext ctx) {
        $f.setObject(ramp_perp_s);
        $f.addProp(RAMP);
    }

    @Rule(salience = 1_000)
    @Where(value = { "$f.z > 0 && $f.isProp(FILLED) && $f.isNotEdge() && !$f.isNeighborsFilled(U) && $f.y == 0 && $f.isNeighborsEmpty(E) && $f.isNeighborsFilled(S, W)" })
    public void ramp_perp_e_top_edge(BlockFact $f, RhsContext ctx) {
        $f.setObject(ramp_perp_e);
        $f.addProp(RAMP);
    }

    @Rule(salience = 1_000)
    @Where(value = { "$f.z > 0 && $f.isProp(FILLED) && $f.isNotEdge() && !$f.isNeighborsFilled(U) && $f.y == $f.h1 && $f.isNeighborsEmpty(E) && $f.isNeighborsFilled(N, W)" })
    public void ramp_perp_e_bottom_edge(BlockFact $f, RhsContext ctx) {
        $f.setObject(ramp_perp_e);
        $f.addProp(RAMP);
    }

    @Rule(salience = 1_000)
    @Where(value = { "$f.z > 0 && $f.isProp(FILLED) && $f.isNotEdge() && !$f.isNeighborsFilled(U) && $f.isNeighborsEmpty(E) && $f.isNeighborsFilled(N, S, W)" })
    public void ramp_perp_e(BlockFact $f, RhsContext ctx) {
        $f.setObject(ramp_perp_e);
        $f.addProp(RAMP);
    }

    @Rule(salience = 1_000)
    @Where(value = { "$f.z > 0 && $f.isProp(FILLED) && $f.isNotEdge() && !$f.isNeighborsFilled(U) && $f.x == 0 && $f.isNeighborsEmpty(N) && $f.isNeighborsFilled(E, S)" })
    public void ramp_perp_n_left_edge(BlockFact $f, RhsContext ctx) {
        $f.setObject(ramp_perp_n);
        $f.addProp(RAMP);
    }

    @Rule(salience = 1_000)
    @Where(value = { "$f.z > 0 && $f.isProp(FILLED) && $f.isNotEdge() && !$f.isNeighborsFilled(U) && $f.x == $f.w1 && $f.isNeighborsEmpty(N) && $f.isNeighborsFilled(S, W)" })
    public void ramp_perp_n_right_edge(BlockFact $f, RhsContext ctx) {
        $f.setObject(ramp_perp_n);
        $f.addProp(RAMP);
    }

    @Rule(salience = 1_000)
    @Where(value = { "$f.z > 0 && $f.isProp(FILLED) && $f.isNotEdge() && !$f.isNeighborsFilled(U) && $f.isNeighborsEmpty(N) && $f.isNeighborsFilled(E, S, W)" })
    public void ramp_perp_n(BlockFact $f, RhsContext ctx) {
        $f.setObject(ramp_perp_n);
        $f.addProp(RAMP);
    }

    @Rule(salience = 1_000)
    @Where(value = { "$f.z > 0 && $f.isProp(FILLED) && $f.isNotEdge() && !$f.isNeighborsFilled(U) && $f.y == 0 && $f.isNeighborsEmpty(W) && $f.isNeighborsFilled(E, S)" })
    public void ramp_perp_w_top_edge(BlockFact $f, RhsContext ctx) {
        $f.setObject(ramp_perp_w);
        $f.addProp(RAMP);
    }

    @Rule(salience = 1_000)
    @Where(value = { "$f.z > 0 && $f.isProp(FILLED) && $f.isNotEdge() && !$f.isNeighborsFilled(U) && $f.y == $f.h1 && $f.isNeighborsEmpty(W) && $f.isNeighborsFilled(N, E)" })
    public void ramp_perp_w_bottom_edge(BlockFact $f, RhsContext ctx) {
        $f.setObject(ramp_perp_w);
        $f.addProp(RAMP);
    }

    @Rule(salience = 1_000)
    @Where(value = { "$f.z > 0 && $f.isProp(FILLED) && $f.isNotEdge() && !$f.isNeighborsFilled(U) && $f.isNeighborsEmpty(W) && $f.isNeighborsFilled(N, E, S)" })
    public void ramp_perp_w(BlockFact $f, RhsContext ctx) {
        $f.setObject(ramp_perp_w);
        $f.addProp(RAMP);
    }

    //
    // ramp_edge_out
    //
    
    @Rule(salience = 1_000)
    @Where(value = { "$f.z > 0 && $f.isProp(FILLED) && $f.isNotEdge() && !$f.isNeighborsFilled(U) && $f.isNeighborsEmpty(E, S) && $f.isNeighborsFilled(NW, N, W)" })
    public void ramp_edge_out_se(BlockFact $f, RhsContext ctx) {
        $f.setObject(ramp_edge_out_se);
        $f.addProp(RAMP);
    }

    @Rule(salience = 1_000)
    @Where(value = { "$f.z > 0 && $f.isProp(FILLED) && $f.isNotEdge() && !$f.isNeighborsFilled(U) && $f.isNeighborsEmpty(N, W) && $f.isNeighborsFilled(SE, E, S)" })
    public void ramp_edge_out_nw(BlockFact $f, RhsContext ctx) {
        $f.setObject(ramp_edge_out_nw);
        $f.addProp(RAMP);
    }

    @Rule(salience = 1_000)
    @Where(value = { "$f.z > 0 && $f.isProp(FILLED) && $f.isNotEdge() && !$f.isNeighborsFilled(U) && $f.isNeighborsEmpty(S, W) && $f.isNeighborsFilled(NE, N, E)" })
    public void ramp_edge_out_sw(BlockFact $f, RhsContext ctx) {
        $f.setObject(ramp_edge_out_sw);
        $f.addProp(RAMP);
    }

    @Rule(salience = 1_000)
    @Where(value = { "$f.z > 0 && $f.isProp(FILLED) && $f.isNotEdge() && !$f.isNeighborsFilled(U) && $f.isNeighborsEmpty(N, E) && $f.isNeighborsFilled(SW, S, W)" })
    public void ramp_edge_out_ne(BlockFact $f, RhsContext ctx) {
        $f.setObject(ramp_edge_out_ne);
        $f.addProp(RAMP);
    }

    @Rule(salience = 1_000)
    @Where(value = { "$f.z > 0 && $f.isProp(FILLED) && $f.isNotEdge() && !$f.isNeighborsFilled(U) && $f.isNeighborsEmpty(SE) && $f.isNeighborsFilled(N, NE, E, S, SW, W)" })
    public void ramp_edge_in_se(BlockFact $f, RhsContext ctx) {
        $f.setObject(ramp_edge_in_se);
        $f.addProp(RAMP);
    }

    @Rule(salience = 1_000)
    @Where(value = { "$f.z > 0 && $f.isProp(FILLED) && $f.isNotEdge() && !$f.isNeighborsFilled(U) && $f.isNeighborsEmpty(NW) && $f.isNeighborsFilled(N, NE, E, SE, S, SW, W)" })
    public void ramp_edge_in_nw(BlockFact $f, RhsContext ctx) {
        $f.setObject(ramp_edge_in_nw);
        $f.addProp(RAMP);
    }

    @Rule(salience = 1_000)
    @Where(value = { "$f.z > 0 && $f.isProp(FILLED) && $f.isNotEdge() && !$f.isNeighborsFilled(U) && $f.isNeighborsEmpty(SW) && $f.isNeighborsFilled(N, NE, E, SE, S, W, NW)" })
    public void ramp_edge_in_sw(BlockFact $f, RhsContext ctx) {
        $f.setObject(ramp_edge_in_sw);
        $f.addProp(RAMP);
    }

    @Rule(salience = 1_000)
    @Where(value = { "$f.z > 0 && $f.isProp(FILLED) && $f.isNotEdge() && !$f.isNeighborsFilled(U) && $f.isNeighborsEmpty(NE) && $f.isNeighborsFilled(N, E, SE, S, SW, W, NW)" })
    public void ramp_edge_in_ne(BlockFact $f, RhsContext ctx) {
        $f.setObject(ramp_edge_in_ne);
        $f.addProp(RAMP);
    }

    @Rule(salience = 1_000)
    @Where(value = { "$f.z > 0 && $f.isProp(FILLED) && $f.isNotEdge() && !$f.isNeighborsFilled(U) && $f.isNeighborsEmpty(NE, SW) && $f.isNeighborsFilled(N, E, SE, S, W, NW)" })
    public void ramp_edge_two_ne(BlockFact $f, RhsContext ctx) {
        $f.setObject(ramp_two_ne);
        $f.addProp(RAMP);
    }

    @Rule(salience = 1_000)
    @Where(value = { "$f.z > 0 && $f.isProp(FILLED) && $f.isNotEdge() && !$f.isNeighborsFilled(U) && $f.isNeighborsEmpty(SE, NW) && $f.isNeighborsFilled(N, NE, E, W, S, SW, W)" })
    public void ramp_edge_two_se(BlockFact $f, RhsContext ctx) {
        $f.setObject(ramp_two_se);
        $f.addProp(RAMP);
    }

    //
    // Have roof based on the upper neighbor.
    //

    @Rule(salience = 100)
    @Where(value = { "$f.isNeighborsExist(U) && $f.isNeighborsFilled(U)" })
    public void have_foor_up(BlockFact $f, RhsContext ctx) {
        $f.addProp(HAVE_CEILING);
    }

    @Rule(salience = 100)
    @Where(value = { "$f.isNeighborsExist(D) && $f.isNeighborsFilled(D)" })
    public void have_floor_down(BlockFact $f, RhsContext ctx) {
        $f.addProp(HAVE_FLOOR);
    }

}
