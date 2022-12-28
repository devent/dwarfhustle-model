package com.anrisoftware.dwarfhustle.model.api;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Granular material composed of finely divided mineral particles. Sand has
 * various compositions but is defined by its grain size. Sand grains are
 * smaller than gravel and coarser than silt.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Sand extends Soil {

	private static final long serialVersionUID = -1399286827364690668L;

	public static final String TYPE = "Sand";

	public Sand(int id, String name, float meltingPoint, float density, float specificHeatCapacity,
			float thermalConductivity) {
		super(id, name, meltingPoint, density, specificHeatCapacity, thermalConductivity);
	}

}
