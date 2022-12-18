package com.anrisoftware.dwarfhustle.model.db.cache;

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;

import akka.actor.typed.ActorRef;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Message to interact with the cache.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AbstractCacheReplyMessage extends Message {

	/**
	 * Base class of cache responses.
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	public static class CacheResponseMessage extends Message {

	}

	/**
	 * Cache error response.
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	@Data
	@EqualsAndHashCode(callSuper = false)
	public static class CacheErrorMessage extends CacheResponseMessage {

		public final AbstractCacheReplyMessage originalMessage;

		public final Throwable error;
	}

	/**
	 * Cache success response.
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	@Data
	@EqualsAndHashCode(callSuper = false)
	public static class CacheSuccessMessage extends CacheResponseMessage {

		public final AbstractCacheReplyMessage originalMessage;
	}

	/**
	 * Reply to {@link ActorRef}.
	 */
	public final ActorRef<CacheResponseMessage> replyTo;
}
