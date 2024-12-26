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
package com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl;

import static java.util.concurrent.CompletableFuture.allOf;
import static org.apache.commons.lang3.function.Consumers.nop;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.eclipse.collections.api.factory.primitive.IntIntMaps;
import org.eclipse.collections.api.factory.primitive.IntLists;
import org.eclipse.collections.api.factory.primitive.IntObjectMaps;
import org.eclipse.collections.api.list.primitive.IntList;
import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.api.map.primitive.IntIntMap;
import org.eclipse.collections.api.map.primitive.IntObjectMap;
import org.eclipse.collections.api.map.primitive.MutableIntIntMap;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;

import com.anrisoftware.dwarfhustle.model.api.map.BlockObject;
import com.anrisoftware.dwarfhustle.model.api.materials.BlockMaterial;
import com.anrisoftware.dwarfhustle.model.api.materials.Gas;
import com.anrisoftware.dwarfhustle.model.api.materials.Liquid;
import com.anrisoftware.dwarfhustle.model.api.materials.Soil;
import com.anrisoftware.dwarfhustle.model.api.materials.Stone;
import com.anrisoftware.dwarfhustle.model.api.objects.KnowledgeObject;
import com.anrisoftware.dwarfhustle.model.knowledge.evrete.AskKnowledge;

import lombok.SneakyThrows;

/**
 * Loads objects and material.
 * 
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public class DefaultLoadKnowledges {

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

    public static final int OBJECT_RAMP_TWO_NE_NAME = "object-ramp-two-ne".hashCode();

    public static final int OBJECT_RAMP_TWO_SE_NAME = "object-ramp-two-se".hashCode();

    protected static final Duration ASK_TIMEOUT = Duration.of(10, ChronoUnit.SECONDS);

    protected final IntObjectMap<IntList> materials;

    protected final IntIntMap objects;

    public DefaultLoadKnowledges() {
        this.materials = IntObjectMaps.mutable.empty();
        this.objects = IntIntMaps.mutable.ofInitialCapacity(100);
    }

    public IntList getMaterials(int name) {
        return materials.get(name);
    }

    public IntObjectMap<IntList> getMaterials() {
        return materials;
    }

    public int getObject(int name) {
        return objects.get(name);
    }

    public IntIntMap getObjects() {
        return objects;
    }

    @SneakyThrows
    public void loadKnowledges(AskKnowledge ask) {
        var objects = (MutableIntIntMap) this.objects;
        var materials = (MutableIntObjectMap<IntList>) this.materials;
        MutableIntList solids = IntLists.mutable.withInitialCapacity(100);
        MutableIntList liquids = IntLists.mutable.withInitialCapacity(100);
        MutableIntList gases = IntLists.mutable.withInitialCapacity(100);
        MutableIntList oxygen = IntLists.mutable.withInitialCapacity(1);
        materials.put(MATERIALS_SOLIDS_NAME, solids);
        materials.put(MATERIALS_LIQUIDS_NAME, liquids);
        materials.put(MATERIALS_GASES_NAME, gases);
        materials.put(MATERIAL_OXYGEN_NAME, oxygen);
        allOf( //
                ask.doAskAsync(ASK_TIMEOUT, Stone.TYPE).whenComplete((res, ex) -> {
                    knowledgeGet(res, solids, nop());
                }).toCompletableFuture(), //
                ask.doAskAsync(ASK_TIMEOUT, Soil.TYPE).whenComplete((res, ex) -> {
                    knowledgeGet(res, solids, nop());
                }).toCompletableFuture(), //
                ask.doAskAsync(ASK_TIMEOUT, Liquid.TYPE).whenComplete((res, ex) -> {
                    knowledgeGet(res, liquids, nop());
                }).toCompletableFuture(), //
                ask.doAskAsync(ASK_TIMEOUT, Gas.TYPE).whenComplete((res, ex) -> {
                    knowledgeGet(res, gases, (o) -> {
                        var mo = (BlockMaterial) o;
                        if (mo.getName().equalsIgnoreCase("oxygen")) {
                            oxygen.add(o.getKid());
                        }
                    });
                }).toCompletableFuture(), //
                ask.doAskAsync(ASK_TIMEOUT, BlockObject.TYPE).whenComplete((res, ex) -> {
                    knowledgeGet(res, IntLists.mutable.empty(), (o) -> {
                        var ot = (BlockObject) o;
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
                        case "BLOCK-RAMP-TWO-NE":
                            objects.put(OBJECT_RAMP_TWO_NE_NAME, o.getKid());
                            break;
                        case "BLOCK-RAMP-TWO-SE":
                            objects.put(OBJECT_RAMP_TWO_SE_NAME, o.getKid());
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

    protected void knowledgeGet(Iterable<KnowledgeObject> res, MutableIntList dest, Consumer<KnowledgeObject> consume) {
        for (var o : res) {
            dest.add(o.getKid());
            consume.accept(o);
        }
    }
}
