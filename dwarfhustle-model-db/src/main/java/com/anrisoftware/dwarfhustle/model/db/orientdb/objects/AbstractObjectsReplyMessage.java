package com.anrisoftware.dwarfhustle.model.db.orientdb.objects;

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;

import akka.actor.typed.ActorRef;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Message to interact with the objects.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AbstractObjectsReplyMessage extends Message {

	/**
	 * Base class of objects responses.
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	public static class ObjectsResponseMessage extends Message {

	}

	/**
	 * Objects error response.
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	@Data
	@EqualsAndHashCode(callSuper = false)
	public static class ObjectsErrorMessage extends ObjectsResponseMessage {

		public final AbstractObjectsReplyMessage originalMessage;

		public final Throwable error;
	}

	/**
	 * Objects success response.
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	@Data
	@EqualsAndHashCode(callSuper = false)
	public static class ObjectsSuccessMessage extends ObjectsResponseMessage {

		public final AbstractObjectsReplyMessage originalMessage;
	}

	/**
	 * Reply to {@link ActorRef}.
	 */
	public final ActorRef<ObjectsResponseMessage> replyTo;
}
