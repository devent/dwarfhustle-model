package com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl;

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.KnowledgeResponseMessage.KnowledgeErrorMessage;
import com.anrisoftware.dwarfhustle.model.knowledge.powerloom.pl.KnowledgeResponseMessage.KnowledgeReplyMessage;

import akka.actor.typed.ActorRef;
import lombok.ToString;

/**
 * Message to retrieve knowledge. Replies with a {@link KnowledgeReplyMessage}
 * on success or with {@link KnowledgeErrorMessage} on error.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@ToString(callSuper = true)
public class KnowledgeGetMessage<T extends Message> extends KnowledgeMessage<T> {

    public final String type;

    public KnowledgeGetMessage(ActorRef<T> replyTo, String type) {
        super(replyTo);
        this.type = type;
    }
}
