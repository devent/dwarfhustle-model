package com.anrisoftware.dwarfhustle.model.db.orientdb.actor;

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
public class CreateDbMessage extends AbstractDbReplyMessage {

	@Data
	@EqualsAndHashCode(callSuper = false)
	public static class DbAlreadyExistMessage extends DbResponseMessage {

		public final CreateDbMessage originalMessage;

	}

	public final ODatabaseType type;

	public CreateDbMessage(ActorRef<DbResponseMessage> replyTo, ODatabaseType type) {
		super(replyTo);
		this.type = type;
	}

}
