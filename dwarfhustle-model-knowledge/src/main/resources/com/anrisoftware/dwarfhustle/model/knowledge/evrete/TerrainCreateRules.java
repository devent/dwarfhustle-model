package com.anrisoftware.dwarfhustle.model.knowledge.evrete;

import java.util.Arrays;
import java.util.List;

import org.eclipse.collections.api.list.primitive.MutableLongList;
import org.eclipse.collections.api.map.primitive.MutableIntLongMap;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.evrete.api.Environment;
import org.evrete.api.RhsContext;
import org.evrete.dsl.Phase;
import org.evrete.dsl.annotation.*;

import com.anrisoftware.dwarfhustle.model.api.objects.*;

public class TerrainCreateRules {

    long materialOxygen;

    MutableLongList solids;

    MutableLongList liquids;

    MutableLongList gases;

    long block;

    long ramp_tri_n;

    long ramp_tri_e;

    long ramp_tri_s;

    long ramp_tri_w;

    long ramp_single;

    long ramp_perp_n;

    long ramp_perp_e;

    long ramp_perp_s;

    long ramp_perp_w;

    long ramp_edge_in_ne;

    long ramp_edge_in_nw;

    long ramp_edge_in_se;

    long ramp_edge_in_sw;

    long ramp_edge_out_ne;

    long ramp_edge_out_nw;

    long ramp_edge_out_se;

    long ramp_edge_out_sw;

    long ramp_corner_ne;

    long ramp_corner_nw;

    long ramp_corner_se;

    long ramp_corner_sw;

    @PhaseListener(Phase.CREATE)
    public void initResources(Environment env) {
        MutableIntObjectMap<MutableLongList> materials = env.get(TerrainCreateKnowledge.MATERIALS_NAME);
        this.materialOxygen = materials.get(TerrainCreateKnowledge.MATERIAL_OXYGEN_NAME).get(0);
        this.solids = materials.get(TerrainCreateKnowledge.MATERIALS_SOLIDS_NAME);
        this.liquids = materials.get(TerrainCreateKnowledge.MATERIALS_LIQUIDS_NAME);
        this.gases = materials.get(TerrainCreateKnowledge.MATERIALS_GASES_NAME);
        MutableIntLongMap objects = env.get(TerrainCreateKnowledge.OBJECTS_NAME);
        this.block = objects.get(TerrainCreateKnowledge.OBJECT_BLOCK_NAME);
        this.ramp_tri_n = objects.get(TerrainCreateKnowledge.OBJECT_RAMP_TRI_N_NAME);
        this.ramp_tri_e = objects.get(TerrainCreateKnowledge.OBJECT_RAMP_TRI_E_NAME);
        this.ramp_tri_s = objects.get(TerrainCreateKnowledge.OBJECT_RAMP_TRI_S_NAME);
        this.ramp_tri_w = objects.get(TerrainCreateKnowledge.OBJECT_RAMP_TRI_W_NAME);
        this.ramp_single = objects.get(TerrainCreateKnowledge.OBJECT_RAMP_SINGLE_NAME);
        this.ramp_perp_n = objects.get(TerrainCreateKnowledge.OBJECT_RAMP_PERP_N_NAME);
        this.ramp_perp_e = objects.get(TerrainCreateKnowledge.OBJECT_RAMP_PERP_E_NAME);
        this.ramp_perp_s = objects.get(TerrainCreateKnowledge.OBJECT_RAMP_PERP_S_NAME);
        this.ramp_perp_w = objects.get(TerrainCreateKnowledge.OBJECT_RAMP_PERP_W_NAME);
        this.ramp_edge_in_ne = objects.get(TerrainCreateKnowledge.OBJECT_RAMP_EDGE_IN_NE_NAME);
        this.ramp_edge_in_nw = objects.get(TerrainCreateKnowledge.OBJECT_RAMP_EDGE_IN_NW_NAME);
        this.ramp_edge_in_se = objects.get(TerrainCreateKnowledge.OBJECT_RAMP_EDGE_IN_SE_NAME);
        this.ramp_edge_in_sw = objects.get(TerrainCreateKnowledge.OBJECT_RAMP_EDGE_IN_SW_NAME);
        this.ramp_edge_out_ne = objects.get(TerrainCreateKnowledge.OBJECT_RAMP_EDGE_OUT_NE_NAME);
        this.ramp_edge_out_nw = objects.get(TerrainCreateKnowledge.OBJECT_RAMP_EDGE_OUT_NW_NAME);
        this.ramp_edge_out_se = objects.get(TerrainCreateKnowledge.OBJECT_RAMP_EDGE_OUT_SE_NAME);
        this.ramp_edge_out_sw = objects.get(TerrainCreateKnowledge.OBJECT_RAMP_EDGE_OUT_SW_NAME);
        this.ramp_corner_ne = objects.get(TerrainCreateKnowledge.OBJECT_RAMP_CORNER_NE_NAME);
        this.ramp_corner_nw = objects.get(TerrainCreateKnowledge.OBJECT_RAMP_CORNER_NW_NAME);
        this.ramp_corner_se = objects.get(TerrainCreateKnowledge.OBJECT_RAMP_CORNER_SE_NAME);
        this.ramp_corner_sw = objects.get(TerrainCreateKnowledge.OBJECT_RAMP_CORNER_SW_NAME);
    }

    //
    // Set block material.
    //

    @Rule(salience = 1000)
    @Where("$f.mid == 0 && $f.block.material == -1")
    public void material_empty_set_oxygen(TerrainFact $f, RhsContext ctx) {
        $f.block.setMaterialRid(materialOxygen);
        ctx.update($f);
    }

    @Rule(salience = 1000)
    @Where("$f.mid > 0 && $f.block.material == -1")
    public void material_set_rid(TerrainFact $f, RhsContext ctx) {
        $f.block.setMaterialRid($f.mid);
        ctx.update($f);
    }

    //
    // Set block properties.
    //

    @Rule(salience = 100)
    @Where(value = "$f.block.p.bits == 0", //
            methods = { //
                    @MethodPredicate( //
                            method = "material_gas_test", //
                            args = { "$f.block.materialRid" }) //
            })
    public void material_gases_set_empty(TerrainFact $f, RhsContext ctx) {
        $f.block.setVisible(true);
        $f.block.setEmpty(true);
        ctx.update($f);
    }

    @Rule(salience = 100)
    @Where(value = "$f.block.p.bits == 0", //
            methods = { //
                    @MethodPredicate( //
                            method = "material_solid_test", //
                            args = { "$f.block.materialRid" }) //
            })
    public void material_solids_set_filled(TerrainFact $f, RhsContext ctx) {
        $f.block.setVisible(true);
        $f.block.setFilled(true);
        ctx.update($f);
    }

    @Rule(salience = 100)
    @Where(value = "$f.block.p.bits == 0", //
            methods = { //
                    @MethodPredicate( //
                            method = "material_liquid_test", //
                            args = { "$f.block.materialRid" }) //
            })
    public void material_liquids_set_liquid(TerrainFact $f, RhsContext ctx) {
        $f.block.setVisible(true);
        $f.block.setLiquid(true);
        ctx.update($f);
    }

    //
    // Block hidden or visible based on neighbors.
    //

    @Rule(salience = 10)
    @Where(value = { "$f.block.isNeighborsFilled($f.neighbors, U)" })
    public void block_is_hidden(TerrainFact $f, RhsContext ctx) {
        $f.block.setHidden(true);
    }

    //
    // Object type based on block status and neighbors.
    //

    @Rule(salience = 10)
    @Where(value = { "$f.block.empty || $f.block.liquid" })
    public void object_set_block_on_empty_liquid(TerrainFact $f, RhsContext ctx) {
        $f.block.setObjectRid(block);
    }

    @Rule(salience = 10)
    @Where(value = { "$f.block.filled", "!$f.block.isNeighborsSameLevelExist($f.neighbors)" })
    public void object_set_block_on_map_edge(TerrainFact $f, RhsContext ctx) {
        $f.block.setObjectRid(block);
    }

    @Rule(salience = 10)
    @Where(value = { "$f.block.filled", "$f.block.isNeighborsSameLevelExist($f.neighbors)",
            "$f.block.isNeighborsSameLevelEmpty($f.neighbors)" })
    public void object_set_ramp_single_on_neighbors_same_level_empty(TerrainFact $f, RhsContext ctx) {
        $f.block.setObjectRid(ramp_single);
        $f.block.setRamp(true);
    }

    @Rule(salience = 10)
    @Where(value = { "!$f.block.empty", "$f.block.isNeighborsSameLevelFilled($f.neighbors)" })
    public void block_set_block_on_neighbors_same_level_filled(TerrainFact $f, RhsContext ctx) {
        $f.block.setObjectRid(block);
    }

    @Rule(salience = 10)
    @Where(value = { "$f.block.filled", "$f.block.isNeighborsExist($f.neighbors, N, E, S, W)",
            "$f.block.isNeighborsEmpty($f.neighbors, E, S, W)", "$f.block.isNeighborsFilled($f.neighbors, N)" })
    public void ramp_tri_s(TerrainFact $f, RhsContext ctx) {
        $f.block.setObjectRid(ramp_tri_s);
        $f.block.setRamp(true);
    }

    @Rule(salience = 10)
    @Where(value = { "$f.block.filled", "$f.block.isNeighborsExist($f.neighbors, N, E, S, W)",
            "$f.block.isNeighborsEmpty($f.neighbors, N, S, W)", "$f.block.isNeighborsFilled($f.neighbors, E)" })
    public void ramp_tri_w(TerrainFact $f, RhsContext ctx) {
        $f.block.setObjectRid(ramp_tri_w);
        $f.block.setRamp(true);
    }

    @Rule(salience = 10)
    @Where(value = { "$f.block.filled", "$f.block.isNeighborsExist($f.neighbors, N, E, S, W)",
            "$f.block.isNeighborsEmpty($f.neighbors, N, E, W)", "$f.block.isNeighborsFilled($f.neighbors, S)" })
    public void ramp_tri_n(TerrainFact $f, RhsContext ctx) {
        $f.block.setObjectRid(ramp_tri_n);
        $f.block.setRamp(true);
    }

    @Rule(salience = 10)
    @Where(value = { "$f.block.filled", "$f.block.isNeighborsExist($f.neighbors, N, E, S, W)",
            "$f.block.isNeighborsEmpty($f.neighbors, N, E, S)", "$f.block.isNeighborsFilled($f.neighbors, W)" })
    public void ramp_tri_e(TerrainFact $f, RhsContext ctx) {
        $f.block.setObjectRid(ramp_tri_e);
        $f.block.setRamp(true);
    }

    @Rule(salience = 10)
    @Where(value = { "$f.block.filled", "$f.block.isNeighborsExist($f.neighbors, N, SE, W, E, S)",
            "$f.block.isNeighborsEmpty($f.neighbors, N, SE, W)", "$f.block.isNeighborsFilled($f.neighbors, E, S)" })
    public void ramp_corner_nw(TerrainFact $f, RhsContext ctx) {
        $f.block.setObjectRid(ramp_corner_nw);
        $f.block.setRamp(true);
    }

    @Rule(salience = 10)
    @Where(value = { "$f.block.filled", "$f.block.isNeighborsExist($f.neighbors, N, E, SW, W, S)",
            "$f.block.isNeighborsEmpty($f.neighbors, N, E, SW)", "$f.block.isNeighborsFilled($f.neighbors, W, S)" })
    public void ramp_corner_ne(TerrainFact $f, RhsContext ctx) {
        $f.block.setObjectRid(ramp_corner_ne);
        $f.block.setRamp(true);
    }

    @Rule(salience = 10)
    @Where(value = { "$f.block.filled", "$f.block.isNeighborsExist($f.neighbors, NE, S, W, N, E)",
            "$f.block.isNeighborsEmpty($f.neighbors, NE, S, W)", "$f.block.isNeighborsFilled($f.neighbors, N, E)" })
    public void ramp_corner_sw(TerrainFact $f, RhsContext ctx) {
        $f.block.setObjectRid(ramp_corner_sw);
        $f.block.setRamp(true);
    }

    @Rule(salience = 10)
    @Where(value = { "$f.block.filled", "$f.block.isNeighborsExist($f.neighbors, S, NW, E, W)",
            "$f.block.isNeighborsEmpty($f.neighbors, S, NW, E)", "$f.block.isNeighborsFilled($f.neighbors, N, W)" })
    public void ramp_corner_se(TerrainFact $f, RhsContext ctx) {
        $f.block.setObjectRid(ramp_corner_se);
        $f.block.setRamp(true);
    }

    @Rule(salience = 10)
    @Where(value = { "$f.block.filled", "$f.block.isNeighborsExist($f.neighbors, S, N, E, W)",
            "$f.block.isNeighborsEmpty($f.neighbors, S)", "$f.block.isNeighborsFilled($f.neighbors, N)",
            "$f.block.isNeighborsRamp($f.neighbors, E, W)" })
    public void ramp_perp_s(TerrainFact $f, RhsContext ctx) {
        $f.block.setObjectRid(ramp_perp_s);
        $f.block.setRamp(true);
    }

    @Rule(salience = 10)
    @Where(value = { "$f.block.filled", "$f.block.isNeighborsExist($f.neighbors, E, W, N, S)",
            "$f.block.isNeighborsEmpty($f.neighbors, E)", "$f.block.isNeighborsFilled($f.neighbors, W)",
            "$f.block.isNeighborsRamp($f.neighbors, N, S)" })
    public void ramp_perp_e(TerrainFact $f, RhsContext ctx) {
        $f.block.setObjectRid(ramp_perp_e);
        $f.block.setRamp(true);
    }

    @Rule(salience = 10)
    @Where(value = { "$f.block.filled", "$f.block.isNeighborsExist($f.neighbors, N, S, E, W)",
            "$f.block.isNeighborsEmpty($f.neighbors, N)", "$f.block.isNeighborsFilled($f.neighbors, S)",
            "$f.block.isNeighborsRamp($f.neighbors, E, W)" })
    public void ramp_perp_n(TerrainFact $f, RhsContext ctx) {
        $f.block.setObjectRid(ramp_perp_n);
        $f.block.setRamp(true);
    }

    @Rule(salience = 10)
    @Where(value = { "$f.block.filled", "$f.block.isNeighborsExist($f.neighbors, W, E, N, S)",
            "$f.block.isNeighborsEmpty($f.neighbors, W)", "$f.block.isNeighborsFilled($f.neighbors, E)",
            "$f.block.isNeighborsRamp($f.neighbors, N, S)" })
    public void ramp_perp_w(TerrainFact $f, RhsContext ctx) {
        $f.block.setObjectRid(ramp_perp_w);
        $f.block.setRamp(true);
    }

    @Rule(salience = 10)
    @Where(value = { "$f.block.filled", "$f.block.isNeighborsExist($f.neighbors, E, S, NW, N, W)",
            "$f.block.isNeighborsEmpty($f.neighbors, E, S)", "$f.block.isNeighborsFilled($f.neighbors, NW)",
            "$f.block.isNeighborsRamp($f.neighbors, N, W)" })
    public void ramp_edge_out_se(TerrainFact $f, RhsContext ctx) {
        $f.block.setObjectRid(ramp_edge_out_se);
        $f.block.setRamp(true);
    }

    @Rule(salience = 10)
    @Where(value = { "$f.block.filled", "$f.block.isNeighborsExist($f.neighbors, N, W, SE, E, S)",
            "$f.block.isNeighborsEmpty($f.neighbors, N, W)", "$f.block.isNeighborsFilled($f.neighbors, SE)",
            "$f.block.isNeighborsRamp($f.neighbors, E, S)" })
    public void ramp_edge_out_nw(TerrainFact $f, RhsContext ctx) {
        $f.block.setObjectRid(ramp_edge_out_nw);
        $f.block.setRamp(true);
    }

    @Rule(salience = 10)
    @Where(value = { "$f.block.filled", "$f.block.isNeighborsExist($f.neighbors, S, W, NE, N, E)",
            "$f.block.isNeighborsEmpty($f.neighbors, S, W)", "$f.block.isNeighborsFilled($f.neighbors, NE)",
            "$f.block.isNeighborsRamp($f.neighbors, N, E)" })
    public void ramp_edge_out_sw(TerrainFact $f, RhsContext ctx) {
        $f.block.setObjectRid(ramp_edge_out_sw);
        $f.block.setRamp(true);
    }

    @Rule(salience = 10)
    @Where(value = { "$f.block.filled", "$f.block.isNeighborsExist($f.neighbors, N, E, SW, S, W)",
            "$f.block.isNeighborsEmpty($f.neighbors, N, E)", "$f.block.isNeighborsFilled($f.neighbors, SW)",
            "$f.block.isNeighborsRamp($f.neighbors, S, W)" })
    public void ramp_edge_out_ne(TerrainFact $f, RhsContext ctx) {
        $f.block.setObjectRid(ramp_edge_out_ne);
        $f.block.setRamp(true);
    }

    @Rule(salience = 10)
    @Where(value = { "$f.block.filled", "$f.block.isNeighborsExist($f.neighbors, E, N, W, NW, SW)",
            "$f.block.isNeighborsEmpty($f.neighbors, E)", "$f.block.isNeighborsFilled($f.neighbors, N, W, NW)",
            "$f.block.isNeighborsRamp($f.neighbors, S, SW)" })
    public void ramp_edge_in_se(TerrainFact $f, RhsContext ctx) {
        $f.block.setObjectRid(ramp_edge_in_se);
        $f.block.setRamp(true);
    }

    @Rule(salience = 10)
    @Where(value = { "$f.block.filled", "$f.block.isNeighborsExist($f.neighbors, W, E, SE, S, N, NE)",
            "$f.block.isNeighborsEmpty($f.neighbors, W)", "$f.block.isNeighborsFilled($f.neighbors, E, SE, S)",
            "$f.block.isNeighborsRamp($f.neighbors, N, NE)" })
    public void ramp_edge_in_nw(TerrainFact $f, RhsContext ctx) {
        $f.block.setObjectRid(ramp_edge_in_nw);
        $f.block.setRamp(true);
    }

    @Rule(salience = 10)
    @Where(value = { "$f.block.filled", "$f.block.isNeighborsExist($f.neighbors, S, N, NE, E, W, NW)",
            "$f.block.isNeighborsEmpty($f.neighbors, S)", "$f.block.isNeighborsFilled($f.neighbors, N, NE, E)",
            "$f.block.isNeighborsRamp($f.neighbors, W, NW)" })
    public void ramp_edge_in_sw(TerrainFact $f, RhsContext ctx) {
        $f.block.setObjectRid(ramp_edge_in_sw);
        $f.block.setRamp(true);
    }

    @Rule(salience = 10)
    @Where(value = { "$f.block.filled", "$f.block.isNeighborsExist($f.neighbors, E, S, SW, W, N, NW)",
            "$f.block.isNeighborsEmpty($f.neighbors, E)", "$f.block.isNeighborsFilled($f.neighbors, S, SW, W)",
            "$f.block.isNeighborsRamp($f.neighbors, N, NW)" })
    public void ramp_edge_in_ne(TerrainFact $f, RhsContext ctx) {
        $f.block.setObjectRid(ramp_edge_in_ne);
        $f.block.setRamp(true);
    }

    public boolean material_gas_test(long mid) {
        return gases.contains(mid);
    }

    public boolean material_liquid_test(long mid) {
        return liquids.contains(mid);
    }

    public boolean material_solid_test(long mid) {
        return solids.contains(mid);
    }

}
