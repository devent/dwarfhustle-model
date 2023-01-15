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
package com.anrisoftware.dwarfhustle.model.generate;

import static com.google.inject.name.Names.named;

import com.anrisoftware.dwarfhustle.model.generate.GenerateMapActor.GenerateMapActorFactory;
import com.anrisoftware.dwarfhustle.model.generate.WorkerBlocks.WorkerBlocksFactory;
import com.anrisoftware.globalpom.threads.external.core.Threads;
import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

/**
 * @author Erwin Müller
 */
public class GenerateModule extends AbstractModule {

	@Override
	protected void configure() {
		install(new FactoryModuleBuilder().implement(GenerateMapActor.class, GenerateMapActor.class)
				.build(GenerateMapActorFactory.class));
		install(new FactoryModuleBuilder().implement(WorkerBlocks.class, WorkerBlocks.class)
				.build(WorkerBlocksFactory.class));
		bind(Threads.class).annotatedWith(named("generateMapThreads")).toProvider(ThreadsProvider.class)
				.asEagerSingleton();
	}

}
