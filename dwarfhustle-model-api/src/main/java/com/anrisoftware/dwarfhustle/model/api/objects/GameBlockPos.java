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
package com.anrisoftware.dwarfhustle.model.api.objects;

import org.apache.commons.lang3.StringUtils;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Start position and end position and a {@link MapBlock}.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Getter
public class GameBlockPos extends GameMapPos {

	private static final long serialVersionUID = -6712721104948565260L;

	private GameMapPos endPos = new GameMapPos();

	public GameBlockPos(int mapid, int x, int y, int z, int ex, int ey, int ez) {
		this(new GameMapPos(mapid, x, y, z), new GameMapPos(mapid, ex, ey, ez));
	}

	public GameBlockPos(GameMapPos pos, GameMapPos endPos) {
		super(pos.getMapid(), pos.getX(), pos.getY(), pos.getZ());
		this.endPos = endPos;
	}

	/**
	 * Returns string that can be used to store the block position.
	 */
	@Override
	public String toSaveString() {
		return super.toSaveString() + "/" + endPos.getX() + "/" + endPos.getY() + "/" + endPos.getZ();
	}

	/**
	 * Returns the {@link GameBlockPos} parsed from the string.
	 */
	public static GameBlockPos parse(String s) {
		var split = StringUtils.split(s, "/");
		var pos = new GameMapPos(toInt(split[0]), toInt(split[1]), toInt(split[2]), toInt(split[3]));
		var ep = new GameMapPos(toInt(split[0]), toInt(split[4]), toInt(split[5]), toInt(split[6]));
		return new GameBlockPos(pos, ep);
	}

	private static int toInt(String s) {
		return Integer.parseInt(s);
	}
}
