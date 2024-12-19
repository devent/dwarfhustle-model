/*
 * dwarfhustle-model-generate-map - Manages the compile dependencies for the model.
 * Copyright © 2022-2024 Erwin Müller (erwin.mueller@anrisoftware.com)
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

import com.anrisoftware.dwarfhustle.model.terrainimage.ImporterMapImage2DbActor.ImporterMapImage2DbActorFactory;
import com.anrisoftware.dwarfhustle.model.terrainimage.ImporterChunksJcsCacheActor.ImporterChunksJcsCacheActorFactory;
import com.anrisoftware.dwarfhustle.model.terrainimage.TerrainImageCreateMap.TerrainImageCreateMapFactory;
import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

/**
 * @author Erwin Müller
 */
public class DwarfhustleModelTerrainimageModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new FactoryModuleBuilder().implement(TerrainImageCreateMap.class, TerrainImageCreateMap.class)
                .build(TerrainImageCreateMapFactory.class));
        install(new FactoryModuleBuilder().implement(ImporterMapImage2DbActor.class, ImporterMapImage2DbActor.class)
                .build(ImporterMapImage2DbActorFactory.class));
        install(new FactoryModuleBuilder()
                .implement(ImporterChunksJcsCacheActor.class, ImporterChunksJcsCacheActor.class)
                .build(ImporterChunksJcsCacheActorFactory.class));
    }

}
