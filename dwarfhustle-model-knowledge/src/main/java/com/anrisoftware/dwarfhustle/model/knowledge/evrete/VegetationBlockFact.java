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

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.collections.api.list.primitive.IntList;
import org.eclipse.collections.api.map.primitive.IntIntMap;
import org.eclipse.collections.api.map.primitive.IntObjectMap;

import com.anrisoftware.dwarfhustle.model.api.objects.MapChunk;
import com.anrisoftware.dwarfhustle.model.api.objects.ObjectsGetter;
import com.anrisoftware.dwarfhustle.model.api.objects.ObjectsSetter;
import com.anrisoftware.dwarfhustle.model.api.vegetations.KnowledgeVegetation;
import com.anrisoftware.dwarfhustle.model.api.vegetations.Vegetation;

import lombok.ToString;

@ToString(callSuper = true)
public class VegetationBlockFact extends BlockFact {

    public final Vegetation v;

    public final KnowledgeVegetation k;

    @ToString.Exclude
    private final IntIntMap objects;

    private final AtomicBoolean done;

    @ToString.Exclude
    private final IntObjectMap<IntList> materials;

    public VegetationBlockFact(AtomicBoolean done, Vegetation v, KnowledgeVegetation k, IntIntMap objects,
            IntObjectMap<IntList> materials, ObjectsGetter og, ObjectsSetter os, MapChunk chunk, int x, int y, int z,
            int w, int h, int d) {
        super(og, os, chunk, x, y, z, w, h, d);
        this.done = done;
        this.v = v;
        this.k = k;
        this.objects = objects;
        this.materials = materials;
    }

    public void setChanged() {
        done.set(false);
    }

    public boolean isOnVegetationBlock() {
        return x == v.pos.x && y == v.pos.y && z == v.pos.z;
    }

    public boolean isOnVegetationNeighbor(int dx, int dy, int dz) {
        return x == v.pos.x + dx && y == v.pos.y + dy && z == v.pos.z + dz;
    }

    public void setObject(int x, int y, int z, VegetationKnowledgeObjects o) {
        setObject(x, y, z, objects.get(o.hash));
    }

    public void setObject(VegetationKnowledgeObjects o) {
        setObject(objects.get(o.hash));
    }

    public boolean isObject(VegetationKnowledgeObjects o) {
        return getObject() == objects.get(o.hash);
    }

    public boolean isObject(int x, int y, int z, VegetationKnowledgeObjects o) {
        return getObject(x, y, z) == objects.get(o.hash);
    }

    public boolean isNotOnMapEdge() {
        return x > 0 && y > 0 && z > 0 && x < w && y < h && z < d;
    }

    public void setMaterial(int x, int y, int z, VegetationKnowledgeMaterials o) {
        setMaterial(x, y, z, materials.get(o.hash).getFirst());
    }

    public void setMaterial(VegetationKnowledgeMaterials o) {
        setMaterial(materials.get(o.hash).getFirst());
    }

}
