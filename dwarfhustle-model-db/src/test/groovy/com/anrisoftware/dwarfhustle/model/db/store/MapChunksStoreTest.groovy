/*
 * dwarfhustle-model-api - Manages the compile dependencies for the model.
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
package com.anrisoftware.dwarfhustle.model.db.store

import static java.nio.charset.StandardCharsets.UTF_8
import static org.junit.jupiter.params.provider.Arguments.of

import java.nio.file.Path
import java.util.stream.Stream

import org.apache.commons.io.IOUtils
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

import com.anrisoftware.dwarfhustle.model.api.objects.MapChunk
import com.anrisoftware.dwarfhustle.model.db.buffers.MapChunkBuffer

import groovy.util.logging.Slf4j

/**
 * @see MapChunksStore
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
class MapChunksStoreTest {

    @TempDir
    static Path tmp

    static MapChunksStore createStore(Path tmp, def name, int width, int height, int chunkSize, int chunksCount) {
        def fileName = "${name}.map.txt"
        def stream = MapChunksStoreTest.class.getResourceAsStream(fileName)
        def file = tmp.resolve("${name}.map")
        IOUtils.copy(MapChunksStoreTest.class.getResource(fileName), file.toFile())
        return new MapChunksStore(file, width, height, chunkSize, chunksCount)
    }

    static Stream load_map_chunks_from_file() {
        def args = []
        args << of(4, 4, 4, 2, 9)
        args << of(8, 8, 8, 2, 73)
        args << of(8, 8, 8, 4, 9)
        args << of(32, 32, 32, 4, 585)
        args << of(32, 32, 32, 8, 73)
        Stream.of(args as Object[])
    }

    @ParameterizedTest
    @MethodSource
    void load_map_chunks_from_file(int w, int h, int d, int chunkSize, int chunksCount) {
        def out = new StringBuilder()
        def store = createStore(tmp, "terrain_${w}_${h}_${d}_${chunkSize}_${chunksCount}", w, h, chunkSize, chunksCount)
        def chunksList = []
        def blocksList = []
        store.forEachValue { MapChunk chunk ->
            assert chunk.id != -1
            out.append(chunk.toString())
            out.append("\n")
            chunksList << chunk
            MapChunkBuffer.forEachBlocks(chunk, { block ->
                out.append(block.toString())
                out.append("\n")
                blocksList << block
            })
        }
        store.close()
        assert chunksList.size() == chunksCount
        assert blocksList.size() == w * h * d
        log.debug("load_map_chunks_from_file {}", out.toString())
        def expectedUrl = MapChunksStoreTest.class.getResource("terrain_${w}_${h}_${d}_${chunkSize}_${chunksCount}_map_expected.txt")
        if (expectedUrl) {
            assert out.toString() == IOUtils.toString(expectedUrl, UTF_8)
        }
    }
}
