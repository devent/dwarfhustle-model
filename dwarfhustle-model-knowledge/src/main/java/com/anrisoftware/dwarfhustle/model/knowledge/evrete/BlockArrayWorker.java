package com.anrisoftware.dwarfhustle.model.knowledge.evrete;

import java.io.IOException;
import java.util.function.Function;

import org.evrete.Configuration;
import org.evrete.KnowledgeService;
import org.evrete.api.Knowledge;
import org.evrete.util.CompilationException;

import com.anrisoftware.dwarfhustle.model.api.objects.MapChunk;
import com.anrisoftware.dwarfhustle.model.api.objects.MapChunksStore;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class BlockArrayWorker {

    private final MapChunksStore store;

    private Knowledge knowledge;

    public void createKnowledge() throws IOException {
        var conf = new Configuration();
        var service = new KnowledgeService(conf);
        var rulesetUrl = BlockArrayWorker.class.getResource("BlockArrayRules.java");
        assert rulesetUrl != null;
        try {
            this.knowledge = service.newKnowledge("JAVA-SOURCE", rulesetUrl);
        } catch (IllegalStateException e) {
            if (e.getCause() instanceof CompilationException ce) {
                ce.getErrorSources().forEach((c) -> {
                    log.error("{} - {}", c, ce.getErrorMessage(c));
                });
            }
        }
    }

    public void runRules() {
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
                            session.insert(new BlockFact(chunk, x, y, z, retriever));
                        }
                    }
                }
                session.fire();
                session.clear();
            }
        });
        session.close();
    }
}
