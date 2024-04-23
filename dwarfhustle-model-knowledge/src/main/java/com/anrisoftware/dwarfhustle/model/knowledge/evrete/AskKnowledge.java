package com.anrisoftware.dwarfhustle.model.knowledge.evrete;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;
import com.anrisoftware.dwarfhustle.model.api.objects.KnowledgeObject;

/**
 * Ask knowledge.
 */
@FunctionalInterface
public interface AskKnowledge {

    /**
     * Ask knowledge asynchronously.
     * 
     * @param timeout   the {@link Duration} timeout.
     * @param typeClass the type of the {@link KnowledgeObject}.
     * @param type      the type of the {@link KnowledgeObject}.
     * @return the {@link CompletionStage} that returns all
     *         {@link KnowledgeObject}s.
     */
    CompletionStage<Iterable<KnowledgeObject>> doAskAsync(Duration timeout, Class<? extends GameObject> typeClass,
            String type);

}
