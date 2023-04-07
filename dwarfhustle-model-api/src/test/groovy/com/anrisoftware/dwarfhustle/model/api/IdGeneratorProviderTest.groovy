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
package com.anrisoftware.dwarfhustle.model.api

import org.junit.jupiter.api.Test

import com.anrisoftware.dwarfhustle.model.api.objects.GameObject
import com.anrisoftware.dwarfhustle.model.api.objects.IdsObjectsProvider

/**
 * @see IdGeneratorProvider
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
class IdGeneratorProviderTest {

	@Test
	void generate_ids() {
		def gen = new IdsObjectsProvider().get()
		def unique = new HashSet()
		for (int i = 0; i < 100000; i++) {
			def buf = gen.generate()
			def id = GameObject.toId(buf)
			if (!unique.add(id)) {
				assert false
			}
		}
	}
}
