package com.anrisoftware.dwarfhustle.model.db.orientdb.objects;

import com.anrisoftware.dwarfhustle.model.api.GameMapPosition;
import com.anrisoftware.dwarfhustle.model.api.GameObject;
import com.anrisoftware.dwarfhustle.model.api.GameObjectStorage;
import com.orientechnologies.orient.core.record.OVertex;

/**
 * Saves and loads the attributes of a {@link GameObject} from the database.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public class AbstractGameObjectStorage implements GameObjectStorage {

	@Override
	public void save(Object o, GameObject go) {
		if (!go.isDirty()) {
			return;
		}
		var v = (OVertex) o;
		v.setProperty("x", go.getPos().getX());
		v.setProperty("y", go.getPos().getY());
		v.setProperty("z", go.getPos().getZ());
		go.setDirty(false);
	}

	@Override
	public void load(Object o, GameObject go) {
		var v = (OVertex) o;
		go.setPos(new GameMapPosition(v.getProperty("x"), v.getProperty("y"), v.getProperty("z")));
		go.setDirty(false);
	}

}
