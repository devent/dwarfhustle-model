package com.anrisoftware.dwarfhustle.model.db.orientdb.actor;

import akka.actor.typed.ActorRef;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Message to delete a database.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DeleteDbMessage extends AbstractDbReplyMessage {

	/**
	 * Response that the database to delete does not exist.
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	@Data
	@EqualsAndHashCode(callSuper = false)
	public static class DbNotExistMessage extends DbResponseMessage {

		public final DeleteDbMessage originalMessage;

	}

	public DeleteDbMessage(ActorRef<DbResponseMessage> replyTo) {
		super(replyTo);
	}

}
