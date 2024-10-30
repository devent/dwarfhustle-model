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
 * Rules to grow a pine tree.
 */
public class PineRules extends AbstractTerrainRules {

    //
    // Set block properties.
    //

    @Rule(salience = 1000000)
    @Where(value = { "$f.v.growth < 0.1",
            "$f.x == $f.v.pos.x && $f.y == $f.v.pos.y && $f.z == $f.v.pos.z && $f.isProp(EMPTY.flag)" })
    public void step_1_twig_pos(VegetationBlockFact $f, RhsContext ctx) {
        System.out.printf("PineRules.step_1_twig_pos(%d, %d, %d)\n", $f.x, $f.y, $f.z); // TODO
        $f.setObject(objects.get(OBJECT_TREE_BRANCH_NAME.hash));
        $f.setProp(FILLED.flag);
        // $f.v.growth += 0.1f;
    }

    @Rule(salience = 1)
    @Where(value = { "$f.v.growth < 0.1",
            "$f.z > 0 && $f.x == $f.v.pos.x && $f.y == $f.v.pos.y && $f.z == $f.v.pos.z - 1 && $f.isProp(EMPTY.flag)" })
    public void step_1_leaf_up_pos(VegetationBlockFact $f, RhsContext ctx) {
        System.out.printf("PineRules.step_1_leaf_up_pos(%d, %d, %d)\n", $f.x, $f.y, $f.z); // TODO
        if ($f.getObject($f.x, $f.y, $f.z + 1) == $f.objects.get(OBJECT_TREE_BRANCH_NAME.hash)) {
            $f.setObject(objects.get(OBJECT_TREE_LEAF_NAME.hash));
            $f.setProp(FILLED.flag);
        }
    }
}
