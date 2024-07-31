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
package com.anrisoftware.dwarfhustle.model.api.objects

import static com.anrisoftware.dwarfhustle.model.api.objects.ExternalizableUtils.readExternalObjectLongMap

import org.eclipse.collections.api.factory.primitive.IntLongMaps
import org.eclipse.collections.api.factory.primitive.LongObjectMaps
import org.eclipse.collections.api.factory.primitive.ObjectLongMaps
import org.junit.jupiter.api.Test

/**
 * @see ExternalizableUtils
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
class ExternalizableUtilsTest {

    @Test
    void writeStreamIntLongMap_with_NeighboringDir_ids_then_read() {
        def map = IntLongMaps.mutable.empty()
        NeighboringDir.values().each {
            map.put(it.ordinal(), 7777777777)
        }
        def bout = new ByteArrayOutputStream(512)
        def sout = new DataOutputStream(bout)
        ExternalizableUtils.writeStreamIntLongMap(sout, map)
        sout.close()
        def bin = new ByteArrayInputStream(bout.toByteArray())
        def sin = new DataInputStream(bin)
        def mapread = ExternalizableUtils.readStreamIntLongMap(sin)
        assert mapread.size() == map.size()
    }

    @Test
    void write_readExternalLongObjectMap() {
        def map = LongObjectMaps.mutable.empty()
        map.put(100, "Aaaa")
        map.put(200, "Baaa")
        def bout = new ByteArrayOutputStream(1024)
        def oout = new ObjectOutputStream(bout)
        ExternalizableUtils.writeExternalLongObjectMap(oout, map)
        oout.close()
        def bin = new ByteArrayInputStream(bout.toByteArray())
        def oin = new ObjectInputStream(bin)
        def mapread = ExternalizableUtils.readExternalLongObjectMap(oin)
        assert mapread.size() == map.size()
    }

    @Test
    void map_block_byte_size_objectstream() {
        def stream = new ByteArrayOutputStream(1024)
        def ostream = new ObjectOutputStream(stream)
        def go = MapBlockTest.createTestBlock(0)
        ostream.writeObject(go)
        assert stream.size() == 452
    }

    @Test
    void readExternalObjectLongMap_chunkpos_map_id() {
        def map = ObjectLongMaps.mutable.empty()
        def pos = new GameChunkPos(1, 2, 3, 4, 5, 6)
        map.put(pos, 111111111)
        def buffout = new ByteArrayOutputStream(1024)
        def oout = new ObjectOutputStream(buffout)
        map.writeExternal(oout)
        oout.close()
        def buffin = new ByteArrayInputStream(buffout.toByteArray())
        def oin = new ObjectInputStream(buffin)
        def thatmap = readExternalObjectLongMap(oin)
        assert thatmap == map
    }
}
