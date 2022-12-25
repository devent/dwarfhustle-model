package com.anrisoftware.dwarfhustle.model.db.orientdb.objects;

import static com.anrisoftware.dwarfhustle.model.db.orientdb.objects.GameObjectSchemaSchema.MAPID_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.objects.GameObjectSchemaSchema.OBJECTID_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.objects.GameObjectSchemaSchema.OBJECTTYPE_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.objects.GameObjectSchemaSchema.X_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.objects.GameObjectSchemaSchema.Y_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.objects.GameObjectSchemaSchema.Z_FIELD;

import com.anrisoftware.dwarfhustle.model.api.GameMapPosition;
import com.anrisoftware.dwarfhustle.model.api.GameObject;
import com.anrisoftware.dwarfhustle.model.api.GameObjectStorage;
import com.orientechnologies.orient.core.record.OVertex;

/**
 * Saves and loads the attributes of a {@link GameObject} from the database.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public abstract class AbstractGameObjectStorage implements GameObjectStorage {

	@Override
	public void save(Object o, GameObject go) {
		if (!go.isDirty()) {
			return;
		}
		var v = (OVertex) o;
		v.setProperty(OBJECTID_FIELD, go.getId());
		v.setProperty(OBJECTTYPE_FIELD, go.getType());
		v.setProperty(MAPID_FIELD, go.getPos().getMapid());
		v.setProperty(X_FIELD, go.getPos().getX());
		v.setProperty(Y_FIELD, go.getPos().getY());
		v.setProperty(Z_FIELD, go.getPos().getZ());
		go.setDirty(false);
	}

	@Override
	public GameObject load(Object o, GameObject go) {
		var v = (OVertex) o;
		go.setId(v.getProperty(OBJECTID_FIELD));
		go.setPos(new GameMapPosition(v.getProperty(MAPID_FIELD), v.getProperty(X_FIELD), v.getProperty(Y_FIELD),
				v.getProperty(Z_FIELD)));
		go.setRid(v.getIdentity());
		go.setDirty(false);
		return go;
	}
}
