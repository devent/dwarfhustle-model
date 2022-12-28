package com.anrisoftware.dwarfhustle.model.api;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Upper layer of soil. It has the highest concentration of organic matter and
 * microorganisms and is where most of the Earth's biological soil activity
 * occurs.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Topsoil extends Soil {

	private static final long serialVersionUID = -8313103572923248267L;

	public static final String TYPE = "Topsoil";

	public Topsoil(int id, String name, float meltingPoint, float density, float specificHeatCapacity,
			float thermalConductivity) {
		super(id, name, meltingPoint, density, specificHeatCapacity, thermalConductivity);
	}

}
