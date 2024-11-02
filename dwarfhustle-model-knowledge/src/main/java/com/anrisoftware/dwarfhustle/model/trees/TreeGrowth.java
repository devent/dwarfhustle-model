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

import static com.anrisoftware.dwarfhustle.model.api.objects.MapChunk.getChunk;
import static com.anrisoftware.dwarfhustle.model.db.buffers.MapChunkBuffer.findBlockIndex;
import static org.apache.commons.math3.util.FastMath.floor;
import static org.apache.commons.math3.util.FastMath.max;
import static org.apache.commons.math3.util.FastMath.min;

import org.eclipse.collections.api.list.ImmutableList;

import com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos;
import com.anrisoftware.dwarfhustle.model.api.objects.GameMap;
import com.anrisoftware.dwarfhustle.model.api.objects.MapChunk;
import com.anrisoftware.dwarfhustle.model.api.objects.ObjectsGetter;
import com.anrisoftware.dwarfhustle.model.api.objects.ObjectsSetter;
import com.anrisoftware.dwarfhustle.model.api.vegetations.KnowledgeVegetation;
import com.anrisoftware.dwarfhustle.model.api.vegetations.Vegetation;

public class TreeGrowth implements Runnable {

    public final GameBlockPos start;

    public final GameBlockPos end;

    private final KnowledgeVegetation k;

    private final Vegetation v;

    private final ObjectsGetter og;

    private final ObjectsSetter os;

    private final MapChunk root;

    private final VegetationLoadKnowledges loadedKnowledges;

    private ImmutableList<RuleObject> rules;

    public TreeGrowth(KnowledgeVegetation k, Vegetation v, GameMap gm, VegetationLoadKnowledges loadedKnowledges,
            ObjectsGetter og, ObjectsSetter os) {
        this.k = k;
        this.v = v;
        final int x = v.getPos().getX(), y = v.getPos().getY(), z = v.getPos().getZ();
        final int wh = (int) floor(k.getWidthMax() / 2f), hh = (int) floor(k.getHeightMax() / 2f);
        final int x0 = max(x - wh, 0), x1 = min(x + wh, gm.width - 1);
        final int y0 = max(y - hh, 0), y1 = min(y + hh, gm.height - 1);
        final int z0 = max(z - k.getDepthMax(), 0), z1 = min(v.getPos().getZ() + k.rootMaxSize, gm.depth - 1);
        this.start = new GameBlockPos(x0, y0, z0);
        this.end = new GameBlockPos(x1, y1, z1);
        this.loadedKnowledges = loadedKnowledges;
        this.og = og;
        this.os = os;
        this.root = getChunk(og, 0);
    }

    public void setRules(ImmutableList<RuleObject> rules) {
        this.rules = rules;
    }

    @Override
    public void run() {
        var res = findBlockIndex(root, v.getPos(), og);
        for (int z = start.z; z < end.z; z++) {
            for (int y = start.y; y < end.y; y++) {
                for (int x = start.x; x < end.x; x++) {
                    for (RuleObject o : rules) {
                        if (o.getPredicate().test(x, y, z, root, res, k, v, og, os, loadedKnowledges)) {
                            o.getRule().apply(x, y, z, root, res, k, v, og, os, loadedKnowledges);
                        }
                    }
                }
            }
        }
    }

}
