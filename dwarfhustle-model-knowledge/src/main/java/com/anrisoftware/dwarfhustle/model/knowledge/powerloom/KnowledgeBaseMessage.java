/*
 * dwarfhustle-model-knowledge - Manages the compile dependencies for the model.
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
package com.anrisoftware.dwarfhustle.model.knowledge.powerloom;

import java.util.Map;

import org.eclipse.collections.api.map.primitive.IntObjectMap;

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.api.materials.Material;

import akka.actor.typed.ActorRef;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Message to the knowledge base.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@RequiredArgsConstructor
@ToString(callSuper = true)
public class KnowledgeBaseMessage extends Message {

	@ToString(callSuper = true)
	public static class GetMessage extends KnowledgeBaseMessage {

		public final ActorRef<Message> caller;

		public final String[] material;

		public GetMessage(ActorRef<Message> caller, String... material) {
			super();
			this.caller = caller;
			this.material = material;
		}
	}

	@ToString(callSuper = true)
	public static class GetReplyMessage extends KnowledgeBaseMessage {

		public final ActorRef<ResponseMessage> replyTo;

		public final String[] material;

		public GetReplyMessage(ActorRef<ResponseMessage> replyTo, String... material) {
			super();
			this.replyTo = replyTo;
			this.material = material;
		}
	}

	@RequiredArgsConstructor
	@ToString(callSuper = true)
	public static class ResponseMessage extends Message {

	}

	@RequiredArgsConstructor
	@ToString(callSuper = true)
	public static class ReplyMessage extends ResponseMessage {

		@ToString.Exclude
		public final Map<String, IntObjectMap<? extends Material>> materials;
	}

	@RequiredArgsConstructor
	@ToString(callSuper = true)
	public static class ErrorMessage extends ResponseMessage {

		@ToString.Exclude
		public final KnowledgeBaseMessage originalMessage;

		public final Exception error;
	}

}
