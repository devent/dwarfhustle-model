/*
 * dwarfhustle-model-knowledge - Manages the compile dependencies for the model.
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
package com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.storages.PowerLoomUtils

/**
 * @see PowerLoomUtils
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
class PowerLoomUtilsTest {

    @BeforeAll
    static void setupActor() {
        PowerLoomKnowledgeActor.loadPowerLoom()
    }

    @ParameterizedTest
    @CsvSource([
        "building-salt-works,object-have-model,true,true",
        "building-salt-works,object-have-texture,true,false",
        "building-salt-works,object-is-visible,true,true",
        "building-salt-works,object-can-select,true,true",
        "Wood-Log,object-have-model,true,false",
        "Wood-Log,object-have-texture,true,true",
        "Wood-Log,object-is-visible,true,true",
        "Wood-Log,object-can-select,true,true",
        "Building,object-have-model,false,true",
        "Building,object-have-texture,false,false",
        "Building,object-is-visible,false,true",
        "Building,object-can-select,false,true",
        "block-normal,object-have-model,true,true",
        "block-normal,object-have-texture,true,false",
        "block-normal,object-is-visible,true,true",
        "block-normal,object-can-select,true,true",
    ])
    void retrieveBoolean_test(def name, def function, boolean recursive, boolean expected) {
        def ret = PowerLoomUtils.retrieveBoolean(name, function, recursive)
        assert expected == ret
    }
}
