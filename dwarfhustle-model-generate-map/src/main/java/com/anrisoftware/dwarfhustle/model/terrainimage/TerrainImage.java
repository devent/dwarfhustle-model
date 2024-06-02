/*
 * dwarfhustle-model-generate-map - Manages the compile dependencies for the model.
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
package com.anrisoftware.dwarfhustle.model.terrainimage;

import java.io.IOException;

import lombok.RequiredArgsConstructor;

/**
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@RequiredArgsConstructor
public enum TerrainImage {

    terrain_2_2_2_1(2, 2, 2, 2, 1, 1),

    terrain_4_4_4_2(4, 4, 4, 2, 2, 9),

    terrain_8_8_8_4(8, 8, 8, 4, 4, 9),

    terrain_32_32_32_4(32, 32, 32, 8, 4, 585),

    terrain_32_32_32_8(32, 32, 32, 8, 8, 73),

    terrain_128_128_128_16(128, 128, 128, 8, 16, 585),

    terrain_128_128_128_32(128, 128, 128, 8, 32, 73),

    terrain_256_256_128_16(256, 256, 128, 16, 16, 2633),

    terrain_256_256_128_32(256, 256, 128, 16, 32, 329),

    terrain_256_256_128_64(256, 256, 128, 16, 64, 41),

    terrain_512_512_128_16(512, 512, 128, 16, 16, 10825),

    terrain_512_512_128_32(512, 512, 128, 16, 32, 1353),

    terrain_512_512_128_64(512, 512, 128, 16, 64, 169),

    ;

    public final int w;

    public final int h;

    public final int d;

    public final int columns;

    public final int chunkSize;

    public final int chunksCount;

    public TerrainLoadImage getTerrain() {
        return new TerrainLoadImage(d, h, w, columns, chunkSize);
    }

    public int[][][] loadTerrain() throws IOException {
        return getTerrain().load(TerrainImage.class.getResource(getImageName()));
    }

    public String getImageName() {
        return String.format("terrain-%d-%d-%d-%d.png", w, h, d, columns);
    }
}
