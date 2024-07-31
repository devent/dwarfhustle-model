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

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

import com.anrisoftware.dwarfhustle.model.db.store.MapChunksStoreIndex.Index

/**
 * @see MapChunksStoreIndex
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
class MapChunksStoreIndexTest {

    @ParameterizedTest
    @CsvSource([
        "1,230",
        "2,254",
        "3,278",
        "9,422",
        "73,1958",
        "585,14246",
    ])
    void index_size_object_stream(int chunksCount, int expectedSize) {
        def stream = new ByteArrayOutputStream()
        def ostream = new ObjectOutputStream(stream)
        def index = new MapChunksStoreIndex(chunksCount)
        assert MapChunksStoreIndex.getSizeObjectStream(chunksCount) == expectedSize
        (0..<chunksCount).each {
            index.map.put(it, new Index(512 * it, 512))
        }
        ostream.writeObject(index)
        assert index.map.size() == chunksCount + 1
        assert stream.size() == expectedSize
    }

    @ParameterizedTest
    @CsvSource([
        "1,36",
        "2,52",
        "3,68",
        "9,164",
        "73,1188",
        "585,9380",
    ])
    void index_size_data_stream(int chunksCount, int expectedSize) {
        def stream = new ByteArrayOutputStream()
        def ostream = new DataOutputStream(stream)
        def index = new MapChunksStoreIndex(chunksCount)
        assert MapChunksStoreIndex.getSizeDataStream(chunksCount) == expectedSize
        (0..<chunksCount).each {
            index.map.put(it, new Index(512 * it, 512))
        }
        index.writeStream(ostream);
        assert index.map.size() == chunksCount + 1
        assert stream.size() == expectedSize
    }
}
