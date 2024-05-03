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

import org.eclipse.collections.api.factory.primitive.IntLongMaps;
import org.eclipse.collections.api.factory.primitive.IntObjectMaps;
import org.eclipse.collections.api.factory.primitive.LongLists;
import org.eclipse.collections.api.list.primitive.MutableLongList;
import org.eclipse.collections.api.map.primitive.MutableIntLongMap;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.evrete.Configuration;
import org.evrete.KnowledgeService;
import org.evrete.api.Knowledge;
import org.evrete.api.StatefulSession;
import org.evrete.util.CompilationException;

import com.anrisoftware.dwarfhustle.model.api.map.ObjectType;
import com.anrisoftware.dwarfhustle.model.api.materials.Gas;
import com.anrisoftware.dwarfhustle.model.api.materials.Liquid;
import com.anrisoftware.dwarfhustle.model.api.materials.Material;
import com.anrisoftware.dwarfhustle.model.api.materials.Soil;
import com.anrisoftware.dwarfhustle.model.api.materials.Stone;
import com.anrisoftware.dwarfhustle.model.api.objects.KnowledgeObject;
import com.anrisoftware.dwarfhustle.model.api.objects.NeighboringDir;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * Knowledge rules to create a terrain.
 */
@Slf4j
public class TerrainCreateKnowledge {

    public static final String MATERIALS_NAME = "materials";

    public static final int MATERIAL_OXYGEN_NAME = "material-oxygen".hashCode();

    public static final int MATERIALS_GASES_NAME = "materials-gases".hashCode();

    public static final int MATERIALS_LIQUIDS_NAME = "materials-liquids".hashCode();

    public static final int MATERIALS_SOLIDS_NAME = "materials-solids".hashCode();

    public static final String OBJECTS_NAME = "objects";

    public static final int OBJECT_BLOCK_NAME = "object-block".hashCode();

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

    private Knowledge knowledge;

    private final MutableIntObjectMap<MutableLongList> materials;

    private final MutableIntLongMap objects;

    public TerrainCreateKnowledge(AskKnowledge askKnowledge) throws IOException {
        this.materials = IntObjectMaps.mutable.empty();
        this.objects = IntLongMaps.mutable.ofInitialCapacity(100);
        loadKnowledges(askKnowledge);
        var conf = new Configuration();
        conf.addImport(NeighboringDir.class);
        for (var d : NeighboringDir.values()) {
            conf.addImport(String.format("static %s.%s", NeighboringDir.class.getName(), d.name()));
        }
        var service = new KnowledgeService(conf);
        var rulesetUrl = TerrainCreateKnowledge.class.getResource("TerrainCreateRules.java");
        assert rulesetUrl != null;
        try {
            this.knowledge = service.newKnowledge("JAVA-SOURCE", rulesetUrl);
            knowledge.set(MATERIALS_NAME, materials);
            knowledge.set(OBJECTS_NAME, objects);
        } catch (IllegalStateException e) {
            if (e.getCause() instanceof CompilationException ce) {
                ce.getErrorSources().forEach((c) -> {
                    log.error("{} - {}", c, ce.getErrorMessage(c));
                });
            }
        }
    }

    @SneakyThrows
    private void loadKnowledges(AskKnowledge ask) {
        MutableLongList solids = LongLists.mutable.withInitialCapacity(100);
        MutableLongList liquids = LongLists.mutable.withInitialCapacity(100);
        MutableLongList gases = LongLists.mutable.withInitialCapacity(100);
        MutableLongList oxygen = LongLists.mutable.withInitialCapacity(1);
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
                        var mo = (Material) o;
                        if (mo.getName().equalsIgnoreCase("oxygen")) {
                            oxygen.add(o.getKid());
                        }
                    });
                }).toCompletableFuture(), //
                ask.doAskAsync(ASK_TIMEOUT, ObjectType.class, ObjectType.TYPE).whenComplete((res, ex) -> {
                    knowledgeGet(res, LongLists.mutable.empty(), (o) -> {
                        var ot = (ObjectType) o;
                        switch (ot.getName()) {
                        case "TILE-RAMP-TRI-N":
                            objects.put(OBJECT_RAMP_TRI_N_NAME, o.getKid());
                            break;
                        case "TILE-RAMP-TRI-S":
                            objects.put(OBJECT_RAMP_TRI_S_NAME, o.getKid());
                            break;
                        case "TILE-RAMP-TRI-E":
                            objects.put(OBJECT_RAMP_TRI_E_NAME, o.getKid());
                            break;
                        case "TILE-RAMP-TRI-W":
                            objects.put(OBJECT_RAMP_TRI_W_NAME, o.getKid());
                            break;
                        case "TILE-RAMP-SINGLE":
                            objects.put(OBJECT_RAMP_SINGLE_NAME, o.getKid());
                            break;
                        case "TILE-RAMP-PERP-N":
                            objects.put(OBJECT_RAMP_PERP_N_NAME, o.getKid());
                            break;
                        case "TILE-RAMP-PERP-E":
                            objects.put(OBJECT_RAMP_PERP_E_NAME, o.getKid());
                            break;
                        case "TILE-RAMP-PERP-S":
                            objects.put(OBJECT_RAMP_PERP_S_NAME, o.getKid());
                            break;
                        case "TILE-RAMP-PERP-W":
                            objects.put(OBJECT_RAMP_PERP_W_NAME, o.getKid());
                            break;
                        case "TILE-RAMP-EDGE-OUT-NE":
                            objects.put(OBJECT_RAMP_EDGE_OUT_NE_NAME, o.getKid());
                            break;
                        case "TILE-RAMP-EDGE-OUT-NW":
                            objects.put(OBJECT_RAMP_EDGE_OUT_NW_NAME, o.getKid());
                            break;
                        case "TILE-RAMP-EDGE-OUT-SE":
                            objects.put(OBJECT_RAMP_EDGE_OUT_SE_NAME, o.getKid());
                            break;
                        case "TILE-RAMP-EDGE-OUT-SW":
                            objects.put(OBJECT_RAMP_EDGE_OUT_SW_NAME, o.getKid());
                            break;
                        case "TILE-RAMP-EDGE-IN-NE":
                            objects.put(OBJECT_RAMP_EDGE_IN_NE_NAME, o.getKid());
                            break;
                        case "TILE-RAMP-EDGE-IN-NW":
                            objects.put(OBJECT_RAMP_EDGE_IN_NW_NAME, o.getKid());
                            break;
                        case "TILE-RAMP-EDGE-IN-SE":
                            objects.put(OBJECT_RAMP_EDGE_IN_SE_NAME, o.getKid());
                            break;
                        case "TILE-RAMP-EDGE-IN-SW":
                            objects.put(OBJECT_RAMP_EDGE_IN_SW_NAME, o.getKid());
                            break;
                        case "TILE-RAMP-CORNER-NE":
                            objects.put(OBJECT_RAMP_CORNER_NE_NAME, o.getKid());
                            break;
                        case "TILE-RAMP-CORNER-NW":
                            objects.put(OBJECT_RAMP_CORNER_NW_NAME, o.getKid());
                            break;
                        case "TILE-RAMP-CORNER-SE":
                            objects.put(OBJECT_RAMP_CORNER_SE_NAME, o.getKid());
                            break;
                        case "TILE-RAMP-CORNER-SW":
                            objects.put(OBJECT_RAMP_CORNER_SW_NAME, o.getKid());
                            break;
                        case "TILE-BLOCK":
                            objects.put(OBJECT_BLOCK_NAME, o.getKid());
                            break;
                        }
                    });
                }).toCompletableFuture() //
        ) //
                .get(30, TimeUnit.SECONDS);
    }

    private void knowledgeGet(Iterable<KnowledgeObject> res, MutableLongList dest, Consumer<KnowledgeObject> consume) {
        for (var o : res) {
            dest.add(o.getKid());
            consume.accept(o);
        }
    }

    public StatefulSession createSession() {
        var session = knowledge.newStatefulSession();
        return session;
    }
}
