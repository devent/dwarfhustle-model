/*
 * dwarfhustle-model-api - Manages the compile dependencies for the model.
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
package com.anrisoftware.dwarfhustle.model.api.materials

import org.junit.jupiter.api.Test

import com.anrisoftware.dwarfhustle.model.api.objects.KnowledgeObject

/**
 * @see KnowledgeObject
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
class KnowledgeObjectTest {

    @Test
    void "object_rid2Id"() {
        assert KnowledgeObject.kid2Id(0) == 1
        assert KnowledgeObject.kid2Id(500) == 2147483648001
        assert KnowledgeObject.kid2Id(501) == 2151778615297
    }
}
