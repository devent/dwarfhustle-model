package com.anrisoftware.dwarfhustle.model.db.orientdb.actor;

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.orientechnologies.orient.core.db.OrientDB;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Database success response.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@RequiredArgsConstructor
@ToString(callSuper = true)
public class ConnectDbSuccessMessage extends DbResponseMessage {

	@ToString.Exclude
	public final Message om;

	public final OrientDB db;
}
