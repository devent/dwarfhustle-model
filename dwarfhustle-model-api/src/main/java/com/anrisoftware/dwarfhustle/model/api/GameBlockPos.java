package com.anrisoftware.dwarfhustle.model.api;

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
}
