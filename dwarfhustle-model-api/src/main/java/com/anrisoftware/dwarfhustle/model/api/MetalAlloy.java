package com.anrisoftware.dwarfhustle.model.api;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Metal alloy material type.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MetalAlloy extends Material {

	private static final long serialVersionUID = 8455084483504021084L;

	public MetalAlloy(int id, String name, float meltingPoint, float density, float specificHeatCapacity,
			float thermalConductivity) {
		super(id, name, meltingPoint, density, specificHeatCapacity, thermalConductivity);
	}

}
