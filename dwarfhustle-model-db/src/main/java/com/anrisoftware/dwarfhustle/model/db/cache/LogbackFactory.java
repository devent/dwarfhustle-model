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
