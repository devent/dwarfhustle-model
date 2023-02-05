package com.anrisoftware.dwarfhustle.model.api.objects;

import java.io.Serializable;

import lombok.Data;

/**
 * Rectangular map area from the north-west corner to the south-east corner.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Data
public class MapArea implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates {@link MapArea} from the north-west corner to the south-east corner.
	 */
	public static MapArea create(float nwlat, float nwlon, float selat, float selon) {
		return new MapArea(new MapCoordinate(nwlat, nwlon), new MapCoordinate(selat, selon));
	}

	/**
	 * North-west corner.
	 */
	public final MapCoordinate nw;

	/**
	 * South-east corner.
	 */
	public final MapCoordinate se;
}
