package com.anrisoftware.dwarfhustle.model.knowledge.db.orientdb;

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;

import akka.actor.typed.ActorRef;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Message to connect to a OrientDb database.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ConnectDbMessage extends Message {

    public static class ConnectDbResponseMessage extends Message {

    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class ConnectDbErrorMessage extends ConnectDbResponseMessage {

        public final ConnectDbMessage originalMessage;

        public final Exception error;
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class ConnectDbSuccessMessage extends ConnectDbResponseMessage {

        public final ConnectDbMessage originalMessage;
    }

    public final ActorRef<ConnectDbResponseMessage> replyTo;

    public final String url;

    public final String user;

    public final String password;

}
