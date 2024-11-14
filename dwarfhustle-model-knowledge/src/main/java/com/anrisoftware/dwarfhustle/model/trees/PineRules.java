/*
 * dwarfhustle-model-knowledge - Manages the compile dependencies for the model.
 * Copyright © 2023 Erwin Müller (erwin.mueller@anrisoftware.com)
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
