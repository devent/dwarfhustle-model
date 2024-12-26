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

import org.evrete.KnowledgeService;
import org.evrete.api.Knowledge;

import com.anrisoftware.dwarfhustle.model.api.objects.GameMap;
import com.anrisoftware.dwarfhustle.model.api.objects.MapChunk;
import com.anrisoftware.dwarfhustle.model.api.objects.ObjectsGetter;
import com.anrisoftware.dwarfhustle.model.api.objects.ObjectsSetter;

/**
 * Knowledge rules for the terrain.
 */
public class TerrainKnowledge extends AbstractKnowledge {

    /**
     * Creates the knowledge from {@code TerrainBlockMaterialRules.java}
     */
    public Knowledge createTerrainBlockMaterialRulesKnowledge(KnowledgeService service) {
        return createRulesKnowledgeFromSource(service, "TerrainBlockMaterialRules.java");
    }

    /**
     * Creates the knowledge from {@code TerrainBlockKindRules.java}
     */
    public Knowledge createTerrainBlockKindRulesKnowledge(KnowledgeService service) {
        return createRulesKnowledgeFromSource(service, "TerrainBlockKindRules.java");
    }

    /**
     * Creates the knowledge from {@code TerrainUpdateRules.java}
     */
    public Knowledge createTerrainUpdateRulesKnowledge(KnowledgeService service) {
        return createRulesKnowledgeFromSource(service, "TerrainUpdateRules.java");
    }

    /**
     * Run the rules to update the terrain.
     */
    public <T extends BlockFact> void runTerrainUpdateRules(ObjectsGetter og, ObjectsSetter os, Knowledge knowledge,
            GameMap gm) {
        var session = knowledge.newStatefulSession();
        for (int i = 0; i < gm.chunksCount; i++) {
            MapChunk chunk = og.get(MapChunk.OBJECT_TYPE, MapChunk.cid2Id(i));
            if (chunk.isLeaf()) {
                var pos = chunk.getPos();
                for (int z = pos.z; z < pos.ep.z; z++) {
                    for (int y = pos.y; y < pos.ep.y; y++) {
                        for (int x = pos.x; x < pos.ep.x; x++) {
                            session.insert(new BlockFact(og, os, chunk, x, y, z, gm.width, gm.height, gm.depth));
                        }
                    }
                }
                session.fire();
                session.clear();
            }
        }
    }

}
