package com.anrisoftware.dwarfhustle.model.knowledge.evrete;

import static com.anrisoftware.dwarfhustle.model.api.objects.MapBlockFlags.*;
import static com.anrisoftware.dwarfhustle.model.api.objects.NeighboringDir.*;
import static com.anrisoftware.dwarfhustle.model.db.buffers.MapBlockBuffer.*;
import static com.anrisoftware.dwarfhustle.model.db.buffers.MapChunkBuffer.*;
import static com.anrisoftware.dwarfhustle.model.knowledge.evrete.VegetationKnowledge.*;
import static com.anrisoftware.dwarfhustle.model.knowledge.evrete.VegetationKnowledgeObjects.*;

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
import com.anrisoftware.dwarfhustle.model.db.buffers.BufferUtils;
import com.anrisoftware.dwarfhustle.model.db.buffers.MapBlockBuffer;
import com.anrisoftware.dwarfhustle.model.db.buffers.MapChunkBuffer;

/**
 * Replaces the tree sampling.
 */
public class TreeSaplingRules extends AbstractTerrainRules {

    //
    // Set block properties.
    //

    @Rule(salience = 1000000)
    @Where(value = { "$f.v.growthStep == 0", "$f.onVegetationBlock && $f.isProp(EMPTY)" })
    public void step_1_branch_pos(VegetationBlockFact $f, RhsContext ctx) {
        System.out.println("PineRules.step_1_branch_pos()"); // TODO
        $f.setChanged();
        $f.setObject(OBJECT_TREE_BRANCH_NAME);
        $f.removeProp(EMPTY);
        $f.addProp(FILLED);
    }

}
