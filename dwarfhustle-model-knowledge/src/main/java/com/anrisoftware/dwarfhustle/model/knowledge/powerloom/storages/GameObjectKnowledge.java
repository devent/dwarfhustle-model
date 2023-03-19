package com.anrisoftware.dwarfhustle.model.knowledge.powerloom.storages;

import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;

/**
 * Retrieves {@link GameObject} game objects from the knowledge base.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public interface GameObjectKnowledge {

    GameObject retrieve(Object db, Object o, GameObject go);
}
