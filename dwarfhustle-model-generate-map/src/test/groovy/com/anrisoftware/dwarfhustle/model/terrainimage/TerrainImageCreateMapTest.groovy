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
package com.anrisoftware.dwarfhustle.model.terrainimage

import static java.lang.String.format
import static org.junit.jupiter.params.provider.Arguments.of

import java.nio.file.Path
import java.util.stream.Stream

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

import com.anrisoftware.dwarfhustle.model.api.objects.GameMap
import com.anrisoftware.dwarfhustle.model.api.objects.MapChunksStore
import com.anrisoftware.dwarfhustle.model.terrainimage.TerrainImageCreateMap.TerrainImageCreateMapFactory
import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.assistedinject.FactoryModuleBuilder

/**
 * @see TerrainImageCreateMap
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
class TerrainImageCreateMapTest {

    static Injector injector

    @BeforeAll
    static void setupActor() {
        this.injector = Guice.createInjector(
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        install(new FactoryModuleBuilder().implement(TerrainImageCreateMap.class, TerrainImageCreateMap.class)
                                .build(TerrainImageCreateMapFactory.class));
                    }
                }
                )
    }

    static Stream test_start_import_terrain() {
        def args = []
        args << of(TerrainImage.terrain_4_4_4_2)
        args << of(TerrainImage.terrain_8_8_8_2)
        args << of(TerrainImage.terrain_8_8_8_4)
        args << of(TerrainImage.terrain_32_32_32_4)
        args << of(TerrainImage.terrain_32_32_32_8)
        args << of(TerrainImage.terrain_128_128_128_16)
        args << of(TerrainImage.terrain_128_128_128_32)
        args << of(TerrainImage.terrain_256_256_128_16)
        args << of(TerrainImage.terrain_256_256_128_32)
        args << of(TerrainImage.terrain_256_256_128_64)
        Stream.of(args as Object[])
    }

    @TempDir
    static File tmp

    @ParameterizedTest
    @MethodSource()
    @Timeout(600)
    void test_start_import_terrain(TerrainImage image) {
        def terrain = image.terrain
        def gm = new GameMap(1)
        gm.chunkSize = image.chunkSize;
        gm.chunksCount = image.chunksCount
        gm.width = terrain.width
        gm.height = terrain.height
        gm.depth = terrain.depth
        def file = format("terrain_%d_%d_%d_%d_%d.map", gm.width, gm.height, gm.depth, gm.chunkSize, gm.chunksCount)
        def store = new MapChunksStore(Path.of(tmp.absolutePath, file), gm.chunkSize, gm.chunksCount);
        def createMap = injector.getInstance(TerrainImageCreateMapFactory).create(store)
        createMap.startImport(TerrainImageCreateMapTest.class.getResource(image.imageName), terrain, gm)
        println "done"
    }
}
