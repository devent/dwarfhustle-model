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
