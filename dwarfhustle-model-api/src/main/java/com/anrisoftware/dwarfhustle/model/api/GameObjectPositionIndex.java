package com.anrisoftware.dwarfhustle.model.api;

import java.io.Serializable;

import lombok.Data;

/**
 * {@link GameObject} index over the object's type and the X, Y and Z position
 * on the game map.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Data
public class GameObjectPositionIndex implements Serializable {

	private static final long serialVersionUID = 8842532533035915317L;

	/**
	 * The type of the {@link GameObject}.
	 */
	private final String type;

	/**
	 * X, Y and Z position of a {@link GameObject} on the game map.
	 */
	private final GameMapPosition pos;

}
