/*
 * dwarfhustle-model-api - Manages the compile dependencies for the model.
 * Copyright © 2022 Erwin Müller (erwin.mueller@anrisoftware.com)
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
package com.anrisoftware.dwarfhustle.model.api;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Game object on the game map.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class GameObject implements Serializable {

	private static final long serialVersionUID = 8715034096848467783L;

	public static final String TYPE = "GameObject";

	/**
	 * Converts the byte array to an Id.
	 */
	public static long toId(byte[] buf) {
		return ((buf[7] & 0xFFL) << 56) | //
				((buf[6] & 0xFFL) << 48) | //
				((buf[5] & 0xFFL) << 40) | //
				((buf[4] & 0xFFL) << 32) | //
				((buf[3] & 0xFFL) << 24) | //
				((buf[2] & 0xFFL) << 16) | //
				((buf[1] & 0xFFL) << 8) | //
				((buf[0] & 0xFFL) << 0);
	}

	/**
	 * Unique ID of the object.
	 */
	@EqualsAndHashCode.Include
	private final long id;

	/**
	 * Record ID set after the object was once stored in the database.
	 */
	private Serializable rid;

	/**
	 * Set to true if the object had changed state and should be stored in the
	 * database.
	 */
	private boolean dirty = false;

	/**
	 * X, Y and Z position of a {@link GameObject} on the game map.
	 */
	private GameMapPosition pos = new GameMapPosition(-1, -1, -1);

	public GameObject(long id) {
		this.id = id;
	}

	public GameObject(byte[] idbuf) {
		this(toId(idbuf));
	}

	public String getType() {
		return TYPE;
	}

	public void setPos(GameMapPosition pos) {
		if (!this.pos.equals(pos)) {
			setDirty(true);
			this.pos = pos;
		}
	}
}
