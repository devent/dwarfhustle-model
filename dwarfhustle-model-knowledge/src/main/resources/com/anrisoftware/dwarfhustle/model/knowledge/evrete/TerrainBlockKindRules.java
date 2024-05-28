package com.anrisoftware.dwarfhustle.model.knowledge.evrete;

import java.util.Arrays;
import java.util.List;

import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.api.map.primitive.MutableIntIntMap;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.evrete.api.Environment;
import org.evrete.api.RhsContext;
import org.evrete.dsl.Phase;
import org.evrete.dsl.annotation.*;

import com.anrisoftware.dwarfhustle.model.api.objects.*;

/**
 * Rules to run on a new map.
 * 
 * <ul>
 * <li>sets filled, empty and liquid type of the block based on the material.
 * </ul>
 */
public class TerrainBlockKindRules extends AbstractTerrainRules {

    //
    // Set block properties.
    //

    @Rule
    @Where(value = "$f.getProp() == 0", methods = { @MethodPredicate(method = "material_gas_test", args = { "$f" }) })
    public void material_gases_set_empty(TerrainFact $f, RhsContext ctx) {
        $f.addProp(MapBlock.EMPTY);
    }

    @Rule
    @Where(value = "$f.getProp() == 0", methods = { @MethodPredicate(method = "material_solid_test", args = { "$f" }) })
    public void material_solids_set_filled(TerrainFact $f, RhsContext ctx) {
        $f.addProp(MapBlock.FILLED);
    }

    @Rule
    @Where(value = "$f.getProp() == 0", methods = {
            @MethodPredicate(method = "material_liquid_test", args = { "$f" }) })
    public void material_liquids_set_liquid(TerrainFact $f, RhsContext ctx) {
        $f.addProp(MapBlock.LIQUID);
    }

    public boolean material_gas_test(TerrainFact f) {
        return gases.contains(f.getMaterial());
    }

    public boolean material_liquid_test(TerrainFact f) {
        return liquids.contains(f.getMaterial());
    }

    public boolean material_solid_test(TerrainFact f) {
        return solids.contains(f.getMaterial());
    }

}
