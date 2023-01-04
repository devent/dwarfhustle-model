package com.anrisoftware.dwarfhustle.model.db.cache;

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;

import akka.actor.typed.ActorRef;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Message to put an object in the cache.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@RequiredArgsConstructor
@ToString(callSuper = true)
public abstract class AbstractPutMessage<T extends Message> extends Message {

	/**
	 * Message to put an object in the cache.
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	@ToString(callSuper = true)
	public static class PutMessage extends AbstractPutMessage<Message> {
		public PutMessage(ActorRef<Message> replyTo, Object key, Object value) {
			super(replyTo, key, value);
		}
	}

	/**
	 * Message to put an object in the cache.
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	@ToString(callSuper = true)
	public static class PutReplyMessage extends AbstractPutMessage<CacheResponseMessage> {
		public PutReplyMessage(ActorRef<CacheResponseMessage> replyTo, Object key, Object value) {
			super(replyTo, key, value);
		}
	}

	/**
	 * Reply to {@link ActorRef}.
	 */
	public final ActorRef<T> replyTo;

	public final Object key;

	public final Object value;

}
