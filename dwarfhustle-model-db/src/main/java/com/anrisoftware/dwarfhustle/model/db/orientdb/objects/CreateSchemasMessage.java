package com.anrisoftware.dwarfhustle.model.db.orientdb.objects;

import akka.actor.typed.ActorRef;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Message to create the schemas and indexes for a new database.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class CreateSchemasMessage extends AbstractObjectsReplyMessage {

	public CreateSchemasMessage(ActorRef<ObjectsResponseMessage> replyTo) {
		super(replyTo);
	}

}
