package com.anrisoftware.dwarfhustle.model.knowledge.generate;

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;

import akka.actor.typed.ActorRef;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Message to generate game map.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class GenerateMessage extends Message {

    public static class GenerateResponseMessage extends Message {

    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class GenerateErrorMessage extends GenerateResponseMessage {

        public final GenerateMessage originalMessage;

        public final Exception error;
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class GenerateSuccessMessage extends GenerateResponseMessage {

        public final GenerateMessage originalMessage;
    }

    public final ActorRef<GenerateResponseMessage> replyTo;

    public final String database;

    public final String user;

    public final String password;

}
