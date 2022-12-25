package com.anrisoftware.dwarfhustle.model.api;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * X, Y and Z position of a {@link GameObject} on the game map.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameMapPosition implements Serializable {

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

}
