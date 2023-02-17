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

import org.eclipse.collections.api.map.MapIterable;
import org.eclipse.collections.api.map.primitive.ObjectLongMap;
import org.eclipse.collections.impl.factory.Maps;
import org.eclipse.collections.impl.factory.primitive.ObjectLongMaps;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Tile on the game map.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Getter
public class MapBlock extends GameObject {

	private static final long serialVersionUID = -5242372790621414364L;

	public static final String OBJECT_TYPE = MapBlock.class.getSimpleName();

	private ObjectLongMap<GameBlockPos> blocks = ObjectLongMaps.immutable.empty();

	private MapIterable<GameMapPos, MapTile> tiles = Maps.immutable.empty();

	private GameBlockPos pos = new GameBlockPos();

	private boolean root = false;

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

	public void setBlocks(ObjectLongMap<GameBlockPos> blocks) {
		this.blocks = blocks;
		setDirty(true);
	}

	public void setTiles(MapIterable<GameMapPos, MapTile> tiles) {
		this.tiles = tiles;
		setDirty(true);
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

	public GameMapPos getStartPos() {
		return pos;
	}

	public GameMapPos getEndPos() {
		return pos.getEndPos();
	}

	public float getWidth() {
		return getEndPos().getDiffX(getStartPos());
	}

	public float getHeight() {
		return getEndPos().getDiffY(getStartPos());
	}

	public float getDepth() {
		return getEndPos().getDiffZ(getStartPos());
	}

	/**
	 * Sets that this block is the top most block.
	 */
	public void setRoot(boolean root) {
		if (this.root != root) {
			setDirty(true);
			this.root = root;
		}
	}

}
