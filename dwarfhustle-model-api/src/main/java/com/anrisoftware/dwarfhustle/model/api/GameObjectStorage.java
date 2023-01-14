package com.anrisoftware.dwarfhustle.model.api;

/**
 * Saves and loads the attributes of a {@link GameObject} from the database.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public interface GameObjectStorage {

	void save(Object db, Object o, GameObject go);

	GameObject load(Object db, Object o, GameObject go);

	GameObject create();
}
