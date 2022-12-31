package com.anrisoftware.dwarfhustle.model.db.orientdb.actor;

import akka.actor.typed.ActorRef;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Message to delete a database.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@RequiredArgsConstructor
@ToString(callSuper = true)
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

	/**
	 * Reply to {@link ActorRef}.
	 */
	public final ActorRef<DbResponseMessage> replyTo;

}
