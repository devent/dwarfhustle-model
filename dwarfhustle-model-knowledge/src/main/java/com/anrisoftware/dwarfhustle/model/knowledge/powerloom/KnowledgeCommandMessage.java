package com.anrisoftware.dwarfhustle.model.knowledge.powerloom;

import java.util.function.Supplier;

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;

import akka.actor.typed.ActorRef;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Message to execute a command on the knowledge base.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@RequiredArgsConstructor
@ToString(callSuper = true)
public class KnowledgeCommandMessage extends Message {

    public static class KnowledgeCommandResponseMessage extends Message {

    }

	@RequiredArgsConstructor
	@ToString(callSuper = true)
    public static class KnowledgeCommandErrorMessage extends KnowledgeCommandResponseMessage {

		public final KnowledgeCommandMessage originalMessage;

        public final Exception error;
    }

	@RequiredArgsConstructor
	@ToString(callSuper = true)
    public static class KnowledgeCommandSuccessMessage extends KnowledgeCommandResponseMessage {

		public final KnowledgeCommandMessage originalMessage;

		public final Object result;
    }

    public final ActorRef<KnowledgeCommandResponseMessage> replyTo;

	public final Supplier<Object> command;

}
