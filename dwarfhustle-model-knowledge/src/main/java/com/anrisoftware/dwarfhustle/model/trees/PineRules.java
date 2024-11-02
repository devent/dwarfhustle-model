package com.anrisoftware.dwarfhustle.model.trees;

import static com.anrisoftware.dwarfhustle.model.api.objects.MapBlockFlags.EMPTY;
import static com.anrisoftware.dwarfhustle.model.api.objects.MapBlockFlags.FILLED;
import static com.anrisoftware.dwarfhustle.model.db.buffers.MapBlockBuffer.addProp;
import static com.anrisoftware.dwarfhustle.model.db.buffers.MapBlockBuffer.isProp;
import static com.anrisoftware.dwarfhustle.model.db.buffers.MapBlockBuffer.removeProp;
import static com.anrisoftware.dwarfhustle.model.db.buffers.MapBlockBuffer.setMaterial;
import static com.anrisoftware.dwarfhustle.model.db.buffers.MapBlockBuffer.setObject;
import static com.anrisoftware.dwarfhustle.model.trees.VegetationKnowledgeObjects.MATERIAL_WOODS_NAME;
import static com.anrisoftware.dwarfhustle.model.trees.VegetationKnowledgeObjects.OBJECT_TREE_TRUNK_NAME;

public class PineRules {

    public static RulePredicate grow_1_test = (x, y, z, root, res, k, v, og, os, ks) -> {
        boolean empty = isProp(res.c.getBlocks(), res.off, EMPTY.flag);
        return v.growth < 0.1f && empty;
    };

    public static Rule grow_1 = (x, y, z, root, res, k, v, og, os, ks) -> {
        removeProp(res.c.getBlocks(), res.off, EMPTY.flag);
        addProp(res.c.getBlocks(), res.off, FILLED.flag);
        setObject(res.c.getBlocks(), res.off, ks.getObject(OBJECT_TREE_TRUNK_NAME.hash));
        setMaterial(res.c.getBlocks(), res.off, ks.getMaterials(MATERIAL_WOODS_NAME.hash).getFirst());
    };
}
