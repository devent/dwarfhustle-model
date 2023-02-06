/*
 * dwarfhustle-model-db - Manages the compile dependencies for the model.
 * Copyright © 2023 Erwin Müller (erwin.mueller@anrisoftware.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
public abstract class AbstractCachePutMessage<T extends Message> extends Message {

	/**
	 * Message to put an object in the cache.
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	@ToString(callSuper = true)
	public static class CachePutMessage extends AbstractCachePutMessage<Message> {
		public CachePutMessage(ActorRef<Message> replyTo, Object key, Object value) {
			super(replyTo, key, value);
		}
	}

	/**
	 * Message to put an object in the cache.
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	@ToString(callSuper = true)
	public static class CachePutReplyMessage extends AbstractCachePutMessage<CacheResponseMessage> {
		public CachePutReplyMessage(ActorRef<CacheResponseMessage> replyTo, Object key, Object value) {
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
