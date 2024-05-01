package com.anrisoftware.dwarfhustle.model.knowledge.evrete;

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

    long ramp_tri;

    long ramp_single;

    long ramp_perp;

    long ramp_edge_in;

    long ramp_edge_out;

    long ramp_corner;

    @PhaseListener(Phase.CREATE)
    public void initResources(Environment env) {
        MutableIntObjectMap<MutableLongList> materials = env.get(TerrainCreateKnowledge.MATERIALS_NAME);
        this.materialOxygen = materials.get(TerrainCreateKnowledge.MATERIAL_OXYGEN_NAME).get(0);
        this.solids = materials.get(TerrainCreateKnowledge.MATERIALS_SOLIDS_NAME);
        this.liquids = materials.get(TerrainCreateKnowledge.MATERIALS_LIQUIDS_NAME);
        this.gases = materials.get(TerrainCreateKnowledge.MATERIALS_GASES_NAME);
        MutableIntLongMap objects = env.get(TerrainCreateKnowledge.OBJECTS_NAME);
        this.block = objects.get(TerrainCreateKnowledge.OBJECT_BLOCK_NAME);
        this.ramp_tri = objects.get(TerrainCreateKnowledge.OBJECT_RAMP_TRI_NAME);
        this.ramp_single = objects.get(TerrainCreateKnowledge.OBJECT_RAMP_SINGLE_NAME);
        this.ramp_perp = objects.get(TerrainCreateKnowledge.OBJECT_RAMP_PERP_NAME);
        this.ramp_edge_in = objects.get(TerrainCreateKnowledge.OBJECT_RAMP_EDGE_IN_NAME);
        this.ramp_edge_out = objects.get(TerrainCreateKnowledge.OBJECT_RAMP_EDGE_OUT_NAME);
        this.ramp_corner = objects.get(TerrainCreateKnowledge.OBJECT_RAMP_CORNER_NAME);
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
    @Where(value = { "$f.block.isNeighborUpFilled($f.neighbors)" })
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
    @Where(value = { "$f.block.filled", "$f.block.isNeighborsEmpty($f.neighbors)" })
    public void object_set_ramp_single_on_neighbors_empty(TerrainFact $f, RhsContext ctx) {
        $f.block.setObjectRid(ramp_single);
        $f.block.setRamp(true);
    }

    @Rule(salience = 10)
    @Where(value = { "!$f.block.empty", "$f.block.isNeighborsFilled($f.neighbors)" })
    public void block_set_block_on_neighbors_filled(TerrainFact $f, RhsContext ctx) {
        $f.block.setObjectRid(block);
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
