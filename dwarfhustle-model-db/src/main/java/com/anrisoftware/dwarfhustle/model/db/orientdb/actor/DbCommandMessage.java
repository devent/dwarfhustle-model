package com.anrisoftware.dwarfhustle.model.db.orientdb.actor;

import java.util.function.Function;

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;

import akka.actor.typed.ActorRef;
import lombok.ToString;

/**
 * Message to execute a command on the database.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@ToString(callSuper = true)
public class DbCommandMessage extends AbstractDbMessage {

	public final Function<ODatabaseDocument, Object> command;

	public DbCommandMessage(ActorRef<Message> replyTo, Function<ODatabaseDocument, Object> command) {
		super(replyTo);
		this.command = command;
	}

}
