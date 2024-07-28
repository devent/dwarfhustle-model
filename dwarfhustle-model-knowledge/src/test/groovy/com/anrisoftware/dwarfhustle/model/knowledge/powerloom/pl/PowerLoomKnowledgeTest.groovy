/*
 * dwarfhustle-model-knowledge - Manages the compile dependencies for the model.
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
package com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

import groovy.util.logging.Slf4j

/**
 * Tests loading the knowledge base.
 * 
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
class PowerLoomKnowledgeTest {

    static final WORK_MODULE = "DWARFHUSTLE-WORKING"

    @BeforeAll
    static void setupPowerLoom() {
        PowerLoomKnowledgeActor.loadPowerLoom()
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "all (growing-climate Wheat ?x)",
        "all (BlockObject ?x)",
    ])
    @Timeout(10l)
    void "retrieve knowledges"(String retrieve) {
        PowerLoomTestUtils.printPowerLoomRetrieve(retrieve, WORK_MODULE)
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "(= (neighbor block-filled dir-n block-filled) block-visible)",
        "(= (neighbor block-filled dir-n block-mined) block-ramp)",
    ])
    @Timeout(10l)
    void "retrieve neighbor"(String retrieve) {
        PowerLoomTestUtils.printPowerLoomAsk(retrieve, WORK_MODULE)
    }
}
