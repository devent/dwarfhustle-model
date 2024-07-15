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

import static org.junit.jupiter.params.provider.Arguments.of

import java.util.stream.Stream

import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

import com.anrisoftware.dwarfhustle.model.actor.ActorSystemProvider
import com.anrisoftware.dwarfhustle.model.actor.DwarfhustleModelActorsModule
import com.anrisoftware.dwarfhustle.model.api.objects.DwarfhustleModelApiObjectsModule
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.DwarfhustlePowerloomModule
import com.google.inject.Guice
import com.google.inject.Injector

/**
 * @see ImporterMapImage2DbApp
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
class ImporterMapImage2DbAppTest {

    static Injector injector

    @BeforeAll
    static void setupActor() {
        this.injector = Guice.createInjector(
                new DwarfhustleModelActorsModule(),
                new DwarfhustlePowerloomModule(),
                new DwarfhustleModelApiObjectsModule(),
                new DwarfhustleModelTerrainimageModule(),
                )
    }

    @AfterAll
    static void testsFinished() {
        def dest = new File("/home/devent/Projects/dwarf-hustle/docu/terrain-maps/game")
        dest.mkdir()
        FileUtils.copyDirectory(tmp, dest)
        println tmp
    }

    static Stream test_start_import_db() {
        def args = []
        def mapProperties = new Properties()
        mapProperties.setProperty("world_name", "The Central World")
        mapProperties.setProperty("map_name", "Fierybringer Castle")
        args << of(TerrainImage.terrain_4_4_4_2, mapProperties)
        args << of(TerrainImage.terrain_8_8_8_4, mapProperties)
        args << of(TerrainImage.terrain_32_32_32_4, mapProperties)
        args << of(TerrainImage.terrain_32_32_32_8, mapProperties)
        //        args << of(TerrainImage.terrain_512_512_128_16, mapProperties)
        //        args << of(TerrainImage.terrain_512_512_128_32, mapProperties)
        //        args << of(TerrainImage.terrain_512_512_128_64, mapProperties)
        Stream.of(args as Object[])
    }

    @TempDir
    static File tmp

    @ParameterizedTest
    @MethodSource
    @Timeout(600)
    void test_start_import_db(TerrainImage image, Properties mapProperties) {
        def actor = injector.getInstance(ActorSystemProvider)
        def importer = injector.getInstance(ImporterMapImage2DbApp)
        def wm = importer.createWorldMap(mapProperties)
        def gm = importer.createGameMap(mapProperties, image.terrain, image.chunksCount, wm)
        def subtmp = new File(tmp, image.name())
        subtmp.mkdir()
        importer.init(injector, subtmp, wm, gm).get()
        importer.startImport(ImporterMapImage2DbAppTest.class.getResource(image.imageName), image.terrain, subtmp, gm.id)
        println "done"
    }
}
