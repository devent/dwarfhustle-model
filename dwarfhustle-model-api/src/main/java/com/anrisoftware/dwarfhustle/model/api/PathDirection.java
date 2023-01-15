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

/**
 * {@link Path} direction to connect two {@link MapTile} map tiles together.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public enum PathDirection {

	/**
	 * Up.
	 */
	U,

	/**
	 * Down.
	 */
	D,

	/**
	 * North.
	 */
	N,

	/**
	 * North east.
	 */
	NE,

	/**
	 * East.
	 */
	E,

	/**
	 * South east.
	 */
	SE,

	/**
	 * South.
	 */
	S,

	/**
	 * South west.
	 */
	SW,

	/**
	 * West.
	 */
	W,

	/**
	 * North west.
	 */
	NW,

	/**
	 * Up north.
	 */
	UN,

	/**
	 * Up north east.
	 */
	UNE,

	/**
	 * Up east.
	 */
	UE,

	/**
	 * Up south east.
	 */
	USE,

	/**
	 * Up south.
	 */
	US,

	/**
	 * Up south west.
	 */
	USW,

	/**
	 * Up west.
	 */
	UW,

	/**
	 * Up north west.
	 */
	UNW,

	/**
	 * Down north.
	 */
	DN,

	/**
	 * Down north east.
	 */
	DNE,

	/**
	 * Down east.
	 */
	DE,

	/**
	 * Down south east.
	 */
	DSE,

	/**
	 * Down south.
	 */
	DS,

	/**
	 * Down south west.
	 */
	DSW,

	/**
	 * Down west.
	 */
	DW,

	/**
	 * Down north west.
	 */
	DNW,
}
