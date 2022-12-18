package com.anrisoftware.dwarfhustle.model.db.orientdb.actor;

import akka.actor.typed.ActorRef;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Message to connect to a OrientDb database.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ConnectDbMessage extends AbstractDbReplyMessage {

	public final String url;

	public final String database;

	public final String user;

	public final String password;

	public ConnectDbMessage(ActorRef<DbResponseMessage> replyTo, String url, String database, String user,
			String password) {
		super(replyTo);
		this.url = url;
		this.database = database;
		this.user = user;
		this.password = password;
	}

}
