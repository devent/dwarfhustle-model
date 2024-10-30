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
package com.anrisoftware.dwarfhustle.model.knowledge.evrete;

import org.eclipse.collections.api.map.primitive.IntIntMap;

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
    public final IntIntMap objects;

    public VegetationBlockFact(Vegetation v, KnowledgeVegetation k, IntIntMap objects, ObjectsGetter og,
            ObjectsSetter os, MapChunk chunk, int x, int y, int z, int w, int h, int d) {
        super(og, os, chunk, x, y, z, w, h, d);
        this.v = v;
        this.k = k;
        this.objects = objects;
    }

}
