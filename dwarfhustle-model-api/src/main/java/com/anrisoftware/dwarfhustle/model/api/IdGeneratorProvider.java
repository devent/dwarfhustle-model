package com.anrisoftware.dwarfhustle.model.api;

import javax.inject.Provider;

import org.lable.oss.uniqueid.IDGenerator;
import org.lable.oss.uniqueid.LocalUniqueIDGeneratorFactory;
import org.lable.oss.uniqueid.bytes.Mode;

/**
 * Provides a Id generator.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public class IdGeneratorProvider implements Provider<IDGenerator>{

	private IDGenerator generator;

	public IdGeneratorProvider() {
		final int generatorID = 0;
		final int clusterID = 0;
		this.generator = LocalUniqueIDGeneratorFactory.generatorFor(generatorID, clusterID, Mode.SPREAD);
	}

	@Override
	public IDGenerator get() {
		return generator;
	}

}
