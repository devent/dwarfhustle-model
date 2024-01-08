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
        String name = String.format("terrain-%d-%d-%d.png", w, h, d);
        return getTerrain().load(TerrainImage.class.getResource(name));
    }
}
