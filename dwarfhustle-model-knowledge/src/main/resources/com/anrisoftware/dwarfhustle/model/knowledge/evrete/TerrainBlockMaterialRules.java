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
 * <li>sets the material of blocks;
 * </ul>
 */
public class TerrainBlockMaterialRules extends AbstractTerrainRules {

    //
    // Set block material.
    //

    @Rule(salience = 1000000)
    @Where("$f.getTerrain() == 0 && $f.getMaterial() == -1")
    public void material_empty_set_oxygen(TerrainFact $f, RhsContext ctx) {
        $f.setMaterial(materialOxygen);
    }

    @Rule(salience = 1000000)
    @Where("$f.getTerrain() > 0 && $f.getMaterial() == -1")
    public void material_set_rid(TerrainFact $f, RhsContext ctx) {
        $f.setMaterial($f.getTerrain());
    }

}
