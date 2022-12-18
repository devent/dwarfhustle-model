package com.anrisoftware.dwarfhustle.model.knowledge.powerloom;

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;

import akka.actor.typed.ActorRef;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Message to execute a command on the knowledge base.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class KnowledgeCommandMessage extends Message {

    public static class KnowledgeCommandResponseMessage extends Message {

    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class KnowledgeCommandErrorMessage extends KnowledgeCommandResponseMessage {

        public final KnowledgeCommandMessage originalMessage;

        public final Exception error;
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class KnowledgeCommandSuccessMessage extends KnowledgeCommandResponseMessage {

        public final KnowledgeCommandMessage originalMessage;
    }

    public final ActorRef<KnowledgeCommandResponseMessage> replyTo;

    public final Runnable command;

}
