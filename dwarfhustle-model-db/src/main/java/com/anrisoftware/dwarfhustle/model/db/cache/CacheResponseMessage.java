package com.anrisoftware.dwarfhustle.model.db.cache;

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Base class of cache responses.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@RequiredArgsConstructor
@ToString(callSuper = true)
public class CacheResponseMessage extends Message {

	/**
	 * Cache error response.
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	@RequiredArgsConstructor
	@ToString(callSuper = true)
	public static class CacheErrorMessage extends CacheResponseMessage {

		@ToString.Exclude
		public final Message m;

		public final Throwable error;
	}

	/**
	 * Cache success response.
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	@RequiredArgsConstructor
	@ToString(callSuper = true)
	public static class CacheSuccessMessage extends CacheResponseMessage {

		@ToString.Exclude
		public final Message m;
	}

}
