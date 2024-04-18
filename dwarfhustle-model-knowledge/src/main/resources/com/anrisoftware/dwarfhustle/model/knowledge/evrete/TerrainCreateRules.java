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
    
    long ramp_nesw;
    
    long ramp_edge;
    
    @PhaseListener(Phase.FIRE)
    public void initResources(Environment env) {
        this.materialOxygen = env.get(TerrainCreateKnowledge.MATERIAL_OXYGEN_NAME);
        this.solids = env.get(TerrainCreateKnowledge.MATERIALS_SOLIDS_NAME);
        this.liquids = env.get(TerrainCreateKnowledge.MATERIALS_LIQUIDS_NAME);
        this.gases = env.get(TerrainCreateKnowledge.MATERIALS_GASES_NAME);
        this.block = env.get(TerrainCreateKnowledge.OBJECT_BLOCK_NAME);
        this.ramp_nesw = env.get(TerrainCreateKnowledge.OBJECT_RAMP_NESW_NAME);
        this.ramp_edge = env.get(TerrainCreateKnowledge.OBJECT_RAMP_EDGE_NAME);
    }

    @Rule(salience = 1)
    @Where("$mid == 0 && $block.material == -1")
    public void material_empty_set_oxygen(Long $mid, MapBlock $block, MapBlock[] $neighbors, RhsContext ctx) {
        $block.setMaterialRid(materialOxygen);
        ctx.update($block);
    }

    @Rule(salience = 2)
    @Where("$mid > 0 && $block.material == -1")
    public void material_set_rid(long $mid, MapBlock $block, MapBlock[] $neighbors, RhsContext ctx) {
        $block.setMaterialRid($mid);
        ctx.update($block);
    }

    @Rule(salience = 3)
    @Where(methods = { //
            @MethodPredicate( //
                    method = "material_gas_test", //
                    args = { "$block.materialRid" }) //
    })
    public void material_gases_set_empty(MapBlock $block, MapBlock[] $neighbors, RhsContext ctx) {
        $block.setEmpty(true);
    }

    @Rule(salience = 4)
    @Where(methods = { //
            @MethodPredicate( //
                    method = "material_solid_test", //
                    args = { "$block.materialRid" }) //
    })
    public void material_solids_set_filled(long $mid, MapBlock $block, MapBlock[] $neighbors, RhsContext ctx) {
        $block.setFilled(true);
    }

    @Rule(salience = 4)
    @Where(methods = { //
            @MethodPredicate( //
                    method = "material_solid_test", //
                    args = { "$block.materialRid" }) //
    })
    public void material_liquid_test(long $mid, MapBlock $block, MapBlock[] $neighbors, RhsContext ctx) {
        $block.setFilled(true);
    }

    @Rule(salience = 10)
    @Where(methods = { //
            @MethodPredicate( //
                    method = "object_set_neighbors_test", //
                    args = { "$block", "$neighbors" }) //
    })
    public void block_set_ramp_on_neighbors_empty(MapBlock $block, MapBlock[] $neighbors, RhsContext ctx) {
        $block.setObjectRid(ramp_nesw);
    }

    public boolean object_set_neighbors_test(MapBlock block, MapBlock[] neighbors) {
        boolean ramp = false;
        if (neighbors[NeighboringDir.N.ordinal()] != null) {
            ramp |= neighbors[NeighboringDir.N.ordinal()].isFilled();
        }
        if (neighbors[NeighboringDir.E.ordinal()] != null) {
            ramp |= neighbors[NeighboringDir.E.ordinal()].isFilled();
        }
        if (neighbors[NeighboringDir.W.ordinal()] != null) {
            ramp |= neighbors[NeighboringDir.W.ordinal()].isFilled();
        }
        if (neighbors[NeighboringDir.S.ordinal()] != null) {
            ramp |= neighbors[NeighboringDir.S.ordinal()].isFilled();
        }
        return ramp;
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
