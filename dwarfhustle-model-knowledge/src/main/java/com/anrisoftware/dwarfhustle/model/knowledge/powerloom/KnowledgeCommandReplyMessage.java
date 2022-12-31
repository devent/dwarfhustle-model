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
public class KnowledgeCommandReplyMessage extends Message {

    public final ActorRef<KnowledgeCommandResponseMessage> replyTo;

	public final Supplier<Object> command;

}
