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

	/**
	 * Calculates the total count of {@link MapBlock} blocks for the specified
	 * width, height, depth and block size.
	 * <ul>
	 * <li>16x16x16 4 = 72+1
	 * <li>32x32x32 4 = 584+1
	 * <li>64x64x64 4 = 4680+1
	 * <li>128x128x128 4 = 37448+1
	 * <li>256x256x256 4 = 299592+1
	 * </ul>
	 * <ul>
	 * <li>16x16x16 8 = 8+1
	 * <li>32x32x32 8 = 72+1
	 * <li>64x64x64 8 = 584+1
	 * <li>128x128x128 8 = 4680+1
	 * <li>256x256x256 8 = 37448+1
	 * </ul>
	 */
	public static int calculateBlocksCount(int width, int height, int depth, int size) {
		int blocks = 1;
		int w = width;
		int h = height;
		int d = depth;
		while (true) {
			if (w < 8 || h < 8 || d < 8) {
				break;
			}
			blocks += w * h * d / (size * size * size);
			w /= 2;
			h /= 2;
			d /= 2;
		}
		return blocks;
	}

	private static final long serialVersionUID = -5242372790621414364L;

	public static final String OBJECT_TYPE = MapBlock.class.getSimpleName();

	private ObjectLongMap<GameBlockPos> blocks = ObjectLongMaps.immutable.empty();

	private MapIterable<GameMapPos, MapTile> tiles = Maps.immutable.empty();

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

	public GameMapPos getEndPos() {
		return pos.getEndPos();
	}
}
