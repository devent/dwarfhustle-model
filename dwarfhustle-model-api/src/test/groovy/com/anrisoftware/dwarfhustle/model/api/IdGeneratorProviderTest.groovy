package com.anrisoftware.dwarfhustle.model.api

import org.junit.jupiter.api.Test

/**
 * @see IdGeneratorProvider
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
class IdGeneratorProviderTest {

	@Test
	void generate_ids() {
		def gen = new IdGeneratorProvider().get()
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
