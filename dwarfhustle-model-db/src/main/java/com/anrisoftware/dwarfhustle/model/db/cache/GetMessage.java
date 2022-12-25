package com.anrisoftware.dwarfhustle.model.db.cache;

import com.anrisoftware.dwarfhustle.model.api.GameObject;

import akka.actor.typed.ActorRef;
import lombok.ToString;

/**
 * Message to get {@link GameObject} game objects from the cache.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@ToString(callSuper = true)
public class GetMessage extends AbstractCacheReplyMessage {

	/**
	 * Get success response.
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	@ToString(callSuper = true)
	public static class GetSuccessMessage extends CacheSuccessMessage {

		public final Object go;

		public GetSuccessMessage(AbstractCacheReplyMessage originalMessage, Object go) {
			super(originalMessage);
			this.go = go;
		}

	}

	public final String type;

	public final Object key;

	public GetMessage(ActorRef<CacheResponseMessage> replyTo, String type, Object key) {
		super(replyTo);
		this.type = type;
		this.key = key;
	}

}
