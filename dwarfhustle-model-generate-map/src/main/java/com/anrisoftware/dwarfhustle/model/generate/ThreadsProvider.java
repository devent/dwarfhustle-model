package com.anrisoftware.dwarfhustle.model.generate;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Provider;

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
