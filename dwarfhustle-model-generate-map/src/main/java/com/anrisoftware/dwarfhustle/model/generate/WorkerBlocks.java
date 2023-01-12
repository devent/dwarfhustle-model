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

import javax.inject.Inject;

import org.apache.commons.jcs3.access.CacheAccess;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.map.primitive.MutableObjectLongMap;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.Maps;
import org.eclipse.collections.impl.factory.primitive.ObjectLongMaps;
import org.lable.oss.uniqueid.GeneratorException;
import org.lable.oss.uniqueid.IDGenerator;

import com.anrisoftware.dwarfhustle.model.api.GameBlockPos;
import com.anrisoftware.dwarfhustle.model.api.GameMapPos;
import com.anrisoftware.dwarfhustle.model.api.MapBlock;
import com.anrisoftware.dwarfhustle.model.api.MapTile;
import com.anrisoftware.dwarfhustle.model.api.PathDirection;
import com.google.inject.assistedinject.Assisted;

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

		WorkerBlocks create(CacheAccess<GameBlockPos, MapBlock> cache);
	}

	@Inject
	@Assisted
	private CacheAccess<GameBlockPos, MapBlock> cache;

	@Inject
	private IDGenerator generator;

	private int blocksDone;

	private boolean generateDone;

	private MutableList<MutableList<MutableList<MapTile>>> nodes;

	public WorkerBlocks() {
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
		this.nodes = Lists.mutable.ofInitialCapacity(d1);
		for (int z = 0; z < d1; z++) {
			MutableList<MutableList<MapTile>> ylist = Lists.mutable.withInitialCapacity(h1);
			nodes.add(ylist);
			for (int y = 0; y < h1; y++) {
				MutableList<MapTile> xlist = Lists.mutable.withInitialCapacity(w1);
				ylist.add(xlist);
			}
		}
		generateMapBlock(m, createBlocksMap(), pos, endPos);
		generatePaths(m);
		this.generateDone = true;
		log.trace("generate done {}", m);
	}

	private MapBlock generateMapBlock(GenerateMapMessage m, MutableObjectLongMap<GameBlockPos> parent, GameMapPos pos,
			GameMapPos endPos) throws GeneratorException {
		int w1 = endPos.getDiffX(pos);
		int h1 = endPos.getDiffY(pos);
		int d1 = endPos.getDiffZ(pos);
		int w2 = endPos.getDiffX(pos) / 2;
		int h2 = endPos.getDiffY(pos) / 2;
		int d2 = endPos.getDiffZ(pos) / 2;
		if (w2 == m.blockSize / 2) {
			var block = createBlock(m, pos, endPos);
			createMapTiles(block);
			cache.put(block.getPos(), block);
			blocksDone++;
			return block;
		}
		var block = createBlock(m, pos, endPos);
		parent.put(block.getPos(), block.getId());
		var map = createBlocksMap();
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		var b0 = generateMapBlock(m, map, pos(m, x, y, z), pos(m, x + w2, y + h2, z + d2));
		map.put(b0.getPos(), b0.getId());
		var b1 = generateMapBlock(m, map, pos(m, x + w2, y, z), pos(m, x + w1, y + h2, z + d2));
		map.put(b1.getPos(), b1.getId());
		var b2 = generateMapBlock(m, map, pos(m, x + w2, y, z + d2), pos(m, x + w1, y + h2, z + d1));
		map.put(b2.getPos(), b2.getId());
		var b3 = generateMapBlock(m, map, pos(m, x, y, z + d2), pos(m, x + w2, y + h2, z + d1));
		map.put(b3.getPos(), b3.getId());
		var b4 = generateMapBlock(m, map, pos(m, x, y + h2, z), pos(m, x + w2, y + h1, z + d2));
		map.put(b4.getPos(), b4.getId());
		var b5 = generateMapBlock(m, map, pos(m, x + w2, y + h2, z), pos(m, x + w1, y + h1, z + d2));
		map.put(b5.getPos(), b5.getId());
		var b6 = generateMapBlock(m, map, pos(m, x + w2, y + h2, z + d2), pos(m, x + w1, y + h1, z + d1));
		map.put(b6.getPos(), b6.getId());
		var b7 = generateMapBlock(m, map, pos(m, x, y + h2, z + d2), pos(m, x + w2, y + h1, z + d1));
		map.put(b7.getPos(), b7.getId());
		block.setBlocks(map.asUnmodifiable());
		cache.put(block.getPos(), block);
		blocksDone++;
		return block;
	}

	private void createMapTiles(MapBlock block) throws GeneratorException {
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
					nodes.get(zz).get(yy).add(tile);
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

	private MapBlock createBlock(GenerateMapMessage m, GameMapPos pos, GameMapPos endPos) throws GeneratorException {
		var block = new MapBlock(generator.generate());
		block.setPos(new GameBlockPos(pos, endPos));
		return block;
	}

	private MutableMap<GameMapPos, MapTile> createTilesMap(int n) {
		return Maps.mutable.ofInitialCapacity(n);
	}

	@SneakyThrows
	private void generatePaths(GenerateMapMessage m) {
		log.debug("generatePaths {}", m);
		pathsMiddleAllDirections(m);
		log.trace("pathsMiddleAllDirections done {}", m);
		pathsMiddleLeftRight(m);
		log.trace("pathsMiddleLeftRight done {}", m);
		pathsMiddleTopBottom(m);
		log.trace("pathsMiddleTopBottom done {}", m);
		pathsTopLeftRight(m);
		log.trace("pathsTopLeftRight done {}", m);
		pathsBottomLeftRight(m);
		log.trace("pathsBottomLeftRight done {}", m);
		pathsTopTopBottom(m);
		log.trace("pathsTopTopBottom done {}", m);
		pathsBottomTopBottom(m);
		log.trace("pathsBottomTopBottom done {}", m);
		pathsEdges(m);
		log.trace("pathsEdges done {}", m);
	}

	private void pathsEdges(GenerateMapMessage m) {
		var p = createPathsMap(7);
		// bottom south west edge
		int x = 0;
		int y = 0;
		int z = 0;
		var tile = nodes.get(z).get(y).get(x);
		p.put(PathDirection.N, nodes.get(z).get(y + 1).get(x).getId());
		p.put(PathDirection.NE, nodes.get(z).get(y + 1).get(x + 1).getId());
		p.put(PathDirection.E, nodes.get(z).get(y).get(x + 1).getId());
		p.put(PathDirection.U, nodes.get(z + 1).get(y).get(x).getId());
		p.put(PathDirection.UN, nodes.get(z + 1).get(y + 1).get(x).getId());
		p.put(PathDirection.UNE, nodes.get(z + 1).get(y + 1).get(x + 1).getId());
		p.put(PathDirection.UE, nodes.get(z + 1).get(y).get(x + 1).getId());
		// top south west edge
		x = 0;
		y = 0;
		z = m.depth - 1;
		tile = nodes.get(z).get(y).get(x);
		p.put(PathDirection.N, nodes.get(z).get(y + 1).get(x).getId());
		p.put(PathDirection.NE, nodes.get(z).get(y + 1).get(x + 1).getId());
		p.put(PathDirection.E, nodes.get(z).get(y).get(x + 1).getId());
		p.put(PathDirection.D, nodes.get(z - 1).get(y).get(x).getId());
		p.put(PathDirection.DN, nodes.get(z - 1).get(y + 1).get(x).getId());
		p.put(PathDirection.DNE, nodes.get(z - 1).get(y + 1).get(x + 1).getId());
		p.put(PathDirection.DE, nodes.get(z - 1).get(y).get(x + 1).getId());
		// bottom south east edge
		x = m.width - 1;
		y = 0;
		z = 0;
		tile = nodes.get(z).get(y).get(x);
		p.put(PathDirection.N, nodes.get(z).get(y + 1).get(x).getId());
		p.put(PathDirection.NW, nodes.get(z).get(y + 1).get(x - 1).getId());
		p.put(PathDirection.W, nodes.get(z).get(y).get(x - 1).getId());
		p.put(PathDirection.U, nodes.get(z + 1).get(y).get(x).getId());
		p.put(PathDirection.UN, nodes.get(z + 1).get(y + 1).get(x).getId());
		p.put(PathDirection.UNW, nodes.get(z + 1).get(y + 1).get(x - 1).getId());
		p.put(PathDirection.UW, nodes.get(z + 1).get(y).get(x - 1).getId());
		// top south east edge
		x = m.width - 1;
		y = 0;
		z = m.depth - 1;
		tile = nodes.get(z).get(y).get(x);
		p.put(PathDirection.N, nodes.get(z).get(y + 1).get(x).getId());
		p.put(PathDirection.NW, nodes.get(z).get(y + 1).get(x - 1).getId());
		p.put(PathDirection.W, nodes.get(z).get(y).get(x - 1).getId());
		p.put(PathDirection.D, nodes.get(z - 1).get(y).get(x).getId());
		p.put(PathDirection.DN, nodes.get(z - 1).get(y + 1).get(x).getId());
		p.put(PathDirection.DNW, nodes.get(z - 1).get(y + 1).get(x - 1).getId());
		p.put(PathDirection.DW, nodes.get(z - 1).get(y).get(x - 1).getId());
		// bottom north east edge
		x = m.width - 1;
		y = m.height - 1;
		z = 0;
		tile = nodes.get(z).get(y).get(x);
		p.put(PathDirection.S, nodes.get(z).get(y - 1).get(x).getId());
		p.put(PathDirection.SW, nodes.get(z).get(y - 1).get(x - 1).getId());
		p.put(PathDirection.W, nodes.get(z).get(y).get(x - 1).getId());
		p.put(PathDirection.U, nodes.get(z + 1).get(y).get(x).getId());
		p.put(PathDirection.US, nodes.get(z + 1).get(y - 1).get(x).getId());
		p.put(PathDirection.USW, nodes.get(z + 1).get(y - 1).get(x - 1).getId());
		p.put(PathDirection.UW, nodes.get(z + 1).get(y).get(x - 1).getId());
		// top north east edge
		x = m.width - 1;
		y = m.height - 1;
		z = m.depth - 1;
		tile = nodes.get(z).get(y).get(x);
		p.put(PathDirection.S, nodes.get(z).get(y - 1).get(x).getId());
		p.put(PathDirection.SW, nodes.get(z).get(y - 1).get(x - 1).getId());
		p.put(PathDirection.W, nodes.get(z).get(y).get(x - 1).getId());
		p.put(PathDirection.D, nodes.get(z - 1).get(y).get(x).getId());
		p.put(PathDirection.DS, nodes.get(z - 1).get(y - 1).get(x).getId());
		p.put(PathDirection.DSW, nodes.get(z - 1).get(y - 1).get(x - 1).getId());
		p.put(PathDirection.DW, nodes.get(z - 1).get(y).get(x - 1).getId());
		// bottom north west edge
		x = 0;
		y = m.height - 1;
		z = 0;
		tile = nodes.get(z).get(y).get(x);
		p.put(PathDirection.E, nodes.get(z).get(y).get(x + 1).getId());
		p.put(PathDirection.SE, nodes.get(z).get(y - 1).get(x + 1).getId());
		p.put(PathDirection.S, nodes.get(z).get(y - 1).get(x).getId());
		p.put(PathDirection.U, nodes.get(z + 1).get(y).get(x).getId());
		p.put(PathDirection.UE, nodes.get(z + 1).get(y).get(x + 1).getId());
		p.put(PathDirection.USE, nodes.get(z + 1).get(y - 1).get(x + 1).getId());
		p.put(PathDirection.US, nodes.get(z + 1).get(y - 1).get(x).getId());
		// top north west edge
		x = 0;
		y = m.height - 1;
		z = m.depth - 1;
		tile = nodes.get(z).get(y).get(x);
		p.put(PathDirection.E, nodes.get(z).get(y).get(x + 1).getId());
		p.put(PathDirection.SE, nodes.get(z).get(y - 1).get(x + 1).getId());
		p.put(PathDirection.S, nodes.get(z).get(y - 1).get(x).getId());
		p.put(PathDirection.D, nodes.get(z - 1).get(y).get(x).getId());
		p.put(PathDirection.DE, nodes.get(z - 1).get(y).get(x + 1).getId());
		p.put(PathDirection.DSE, nodes.get(z - 1).get(y - 1).get(x + 1).getId());
		p.put(PathDirection.DS, nodes.get(z - 1).get(y - 1).get(x).getId());
		// save
		tile.setPaths(p.asUnmodifiable());
	}

	/**
	 * Paths bottom z=0 on the bottom x=0 and top x=width-1.
	 */
	private void pathsBottomTopBottom(GenerateMapMessage m) {
		int z = 0;
		int x = 0;
		for (int y = 1; y < m.width - 1; y++) {
			var n = nodes.get(z).get(y).get(y);
			var p = createPathsMap(5 + 5 + 1);
			p.put(PathDirection.N, nodes.get(z).get(y + 1).get(x).getId());
			p.put(PathDirection.NE, nodes.get(z).get(y + 1).get(x + 1).getId());
			p.put(PathDirection.E, nodes.get(z).get(y).get(x + 1).getId());
			p.put(PathDirection.SE, nodes.get(z).get(y - 1).get(x + 1).getId());
			p.put(PathDirection.S, nodes.get(z).get(y - 1).get(x).getId());
			// up
			p.put(PathDirection.U, nodes.get(z + 1).get(y).get(x).getId());
			p.put(PathDirection.UN, nodes.get(z + 1).get(y + 1).get(x).getId());
			p.put(PathDirection.UNE, nodes.get(z + 1).get(y + 1).get(x + 1).getId());
			p.put(PathDirection.UE, nodes.get(z + 1).get(y).get(x + 1).getId());
			p.put(PathDirection.USE, nodes.get(z + 1).get(y - 1).get(x + 1).getId());
			p.put(PathDirection.US, nodes.get(z + 1).get(y - 1).get(x).getId());
			// save
			n.setPaths(p.asUnmodifiable());
		}
		x = m.width - 1;
		for (int y = 1; y < m.width - 1; y++) {
			var n = nodes.get(z).get(y).get(y);
			var p = createPathsMap(5 + 5 + 1);
			p.put(PathDirection.N, nodes.get(z).get(y + 1).get(x).getId());
			p.put(PathDirection.S, nodes.get(z).get(y - 1).get(x).getId());
			p.put(PathDirection.SW, nodes.get(z).get(y - 1).get(x - 1).getId());
			p.put(PathDirection.W, nodes.get(z).get(y).get(x - 1).getId());
			p.put(PathDirection.NW, nodes.get(z).get(y + 1).get(x - 1).getId());
			// up
			p.put(PathDirection.U, nodes.get(z + 1).get(y).get(x).getId());
			p.put(PathDirection.UN, nodes.get(z + 1).get(y + 1).get(x).getId());
			p.put(PathDirection.US, nodes.get(z + 1).get(y - 1).get(x).getId());
			p.put(PathDirection.USW, nodes.get(z + 1).get(y - 1).get(x - 1).getId());
			p.put(PathDirection.UW, nodes.get(z + 1).get(y).get(x - 1).getId());
			p.put(PathDirection.UNW, nodes.get(z + 1).get(y + 1).get(x - 1).getId());
			// save
			n.setPaths(p.asUnmodifiable());
		}
	}

	/**
	 * Paths top z=depth-1 on the bottom x=0 and top x=width-1.
	 */
	private void pathsTopTopBottom(GenerateMapMessage m) {
		int z = m.depth - 1;
		int x = 0;
		for (int y = 1; y < m.width - 1; y++) {
			var n = nodes.get(z).get(y).get(y);
			var p = createPathsMap(5 + 5 + 1);
			p.put(PathDirection.N, nodes.get(z).get(y + 1).get(x).getId());
			p.put(PathDirection.NE, nodes.get(z).get(y + 1).get(x + 1).getId());
			p.put(PathDirection.E, nodes.get(z).get(y).get(x + 1).getId());
			p.put(PathDirection.SE, nodes.get(z).get(y - 1).get(x + 1).getId());
			p.put(PathDirection.S, nodes.get(z).get(y - 1).get(x).getId());
			// down
			p.put(PathDirection.D, nodes.get(z - 1).get(y).get(x).getId());
			p.put(PathDirection.DN, nodes.get(z - 1).get(y + 1).get(x).getId());
			p.put(PathDirection.DNE, nodes.get(z - 1).get(y + 1).get(x + 1).getId());
			p.put(PathDirection.DE, nodes.get(z - 1).get(y).get(x + 1).getId());
			p.put(PathDirection.DSE, nodes.get(z - 1).get(y - 1).get(x + 1).getId());
			p.put(PathDirection.DS, nodes.get(z - 1).get(y - 1).get(x).getId());
			// save
			n.setPaths(p.asUnmodifiable());
		}
		x = m.width - 1;
		for (int y = 1; y < m.width - 1; y++) {
			var n = nodes.get(z).get(y).get(y);
			var p = createPathsMap(5 + 5 + 1);
			p.put(PathDirection.N, nodes.get(z).get(y + 1).get(x).getId());
			p.put(PathDirection.S, nodes.get(z).get(y - 1).get(x).getId());
			p.put(PathDirection.SW, nodes.get(z).get(y - 1).get(x - 1).getId());
			p.put(PathDirection.W, nodes.get(z).get(y).get(x - 1).getId());
			p.put(PathDirection.NW, nodes.get(z).get(y + 1).get(x - 1).getId());
			// down
			p.put(PathDirection.D, nodes.get(z - 1).get(y).get(x).getId());
			p.put(PathDirection.DN, nodes.get(z - 1).get(y + 1).get(x).getId());
			p.put(PathDirection.DS, nodes.get(z - 1).get(y - 1).get(x).getId());
			p.put(PathDirection.DSW, nodes.get(z - 1).get(y - 1).get(x - 1).getId());
			p.put(PathDirection.DW, nodes.get(z - 1).get(y).get(x - 1).getId());
			p.put(PathDirection.DNW, nodes.get(z - 1).get(y + 1).get(x - 1).getId());
			// save
			n.setPaths(p.asUnmodifiable());
		}
	}

	/**
	 * Paths bottom z=0 on the left y=0 and right y=height-1.
	 */
	private void pathsBottomLeftRight(GenerateMapMessage m) {
		int z = 0;
		int y = 0;
		for (int x = 1; x < m.width - 1; x++) {
			var n = nodes.get(z).get(y).get(x);
			var p = createPathsMap(5 + 5 + 1);
			p.put(PathDirection.N, nodes.get(z).get(y + 1).get(x).getId());
			p.put(PathDirection.NE, nodes.get(z).get(y + 1).get(x + 1).getId());
			p.put(PathDirection.E, nodes.get(z).get(y).get(x + 1).getId());
			p.put(PathDirection.W, nodes.get(z).get(y).get(x - 1).getId());
			p.put(PathDirection.NW, nodes.get(z).get(y + 1).get(x - 1).getId());
			// up
			p.put(PathDirection.U, nodes.get(z + 1).get(y).get(x).getId());
			p.put(PathDirection.UN, nodes.get(z + 1).get(y + 1).get(x).getId());
			p.put(PathDirection.UNE, nodes.get(z + 1).get(y + 1).get(x + 1).getId());
			p.put(PathDirection.UE, nodes.get(z + 1).get(y).get(x + 1).getId());
			p.put(PathDirection.UW, nodes.get(z + 1).get(y).get(x - 1).getId());
			p.put(PathDirection.UNW, nodes.get(z + 1).get(y + 1).get(x - 1).getId());
			// save
			n.setPaths(p.asUnmodifiable());
		}
		y = m.height - 1;
		for (int x = 1; x < m.width - 1; x++) {
			var n = nodes.get(z).get(y).get(x);
			var p = createPathsMap(5 + 5 + 1);
			p.put(PathDirection.E, nodes.get(z).get(y).get(x + 1).getId());
			p.put(PathDirection.SE, nodes.get(z).get(y - 1).get(x + 1).getId());
			p.put(PathDirection.S, nodes.get(z).get(y - 1).get(x).getId());
			p.put(PathDirection.SW, nodes.get(z).get(y - 1).get(x - 1).getId());
			p.put(PathDirection.W, nodes.get(z).get(y).get(x - 1).getId());
			// up
			p.put(PathDirection.U, nodes.get(z + 1).get(y).get(x).getId());
			p.put(PathDirection.UE, nodes.get(z + 1).get(y).get(x + 1).getId());
			p.put(PathDirection.USE, nodes.get(z + 1).get(y - 1).get(x + 1).getId());
			p.put(PathDirection.US, nodes.get(z + 1).get(y - 1).get(x).getId());
			p.put(PathDirection.USW, nodes.get(z + 1).get(y - 1).get(x - 1).getId());
			p.put(PathDirection.UW, nodes.get(z + 1).get(y).get(x - 1).getId());
			// save
			n.setPaths(p.asUnmodifiable());
		}
	}

	/**
	 * Paths top z=depth-1 on the left y=0 and right y=height-1.
	 */
	private void pathsTopLeftRight(GenerateMapMessage m) {
		int z = m.depth - 1;
		int y = 0;
		for (int x = 1; x < m.width - 1; x++) {
			var n = nodes.get(z).get(y).get(x);
			var p = createPathsMap(5 + 5 + 1);
			p.put(PathDirection.N, nodes.get(z).get(y + 1).get(x).getId());
			p.put(PathDirection.NE, nodes.get(z).get(y + 1).get(x + 1).getId());
			p.put(PathDirection.E, nodes.get(z).get(y).get(x + 1).getId());
			p.put(PathDirection.W, nodes.get(z).get(y).get(x - 1).getId());
			p.put(PathDirection.NW, nodes.get(z).get(y + 1).get(x - 1).getId());
			// down
			p.put(PathDirection.D, nodes.get(z - 1).get(y).get(x).getId());
			p.put(PathDirection.DN, nodes.get(z - 1).get(y + 1).get(x).getId());
			p.put(PathDirection.DNE, nodes.get(z - 1).get(y + 1).get(x + 1).getId());
			p.put(PathDirection.DE, nodes.get(z - 1).get(y).get(x + 1).getId());
			p.put(PathDirection.DW, nodes.get(z - 1).get(y).get(x - 1).getId());
			p.put(PathDirection.DNW, nodes.get(z - 1).get(y + 1).get(x - 1).getId());
			// save
			n.setPaths(p.asUnmodifiable());
		}
		y = m.height - 1;
		for (int x = 1; x < m.width - 1; x++) {
			var n = nodes.get(z).get(y).get(x);
			var p = createPathsMap(5 + 5 + 1);
			p.put(PathDirection.E, nodes.get(z).get(y).get(x + 1).getId());
			p.put(PathDirection.SE, nodes.get(z).get(y - 1).get(x + 1).getId());
			p.put(PathDirection.S, nodes.get(z).get(y - 1).get(x).getId());
			p.put(PathDirection.SW, nodes.get(z).get(y - 1).get(x - 1).getId());
			p.put(PathDirection.W, nodes.get(z).get(y).get(x - 1).getId());
			// down
			p.put(PathDirection.D, nodes.get(z - 1).get(y).get(x).getId());
			p.put(PathDirection.DE, nodes.get(z - 1).get(y).get(x + 1).getId());
			p.put(PathDirection.DSE, nodes.get(z - 1).get(y - 1).get(x + 1).getId());
			p.put(PathDirection.DS, nodes.get(z - 1).get(y - 1).get(x).getId());
			p.put(PathDirection.DSW, nodes.get(z - 1).get(y - 1).get(x - 1).getId());
			p.put(PathDirection.DW, nodes.get(z - 1).get(y).get(x - 1).getId());
			// save
			n.setPaths(p.asUnmodifiable());
		}
	}

	/**
	 * Paths middle 0<z<depth-1 on the top x=0 and bottom x=width-1.
	 */
	private void pathsMiddleTopBottom(GenerateMapMessage m) {
		for (int z = 1; z < m.depth - 1; z++) {
			int x = 0;
			for (int y = 1; y < m.height - 1; y++) {
				var n = nodes.get(z).get(y).get(y);
				var p = createPathsMap(5 + 5 + 5 + 2);
				p.put(PathDirection.N, nodes.get(z).get(y + 1).get(x).getId());
				p.put(PathDirection.NE, nodes.get(z).get(y + 1).get(x + 1).getId());
				p.put(PathDirection.E, nodes.get(z).get(y).get(x + 1).getId());
				p.put(PathDirection.SE, nodes.get(z).get(y - 1).get(x + 1).getId());
				p.put(PathDirection.S, nodes.get(z).get(y - 1).get(x).getId());
				// up
				p.put(PathDirection.U, nodes.get(z + 1).get(y).get(x).getId());
				p.put(PathDirection.UN, nodes.get(z + 1).get(y + 1).get(x).getId());
				p.put(PathDirection.UNE, nodes.get(z + 1).get(y + 1).get(x + 1).getId());
				p.put(PathDirection.UE, nodes.get(z + 1).get(y).get(x + 1).getId());
				p.put(PathDirection.USE, nodes.get(z + 1).get(y - 1).get(x + 1).getId());
				p.put(PathDirection.US, nodes.get(z + 1).get(y - 1).get(x).getId());
				// down
				p.put(PathDirection.D, nodes.get(z - 1).get(y).get(x).getId());
				p.put(PathDirection.DN, nodes.get(z - 1).get(y + 1).get(x).getId());
				p.put(PathDirection.DNE, nodes.get(z - 1).get(y + 1).get(x + 1).getId());
				p.put(PathDirection.DE, nodes.get(z - 1).get(y).get(x + 1).getId());
				p.put(PathDirection.DSE, nodes.get(z - 1).get(y - 1).get(x + 1).getId());
				p.put(PathDirection.DS, nodes.get(z - 1).get(y - 1).get(x).getId());
				// save
				n.setPaths(p.asUnmodifiable());
			}
			x = m.width - 1;
			for (int y = 1; y < m.height - 1; y++) {
				var n = nodes.get(z).get(y).get(y);
				var p = createPathsMap(5 + 5 + 5 + 2);
				p.put(PathDirection.N, nodes.get(z).get(y + 1).get(x).getId());
				p.put(PathDirection.S, nodes.get(z).get(y - 1).get(x).getId());
				p.put(PathDirection.SW, nodes.get(z).get(y - 1).get(x - 1).getId());
				p.put(PathDirection.W, nodes.get(z).get(y).get(x - 1).getId());
				p.put(PathDirection.NW, nodes.get(z).get(y + 1).get(x - 1).getId());
				// up
				p.put(PathDirection.U, nodes.get(z + 1).get(y).get(x).getId());
				p.put(PathDirection.UN, nodes.get(z + 1).get(y + 1).get(x).getId());
				p.put(PathDirection.US, nodes.get(z + 1).get(y - 1).get(x).getId());
				p.put(PathDirection.USW, nodes.get(z + 1).get(y - 1).get(x - 1).getId());
				p.put(PathDirection.UW, nodes.get(z + 1).get(y).get(x - 1).getId());
				p.put(PathDirection.UNW, nodes.get(z + 1).get(y + 1).get(x - 1).getId());
				// down
				p.put(PathDirection.D, nodes.get(z - 1).get(y).get(x).getId());
				p.put(PathDirection.DN, nodes.get(z - 1).get(y + 1).get(x).getId());
				p.put(PathDirection.DS, nodes.get(z - 1).get(y - 1).get(x).getId());
				p.put(PathDirection.DSW, nodes.get(z - 1).get(y - 1).get(x - 1).getId());
				p.put(PathDirection.DW, nodes.get(z - 1).get(y).get(x - 1).getId());
				p.put(PathDirection.DNW, nodes.get(z - 1).get(y + 1).get(x - 1).getId());
				// save
				n.setPaths(p.asUnmodifiable());
			}
		}
	}

	/**
	 * Paths middle 0<z<depth-1 on the left y=0 and right y=height-1.
	 */
	private void pathsMiddleLeftRight(GenerateMapMessage m) {
		for (int z = 1; z < m.depth - 1; z++) {
			int y = 0;
			for (int x = 1; x < m.width - 1; x++) {
				var n = nodes.get(z).get(y).get(x);
				var p = createPathsMap(5 + 5 + 5 + 2);
				p.put(PathDirection.N, nodes.get(z).get(y + 1).get(x).getId());
				p.put(PathDirection.NE, nodes.get(z).get(y + 1).get(x + 1).getId());
				p.put(PathDirection.E, nodes.get(z).get(y).get(x + 1).getId());
				p.put(PathDirection.W, nodes.get(z).get(y).get(x - 1).getId());
				p.put(PathDirection.NW, nodes.get(z).get(y + 1).get(x - 1).getId());
				// up
				p.put(PathDirection.U, nodes.get(z + 1).get(y).get(x).getId());
				p.put(PathDirection.UN, nodes.get(z + 1).get(y + 1).get(x).getId());
				p.put(PathDirection.UNE, nodes.get(z + 1).get(y + 1).get(x + 1).getId());
				p.put(PathDirection.UE, nodes.get(z + 1).get(y).get(x + 1).getId());
				p.put(PathDirection.UW, nodes.get(z + 1).get(y).get(x - 1).getId());
				p.put(PathDirection.UNW, nodes.get(z + 1).get(y + 1).get(x - 1).getId());
				// down
				p.put(PathDirection.D, nodes.get(z - 1).get(y).get(x).getId());
				p.put(PathDirection.DN, nodes.get(z - 1).get(y + 1).get(x).getId());
				p.put(PathDirection.DNE, nodes.get(z - 1).get(y + 1).get(x + 1).getId());
				p.put(PathDirection.DE, nodes.get(z - 1).get(y).get(x + 1).getId());
				p.put(PathDirection.DW, nodes.get(z - 1).get(y).get(x - 1).getId());
				p.put(PathDirection.DNW, nodes.get(z - 1).get(y + 1).get(x - 1).getId());
				// save
				n.setPaths(p.asUnmodifiable());
			}
			y = m.height - 1;
			for (int x = 1; x < m.width - 1; x++) {
				var n = nodes.get(z).get(y).get(x);
				var p = createPathsMap(5 + 5 + 5 + 2);
				p.put(PathDirection.E, nodes.get(z).get(y).get(x + 1).getId());
				p.put(PathDirection.SE, nodes.get(z).get(y - 1).get(x + 1).getId());
				p.put(PathDirection.S, nodes.get(z).get(y - 1).get(x).getId());
				p.put(PathDirection.SW, nodes.get(z).get(y - 1).get(x - 1).getId());
				p.put(PathDirection.W, nodes.get(z).get(y).get(x - 1).getId());
				// up
				p.put(PathDirection.U, nodes.get(z + 1).get(y).get(x).getId());
				p.put(PathDirection.UE, nodes.get(z + 1).get(y).get(x + 1).getId());
				p.put(PathDirection.USE, nodes.get(z + 1).get(y - 1).get(x + 1).getId());
				p.put(PathDirection.US, nodes.get(z + 1).get(y - 1).get(x).getId());
				p.put(PathDirection.USW, nodes.get(z + 1).get(y - 1).get(x - 1).getId());
				p.put(PathDirection.UW, nodes.get(z + 1).get(y).get(x - 1).getId());
				// down
				p.put(PathDirection.D, nodes.get(z - 1).get(y).get(x).getId());
				p.put(PathDirection.DE, nodes.get(z - 1).get(y).get(x + 1).getId());
				p.put(PathDirection.DSE, nodes.get(z - 1).get(y - 1).get(x + 1).getId());
				p.put(PathDirection.DS, nodes.get(z - 1).get(y - 1).get(x).getId());
				p.put(PathDirection.DSW, nodes.get(z - 1).get(y - 1).get(x - 1).getId());
				p.put(PathDirection.DW, nodes.get(z - 1).get(y).get(x - 1).getId());
				// save
				n.setPaths(p.asUnmodifiable());
			}
		}
	}

	/**
	 * Paths for tiles that have all directions available 0<z<depth-1, 0<y<height-1,
	 * 0<x<width-1.
	 * <p>
	 * edges = 26*(depth-2)*(height-2)*(width-2)
	 */
	private void pathsMiddleAllDirections(GenerateMapMessage m) {
		for (int z = 1; z < m.depth - 1; z++) {
			for (int y = 1; y < m.height - 1; y++) {
				for (int x = 1; x < m.width - 1; x++) {
					var n = nodes.get(z).get(y).get(x);
					var p = createPathsMap(8 + 8 + 8 + 2);
					p.put(PathDirection.N, nodes.get(z).get(y + 1).get(x).getId());
					p.put(PathDirection.NE, nodes.get(z).get(y + 1).get(x + 1).getId());
					p.put(PathDirection.E, nodes.get(z).get(y).get(x + 1).getId());
					p.put(PathDirection.SE, nodes.get(z).get(y - 1).get(x + 1).getId());
					p.put(PathDirection.S, nodes.get(z).get(y - 1).get(x).getId());
					p.put(PathDirection.SW, nodes.get(z).get(y - 1).get(x - 1).getId());
					p.put(PathDirection.W, nodes.get(z).get(y).get(x - 1).getId());
					p.put(PathDirection.NW, nodes.get(z).get(y + 1).get(x - 1).getId());
					// up
					p.put(PathDirection.U, nodes.get(z + 1).get(y).get(x).getId());
					p.put(PathDirection.UN, nodes.get(z + 1).get(y + 1).get(x).getId());
					p.put(PathDirection.UNE, nodes.get(z + 1).get(y + 1).get(x + 1).getId());
					p.put(PathDirection.UE, nodes.get(z + 1).get(y).get(x + 1).getId());
					p.put(PathDirection.USE, nodes.get(z + 1).get(y - 1).get(x + 1).getId());
					p.put(PathDirection.US, nodes.get(z + 1).get(y - 1).get(x).getId());
					p.put(PathDirection.USW, nodes.get(z + 1).get(y - 1).get(x - 1).getId());
					p.put(PathDirection.UW, nodes.get(z + 1).get(y).get(x - 1).getId());
					p.put(PathDirection.UNW, nodes.get(z + 1).get(y + 1).get(x - 1).getId());
					// down
					p.put(PathDirection.D, nodes.get(z - 1).get(y).get(x).getId());
					p.put(PathDirection.DN, nodes.get(z - 1).get(y + 1).get(x).getId());
					p.put(PathDirection.DNE, nodes.get(z - 1).get(y + 1).get(x + 1).getId());
					p.put(PathDirection.DE, nodes.get(z - 1).get(y).get(x + 1).getId());
					p.put(PathDirection.DSE, nodes.get(z - 1).get(y - 1).get(x + 1).getId());
					p.put(PathDirection.DS, nodes.get(z - 1).get(y - 1).get(x).getId());
					p.put(PathDirection.DSW, nodes.get(z - 1).get(y - 1).get(x - 1).getId());
					p.put(PathDirection.DW, nodes.get(z - 1).get(y).get(x - 1).getId());
					p.put(PathDirection.DNW, nodes.get(z - 1).get(y + 1).get(x - 1).getId());
					// save
					n.setPaths(p.asUnmodifiable());
				}
			}
		}
	}

	private MutableObjectLongMap<PathDirection> createPathsMap(int n) {
		return ObjectLongMaps.mutable.ofInitialCapacity(n);
	}

}
