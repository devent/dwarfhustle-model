package com.anrisoftware.dwarfhustle.model.db.cache;

import org.apache.commons.jcs3.access.CacheAccess;

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;

import akka.actor.typed.ActorRef;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Message to return the cache.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@RequiredArgsConstructor
@ToString(callSuper = true)
public class RetrieveCacheMessage<K, V> extends Message {

	/**
	 * Cache success response.
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	@RequiredArgsConstructor
	@ToString(callSuper = true)
	public static class RetrieveCacheResponseMessage<K, V> extends Message {

		@ToString.Exclude
		public final Message m;

		public final CacheAccess<K, V> cache;
	}

	/**
	 * Reply to {@link ActorRef}.
	 */
	public final ActorRef<RetrieveCacheResponseMessage<K, V>> replyTo;
}
