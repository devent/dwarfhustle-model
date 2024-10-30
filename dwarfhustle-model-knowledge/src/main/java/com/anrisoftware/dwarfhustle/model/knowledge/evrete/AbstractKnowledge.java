package com.anrisoftware.dwarfhustle.model.knowledge.evrete;

import static java.util.concurrent.CompletableFuture.allOf;
import static org.apache.commons.lang3.function.Consumers.nop;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

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

import com.anrisoftware.dwarfhustle.model.api.map.BlockObject;
import com.anrisoftware.dwarfhustle.model.api.materials.BlockMaterial;
import com.anrisoftware.dwarfhustle.model.api.materials.Gas;
import com.anrisoftware.dwarfhustle.model.api.materials.Liquid;
import com.anrisoftware.dwarfhustle.model.api.materials.Soil;
import com.anrisoftware.dwarfhustle.model.api.materials.Stone;
import com.anrisoftware.dwarfhustle.model.api.objects.KnowledgeObject;
import com.anrisoftware.dwarfhustle.model.api.objects.MapBlockFlags;
import com.anrisoftware.dwarfhustle.model.api.objects.NeighboringDir;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * 
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
public class AbstractKnowledge {

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

    protected final MutableIntObjectMap<MutableIntList> materials;

    protected final MutableIntIntMap objects;

    public AbstractKnowledge() {
        this.materials = IntObjectMaps.mutable.empty();
        this.objects = IntIntMaps.mutable.ofInitialCapacity(100);
    }

    public KnowledgeService createKnowledgeService() {
        var conf = createKnowledgeConf();
        return new KnowledgeService(conf);
    }

    protected Configuration createKnowledgeConf() {
        var conf = new Configuration();
        conf.setProperty("evrete.core.parallelism", "1");
        conf.addImport(NeighboringDir.class);
        for (var d : NeighboringDir.values()) {
            conf.addImport(String.format("static %s.%s", NeighboringDir.class.getName(), d.name()));
        }
        conf.addImport(MapBlockFlags.class);
        for (var d : MapBlockFlags.values()) {
            conf.addImport(String.format("static %s.%s", MapBlockFlags.class.getName(), d.name()));
        }
        return conf;
    }

    public MutableIntList getMaterials(int name) {
        return materials.get(name);
    }

    public int getObject(int name) {
        return objects.get(name);
    }

    @SneakyThrows
    public Knowledge createRulesKnowledgeFromSource(KnowledgeService service, String name) {
        var rulesetUrl = AbstractKnowledge.class.getResource(name);
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

    @SneakyThrows
    public void loadKnowledges(AskKnowledge ask) {
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
