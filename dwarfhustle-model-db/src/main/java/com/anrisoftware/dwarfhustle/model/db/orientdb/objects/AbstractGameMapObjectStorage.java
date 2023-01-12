package com.anrisoftware.dwarfhustle.model.db.orientdb.objects;

import static com.anrisoftware.dwarfhustle.model.db.orientdb.objects.GameObjectSchemaSchema.MAPID_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.objects.GameObjectSchemaSchema.X_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.objects.GameObjectSchemaSchema.Y_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.objects.GameObjectSchemaSchema.Z_FIELD;

import com.anrisoftware.dwarfhustle.model.api.GameMapObject;
import com.anrisoftware.dwarfhustle.model.api.GameMapPos;
import com.anrisoftware.dwarfhustle.model.api.GameObject;
import com.anrisoftware.dwarfhustle.model.api.MapTile;
import com.orientechnologies.orient.core.record.OVertex;

/**
 * Saves and loads the attributes of a {@link GameMapObject} from the database.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public class AbstractGameMapObjectStorage extends AbstractGameObjectStorage {

	@Override
	public void save(Object o, GameObject go) {
		if (!go.isDirty()) {
			return;
		}
		var v = (OVertex) o;
		var gmo = (GameMapObject) go;
		v.setProperty(MAPID_FIELD, gmo.getPos().getMapid());
		v.setProperty(X_FIELD, gmo.getPos().getX());
		v.setProperty(Y_FIELD, gmo.getPos().getY());
		v.setProperty(Z_FIELD, gmo.getPos().getZ());
		super.save(o, go);
	}

	@Override
	public GameObject load(Object o, GameObject go) {
		var v = (OVertex) o;
		var gmo = (GameMapObject) go;
		gmo.setPos(new GameMapPos(v.getProperty(MAPID_FIELD), v.getProperty(X_FIELD), v.getProperty(Y_FIELD),
				v.getProperty(Z_FIELD)));
		gmo.setRid(v.getIdentity());
		return super.load(o, go);
	}

	@Override
	public GameObject create() {
		return new MapTile();
	}
}
