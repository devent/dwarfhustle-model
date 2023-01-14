package com.anrisoftware.dwarfhustle.model.db.orientdb.objects;

import static com.anrisoftware.dwarfhustle.model.db.orientdb.objects.GameObjectSchemaSchema.OBJECTID_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.objects.GameObjectSchemaSchema.OBJECTTYPE_FIELD;

import com.anrisoftware.dwarfhustle.model.api.GameObject;
import com.anrisoftware.dwarfhustle.model.api.GameObjectStorage;
import com.orientechnologies.orient.core.record.OElement;

/**
 * Saves and loads the attributes of a {@link GameObject} from the database.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public abstract class AbstractGameObjectStorage implements GameObjectStorage {

	@Override
	public void save(Object db, Object o, GameObject go) {
		if (!go.isDirty()) {
			return;
		}
		var v = (OElement) o;
		v.setProperty(OBJECTID_FIELD, go.getId());
		v.setProperty(OBJECTTYPE_FIELD, go.getObjectType());
		go.setDirty(false);
	}

	@Override
	public GameObject load(Object db, Object o, GameObject go) {
		var v = (OElement) o;
		go.setId(v.getProperty(OBJECTID_FIELD));
		go.setDirty(false);
		return go;
	}
}
