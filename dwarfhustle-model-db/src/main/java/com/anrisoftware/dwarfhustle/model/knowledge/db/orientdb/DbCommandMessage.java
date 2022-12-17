package com.anrisoftware.dwarfhustle.model.knowledge.db.orientdb;

import java.util.function.Consumer;

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;

import akka.actor.typed.ActorRef;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Message to execute a command on the database.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DbCommandMessage extends Message {

    public static class DbCommandResponseMessage extends Message {

    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class DbCommandErrorMessage extends DbCommandResponseMessage {

        public final DbCommandMessage originalMessage;

        public final Exception error;
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class DbCommandSuccessMessage extends DbCommandResponseMessage {

        public final DbCommandMessage originalMessage;
    }

    public final ActorRef<DbCommandResponseMessage> replyTo;

    public final String database;

    public final String user;

    public final String password;

    public final Consumer<ODatabaseDocument> command;

}
