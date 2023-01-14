package com.anrisoftware.dwarfhustle.model.db.orientdb.objects;

import com.anrisoftware.dwarfhustle.model.api.GameObject;
import com.anrisoftware.dwarfhustle.model.api.MapTile;
import com.orientechnologies.orient.core.record.OElement;

/**
 * Saves and loads the attributes of a {@link MapTile} from the database.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public class MapTileStorage extends AbstractGameMapObjectStorage {

	@Override
	public void save(Object db, Object o, GameObject go) {
		if (!go.isDirty()) {
			return;
		}
		var v = (OElement) o;
		var mt = (MapTile) go;
		v.setProperty(MapTileSchema.MATERIAL_FIELD, mt.getMaterial());
		super.save(db, o, go);
	}

	@Override
	public GameObject load(Object db, Object o, GameObject go) {
		var v = (OElement) o;
		var mt = (MapTile) go;
		mt.setMaterial(v.getProperty("material"));
		return super.load(db, o, go);
	}

	@Override
	public GameObject create() {
		return new MapTile();
	}
}
