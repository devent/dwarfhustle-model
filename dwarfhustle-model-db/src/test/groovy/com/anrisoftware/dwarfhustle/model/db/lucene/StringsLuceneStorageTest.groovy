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
package com.anrisoftware.dwarfhustle.model.db.lucene

import java.nio.file.Path

import org.eclipse.collections.api.factory.Lists
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import com.anrisoftware.dwarfhustle.model.api.objects.StringObject
import com.anrisoftware.dwarfhustle.model.db.strings.DwarfhustleModelDbStringsModule
import com.anrisoftware.dwarfhustle.model.db.strings.StringsLuceneStorage.StringsLuceneStorageFactory
import com.google.inject.Guice
import com.google.inject.Injector

import groovy.util.logging.Slf4j

/**
 * @see StringsLuceneStorage
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
class StringsLuceneStorageTest {

    static Injector injector

    @BeforeAll
    static void setup() {
        injector = Guice.createInjector(new DwarfhustleModelDbStringsModule())
    }

    @Test
    void setObject_update_get_test(@TempDir Path dir) {
        def storage = injector.getInstance(StringsLuceneStorageFactory).create(dir)

        def so = new StringObject(10, "carpender-10")
        log.debug("storage.setObject")
        storage.setObject(so)
        log.debug("done storage.setObject")

        so.setS("carpender-tables")
        log.debug("storage.setObject")
        storage.setObject(so)
        log.debug("done storage.setObject")

        log.debug("storage.getObject")
        StringObject soThat = storage.getObject(StringObject.OBJECT_TYPE, 10)
        log.debug("done storage.getObject")
        assert soThat.id == so.id
        assert soThat.s == so.s

        log.debug("storage.removeObject")
        storage.removeObject(StringObject.OBJECT_TYPE, 10)
        log.debug("done storage.removeObject")

        log.debug("storage.getObject")
        soThat = storage.getObject(StringObject.OBJECT_TYPE, 10)
        assert soThat.id == 0
        assert soThat.s == null
        log.debug("done storage.getObject")

        storage.close()
    }

    @Test
    void benchmark_test(@TempDir Path dir) {
        def storage = injector.getInstance(StringsLuceneStorageFactory).create(dir)

        int n = 5000
        def list = Lists.mutable.ofInitialCapacity(n)
        for (int i = 0; i < 5000; i++) {
            def so = new StringObject(i, "carpender-" + i)
            list.add(so)
        }
        log.debug("storage.addObject bulk")
        storage.addObject(list)
        log.debug("done storage.addObject bulk")

        def rnd = new Random();
        log.debug("storage.getObject");
        for (int i = 0; i < 1000; i++) {
            StringObject soThat = storage.getObject(StringObject.OBJECT_TYPE, rnd.nextLong(n))
        }
        log.debug("done storage.getObject");

        log.debug("storage.setObject with update");
        int m = 100
        def timenow = System.currentTimeMillis()
        for (int i = 0; i < m; i++) {
            int id = rnd.nextLong(n)
            def so = new StringObject(id, "carpender-table-" + id)
            storage.setObject(so)
        }
        def time = System.currentTimeMillis()
        log.debug("done storage.setObject with update {} {}", time - timenow, (time - timenow)/m);


        storage.close()
    }
}
