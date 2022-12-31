package com.anrisoftware.dwarfhustle.model.db.orientdb.actor;

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.orientechnologies.orient.core.db.ODatabaseType;

import akka.actor.typed.ActorRef;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Message to create a new database.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@RequiredArgsConstructor
@ToString(callSuper = true)
public class CreateDbMessage extends Message {

	@Data
	@EqualsAndHashCode(callSuper = false)
	public static class DbAlreadyExistMessage extends DbResponseMessage {

		public final CreateDbMessage originalMessage;

	}

	/**
	 * Reply to {@link ActorRef}.
	 */
	public final ActorRef<DbResponseMessage> replyTo;

	public final ODatabaseType type;
}
