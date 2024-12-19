/*
 * dwarfhustle-model-knowledge - Manages the compile dependencies for the model.
 * Copyright © 2022-2024 Erwin Müller (erwin.mueller@anrisoftware.com)
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

import static com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.DefaultLoadKnowledges.MATERIALS_NAME;
import static com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.DefaultLoadKnowledges.OBJECTS_NAME;

import org.eclipse.collections.api.list.primitive.IntList;
import org.eclipse.collections.api.map.primitive.IntIntMap;
import org.eclipse.collections.api.map.primitive.IntObjectMap;
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

    public IntList getMaterials(int name) {
        return loadedKnowledges.getMaterials(name);
    }

    public IntObjectMap<IntList> getMaterials() {
        return loadedKnowledges.getMaterials();
    }

    public int getObject(int name) {
        return loadedKnowledges.getObject(name);
    }

    public IntIntMap getObjects() {
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
