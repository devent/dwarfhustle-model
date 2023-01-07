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

import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.primitive.ImmutableObjectLongMap;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Tile on the game map.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class MapBlock extends GameObject {

	private static final long serialVersionUID = -5242372790621414364L;

	public static final String OBJECT_TYPE = MapBlock.class.getSimpleName();

	private ImmutableObjectLongMap<GameMapPos> blocks;

	private ImmutableMap<GameMapPos, MapTile> tiles;

	private GameBlockPos pos = new GameBlockPos();

	public MapBlock(long id) {
		super(id);
	}

	public MapBlock(byte[] idbuf) {
		super(idbuf);
	}

	public MapBlock(long id, GameBlockPos pos) {
		super(id);
		this.pos = pos;
	}

	public MapBlock(byte[] idbuf, GameBlockPos pos) {
		super(idbuf);
		this.pos = pos;
	}

	@Override
	public String getObjectType() {
		return OBJECT_TYPE;
	}

	/**
	 * Sets the X, Y and Z start position and end position of a {@link MapBlock} on
	 * the game map.
	 */
	public void setPos(GameBlockPos pos) {
		if (!this.pos.equals(pos)) {
			setDirty(true);
			this.pos = pos;
		}
	}

	public GameMapPos getEndPos() {
		return pos.getEndPos();
	}
}
