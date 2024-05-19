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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import ar.com.hjg.pngj.ImageLineInt;
import ar.com.hjg.pngj.PngHelperInternal;
import ar.com.hjg.pngj.PngReader;
import lombok.RequiredArgsConstructor;

/**
 * Loads the terrain from a set of images.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@RequiredArgsConstructor
public class TerrainLoadImage {

    public final int depth;

    public final int height;

    public final int width;

    public final int columns;

    public final int chunkSize;

    public CompletionStage<int[][][]> loadAsync(File file) {
        return CompletableFuture.supplyAsync(() -> load(file));
    }

    public int[][][] load(URL file) throws IOException {
        return load(file.openStream());
    }

    public int[][][] load(File file) {
        return load(PngHelperInternal.istreamFromFile(file));
    }

    public int[][][] load(InputStream stream) {
        int[][][] terrain = new int[depth][height][width];
        try (var reader = new PngReader(stream)) {
            int channels = reader.imgInfo.channels;
            if (channels < 3 || reader.imgInfo.bitDepth != 8) {
                throw new RuntimeException("This method is for RGB8/RGBA8 images");
            }
            readImage(reader, channels, terrain);
        }
        return terrain;
    }

    private void readImage(PngReader reader, int channels, int[][][] terrain) {
        int x = 0, y = 0, z = 0, zoff = 0;
        for (int r = 0; r < reader.imgInfo.rows; r++) {
            var line = (ImageLineInt) reader.readRow();
            for (int c = 0, j = 0; c < reader.imgInfo.cols; c++) {
                int[] scanline = line.getScanline();
                int red = scanline[j++];
                int green = scanline[j++];
                int blue = scanline[j++];
                if (channels > 3) {
                    j++;
                }
                int id = convert2Id(red, green, blue);
                terrain[z + zoff][y][x++] = id;
                if (x == width) {
                    x = 0;
                    z++;
                }
            }
            x = 0;
            z = 0;
            y++;
            if (y > 0 && y % height == 0) {
                y = 0;
                zoff += columns;
            }
        }
    }

    public static int convert2Id(int r, int g, int b) {
        return r + (g << 8) + (b << 16);
    }

}
