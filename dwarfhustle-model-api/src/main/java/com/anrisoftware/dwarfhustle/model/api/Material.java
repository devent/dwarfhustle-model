package com.anrisoftware.dwarfhustle.model.api;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Material what stuff is made of.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Material implements Serializable {

	private static final long serialVersionUID = -6527071336221066901L;

	public static final String TYPE = "Material";

	@EqualsAndHashCode.Include
	public final int id;

	public final String name;

	public final float meltingPoint;

	public final float density;

	public final float specificHeatCapacity;

	public final float thermalConductivity;
}
