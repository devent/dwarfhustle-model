package com.anrisoftware.dwarfhustle.model.knowledge.evrete;

import org.evrete.api.RhsContext;
import org.evrete.dsl.annotation.*;

import com.anrisoftware.dwarfhustle.model.api.objects.*;

public class TerrainCreateRules {

    @Rule(salience = 1)
    @Where("$mid == 0 && $block.material == -1")
    public void material_empty_set_oxygen(Long $mid, MapBlock $block, MapBlock[] $neighbors, RhsContext ctx) {
        $block.setMaterialRid(898);
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
        $block.setObjectRid(0);
    }

    public boolean object_set_neighbors_test(MapBlock block, MapBlock[] neighbors) {
        boolean ramp = $neighbors[NeighboringDir.N].isFilled();
        ramp |= $neighbors[NeighboringDir.E].isFilled();
        ramp |= $neighbors[NeighboringDir.W].isFilled();
        ramp |= $neighbors[NeighboringDir.S].isFilled();
        return ramp;
    }

    public boolean material_gas_test(long mid) {
        int amid = (int) mid;
        switch (amid) {
        case 903:
        case 902:
        case 900:
        case 898:
        case 897:
            return true;
        default:
            return false;
        }
    }

    public boolean material_liquid_test(long mid) {
        int amid = (int) mid;
        switch (amid) {
        case 815:
            return true;
        default:
            return false;
        }
    }

    public boolean material_solid_test(long mid) {
        int amid = (int) mid;
        switch (amid) {
        case 827:
        case 826:
        case 825:
        case 824:
        case 823:
        case 822:
        case 821:
        case 820:
        case 819:
        case 818:
        case 817:
        case 831:
        case 830:
        case 829:
        case 837:
        case 836:
        case 835:
        case 834:
        case 833:
        case 844:
        case 843:
        case 842:
        case 841:
        case 840:
        case 839:

        case 895:
        case 894:
        case 893:
        case 892:
        case 891:
        case 890:
        case 889:
        case 888:
        case 885:
        case 884:
        case 883:
        case 881:
        case 880:
        case 879:
        case 878:
        case 778:
        case 875:
        case 873:
        case 871:
        case 869:
        case 779:
            return true;
        default:
            return false;
        }
    }

}
