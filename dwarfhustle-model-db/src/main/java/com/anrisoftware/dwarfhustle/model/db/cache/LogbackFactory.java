/*
 * dwarfhustle-model-db - Manages the compile dependencies for the model.
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
package com.anrisoftware.dwarfhustle.model.db.cache;

import org.apache.commons.jcs3.log.Log;
import org.apache.commons.jcs3.log.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.auto.service.AutoService;

/**
 * This is a SPI factory implementation for log4j2
 */
@AutoService(LogFactory.class)
public class LogbackFactory implements LogFactory {

	/**
	 * Return the name of the Log subsystem managed by this factory
	 *
	 * @return the name of the log subsystem
	 */
	@Override
	public String getName() {
		return "logback";
	}

	/**
	 * Shutdown the logging system if the logging system supports it.
	 */
	@Override
	public void shutdown() {
		// NOP
	}

	/**
	 * Returns a Log using the fully qualified name of the Class as the Log name.
	 *
	 * @param clazz The Class whose name should be used as the Log name. If null it
	 *              will default to the calling class.
	 * @return The Log.
	 * @throws UnsupportedOperationException if {@code clazz} is {@code null} and
	 *                                       the calling class cannot be determined.
	 */
	@Override
	public Log getLog(final Class<?> clazz) {
		final Logger logger = LoggerFactory.getLogger(clazz);
		return new LogbackLogAdapter(logger);
	}

	/**
	 * Returns a Log with the specified name.
	 *
	 * @param name The logger name. If null the name of the calling class will be
	 *             used.
	 * @return The Log.
	 * @throws UnsupportedOperationException if {@code name} is {@code null} and the
	 *                                       calling class cannot be determined.
	 */
	@Override
	public Log getLog(final String name) {
		final Logger logger = LoggerFactory.getLogger(name);
		return new LogbackLogAdapter(logger);
	}
}
