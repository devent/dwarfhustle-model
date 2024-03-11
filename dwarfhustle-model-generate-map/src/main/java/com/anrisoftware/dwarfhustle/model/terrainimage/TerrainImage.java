package com.anrisoftware.dwarfhustle.model.terrainimage;

import java.io.IOException;

import lombok.RequiredArgsConstructor;

/**
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@RequiredArgsConstructor
public enum TerrainImage {

    terrain_2_2_2(2, 2, 2, 2, 1),

    terrain_4_4_4(4, 4, 4, 2, 2),

    terrain_8_8_8(8, 8, 8, 4, 2),

    terrain_32_32_32(32, 32, 32, 8, 4),

    terrain_128_128_128(128, 128, 128, 8, 16),

    terrain_256_256_128(256, 256, 128, 16, 16),

    ;

    public final int w;

    public final int h;

    public final int d;

    public final int columns;

    public final int chunkSize;

    public TerrainLoadImage getTerrain() {
        return new TerrainLoadImage(d, h, w, columns, chunkSize);
    }

    public long[][][] loadTerrain() throws IOException {
        return getTerrain().load(TerrainImage.class.getResource(getImageName()));
    }

    public String getImageName() {
        return String.format("terrain-%d-%d-%d-%d.png", w, h, d, columns);
    }
}
