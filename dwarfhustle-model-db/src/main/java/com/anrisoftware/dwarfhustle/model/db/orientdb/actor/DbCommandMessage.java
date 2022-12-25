package com.anrisoftware.dwarfhustle.model.db.orientdb.actor;

import java.util.function.Function;

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

	/**
	 * Database command success response with return value.
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	@Data
	@EqualsAndHashCode(callSuper = false)
	public static class DbCommandSuccessMessage extends DbSuccessMessage {

		public final Object value;

		public DbCommandSuccessMessage(AbstractDbReplyMessage originalMessage, Object value) {
			super(originalMessage);
			this.value = value;
		}
	}

	public final Function<ODatabaseDocument, Object> command;

	public DbCommandMessage(ActorRef<DbResponseMessage> replyTo, Function<ODatabaseDocument, Object> command) {
		super(replyTo);
		this.command = command;
	}

}
