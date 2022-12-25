package com.anrisoftware.dwarfhustle.model.api;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Stone that creates entire layers.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class StoneLayer extends Stone {

	private static final long serialVersionUID = -6750889966414477986L;

	public StoneLayer(int id, String name, float meltingPoint, float density, float specificHeatCapacity,
			float thermalConductivity) {
		super(id, name, meltingPoint, density, specificHeatCapacity, thermalConductivity);
	}

}
