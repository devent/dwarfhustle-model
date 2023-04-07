package com.anrisoftware.dwarfhustle.model.knowledge.powerloom.storages;

import com.anrisoftware.dwarfhustle.model.api.materials.KnowledgeObject;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;

/**
 * Retrieves {@link KnowledgeObject} knowledge objects from the knowledge base.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public interface GameObjectKnowledge {

    KnowledgeObject retrieve(Object o, GameObject go);

    /**
     * Returns a new {@link KnowledgeObject}.
     */
    KnowledgeObject create();
}
