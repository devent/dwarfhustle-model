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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Game object on the game map.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class GameMapObject extends GameObject {

	private static final long serialVersionUID = 8715034096848467783L;

	public static final String OBJECT_TYPE = GameMapObject.class.getSimpleName();

	private GameMapPos pos = new GameMapPos();

	/**
	 * Record ID set after the object was once stored in the database.
	 */
	private Serializable rid = null;

	public GameMapObject(long id) {
		super(id);
	}

	public GameMapObject(byte[] idbuf) {
		super(idbuf);
	}

	public GameMapObject(long id, GameMapPos pos) {
		super(id);
		this.pos = pos;
	}

	public GameMapObject(byte[] idbuf, GameMapPos pos) {
		super(idbuf);
		this.pos = pos;
	}

	@Override
	public String getObjectType() {
		return OBJECT_TYPE;
	}

	/**
	 * Sets the X, Y and Z position of a {@link GameMapObject} on the game map.
	 */
	public void setPos(GameMapPos pos) {
		if (!this.pos.equals(pos)) {
			setDirty(true);
			this.pos = pos;
		}
	}
}
