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

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Game object on the game map.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class GameObject implements Serializable {

	private static final long serialVersionUID = 8715034096848467783L;

	public static final String TYPE = "GameObject";

	@EqualsAndHashCode.Include
	public Object id;

	public boolean dirty = false;

	public int x;

	public int y;

	public int z;

	public String getType() {
		return TYPE;
	}

	public void setX(int x) {
		if (this.x != x) {
			setDirty(true);
			this.x = x;
		}
	}

	public void setY(int y) {
		if (this.y != y) {
			setDirty(true);
			this.y = y;
		}
	}

	public void setZ(int z) {
		if (this.z != z) {
			setDirty(true);
			this.z = z;
		}
	}
}
