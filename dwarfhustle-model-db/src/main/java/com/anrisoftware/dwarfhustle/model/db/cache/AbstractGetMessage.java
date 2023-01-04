package com.anrisoftware.dwarfhustle.model.db.cache;

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.api.GameObject;
import com.anrisoftware.dwarfhustle.model.db.cache.CacheResponseMessage.CacheSuccessMessage;

import akka.actor.typed.ActorRef;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Message to get {@link GameObject} game objects from the cache.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@RequiredArgsConstructor
@ToString(callSuper = true)
public abstract class AbstractGetMessage<T extends Message> extends Message {

	/**
	 * Message to get {@link GameObject} game objects from the cache.
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	@ToString(callSuper = true)
	public static class GetMessage extends AbstractGetMessage<Message> {
		public GetMessage(ActorRef<Message> replyTo, String type, Object value) {
			super(replyTo, type, value);
		}
	}

	/**
	 * Message to get {@link GameObject} game objects from the cache.
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	@ToString(callSuper = true)
	public static class GetReplyMessage extends AbstractGetMessage<CacheResponseMessage> {
		public GetReplyMessage(ActorRef<CacheResponseMessage> replyTo, String type, Object value) {
			super(replyTo, type, value);
		}
	}

	/**
	 * Message to get {@link GameObject} game objects from the cache.
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	@ToString(callSuper = true)
	public static class GetSuccessMessage extends CacheSuccessMessage {

		public final GameObject go;

		public GetSuccessMessage(Message m, GameObject go) {
			super(m);
			this.go = go;
		}
	}

	/**
	 * Reply to {@link ActorRef}.
	 */
	public final ActorRef<T> replyTo;

	public final String type;

	public final Object key;

}
