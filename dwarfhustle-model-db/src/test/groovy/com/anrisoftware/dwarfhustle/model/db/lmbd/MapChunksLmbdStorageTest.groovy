/*
 * dwarfhustle-model-db - Manages the compile dependencies for the model.
 * Copyright © 2022-2025 Erwin Müller (erwin.mueller@anrisoftware.com)
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
package com.anrisoftware.dwarfhustle.model.db.lmbd

import static com.anrisoftware.dwarfhustle.model.api.objects.MapChunk.cid2Id

import java.nio.file.Path

import org.eclipse.collections.api.factory.primitive.LongObjectMaps
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import com.anrisoftware.dwarfhustle.model.api.objects.GameBlockPos
import com.anrisoftware.dwarfhustle.model.api.objects.GameChunkPos
import com.anrisoftware.dwarfhustle.model.api.objects.GameMap
import com.anrisoftware.dwarfhustle.model.api.objects.MapBlock
import com.anrisoftware.dwarfhustle.model.api.objects.MapChunk
import com.anrisoftware.dwarfhustle.model.api.objects.PropertiesSet
import com.anrisoftware.dwarfhustle.model.db.buffers.MapBlockBuffer
import com.anrisoftware.dwarfhustle.model.db.lmbd.MapChunksLmbdStorage.MapChunksLmbdStorageFactory
import com.google.inject.Guice
import com.google.inject.Injector

import groovy.util.logging.Slf4j

/**
 * @see MapChunksLmbdStorage
 */
@Slf4j
class MapChunksLmbdStorageTest {

	static Injector injector

	@BeforeAll
	static void setupInjector() {
		injector = Guice.createInjector(new DwarfhustleModelDbLmbdModule())
	}

	static List createChunks() {
		def chunks = []
		def root = new MapChunk(cid2Id(0), 0, 16, 32, 32, new GameChunkPos(0, 0, 0, 32, 32, 32))
		def rootchunks = LongObjectMaps.mutable.empty()
		chunks << root
		def chunk = new MapChunk(cid2Id(1), root.cid, 16, 32, 32, new GameChunkPos(0, 0, 0, 16, 16, 16))
		chunks << chunk
		rootchunks.put(chunk.id, chunk.pos)
		chunk = new MapChunk(cid2Id(2), root.cid, 16, 32, 32, new GameChunkPos(16, 0, 0, 32, 16, 16))
		chunks << chunk
		rootchunks.put(chunk.id, chunk.pos)
		chunk = new MapChunk(cid2Id(3), root.cid, 16, 32, 32, new GameChunkPos(16, 16, 0, 32, 32, 16))
		chunks << chunk
		rootchunks.put(chunk.id, chunk.pos)
		root.chunks = rootchunks
		return chunks
	}

	MapBlock createBlock(List chunks, int parent, int x, int y, int z) {
		def block = new MapBlock()
		block.parent = parent
		block.pos = new GameBlockPos(x, y, z)
		block.material = 200
		block.object = 300
		block.p = new PropertiesSet()
		block.temp = 25
		block.lux = 150
		MapChunk chunk = chunks[block.parent]
		int off = GameChunkPos.calcIndex(chunk.pos.getSizeX(), chunk.pos.getSizeY(), chunk.pos.getSizeZ(),
				chunk.pos.x, chunk.pos.y, chunk.pos.z, x, y, z);
		MapBlockBuffer.write(chunk.blocks, off, block)
		return block
	}

	@Test
	void putChunk_test(@TempDir Path tmp) {
		def gm = new GameMap(1, 32, 32, 32)
		int chunkSize = 16
		def storage = injector.getInstance(MapChunksLmbdStorageFactory).create(tmp, chunkSize)
		def chunks = createChunks()
		createBlock chunks, 1, 0, 0, 0
		createBlock chunks, 1, 0, 0, 1
		chunks.each {
			storage.putChunk(it)
		}
		chunks.each {
			def thatChunk = storage.getChunk(it.cid)
			println thatChunk
			println thatChunk.chunks
			assert thatChunk.cid == it.cid
			assert thatChunk.parent == it.parent
			assert thatChunk.chunkSize == it.chunkSize
			assert thatChunk.pos == it.pos
			if (thatChunk.leaf) {
				def thatBlock = MapBlockBuffer.read(thatChunk.blocks, 0, new GameBlockPos(0, 0, 0))
				println thatBlock
				thatBlock = MapBlockBuffer.read(thatChunk.blocks, 0, new GameBlockPos(0, 0, 1))
				println thatBlock
			}
		}
		storage.close()
		TestUtils.listFiles log, tmp
	}

	@Test
	void forEachValue_test(@TempDir Path tmp) {
		def gm = new GameMap(1, 32, 32, 32)
		int chunkSize = 16
		def storage = injector.getInstance(MapChunksLmbdStorageFactory).create(tmp, chunkSize)
		def chunks = createChunks()
		storage.putChunks(chunks)
		def thatChunks = []
		storage.forEachValue({
			println it
			thatChunks << it
		})
		assert thatChunks.size() == chunks.size()
		storage.close()
	}
}
