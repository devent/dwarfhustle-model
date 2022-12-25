package com.anrisoftware.dwarfhustle.model.api;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Metal ore material type.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MetalOre extends Material{

	private static final long serialVersionUID = 7612699665527178770L;

	public MetalOre(int id, String name, float meltingPoint, float density, float specificHeatCapacity,
			float thermalConductivity) {
		super(id, name, meltingPoint, density, specificHeatCapacity, thermalConductivity);
	}

}
