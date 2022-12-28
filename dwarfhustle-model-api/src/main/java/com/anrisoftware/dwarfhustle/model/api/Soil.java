package com.anrisoftware.dwarfhustle.model.api;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Soil material.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Soil extends Material {

	private static final long serialVersionUID = -5079031705039022133L;

	public static final String TYPE = "Soil";

	public Soil(int id, String name, float meltingPoint, float density, float specificHeatCapacity,
			float thermalConductivity) {
		super(id, name, meltingPoint, density, specificHeatCapacity, thermalConductivity);
	}

}
