package com.anrisoftware.dwarfhustle.model.trees;

import com.anrisoftware.dwarfhustle.model.api.objects.MapChunk;
import com.anrisoftware.dwarfhustle.model.api.objects.ObjectsGetter;
import com.anrisoftware.dwarfhustle.model.api.objects.ObjectsSetter;
import com.anrisoftware.dwarfhustle.model.api.vegetations.KnowledgeVegetation;
import com.anrisoftware.dwarfhustle.model.api.vegetations.Vegetation;
import com.anrisoftware.dwarfhustle.model.db.buffers.MapChunkBuffer.MapBlockResult;

@FunctionalInterface
public interface RulePredicate {

    boolean test(int x, int y, int z, MapChunk root, MapBlockResult res, KnowledgeVegetation k, Vegetation v,
            ObjectsGetter og, ObjectsSetter os, VegetationLoadKnowledges ks);

}
