package com.anrisoftware.dwarfhustle.model.knowledge.evrete;

import java.util.List;

import org.evrete.api.Environment;
import org.evrete.api.RhsContext;
import org.evrete.dsl.Phase;
import org.evrete.dsl.annotation.*;

import com.anrisoftware.dwarfhustle.model.api.objects.*;

public class TerrainCreateRules {

    long materialOxygen;

    List<Long> solids;

    List<Long> liquids;

    List<Long> gases;

    long block;

    long ramp_single;

    long ramp_nesw;

    long ramp_edge;

    @PhaseListener(Phase.CREATE)
    public void initResources(Environment env) {
        this.materialOxygen = env.get(TerrainCreateKnowledge.MATERIAL_OXYGEN_NAME);
        this.solids = env.get(TerrainCreateKnowledge.MATERIALS_SOLIDS_NAME);
        this.liquids = env.get(TerrainCreateKnowledge.MATERIALS_LIQUIDS_NAME);
        this.gases = env.get(TerrainCreateKnowledge.MATERIALS_GASES_NAME);
        this.block = env.get(TerrainCreateKnowledge.OBJECT_BLOCK_NAME);
        this.ramp_single = env.get(TerrainCreateKnowledge.OBJECT_RAMP_SINGLE_NAME);
        this.ramp_nesw = env.get(TerrainCreateKnowledge.OBJECT_RAMP_NESW_NAME);
        this.ramp_edge = env.get(TerrainCreateKnowledge.OBJECT_RAMP_EDGE_NAME);
    }

    //
    // Set block material.
    //

    @Rule(salience = 1000)
    @Where("$mid == 0 && $block.material == -1")
    public void material_empty_set_oxygen(Long $mid, MapBlock $block, MapBlock[] $neighbors, RhsContext ctx) {
        $block.setMaterialRid(materialOxygen);
        ctx.update($block);
    }

    @Rule(salience = 1000)
    @Where("$mid > 0 && $block.material == -1")
    public void material_set_rid(long $mid, MapBlock $block, MapBlock[] $neighbors, RhsContext ctx) {
        $block.setMaterialRid($mid);
        ctx.update($block);
    }

    //
    // Set block properties.
    //

    @Rule(salience = 100)
    @Where(value = "$block.p.bits == 0", //
            methods = { //
                    @MethodPredicate( //
                            method = "material_gas_test", //
                            args = { "$block.materialRid" }) //
            })
    public void material_gases_set_empty(MapBlock $block, MapBlock[] $neighbors, RhsContext ctx) {
        $block.setVisible(true);
        $block.setEmpty(true);
        ctx.update($block);
    }

    @Rule(salience = 100)
    @Where(value = "$block.p.bits == 0", //
            methods = { //
                    @MethodPredicate( //
                            method = "material_solid_test", //
                            args = { "$block.materialRid" }) //
            })
    public void material_solids_set_filled(long $mid, MapBlock $block, MapBlock[] $neighbors, RhsContext ctx) {
        $block.setVisible(true);
        $block.setFilled(true);
        ctx.update($block);
    }

    @Rule(salience = 100)
    @Where(value = "$block.p.bits == 0", //
            methods = { //
                    @MethodPredicate( //
                            method = "material_liquid_test", //
                            args = { "$block.materialRid" }) //
            })
    public void material_liquids_set_liquid(long $mid, MapBlock $block, MapBlock[] $neighbors, RhsContext ctx) {
        $block.setVisible(true);
        $block.setLiquid(true);
        ctx.update($block);
    }

    //
    // Block hidden or visible based on neighbors.
    //

    @Rule(salience = 10)
    @Where(value = { "$block.isNeighborUpNotEmpty($neighbors)" })
    public void block_is_hidden(MapBlock $block, MapBlock[] $neighbors, RhsContext ctx) {
        $block.setHidden(true);
    }

    //
    // Object type based on block status and neighbors.
    //

    @Rule(salience = 10)
    @Where(value = { "$block.empty || $block.liquid" })
    public void object_set_block_on_empty_liquid(MapBlock $block, MapBlock[] $neighbors, RhsContext ctx) {
        $block.setObjectRid(block);
    }

    @Rule(salience = 10)
    @Where(value = { "$block.filled", "$block.isNeighborsEmpty($neighbors)" })
    public void object_set_ramp_single_on_neighbors_empty(MapBlock $block, MapBlock[] $neighbors, RhsContext ctx) {
        $block.setObjectRid(ramp_single);
    }

    @Rule(salience = 10)
    @Where(value = { "!$block.empty", "$block.isNeighborsFilled($neighbors)" })
    public void block_set_block_on_neighbors_filled(MapBlock $block, MapBlock[] $neighbors, RhsContext ctx) {
        $block.setObjectRid(block);
    }

    @Rule(salience = 10)
    @Where(value = { "!$block.empty", "$block.isOneNeighborPerpendicularEmpty($neighbors)",
            "$block.isNeighborsEdgeNotEmpty($neighbors)" })
    public void block_set_ramp_nesw_on_neighbors_empty(MapBlock $block, MapBlock[] $neighbors, RhsContext ctx) {
        $block.setObjectRid(ramp_nesw);
    }

    @Rule(salience = 10)
    @Where(value = { "!$block.empty", "$block.isOneNeighborEdgeEmpty($neighbors)" })
    public void block_set_ramp_edge_on_neighbors_empty(MapBlock $block, MapBlock[] $neighbors, RhsContext ctx) {
        $block.setObjectRid(ramp_edge);
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
