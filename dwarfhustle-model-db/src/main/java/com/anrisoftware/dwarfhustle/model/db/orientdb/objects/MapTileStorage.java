package com.anrisoftware.dwarfhustle.model.db.orientdb.objects;

import com.anrisoftware.dwarfhustle.model.api.GameObject;
import com.anrisoftware.dwarfhustle.model.api.MapTile;
import com.orientechnologies.orient.core.record.OVertex;

/**
 * Saves and loads the attributes of a {@link MapTileS} from the database.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public class MapTileStorage extends AbstractGameObjectStorage {

	@Override
	public void save(Object o, GameObject go) {
		if (!go.isDirty()) {
			return;
		}
		var v = (OVertex) o;
		var mt = (MapTile) go;
		v.setProperty("material", mt.getMaterial());
		super.save(o, go);
	}

	@Override
	public void load(Object o, GameObject go) {
		var v = (OVertex) o;
		var mt = (MapTile) go;
		mt.setMaterial(v.getProperty("material"));
		super.load(o, go);
	}
}
