package com.anrisoftware.dwarfhustle.model.db.orientdb.actor;

import java.util.function.Consumer;

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
public class DbCommandMessage extends AbstractDbReplyMessage {

	public final Consumer<ODatabaseDocument> command;

	public DbCommandMessage(ActorRef<DbResponseMessage> replyTo, Consumer<ODatabaseDocument> command) {
		super(replyTo);
		this.command = command;
	}

}
