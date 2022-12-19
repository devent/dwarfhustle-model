package com.anrisoftware.dwarfhustle.model.db.cache;

import com.anrisoftware.dwarfhustle.model.api.GameObject;

import akka.actor.typed.ActorRef;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Message to get a {@link GameObject} from the cache.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class GetMessage extends AbstractCacheReplyMessage {

	/**
	 * Get success response.
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	@Data
	@EqualsAndHashCode(callSuper = false)
	public static class GetSuccessMessage extends CacheSuccessMessage {

		public final GameObject go;

		public GetSuccessMessage(AbstractCacheReplyMessage originalMessage, GameObject go) {
			super(originalMessage);
			this.go = go;
		}

	}

	public final Object key;

	public GetMessage(ActorRef<CacheResponseMessage> replyTo, Object key) {
		super(replyTo);
		this.key = key;
	}

}
