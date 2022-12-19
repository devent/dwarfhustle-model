package com.anrisoftware.dwarfhustle.model.api;

import java.io.Serializable;

import lombok.Data;

/**
 * X, Y and Z position of a {@link GameObject} on the game map.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Data
public class GameMapPosition implements Serializable {

	private static final long serialVersionUID = 8842532533035915317L;

	/**
	 * X position on the game map
	 */
	private final int x;

	/**
	 * Y position on the game map
	 */
	private final int y;

	/**
	 * Z position on the game map
	 */
	private final int z;

}
