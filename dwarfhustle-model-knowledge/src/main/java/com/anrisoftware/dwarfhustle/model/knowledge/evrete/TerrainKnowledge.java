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

import static java.util.concurrent.CompletableFuture.allOf;
import static org.apache.commons.lang3.function.Consumers.nop;

import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.collections.api.factory.primitive.IntIntMaps;
import org.eclipse.collections.api.factory.primitive.IntLists;
import org.eclipse.collections.api.factory.primitive.IntObjectMaps;
import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.api.map.primitive.MutableIntIntMap;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.evrete.Configuration;
import org.evrete.KnowledgeService;
import org.evrete.api.Knowledge;
import org.evrete.util.CompilationException;

import com.anrisoftware.dwarfhustle.model.api.map.ObjectType;
import com.anrisoftware.dwarfhustle.model.api.materials.BlockMaterial;
import com.anrisoftware.dwarfhustle.model.api.materials.Gas;
import com.anrisoftware.dwarfhustle.model.api.materials.Liquid;
import com.anrisoftware.dwarfhustle.model.api.materials.Soil;
import com.anrisoftware.dwarfhustle.model.api.materials.Stone;
import com.anrisoftware.dwarfhustle.model.api.objects.GameMap;
import com.anrisoftware.dwarfhustle.model.api.objects.KnowledgeObject;
import com.anrisoftware.dwarfhustle.model.api.objects.MapBlockFlags;
import com.anrisoftware.dwarfhustle.model.api.objects.MapChunk;
import com.anrisoftware.dwarfhustle.model.api.objects.MapChunksStore;
import com.anrisoftware.dwarfhustle.model.api.objects.NeighboringDir;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * Knowledge rules for the terrain.
 */
@Slf4j
public class TerrainKnowledge {

    public static final String MATERIALS_NAME = "materials";

    public static final int MATERIAL_OXYGEN_NAME = "material-oxygen".hashCode();

    public static final int MATERIALS_GASES_NAME = "materials-gases".hashCode();

    public static final int MATERIALS_LIQUIDS_NAME = "materials-liquids".hashCode();

    public static final int MATERIALS_SOLIDS_NAME = "materials-solids".hashCode();

    public static final String OBJECTS_NAME = "objects";

    public static final int OBJECT_BLOCK_NAME = "object-block".hashCode();

    public static final int OBJECT_WATER_NAME = "object-water".hashCode();

    public static final int OBJECT_RAMP_SINGLE_NAME = "object-ramp-single".hashCode();

    public static final int OBJECT_RAMP_TRI_N_NAME = "object-ramp-tri-n".hashCode();

    public static final int OBJECT_RAMP_TRI_E_NAME = "object-ramp-tri-e".hashCode();

    public static final int OBJECT_RAMP_TRI_S_NAME = "object-ramp-tri-s".hashCode();

    public static final int OBJECT_RAMP_TRI_W_NAME = "object-ramp-tri-w".hashCode();

    public static final int OBJECT_RAMP_EDGE_IN_NE_NAME = "object-ramp-edge-in-ne".hashCode();

    public static final int OBJECT_RAMP_EDGE_IN_NW_NAME = "object-ramp-edge-in-nw".hashCode();

    public static final int OBJECT_RAMP_EDGE_IN_SE_NAME = "object-ramp-edge-in-se".hashCode();

    public static final int OBJECT_RAMP_EDGE_IN_SW_NAME = "object-ramp-edge-in-sw".hashCode();

    public static final int OBJECT_RAMP_EDGE_OUT_NE_NAME = "object-ramp-edge-out-ne".hashCode();

    public static final int OBJECT_RAMP_EDGE_OUT_NW_NAME = "object-ramp-edge-out-nw".hashCode();

    public static final int OBJECT_RAMP_EDGE_OUT_SE_NAME = "object-ramp-edge-out-se".hashCode();

    public static final int OBJECT_RAMP_EDGE_OUT_SW_NAME = "object-ramp-edge-out-sw".hashCode();

    public static final int OBJECT_RAMP_PERP_N_NAME = "object-ramp-perp-n".hashCode();

    public static final int OBJECT_RAMP_PERP_E_NAME = "object-ramp-perp-e".hashCode();

    public static final int OBJECT_RAMP_PERP_S_NAME = "object-ramp-perp-s".hashCode();

    public static final int OBJECT_RAMP_PERP_W_NAME = "object-ramp-perp-w".hashCode();

    public static final int OBJECT_RAMP_CORNER_NE_NAME = "object-ramp-corner-ne".hashCode();

    public static final int OBJECT_RAMP_CORNER_NW_NAME = "object-ramp-corner-nw".hashCode();

    public static final int OBJECT_RAMP_CORNER_SE_NAME = "object-ramp-corner-se".hashCode();

    public static final int OBJECT_RAMP_CORNER_SW_NAME = "object-ramp-corner-sw".hashCode();

    private static final Duration ASK_TIMEOUT = Duration.of(10, ChronoUnit.SECONDS);

    private final MutableIntObjectMap<MutableIntList> materials;

    private final MutableIntIntMap objects;

    private final Configuration conf;

    private final KnowledgeService service;

    public TerrainKnowledge(AskKnowledge askKnowledge) throws IOException {
        this.materials = IntObjectMaps.mutable.empty();
        this.objects = IntIntMaps.mutable.ofInitialCapacity(100);
        loadKnowledges(askKnowledge);
        this.conf = new Configuration();
        conf.addImport(NeighboringDir.class);
        for (var d : NeighboringDir.values()) {
            conf.addImport(String.format("static %s.%s", NeighboringDir.class.getName(), d.name()));
        }
        conf.addImport(MapBlockFlags.class);
        for (var d : MapBlockFlags.values()) {
            conf.addImport(String.format("static %s.%s", MapBlockFlags.class.getName(), d.name()));
        }
        this.service = new KnowledgeService(conf);
    }

    public MutableIntList getMaterials(int name) {
        return materials.get(name);
    }

    public int getObject(int name) {
        return objects.get(name);
    }

    /**
     * Creates the knowledge from {@code TerrainBlockMaterialRules.java}
     */
    public Knowledge createTerrainBlockMaterialRulesKnowledge() throws IOException {
        return createRulesKnowledgeFromSource("TerrainBlockMaterialRules.java");
    }

    /**
     * Creates the knowledge from {@code TerrainBlockKindRules.java}
     */
    public Knowledge createTerrainBlockKindRulesKnowledge() throws IOException {
        return createRulesKnowledgeFromSource("TerrainBlockKindRules.java");
    }

    /**
     * Creates the knowledge from {@code TerrainUpdateRules.java}
     */
    public Knowledge createTerrainUpdateRulesKnowledge() throws IOException {
        return createRulesKnowledgeFromSource("TerrainUpdateRules.java");
    }

    private Knowledge createRulesKnowledgeFromSource(String name) throws IOException {
        var rulesetUrl = TerrainKnowledge.class.getResource(name);
        assert rulesetUrl != null;
        try {
            var knowledge = service.newKnowledge("JAVA-SOURCE", rulesetUrl);
            knowledge.set(MATERIALS_NAME, materials);
            knowledge.set(OBJECTS_NAME, objects);
            return knowledge;
        } catch (IllegalStateException e) {
            if (e.getCause() instanceof CompilationException ce) {
                ce.getErrorSources().forEach((c) -> {
                    log.error("{} - {}", c, ce.getErrorMessage(c));
                });
            }
            throw e;
        }
    }

    /**
     * Run the rules to update the terrain.
     */
    public <T extends BlockFact> void runTerrainUpdateRules(MapChunksStore store, Knowledge knowledge, GameMap gm) {
        var session = knowledge.newStatefulSession();
        store.forEachValue(chunk -> {
            if (chunk.isLeaf() && chunk.pos.ep.z < 128) {
                chunk.changed = true;
            }
        });
        Function<Integer, MapChunk> retriever = store::getChunk;
        store.forEachValue(chunk -> {
            if (chunk.changed && chunk.isLeaf()) {
                var pos = chunk.getPos();
                for (int z = pos.z; z < pos.ep.z; z++) {
                    for (int y = pos.y; y < pos.ep.y; y++) {
                        for (int x = pos.x; x < pos.ep.x; x++) {
                            session.insert(new BlockFact(chunk, x, y, z, gm.width, gm.height, gm.depth, retriever));
                        }
                    }
                }
                session.fire();
                session.clear();
            }
        });
        session.close();
    }

    @SneakyThrows
    private void loadKnowledges(AskKnowledge ask) {
        MutableIntList solids = IntLists.mutable.withInitialCapacity(100);
        MutableIntList liquids = IntLists.mutable.withInitialCapacity(100);
        MutableIntList gases = IntLists.mutable.withInitialCapacity(100);
        MutableIntList oxygen = IntLists.mutable.withInitialCapacity(1);
        materials.put(MATERIALS_SOLIDS_NAME, solids);
        materials.put(MATERIALS_LIQUIDS_NAME, liquids);
        materials.put(MATERIALS_GASES_NAME, gases);
        materials.put(MATERIAL_OXYGEN_NAME, oxygen);
        allOf( //
                ask.doAskAsync(ASK_TIMEOUT, Stone.class, Stone.TYPE).whenComplete((res, ex) -> {
                    knowledgeGet(res, solids, nop());
                }).toCompletableFuture(), //
                ask.doAskAsync(ASK_TIMEOUT, Soil.class, Soil.TYPE).whenComplete((res, ex) -> {
                    knowledgeGet(res, solids, nop());
                }).toCompletableFuture(), //
                ask.doAskAsync(ASK_TIMEOUT, Liquid.class, Liquid.TYPE).whenComplete((res, ex) -> {
                    knowledgeGet(res, liquids, nop());
                }).toCompletableFuture(), //
                ask.doAskAsync(ASK_TIMEOUT, Gas.class, Gas.TYPE).whenComplete((res, ex) -> {
                    knowledgeGet(res, gases, (o) -> {
                        var mo = (BlockMaterial) o;
                        if (mo.getName().equalsIgnoreCase("oxygen")) {
                            oxygen.add(o.getKid());
                        }
                    });
                }).toCompletableFuture(), //
                ask.doAskAsync(ASK_TIMEOUT, ObjectType.class, ObjectType.TYPE).whenComplete((res, ex) -> {
                    knowledgeGet(res, IntLists.mutable.empty(), (o) -> {
                        var ot = (ObjectType) o;
                        switch (ot.getName()) {
                        case "BLOCK-RAMP-TRI-N":
                            objects.put(OBJECT_RAMP_TRI_N_NAME, o.getKid());
                            break;
                        case "BLOCK-RAMP-TRI-S":
                            objects.put(OBJECT_RAMP_TRI_S_NAME, o.getKid());
                            break;
                        case "BLOCK-RAMP-TRI-E":
                            objects.put(OBJECT_RAMP_TRI_E_NAME, o.getKid());
                            break;
                        case "BLOCK-RAMP-TRI-W":
                            objects.put(OBJECT_RAMP_TRI_W_NAME, o.getKid());
                            break;
                        case "BLOCK-RAMP-SINGLE":
                            objects.put(OBJECT_RAMP_SINGLE_NAME, o.getKid());
                            break;
                        case "BLOCK-RAMP-PERP-N":
                            objects.put(OBJECT_RAMP_PERP_N_NAME, o.getKid());
                            break;
                        case "BLOCK-RAMP-PERP-E":
                            objects.put(OBJECT_RAMP_PERP_E_NAME, o.getKid());
                            break;
                        case "BLOCK-RAMP-PERP-S":
                            objects.put(OBJECT_RAMP_PERP_S_NAME, o.getKid());
                            break;
                        case "BLOCK-RAMP-PERP-W":
                            objects.put(OBJECT_RAMP_PERP_W_NAME, o.getKid());
                            break;
                        case "BLOCK-RAMP-EDGE-OUT-NE":
                            objects.put(OBJECT_RAMP_EDGE_OUT_NE_NAME, o.getKid());
                            break;
                        case "BLOCK-RAMP-EDGE-OUT-NW":
                            objects.put(OBJECT_RAMP_EDGE_OUT_NW_NAME, o.getKid());
                            break;
                        case "BLOCK-RAMP-EDGE-OUT-SE":
                            objects.put(OBJECT_RAMP_EDGE_OUT_SE_NAME, o.getKid());
                            break;
                        case "BLOCK-RAMP-EDGE-OUT-SW":
                            objects.put(OBJECT_RAMP_EDGE_OUT_SW_NAME, o.getKid());
                            break;
                        case "BLOCK-RAMP-EDGE-IN-NE":
                            objects.put(OBJECT_RAMP_EDGE_IN_NE_NAME, o.getKid());
                            break;
                        case "BLOCK-RAMP-EDGE-IN-NW":
                            objects.put(OBJECT_RAMP_EDGE_IN_NW_NAME, o.getKid());
                            break;
                        case "BLOCK-RAMP-EDGE-IN-SE":
                            objects.put(OBJECT_RAMP_EDGE_IN_SE_NAME, o.getKid());
                            break;
                        case "BLOCK-RAMP-EDGE-IN-SW":
                            objects.put(OBJECT_RAMP_EDGE_IN_SW_NAME, o.getKid());
                            break;
                        case "BLOCK-RAMP-CORNER-NE":
                            objects.put(OBJECT_RAMP_CORNER_NE_NAME, o.getKid());
                            break;
                        case "BLOCK-RAMP-CORNER-NW":
                            objects.put(OBJECT_RAMP_CORNER_NW_NAME, o.getKid());
                            break;
                        case "BLOCK-RAMP-CORNER-SE":
                            objects.put(OBJECT_RAMP_CORNER_SE_NAME, o.getKid());
                            break;
                        case "BLOCK-RAMP-CORNER-SW":
                            objects.put(OBJECT_RAMP_CORNER_SW_NAME, o.getKid());
                            break;
                        case "BLOCK-NORMAL":
                            objects.put(OBJECT_BLOCK_NAME, o.getKid());
                            break;
                        case "BLOCK-WATER":
                            objects.put(OBJECT_WATER_NAME, o.getKid());
                            break;
                        }
                    });
                }).toCompletableFuture() //
        ) //
                .get(30, TimeUnit.SECONDS);
    }

    private void knowledgeGet(Iterable<KnowledgeObject> res, MutableIntList dest, Consumer<KnowledgeObject> consume) {
        for (var o : res) {
            dest.add(o.getKid());
            consume.accept(o);
        }
    }
}
