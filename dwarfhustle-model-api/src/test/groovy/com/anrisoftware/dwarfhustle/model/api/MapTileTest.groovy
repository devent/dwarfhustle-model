package com.anrisoftware.dwarfhustle.model.api

import org.junit.jupiter.api.Test

/**
 * @see MapTile
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
class MapTileTest {

	@Test
	void map_tile_type() {
		def go = new MapTile()
		assert go.type == "MapTile"
	}
}
