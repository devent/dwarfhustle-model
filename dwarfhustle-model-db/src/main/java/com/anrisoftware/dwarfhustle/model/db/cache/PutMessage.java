package com.anrisoftware.dwarfhustle.model.db.cache;

import akka.actor.typed.ActorRef;
import lombok.ToString;

/**
 * Message to put an object in the cache.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@ToString(callSuper = true)
public class PutMessage extends AbstractCacheReplyMessage {

	public final Object key;

	public final Object value;

	public PutMessage(ActorRef<CacheResponseMessage> replyTo, Object key, Object value) {
		super(replyTo);
		this.key = key;
		this.value = value;
	}
}
