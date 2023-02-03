/*
 * dwarfhustle-model-db - Manages the compile dependencies for the model.
 * Copyright © 2023 Erwin Müller (erwin.mueller@anrisoftware.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.anrisoftware.dwarfhustle.model.db.orientdb.storages;

import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapBlockSchema.BLOCKS_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapBlockSchema.BLOCK_ID_CLASS;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapBlockSchema.END_X_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapBlockSchema.END_Y_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapBlockSchema.END_Z_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapBlockSchema.MAPID_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapBlockSchema.OBJECTID_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapBlockSchema.START_X_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapBlockSchema.START_Y_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapBlockSchema.START_Z_FIELD;
import static com.anrisoftware.dwarfhustle.model.db.orientdb.schemas.MapBlockSchema.TILES_FIELD;

import java.util.Map;

import javax.inject.Inject;

import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.map.primitive.MutableObjectLongMap;
import org.eclipse.collections.impl.factory.Maps;
import org.eclipse.collections.impl.factory.primitive.ObjectLongMaps;

import com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos;
import com.anrisoftware.dwarfhustle.model.api.objects.GameMapPos;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObjectStorage;
import com.anrisoftware.dwarfhustle.model.api.objects.MapBlock;
import com.anrisoftware.dwarfhustle.model.api.objects.MapTile;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.OElement;

/**
 * Stores and retrieves the properties of a {@link MapBlock} to/from the
 * database. Does not commit the changes into the database.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public class MapBlockStorage extends AbstractGameObjectStorage {

	private GameObjectStorage mapTileStorage;

	@Inject
	public void setStorages(Map<String, GameObjectStorage> storages) {
		this.mapTileStorage = storages.get(MapTile.OBJECT_TYPE);
	}

	@Override
	public void store(Object db, Object o, GameObject go) {
		if (!go.isDirty()) {
			return;
		}
		var v = (OElement) o;
		var mb = (MapBlock) go;
		var odb = (ODatabaseDocument) db;
		Map<String, OElement> blocks = Maps.mutable.ofInitialCapacity(mb.getBlocks().size());
		mb.getBlocks().forEachKeyValue((pos, id) -> {
			var blockid = odb.newElement(BLOCK_ID_CLASS);
			blockid.setProperty(OBJECTID_FIELD, id);
			blocks.put(pos.toSaveString(), blockid);
		});
		v.setProperty(BLOCKS_FIELD, blocks, OType.EMBEDDEDMAP);
		Map<String, OElement> tiles = Maps.mutable.ofInitialCapacity(mb.getBlocks().size());
		mb.getTiles().forEachKeyValue((pos, tile) -> {
			var vtile = odb.newVertex(MapTile.OBJECT_TYPE);
			mapTileStorage.store(db, vtile, tile);
			tiles.put(tile.getPos().toSaveString(), vtile);
		});
		v.setProperty(TILES_FIELD, tiles, OType.EMBEDDEDMAP);
		v.setProperty(MAPID_FIELD, mb.getPos().getMapid());
		v.setProperty(START_X_FIELD, mb.getPos().getX());
		v.setProperty(START_Y_FIELD, mb.getPos().getY());
		v.setProperty(START_Z_FIELD, mb.getPos().getZ());
		v.setProperty(END_X_FIELD, mb.getPos().getEndPos().getX());
		v.setProperty(END_Y_FIELD, mb.getPos().getEndPos().getY());
		v.setProperty(END_Z_FIELD, mb.getPos().getEndPos().getZ());
		super.store(db, o, go);
	}

	@Override
	public GameObject retrieve(Object db, Object o, GameObject go) {
		var v = (OElement) o;
		var mb = (MapBlock) go;
		Map<String, OElement> oblocks = v.getProperty(BLOCKS_FIELD);
		MutableObjectLongMap<GameBlockPos> blocks = ObjectLongMaps.mutable.ofInitialCapacity(oblocks.size());
		for (Map.Entry<String, OElement> oblock : oblocks.entrySet()) {
			var pos = GameBlockPos.parse(oblock.getKey());
			long id = oblock.getValue().getProperty(OBJECTID_FIELD);
			blocks.put(pos, id);
		}
		Map<String, OElement> otiles = v.getProperty(TILES_FIELD);
		MutableMap<GameMapPos, MapTile> tiles = Maps.mutable.ofInitialCapacity(otiles.size());
		for (Map.Entry<String, OElement> otile : otiles.entrySet()) {
			var pos = GameMapPos.parse(otile.getKey());
			var tile = (MapTile) mapTileStorage.retrieve(db, otile.getValue(), mapTileStorage.create());
			tiles.put(pos, tile);
		}
		mb.setBlocks(blocks.asUnmodifiable());
		mb.setTiles(tiles.asUnmodifiable());
		mb.setPos(new GameBlockPos(v.getProperty(MAPID_FIELD), v.getProperty(START_X_FIELD),
				v.getProperty(START_Y_FIELD), v.getProperty(START_Z_FIELD), v.getProperty(END_X_FIELD),
				v.getProperty(END_Y_FIELD), v.getProperty(END_Z_FIELD)));
		return super.retrieve(db, o, go);
	}

	@Override
	public GameObject create() {
		return new MapBlock();
	}
}
