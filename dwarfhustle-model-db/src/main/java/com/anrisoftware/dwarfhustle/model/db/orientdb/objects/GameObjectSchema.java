package com.anrisoftware.dwarfhustle.model.db.orientdb.objects;

import com.anrisoftware.dwarfhustle.model.api.GameObject;

/**
 * Creates the schema for the {@link GameObject}.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public interface GameObjectSchema {

	void createSchema(Object db);
}
