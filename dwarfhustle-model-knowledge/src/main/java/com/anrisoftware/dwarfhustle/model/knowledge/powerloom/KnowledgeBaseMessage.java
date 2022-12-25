package com.anrisoftware.dwarfhustle.model.knowledge.powerloom;

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
	public static class SedimentaryMaterialsMessage extends KnowledgeBaseMessage {

		public SedimentaryMaterialsMessage(ActorRef<KnowledgeBaseResponseMessage> replyTo) {
			super(replyTo);
		}

	}

	@RequiredArgsConstructor
	@ToString(callSuper = true)
	public static class SedimentaryMaterialsSuccessMessage extends KnowledgeBaseResponseMessage {

		@ToString.Exclude
		public final IntObjectMap<Material> sedimentary;
	}

	@RequiredArgsConstructor
	@ToString(callSuper = true)
	public static class KnowledgeBaseResponseMessage extends Message {

	}

	@RequiredArgsConstructor
	@ToString(callSuper = true)
	public static class KnowledgeCommandErrorMessage extends KnowledgeBaseResponseMessage {

		public final KnowledgeBaseMessage originalMessage;

		public final Exception error;
	}

	public final ActorRef<KnowledgeBaseResponseMessage> replyTo;

}
