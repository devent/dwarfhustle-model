/*
 * dwarfhustle-model-api - Manages the compile dependencies for the model.
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
package com.anrisoftware.dwarfhustle.model.api;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * X, Y and Z position of a {@link GameObject} on the game map.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Getter
public class GameMapPos implements Serializable {

	private static final long serialVersionUID = 8842532533035915317L;

	/**
	 * The game map id.
	 */
	private int mapid = -1;

	/**
	 * X position on the game map
	 */
	private int x = -1;

	/**
	 * Y position on the game map
	 */
	private int y = -1;

	/**
	 * Z position on the game map
	 */
	private int z = -1;

	public int getDiffX(GameMapPos pos) {
		return x - pos.x;
	}

	public int getDiffY(GameMapPos pos) {
		return y - pos.y;
	}

	public int getDiffZ(GameMapPos pos) {
		return z - pos.z;
	}

	/**
	 * Returns string that can be used to store the block position.
	 */
	public String toSaveString() {
		return getMapid() + "/" + getX() + "/" + getY() + "/" + getZ();
	}

	/**
	 * Returns the {@link GameMapPos} parsed from the string.
	 */
	public static GameMapPos parse(String s) {
		var split = StringUtils.split(s, "/");
		var pos = new GameMapPos(toInt(split[0]), toInt(split[1]), toInt(split[2]), toInt(split[3]));
		return pos;
	}

	private static int toInt(String s) {
		return Integer.parseInt(s);
	}
}
