package com.anrisoftware.dwarfhustle.model.db.cache;

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;

import akka.actor.typed.ActorRef;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Message to interact with the cache.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@RequiredArgsConstructor
@ToString(callSuper = true)
public class AbstractCacheReplyMessage extends Message {

	/**
	 * Base class of cache responses.
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	@RequiredArgsConstructor
	@ToString(callSuper = true)
	public static class CacheResponseMessage extends Message {

	}

	/**
	 * Cache error response.
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	@RequiredArgsConstructor
	@ToString(callSuper = true)
	public static class CacheErrorMessage extends CacheResponseMessage {

		public final AbstractCacheReplyMessage originalMessage;

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

		public final AbstractCacheReplyMessage originalMessage;
	}

	/**
	 * Reply to {@link ActorRef}.
	 */
	public final ActorRef<CacheResponseMessage> replyTo;
}
