package com.anrisoftware.dwarfhustle.model.db.cache;

import com.anrisoftware.dwarfhustle.model.api.GameObject;

import akka.actor.typed.ActorRef;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Message to put a {@link GameObject} in the cache.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PutMessage extends AbstractCacheReplyMessage {

	public final Object key;

	public final GameObject go;

	public PutMessage(ActorRef<CacheResponseMessage> replyTo, Object key, GameObject go) {
		super(replyTo);
		this.key = key;
		this.go = go;
	}

}
