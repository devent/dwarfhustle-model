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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.MutableMap;
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

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * Knowledge rules to create a terrain.
 */
@Slf4j
public class TerrainCreateKnowledge {

    public static final String MATERIAL_OXYGEN_NAME = "material-oxygen";

    public static final String MATERIALS_GASES_NAME = "materials-gases";

    public static final String MATERIALS_LIQUIDS_NAME = "materials-liquids";

    public static final String MATERIALS_SOLIDS_NAME = "materials-solids";

    public static final String OBJECT_BLOCK_NAME = "object-block";

    public static final String OBJECT_RAMP_SINGLE_NAME = "object-ramp-single";

    public static final String OBJECT_RAMP_NESW_NAME = "object-ramp-nesw";

    public static final String OBJECT_RAMP_EDGE_NAME = "object-ramp-edge";

    private static final Duration ASK_TIMEOUT = Duration.of(10, ChronoUnit.SECONDS);

    private Knowledge knowledge;

    private final MutableMap<String, List<Long>> materials;

    public TerrainCreateKnowledge(AskKnowledge askKnowledge) throws IOException {
        this.materials = Maps.mutable.empty();
        loadKnowledges(askKnowledge, materials);
        var service = new KnowledgeService();
        var rulesetUrl = TerrainCreateKnowledge.class.getResource("TerrainCreateRules.java");
        assert rulesetUrl != null;
        try {
            this.knowledge = service.newKnowledge("JAVA-SOURCE", rulesetUrl);
            knowledge.set(MATERIALS_SOLIDS_NAME, materials.get(MATERIALS_SOLIDS_NAME));
            knowledge.set(MATERIALS_LIQUIDS_NAME, materials.get(MATERIALS_LIQUIDS_NAME));
            knowledge.set(MATERIALS_GASES_NAME, materials.get(MATERIALS_GASES_NAME));
            knowledge.set(MATERIAL_OXYGEN_NAME, materials.get(MATERIAL_OXYGEN_NAME).get(0));
            knowledge.set(OBJECT_BLOCK_NAME, materials.get(OBJECT_BLOCK_NAME).get(0));
            knowledge.set(OBJECT_RAMP_SINGLE_NAME, materials.get(OBJECT_RAMP_SINGLE_NAME).get(0));
            knowledge.set(OBJECT_RAMP_NESW_NAME, materials.get(OBJECT_RAMP_NESW_NAME).get(0));
            knowledge.set(OBJECT_RAMP_EDGE_NAME, materials.get(OBJECT_RAMP_EDGE_NAME).get(0));
        } catch (IllegalStateException e) {
            if (e.getCause() instanceof CompilationException ce) {
                ce.getErrorSources().forEach((c) -> {
                    log.error("{} - {}", c, ce.getErrorMessage(c));
                });
            }
        }
    }

    @SneakyThrows
    private void loadKnowledges(AskKnowledge ask, MutableMap<String, List<Long>> materials) {
        List<Long> solids = new ArrayList<>(100);
        List<Long> liquids = new ArrayList<>(100);
        List<Long> gases = new ArrayList<>(100);
        List<Long> objects = new ArrayList<>(100);
        List<Long> oxygen = new ArrayList<>(1);
        List<Long> block = new ArrayList<>(1);
        List<Long> ramp_single = new ArrayList<>(1);
        List<Long> ramp_nesw = new ArrayList<>(1);
        List<Long> ramp_edge = new ArrayList<>(1);
        materials.put(MATERIALS_SOLIDS_NAME, solids);
        materials.put(MATERIALS_LIQUIDS_NAME, liquids);
        materials.put(MATERIALS_GASES_NAME, gases);
        materials.put(MATERIAL_OXYGEN_NAME, oxygen);
        materials.put(OBJECT_BLOCK_NAME, block);
        materials.put(OBJECT_RAMP_SINGLE_NAME, ramp_single);
        materials.put(OBJECT_RAMP_NESW_NAME, ramp_nesw);
        materials.put(OBJECT_RAMP_EDGE_NAME, ramp_edge);
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
                    knowledgeGet(res, objects, (o) -> {
                        var ot = (ObjectType) o;
                        if (ot.getName().equalsIgnoreCase("tile-block")) {
                            block.add(o.getKid());
                        } else if (ot.getName().equalsIgnoreCase("tile-ramp-single")) {
                            ramp_single.add(o.getKid());
                        } else if (ot.getName().equalsIgnoreCase("tile-ramp-nesw")) {
                            ramp_nesw.add(o.getKid());
                        } else if (ot.getName().equalsIgnoreCase("tile-ramp-edge")) {
                            ramp_edge.add(o.getKid());
                        }
                    });
                }).toCompletableFuture() //
        ) //
                .get(30, TimeUnit.SECONDS);
    }

    private void knowledgeGet(Iterable<KnowledgeObject> res, List<Long> list, Consumer<KnowledgeObject> consume) {
        for (var o : res) {
            list.add(o.getKid());
            consume.accept(o);
        }
    }

    public StatefulSession createSession() {
        var session = knowledge.newStatefulSession();
        return session;
    }
}
