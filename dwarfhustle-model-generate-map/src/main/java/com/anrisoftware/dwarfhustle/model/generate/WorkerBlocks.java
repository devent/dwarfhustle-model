/*
 * dwarfhustle-model-generate-map - Manages the compile dependencies for the model.
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
package com.anrisoftware.dwarfhustle.model.generate;

import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.jcs3.access.CacheAccess;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.map.primitive.MutableObjectLongMap;
import org.eclipse.collections.impl.factory.Maps;
import org.eclipse.collections.impl.factory.primitive.ObjectLongMaps;
import org.lable.oss.uniqueid.GeneratorException;
import org.lable.oss.uniqueid.IDGenerator;

import com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos;
import com.anrisoftware.dwarfhustle.model.api.objects.GameMap;
import com.anrisoftware.dwarfhustle.model.api.objects.GameMapPos;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObjectStorage;
import com.anrisoftware.dwarfhustle.model.api.objects.MapBlock;
import com.anrisoftware.dwarfhustle.model.api.objects.MapTile;
import com.google.inject.assistedinject.Assisted;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.intent.OIntentMassiveInsert;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
public class WorkerBlocks {

	/**
	 * Factory to create {@link WorkerBlocks}.
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	public interface WorkerBlocksFactory {

		WorkerBlocks create(CacheAccess<GameBlockPos, MapBlock> cache, OrientDB orientdb);
	}

	@Inject
	@Assisted
	private CacheAccess<GameBlockPos, MapBlock> cache;

	@Inject
	@Assisted
	private OrientDB orientdb;

	@Inject
	private IDGenerator generator;

	private int blocksDone;

	private boolean generateDone;

	private GameObjectStorage mapBlockStore;

	private GameObjectStorage gameMapStore;

	public WorkerBlocks() {
	}

	@Inject
	public void setStorages(Map<String, GameObjectStorage> storages) {
		this.gameMapStore = storages.get(GameMap.OBJECT_TYPE);
		this.mapBlockStore = storages.get(MapBlock.OBJECT_TYPE);
	}

	public int getBlocksDone() {
		return blocksDone;
	}

	public boolean isGenerateDone() {
		return generateDone;
	}

	@SneakyThrows
	public void generate(GenerateMapMessage m) {
		log.debug("generate {}", m);
		this.blocksDone = 0;
		this.generateDone = false;
		int w1 = m.width;
		int h1 = m.height;
		int d1 = m.depth;
		var pos = pos(m, 0, 0, 0);
		var endPos = pos(m, w1, h1, d1);
		try (var db = orientdb.open(m.database, m.user, m.password)) {
			saveGameMap(m, db);
			db.declareIntent(new OIntentMassiveInsert());
			generateMapBlock(m, db, createBlocksMap(), pos, endPos);
			db.declareIntent(null);
		}
		this.generateDone = true;
		log.trace("generate done {}", m);
	}

	private void saveGameMap(GenerateMapMessage m, ODatabaseSession db) throws GeneratorException {
		var gamemap = new GameMap(generator.generate());
		gamemap.setName(m.name);
		gamemap.setWidth(m.width);
		gamemap.setHeight(m.height);
		gamemap.setDepth(m.depth);
		var v = db.newVertex(GameMap.OBJECT_TYPE);
		gameMapStore.save(db, v, gamemap);
		v.save();
	}

	private MapBlock generateMapBlock(GenerateMapMessage m, ODatabaseSession db,
			MutableObjectLongMap<GameBlockPos> parent, GameMapPos pos, GameMapPos endPos) throws GeneratorException {
		int w1 = endPos.getDiffX(pos);
		int h1 = endPos.getDiffY(pos);
		int d1 = endPos.getDiffZ(pos);
		int w2 = endPos.getDiffX(pos) / 2;
		int h2 = endPos.getDiffY(pos) / 2;
		int d2 = endPos.getDiffZ(pos) / 2;
		if (w2 == m.blockSize / 2) {
			var block = createBlock(m, db, pos, endPos);
			createMapTiles(db, block);
			cache.put(block.getPos(), block);
			saveBlock(db, block);
			blocksDone++;
			return block;
		}
		var block = createBlock(m, db, pos, endPos);
		parent.put(block.getPos(), block.getId());
		var map = createBlocksMap();
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		var b0 = generateMapBlock(m, db, map, pos(m, x, y, z), pos(m, x + w2, y + h2, z + d2));
		map.put(b0.getPos(), b0.getId());
		var b1 = generateMapBlock(m, db, map, pos(m, x + w2, y, z), pos(m, x + w1, y + h2, z + d2));
		map.put(b1.getPos(), b1.getId());
		var b2 = generateMapBlock(m, db, map, pos(m, x + w2, y, z + d2), pos(m, x + w1, y + h2, z + d1));
		map.put(b2.getPos(), b2.getId());
		var b3 = generateMapBlock(m, db, map, pos(m, x, y, z + d2), pos(m, x + w2, y + h2, z + d1));
		map.put(b3.getPos(), b3.getId());
		var b4 = generateMapBlock(m, db, map, pos(m, x, y + h2, z), pos(m, x + w2, y + h1, z + d2));
		map.put(b4.getPos(), b4.getId());
		var b5 = generateMapBlock(m, db, map, pos(m, x + w2, y + h2, z), pos(m, x + w1, y + h1, z + d2));
		map.put(b5.getPos(), b5.getId());
		var b6 = generateMapBlock(m, db, map, pos(m, x + w2, y + h2, z + d2), pos(m, x + w1, y + h1, z + d1));
		map.put(b6.getPos(), b6.getId());
		var b7 = generateMapBlock(m, db, map, pos(m, x, y + h2, z + d2), pos(m, x + w2, y + h1, z + d1));
		map.put(b7.getPos(), b7.getId());
		block.setBlocks(map.asUnmodifiable());
		cache.put(block.getPos(), block);
		blocksDone++;
		saveBlock(db, block);
		return block;
	}

	private void saveBlock(ODatabaseSession db, MapBlock block) {
		var v = db.newVertex(MapBlock.OBJECT_TYPE);
		mapBlockStore.save(db, v, block);
		v.save();
	}

	private void createMapTiles(ODatabaseSession db, MapBlock block) throws GeneratorException {
		int w = block.getEndPos().getDiffX(block.getPos());
		int h = block.getEndPos().getDiffY(block.getPos());
		int d = block.getEndPos().getDiffZ(block.getPos());
		int mapid = block.getPos().getMapid();
		var tiles = createTilesMap(w * h * d);
		var ids = generator.batch(w * h * d);
		for (int z = 0; z < d; z++) {
			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					int xx = x + block.getPos().getX();
					int yy = y + block.getPos().getY();
					int zz = z + block.getPos().getZ();
					var tile = new MapTile(ids.pop());
					tile.setPos(new GameMapPos(mapid, xx, yy, zz));
					tiles.put(tile.getPos(), tile);
				}
			}
		}
		block.setTiles(tiles.asUnmodifiable());
	}

	private GameMapPos pos(GenerateMapMessage m, int x, int y, int z) {
		return new GameMapPos(m.mapid, x, y, z);
	}

	private MutableObjectLongMap<GameBlockPos> createBlocksMap() {
		return ObjectLongMaps.mutable.ofInitialCapacity(8);
	}

	private MapBlock createBlock(GenerateMapMessage m, ODatabaseSession db, GameMapPos pos, GameMapPos endPos)
			throws GeneratorException {
		var block = new MapBlock(generator.generate());
		block.setPos(new GameBlockPos(pos, endPos));
		return block;
	}

	private MutableMap<GameMapPos, MapTile> createTilesMap(int n) {
		return Maps.mutable.ofInitialCapacity(n);
	}

}
