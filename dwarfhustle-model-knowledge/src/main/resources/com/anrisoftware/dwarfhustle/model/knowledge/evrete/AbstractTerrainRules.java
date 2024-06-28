package com.anrisoftware.dwarfhustle.model.knowledge.evrete;

import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.api.map.primitive.MutableIntIntMap;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.evrete.api.Environment;
import org.evrete.dsl.Phase;
import org.evrete.dsl.annotation.PhaseListener;

/**
 * Setups the IDs of the materials and objects.
 */
public class AbstractTerrainRules {

    int materialOxygen;

    MutableIntList solids;

    MutableIntList liquids;

    MutableIntList gases;

    int block;
    
    int water;

    int ramp_tri_n;

    int ramp_tri_e;

    int ramp_tri_s;

    int ramp_tri_w;

    int ramp_single;

    int ramp_perp_n;

    int ramp_perp_e;

    int ramp_perp_s;

    int ramp_perp_w;

    int ramp_edge_in_ne;

    int ramp_edge_in_nw;

    int ramp_edge_in_se;

    int ramp_edge_in_sw;

    int ramp_edge_out_ne;

    int ramp_edge_out_nw;

    int ramp_edge_out_se;

    int ramp_edge_out_sw;

    int ramp_corner_ne;

    int ramp_corner_nw;

    int ramp_corner_se;

    int ramp_corner_sw;    

    int ramp_two_ne;

    int ramp_two_se;    

    @PhaseListener(Phase.CREATE)
    public void initResources(Environment env) {
        MutableIntObjectMap<MutableIntList> materials = env.get(TerrainKnowledge.MATERIALS_NAME);
        this.materialOxygen = materials.get(TerrainKnowledge.MATERIAL_OXYGEN_NAME).get(0);
        this.solids = materials.get(TerrainKnowledge.MATERIALS_SOLIDS_NAME);
        this.liquids = materials.get(TerrainKnowledge.MATERIALS_LIQUIDS_NAME);
        this.gases = materials.get(TerrainKnowledge.MATERIALS_GASES_NAME);
        MutableIntIntMap objects = env.get(TerrainKnowledge.OBJECTS_NAME);
        this.block = objects.get(TerrainKnowledge.OBJECT_BLOCK_NAME);
        this.water = objects.get(TerrainKnowledge.OBJECT_WATER_NAME);
        this.ramp_tri_n = objects.get(TerrainKnowledge.OBJECT_RAMP_TRI_N_NAME);
        this.ramp_tri_e = objects.get(TerrainKnowledge.OBJECT_RAMP_TRI_E_NAME);
        this.ramp_tri_s = objects.get(TerrainKnowledge.OBJECT_RAMP_TRI_S_NAME);
        this.ramp_tri_w = objects.get(TerrainKnowledge.OBJECT_RAMP_TRI_W_NAME);
        this.ramp_single = objects.get(TerrainKnowledge.OBJECT_RAMP_SINGLE_NAME);
        this.ramp_perp_n = objects.get(TerrainKnowledge.OBJECT_RAMP_PERP_N_NAME);
        this.ramp_perp_e = objects.get(TerrainKnowledge.OBJECT_RAMP_PERP_E_NAME);
        this.ramp_perp_s = objects.get(TerrainKnowledge.OBJECT_RAMP_PERP_S_NAME);
        this.ramp_perp_w = objects.get(TerrainKnowledge.OBJECT_RAMP_PERP_W_NAME);
        this.ramp_edge_in_ne = objects.get(TerrainKnowledge.OBJECT_RAMP_EDGE_IN_NE_NAME);
        this.ramp_edge_in_nw = objects.get(TerrainKnowledge.OBJECT_RAMP_EDGE_IN_NW_NAME);
        this.ramp_edge_in_se = objects.get(TerrainKnowledge.OBJECT_RAMP_EDGE_IN_SE_NAME);
        this.ramp_edge_in_sw = objects.get(TerrainKnowledge.OBJECT_RAMP_EDGE_IN_SW_NAME);
        this.ramp_edge_out_ne = objects.get(TerrainKnowledge.OBJECT_RAMP_EDGE_OUT_NE_NAME);
        this.ramp_edge_out_nw = objects.get(TerrainKnowledge.OBJECT_RAMP_EDGE_OUT_NW_NAME);
        this.ramp_edge_out_se = objects.get(TerrainKnowledge.OBJECT_RAMP_EDGE_OUT_SE_NAME);
        this.ramp_edge_out_sw = objects.get(TerrainKnowledge.OBJECT_RAMP_EDGE_OUT_SW_NAME);
        this.ramp_corner_ne = objects.get(TerrainKnowledge.OBJECT_RAMP_CORNER_NE_NAME);
        this.ramp_corner_nw = objects.get(TerrainKnowledge.OBJECT_RAMP_CORNER_NW_NAME);
        this.ramp_corner_se = objects.get(TerrainKnowledge.OBJECT_RAMP_CORNER_SE_NAME);
        this.ramp_corner_sw = objects.get(TerrainKnowledge.OBJECT_RAMP_CORNER_SW_NAME);
        this.ramp_two_ne = objects.get(TerrainKnowledge.OBJECT_RAMP_TWO_NE_NAME);
        this.ramp_two_se = objects.get(TerrainKnowledge.OBJECT_RAMP_TWO_SE_NAME);
    }

}
