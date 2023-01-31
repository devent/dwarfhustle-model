package com.anrisoftware.dwarfhustle.model.db.orientdb.objects;

import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;

/**
 * Thrown on errors loading {@link GameObject} from the database that are not
 * related to the database, like if the object should be in the database but was
 * not found.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public class LoadObjectException extends Exception {

	private static final long serialVersionUID = 1L;

	public LoadObjectException(String message, Throwable cause) {
		super(message, cause);
	}

	public LoadObjectException(String message) {
		super(message);
	}

}
