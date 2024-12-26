/*
 * dwarfhustle-model-knowledge - Manages the compile dependencies for the model.
 * Copyright © 2022-2025 Erwin Müller (erwin.mueller@anrisoftware.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.anrisoftware.dwarfhustle.model.knowledge.evrete;

import static com.anrisoftware.dwarfhustle.model.knowledge.evrete.VegetationKnowledgeMaterials.MATERIAL_TREE_WOOD_NAME;
import static com.anrisoftware.dwarfhustle.model.knowledge.evrete.VegetationKnowledgeObjects.OBJECT_TREE_BRANCH_NAME;
import static com.anrisoftware.dwarfhustle.model.knowledge.evrete.VegetationKnowledgeObjects.OBJECT_TREE_LEAF_NAME;
import static com.anrisoftware.dwarfhustle.model.knowledge.evrete.VegetationKnowledgeObjects.OBJECT_TREE_ROOT_NAME;
import static com.anrisoftware.dwarfhustle.model.knowledge.evrete.VegetationKnowledgeObjects.OBJECT_TREE_TRUNK_NAME;
import static com.anrisoftware.dwarfhustle.model.knowledge.evrete.VegetationKnowledgeObjects.OBJECT_TREE_TWIG_NAME;
import static java.util.concurrent.CompletableFuture.allOf;
import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;

import java.util.concurrent.TimeUnit;

import org.eclipse.collections.api.factory.primitive.IntLists;
import org.eclipse.collections.api.list.ListIterable;
import org.eclipse.collections.api.list.primitive.IntList;
import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.api.map.primitive.MutableIntIntMap;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;

import com.anrisoftware.dwarfhustle.model.api.materials.BlockMaterial;
import com.anrisoftware.dwarfhustle.model.api.materials.Wood;
import com.anrisoftware.dwarfhustle.model.api.objects.KnowledgeObject;
import com.anrisoftware.dwarfhustle.model.api.vegetations.KnowledgeTreeBranch;
import com.anrisoftware.dwarfhustle.model.api.vegetations.KnowledgeTreeLeaf;
import com.anrisoftware.dwarfhustle.model.api.vegetations.KnowledgeTreeRoot;
import com.anrisoftware.dwarfhustle.model.api.vegetations.KnowledgeTreeTrunk;
import com.anrisoftware.dwarfhustle.model.api.vegetations.KnowledgeTreeTwig;
import com.anrisoftware.dwarfhustle.model.api.vegetations.KnowledgeVegetation;
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
        var materials = (MutableIntObjectMap<IntList>) this.materials;
        MutableIntList wood = IntLists.mutable.withInitialCapacity(1);
        materials.put(MATERIAL_TREE_WOOD_NAME.hash, wood);
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
                    materialsGetFilterName(k.getName(), res, MATERIAL_TREE_WOOD_NAME.hash);
                }).toCompletableFuture() //
        ) //
                .get(1000, TimeUnit.SECONDS);
    }

    private void objectsGetFilterName(String name, ListIterable<KnowledgeObject> res, int hash) {
        var objects = (MutableIntIntMap) this.objects;
        knowledgeGet(res, IntLists.mutable.empty(), (o) -> {
            var ot = (KnowledgeVegetation) o;
            if (startsWithIgnoreCase(ot.getName(), name)) {
                objects.put(hash, o.getKid());
            }
        });
    }

    private void materialsGetFilterName(String name, ListIterable<KnowledgeObject> res, int hash) {
        var materials = (MutableIntObjectMap<IntList>) this.materials;
        knowledgeGet(res, IntLists.mutable.empty(), (o) -> {
            var ot = (BlockMaterial) o;
            if (startsWithIgnoreCase(ot.getName(), name)) {
                var m = (MutableIntList) materials.get(hash);
                m.add(o.getKid());
            }
        });
    }

}
