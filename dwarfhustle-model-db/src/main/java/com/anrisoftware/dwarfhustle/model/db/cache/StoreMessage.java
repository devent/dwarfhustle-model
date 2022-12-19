package com.anrisoftware.dwarfhustle.model.db.cache;

import com.anrisoftware.dwarfhustle.model.actor.MessageActor.Message;
import com.anrisoftware.dwarfhustle.model.api.GameObject;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Message to store a {@link GameObject} in the database.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class StoreMessage extends Message {

	public final Object key;

	public final GameObject go;

	@Data
	@EqualsAndHashCode(callSuper = false)
	public static class StoreDoneMessage extends Message {

	}
}
