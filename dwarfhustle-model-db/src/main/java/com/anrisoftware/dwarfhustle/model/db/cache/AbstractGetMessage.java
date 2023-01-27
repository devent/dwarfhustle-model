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
import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;
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
