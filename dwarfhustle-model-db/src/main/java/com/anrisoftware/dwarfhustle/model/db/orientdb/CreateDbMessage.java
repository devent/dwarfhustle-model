package com.anrisoftware.dwarfhustle.model.db.orientdb;

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.orientechnologies.orient.core.db.ODatabaseType;

import akka.actor.typed.ActorRef;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Message to create a new database.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class CreateDbMessage extends Message {

    public static class CreateDbResponseMessage extends Message {

    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class CreateDbErrorMessage extends CreateDbResponseMessage {

        public final CreateDbMessage originalMessage;

        public final Exception error;
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class DbAlreadyExistMessage extends CreateDbResponseMessage {

        public final CreateDbMessage originalMessage;

    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class CreateDbSuccessMessage extends CreateDbResponseMessage {

        public final CreateDbMessage originalMessage;
    }

    public final ActorRef<CreateDbResponseMessage> replyTo;

    public final String database;

    public final ODatabaseType type;
}
