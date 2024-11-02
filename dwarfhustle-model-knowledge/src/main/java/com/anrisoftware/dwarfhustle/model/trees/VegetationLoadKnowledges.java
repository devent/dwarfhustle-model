package com.anrisoftware.dwarfhustle.model.trees;

import static com.anrisoftware.dwarfhustle.model.trees.VegetationKnowledgeObjects.MATERIAL_WOODS_NAME;
import static com.anrisoftware.dwarfhustle.model.trees.VegetationKnowledgeObjects.OBJECT_TREE_BRANCH_NAME;
import static com.anrisoftware.dwarfhustle.model.trees.VegetationKnowledgeObjects.OBJECT_TREE_LEAF_NAME;
import static com.anrisoftware.dwarfhustle.model.trees.VegetationKnowledgeObjects.OBJECT_TREE_ROOT_NAME;
import static com.anrisoftware.dwarfhustle.model.trees.VegetationKnowledgeObjects.OBJECT_TREE_TRUNK_NAME;
import static com.anrisoftware.dwarfhustle.model.trees.VegetationKnowledgeObjects.OBJECT_TREE_TWIG_NAME;
import static java.util.concurrent.CompletableFuture.allOf;
import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;

import java.util.concurrent.TimeUnit;

import org.eclipse.collections.api.factory.primitive.IntLists;
import org.eclipse.collections.api.list.ListIterable;
import org.eclipse.collections.api.list.primitive.MutableIntList;

import com.anrisoftware.dwarfhustle.model.api.materials.BlockMaterial;
import com.anrisoftware.dwarfhustle.model.api.materials.Wood;
import com.anrisoftware.dwarfhustle.model.api.objects.KnowledgeObject;
import com.anrisoftware.dwarfhustle.model.api.vegetations.KnowledgeTreeBranch;
import com.anrisoftware.dwarfhustle.model.api.vegetations.KnowledgeTreeLeaf;
import com.anrisoftware.dwarfhustle.model.api.vegetations.KnowledgeTreeRoot;
import com.anrisoftware.dwarfhustle.model.api.vegetations.KnowledgeTreeTrunk;
import com.anrisoftware.dwarfhustle.model.api.vegetations.KnowledgeTreeTwig;
import com.anrisoftware.dwarfhustle.model.api.vegetations.KnowledgeVegetation;
import com.anrisoftware.dwarfhustle.model.knowledge.evrete.AskKnowledge;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.DefaultLoadKnowledges;

import lombok.SneakyThrows;

/**
 * Loads objects and material.
 * 
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public class VegetationLoadKnowledges extends DefaultLoadKnowledges {

    private final KnowledgeVegetation k;

    public VegetationLoadKnowledges(KnowledgeVegetation k) {
        this.k = k;
    }

    @Override
    @SneakyThrows
    public void loadKnowledges(AskKnowledge ask) {
        super.loadKnowledges(ask);
        MutableIntList wood = IntLists.mutable.withInitialCapacity(1);
        materials.put(MATERIAL_WOODS_NAME.hash, wood);
        allOf( //
                ask.doAskAsync(ASK_TIMEOUT, KnowledgeTreeRoot.TYPE).whenComplete((res, ex) -> {
                    objectsGetFilterName(k.getName(), res, OBJECT_TREE_ROOT_NAME.hash);
                }).toCompletableFuture(), //
                ask.doAskAsync(ASK_TIMEOUT, KnowledgeTreeLeaf.TYPE).whenComplete((res, ex) -> {
                    objectsGetFilterName(k.getName(), res, OBJECT_TREE_LEAF_NAME.hash);
                }).toCompletableFuture(), //
                ask.doAskAsync(ASK_TIMEOUT, KnowledgeTreeTwig.TYPE).whenComplete((res, ex) -> {
                    objectsGetFilterName(k.getName(), res, OBJECT_TREE_TWIG_NAME.hash);
                }).toCompletableFuture(), //
                ask.doAskAsync(ASK_TIMEOUT, KnowledgeTreeBranch.TYPE).whenComplete((res, ex) -> {
                    objectsGetFilterName(k.getName(), res, OBJECT_TREE_BRANCH_NAME.hash);
                }).toCompletableFuture(), //
                ask.doAskAsync(ASK_TIMEOUT, KnowledgeTreeTrunk.TYPE).whenComplete((res, ex) -> {
                    objectsGetFilterName(k.getName(), res, OBJECT_TREE_TRUNK_NAME.hash);
                }).toCompletableFuture(), //
                ask.doAskAsync(ASK_TIMEOUT, Wood.TYPE).whenComplete((res, ex) -> {
                    materialsGetFilterName(k.getName(), res, MATERIAL_WOODS_NAME.hash);
                }).toCompletableFuture() //
        ) //
                .get(10, TimeUnit.SECONDS);
    }

    private void objectsGetFilterName(String name, ListIterable<KnowledgeObject> res, int hash) {
        knowledgeGet(res, IntLists.mutable.empty(), (o) -> {
            var ot = (KnowledgeVegetation) o;
            if (startsWithIgnoreCase(ot.getName(), name)) {
                objects.put(hash, o.getKid());
            }
        });
    }

    private void materialsGetFilterName(String name, ListIterable<KnowledgeObject> res, int hash) {
        knowledgeGet(res, IntLists.mutable.empty(), (o) -> {
            var ot = (BlockMaterial) o;
            if (startsWithIgnoreCase(ot.getName(), name)) {
                materials.get(hash).add(o.getKid());
            }
        });
    }

}
