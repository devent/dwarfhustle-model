/*
 * Copyright (C) 2021-2022 Erwin Müller <erwin@muellerpublic.de>
 * Released as open-source under the Apache License, Version 2.0.
 *
 * ****************************************************************************
 * ANL-OpenCL :: JME3 - App - Model
 * ****************************************************************************
 *
 * Copyright (C) 2021-2022 Erwin Müller <erwin@muellerpublic.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not USe this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ****************************************************************************
 * ANL-OpenCL :: JME3 - App - Model is a derivative work based on Josua Tippetts' C++ library:
 * http://accidentalnoise.sourceforge.net/index.html
 * ****************************************************************************
 *
 * Copyright (C) 2011 Joshua Tippetts
 *
 *   This software is provided 'as-is', without any express or implied
 *   warranty.  In no event will the authors be held liable for any damages
 *   arising from the USe of this software.
 *
 *   Permission is granted to anyone to USe this software for any purpose,
 *   including commercial applications, and to alter it and redistribute it
 *   freely, subject to the following restrictions:
 *
 *   1. The origin of this software mUSt not be misrepresented; you mUSt not
 *      claim that you wrote the original software. If you USe this software
 *      in a product, an acknowledgment in the product documentation would be
 *      appreciated but is not required.
 *   2. Altered source versions mUSt be plainly marked as such, and mUSt not be
 *      misrepresented as being the original software.
 *   3. This notice may not be removed or altered from any source distribution.
 *
 *
 * ****************************************************************************
 * ANL-OpenCL :: JME3 - App - Model bundles and USes the RandomCL library:
 * https://github.com/bstatcomp/RandomCL
 * ****************************************************************************
 *
 * BSD 3-ClaUSe License
 *
 * Copyright (c) 2018, Tadej Ciglarič, Erik Štrumbelj, Rok Češnovar. All rights reserved.
 *
 * Redistribution and USe in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code mUSt retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form mUSt reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of the copyright holder nor the names of its contributors may be USed to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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

import com.anrisoftware.dwarfhustle.model.api.GameBlockPos;
import com.anrisoftware.dwarfhustle.model.api.GameMapPos;
import com.anrisoftware.dwarfhustle.model.api.GameObjectStorage;
import com.anrisoftware.dwarfhustle.model.api.MapBlock;
import com.anrisoftware.dwarfhustle.model.api.MapTile;
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

	public WorkerBlocks() {
	}

	@Inject
	public void setStorages(Map<String, GameObjectStorage> storages) {
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
			db.declareIntent(new OIntentMassiveInsert());
			generateMapBlock(m, db, createBlocksMap(), pos, endPos);
			db.declareIntent(null);
		}
		this.generateDone = true;
		log.trace("generate done {}", m);
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
