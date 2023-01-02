package com.anrisoftware.dwarfhustle.model.db.orientdb.actor;

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.orientechnologies.orient.core.db.OrientDB;

import akka.actor.typed.ActorRef;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Message to connect to a OrientDb database.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@RequiredArgsConstructor
@ToString(callSuper = true)
public class ConnectDbMessage extends Message {

	/**
	 * Reply to {@link ActorRef}.
	 */
	public final ActorRef<DbResponseMessage> replyTo;

	public final String url;

	public final String database;

	public final String user;

	public final String password;

	/**
	 * Database success response.
	 *
	 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
	 */
	@RequiredArgsConstructor
	@ToString(callSuper = true)
	public static class ConnectDbSuccessMessage extends DbResponseMessage {

		@ToString.Exclude
		public final Message om;

		public final OrientDB db;
	}

}
