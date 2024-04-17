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
