package com.anrisoftware.dwarfhustle.model.knowledge.powerloom;

import java.util.Map;

import org.eclipse.collections.api.map.primitive.IntObjectMap;

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.api.Material;

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

		public final String[] material;

		public GetMessage(ActorRef<ResponseMessage> replyTo, String... material) {
			super(replyTo);
			this.material = material;
		}

	}

	@RequiredArgsConstructor
	@ToString(callSuper = true)
	public static class ReplyMessage extends ResponseMessage {

		@ToString.Exclude
		public final Map<String, IntObjectMap<? extends Material>> materials;
	}

	@RequiredArgsConstructor
	@ToString(callSuper = true)
	public static class ResponseMessage extends Message {

	}

	@RequiredArgsConstructor
	@ToString(callSuper = true)
	public static class ErrorMessage extends ResponseMessage {

		@ToString.Exclude
		public final KnowledgeBaseMessage originalMessage;

		public final Exception error;
	}

	public final ActorRef<ResponseMessage> replyTo;

}
