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
package com.anrisoftware.dwarfhustle.model.generate;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import com.anrisoftware.globalpom.threads.external.core.Threads;
import com.anrisoftware.globalpom.threads.external.core.ThreadsException;
import com.anrisoftware.globalpom.threads.properties.external.PropertiesThreads;
import com.anrisoftware.globalpom.threads.properties.external.PropertiesThreadsFactory;
import com.anrisoftware.propertiesutils.ContextPropertiesFactory;

/**
 * Provides the threads pool to generate the game map.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public class ThreadsProvider implements Provider<Threads> {

	private PropertiesThreads threads;

	@Inject
	public ThreadsProvider(PropertiesThreadsFactory threadsFactory) throws IOException, ThreadsException {
		this.threads = threadsFactory.create();
		threads.setProperties(new ContextPropertiesFactory(PropertiesThreads.class)
				.fromResource(ThreadsProvider.class.getResource("/generate-threads.properties"), UTF_8));
		threads.setName("generate-threads");
	}

	@Override
	public Threads get() {
		return threads;
	}
}
