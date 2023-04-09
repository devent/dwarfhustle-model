/*
 * dwarfhustle-model-db - Manages the compile dependencies for the model.
 * Copyright © 2023 Erwin Müller (erwin.mueller@anrisoftware.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
