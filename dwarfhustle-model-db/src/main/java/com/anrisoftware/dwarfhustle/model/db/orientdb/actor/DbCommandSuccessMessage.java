package com.anrisoftware.dwarfhustle.model.db.orientdb.actor;

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.db.orientdb.actor.DbResponseMessage.DbSuccessMessage;

import lombok.ToString;

/**
 * Database command success response with return value.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@ToString(callSuper = true)
public class DbCommandSuccessMessage extends DbSuccessMessage {

	public final Object value;

	public DbCommandSuccessMessage(Message om, Object value) {
		super(om);
		this.value = value;
	}
}
