package com.anrisoftware.dwarfhustle.model.db.cache;

import java.util.function.Supplier;

import org.apache.commons.jcs3.log.Log;
import org.slf4j.Logger;

import ch.qos.logback.classic.Level;

/**
 * This is a wrapper around the <code>org.slf4j.Logger</code> implementing our
 * own <code>Log</code> interface.
 */
public class LogbackLogAdapter implements Log {

	private final Logger logger;

	/**
	 * Construct a Logback Logger wrapper
	 *
	 * @param logger the logback Logger
	 */
	public LogbackLogAdapter(final Logger logger) {
		this.logger = logger;
	}

	private void log(final Level level, final String message, final Supplier<?>... paramSuppliers) {
		if (level == Level.OFF) {
			return;
		}
		if (level == Level.ERROR) {
			if (paramSuppliers == null) {
				logger.error(message);
				return;
			} else {
				switch (paramSuppliers.length) {
				case 1:
					logger.error(message, paramSuppliers[0].get());
					break;
				case 2:
					logger.error(message, paramSuppliers[0].get(), paramSuppliers[1].get());
					break;
				case 3:
					logger.error(message, paramSuppliers[0].get(), paramSuppliers[1].get(), paramSuppliers[2].get());
					break;
				case 4:
					logger.error(message, paramSuppliers[0].get(), paramSuppliers[1].get(), paramSuppliers[2].get(),
							paramSuppliers[3].get());
					break;
				case 5:
					logger.error(message, paramSuppliers[0].get(), paramSuppliers[1].get(), paramSuppliers[2].get(),
							paramSuppliers[3].get(), paramSuppliers[4].get());
					break;
				default:
					logger.error(message, paramSuppliers[0].get(), paramSuppliers[1].get(), paramSuppliers[2].get(),
							paramSuppliers[3].get(), paramSuppliers[4].get(), paramSuppliers[5].get());
					break;
				}
				return;
			}
		}
		if (level == Level.WARN) {
			if (paramSuppliers == null) {
				logger.warn(message);
				return;
			} else {
				switch (paramSuppliers.length) {
				case 1:
					logger.warn(message, paramSuppliers[0].get());
					break;
				case 2:
					logger.warn(message, paramSuppliers[0].get(), paramSuppliers[1].get());
					break;
				case 3:
					logger.warn(message, paramSuppliers[0].get(), paramSuppliers[1].get(), paramSuppliers[2].get());
					break;
				case 4:
					logger.warn(message, paramSuppliers[0].get(), paramSuppliers[1].get(), paramSuppliers[2].get(),
							paramSuppliers[3].get());
					break;
				case 5:
					logger.warn(message, paramSuppliers[0].get(), paramSuppliers[1].get(), paramSuppliers[2].get(),
							paramSuppliers[3].get(), paramSuppliers[4].get());
					break;
				default:
					logger.warn(message, paramSuppliers[0].get(), paramSuppliers[1].get(), paramSuppliers[2].get(),
							paramSuppliers[3].get(), paramSuppliers[4].get(), paramSuppliers[5].get());
					break;
				}
				return;
			}
		}
		if (level == Level.INFO) {
			if (paramSuppliers == null) {
				logger.info(message);
				return;
			} else {
				switch (paramSuppliers.length) {
				case 1:
					logger.info(message, paramSuppliers[0].get());
					break;
				case 2:
					logger.info(message, paramSuppliers[0].get(), paramSuppliers[1].get());
					break;
				case 3:
					logger.info(message, paramSuppliers[0].get(), paramSuppliers[1].get(), paramSuppliers[2].get());
					break;
				case 4:
					logger.info(message, paramSuppliers[0].get(), paramSuppliers[1].get(), paramSuppliers[2].get(),
							paramSuppliers[3].get());
					break;
				case 5:
					logger.info(message, paramSuppliers[0].get(), paramSuppliers[1].get(), paramSuppliers[2].get(),
							paramSuppliers[3].get(), paramSuppliers[4].get());
					break;
				default:
					logger.info(message, paramSuppliers[0].get(), paramSuppliers[1].get(), paramSuppliers[2].get(),
							paramSuppliers[3].get(), paramSuppliers[4].get(), paramSuppliers[5].get());
					break;
				}
				return;
			}
		}
		if (level == Level.TRACE) {
			if (paramSuppliers == null) {
				logger.trace(message);
				return;
			} else {
				switch (paramSuppliers.length) {
				case 1:
					logger.trace(message, paramSuppliers[0].get());
					break;
				case 2:
					logger.trace(message, paramSuppliers[0].get(), paramSuppliers[1].get());
					break;
				case 3:
					logger.trace(message, paramSuppliers[0].get(), paramSuppliers[1].get(), paramSuppliers[2].get());
					break;
				case 4:
					logger.trace(message, paramSuppliers[0].get(), paramSuppliers[1].get(), paramSuppliers[2].get(),
							paramSuppliers[3].get());
					break;
				case 5:
					logger.trace(message, paramSuppliers[0].get(), paramSuppliers[1].get(), paramSuppliers[2].get(),
							paramSuppliers[3].get(), paramSuppliers[4].get());
					break;
				default:
					logger.trace(message, paramSuppliers[0].get(), paramSuppliers[1].get(), paramSuppliers[2].get(),
							paramSuppliers[3].get(), paramSuppliers[4].get(), paramSuppliers[5].get());
					break;
				}
				return;
			}
		}
	}

	/**
	 * Logs a message object with the DEBUG level.
	 *
	 * @param message the message string to log.
	 */
	@Override
	public void debug(final String message) {
		logger.debug(message);
	}

	/**
	 * Logs a message object with the DEBUG level.
	 *
	 * @param message the message object to log.
	 */
	@Override
	public void debug(final Object message) {
		logger.debug(message.toString());
	}

	/**
	 * Logs a message with parameters at the DEBUG level.
	 *
	 * @param message the message to log; the format depends on the message factory.
	 * @param params  parameters to the message.
	 */
	@Override
	public void debug(final String message, final Object... params) {
		logger.debug(message, params);
	}

	/**
	 * Logs a message with parameters which are only to be constructed if the
	 * logging level is the DEBUG level.
	 *
	 * @param message        the message to log; the format depends on the message
	 *                       factory.
	 * @param paramSuppliers An array of functions, which when called, produce the
	 *                       desired log message parameters.
	 */
	@Override
	public void debug(final String message, final Supplier<?>... paramSuppliers) {
		log(Level.DEBUG, message, paramSuppliers);
	}

	/**
	 * Logs a message at the DEBUG level including the stack trace of the
	 * {@link Throwable} <code>t</code> passed as parameter.
	 *
	 * @param message the message to log.
	 * @param t       the exception to log, including its stack trace.
	 */
	@Override
	public void debug(final String message, final Throwable t) {
		logger.debug(message, t);
	}

	/**
	 * Logs a message object with the ERROR level.
	 *
	 * @param message the message string to log.
	 */
	@Override
	public void error(final String message) {
		logger.error(message);
	}

	/**
	 * Logs a message object with the ERROR level.
	 *
	 * @param message the message object to log.
	 */
	@Override
	public void error(final Object message) {
		logger.error(message.toString());
	}

	/**
	 * Logs a message with parameters at the ERROR level.
	 *
	 * @param message the message to log; the format depends on the message factory.
	 * @param params  parameters to the message.
	 */
	@Override
	public void error(final String message, final Object... params) {
		logger.error(message, params);
	}

	/**
	 * Logs a message with parameters which are only to be constructed if the
	 * logging level is the ERROR level.
	 *
	 * @param message        the message to log; the format depends on the message
	 *                       factory.
	 * @param paramSuppliers An array of functions, which when called, produce the
	 *                       desired log message parameters.
	 */
	@Override
	public void error(final String message, final Supplier<?>... paramSuppliers) {
		log(Level.ERROR, message, paramSuppliers);
	}

	/**
	 * Logs a message at the ERROR level including the stack trace of the
	 * {@link Throwable} <code>t</code> passed as parameter.
	 *
	 * @param message the message object to log.
	 * @param t       the exception to log, including its stack trace.
	 */
	@Override
	public void error(final String message, final Throwable t) {
		logger.error(message, t);
	}

	/**
	 * Logs a message object with the FATAL level.
	 *
	 * @param message the message string to log.
	 */
	@Override
	public void fatal(final String message) {
		logger.error(message);
	}

	/**
	 * Logs a message object with the FATAL level.
	 *
	 * @param message the message object to log.
	 */
	@Override
	public void fatal(final Object message) {
		logger.error(message.toString());
	}

	/**
	 * Logs a message with parameters at the FATAL level.
	 *
	 * @param message the message to log; the format depends on the message factory.
	 * @param params  parameters to the message.
	 */
	@Override
	public void fatal(final String message, final Object... params) {
		logger.error(message, params);
	}

	/**
	 * Logs a message with parameters which are only to be constructed if the
	 * logging level is the FATAL level.
	 *
	 * @param message        the message to log; the format depends on the message
	 *                       factory.
	 * @param paramSuppliers An array of functions, which when called, produce the
	 *                       desired log message parameters.
	 */
	@Override
	public void fatal(final String message, final Supplier<?>... paramSuppliers) {
		log(Level.ERROR, message, paramSuppliers);
	}

	/**
	 * Logs a message at the FATAL level including the stack trace of the
	 * {@link Throwable} <code>t</code> passed as parameter.
	 *
	 * @param message the message object to log.
	 * @param t       the exception to log, including its stack trace.
	 */
	@Override
	public void fatal(final String message, final Throwable t) {
		logger.error(message, t);
	}

	/**
	 * Gets the logger name.
	 *
	 * @return the logger name.
	 */
	@Override
	public String getName() {
		return logger.getName();
	}

	/**
	 * Logs a message object with the INFO level.
	 *
	 * @param message the message string to log.
	 */
	@Override
	public void info(final String message) {
		logger.info(message);
	}

	/**
	 * Logs a message object with the INFO level.
	 *
	 * @param message the message object to log.
	 */
	@Override
	public void info(final Object message) {
		logger.info(message.toString());
	}

	/**
	 * Logs a message with parameters at the INFO level.
	 *
	 * @param message the message to log; the format depends on the message factory.
	 * @param params  parameters to the message.
	 */
	@Override
	public void info(final String message, final Object... params) {
		logger.info(message, params);
	}

	/**
	 * Logs a message with parameters which are only to be constructed if the
	 * logging level is the INFO level.
	 *
	 * @param message        the message to log; the format depends on the message
	 *                       factory.
	 * @param paramSuppliers An array of functions, which when called, produce the
	 *                       desired log message parameters.
	 */
	@Override
	public void info(final String message, final Supplier<?>... paramSuppliers) {
		log(Level.INFO, message, paramSuppliers);
	}

	/**
	 * Logs a message at the INFO level including the stack trace of the
	 * {@link Throwable} <code>t</code> passed as parameter.
	 *
	 * @param message the message object to log.
	 * @param t       the exception to log, including its stack trace.
	 */
	@Override
	public void info(final String message, final Throwable t) {
		logger.info(message, t);
	}

	/**
	 * Checks whether this Logger is enabled for the DEBUG Level.
	 *
	 * @return boolean - {@code true} if this Logger is enabled for level DEBUG,
	 *         {@code false} otherwise.
	 */
	@Override
	public boolean isDebugEnabled() {
		return logger.isDebugEnabled();
	}

	/**
	 * Checks whether this Logger is enabled for the ERROR Level.
	 *
	 * @return boolean - {@code true} if this Logger is enabled for level ERROR,
	 *         {@code false} otherwise.
	 */
	@Override
	public boolean isErrorEnabled() {
		return logger.isErrorEnabled();
	}

	/**
	 * Checks whether this Logger is enabled for the FATAL Level.
	 *
	 * @return boolean - {@code true} if this Logger is enabled for level FATAL,
	 *         {@code false} otherwise.
	 */
	@Override
	public boolean isFatalEnabled() {
		return logger.isErrorEnabled();
	}

	/**
	 * Checks whether this Logger is enabled for the INFO Level.
	 *
	 * @return boolean - {@code true} if this Logger is enabled for level INFO,
	 *         {@code false} otherwise.
	 */
	@Override
	public boolean isInfoEnabled() {
		return logger.isInfoEnabled();
	}

	/**
	 * Checks whether this Logger is enabled for the TRACE level.
	 *
	 * @return boolean - {@code true} if this Logger is enabled for level TRACE,
	 *         {@code false} otherwise.
	 */
	@Override
	public boolean isTraceEnabled() {
		return logger.isTraceEnabled();
	}

	/**
	 * Checks whether this Logger is enabled for the WARN Level.
	 *
	 * @return boolean - {@code true} if this Logger is enabled for level WARN,
	 *         {@code false} otherwise.
	 */
	@Override
	public boolean isWarnEnabled() {
		return logger.isWarnEnabled();
	}

	/**
	 * Logs a message object with the TRACE level.
	 *
	 * @param message the message string to log.
	 */
	@Override
	public void trace(final String message) {
		logger.trace(message);
	}

	/**
	 * Logs a message object with the TRACE level.
	 *
	 * @param message the message object to log.
	 */
	@Override
	public void trace(final Object message) {
		logger.trace(message.toString());
	}

	/**
	 * Logs a message with parameters at the TRACE level.
	 *
	 * @param message the message to log; the format depends on the message factory.
	 * @param params  parameters to the message.
	 */
	@Override
	public void trace(final String message, final Object... params) {
		logger.trace(message, params);
	}

	/**
	 * Logs a message with parameters which are only to be constructed if the
	 * logging level is the TRACE level.
	 *
	 * @param message        the message to log; the format depends on the message
	 *                       factory.
	 * @param paramSuppliers An array of functions, which when called, produce the
	 *                       desired log message parameters.
	 */
	@Override
	public void trace(final String message, final Supplier<?>... paramSuppliers) {
		log(Level.TRACE, message, paramSuppliers);
	}

	/**
	 * Logs a message at the TRACE level including the stack trace of the
	 * {@link Throwable} <code>t</code> passed as parameter.
	 *
	 * @param message the message object to log.
	 * @param t       the exception to log, including its stack trace.
	 * @see #debug(String)
	 */
	@Override
	public void trace(final String message, final Throwable t) {
		logger.trace(message, t);
	}

	/**
	 * Logs a message object with the WARN level.
	 *
	 * @param message the message string to log.
	 */
	@Override
	public void warn(final String message) {
		logger.warn(message);
	}

	/**
	 * Logs a message object with the WARN level.
	 *
	 * @param message the message object to log.
	 */
	@Override
	public void warn(final Object message) {
		logger.warn(message.toString());
	}

	/**
	 * Logs a message with parameters at the WARN level.
	 *
	 * @param message the message to log; the format depends on the message factory.
	 * @param params  parameters to the message.
	 */
	@Override
	public void warn(final String message, final Object... params) {
		logger.warn(message, params);
	}

	/**
	 * Logs a message with parameters which are only to be constructed if the
	 * logging level is the WARN level.
	 *
	 * @param message        the message to log; the format depends on the message
	 *                       factory.
	 * @param paramSuppliers An array of functions, which when called, produce the
	 *                       desired log message parameters.
	 */
	@Override
	public void warn(final String message, final Supplier<?>... paramSuppliers) {
		log(Level.WARN, message, paramSuppliers);
	}

	/**
	 * Logs a message at the WARN level including the stack trace of the
	 * {@link Throwable} <code>t</code> passed as parameter.
	 *
	 * @param message the message object to log.
	 * @param t       the exception to log, including its stack trace.
	 */
	@Override
	public void warn(final String message, final Throwable t) {
		logger.warn(message, t);
	}
}
