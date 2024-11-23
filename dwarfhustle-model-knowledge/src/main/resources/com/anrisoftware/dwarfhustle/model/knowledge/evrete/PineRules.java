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
    @Where(value = { "$f.v.growthStep == 0", "$f.onVegetationBlock && $f.isProp(EMPTY)" })
    public void step_1_branch_pos(VegetationBlockFact $f, RhsContext ctx) {
        System.out.println("PineRules.step_1_branch_pos()"); // TODO
        $f.setChanged();
        $f.setObject(OBJECT_TREE_BRANCH_NAME);
        $f.removeProp(EMPTY);
        $f.addProp(FILLED);
    }
//
//    @Rule(salience = 1)
//    @Where(value = { "$f.v.growthStep == 0", "$f.z > 0 && $f.isOnVegetationNeighbor(0, 0, -1) && $f.isProp(EMPTY)" })
//    public void step_1_leaf_up_pos(VegetationBlockFact $f, RhsContext ctx) {
//        if ($f.isObject($f.x, $f.y, $f.z + 1, OBJECT_TREE_BRANCH_NAME)) {
//            System.out.println("PineRules.step_1_leaf_up_pos()"); // TODO
//            $f.setChanged();
//            $f.setObject(OBJECT_TREE_LEAF_NAME);
//            $f.removeProp(EMPTY);
//            $f.addProp(FILLED);
//        }
//    }
//
//    @Rule(salience = 1)
//    @Where(value = { "$f.v.growthStep == 0", "$f.x > 0 && $f.isOnVegetationNeighbor(-1, 0, 0) && $f.isProp(EMPTY)" })
//    public void step_1_leaf_west_pos(VegetationBlockFact $f, RhsContext ctx) {
//        if ($f.isObject($f.x + 1, $f.y, $f.z, OBJECT_TREE_BRANCH_NAME)) {
//            System.out.println("PineRules.step_1_leaf_west_pos()"); // TODO
//            $f.setChanged();
//            $f.setObject(OBJECT_TREE_LEAF_NAME);
//            $f.removeProp(EMPTY);
//            $f.addProp(FILLED);
//        }
//    }
//
//    @Rule(salience = 1)
//    @Where(value = { "$f.v.growthStep == 0", "$f.x < $f.w && $f.isOnVegetationNeighbor(1, 0, 0) && $f.isProp(EMPTY)" })
//    public void step_1_leaf_east_pos(VegetationBlockFact $f, RhsContext ctx) {
//        if ($f.isObject($f.x - 1, $f.y, $f.z, OBJECT_TREE_BRANCH_NAME)) {
//            System.out.println("PineRules.step_1_leaf_east_pos()"); // TODO
//            $f.setChanged();
//            $f.setObject(OBJECT_TREE_LEAF_NAME);
//            $f.removeProp(EMPTY);
//            $f.addProp(FILLED);
//        }
//    }
//
//    @Rule(salience = 1)
//    @Where(value = { "$f.v.growthStep == 0",
//            "$f.z < $f.d && $f.isOnVegetationNeighbor(0, 0, 1) && $f.isProp(FILLED) && !$f.isObject(OBJECT_TREE_ROOT_NAME)" })
//    public void step_1_root_down_pos(VegetationBlockFact $f, RhsContext ctx) {
//        if ($f.isObject($f.x, $f.y, $f.z - 1, OBJECT_TREE_BRANCH_NAME)) {
//            System.out.println("PineRules.step_1_root_down_pos()"); // TODO
//            $f.setChanged();
//            $f.setObject(OBJECT_TREE_ROOT_NAME);
//            $f.removeProp(EMPTY);
//            $f.addProp(FILLED);
//        }
//    }
//
//    @Rule(salience = 1000000)
//    @Where(value = { "$f.v.growthStep == 1", "$f.onVegetationBlock && $f.isObject(OBJECT_TREE_BRANCH_NAME)" })
//    public void step_2_trunk_pos(VegetationBlockFact $f, RhsContext ctx) {
//        System.out.println("PineRules.step_2_trunk_pos()"); // TODO
//        $f.setChanged();
//        $f.setObject(OBJECT_TREE_TRUNK_NAME);
//        if ($f.x > 0 && $f.isObject($f.x - 1, $f.y, $f.z, OBJECT_TREE_LEAF_NAME)) {
//            $f.setObject($f.x - 1, $f.y, $f.z, block);
//        }
//        if ($f.x < $f.w && $f.isObject($f.x + 1, $f.y, $f.z, OBJECT_TREE_LEAF_NAME)) {
//            $f.setObject($f.x + 1, $f.y, $f.z, block);
//        }
//        if ($f.y > 0 && $f.isObject($f.x, $f.y - 1, $f.z, OBJECT_TREE_LEAF_NAME)) {
//            $f.setObject($f.x, $f.y - 1, $f.z, block);
//        }
//        if ($f.y < $f.h && $f.isObject($f.x, $f.y + 1, $f.z, OBJECT_TREE_LEAF_NAME)) {
//            $f.setObject($f.x, $f.y + 1, $f.z, block);
//        }
//    }
//
//    @Rule(salience = 1000000)
//    @Where(value = { "$f.v.growthStep == 1", "$f.isOnVegetationNeighbor(0, 0, 1)",
//            "!$f.isObject(OBJECT_TREE_BRANCH_NAME)" })
//    public void step_2_branch_pos(VegetationBlockFact $f, RhsContext ctx) {
//        System.out.println("PineRules.step_2_branch_pos()"); // TODO
//        $f.setChanged();
//        $f.setObject(OBJECT_TREE_BRANCH_NAME);
//        if ($f.x > 0 && $f.isProp($f.x - 1, $f.y, $f.z, EMPTY)) {
//            $f.setObject($f.x - 1, $f.y, $f.z, OBJECT_TREE_LEAF_NAME);
//        }
//        if ($f.x < $f.w && $f.isProp($f.x + 1, $f.y, $f.z, EMPTY)) {
//            $f.setObject($f.x + 1, $f.y, $f.z, OBJECT_TREE_LEAF_NAME);
//        }
//        if ($f.y > 0 && $f.isProp($f.x, $f.y - 1, $f.z, EMPTY)) {
//            $f.setObject($f.x, $f.y - 1, $f.z, OBJECT_TREE_LEAF_NAME);
//        }
//        if ($f.y < $f.w && $f.isProp($f.x, $f.y + 1, $f.z, EMPTY)) {
//            $f.setObject($f.x, $f.y + 1, $f.z, OBJECT_TREE_LEAF_NAME);
//        }
//        if ($f.z > 0 && $f.isProp($f.x, $f.y, $f.z - 1, EMPTY)) {
//            $f.setObject($f.x, $f.y, $f.z - 1, OBJECT_TREE_LEAF_NAME);
//        }
//    }
//
}
