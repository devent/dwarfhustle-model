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

import static com.anrisoftware.dwarfhustle.model.api.objects.MapChunk.getChunk;
import static com.anrisoftware.dwarfhustle.model.db.buffers.MapChunkBuffer.findChunk;
import static java.lang.String.format;
import static org.apache.commons.math3.util.FastMath.floor;
import static org.apache.commons.math3.util.FastMath.max;
import static org.apache.commons.math3.util.FastMath.min;

import java.util.concurrent.atomic.AtomicBoolean;

import org.evrete.Configuration;
import org.evrete.api.Knowledge;

import com.anrisoftware.dwarfhustle.model.api.objects.GameMap;
import com.anrisoftware.dwarfhustle.model.api.objects.ObjectsGetter;
import com.anrisoftware.dwarfhustle.model.api.objects.ObjectsSetter;
import com.anrisoftware.dwarfhustle.model.api.vegetations.KnowledgeVegetation;
import com.anrisoftware.dwarfhustle.model.api.vegetations.Vegetation;

/**
 * Knowledge rules for vegetation.
 */
public class VegetationKnowledge extends AbstractKnowledge {

    @Override
    protected Configuration createKnowledgeConf() {
        var conf = super.createKnowledgeConf();
        for (var d : VegetationKnowledgeObjects.values()) {
            conf.addImport(format("static %s.%s", VegetationKnowledgeObjects.class.getName(), d.name()));
        }
        return conf;
    }

    /**
     * Run the rules to update the vegetation.
     */
    public <T extends VegetationBlockFact> void run(AskKnowledge ask, Knowledge knowledge, Vegetation v,
            KnowledgeVegetation k, ObjectsGetter og, ObjectsSetter os, GameMap gm) {
        var session = knowledge.newStatefulSession();
        final var root = getChunk(og, 0);
        final int x = v.getPos().getX(), y = v.getPos().getY(), z = v.getPos().getZ();
        final int wh = (int) floor(k.getWidthMax() / 2f), hh = (int) floor(k.getHeightMax() / 2f);
        final int x0 = max(x - wh, 0), x1 = min(x + wh, gm.width - 1);
        final int y0 = max(y - hh, 0), y1 = min(y + hh, gm.height - 1);
        final int z0 = max(z - k.getDepthMax(), 0), z1 = min(v.getPos().getZ() + k.rootMaxSize, gm.depth - 1);
        var chunk = findChunk(root, x0, y0, z0, og);
        final var done = new AtomicBoolean(false);
        int radius = 1, x0r, y0r, z0r, x1r, y1r, z1r;
        do {
            x0r = max(x - radius, x0);
            y0r = max(y - radius, y0);
            z0r = max(z - radius, z0);
            x1r = min(x + radius, x1);
            y1r = min(y + radius, y1);
            z1r = min(z + radius, z1);
            done.set(true);
            for (int zz = z0r; zz <= z1r; zz++) {
                for (int yy = y0r; yy <= y1r; yy++) {
                    for (int xx = x0r; xx <= x1r; xx++) {
                        if (!chunk.isInside(xx, yy, zz)) {
                            chunk = findChunk(root, xx, yy, zz, og);
                        }
                        session.insert(new VegetationBlockFact(done, v, k, getObjects(), getMaterials(), og, os, root,
                                xx, yy, zz, gm.width, gm.height, gm.depth));
                    }
                }
            }
            session.fire();
            radius++;
            if (x0r == x0 && y0r == y0 && z0r == z0) {
                break;
            }
        } while (!done.get());
        v.setGrowthStep(v.getGrowthStep() + 1);
    }
}
