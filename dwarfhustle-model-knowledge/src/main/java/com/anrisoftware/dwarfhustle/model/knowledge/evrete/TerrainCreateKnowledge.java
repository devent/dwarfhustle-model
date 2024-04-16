package com.anrisoftware.dwarfhustle.model.knowledge.evrete;

import java.io.IOException;

import org.evrete.KnowledgeService;
import org.evrete.api.Knowledge;
import org.evrete.api.StatefulSession;
import org.evrete.util.CompilationException;

/**
 * Knowledge rules to create a terrain.
 */
public class TerrainCreateKnowledge {

    private Knowledge knowledge;

    public TerrainCreateKnowledge() throws IOException {
        var service = new KnowledgeService();
        var rulesetUrl = TerrainCreateKnowledge.class.getResource("TerrainCreateRules.java");
        assert rulesetUrl != null;
        try {
            this.knowledge = service.newKnowledge("JAVA-SOURCE", rulesetUrl);
        } catch (IllegalStateException e) {
            if (e.getCause() instanceof CompilationException ce) {
                ce.getErrorSources().forEach((c) -> {
                    System.out.println(c); // TODO
                    System.out.println(ce.getErrorMessage(c)); // TODO
                });
            }
        }
    }

    public StatefulSession createSession() {
        return knowledge.newStatefulSession();
    }
}
