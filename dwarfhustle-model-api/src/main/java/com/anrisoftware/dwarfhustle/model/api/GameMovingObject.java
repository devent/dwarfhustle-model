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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Object that can move on the game map.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Getter
public class GameMovingObject extends GameMapObject {

	private static final long serialVersionUID = -2588184910010763410L;

	public static final String OBJECT_TYPE = GameMovingObject.class.getSimpleName();

	private float xx;

	private float xy;

	private float xz;

	private float vx;

	private float vy;

	private float vz;

	private float rx;

	private float ry;

	private float rz;

	private float rw;

	public GameMovingObject(long id) {
		super(id);
	}

	public GameMovingObject(byte[] idbuf) {
		super(idbuf);
	}

	@Override
	public String getObjectType() {
		return OBJECT_TYPE;
	}

	public void setXx(float xx) {
		if (this.xx != xx) {
			this.xx = xx;
			setDirty(true);
		}
	}

	public void setXy(float xy) {
		if (this.xy != xy) {
			this.xy = xy;
			setDirty(true);
		}
	}

	public void setXz(float xz) {
		if (this.xz != xz) {
			this.xz = xz;
			setDirty(true);
		}
	}

	public void setVx(float vx) {
		if (this.vx != vx) {
			this.vx = vx;
			setDirty(true);
		}
	}

	public void setVy(float vy) {
		if (this.vy != vy) {
			this.vy = vy;
			setDirty(true);
		}
	}

	public void setVz(float vz) {
		if (this.vz != vz) {
			this.vz = vz;
			setDirty(true);
		}
	}

	public void setRx(float rx) {
		if (this.rx != rx) {
			this.rx = rx;
			setDirty(true);
		}
	}

	public void setRy(float ry) {
		if (this.ry != ry) {
			this.ry = ry;
			setDirty(true);
		}
	}

	public void setRz(float rz) {
		if (this.rz != rz) {
			this.rz = rz;
			setDirty(true);
		}
	}

	public void setRw(float rw) {
		if (this.rw != rw) {
			this.rw = rw;
			setDirty(true);
		}
	}

}
