package com.anrisoftware.dwarfhustle.model.knowledge.evrete;

import static com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.DefaultLoadKnowledges.MATERIALS_NAME;
import static com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.DefaultLoadKnowledges.OBJECTS_NAME;

import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.api.map.primitive.MutableIntIntMap;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.evrete.Configuration;
import org.evrete.KnowledgeService;
import org.evrete.api.Knowledge;
import org.evrete.util.CompilationException;

import com.anrisoftware.dwarfhustle.model.api.objects.MapBlockFlags;
import com.anrisoftware.dwarfhustle.model.api.objects.NeighboringDir;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.DefaultLoadKnowledges;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * 
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
public class AbstractKnowledge {

    protected DefaultLoadKnowledges loadedKnowledges;

    public MutableIntList getMaterials(int name) {
        return loadedKnowledges.getMaterials(name);
    }

    public MutableIntObjectMap<MutableIntList> getMaterials() {
        return loadedKnowledges.getMaterials();
    }

    public int getObject(int name) {
        return loadedKnowledges.getObject(name);
    }

    public MutableIntIntMap getObjects() {
        return loadedKnowledges.getObjects();
    }

    public void setLoadedKnowledges(DefaultLoadKnowledges loadedKnowledges) {
        this.loadedKnowledges = loadedKnowledges;
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

    @SneakyThrows
    public Knowledge createRulesKnowledgeFromSource(KnowledgeService service, String name) {
        var rulesetUrl = AbstractKnowledge.class.getResource(name);
        assert rulesetUrl != null;
        try {
            var knowledge = service.newKnowledge("JAVA-SOURCE", rulesetUrl);
            knowledge.set(MATERIALS_NAME, loadedKnowledges.getMaterials());
            knowledge.set(OBJECTS_NAME, loadedKnowledges.getObjects());
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

}
